package com.telco.reconciliator.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outcome of comparing a subscriber invoice against the CDR consumption record.
 * status codes: MATCHED | OVERCHARGED | UNDERCHARGED | MISSING_CONSUMPTION | ERROR
 */
@Entity
@Table(name = "RECONCILIATION_RESULTS")
public class ReconciliationResult {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "INVOICE_ID_FK", nullable = false)
    private Invoice invoice;

    @OneToOne
    @JoinColumn(name = "CONSUMPTION_RECORD_ID_FK", nullable = true)
    private ConsumptionRecord consumptionRecord;

    @Column(name = "PROCESSED_AT")
    private LocalDateTime processedAt;

    @Column(name = "STATUS", length = 30)
    private String status;

    @Column(name = "INVOICED_AMOUNT", precision = 10, scale = 2)
    private BigDecimal invoicedAmount;

    @Column(name = "EXPECTED_AMOUNT", precision = 10, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "DISCREPANCY_AMOUNT", precision = 10, scale = 2)
    private BigDecimal discrepancyAmount;

    @Column(name = "DISCREPANCY_DETAILS", length = 2000)
    private String discrepancyDetails;

    @Column(name = "RESOLVED_AT")
    private LocalDateTime resolvedAt;

    @Column(name = "RESOLVED_BY", length = 80)
    private String resolvedBy;

    public ReconciliationResult() {}

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice v) { this.invoice = v; }
    public ConsumptionRecord getConsumptionRecord() { return consumptionRecord; }
    public void setConsumptionRecord(ConsumptionRecord v) { this.consumptionRecord = v; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime v) { this.processedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public BigDecimal getInvoicedAmount() { return invoicedAmount; }
    public void setInvoicedAmount(BigDecimal v) { this.invoicedAmount = v; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
    public void setExpectedAmount(BigDecimal v) { this.expectedAmount = v; }
    public BigDecimal getDiscrepancyAmount() { return discrepancyAmount; }
    public void setDiscrepancyAmount(BigDecimal v) { this.discrepancyAmount = v; }
    public String getDiscrepancyDetails() { return discrepancyDetails; }
    public void setDiscrepancyDetails(String v) { this.discrepancyDetails = v; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime v) { this.resolvedAt = v; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String v) { this.resolvedBy = v; }
}
