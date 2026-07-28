package com.carbon.carbon.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_prediction_result")
public class PredictionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prediction_time")
    private LocalDateTime predictionTime;

    @Column(name = "prediction_horizon")
    private Integer predictionHorizon;

    @Column(name = "base_price", precision = 18, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "predicted_price", precision = 18, scale = 2)
    private BigDecimal predictedPrice;

    @Column(name = "lower_bound_80", precision = 18, scale = 2)
    private BigDecimal lowerBound80;

    @Column(name = "upper_bound_80", precision = 18, scale = 2)
    private BigDecimal upperBound80;

    @Column(name = "lower_bound_95", precision = 18, scale = 2)
    private BigDecimal lowerBound95;

    @Column(name = "upper_bound_95", precision = 18, scale = 2)
    private BigDecimal upperBound95;

    @Column(name = "probability_distribution", columnDefinition = "JSON")
    private String probabilityDistribution;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "input_snapshot", columnDefinition = "JSON")
    private String inputSnapshot;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getPredictionTime() { return predictionTime; }
    public void setPredictionTime(LocalDateTime predictionTime) { this.predictionTime = predictionTime; }

    public Integer getPredictionHorizon() { return predictionHorizon; }
    public void setPredictionHorizon(Integer predictionHorizon) { this.predictionHorizon = predictionHorizon; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getPredictedPrice() { return predictedPrice; }
    public void setPredictedPrice(BigDecimal predictedPrice) { this.predictedPrice = predictedPrice; }

    public BigDecimal getLowerBound80() { return lowerBound80; }
    public void setLowerBound80(BigDecimal lowerBound80) { this.lowerBound80 = lowerBound80; }

    public BigDecimal getUpperBound80() { return upperBound80; }
    public void setUpperBound80(BigDecimal upperBound80) { this.upperBound80 = upperBound80; }

    public BigDecimal getLowerBound95() { return lowerBound95; }
    public void setLowerBound95(BigDecimal lowerBound95) { this.lowerBound95 = lowerBound95; }

    public BigDecimal getUpperBound95() { return upperBound95; }
    public void setUpperBound95(BigDecimal upperBound95) { this.upperBound95 = upperBound95; }

    public String getProbabilityDistribution() { return probabilityDistribution; }
    public void setProbabilityDistribution(String probabilityDistribution) { this.probabilityDistribution = probabilityDistribution; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getInputSnapshot() { return inputSnapshot; }
    public void setInputSnapshot(String inputSnapshot) { this.inputSnapshot = inputSnapshot; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
