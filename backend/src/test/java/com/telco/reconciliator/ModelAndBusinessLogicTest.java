package com.telco.reconciliator;

import com.telco.reconciliator.model.Customer;
import com.telco.reconciliator.model.Invoice;
import com.telco.reconciliator.model.InvoiceItem;
import com.telco.reconciliator.model.ServicePlan;
import com.telco.reconciliator.model.ConsumptionRecord;
import com.telco.reconciliator.model.ReconciliationResult;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * Additional unit tests for the TelcoRec domain model.
 * Covers customer-invoice relationships, overage cost calculations,
 * invoice item arithmetic, and reconciliation edge cases.
 */
public class ModelAndBusinessLogicTest {

    // ------------------------------------------------------------------
    // Customer–Invoice relationship tests
    // ------------------------------------------------------------------

    @Test
    public void testCustomerInvoiceRelationship() {
        Customer c = new Customer();
        c.setCustomerId("CUST-000010");
        c.setFullName("Giulia Bianchi");
        c.setCity("Roma");
        c.setStatus("ACTIVE");

        Invoice inv = new Invoice();
        inv.setInvoiceNumber("INV-2025-00000050");
        inv.setCustomer(c);
        inv.setTotalAmount(new BigDecimal("29.99"));
        c.getInvoices().add(inv);

        assertEquals(1, c.getInvoices().size());
        assertSame(c, inv.getCustomer());
        assertEquals("CUST-000010", inv.getCustomer().getCustomerId());
    }

    @Test
    public void testCustomerMultipleInvoices() {
        Customer c = new Customer();
        c.setCustomerId("CUST-000011");
        c.setFullName("Luca Ferrari");

        for (int i = 1; i <= 3; i++) {
            Invoice inv = new Invoice();
            inv.setInvoiceNumber("INV-2025-" + String.format("%08d", i));
            inv.setCustomer(c);
            inv.setTotalAmount(new BigDecimal("19.99"));
            c.getInvoices().add(inv);
        }

        assertEquals(3, c.getInvoices().size());
    }

    // ------------------------------------------------------------------
    // Service plan overage calculation tests
    // ------------------------------------------------------------------

    @Test
    public void testOverageDataCostCalculation() {
        ServicePlan plan = new ServicePlan();
        plan.setPlanCode("MOBILE-BASE");
        plan.setMonthlyFee(new BigDecimal("9.99"));
        plan.setIncludedDataMb(5120L);  // 5 GB
        plan.setExtraDataCostPerMb(new BigDecimal("0.0200"));

        long actualDataMb = 7168L;  // 7 GB
        long overageMb = actualDataMb - plan.getIncludedDataMb();
        BigDecimal overageCost = new BigDecimal(overageMb)
                .multiply(plan.getExtraDataCostPerMb())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = plan.getMonthlyFee().add(overageCost);

        assertEquals(2048L, overageMb);
        assertEquals(0, new BigDecimal("40.96").compareTo(overageCost));
        assertEquals(0, new BigDecimal("50.95").compareTo(expectedTotal));
    }

    @Test
    public void testOverageVoiceCostCalculation() {
        ServicePlan plan = new ServicePlan();
        plan.setPlanCode("MOBILE-PLUS");
        plan.setMonthlyFee(new BigDecimal("19.99"));
        plan.setIncludedVoiceMinutes(500);
        plan.setExtraVoiceCostPerMinute(new BigDecimal("0.1500"));

        int actualMinutes = 620;
        int overageMinutes = actualMinutes - plan.getIncludedVoiceMinutes();
        BigDecimal overageCost = new BigDecimal(overageMinutes)
                .multiply(plan.getExtraVoiceCostPerMinute())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = plan.getMonthlyFee().add(overageCost);

        assertEquals(120, overageMinutes);
        assertEquals(0, new BigDecimal("18.00").compareTo(overageCost));
        assertEquals(0, new BigDecimal("37.99").compareTo(expectedTotal));
    }

