package com.telco.reconciliator.ejb;

import com.telco.reconciliator.model.ConsumptionRecord;
import com.telco.reconciliator.model.Invoice;
import com.telco.reconciliator.model.InvoiceItem;
import com.telco.reconciliator.model.ReconciliationResult;
import com.telco.reconciliator.model.ServicePlan;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core reconciliation EJB.
 * Compares each invoice against CDR consumption data and records discrepancies.
 */
@Stateless
public class ReconciliationServiceBean {

    @PersistenceContext(unitName = "ReconciliatorPU")
    private EntityManager em;

    /** Reconcile a single invoice identified by its primary key. */
    public ReconciliationResult reconcileInvoice(Long invoiceId) {
        Invoice invoice = em.find(Invoice.class, invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found: " + invoiceId);
        }

        // Look for a matching CDR record covering the same customer and billing window
        List<ConsumptionRecord> records = em.createQuery(
            "SELECT r FROM ConsumptionRecord r" +
            " WHERE r.customerId = :cid" +
            " AND r.billingPeriodStart = :s" +
            " AND r.billingPeriodEnd = :e",
            ConsumptionRecord.class)
            .setParameter("cid", invoice.getCustomer().getCustomerId())
            .setParameter("s", invoice.getBillingPeriodStart())
            .setParameter("e", invoice.getBillingPeriodEnd())
            .getResultList();

        ReconciliationResult result = new ReconciliationResult();
        result.setInvoice(invoice);
        result.setProcessedAt(LocalDateTime.now());
        result.setInvoicedAmount(invoice.getTotalAmount());

        if (records.isEmpty()) {
            result.setStatus("MISSING_CONSUMPTION");
            result.setExpectedAmount(BigDecimal.ZERO);
            result.setDiscrepancyAmount(invoice.getTotalAmount());
            result.setDiscrepancyDetails("No CDR consumption record found for this billing period.");
            invoice.setReconciliationStatus("DISCREPANCY_FOUND");
        } else {
            ConsumptionRecord cdr = records.get(0);
            result.setConsumptionRecord(cdr);
            BigDecimal expected = computeExpectedAmount(invoice, cdr);
            result.setExpectedAmount(expected);
            BigDecimal diff = invoice.getTotalAmount().subtract(expected).setScale(2, RoundingMode.HALF_UP);
            result.setDiscrepancyAmount(diff);
            if (diff.abs().compareTo(new BigDecimal("0.01")) <= 0) {
                result.setStatus("MATCHED");
                invoice.setReconciliationStatus("MATCHED");
                result.setDiscrepancyDetails("Invoice matches CDR data within tolerance.");
            } else if (diff.compareTo(BigDecimal.ZERO) > 0) {
                result.setStatus("OVERCHARGED");
                invoice.setReconciliationStatus("DISCREPANCY_FOUND");
                result.setDiscrepancyDetails("Customer was overcharged by EUR " + diff.abs() + ". Invoiced: " + invoice.getTotalAmount() + ", Expected: " + expected);
            } else {
                result.setStatus("UNDERCHARGED");
                invoice.setReconciliationStatus("DISCREPANCY_FOUND");
                result.setDiscrepancyDetails("Customer was undercharged by EUR " + diff.abs() + ". Invoiced: " + invoice.getTotalAmount() + ", Expected: " + expected);
            }
        }

        em.merge(invoice);
        em.persist(result);
        em.flush();
        return result;
    }

