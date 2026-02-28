package com.telco.reconciliator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** TelcoCorp Italia subscriber entity. */
@Entity
@Table(name = "CUSTOMERS")
public class Customer {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "CUSTOMER_ID", unique = true, nullable = false, length = 20)
    private String customerId;

    @Column(name = "FULL_NAME", length = 120)
    private String fullName;

    @Column(name = "EMAIL", length = 120)
    private String email;

    /** Italian mobile, e.g. +39 347 1234567 */
    @Column(name = "PHONE_NUMBER", length = 20)
    private String phoneNumber;

    @Column(name = "ADDRESS", length = 200)
    private String address;

    @Column(name = "CITY", length = 60)
    private String city;

    @Column(name = "COUNTRY", length = 60)
    private String country = "Italy";

    /** ACTIVE | SUSPENDED | CHURNED */
    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "SERVICE_PLAN", length = 30)
    private String servicePlan;

    @Column(name = "CONTRACT_START_DATE")
    private LocalDate contractStartDate;

    @JsonIgnore
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Invoice> invoices = new ArrayList<>();

    public Customer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String v) { this.customerId = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String v) { this.phoneNumber = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public String getCity() { return city; }
    public void setCity(String v) { this.city = v; }
    public String getCountry() { return country; }
    public void setCountry(String v) { this.country = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getServicePlan() { return servicePlan; }
    public void setServicePlan(String v) { this.servicePlan = v; }
    public LocalDate getContractStartDate() { return contractStartDate; }
    public void setContractStartDate(LocalDate v) { this.contractStartDate = v; }
    public List<Invoice> getInvoices() { return invoices; }
    public void setInvoices(List<Invoice> v) { this.invoices = v; }
}