    @Test
    public void testNoOverageWhenWithinPlan() {
        ServicePlan plan = new ServicePlan();
        plan.setPlanCode("FIBRA-ULTRA");
        plan.setMonthlyFee(new BigDecimal("49.99"));
        plan.setIncludedDataMb(102400L);  // 100 GB
        plan.setExtraDataCostPerMb(new BigDecimal("0.0500"));
        plan.setIncludedVoiceMinutes(999999);

        long actualDataMb = 50000L;  // 50 GB (within plan)
        BigDecimal total = plan.getMonthlyFee();
        if (actualDataMb > plan.getIncludedDataMb()) {
            long overageMb = actualDataMb - plan.getIncludedDataMb();
            total = total.add(new BigDecimal(overageMb).multiply(plan.getExtraDataCostPerMb()));
        }

        assertEquals(0, new BigDecimal("49.99").compareTo(total));
    }

    @Test
    public void testRoamingSurcharge() {
        ServicePlan plan = new ServicePlan();
        plan.setPlanCode("MOBILE-PLUS");
        plan.setMonthlyFee(new BigDecimal("19.99"));
        plan.setRoamingEnabled(true);

        long roamingDataMb = 200L;
        BigDecimal roamingCost = BigDecimal.ZERO;
        if (roamingDataMb > 0 && Boolean.TRUE.equals(plan.getRoamingEnabled())) {
            roamingCost = new BigDecimal(roamingDataMb).multiply(new BigDecimal("0.05"));
        }

        assertEquals(0, new BigDecimal("10.00").compareTo(roamingCost));
    }

    // ------------------------------------------------------------------
    // Invoice item tests
    // ------------------------------------------------------------------

    @Test
    public void testInvoiceItemTypes() {
        String[] validTypes = {
            "SUBSCRIPTION_FEE", "EXTRA_DATA", "EXTRA_VOICE",
            "ROAMING", "PREMIUM_SMS", "EQUIPMENT_RENTAL",
            "ACTIVATION_FEE", "DISCOUNT"
        };

        for (String type : validTypes) {
            InvoiceItem item = new InvoiceItem();
            item.setItemType(type);
            assertEquals(type, item.getItemType());
        }
    }

    @Test
    public void testInvoiceItemDateRange() {
        InvoiceItem item = new InvoiceItem();
        item.setServiceStartDate(LocalDate.of(2025, 10, 1));
        item.setServiceEndDate(LocalDate.of(2025, 10, 31));

        assertFalse(item.getServiceEndDate().isBefore(item.getServiceStartDate()));
    }

    // ------------------------------------------------------------------
    // Consumption record tests
    // ------------------------------------------------------------------

    @Test
    public void testConsumptionRecordSourceValues() {
        ConsumptionRecord cdr1 = new ConsumptionRecord();
        cdr1.setSource("NETWORK_CDR");
        assertEquals("NETWORK_CDR", cdr1.getSource());

        ConsumptionRecord cdr2 = new ConsumptionRecord();
        cdr2.setSource("BILLING_SYSTEM");
        assertEquals("BILLING_SYSTEM", cdr2.getSource());
    }

    @Test
    public void testConsumptionRecordNullFields() {
        ConsumptionRecord cdr = new ConsumptionRecord();
        assertNull(cdr.getActualVoiceMinutes());
        assertNull(cdr.getActualDataMb());
        assertNull(cdr.getRoamingDataMb());
        assertNull(cdr.getPremiumSmsCount());
    }

    // ------------------------------------------------------------------
    // Reconciliation result edge case tests
    // ------------------------------------------------------------------

    @Test
    public void testReconciliationUnderchargedStatus() {
        BigDecimal invoiced = new BigDecimal("15.00");
        BigDecimal expected = new BigDecimal("29.99");
        BigDecimal diff = invoiced.subtract(expected).setScale(2, RoundingMode.HALF_UP);

        assertTrue("Diff should be negative for undercharged", diff.compareTo(BigDecimal.ZERO) < 0);

        ReconciliationResult result = new ReconciliationResult();
        result.setStatus("UNDERCHARGED");
        result.setInvoicedAmount(invoiced);
        result.setExpectedAmount(expected);
        result.setDiscrepancyAmount(diff);

        assertEquals("UNDERCHARGED", result.getStatus());
        assertEquals(0, new BigDecimal("-14.99").compareTo(result.getDiscrepancyAmount()));
    }

