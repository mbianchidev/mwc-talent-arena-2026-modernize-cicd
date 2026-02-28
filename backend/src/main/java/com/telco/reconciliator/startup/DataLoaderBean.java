package com.telco.reconciliator.startup;

import com.telco.reconciliator.model.ConsumptionRecord;
import com.telco.reconciliator.model.Customer;
import com.telco.reconciliator.model.Invoice;
import com.telco.reconciliator.model.InvoiceItem;
import com.telco.reconciliator.model.ServicePlan;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Singleton startup bean that seeds the embedded H2 database with realistic
 * Italian telco mock data on first deployment.
 */
@Singleton
@Startup
public class DataLoaderBean {

    private static final Logger LOG = Logger.getLogger(DataLoaderBean.class.getName());

    @PersistenceContext(unitName = "ReconciliatorPU")
    private EntityManager em;

    @PostConstruct
    public void init() {
        Long customerCount = (Long) em.createQuery("SELECT COUNT(c) FROM Customer c").getSingleResult();
        if (customerCount > 0) {
            LOG.info("DataLoaderBean: database already populated, skipping seed.");
            return;
        }
        LOG.info("DataLoaderBean: seeding database with TelcoCorp Italia mock data...");
        seedServicePlans();
        seedCustomersAndInvoices();
        LOG.info("DataLoaderBean: seed complete.");
    }

    // -------------------------------------------------------------------------
    // Service Plans
    // -------------------------------------------------------------------------

    private void seedServicePlans() {
        ServicePlan p1 = plan("MOBILE-BASIC", "Mobile Base 5GB",
            "9.99", 100, 100, 5120L, "0.5000", "0.2500", false,
            "Piano mobile base con 5 GB di dati, 100 minuti e 100 SMS inclusi.");
        ServicePlan p2 = plan("MOBILE-PLUS", "Mobile Plus 20GB",
            "19.99", 500, 999999, 20480L, "0.2000", "0.1500", true,
            "Piano mobile avanzato con 20 GB, chiamate illimitate e roaming EU.");
        ServicePlan p3 = plan("FIBRA-BASIC", "Fibra 500M + Mobile",
            "29.99", 500, 999999, 51200L, "0.1000", "0.1000", true,
            "Bundle fibra ottica 500 Mbps con SIM mobile 50 GB inclusa.");
        ServicePlan p4 = plan("FIBRA-ULTRA", "Fibra Ultra 1Gbps + Mobile Premium",
            "49.99", 999999, 999999, 102400L, "0.0500", "0.0500", true,
            "Top di gamma: fibra 1 Gbps + SIM con 100 GB, chiamate e SMS illimitati.");
        for (ServicePlan p : Arrays.asList(p1, p2, p3, p4)) {
            em.persist(p);
        }
        em.flush();
    }

    private ServicePlan plan(String code, String name, String fee,
                             int voice, int sms, long dataMb,
                             String extraData, String extraVoice,
                             boolean roaming, String desc) {
        ServicePlan p = new ServicePlan();
        p.setPlanCode(code);
        p.setPlanName(name);
        p.setMonthlyFee(new BigDecimal(fee));
        p.setIncludedVoiceMinutes(voice);
        p.setIncludedSmsCount(sms);
        p.setIncludedDataMb(dataMb);
        p.setExtraDataCostPerMb(new BigDecimal(extraData));
        p.setExtraVoiceCostPerMinute(new BigDecimal(extraVoice));
        p.setRoamingEnabled(roaming);
        p.setDescription(desc);
        return p;
    }

    // -------------------------------------------------------------------------
    // Customers and their invoices
    // -------------------------------------------------------------------------

