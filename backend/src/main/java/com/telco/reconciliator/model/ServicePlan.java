package com.telco.reconciliator.model;

import javax.persistence.*;
import java.math.BigDecimal;

/** Represents a TelcoCorp Italia commercial offer (mobile or fibre bundle). */
@Entity
@Table(name = "SERVICE_PLANS")
public class ServicePlan {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "PLAN_CODE", unique = true, nullable = false, length = 30)
    private String planCode;

    @Column(name = "PLAN_NAME", length = 120)
    private String planName;

    @Column(name = "MONTHLY_FEE", precision = 10, scale = 2)
    private BigDecimal monthlyFee;

    @Column(name = "INCLUDED_VOICE_MINUTES")
    private Integer includedVoiceMinutes;

    @Column(name = "INCLUDED_SMS_COUNT")
    private Integer includedSmsCount;

    /** Data bundle size in megabytes */
    @Column(name = "INCLUDED_DATA_MB")
    private Long includedDataMb;

    @Column(name = "EXTRA_DATA_COST_PER_MB", precision = 10, scale = 4)
    private BigDecimal extraDataCostPerMb;

    @Column(name = "EXTRA_VOICE_COST_PER_MIN", precision = 10, scale = 4)
    private BigDecimal extraVoiceCostPerMinute;

    @Column(name = "ROAMING_ENABLED")
    private Boolean roamingEnabled;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    public ServicePlan() {}

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getPlanCode() { return planCode; }
    public void setPlanCode(String v) { this.planCode = v; }
    public String getPlanName() { return planName; }
    public void setPlanName(String v) { this.planName = v; }
    public BigDecimal getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(BigDecimal v) { this.monthlyFee = v; }
    public Integer getIncludedVoiceMinutes() { return includedVoiceMinutes; }
    public void setIncludedVoiceMinutes(Integer v) { this.includedVoiceMinutes = v; }
    public Integer getIncludedSmsCount() { return includedSmsCount; }
    public void setIncludedSmsCount(Integer v) { this.includedSmsCount = v; }
    public Long getIncludedDataMb() { return includedDataMb; }
    public void setIncludedDataMb(Long v) { this.includedDataMb = v; }
    public BigDecimal getExtraDataCostPerMb() { return extraDataCostPerMb; }
    public void setExtraDataCostPerMb(BigDecimal v) { this.extraDataCostPerMb = v; }
    public BigDecimal getExtraVoiceCostPerMinute() { return extraVoiceCostPerMinute; }
    public void setExtraVoiceCostPerMinute(BigDecimal v) { this.extraVoiceCostPerMinute = v; }
    public Boolean getRoamingEnabled() { return roamingEnabled; }
    public void setRoamingEnabled(Boolean v) { this.roamingEnabled = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
}
