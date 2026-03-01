package com.telco.reconciliator.ejb;

import com.telco.reconciliator.model.Invoice;
import com.telco.reconciliator.model.InvoiceItem;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Stateless EJB providing queries and operations on Invoice records. */
@Stateless
public class InvoiceServiceBean {

    @PersistenceContext(unitName = "ReconciliatorPU")
    private EntityManager em;

    public List<Invoice> findAll() {
        return em.createQuery("SELECT i FROM Invoice i ORDER BY i.issueDate DESC", Invoice.class)
                 .getResultList();
    }

    public Invoice findById(Long id) {
        return em.find(Invoice.class, id);
    }

    public List<Invoice> findByCustomer(String customerId) {
        return em.createQuery(
            "SELECT i FROM Invoice i WHERE i.customer.customerId = :cid ORDER BY i.billingPeriodStart DESC",
            Invoice.class)
                 .setParameter("cid", customerId)
                 .getResultList();
    }

    public List<Invoice> findByReconciliationStatus(String status) {
        return em.createQuery(
            "SELECT i FROM Invoice i WHERE i.reconciliationStatus = :st ORDER BY i.issueDate DESC",
            Invoice.class)
                 .setParameter("st", status)
                 .getResultList();
    }

    public List<Invoice> findByBillingPeriod(LocalDate start, LocalDate end) {
        return em.createQuery(
            "SELECT i FROM Invoice i WHERE i.billingPeriodStart >= :s AND i.billingPeriodEnd <= :e ORDER BY i.billingPeriodStart",
            Invoice.class)
                 .setParameter("s", start)
                 .setParameter("e", end)
                 .getResultList();
    }

    /** Recomputes the invoice total by summing all line-item totals. */
    public BigDecimal computeTotal(Invoice inv) {
        BigDecimal sum = BigDecimal.ZERO;
        if (inv.getItems() != null) {
            for (InvoiceItem item : inv.getItems()) {
                if (item.getTotalPrice() != null) {
                    sum = sum.add(item.getTotalPrice());
                }
            }
        }
        return sum;
    }

    public Invoice create(Invoice inv) {
        em.persist(inv);
        em.flush();
        return inv;
    }
}
