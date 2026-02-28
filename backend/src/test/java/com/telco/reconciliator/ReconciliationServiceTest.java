package com.telco.reconciliator;

import com.telco.reconciliator.model.ConsumptionRecord;
import com.telco.reconciliator.model.Customer;
import com.telco.reconciliator.model.Invoice;
import com.telco.reconciliator.model.InvoiceItem;
import com.telco.reconciliator.model.ReconciliationResult;
import com.telco.reconciliator.model.ServicePlan;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * Unit tests for the telco reconciliation domain model and business rules.
 * Tests entity construction and basic reconciliation arithmetic without
 * requiring an EJB container or database.
 */
public class ReconciliationServiceTest {

    // ------------------------------------------------------------------
    // Entity construction tests
    // ------------------------------------------------------------------

    @Test
    public void testCustomerEntityDefaults() {
        Customer c = new Customer();
        c.setCustomerId("CUST-000001");
        c.setFullName("Marco Rossi");
        c.setEmail("marco.rossi@email.it");
        c.setStatus("ACTIVE");
        c.setServicePlan("MOBILE-PLUS");
        c.setContractStartDate(LocalDate.of(2024, 1, 15));

        assertEquals("CUST-000001", c.getCustomerId());
        assertEquals("Marco Rossi", c.getFullName());
        assertEquals("ACTIVE", c.getStatus());
        assertEquals("Italy", c.getCountry()); // default value
        assertTrue("Invoices list should be initialised empty", c.getInvoices().isEmpty());
    }

    @Test
    public void testServicePlanMonthlyFee() {
        ServicePlan plan = new ServicePlan();
        plan.setPlanCode("FIBRA-ULTRA");
        plan.setPlanName("Fibra Ultra 1Gbps + Mobile Premium");
        plan.setMonthlyFee(new BigDecimal("49.99"));
        plan.setIncludedVoiceMinutes(999999);
        plan.setIncludedSmsCount(999999);
        plan.setIncludedDataMb(102400L);
        plan.setExtraDataCostPerMb(new BigDecimal("0.0500"));
        plan.setRoamingEnabled(true);

        assertEquals(0, new BigDecimal("49.99").compareTo(plan.getMonthlyFee()));
        assertTrue("Roaming should be enabled for FIBRA-ULTRA", plan.getRoamingEnabled());
        assertEquals(Long.valueOf(102400L), plan.getIncludedDataMb());
    }

    @Test
    public void testInvoiceDefaultReconciliationStatus() {
        Invoice inv = new Invoice();
        inv.setInvoiceNumber("INV-2025-00000001");
        inv.setBillingPeriodStart(LocalDate.of(2025, 10, 1));
        inv.setBillingPeriodEnd(LocalDate.of(2025, 10, 31));
        inv.setTotalAmount(new BigDecimal("29.99"));
        inv.setStatus("PENDING");

        // reconciliationStatus defaults to UNPROCESSED
        assertEquals("UNPROCESSED", inv.getReconciliationStatus());
        assertTrue("Items list should be initialised empty", inv.getItems().isEmpty());
    }

    @Test
    public void testInvoiceItemTotalPrice() {
        InvoiceItem item = new InvoiceItem();
        item.setItemType("SUBSCRIPTION_FEE");
        item.setDescription("Canone mensile Mobile Plus 20GB");
        item.setQuantity(new BigDecimal("1"));
        item.setUnitPrice(new BigDecimal("19.99"));
        item.setTotalPrice(
            item.getQuantity().multiply(item.getUnitPrice()));

        assertEquals(0, new BigDecimal("19.99").compareTo(item.getTotalPrice()));
    }

    @Test
    public void testConsumptionRecordFields() {
        ConsumptionRecord cdr = new ConsumptionRecord();
        cdr.setCustomerId("CUST-000001");
        cdr.setBillingPeriodStart(LocalDate.of(2025, 10, 1));
        cdr.setBillingPeriodEnd(LocalDate.of(2025, 10, 31));
        cdr.setActualVoiceMinutes(380);
        cdr.setActualSmsCount(47);
        cdr.setActualDataMb(18200L);
        cdr.setRoamingVoiceMinutes(0);
        cdr.setRoamingDataMb(0L);
        cdr.setSource("NETWORK_CDR");
        cdr.setRecordedAt(LocalDateTime.of(2025, 11, 2, 3, 0));

        assertEquals("CUST-000001", cdr.getCustomerId());
        assertEquals(Integer.valueOf(380), cdr.getActualVoiceMinutes());
        assertEquals("NETWORK_CDR", cdr.getSource());
    }

    @Test
    public void testReconciliationResultDiscrepancyCalculation() {
        // Simulate: invoiced 35.49 but expected 29.99 -> overcharged by 5.50
        BigDecimal invoiced = new BigDecimal("35.49");
        BigDecimal expected = new BigDecimal("29.99");
        BigDecimal discrepancy = invoiced.subtract(expected);

        ReconciliationResult result = new ReconciliationResult();
        result.setInvoicedAmount(invoiced);
        result.setExpectedAmount(expected);
        result.setDiscrepancyAmount(discrepancy);
        result.setStatus("OVERCHARGED");
        result.setProcessedAt(LocalDateTime.now());
        result.setDiscrepancyDetails(
            "Customer overcharged by EUR " + discrepancy);

        assertEquals("OVERCHARGED", result.getStatus());
        assertEquals(0, new BigDecimal("5.50").compareTo(result.getDiscrepancyAmount()));
        assertTrue(result.getDiscrepancyDetails().contains("5.50"));
        assertNull("Unresolved result should have null resolvedAt", result.getResolvedAt());
    }

    @Test
    public void testReconciliationResultMatchedStatus() {
        // Simulate: invoiced and expected differ by less than 0.01 -> MATCHED
        BigDecimal invoiced = new BigDecimal("19.99");
        BigDecimal expected = new BigDecimal("19.99");
        BigDecimal diff = invoiced.subtract(expected).abs();

        assertTrue("Diff should be within tolerance", diff.compareTo(new BigDecimal("0.01")) <= 0);

        ReconciliationResult result = new ReconciliationResult();
        result.setStatus("MATCHED");
        result.setInvoicedAmount(invoiced);
        result.setExpectedAmount(expected);
        result.setDiscrepancyAmount(diff);

        assertEquals("MATCHED", result.getStatus());
    }
}
