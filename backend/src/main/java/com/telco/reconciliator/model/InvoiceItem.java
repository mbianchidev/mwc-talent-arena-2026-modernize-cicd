package com.telco.reconciliator.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single charge line on an invoice.
 * itemType codes: SUBSCRIPTION_FEE | EXTRA_DATA | EXTRA_VOICE | ROAMING |
 *                  PREMIUM_SMS | EQUIPMENT_RENTAL | ACTIVATION_FEE | DISCOUNT
 */
@Entity
@Table(name = "INVOICE_ITEMS")
public class InvoiceItem {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "INVOICE_ID_FK", nullable = false)
    private Invoice invoice;

    @Column(name = "ITEM_TYPE", length = 30)
    private String itemType;

    @Column(name = "DESCRIPTION", length = 300)
    private String description;

    @Column(name = "QUANTITY", precision = 12, scale = 4)
    private BigDecimal quantity;

    @Column(name = "UNIT_PRICE", precision = 10, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "TOTAL_PRICE", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "SERVICE_START_DATE")
    private LocalDate serviceStartDate;

    @Column(name = "SERVICE_END_DATE")
    private LocalDate serviceEndDate;

    public InvoiceItem() {}

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice v) { this.invoice = v; }
    public String getItemType() { return itemType; }
    public void setItemType(String v) { this.itemType = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal v) { this.quantity = v; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal v) { this.unitPrice = v; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal v) { this.totalPrice = v; }
    public LocalDate getServiceStartDate() { return serviceStartDate; }
    public void setServiceStartDate(LocalDate v) { this.serviceStartDate = v; }
    public LocalDate getServiceEndDate() { return serviceEndDate; }
    public void setServiceEndDate(LocalDate v) { this.serviceEndDate = v; }
}
