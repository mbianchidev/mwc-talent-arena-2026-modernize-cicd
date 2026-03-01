package com.telco.reconciliator.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Raw usage record sourced from the network CDR feed or billing system.
 * One record covers an entire billing period for one subscriber.
 */
@Entity
@Table(name = "CONSUMPTION_RECORDS")
public class ConsumptionRecord {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "CUSTOMER_ID", nullable = false, length = 20)
    private String customerId;

    @Column(name = "BILLING_PERIOD_START")
    private LocalDate billingPeriodStart;

    @Column(name = "BILLING_PERIOD_END")
    private LocalDate billingPeriodEnd;

    @Column(name = "ACTUAL_VOICE_MINUTES")
    private Integer actualVoiceMinutes;

    @Column(name = "ACTUAL_SMS_COUNT")
    private Integer actualSmsCount;

    @Column(name = "ACTUAL_DATA_MB")
    private Long actualDataMb;

    @Column(name = "ROAMING_VOICE_MINUTES")
    private Integer roamingVoiceMinutes;

    @Column(name = "ROAMING_DATA_MB")
    private Long roamingDataMb;

    @Column(name = "PREMIUM_SMS_COUNT")
    private Integer premiumSmsCount;

    @Column(name = "RECORDED_AT")
    private LocalDateTime recordedAt;

    /** Data origin: NETWORK_CDR or BILLING_SYSTEM */
    @Column(name = "SOURCE", length = 30)
    private String source;

    public ConsumptionRecord() {}

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String v) { this.customerId = v; }
    public LocalDate getBillingPeriodStart() { return billingPeriodStart; }
    public void setBillingPeriodStart(LocalDate v) { this.billingPeriodStart = v; }
    public LocalDate getBillingPeriodEnd() { return billingPeriodEnd; }
    public void setBillingPeriodEnd(LocalDate v) { this.billingPeriodEnd = v; }
    public Integer getActualVoiceMinutes() { return actualVoiceMinutes; }
    public void setActualVoiceMinutes(Integer v) { this.actualVoiceMinutes = v; }
    public Integer getActualSmsCount() { return actualSmsCount; }
    public void setActualSmsCount(Integer v) { this.actualSmsCount = v; }
    public Long getActualDataMb() { return actualDataMb; }
    public void setActualDataMb(Long v) { this.actualDataMb = v; }
    public Integer getRoamingVoiceMinutes() { return roamingVoiceMinutes; }
    public void setRoamingVoiceMinutes(Integer v) { this.roamingVoiceMinutes = v; }
    public Long getRoamingDataMb() { return roamingDataMb; }
    public void setRoamingDataMb(Long v) { this.roamingDataMb = v; }
    public Integer getPremiumSmsCount() { return premiumSmsCount; }
    public void setPremiumSmsCount(Integer v) { this.premiumSmsCount = v; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime v) { this.recordedAt = v; }
    public String getSource() { return source; }
    public void setSource(String v) { this.source = v; }
}
