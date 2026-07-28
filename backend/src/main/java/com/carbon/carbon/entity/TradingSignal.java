package com.carbon.carbon.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_trading_signal")
public class TradingSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "signal_time")
    private LocalDateTime signalTime;

    @Column(name = "signal_type", length = 20)
    private String signalType;

    @Column(name = "confidence_level", length = 10)
    private String confidenceLevel;

    @Column(name = "price_range_start", precision = 18, scale = 2)
    private BigDecimal priceRangeStart;

    @Column(name = "price_range_end", precision = 18, scale = 2)
    private BigDecimal priceRangeEnd;

    @Column(name = "threshold_description", length = 200)
    private String thresholdDescription;

    @Column(name = "key_factors", columnDefinition = "JSON")
    private String keyFactors;

    @Column(name = "factor_contribution", columnDefinition = "JSON")
    private String factorContribution;

    @Column(name = "prediction_id")
    private Long predictionId;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // Signal type constants
    public static final String BUY = "BUY";
    public static final String SELL = "SELL";
    public static final String HOLD = "HOLD";

    // Confidence level constants
    public static final String HIGH = "HIGH";
    public static final String MEDIUM = "MEDIUM";
    public static final String LOW = "LOW";

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getSignalTime() { return signalTime; }
    public void setSignalTime(LocalDateTime signalTime) { this.signalTime = signalTime; }

    public String getSignalType() { return signalType; }
    public void setSignalType(String signalType) { this.signalType = signalType; }

    public String getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(String confidenceLevel) { this.confidenceLevel = confidenceLevel; }

    public BigDecimal getPriceRangeStart() { return priceRangeStart; }
    public void setPriceRangeStart(BigDecimal priceRangeStart) { this.priceRangeStart = priceRangeStart; }

    public BigDecimal getPriceRangeEnd() { return priceRangeEnd; }
    public void setPriceRangeEnd(BigDecimal priceRangeEnd) { this.priceRangeEnd = priceRangeEnd; }

    public String getThresholdDescription() { return thresholdDescription; }
    public void setThresholdDescription(String thresholdDescription) { this.thresholdDescription = thresholdDescription; }

    public String getKeyFactors() { return keyFactors; }
    public void setKeyFactors(String keyFactors) { this.keyFactors = keyFactors; }

    public String getFactorContribution() { return factorContribution; }
    public void setFactorContribution(String factorContribution) { this.factorContribution = factorContribution; }

    public Long getPredictionId() { return predictionId; }
    public void setPredictionId(Long predictionId) { this.predictionId = predictionId; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
