package com.carbon.carbon.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_signal_tracking")
public class SignalTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "signal_id")
    private Long signalId;

    @Column(name = "actual_price", precision = 18, scale = 2)
    private BigDecimal actualPrice;

    @Column(name = "price_check_time")
    private LocalDateTime priceCheckTime;

    @Column(name = "signal_result", length = 20)
    private String signalResult;

    @Column(name = "return_rate", precision = 10, scale = 4)
    private BigDecimal returnRate;

    @Column(name = "evaluation_notes", length = 500)
    private String evaluationNotes;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // Signal result constants
    public static final String PROFIT = "PROFIT";
    public static final String LOSS = "LOSS";
    public static final String HOLD_RESULT = "HOLD";

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSignalId() { return signalId; }
    public void setSignalId(Long signalId) { this.signalId = signalId; }

    public BigDecimal getActualPrice() { return actualPrice; }
    public void setActualPrice(BigDecimal actualPrice) { this.actualPrice = actualPrice; }

    public LocalDateTime getPriceCheckTime() { return priceCheckTime; }
    public void setPriceCheckTime(LocalDateTime priceCheckTime) { this.priceCheckTime = priceCheckTime; }

    public String getSignalResult() { return signalResult; }
    public void setSignalResult(String signalResult) { this.signalResult = signalResult; }

    public BigDecimal getReturnRate() { return returnRate; }
    public void setReturnRate(BigDecimal returnRate) { this.returnRate = returnRate; }

    public String getEvaluationNotes() { return evaluationNotes; }
    public void setEvaluationNotes(String evaluationNotes) { this.evaluationNotes = evaluationNotes; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
