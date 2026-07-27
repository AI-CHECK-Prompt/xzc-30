package com.carbon.carbon.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_quota")
public class Quota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private Integer year;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal initialAllocation;

    @Column(precision = 18, scale = 2)
    private BigDecimal additionalAllocation;

    @Column(precision = 18, scale = 2)
    private BigDecimal tradedIn;

    @Column(precision = 18, scale = 2)
    private BigDecimal tradedOut;

    @Column(precision = 18, scale = 2)
    private BigDecimal offsetUsed;

    @Column(precision = 18, scale = 2)
    private BigDecimal surrendered;

    @Column(precision = 18, scale = 2)
    private BigDecimal currentBalance;

    @Column(length = 20)
    private String complianceStatus;

    private LocalDateTime allocationDate;

    private LocalDateTime complianceDeadline;

    private LocalDateTime complianceDate;

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

    public BigDecimal getInitialAllocation() {
        return initialAllocation;
    }

    public void setInitialAllocation(BigDecimal initialAllocation) {
        this.initialAllocation = initialAllocation;
    }

    public BigDecimal getAdditionalAllocation() {
        return additionalAllocation;
    }

    public void setAdditionalAllocation(BigDecimal additionalAllocation) {
        this.additionalAllocation = additionalAllocation;
    }

    public BigDecimal getTradedIn() {
        return tradedIn;
    }

    public void setTradedIn(BigDecimal tradedIn) {
        this.tradedIn = tradedIn;
    }

    public BigDecimal getTradedOut() {
        return tradedOut;
    }

    public void setTradedOut(BigDecimal tradedOut) {
        this.tradedOut = tradedOut;
    }

    public BigDecimal getOffsetUsed() {
        return offsetUsed;
    }

    public void setOffsetUsed(BigDecimal offsetUsed) {
        this.offsetUsed = offsetUsed;
    }

    public BigDecimal getSurrendered() {
        return surrendered;
    }

    public void setSurrendered(BigDecimal surrendered) {
        this.surrendered = surrendered;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getComplianceStatus() {
        return complianceStatus;
    }

    public void setComplianceStatus(String complianceStatus) {
        this.complianceStatus = complianceStatus;
    }

    public LocalDateTime getAllocationDate() {
        return allocationDate;
    }

    public void setAllocationDate(LocalDateTime allocationDate) {
        this.allocationDate = allocationDate;
    }

    public LocalDateTime getComplianceDeadline() {
        return complianceDeadline;
    }

    public void setComplianceDeadline(LocalDateTime complianceDeadline) {
        this.complianceDeadline = complianceDeadline;
    }

    public LocalDateTime getComplianceDate() {
        return complianceDate;
    }

    public void setComplianceDate(LocalDateTime complianceDate) {
        this.complianceDate = complianceDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