    private void seedCustomersAndInvoices() {
        // 8 Italian subscribers across different cities and plans
        Customer c1 = customer("CUST-000001", "Marco Esposito", "marco.esposito@email.it",
            "+39 347 1234567", "Via Roma 12", "Napoli", "MOBILE-PLUS", "ACTIVE",
            LocalDate.of(2022, 3, 15));
        Customer c2 = customer("CUST-000002", "Giulia Bianchi", "giulia.bianchi@gmail.com",
            "+39 333 9876543", "Corso Vittorio Emanuele 45", "Milano", "FIBRA-ULTRA", "ACTIVE",
            LocalDate.of(2021, 7, 1));
        Customer c3 = customer("CUST-000003", "Lorenzo Ricci", "lorenzo.ricci@libero.it",
            "+39 320 5551234", "Via Nazionale 78", "Roma", "FIBRA-BASIC", "ACTIVE",
            LocalDate.of(2023, 1, 10));
        Customer c4 = customer("CUST-000004", "Sofia Ferrara", "sofia.ferrara@yahoo.it",
            "+39 348 7654321", "Piazza Garibaldi 3", "Torino", "MOBILE-BASIC", "ACTIVE",
            LocalDate.of(2022, 11, 20));
        Customer c5 = customer("CUST-000005", "Alessandro Conti", "a.conti@pec.it",
            "+39 366 3331234", "Via Indipendenza 100", "Bologna", "FIBRA-ULTRA", "ACTIVE",
            LocalDate.of(2020, 5, 5));
        Customer c6 = customer("CUST-000006", "Valentina Gallo", "valentina.gallo@hotmail.it",
            "+39 391 2223344", "Lungarno Mediceo 22", "Firenze", "MOBILE-PLUS", "SUSPENDED",
            LocalDate.of(2023, 6, 1));
        Customer c7 = customer("CUST-000007", "Davide Lombardi", "d.lombardi@tin.it",
            "+39 345 6667788", "Via Caracciolo 55", "Napoli", "FIBRA-BASIC", "ACTIVE",
            LocalDate.of(2021, 9, 14));
        Customer c8 = customer("CUST-000008", "Chiara Moretti", "chiara.moretti@alice.it",
            "+39 329 9998877", "Via Po 8", "Torino", "MOBILE-BASIC", "CHURNED",
            LocalDate.of(2019, 2, 28));

        List<Customer> customers = Arrays.asList(c1, c2, c3, c4, c5, c6, c7, c8);
        for (Customer c : customers) {
            em.persist(c);
        }
        em.flush();

        // 3 months: Oct, Nov, Dec 2025
        int[][] months = {{2025,10},{2025,11},{2025,12}};
        int invoiceSeq = 1;
        for (Customer customer : customers) {
            for (int[] ym : months) {
                int year = ym[0]; int month = ym[1];
                LocalDate periodStart = LocalDate.of(year, month, 1);
                LocalDate periodEnd   = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
                LocalDate issueDate    = periodEnd.plusDays(3);
                LocalDate dueDate      = issueDate.plusDays(30);
                String invNum = "INV-" + year + "-" + String.format("%08d", invoiceSeq++);
                Invoice inv = buildInvoice(customer, invNum, periodStart, periodEnd, issueDate, dueDate);
                em.persist(inv);
                ConsumptionRecord cdr = buildCdr(customer, periodStart, periodEnd, inv, invoiceSeq);
                em.persist(cdr);
            }
        }
        em.flush();
    }

    private Customer customer(String id, String name, String email,
                              String phone, String addr, String city,
                              String plan, String status, LocalDate since) {
        Customer c = new Customer();
        c.setCustomerId(id);
        c.setFullName(name);
        c.setEmail(email);
        c.setPhoneNumber(phone);
        c.setAddress(addr);
        c.setCity(city);
        c.setServicePlan(plan);
        c.setStatus(status);
        c.setContractStartDate(since);
        return c;
    }

