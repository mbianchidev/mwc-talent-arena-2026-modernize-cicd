package com.telco.reconciliator.ejb;

import com.telco.reconciliator.model.Customer;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/** Stateless EJB providing CRUD operations on Customer records. */
@Stateless
public class CustomerServiceBean {

    @PersistenceContext(unitName = "ReconciliatorPU")
    private EntityManager em;

    public List<Customer> findAll() {
        return em.createQuery("SELECT c FROM Customer c ORDER BY c.fullName", Customer.class)
                 .getResultList();
    }

    public Customer findById(Long id) {
        return em.find(Customer.class, id);
    }

    public Customer findByCustomerId(String customerId) {
        TypedQuery<Customer> q = em.createQuery(
            "SELECT c FROM Customer c WHERE c.customerId = :cid", Customer.class);
        q.setParameter("cid", customerId);
        List<Customer> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Customer> findByStatus(String status) {
        return em.createQuery(
            "SELECT c FROM Customer c WHERE c.status = :st ORDER BY c.fullName", Customer.class)
                 .setParameter("st", status)
                 .getResultList();
    }

    public Customer create(Customer c) {
        em.persist(c);
        em.flush();
        return c;
    }

    public Customer update(Customer c) {
        return em.merge(c);
    }

    public Long count() {
        return em.createQuery("SELECT COUNT(c) FROM Customer c", Long.class)
                 .getSingleResult();
    }
}
