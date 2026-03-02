package com.telco.reconciliator;

import com.telco.reconciliator.model.ConsumptionRecord;
import com.telco.reconciliator.model.Customer;
import com.telco.reconciliator.model.Invoice;
import com.telco.reconciliator.model.InvoiceItem;
import com.telco.reconciliator.model.ReconciliationResult;
import com.telco.reconciliator.model.ServicePlan;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Extended unit tests for domain model and business-logic rules.
 * Covers entity relationships, edge cases, and reconciliation arithmetic.
 */
public class ModelAndBusinessLogicTest {

    // ------------------------------------------------------------------
    // Customer entity tests
    // ------------------------------------------------------------------

    @Test
    public void testCustomerDefaultCountryIsItaly() {
        Customer c = new Customer();
        assertEquals("Italy", c.getCountry());
    }

    @Test
    public void testCustomerStatusValues() {
        Customer c = new Customer();
        for (String status : new String[]{"ACTIVE", "SUSPENDED", "CHURNED"}) {
            c.setStatus(status);
            assertEquals(status, c.getStatus());
        }
    }

    @Test
    public void testCustomerInvoicesListInitialisedEmpty() {
        Customer c = new Customer();
        assertNotNull(c.getInvoices());
        assertTrue(c.getInvoices().isEmpty());
    }

    @Test
    public void testCustomerSetAllFields() {
        Customer c = new Customer();
        c.setId(1L);
        c.setCustomerId("CUST-000099");
        c.setFullName("Test User");
        c.setEmail("test@example.com");
        c.setPhoneNumber("+39 000 1234567");
        c.setAddress("Via Test 1");
        c.setCity("Milano");
        c.setCountry("Italy");
        c.setServicePlan("MOBILE-BASIC");
        c.setContractStartDate(LocalDate.of(2024, 6, 1));

        assertEquals(Long.valueOf(1L), c.getId());
        assertEquals("CUST-000099", c.getCustomerId());
        assertEquals("test@example.com", c.getEmail());
        assertEquals("+39 000 1234567", c.getPhoneNumber());
        assertEquals("Via Test 1", c.getAddress());
        assertEquals("Milano", c.getCity());
        assertEquals("MOBILE-BASIC", c.getServicePlan());
        assertEquals(LocalDate.of(2024, 6, 1), c.getContractStartDate());
    }

    @Test
    public void testCustomerInvoicesCanBeSet() {
        Customer c = new Customer();
        ArrayList<Invoice> invoices = new ArrayList<>();
        invoices.add(new Invoice());
        c.setInvoices(invoices);
        assertEquals(1, c.getInvoices().size());
    }

    // ------------------------------------------------------------------
    // ServicePlan entity tests
    // ------------------------------------------------------------------

