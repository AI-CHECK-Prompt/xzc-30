package com.carbon.carbon.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_external_factor")
public class ExternalFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factor_date")
    private LocalDate factorDate;

    @Column(name = "factor_type", length = 50, nullable = false)
    private String factorType;

    @Column(name = "factor_value", precision = 18, scale = 4)
    private BigDecimal factorValue;

    @Column(name = "data_source", length = 100)
    private String dataSource;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFactorDate() { return factorDate; }
    public void setFactorDate(LocalDate factorDate) { this.factorDate = factorDate; }

    public String getFactorType() { return factorType; }
    public void setFactorType(String factorType) { this.factorType = factorType; }

    public BigDecimal getFactorValue() { return factorValue; }
    public void setFactorValue(BigDecimal factorValue) { this.factorValue = factorValue; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    // Factor type constants
    public static final String POWER_DEMAND = "POWER_DEMAND";
    public static final String STEEL_OUTPUT = "STEEL_OUTPUT";
    public static final String CEMENT_OUTPUT = "CEMENT_OUTPUT";
    public static final String COAL_PRICE = "COAL_PRICE";
    public static final String CRUDE_OIL_PRICE = "CRUDE_OIL_PRICE";
    public static final String WEATHER_LEVEL = "WEATHER_LEVEL";
    public static final String POLICY_SENTIMENT = "POLICY_SENTIMENT";
}