    /**
     * Builds an Invoice with realistic line items.
     * Some invoices intentionally carry wrong totals to exercise discrepancy detection.
     */
    private Invoice buildInvoice(Customer customer, String invNum,
                                 LocalDate start, LocalDate end,
                                 LocalDate issueDate, LocalDate dueDate) {
        String planCode = customer.getServicePlan();
        BigDecimal baseFee = baseFeeForPlan(planCode);
        Invoice inv = new Invoice();
        inv.setInvoiceNumber(invNum);
        inv.setCustomer(customer);
        inv.setBillingPeriodStart(start);
        inv.setBillingPeriodEnd(end);
        inv.setIssueDate(issueDate);
        inv.setDueDate(dueDate);
        inv.setStatus("PENDING");
        inv.setReconciliationStatus("UNPROCESSED");

        // Subscription fee line
        InvoiceItem sub = lineItem(inv, "SUBSCRIPTION_FEE",
            "Canone mensile " + planNameForCode(planCode),
            "1", baseFee.toPlainString(), baseFee.toPlainString(), start, end);
        inv.getItems().add(sub);
        BigDecimal total = baseFee;

        // Extra usage lines based on plan
        boolean highUser = customer.getCustomerId().hashCode() % 3 != 0;
        if (highUser && planCode.startsWith("MOBILE")) {
            BigDecimal extraData = new BigDecimal("3.50");
            InvoiceItem ed = lineItem(inv, "EXTRA_DATA", "Traffico dati aggiuntivo (700 MB)",
                "700", "0.0050", extraData.toPlainString(), start, end);
            inv.getItems().add(ed);
            total = total.add(extraData);
        }
        if (planCode.equals("MOBILE-PLUS") || planCode.equals("FIBRA-ULTRA")) {
            BigDecimal roaming = new BigDecimal("2.50");
            InvoiceItem rr = lineItem(inv, "ROAMING", "Traffico roaming EU (50 MB)",
                "50", "0.0500", roaming.toPlainString(), start, end);
            inv.getItems().add(rr);
            total = total.add(roaming);
        }

        // Introduce deliberate discrepancy on specific invoice numbers
        boolean corruptTotal = invNum.endsWith("3") || invNum.endsWith("7");
        if (corruptTotal) {
            total = total.add(new BigDecimal("5.00")); // billing system added phantom charge
        }

        inv.setTotalAmount(total.setScale(2, java.math.RoundingMode.HALF_UP));
        return inv;
    }

    private ConsumptionRecord buildCdr(Customer customer, LocalDate start, LocalDate end,
                                       Invoice inv, int seq) {
        String planCode = customer.getServicePlan();
        ConsumptionRecord cdr = new ConsumptionRecord();
        cdr.setCustomerId(customer.getCustomerId());
        cdr.setBillingPeriodStart(start);
        cdr.setBillingPeriodEnd(end);
        cdr.setRecordedAt(LocalDateTime.now());
        cdr.setSource("NETWORK_CDR");

        // Typical usage - within bundle limits
        cdr.setActualVoiceMinutes(80 + (seq % 5) * 20);
        cdr.setActualSmsCount(40 + (seq % 4) * 10);
        cdr.setActualDataMb(4000L + (seq % 6) * 300L);
        cdr.setRoamingVoiceMinutes(0);
        cdr.setRoamingDataMb(0L);
        cdr.setPremiumSmsCount(0);

        // High-usage customers go over data bundle
        boolean overData = customer.getCustomerId().hashCode() % 3 != 0 && planCode.startsWith("MOBILE");
        if (overData) {
            cdr.setActualDataMb(cdr.getActualDataMb() + 700L);
        }
        // Roaming usage for Plus/Ultra plans
        if (planCode.equals("MOBILE-PLUS") || planCode.equals("FIBRA-ULTRA")) {
            cdr.setRoamingDataMb(50L);
        }
        return cdr;
    }

    private InvoiceItem lineItem(Invoice inv, String type, String desc,
                                 String qty, String unitPrice, String total,
                                 LocalDate start, LocalDate end) {
        InvoiceItem item = new InvoiceItem();
        item.setInvoice(inv);
        item.setItemType(type);
        item.setDescription(desc);
        item.setQuantity(new BigDecimal(qty));
        item.setUnitPrice(new BigDecimal(unitPrice));
        item.setTotalPrice(new BigDecimal(total));
        item.setServiceStartDate(start);
        item.setServiceEndDate(end);
        return item;
    }

    private BigDecimal baseFeeForPlan(String code) {
        switch (code) {
            case "MOBILE-BASIC":  return new BigDecimal("9.99");
            case "MOBILE-PLUS":   return new BigDecimal("19.99");
            case "FIBRA-BASIC":   return new BigDecimal("29.99");
            case "FIBRA-ULTRA":   return new BigDecimal("49.99");
            default:               return BigDecimal.ZERO;
        }
    }

    private String planNameForCode(String code) {
        switch (code) {
            case "MOBILE-BASIC":  return "Mobile Base 5GB";
            case "MOBILE-PLUS":   return "Mobile Plus 20GB";
            case "FIBRA-BASIC":   return "Fibra 500M + Mobile";
            case "FIBRA-ULTRA":   return "Fibra Ultra 1Gbps + Mobile Premium";
            default:               return code;
        }
    }
}