    @Test
    public void testServicePlanExtraDataCost() {
        ServicePlan plan = new ServicePlan();
        plan.setExtraDataCostPerMb(new BigDecimal("0.20"));
        long overageMb = 1024;
        BigDecimal charge = new BigDecimal(overageMb)
                .multiply(plan.getExtraDataCostPerMb())
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, new BigDecimal("204.80").compareTo(charge));
    }

    @Test
    public void testServicePlanExtraVoiceCost() {
        ServicePlan plan = new ServicePlan();
        plan.setExtraVoiceCostPerMinute(new BigDecimal("0.15"));
        int overageMinutes = 60;
        BigDecimal charge = new BigDecimal(overageMinutes)
                .multiply(plan.getExtraVoiceCostPerMinute())
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, new BigDecimal("9.00").compareTo(charge));
    }

    @Test
    public void testServicePlanRoamingDisabledByDefault() {
        ServicePlan plan = new ServicePlan();
        assertNull(plan.getRoamingEnabled());
    }

    @Test
    public void testServicePlanAllFields() {
        ServicePlan p = new ServicePlan();
        p.setId(10L);
        p.setPlanCode("TEST-PLAN");
        p.setPlanName("Test Plan");
        p.setMonthlyFee(new BigDecimal("14.99"));
        p.setIncludedVoiceMinutes(200);
        p.setIncludedSmsCount(100);
        p.setIncludedDataMb(10240L);
        p.setExtraDataCostPerMb(new BigDecimal("0.30"));
        p.setExtraVoiceCostPerMinute(new BigDecimal("0.25"));
        p.setRoamingEnabled(false);
        p.setDescription("A test plan");

        assertEquals(Long.valueOf(10L), p.getId());
        assertEquals("TEST-PLAN", p.getPlanCode());
        assertEquals("Test Plan", p.getPlanName());
        assertEquals(Integer.valueOf(200), p.getIncludedVoiceMinutes());
        assertEquals(Integer.valueOf(100), p.getIncludedSmsCount());
        assertEquals(Long.valueOf(10240L), p.getIncludedDataMb());
        assertFalse(p.getRoamingEnabled());
        assertEquals("A test plan", p.getDescription());
    }

    // ------------------------------------------------------------------
    // Invoice entity tests
    // ------------------------------------------------------------------

    @Test
    public void testInvoiceDefaultReconciliationStatusIsUnprocessed() {
        Invoice inv = new Invoice();
        assertEquals("UNPROCESSED", inv.getReconciliationStatus());
    }

    @Test
    public void testInvoiceItemsListInitialisedEmpty() {
        Invoice inv = new Invoice();
        assertNotNull(inv.getItems());
        assertTrue(inv.getItems().isEmpty());
    }

    @Test
    public void testInvoiceBillingPeriod() {
        Invoice inv = new Invoice();
        inv.setBillingPeriodStart(LocalDate.of(2025, 10, 1));
        inv.setBillingPeriodEnd(LocalDate.of(2025, 10, 31));

        assertEquals(30, java.time.temporal.ChronoUnit.DAYS.between(
                inv.getBillingPeriodStart(), inv.getBillingPeriodEnd()));
    }

    @Test
    public void testInvoiceSetAllFields() {
        Invoice inv = new Invoice();
        inv.setId(5L);
        inv.setInvoiceNumber("INV-2025-00000099");
        inv.setIssueDate(LocalDate.of(2025, 11, 1));
        inv.setDueDate(LocalDate.of(2025, 12, 1));
        inv.setTotalAmount(new BigDecimal("42.50"));
        inv.setStatus("PAID");
        inv.setReconciliationStatus("MATCHED");
        inv.setNotes("Test note");

        assertEquals(Long.valueOf(5L), inv.getId());
        assertEquals("INV-2025-00000099", inv.getInvoiceNumber());
        assertEquals(LocalDate.of(2025, 11, 1), inv.getIssueDate());
        assertEquals(LocalDate.of(2025, 12, 1), inv.getDueDate());
        assertEquals(0, new BigDecimal("42.50").compareTo(inv.getTotalAmount()));
        assertEquals("PAID", inv.getStatus());
        assertEquals("MATCHED", inv.getReconciliationStatus());
        assertEquals("Test note", inv.getNotes());
    }

    // ------------------------------------------------------------------
    // InvoiceItem tests
    // ------------------------------------------------------------------

    @Test
    public void testInvoiceItemCalculation() {
        InvoiceItem item = new InvoiceItem();
        item.setQuantity(new BigDecimal("3"));
        item.setUnitPrice(new BigDecimal("10.50"));
        item.setTotalPrice(item.getQuantity().multiply(item.getUnitPrice()));
        assertEquals(0, new BigDecimal("31.50").compareTo(item.getTotalPrice()));
    }

    @Test
    public void testInvoiceItemServiceDates() {
        InvoiceItem item = new InvoiceItem();
        item.setServiceStartDate(LocalDate.of(2025, 10, 1));
        item.setServiceEndDate(LocalDate.of(2025, 10, 31));
        assertTrue(item.getServiceEndDate().isAfter(item.getServiceStartDate()));
    }

    @Test
    public void testInvoiceItemTypes() {
        InvoiceItem item = new InvoiceItem();
        String[] types = {"SUBSCRIPTION_FEE", "EXTRA_DATA", "EXTRA_VOICE",
                          "ROAMING", "PREMIUM_SMS", "EQUIPMENT_RENTAL",
                          "ACTIVATION_FEE", "DISCOUNT"};
        for (String type : types) {
            item.setItemType(type);
            assertEquals(type, item.getItemType());
        }
    }

    // ------------------------------------------------------------------
    // ConsumptionRecord tests
    // ------------------------------------------------------------------

    @Test
    public void testConsumptionRecordSources() {
        ConsumptionRecord cdr = new ConsumptionRecord();
        cdr.setSource("NETWORK_CDR");
        assertEquals("NETWORK_CDR", cdr.getSource());

        cdr.setSource("BILLING_SYSTEM");
        assertEquals("BILLING_SYSTEM", cdr.getSource());
    }

    @Test
    public void testConsumptionRecordAllFields() {
        ConsumptionRecord cdr = new ConsumptionRecord();
        cdr.setId(1L);
        cdr.setCustomerId("CUST-000001");
        cdr.setBillingPeriodStart(LocalDate.of(2025, 10, 1));
        cdr.setBillingPeriodEnd(LocalDate.of(2025, 10, 31));
        cdr.setActualVoiceMinutes(450);
        cdr.setActualSmsCount(120);
        cdr.setActualDataMb(25000L);
        cdr.setRoamingVoiceMinutes(30);
        cdr.setRoamingDataMb(500L);
        cdr.setPremiumSmsCount(2);
        cdr.setRecordedAt(LocalDateTime.of(2025, 11, 1, 2, 0));

        assertEquals(Long.valueOf(1L), cdr.getId());
        assertEquals(Integer.valueOf(450), cdr.getActualVoiceMinutes());
        assertEquals(Integer.valueOf(120), cdr.getActualSmsCount());
        assertEquals(Long.valueOf(25000L), cdr.getActualDataMb());
        assertEquals(Integer.valueOf(30), cdr.getRoamingVoiceMinutes());
        assertEquals(Long.valueOf(500L), cdr.getRoamingDataMb());
        assertEquals(Integer.valueOf(2), cdr.getPremiumSmsCount());
    }

    // ------------------------------------------------------------------
    // ReconciliationResult tests
    // ------------------------------------------------------------------

    @Test
    public void testReconciliationResultResolution() {
        ReconciliationResult result = new ReconciliationResult();
        result.setStatus("OVERCHARGED");
        assertNull(result.getResolvedAt());
        assertNull(result.getResolvedBy());

        result.setResolvedAt(LocalDateTime.of(2025, 12, 1, 10, 0));
        result.setResolvedBy("admin@telco.com");
        assertNotNull(result.getResolvedAt());
        assertEquals("admin@telco.com", result.getResolvedBy());
    }

    @Test
    public void testReconciliationStatusValues() {
        ReconciliationResult result = new ReconciliationResult();
        String[] statuses = {"MATCHED", "OVERCHARGED", "UNDERCHARGED",
                             "MISSING_CONSUMPTION", "ERROR"};
        for (String status : statuses) {
            result.setStatus(status);
            assertEquals(status, result.getStatus());
        }
    }

    @Test
    public void testUnderchargedDiscrepancyIsNegative() {
        BigDecimal invoiced = new BigDecimal("19.99");
        BigDecimal expected = new BigDecimal("29.99");
        BigDecimal diff = invoiced.subtract(expected).setScale(2, RoundingMode.HALF_UP);

        assertTrue("Undercharged diff should be negative", diff.compareTo(BigDecimal.ZERO) < 0);
        assertEquals(0, new BigDecimal("10.00").compareTo(diff.abs()));
    }

    @Test
    public void testMatchedWithinTolerance() {
        BigDecimal invoiced = new BigDecimal("19.99");
        BigDecimal expected = new BigDecimal("19.995");
        BigDecimal diff = invoiced.subtract(expected).abs().setScale(2, RoundingMode.HALF_UP);
        assertTrue("Diff within 0.01 should be considered matched",
                diff.compareTo(new BigDecimal("0.01")) <= 0);
    }

    @Test
    public void testOverchargedDiscrepancyIsPositive() {
        BigDecimal invoiced = new BigDecimal("55.00");
        BigDecimal expected = new BigDecimal("49.99");
        BigDecimal diff = invoiced.subtract(expected).setScale(2, RoundingMode.HALF_UP);

        assertTrue("Overcharged diff should be positive", diff.compareTo(BigDecimal.ZERO) > 0);
        assertEquals(0, new BigDecimal("5.01").compareTo(diff));
    }

    @Test
    public void testMissingConsumptionResult() {
        ReconciliationResult result = new ReconciliationResult();
        result.setStatus("MISSING_CONSUMPTION");
        result.setExpectedAmount(BigDecimal.ZERO);
        result.setInvoicedAmount(new BigDecimal("29.99"));
        result.setDiscrepancyAmount(new BigDecimal("29.99"));
        result.setDiscrepancyDetails("No CDR consumption record found for this billing period.");

        assertEquals("MISSING_CONSUMPTION", result.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getExpectedAmount()));
        assertEquals(0, result.getInvoicedAmount().compareTo(result.getDiscrepancyAmount()));
        assertTrue(result.getDiscrepancyDetails().contains("No CDR"));
    }
}
