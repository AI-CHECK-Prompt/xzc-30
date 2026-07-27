package com.carbon.carbon.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;

@Entity
@Table(name = "t_emission_data")
public class EmissionData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, length = 20)
    private String reportingPeriod;

    @Column(precision = 18, scale = 6)
    private BigDecimal directEmissions;

    @Column(precision = 18, scale = 6)
    private BigDecimal indirectEmissions;

    @Column(precision = 18, scale = 6)
    private BigDecimal totalEmissions;

    @Column(precision = 18, scale = 6)
    private BigDecimal emissionIntensity;

    @Column(length = 20)
    private String dataStatus;

    @Column(length = 20)
    private String verificationStatus;

    private LocalDateTime reportingDate;

    private LocalDateTime verificationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifier;

    @Column(length = 500)
    private String remarks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getReportingPeriod() {
        return reportingPeriod;
    }

    public void setReportingPeriod(String reportingPeriod) {
        this.reportingPeriod = reportingPeriod;
    }

    public BigDecimal getDirectEmissions() {
        return directEmissions;
    }

    public void setDirectEmissions(BigDecimal directEmissions) {
        this.directEmissions = directEmissions;
    }

    public BigDecimal getIndirectEmissions() {
        return indirectEmissions;
    }

    public void setIndirectEmissions(BigDecimal indirectEmissions) {
        this.indirectEmissions = indirectEmissions;
    }

    public BigDecimal getTotalEmissions() {
        return totalEmissions;
    }

    public void setTotalEmissions(BigDecimal totalEmissions) {
        this.totalEmissions = totalEmissions;
    }

    public BigDecimal getEmissionIntensity() {
        return emissionIntensity;
    }

    public void setEmissionIntensity(BigDecimal emissionIntensity) {
        this.emissionIntensity = emissionIntensity;
    }

    public String getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(String dataStatus) {
        this.dataStatus = dataStatus;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getReportingDate() {
        return reportingDate;
    }

    public void setReportingDate(LocalDateTime reportingDate) {
        this.reportingDate = reportingDate;
    }

    public LocalDateTime getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(LocalDateTime verificationDate) {
        this.verificationDate = verificationDate;
    }

    public User getVerifier() {
        return verifier;
    }

    public void setVerifier(User verifier) {
        this.verifier = verifier;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
