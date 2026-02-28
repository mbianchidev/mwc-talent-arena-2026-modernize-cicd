package com.telco.reconciliator.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Monthly invoice issued to a TelcoCorp Italia subscriber. */
@Entity
@Table(name = "INVOICES")
public class Invoice {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Business key, format INV-YYYY-XXXXXXXX */
    @Column(name = "INVOICE_NUMBER", unique = true, nullable = false, length = 30)
    private String invoiceNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "CUSTOMER_ID_FK", nullable = false)
    private Customer customer;

    @Column(name = "BILLING_PERIOD_START")
    private LocalDate billingPeriodStart;

    @Column(name = "BILLING_PERIOD_END")
    private LocalDate billingPeriodEnd;

    @Column(name = "ISSUE_DATE")
    private LocalDate issueDate;

    @Column(name = "DUE_DATE")
    private LocalDate dueDate;

    @Column(name = "TOTAL_AMOUNT", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /** Payment state: PENDING | PAID | OVERDUE | DISPUTED */
    @Column(name = "STATUS", length = 20)
    private String status;

    /** Reconciliation workflow state: UNPROCESSED | MATCHED | DISCREPANCY_FOUND | ERROR */
    @Column(name = "RECONCILIATION_STATUS", length = 30)
    private String reconciliationStatus = "UNPROCESSED";

    @Column(name = "NOTES", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {}

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String v) { this.invoiceNumber = v; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer v) { this.customer = v; }
    public LocalDate getBillingPeriodStart() { return billingPeriodStart; }
    public void setBillingPeriodStart(LocalDate v) { this.billingPeriodStart = v; }
    public LocalDate getBillingPeriodEnd() { return billingPeriodEnd; }
    public void setBillingPeriodEnd(LocalDate v) { this.billingPeriodEnd = v; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate v) { this.issueDate = v; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate v) { this.dueDate = v; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal v) { this.totalAmount = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getReconciliationStatus() { return reconciliationStatus; }
    public void setReconciliationStatus(String v) { this.reconciliationStatus = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> v) { this.items = v; }
}