    @Test
    public void testReconciliationMissingConsumption() {
        ReconciliationResult result = new ReconciliationResult();
        result.setStatus("MISSING_CONSUMPTION");
        result.setInvoicedAmount(new BigDecimal("29.99"));
        result.setExpectedAmount(BigDecimal.ZERO);
        result.setDiscrepancyAmount(new BigDecimal("29.99"));
        result.setDiscrepancyDetails("No CDR consumption record found for this billing period.");

        assertEquals("MISSING_CONSUMPTION", result.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getExpectedAmount()));
        assertTrue(result.getDiscrepancyDetails().contains("No CDR"));
    }

    @Test
    public void testReconciliationResultResolution() {
        ReconciliationResult result = new ReconciliationResult();
        result.setStatus("OVERCHARGED");
        result.setProcessedAt(LocalDateTime.of(2025, 11, 1, 10, 0));

        assertNull(result.getResolvedAt());
        assertNull(result.getResolvedBy());

        result.setResolvedAt(LocalDateTime.of(2025, 11, 5, 14, 30));
        result.setResolvedBy("admin@telcocorp.it");

        assertNotNull(result.getResolvedAt());
        assertEquals("admin@telcocorp.it", result.getResolvedBy());
        assertTrue(result.getResolvedAt().isAfter(result.getProcessedAt()));
    }

    @Test
    public void testReconciliationToleranceEdgeCase() {
        // Exactly at tolerance boundary: diff of 0.01
        BigDecimal invoiced = new BigDecimal("20.00");
        BigDecimal expected = new BigDecimal("19.99");
        BigDecimal diff = invoiced.subtract(expected).abs();

        assertTrue("0.01 should be within tolerance",
                diff.compareTo(new BigDecimal("0.01")) <= 0);
    }

    @Test
    public void testReconciliationJustOverTolerance() {
        // Just above tolerance: diff of 0.02
        BigDecimal invoiced = new BigDecimal("20.01");
        BigDecimal expected = new BigDecimal("19.99");
        BigDecimal diff = invoiced.subtract(expected).abs();

        assertFalse("0.02 should exceed tolerance",
                diff.compareTo(new BigDecimal("0.01")) <= 0);
    }

    // ------------------------------------------------------------------
    // Invoice billing period tests
    // ------------------------------------------------------------------

    @Test
    public void testInvoiceBillingPeriodCoversFullMonth() {
        Invoice inv = new Invoice();
        inv.setInvoiceNumber("INV-2025-00000100");
        inv.setBillingPeriodStart(LocalDate.of(2025, 10, 1));
        inv.setBillingPeriodEnd(LocalDate.of(2025, 10, 31));
        inv.setIssueDate(LocalDate.of(2025, 11, 1));
        inv.setDueDate(LocalDate.of(2025, 11, 30));

        assertTrue(inv.getDueDate().isAfter(inv.getIssueDate()));
        assertEquals(10, inv.getBillingPeriodStart().getMonthValue());
        assertEquals(10, inv.getBillingPeriodEnd().getMonthValue());
    }

    @Test
    public void testInvoiceStatusTransitions() {
        Invoice inv = new Invoice();
        inv.setStatus("PENDING");
        inv.setReconciliationStatus("UNPROCESSED");

        assertEquals("PENDING", inv.getStatus());
        assertEquals("UNPROCESSED", inv.getReconciliationStatus());

        inv.setReconciliationStatus("MATCHED");
        assertEquals("MATCHED", inv.getReconciliationStatus());

        inv.setReconciliationStatus("DISCREPANCY_FOUND");
        assertEquals("DISCREPANCY_FOUND", inv.getReconciliationStatus());
    }

    @Test
    public void testInvoiceNotes() {
        Invoice inv = new Invoice();
        assertNull(inv.getNotes());

        inv.setNotes("Customer disputed this invoice via call center");
        assertTrue(inv.getNotes().contains("disputed"));
    }

    // ------------------------------------------------------------------
    // Customer field tests
    // ------------------------------------------------------------------

    @Test
    public void testCustomerAllStatuses() {
        for (String status : new String[]{"ACTIVE", "SUSPENDED", "CHURNED"}) {
            Customer c = new Customer();
            c.setStatus(status);
            assertEquals(status, c.getStatus());
        }
    }

    @Test
    public void testCustomerContactInfo() {
        Customer c = new Customer();
        c.setPhoneNumber("+39 347 1234567");
        c.setEmail("test@telcocorp.it");
        c.setAddress("Via Roma 123");
        c.setCity("Milano");

        assertTrue(c.getPhoneNumber().startsWith("+39"));
        assertTrue(c.getEmail().contains("@"));
        assertEquals("Milano", c.getCity());
    }
}