    /**
     * Compute expected billing amount from CDR data and the customer's service plan.
     * Subscription fee + any overage charges for data, voice and roaming.
     */
    private BigDecimal computeExpectedAmount(Invoice invoice, ConsumptionRecord cdr) {
        String planCode = invoice.getCustomer().getServicePlan();
        List<ServicePlan> plans = em.createQuery(
            "SELECT p FROM ServicePlan p WHERE p.planCode = :pc", ServicePlan.class)
            .setParameter("pc", planCode)
            .getResultList();

        if (plans.isEmpty()) {
            // Fall back to summing existing invoice line items if plan not found
            return invoice.getTotalAmount();
        }

        ServicePlan plan = plans.get(0);
        BigDecimal total = plan.getMonthlyFee() != null ? plan.getMonthlyFee() : BigDecimal.ZERO;

        // Extra data charges
        long usedData = cdr.getActualDataMb() != null ? cdr.getActualDataMb() : 0L;
        long includedData = plan.getIncludedDataMb() != null ? plan.getIncludedDataMb() : 0L;
        if (usedData > includedData && plan.getExtraDataCostPerMb() != null) {
            BigDecimal extraMb = new BigDecimal(usedData - includedData);
            total = total.add(extraMb.multiply(plan.getExtraDataCostPerMb()));
        }

        // Extra voice charges
        int usedVoice = cdr.getActualVoiceMinutes() != null ? cdr.getActualVoiceMinutes() : 0;
        int includedVoice = plan.getIncludedVoiceMinutes() != null ? plan.getIncludedVoiceMinutes() : 0;
        if (usedVoice > includedVoice && plan.getExtraVoiceCostPerMinute() != null) {
            BigDecimal extraMin = new BigDecimal(usedVoice - includedVoice);
            total = total.add(extraMin.multiply(plan.getExtraVoiceCostPerMinute()));
        }

        // Roaming data surcharge (flat EUR 0.05/MB when not included)
        long roamData = cdr.getRoamingDataMb() != null ? cdr.getRoamingDataMb() : 0L;
        if (roamData > 0 && Boolean.TRUE.equals(plan.getRoamingEnabled())) {
            total = total.add(new BigDecimal(roamData).multiply(new BigDecimal("0.05")));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /** Reconcile all invoices that are still in UNPROCESSED state. */
    public List<ReconciliationResult> reconcileAll() {
        List<Invoice> pending = em.createQuery(
            "SELECT i FROM Invoice i WHERE i.reconciliationStatus = 'UNPROCESSED'", Invoice.class)
            .getResultList();
        List<ReconciliationResult> results = new ArrayList<>();
        for (Invoice inv : pending) {
            try {
                results.add(reconcileInvoice(inv.getId()));
            } catch (Exception ex) {
                ReconciliationResult errResult = new ReconciliationResult();
                errResult.setInvoice(inv);
                errResult.setProcessedAt(LocalDateTime.now());
                errResult.setStatus("ERROR");
                errResult.setDiscrepancyDetails("Processing error: " + ex.getMessage());
                errResult.setInvoicedAmount(inv.getTotalAmount());
                errResult.setExpectedAmount(BigDecimal.ZERO);
                errResult.setDiscrepancyAmount(BigDecimal.ZERO);
                em.persist(errResult);
                em.flush();
                results.add(errResult);
            }
        }
        return results;
    }

    public List<ReconciliationResult> findResultsByStatus(String status) {
        return em.createQuery(
            "SELECT r FROM ReconciliationResult r WHERE r.status = :st ORDER BY r.processedAt DESC",
            ReconciliationResult.class)
            .setParameter("st", status)
            .getResultList();
    }

    /** Returns aggregate statistics useful for the dashboard. */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Object[]> rows = em.createQuery(
            "SELECT r.status, COUNT(r), SUM(ABS(r.discrepancyAmount)) FROM ReconciliationResult r GROUP BY r.status",
            Object[].class)
            .getResultList();
        long totalInvoices = ((Number) em.createQuery("SELECT COUNT(i) FROM Invoice i").getSingleResult()).longValue();
        long unprocessed  = ((Number) em.createQuery("SELECT COUNT(i) FROM Invoice i WHERE i.reconciliationStatus = 'UNPROCESSED'").getSingleResult()).longValue();
        stats.put("totalInvoices", totalInvoices);
        stats.put("unprocessedInvoices", unprocessed);
        BigDecimal totalDiscrepancy = BigDecimal.ZERO;
        for (Object[] row : rows) {
            String s = (String) row[0];
            long cnt = ((Number) row[1]).longValue();
            BigDecimal disc = row[2] != null ? ((BigDecimal) row[2]).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            stats.put("count_" + s, cnt);
            stats.put("discrepancy_" + s, disc);
            totalDiscrepancy = totalDiscrepancy.add(disc);
        }
        stats.put("totalDiscrepancyAmount", totalDiscrepancy.setScale(2, RoundingMode.HALF_UP));
        return stats;
    }
}
