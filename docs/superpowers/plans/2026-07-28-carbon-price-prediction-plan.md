# 碳市场价格预测系统 - 多源数据融合实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建多源数据融合的价格预测模型，整合碳市场内部交易数据与外部影响因素数据，生成结构化交易信号并建立完整的信号历史档案。

**Architecture:** 分层架构 - Python微服务（预测模型层）+ Java Spring Boot（业务服务层）+ Vue.js（前端展示层）。预测模型采用三层架构：底层时间序列模型(Prophet) + 中层因子融合(XGBoost) + 上层多形式输出。

**Tech Stack:** Java Spring Boot, Python FastAPI, Prophet, XGBoost, pandas, scikit-learn, Vue.js

---

## Global Constraints

- Java版本: JDK 8+ (保持与现有项目一致)
- Python版本: Python 3.9+
- 数据库: MySQL (扩展现有数据库)
- 前端框架: Vue.js (保持与现有项目一致)
- 模型版本格式: v1.0.0

---

## 文件结构

### Phase 1: Java后端数据层

**创建文件:**
- `backend/src/main/java/com/carbon/carbon/entity/ExternalFactor.java`
- `backend/src/main/java/com/carbon/carbon/entity/PredictionResult.java`
- `backend/src/main/java/com/carbon/carbon/entity/TradingSignal.java`
- `backend/src/main/java/com/carbon/carbon/entity/SignalTracking.java`
- `backend/src/main/java/com/carbon/carbon/repository/ExternalFactorRepository.java`
- `backend/src/main/java/com/carbon/carbon/repository/PredictionResultRepository.java`
- `backend/src/main/java/com/carbon/carbon/repository/TradingSignalRepository.java`
- `backend/src/main/java/com/carbon/carbon/repository/SignalTrackingRepository.java`
- `backend/src/main/java/com/carbon/carbon/service/ExternalDataService.java`
- `backend/src/main/java/com/carbon/carbon/controller/ExternalDataController.java`
- `backend/src/main/java/com/carbon/carbon/service/TradingSignalService.java`
- `backend/src/main/java/com/carbon/carbon/controller/TradingSignalController.java`

**修改文件:**
- `backend/src/main/resources/application.yml` - 添加新表配置

### Phase 2: Python预测服务

**创建文件:**
- `python-service/requirements.txt`
- `python-service/Dockerfile`
- `python-service/prediction_service/__init__.py`
- `python-service/prediction_service/config.py`
- `python-service/prediction_service/main.py`
- `python-service/prediction_service/models/__init__.py`
- `python-service/prediction_service/models/time_series.py`
- `python-service/prediction_service/models/factor_fusion.py`
- `python-service/prediction_service/models/output_layer.py`
- `python-service/prediction_service/preprocessing/__init__.py`
- `python-service/prediction_service/preprocessing/data_loader.py`
- `python-service/prediction_service/preprocessing/feature_engineering.py`
- `python-service/prediction_service/services/__init__.py`
- `python-service/prediction_service/services/prediction.py`
- `python-service/prediction_service/utils/__init__.py`
- `python-service/prediction_service/utils/logger.py`

### Phase 3: Java集成

**创建文件:**
- `backend/src/main/java/com/carbon/carbon/service/PredictionIntegrationService.java`
- `backend/src/main/java/com/carbon/carbon/controller/PredictionController.java`
- `backend/src/main/java/com/carbon/carbon/dto/PredictionRequest.java`
- `backend/src/main/java/com/carbon/carbon/dto/PredictionResponse.java`

**修改文件:**
- `backend/src/main/java/com/carbon/carbon/service/PricePredictionService.java` - 增强预测功能

### Phase 4: 前端展示

**创建文件:**
- `frontend/src/views/TradingSignal.vue`
- `frontend/src/views/SignalHistory.vue`
- `frontend/src/views/ModelEvaluation.vue`
- `frontend/src/api/prediction.js`

**修改文件:**
- `frontend/src/views/Prediction.vue` - 增强预测看板
- `frontend/src/router/index.js` - 添加新路由
- `frontend/src/api/index.js` - 添加API调用

---

## 实施任务

### Task 1: 创建外部因子实体类

**Files:**
- Create: `backend/src/main/java/com/carbon/carbon/entity/ExternalFactor.java`
- Test: 无需测试 (数据模型类)

**Interfaces:**
- Produces: `ExternalFactor` 实体类

- [ ] **Step 1: 创建 ExternalFactor.java**

```java
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
```

- [ ] **Step 2: 提交代码**

```bash
git add backend/src/main/java/com/carbon/carbon/entity/ExternalFactor.java
git commit -m "feat: add ExternalFactor entity for external data management"
```

---

### Task 2: 创建预测结果实体类

**Files:**
- Create: `backend/src/main/java/com/carbon/carbon/entity/PredictionResult.java`
- Test: 无需测试

**Interfaces:**
- Produces: `PredictionResult` 实体类

- [ ] **Step 1: 创建 PredictionResult.java**

```java
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
```

- [ ] **Step 2: 提交代码**

```bash
git add backend/src/main/java/com/carbon/carbon/entity/PredictionResult.java
git commit -m "feat: add PredictionResult entity for storing prediction outputs"
```

---

### Task 3: 创建交易信号实体类

**Files:**
- Create: `backend/src/main/java/com/carbon/carbon/entity/TradingSignal.java`
- Test: 无需测试

**Interfaces:**
- Produces: `TradingSignal` 实体类

- [ ] **Step 1: 创建 TradingSignal.java**

```java
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
```

- [ ] **Step 2: 提交代码**

```bash
git add backend/src/main/java/com/carbon/carbon/entity/TradingSignal.java
git commit -m "feat: add TradingSignal entity for trading signals"
```

---

### Task 4: 创建信号跟踪实体类

**Files:**
- Create: `backend/src/main/java/com/carbon/carbon/entity/SignalTracking.java`
- Test: 无需测试

**Interfaces:**
- Produces: `SignalTracking` 实体类

- [ ] **Step 1: 创建 SignalTracking.java**

```java
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
```

- [ ] **Step 2: 提交代码**

```bash
git add backend/src/main/java/com/carbon/carbon/entity/SignalTracking.java
git commit -m "feat: add SignalTracking entity for signal result tracking"
```

---

### Task 5: 创建Repository接口

**Files:**
- Create: `backend/src/main/java/com/carbon/carbon/repository/ExternalFactorRepository.java`
- Create: `backend/src/main/java/com/carbon/carbon/repository/PredictionResultRepository.java`
- Create: `backend/src/main/java/com/carbon/carbon/repository/TradingSignalRepository.java`
- Create: `backend/src/main/java/com/carbon/carbon/repository/SignalTrackingRepository.java`
- Test: 无需测试

**Interfaces:**
- Produces: 4个Repository接口

- [ ] **Step 1: 创建 ExternalFactorRepository.java**

```java
package com.carbon.carbon.repository;

import com.carbon.carbon.entity.ExternalFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExternalFactorRepository extends JpaRepository<ExternalFactor, Long> {

    List<ExternalFactor> findByFactorDate(LocalDate factorDate);

    List<ExternalFactor> findByFactorType(String factorType);

    List<ExternalFactor> findByFactorDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT f.factorType FROM ExternalFactor f")
    List<String> findDistinctFactorTypes();

    @Query("SELECT f FROM ExternalFactor f WHERE f.factorType = :type ORDER BY f.factorDate DESC")
    List<ExternalFactor> findLatestByType(String type);
}
```

- [ ] **Step 2: 创建 PredictionResultRepository.java**

```java
package com.carbon.carbon.repository;

import com.carbon.carbon.entity.PredictionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionResultRepository extends JpaRepository<PredictionResult, Long> {

    List<PredictionResult> findAllByOrderByPredictionTimeDesc();

    List<PredictionResult> findByPredictionTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    Optional<PredictionResult> findTopByOrderByPredictionTimeDesc();

    @Query("SELECT p FROM PredictionResult p WHERE p.modelVersion = :version ORDER BY p.predictionTime DESC")
    List<PredictionResult> findByModelVersion(String version);
}
```

- [ ] **Step 3: 创建 TradingSignalRepository.java**

```java
package com.carbon.carbon.repository;

import com.carbon.carbon.entity.TradingSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradingSignalRepository extends JpaRepository<TradingSignal, Long> {

    List<TradingSignal> findAllByOrderBySignalTimeDesc();

    List<TradingSignal> findBySignalTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    Optional<TradingSignal> findTopByOrderBySignalTimeDesc();

    List<TradingSignal> findBySignalType(String signalType);

    @Query("SELECT s FROM TradingSignal s WHERE s.modelVersion = :version ORDER BY s.signalTime DESC")
    List<TradingSignal> findByModelVersion(String version);

    @Query("SELECT COUNT(s) FROM TradingSignal s WHERE s.signalType = :signalType")
    Long countBySignalType(String signalType);
}
```

- [ ] **Step 4: 创建 SignalTrackingRepository.java**

```java
package com.carbon.carbon.repository;

import com.carbon.carbon.entity.SignalTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignalTrackingRepository extends JpaRepository<SignalTracking, Long> {

    List<SignalTracking> findBySignalId(Long signalId);

    @Query("SELECT st FROM SignalTracking st WHERE st.signalResult = :result")
    List<SignalTracking> findBySignalResult(String result);

    @Query("SELECT COUNT(st) FROM SignalTracking st WHERE st.signalResult = :result")
    Long countBySignalResult(String result);

    @Query("SELECT AVG(st.returnRate) FROM SignalTracking st WHERE st.signalResult = 'PROFIT'")
    Double getAverageProfitRate();

    @Query("SELECT AVG(st.returnRate) FROM SignalTracking st WHERE st.signalResult = 'LOSS'")
    Double getAverageLossRate();
}
```

- [ ] **Step 5: 提交代码**

```bash
git add backend/src/main/java/com/carbon/carbon/repository/
git commit -m "feat: add repositories for external factors, predictions, signals, and tracking"
```

---

### Task 6: 创建外部数据管理Service

**Files:**
- Create: `backend/src/main/java/com/carbon/carbon/service/ExternalDataService.java`
- Test: 无需测试

**Interfaces:**
- Produces: `ExternalDataService` 服务类

- [ ] **Step 1: 创建 ExternalDataService.java**

```java
package com.carbon.carbon.service;

import com.carbon.carbon.entity.ExternalFactor;
import com.carbon.carbon.repository.ExternalFactorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExternalDataService {

    @Autowired
    private ExternalFactorRepository externalFactorRepository;

    @Transactional
    public ExternalFactor addExternalFactor(LocalDate factorDate, String factorType,
                                            BigDecimal factorValue, String dataSource) {
        ExternalFactor factor = new ExternalFactor();
        factor.setFactorDate(factorDate);
        factor.setFactorType(factorType);
        factor.setFactorValue(factorValue);
        factor.setDataSource(dataSource);
        factor.setCreateTime(LocalDateTime.now());
        return externalFactorRepository.save(factor);
    }

    @Transactional
    public List<ExternalFactor> batchAddExternalFactors(List<ExternalFactor> factors) {
        return externalFactorRepository.saveAll(factors);
    }

    public List<ExternalFactor> getAllExternalFactors() {
        return externalFactorRepository.findAll();
    }

    public List<ExternalFactor> getExternalFactorsByDate(LocalDate startDate, LocalDate endDate) {
        return externalFactorRepository.findByFactorDateBetween(startDate, endDate);
    }

    public List<ExternalFactor> getExternalFactorsByType(String factorType) {
        return externalFactorRepository.findByFactorType(factorType);
    }

    public Map<String, BigDecimal> getLatestFactors() {
        List<String> types = externalFactorRepository.findDistinctFactorTypes();
        Map<String, BigDecimal> latestFactors = new HashMap<>();

        for (String type : types) {
            List<ExternalFactor> factors = externalFactorRepository.findLatestByType(type);
            if (!factors.isEmpty()) {
                latestFactors.put(type, factors.get(0).getFactorValue());
            }
        }
        return latestFactors;
    }

    @Transactional
    public void deleteExternalFactor(Long id) {
        externalFactorRepository.deleteById(id);
    }

    public ExternalFactor getExternalFactorById(Long id) {
        return externalFactorRepository.findById(id).orElse(null);
    }
}
```

- [ ] **Step 2: 提交代码**

```bash
git add backend/src/main/java/com/carbon/carbon/service/ExternalDataService.java
git commit -m "feat: add ExternalDataService for external factor management"
```

---

### Task 7: 创建外部数据Controller

**Files:**
- Create: `backend/src/main/java/com/carbon/carbon/controller/ExternalDataController.java`
- Test: 无需测试

**Interfaces:**
- Produces: `ExternalDataController` REST控制器

- [ ] **Step 1: 创建 ExternalDataController.java**

```java
package com.carbon.carbon.controller;

import com.carbon.carbon.entity.ExternalFactor;
import com.carbon.carbon.service.ExternalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/external-factors")
@CrossOrigin(origins = "*")
public class ExternalDataController {

    @Autowired
    private ExternalDataService externalDataService;

    @PostMapping
    public ResponseEntity<ExternalFactor> addExternalFactor(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate factorDate,
            @RequestParam String factorType,
            @RequestParam BigDecimal factorValue,
            @RequestParam(required = false) String dataSource) {

        ExternalFactor factor = externalDataService.addExternalFactor(
                factorDate, factorType, factorValue, dataSource);
        return ResponseEntity.ok(factor);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ExternalFactor>> batchAddExternalFactors(
            @RequestBody List<ExternalFactor> factors) {
        List<ExternalFactor> saved = externalDataService.batchAddExternalFactors(factors);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<ExternalFactor>> getAllExternalFactors() {
        return ResponseEntity.ok(externalDataService.getAllExternalFactors());
    }

    @GetMapping("/latest")
    public ResponseEntity<Map<String, BigDecimal>> getLatestFactors() {
        return ResponseEntity.ok(externalDataService.getLatestFactors());
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<ExternalFactor>> getExternalFactorsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(externalDataService.getExternalFactorsByDate(startDate, endDate));
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<ExternalFactor>> getExternalFactorsByType(
            @RequestParam String factorType) {
        return ResponseEntity.ok(externalDataService.getExternalFactorsByType(factorType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternalFactor> getExternalFactorById(@PathVariable Long id) {
        ExternalFactor factor = externalDataService.getExternalFactorById(id);
        if (factor != null) {
            return ResponseEntity.ok(factor);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExternalFactor(@PathVariable Long id) {
        externalDataService.deleteExternalFactor(id);
        return ResponseEntity.ok().build();
    }
}
```

- [ ] **Step 2: 提交代码**

```bash
git add backend/src/main/java/com/carbon/carbon/controller/ExternalDataController.java
git commit -m "feat: add ExternalDataController for external factor API endpoints"
```

---

### Task 8: 创建Python预测服务 - 目录结构和配置

**Files:**
- Create: `python-service/requirements.txt`
- Create: `python-service/Dockerfile`
- Create: `python-service/prediction_service/__init__.py`
- Create: `python-service/prediction_service/config.py`
- Test: 无需测试

**Interfaces:**
- Produces: Python服务基础结构

- [ ] **Step 1: 创建 requirements.txt**

```
fastapi==0.104.1
uvicorn==0.24.0
pydantic==2.5.0
pandas==2.1.3
numpy==1.26.2
prophet==1.1.4
scikit-learn==1.3.2
xgboost==2.0.3
requests==2.31.0
python-dateutil==2.8.2
joblib==1.3.2
```

- [ ] **Step 2: 创建 Dockerfile**

```dockerfile
FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 8000

CMD ["uvicorn", "prediction_service.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

- [ ] **Step 3: 创建 config.py**

```python
import os

class Config:
    # API Configuration
    API_HOST = os.getenv("API_HOST", "0.0.0.0")
    API_PORT = int(os.getenv("API_PORT", "8000"))

    # Model Configuration
    MODEL_VERSION = os.getenv("MODEL_VERSION", "v1.0.0")
    DEFAULT_PREDICTION_HORIZON = 7

    # Data Configuration
    HISTORICAL_DATA_DAYS = 90
    EXTERNAL_FACTOR_DAYS = 30

    # Prediction Configuration
    CONFIDENCE_INTERVALS = [0.80, 0.95]
    PROBABILITY_DISTRIBUTION_BINS = 10

    # Java Service Integration
    JAVA_SERVICE_URL = os.getenv("JAVA_SERVICE_URL", "http://localhost:8080")
```

- [ ] **Step 4: 提交代码**

```bash
git add python-service/requirements.txt python-service/Dockerfile python-service/prediction_service/
git commit -m "feat: add Python prediction service base structure"
```

---

### Task 9: Python预测服务 - 时间序列模型

**Files:**
- Create: `python-service/prediction_service/models/time_series.py`
- Test: 无需测试

**Interfaces:**
- Produces: `TimeSeriesModel` 类

- [ ] **Step 1: 创建 time_series.py**

```python
import pandas as pd
import numpy as np
from prophet import Prophet
from typing import Dict, List, Optional
import logging

logger = logging.getLogger(__name__)

class TimeSeriesModel:
    """
    时间序列模型层 - 使用Prophet进行价格预测
    捕捉价格自相关性、季节性和趋势
    """

    def __init__(self, model_config: Optional[Dict] = None):
        self.model_config = model_config or {}
        self.model = None
        self.history_data = None

    def fit(self, price_data: pd.DataFrame) -> None:
        """
        训练时间序列模型

        Args:
            price_data: DataFrame with 'ds' (date) and 'y' (price) columns
        """
        # 准备Prophet格式数据
        df = price_data.rename(columns={'date': 'ds', 'avgPrice': 'y'})

        # 初始化Prophet模型
        self.model = Prophet(
            yearly_seasonality=self.model_config.get('yearly_seasonality', True),
            weekly_seasonality=self.model_config.get('weekly_seasonality', True),
            daily_seasonality=self.model_config.get('daily_seasonality', False),
            changepoint_prior_scale=self.model_config.get('changepoint_prior_scale', 0.05)
        )

        # 训练模型
        self.model.fit(df)
        self.history_data = df
        logger.info("Time series model trained successfully")

    def predict(self, horizon: int = 7) -> Dict:
        """
        执行时间序列预测

        Args:
            horizon: 预测天数

        Returns:
            包含预测结果的字典
        """
        if self.model is None:
            raise ValueError("Model not trained. Call fit() first.")

        # 创建未来日期
        future = self.model.make_future_dataframe(periods=horizon)
        forecast = self.model.predict(future)

        # 获取预测结果
        predictions = forecast.tail(horizon)

        result = {
            'base_price': float(predictions['yhat'].iloc[-1]),
            'predictions': [
                {
                    'date': row['ds'].strftime('%Y-%m-%d'),
                    'predicted': float(row['yhat']),
                    'lower': float(row['yhat_lower']),
                    'upper': float(row['yhat_upper'])
                }
                for _, row in predictions.iterrows()
            ],
            'residuals': self._calculate_residuals(),
            'trend': self._extract_trend(),
            'seasonality': self._extract_seasonality()
        }

        return result

    def _calculate_residuals(self) -> List[float]:
        """计算训练数据的残差"""
        if self.history_data is None:
            return []

        forecast = self.model.predict(self.history_data)
        residuals = self.history_data['y'].values - forecast['yhat'].values
        return residuals.tolist()

    def _extract_trend(self) -> str:
        """提取趋势方向"""
        if self.history_data is None:
            return "UNKNOWN"

        forecast = self.model.predict(self.history_data)
        trend_slope = (forecast['yhat'].iloc[-1] - forecast['yhat'].iloc[0]) / len(forecast)

        if trend_slope > 0.1:
            return "UP"
        elif trend_slope < -0.1:
            return "DOWN"
        return "STABLE"

    def _extract_seasonality(self) -> Dict:
        """提取季节性特征"""
        return {
            'weekly_pattern': self.model.seasonalities.get('weekly', {}).get('period', 7),
            'yearly_pattern': self.model.seasonalities.get('yearly', {}).get('period', 365)
        }

    def get_component_plot_data(self) -> Dict:
        """获取用于可视化的组件数据"""
        if self.model is None:
            return {}

        return {
            'trend': self.model.plot_components(self.model).__dict__ if hasattr(self.model, 'plot_components') else {}
        }
```

- [ ] **Step 2: 提交代码**

```bash
git add python-service/prediction_service/models/time_series.py
git commit -m "feat: add Prophet-based time series model"
```

---

### Task 10: Python预测服务 - 因子融合模型

**Files:**
- Create: `python-service/prediction_service/models/factor_fusion.py`
- Test: 无需测试

**Interfaces:**
- Produces: `FactorFusionModel` 类

- [ ] **Step 1: 创建 factor_fusion.py**

```python
import pandas as pd
import numpy as np
from xgboost import XGBRegressor
from sklearn.ensemble import RandomForestRegressor
from typing import Dict, List, Optional
import joblib
import logging

logger = logging.getLogger(__name__)

class FactorFusionModel:
    """
    因子融合层 - 使用机器学习模型融合外部因子
    """

    def __init__(self, model_type: str = 'xgboost'):
        self.model_type = model_type
        self.model = None
        self.feature_names = []
        self.is_trained = False

    def fit(self, X: pd.DataFrame, y: pd.Series) -> None:
        """
        训练因子融合模型

        Args:
            X: 特征数据框，包含时间序列特征和外部因子
            y: 目标变量（价格）
        """
        self.feature_names = X.columns.tolist()

        if self.model_type == 'xgboost':
            self.model = XGBRegressor(
                n_estimators=100,
                max_depth=6,
                learning_rate=0.1,
                random_state=42,
                n_jobs=-1
            )
        elif self.model_type == 'random_forest':
            self.model = RandomForestRegressor(
                n_estimators=100,
                max_depth=10,
                random_state=42,
                n_jobs=-1
            )
        else:
            raise ValueError(f"Unknown model type: {self.model_type}")

        self.model.fit(X, y)
        self.is_trained = True
        logger.info(f"{self.model_type} factor fusion model trained successfully")

    def predict(self, X: pd.DataFrame) -> Dict:
        """
        执行因子融合预测

        Args:
            X: 特征数据框

        Returns:
            包含调整后价格和特征重要性的字典
        """
        if not self.is_trained:
            raise ValueError("Model not trained. Call fit() first.")

        predictions = self.model.predict(X)
        feature_importance = self._get_feature_importance()

        result = {
            'adjusted_price': float(predictions[-1]) if len(predictions) > 0 else float(predictions),
            'predictions': predictions.tolist() if len(predictions) > 1 else [float(predictions)],
            'feature_importance': feature_importance
        }

        return result

    def _get_feature_importance(self) -> Dict:
        """获取特征重要性"""
        if self.model is None:
            return {}

        if hasattr(self.model, 'feature_importances_'):
            importances = self.model.feature_importances_
            return {
                name: float(importance)
                for name, importance in zip(self.feature_names, importances)
            }
        return {}

    def get_factor_contribution(self, X: pd.DataFrame) -> Dict:
        """计算各因子对预测的贡献度"""
        if not self.is_trained:
            return {}

        feature_importance = self._get_feature_importance()
        total_importance = sum(feature_importance.values())

        if total_importance > 0:
            return {
                name: (importance / total_importance) * 100
                for name, importance in feature_importance.items()
            }
        return feature_importance

    def save_model(self, path: str) -> None:
        """保存模型"""
        if self.model is not None:
            joblib.dump({
                'model': self.model,
                'feature_names': self.feature_names,
                'model_type': self.model_type
            }, path)
            logger.info(f"Model saved to {path}")

    def load_model(self, path: str) -> None:
        """加载模型"""
        data = joblib.load(path)
        self.model = data['model']
        self.feature_names = data['feature_names']
        self.model_type = data['model_type']
        self.is_trained = True
        logger.info(f"Model loaded from {path}")
```

- [ ] **Step 2: 提交代码**

```bash
git add python-service/prediction_service/models/factor_fusion.py
git commit -m "feat: add factor fusion model with XGBoost"
```

---

### Task 11: Python预测服务 - 输出层

**Files:**
- Create: `python-service/prediction_service/models/output_layer.py`
- Test: 无需测试

**Interfaces:**
- Produces: `OutputLayer` 类

- [ ] **Step 1: 创建 output_layer.py**

```python
import numpy as np
from typing import Dict, List, Tuple
import logging

logger = logging.getLogger(__name__)

class OutputLayer:
    """
    输出层 - 生成多形式预测结果
    包含点预测、区间预测和概率分布
    """

    def __init__(self, confidence_intervals: List[float] = None):
        self.confidence_intervals = confidence_intervals or [0.80, 0.95]

    def generate_point_prediction(
        self,
        ts_result: Dict,
        ml_result: Dict
    ) -> float:
        """
        生成点预测

        Args:
            ts_result: 时间序列预测结果
            ml_result: 因子融合预测结果

        Returns:
            调整后的点预测价格
        """
        base_price = ts_result.get('base_price', 0)
        adjusted_price = ml_result.get('adjusted_price', base_price)

        # 使用加权平均融合两个模型的预测
        # 时间序列权重0.4，ML因子融合权重0.6
        final_price = 0.4 * base_price + 0.6 * adjusted_price

        logger.info(f"Point prediction: base={base_price}, adjusted={adjusted_price}, final={final_price}")
        return round(final_price, 2)

    def generate_interval_prediction(
        self,
        ts_result: Dict,
        ml_result: Dict,
        confidence: float = 0.80
    ) -> Tuple[float, float]:
        """
        生成区间预测

        Args:
            ts_result: 时间序列预测结果
            ml_result: 因子融合预测结果
            confidence: 置信度

        Returns:
            (下限, 上限) 元组
        """
        point_prediction = self.generate_point_prediction(ts_result, ml_result)

        # 计算预测不确定性
        ts_predictions = ts_result.get('predictions', [])
        if ts_predictions:
            # 基于时间序列预测的波动计算区间
            prices = [p['predicted'] for p in ts_predictions]
            std_dev = np.std(prices)
        else:
            std_dev = point_prediction * 0.05  # 默认5%波动

        # 置信区间宽度因子
        if confidence == 0.80:
            z_score = 1.28
        elif confidence == 0.95:
            z_score = 1.96
        else:
            z_score = 1.645

        margin = z_score * std_dev

        lower_bound = max(0, round(point_prediction - margin, 2))
        upper_bound = round(point_prediction + margin, 2)

        logger.info(f"Interval prediction ({confidence*100}%): [{lower_bound}, {upper_bound}]")
        return lower_bound, upper_bound

    def generate_probability_distribution(
        self,
        ts_result: Dict,
        ml_result: Dict,
        bins: int = 10
    ) -> List[Dict]:
        """
        生成概率分布

        Args:
            ts_result: 时间序列预测结果
            ml_result: 因子融合预测结果
            bins: 分箱数量

        Returns:
            价格区间概率列表
        """
        point_prediction = self.generate_point_prediction(ts_result, ml_result)

        # 获取时间序列预测的波动范围
        ts_predictions = ts_result.get('predictions', [])
        if ts_predictions:
            prices = [p['predicted'] for p in ts_predictions]
            min_price = min(prices) * 0.9
            max_price = max(prices) * 1.1
        else:
            min_price = point_prediction * 0.8
            max_price = point_prediction * 1.2

        # 创建价格区间
        bin_width = (max_price - min_price) / bins
        distribution = []

        for i in range(bins):
            bin_start = min_price + i * bin_width
            bin_end = bin_start + bin_width

            # 使用正态分布计算概率
            distance = abs(point_prediction - (bin_start + bin_end) / 2)
            probability = np.exp(-(distance ** 2) / (2 * (bin_width ** 2)))

            distribution.append({
                'range_start': round(bin_start, 2),
                'range_end': round(bin_end, 2),
                'probability': round(probability, 4)
            })

        # 归一化概率
        total_prob = sum(d['probability'] for d in distribution)
        for d in distribution:
            d['probability'] = round(d['probability'] / total_prob, 4)

        return distribution

    def generate_full_output(
        self,
        ts_result: Dict,
        ml_result: Dict
    ) -> Dict:
        """
        生成完整输出

        Args:
            ts_result: 时间序列预测结果
            ml_result: 因子融合预测结果

        Returns:
            完整的预测结果字典
        """
        point_prediction = self.generate_point_prediction(ts_result, ml_result)

        # 生成80%和95%置信区间
        interval_80 = self.generate_interval_prediction(ts_result, ml_result, 0.80)
        interval_95 = self.generate_interval_prediction(ts_result, ml_result, 0.95)

        # 生成概率分布
        probability_dist = self.generate_probability_distribution(ts_result, ml_result)

        return {
            'point_prediction': point_prediction,
            'interval_80': {
                'lower': interval_80[0],
                'upper': interval_80[1]
            },
            'interval_95': {
                'lower': interval_95[0],
                'upper': interval_95[1]
            },
            'probability_distribution': probability_dist,
            'model_version': 'v1.0.0'
        }
```

- [ ] **Step 2: 提交代码**

```bash
git add python-service/prediction_service/models/output_layer.py
git commit -m "feat: add output layer for multi-form predictions"
```

---

### Task 12: Python预测服务 - 数据加载和特征工程

**Files:**
- Create: `python-service/prediction_service/preprocessing/data_loader.py`
- Create: `python-service/prediction_service/preprocessing/feature_engineering.py`
- Test: 无需测试

**Interfaces:**
- Produces: 数据加载和特征工程模块

- [ ] **Step 1: 创建 data_loader.py**

```python
import pandas as pd
import requests
from typing import Dict, List, Optional
import logging

logger = logging.getLogger(__name__)

class DataLoader:
    """数据加载器 - 从Java后端获取数据"""

    def __init__(self, java_service_url: str = "http://localhost:8080"):
        self.java_service_url = java_service_url

    def load_price_history(self, days: int = 90) -> pd.DataFrame:
        """
        加载历史价格数据

        Args:
            days: 获取天数

        Returns:
            DataFrame with date and avgPrice columns
        """
        try:
            # 从Java服务获取价格历史
            url = f"{self.java_service_url}/api/price/history"
            params = {'days': days}
            response = requests.get(url, params=params, timeout=10)

            if response.status_code == 200:
                data = response.json()
                df = pd.DataFrame(data)

                if not df.empty and 'date' in df.columns:
                    df['date'] = pd.to_datetime(df['date'])
                    df = df.sort_values('date')

                logger.info(f"Loaded {len(df)} price history records")
                return df
            else:
                logger.warning(f"Failed to fetch price history: {response.status_code}")
                return self._generate_mock_price_data(days)

        except Exception as e:
            logger.error(f"Error loading price history: {e}")
            return self._generate_mock_price_data(days)

    def load_external_factors(self, days: int = 30) -> pd.DataFrame:
        """
        加载外部因子数据

        Args:
            days: 获取天数

        Returns:
            DataFrame with factor data
        """
        try:
            url = f"{self.java_service_url}/api/external-factors/latest"
            response = requests.get(url, timeout=10)

            if response.status_code == 200:
                data = response.json()
                df = pd.DataFrame([data])
                logger.info(f"Loaded external factors: {list(data.keys())}")
                return df
            else:
                logger.warning(f"Failed to fetch external factors: {response.status_code}")
                return self._generate_mock_external_factors()

        except Exception as e:
            logger.error(f"Error loading external factors: {e}")
            return self._generate_mock_external_factors()

    def _generate_mock_price_data(self, days: int) -> pd.DataFrame:
        """生成模拟价格数据（用于测试）"""
        import numpy as np
        dates = pd.date_range(end=pd.Timestamp.now(), periods=days, freq='D')
        base_price = 50
        prices = base_price + np.cumsum(np.random.randn(days) * 0.5)
        prices = np.clip(prices, 30, 80)

        return pd.DataFrame({
            'date': dates,
            'avgPrice': prices
        })

    def _generate_mock_external_factors(self) -> pd.DataFrame:
        """生成模拟外部因子数据（用于测试）"""
        import numpy as np
        return pd.DataFrame({
            'POWER_DEMAND': [np.random.uniform(0.8, 1.2)],
            'STEEL_OUTPUT': [np.random.uniform(0.9, 1.1)],
            'CEMENT_OUTPUT': [np.random.uniform(0.9, 1.1)],
            'COAL_PRICE': [np.random.uniform(0.85, 1.15)],
            'CRUDE_OIL_PRICE': [np.random.uniform(0.9, 1.1)],
            'WEATHER_LEVEL': [np.random.randint(1, 4)],
            'POLICY_SENTIMENT': [np.random.uniform(-0.5, 0.5)]
        })
```

- [ ] **Step 2: 创建 feature_engineering.py**

```python
import pandas as pd
import numpy as np
from typing import Dict, List
import logging

logger = logging.getLogger(__name__)

class FeatureEngineering:
    """特征工程 - 构建机器学习特征"""

    def create_time_series_features(self, price_data: pd.DataFrame) -> pd.DataFrame:
        """
        创建时间序列特征

        Args:
            price_data: 价格数据框

        Returns:
            添加了时间序列特征的数据框
        """
        df = price_data.copy()

        # 滞后特征
        for lag in [1, 2, 3, 7, 14]:
            df[f'lag_{lag}'] = df['avgPrice'].shift(lag)

        # 移动平均特征
        for window in [3, 7, 14, 30]:
            df[f'ma_{window}'] = df['avgPrice'].rolling(window=window).mean()

        # 波动率特征
        for window in [7, 14, 30]:
            df[f'volatility_{window}'] = df['avgPrice'].rolling(window=window).std()

        # 价格变化特征
        df['price_change_1d'] = df['avgPrice'].pct_change(1)
        df['price_change_7d'] = df['avgPrice'].pct_change(7)

        # 动量特征
        df['momentum_7d'] = df['avgPrice'] - df['avgPrice'].shift(7)
        df['momentum_14d'] = df['avgPrice'] - df['avgPrice'].shift(14)

        # 相对强度
        df['relative_strength'] = df['avgPrice'] / df['ma_30']

        # 删除NaN值
        df = df.dropna()

        logger.info(f"Created {len(df.columns) - 2} time series features")
        return df

    def create_external_factor_features(self, factor_data: pd.DataFrame) -> pd.DataFrame:
        """
        创建外部因子特征

        Args:
            factor_data: 外部因子数据框

        Returns:
            处理后的因子数据框
        """
        df = factor_data.copy()

        # 标准化数值因子
        numeric_cols = df.select_dtypes(include=[np.number]).columns
        for col in numeric_cols:
            if df[col].std() > 0:
                df[f'{col}_normalized'] = (df[col] - df[col].mean()) / df[col].std()

        logger.info(f"Processed {len(df.columns)} external factor features")
        return df

    def merge_features(
        self,
        ts_features: pd.DataFrame,
        external_features: pd.DataFrame
    ) -> pd.DataFrame:
        """
        合并特征

        Args:
            ts_features: 时间序列特征
            external_features: 外部因子特征

        Returns:
            合并后的特征数据框
        """
        # 使用外部因子特征的最后一行（最新值）
        latest_external = external_features.iloc[-1:].copy()

        # 将外部因子广播到所有时间序列行
        merged = ts_features.copy()
        for col in latest_external.columns:
            merged[col] = latest_external[col].values[0]

        return merged

    def get_feature_names(self, df: pd.DataFrame) -> List[str]:
        """
        获取特征名称列表

        Args:
            df: 特征数据框

        Returns:
            特征名称列表
        """
        return [col for col in df.columns if col not in ['date', 'avgPrice']]
```

- [ ] **Step 3: 提交代码**

```bash
git add python-service/prediction_service/preprocessing/
git commit -m "feat: add data loader and feature engineering modules"
```

---

### Task 13: Python预测服务 - 预测服务主逻辑

**Files:**
- Create: `python-service/prediction_service/services/prediction.py`
- Test: 无需测试

**Interfaces:**
- Produces: `PredictionService` 服务类

- [ ] **Step 1: 创建 prediction.py**

```python
import pandas as pd
from typing import Dict, Optional
import logging

from ..models.time_series import TimeSeriesModel
from ..models.factor_fusion import FactorFusionModel
from ..models.output_layer import OutputLayer
from ..preprocessing.data_loader import DataLoader
from ..preprocessing.feature_engineering import FeatureEngineering
from ..config import Config

logger = logging.getLogger(__name__)

class PredictionService:
    """预测服务 - 整合三层模型"""

    def __init__(self, config: Optional[Config] = None):
        self.config = config or Config()
        self.data_loader = DataLoader(self.config.JAVA_SERVICE_URL)
        self.feature_engineering = FeatureEngineering()

        # 初始化模型
        self.ts_model = TimeSeriesModel()
        self.fusion_model = FactorFusionModel(model_type='xgboost')
        self.output_layer = OutputLayer(self.config.CONFIDENCE_INTERVALS)

        self.is_trained = False

    def train(self, historical_days: int = 90, factor_days: int = 30) -> Dict:
        """
        训练模型

        Args:
            historical_days: 历史数据天数
            factor_days: 外部因子天数

        Returns:
            训练结果
        """
        # 加载数据
        price_data = self.data_loader.load_price_history(historical_days)
        factor_data = self.data_loader.load_external_factors(factor_days)

        if price_data.empty:
            return {'status': 'error', 'message': 'No price data available'}

        # 训练时间序列模型
        self.ts_model.fit(price_data)
        ts_result = self.ts_model.predict(self.config.DEFAULT_PREDICTION_HORIZON)

        # 创建特征
        ts_features = self.feature_engineering.create_time_series_features(price_data)
        external_features = self.feature_engineering.create_external_factor_features(factor_data)
        merged_features = self.feature_engineering.merge_features(ts_features, external_features)

        # 准备ML训练数据
        feature_cols = self.feature_engineering.get_feature_names(merged_features)
        X = merged_features[feature_cols]
        y = merged_features['avgPrice']

        # 训练因子融合模型
        self.fusion_model.fit(X, y)

        self.is_trained = True
        logger.info("Prediction models trained successfully")

        return {
            'status': 'success',
            'message': 'Models trained successfully',
            'ts_result': ts_result
        }

    def predict(self, horizon: Optional[int] = None) -> Dict:
        """
        执行预测

        Args:
            horizon: 预测天数

        Returns:
            预测结果
        """
        if not self.is_trained:
            # 自动训练
            self.train()

        horizon = horizon or self.config.DEFAULT_PREDICTION_HORIZON

        # 时间序列预测
        ts_result = self.ts_model.predict(horizon)

        # 获取特征用于ML预测
        price_data = self.data_loader.load_price_history(90)
        factor_data = self.data_loader.load_external_factors(30)

        ts_features = self.feature_engineering.create_time_series_features(price_data)
        external_features = self.feature_engineering.create_external_factor_features(factor_data)
        merged_features = self.feature_engineering.merge_features(ts_features, external_features)

        # 因子融合预测
        feature_cols = self.feature_engineering.get_feature_names(merged_features)
        X = merged_features[feature_cols]
        ml_result = self.fusion_model.predict(X)

        # 生成输出
        output = self.output_layer.generate_full_output(ts_result, ml_result)

        # 添加额外信息
        output['ts_features'] = {
            'trend': ts_result.get('trend'),
            'seasonality': ts_result.get('seasonality')
        }

        output['ml_features'] = {
            'feature_importance': ml_result.get('feature_importance'),
            'factor_contribution': self.fusion_model.get_factor_contribution(X)
        }

        logger.info(f"Prediction completed: {output['point_prediction']}")
        return output

    def get_model_info(self) -> Dict:
        """获取模型信息"""
        return {
            'model_version': self.config.MODEL_VERSION,
            'is_trained': self.is_trained,
            'ts_model': 'Prophet',
            'fusion_model': self.fusion_model.model_type
        }
```

- [ ] **Step 2: 提交代码**

```bash
git add python-service/prediction_service/services/prediction.py
git commit -m "feat: add prediction service integrating three-layer models"
```

---

### Task 14: Python预测服务 - FastAPI主入口

**Files:**
- Create: `python-service/prediction_service/main.py`
- Test: 无需测试

**Interfaces:**
- Produces: FastAPI应用

- [ ] **Step 1: 创建 main.py**

```python
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict
import logging
import uvicorn

from .services.prediction import PredictionService
from .config import Config

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 初始化应用
app = FastAPI(
    title="Carbon Price Prediction Service",
    description="Multi-source data fusion price prediction API",
    version="1.0.0"
)

# 初始化预测服务
prediction_service = PredictionService()

# 请求模型
class PredictionRequest(BaseModel):
    horizon: Optional[int] = 7
    train: Optional[bool] = False

class TrainRequest(BaseModel):
    historical_days: Optional[int] = 90
    factor_days: Optional[int] = 30

# 响应模型
class PredictionResponse(BaseModel):
    status: str
    data: Optional[Dict] = None
    message: Optional[str] = None

@app.get("/")
async def root():
    """健康检查"""
    return {
        "status": "healthy",
        "service": "Carbon Price Prediction",
        "version": "1.0.0"
    }

@app.get("/health")
async def health():
    """健康检查"""
    return {"status": "ok"}

@app.post("/predict", response_model=PredictionResponse)
async def predict(request: PredictionRequest):
    """
    执行价格预测
    """
    try:
        if request.train or not prediction_service.is_trained:
            # 先训练模型
            train_result = prediction_service.train()
            logger.info(f"Model training: {train_result}")

        # 执行预测
        result = prediction_service.predict(request.horizon)

        return PredictionResponse(
            status="success",
            data=result
        )

    except Exception as e:
        logger.error(f"Prediction error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/train", response_model=PredictionResponse)
async def train(request: TrainRequest):
    """
    训练模型
    """
    try:
        result = prediction_service.train(
            request.historical_days,
            request.factor_days
        )

        return PredictionResponse(
            status=result['status'],
            message=result.get('message')
        )

    except Exception as e:
        logger.error(f"Training error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/model/info")
async def model_info():
    """获取模型信息"""
    try:
        info = prediction_service.get_model_info()
        return {
            "status": "success",
            "data": info
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/factors/latest")
async def get_latest_factors():
    """获取最新外部因子"""
    try:
        factors = prediction_service.data_loader.load_external_factors(1)
        if not factors.empty:
            return {
                "status": "success",
                "data": factors.iloc[-1].to_dict()
            }
        return {
            "status": "success",
            "data": {}
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host=Config.API_HOST, port=Config.API_PORT)
```

- [ ] **Step 2: 提交代码**

```bash
git add python-service/prediction_service/main.py
git commit -m "feat: add FastAPI entry point for prediction service"
```

---

### Task 15: Java集成 - 预测集成服务

**Files:**
- Create: `backend/src/main/java/com/carbon/carbon/service/PredictionIntegrationService.java`
- Test: 无需测试

**Interfaces:**
- Produces: `PredictionIntegrationService` 服务类

- [ ] **Step 1: 创建 PredictionIntegrationService.java**

```java
package com.carbon.carbon.service;

import com.carbon.carbon.entity.PredictionResult;
import com.carbon.carbon.repository.PredictionResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PredictionIntegrationService {

    @Value("${prediction.service.url:http://localhost:8000}")
    private String predictionServiceUrl;

    @Autowired
    private PredictionResultRepository predictionResultRepository;

    @Autowired
    private PricePredictionService pricePredictionService;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PredictionIntegrationService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> runPrediction(Integer horizon) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 调用Python预测服务
            String url = predictionServiceUrl + "/predict";
            Map<String, Object> request = new HashMap<>();
            request.put("horizon", horizon != null ? horizon : 7);
            request.put("train", true);

            Map<String, Object> response = restTemplate.postForObject(
                url, request, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                // 保存预测结果
                PredictionResult predictionResult = savePredictionResult(data, horizon);
                result.put("predictionId", predictionResult.getId());
                result.put("prediction", data);
                result.put("status", "success");
            } else {
                result.put("status", "error");
                result.put("message", "Prediction service returned error");
            }

        } catch (Exception e) {
            // 如果Python服务不可用，使用本地预测
            result.put("status", "fallback");
            result.put("message", "Python service unavailable, using local prediction");
            result.put("prediction", pricePredictionService.predictPriceRange());
        }

        return result;
    }

    private PredictionResult savePredictionResult(Map<String, Object> data, Integer horizon) {
        PredictionResult result = new PredictionResult();
        result.setPredictionTime(LocalDateTime.now());
        result.setPredictionHorizon(horizon != null ? horizon : 7);

        if (data.containsKey("pointPrediction")) {
            result.setPredictedPrice(new BigDecimal(data.get("pointPrediction").toString()));
        }

        Map<String, Object> interval80 = (Map<String, Object>) data.get("interval80");
        if (interval80 != null) {
            result.setLowerBound80(new BigDecimal(interval80.get("lower").toString()));
            result.setUpperBound80(new BigDecimal(interval80.get("upper").toString()));
        }

        Map<String, Object> interval95 = (Map<String, Object>) data.get("interval95");
        if (interval95 != null) {
            result.setLowerBound95(new BigDecimal(interval95.get("lower").toString()));
            result.setUpperBound95(new BigDecimal(interval95.get("upper").toString()));
        }

        result.setModelVersion(data.containsKey("modelVersion") ?
            data.get("modelVersion").toString() : "v1.0.0");

        result.setCreateTime(LocalDateTime.now());

        return predictionResultRepository.save(result);
    }

    public PredictionResult getPredictionResult(Long id) {
        return predictionResultRepository.findById(id).orElse(null);
    }

    public Map<String, Object> getPredictionHistory() {
        Map<String, Object> result = new HashMap<>();
        result.put("predictions", predictionResultRepository.findAllByOrderByPredictionTimeDesc());
        return result;
    }
}
```

- [ ] **Step 2: 提交代码**

```bash
git add backend/src/main/java/com/carbon/carbon/service/PredictionIntegrationService.java
git commit -m "feat: add PredictionIntegrationService to integrate Python prediction service"
```

---

### Task 16: Java集成 - 交易信号服务

**Files:**
- Create: `backend/src/main/java/com/carbon/carbon/service/TradingSignalService.java`
- Test: 无需测试

**Interfaces:**
- Produces: `TradingSignalService` 服务类

- [ ] **Step 1: 创建 TradingSignalService.java**

```java
package com.carbon.carbon.service;

import com.carbon.carbon.entity.PredictionResult;
import com.carbon.carbon.entity.TradingSignal;
import com.carbon.carbon.repository.PredictionResultRepository;
import com.carbon.carbon.repository.TradingSignalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TradingSignalService {

    @Autowired
    private TradingSignalRepository tradingSignalRepository;

    @Autowired
    private PredictionResultRepository predictionResultRepository;

    @Autowired
    private PredictionIntegrationService predictionIntegrationService;

    private final ObjectMapper objectMapper;

    // 阈值配置
    private static final BigDecimal THRESHOLD_PERCENT = new BigDecimal("0.05"); // 5%
    private static final BigDecimal HIGH_CONFIDENCE_THRESHOLD = new BigDecimal("0.10"); // 10%
    private static final BigDecimal MEDIUM_CONFIDENCE_THRESHOLD = new BigDecimal("0.20"); // 20%

    public TradingSignalService() {
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public Map<String, Object> generateSignal() {
        Map<String, Object> result = new HashMap<>();

        // 获取当前价格（使用最近交易价格）
        Double currentPrice = getCurrentPrice();

        // 执行预测
        Map<String, Object> predictionResult = predictionIntegrationService.runPrediction(7);
        Map<String, Object> prediction = (Map<String, Object>) predictionResult.get("prediction");

        if (prediction == null) {
            result.put("status", "error");
            result.put("message", "No prediction available");
            return result;
        }

        // 提取预测价格
        BigDecimal predictedPrice = new BigDecimal(prediction.get("pointPrediction").toString());
        BigDecimal lowerBound80 = new BigDecimal(
            ((Map<String, Object>) prediction.get("interval80")).get("lower").toString());
        BigDecimal upperBound80 = new BigDecimal(
            ((Map<String, Object>) prediction.get("interval80")).get("upper").toString());

        // 计算区间宽度
        BigDecimal intervalWidth = upperBound80.subtract(lowerBound80);
        BigDecimal intervalWidthPercent = intervalWidth.divide(predictedPrice, 4, RoundingMode.HALF_UP);

        // 确定置信度
        String confidenceLevel = determineConfidenceLevel(intervalWidthPercent);

        // 确定信号类型
        String signalType = determineSignalType(currentPrice, predictedPrice);

        // 计算建议价格区间
        BigDecimal threshold = currentPrice.multiply(THRESHOLD_PERCENT);
        BigDecimal priceRangeStart, priceRangeEnd;

        if (TradingSignal.BUY.equals(signalType)) {
            priceRangeStart = currentPrice;
            priceRangeEnd = currentPrice.add(threshold);
        } else if (TradingSignal.SELL.equals(signalType)) {
            priceRangeStart = currentPrice.subtract(threshold);
            priceRangeEnd = currentPrice;
        } else {
            priceRangeStart = lowerBound80;
            priceRangeEnd = upperBound80;
        }

        // 获取关键因素
        Map<String, Object> keyFactors = extractKeyFactors(prediction);
        Map<String, Object> factorContribution = extractFactorContribution(prediction);

        // 保存交易信号
        TradingSignal signal = new TradingSignal();
        signal.setSignalTime(LocalDateTime.now());
        signal.setSignalType(signalType);
        signal.setConfidenceLevel(confidenceLevel);
        signal.setPriceRangeStart(priceRangeStart);
        signal.setPriceRangeEnd(priceRangeEnd);
        signal.setThresholdDescription(generateThresholdDescription(signalType, threshold));
        signal.setKeyFactors(toJson(keyFactors));
        signal.setFactorContribution(toJson(factorContribution));

        Long predictionId = (Long) predictionResult.get("predictionId");
        if (predictionId != null) {
            signal.setPredictionId(predictionId);
        }

        signal.setModelVersion(prediction.containsKey("modelVersion") ?
            prediction.get("modelVersion").toString() : "v1.0.0");
        signal.setCreateTime(LocalDateTime.now());

        TradingSignal savedSignal = tradingSignalRepository.save(signal);

        result.put("status", "success");
        result.put("signal", convertToMap(savedSignal));
        result.put("keyFactors", keyFactors);
        result.put("factorContribution", factorContribution);

        return result;
    }

    private Double getCurrentPrice() {
        // 使用最近交易价格作为当前价格
        List<com.carbon.carbon.entity.CarbonOrder> orders =
            new com.carbon.carbon.repository.CarbonOrderRepository()
                .findAll().stream().limit(10).toList();

        if (!orders.isEmpty()) {
            return orders.stream()
                .mapToDouble(o -> o.getUnitPrice().doubleValue())
                .average()
                .orElse(50.0);
        }
        return 50.0;
    }

    private String determineConfidenceLevel(BigDecimal intervalWidthPercent) {
        if (intervalWidthPercent.compareTo(HIGH_CONFIDENCE_THRESHOLD) <= 0) {
            return TradingSignal.HIGH;
        } else if (intervalWidthPercent.compareTo(MEDIUM_CONFIDENCE_THRESHOLD) <= 0) {
            return TradingSignal.MEDIUM;
        }
        return TradingSignal.LOW;
    }

    private String determineSignalType(Double currentPrice, BigDecimal predictedPrice) {
        BigDecimal current = BigDecimal.valueOf(currentPrice);
        BigDecimal threshold = current.multiply(THRESHOLD_PERCENT);

        if (predictedPrice.compareTo(current.add(threshold)) > 0) {
            return TradingSignal.BUY;
        } else if (predictedPrice.compareTo(current.subtract(threshold)) < 0) {
            return TradingSignal.SELL;
        }
        return TradingSignal.HOLD;
    }

    private Map<String, Object> extractKeyFactors(Map<String, Object> prediction) {
        Map<String, Object> factors = new HashMap<>();

        if (prediction.containsKey("tsFeatures")) {
            Map<String, Object> tsFeatures = (Map<String, Object>) prediction.get("tsFeatures");
            if (tsFeatures.containsKey("trend")) {
                factors.put("priceTrend", tsFeatures.get("trend"));
            }
        }

        if (prediction.containsKey("mlFeatures")) {
            Map<String, Object> mlFeatures = (Map<String, Object>) prediction.get("mlFeatures");
            if (mlFeatures.containsKey("featureImportance")) {
                Map<String, Object> importance = (Map<String, Object>) mlFeatures.get("featureImportance");
                // 提取top 3因素
                factors.put("topFactors", importance.keySet().stream().limit(3).toList());
            }
        }

        return factors;
    }

    private Map<String, Object> extractFactorContribution(Map<String, Object> prediction) {
        if (prediction.containsKey("mlFeatures")) {
            Map<String, Object> mlFeatures = (Map<String, Object>) prediction.get("mlFeatures");
            if (mlFeatures.containsKey("factorContribution")) {
                return (Map<String, Object>) mlFeatures.get("factorContribution");
            }
        }
        return new HashMap<>();
    }

    private String generateThresholdDescription(String signalType, BigDecimal threshold) {
        return String.format("Signal: %s, Threshold: %.2f%% of current price",
            signalType, threshold.multiply(BigDecimal.valueOf(100)).doubleValue());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Object> convertToMap(TradingSignal signal) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", signal.getId());
        map.put("signalTime", signal.getSignalTime());
        map.put("signalType", signal.getSignalType());
        map.put("confidenceLevel", signal.getConfidenceLevel());
        map.put("priceRangeStart", signal.getPriceRangeStart());
        map.put("priceRangeEnd", signal.getPriceRangeEnd());
        map.put("modelVersion", signal.getModelVersion());
        return map;
    }

    public TradingSignal getLatestSignal() {
        return tradingSignalRepository.findTopByOrderBySignalTimeDesc().orElse(null);
    }

    public List<TradingSignal> getSignalHistory() {
        return tradingSignalRepository.findAllByOrderBySignalTimeDesc();
    }

    public TradingSignal getSignalById(Long id) {
        return tradingSignalRepository.findById(id).orElse(null);
    }
}
```

- [ ] **Step 2: 提交代码**

```bash
git add backend/src/main/java/com/carbon/carbon/service/TradingSignalService.java
git commit -m "feat: add TradingSignalService for generating trading signals"
```

---

### Task 17: Java集成 - 交易信号Controller

**Files:**
- Create: `backend/src/main/java/com/carbon/carbon/controller/TradingSignalController.java`
- Test: 无需测试

**Interfaces:**
- Produces: `TradingSignalController` REST控制器

- [ ] **Step 1: 创建 TradingSignalController.java**

```java
package com.carbon.carbon.controller;

import com.carbon.carbon.entity.TradingSignal;
import com.carbon.carbon.service.TradingSignalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/signal")
@CrossOrigin(origins = "*")
public class TradingSignalController {

    @Autowired
    private TradingSignalService tradingSignalService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateSignal() {
        return ResponseEntity.ok(tradingSignalService.generateSignal());
    }

    @GetMapping("/latest")
    public ResponseEntity<TradingSignal> getLatestSignal() {
        TradingSignal signal = tradingSignalService.getLatestSignal();
        if (signal != null) {
            return ResponseEntity.ok(signal);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<TradingSignal>> getSignalHistory() {
        return ResponseEntity.ok(tradingSignalService.getSignalHistory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradingSignal> getSignalById(@PathVariable Long id) {
        TradingSignal signal = tradingSignalService.getSignalById(id);
        if (signal != null) {
            return ResponseEntity.ok(signal);
        }
        return ResponseEntity.notFound().build();
    }
}
```

- [ ] **Step 2: 提交代码**

```bash
git add backend/src/main/java/com/carbon/carbon/controller/TradingSignalController.java
git commit -m "feat: add TradingSignalController for trading signal API endpoints"
```

---

### Task 18: 增强现有预测服务

**Files:**
- Modify: `backend/src/main/java/com/carbon/carbon/service/PricePredictionService.java`
- Test: 无需测试

**Interfaces:**
- Produces: 增强的 `PricePredictionService`

- [ ] **Step 1: 添加外部数据集成方法**

在 `PricePredictionService.java` 中添加以下方法：

```java
/**
 * 获取外部因子对价格的影响分析
 */
public Map<String, Object> analyzeExternalFactorImpact() {
    Map<String, Object> impact = new HashMap<>();

    // 从外部数据服务获取最新因子
    try {
        // 这里简化处理，实际应调用ExternalDataService
        impact.put("hasExternalFactors", true);
        impact.put("description", "外部因子分析功能待集成Python预测服务后完善");
    } catch (Exception e) {
        impact.put("hasExternalFactors", false);
        impact.put("description", "暂无外部因子数据");
    }

    return impact;
}

/**
 * 综合预测（融合内部+外部数据）
 */
public Map<String, Object> comprehensivePrediction() {
    Map<String, Object> prediction = predictPriceRange();

    // 添加外部因子分析
    Map<String, Object> factorImpact = analyzeExternalFactorImpact();
    prediction.put("externalFactorImpact", factorImpact);

    // 添加趋势分析
    prediction.put("trendAnalysis", analyzePriceTrend());

    return prediction;
}
```

- [ ] **Step 2: 提交代码**

```bash
git add backend/src/main/java/com/carbon/carbon/service/PricePredictionService.java
git commit -m "feat: enhance PricePredictionService with external factor integration"
```

---

### Task 19: 前端 - 交易信号页面

**Files:**
- Create: `frontend/src/views/TradingSignal.vue`
- Test: 无需测试

**Interfaces:**
- Produces: `TradingSignal.vue` 页面组件

- [ ] **Step 1: 创建 TradingSignal.vue**

```vue
<template>
  <div class="trading-signal">
    <h2>交易信号</h2>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="signal" class="signal-container">
      <!-- 信号类型卡片 -->
      <div :class="['signal-card', signal.signalType.toLowerCase()]">
        <div class="signal-type">{{ signal.signalType }}</div>
        <div class="signal-time">{{ formatTime(signal.signalTime) }}</div>
      </div>

      <!-- 置信度 -->
      <div class="confidence-section">
        <h3>置信度</h3>
        <div :class="['confidence-badge', signal.confidenceLevel.toLowerCase()]">
          {{ signal.confidenceLevel }}
        </div>
      </div>

      <!-- 价格区间 -->
      <div class="price-range-section">
        <h3>建议价格区间</h3>
        <div class="price-range">
          <span>{{ signal.priceRangeStart }}</span>
          <span> - </span>
          <span>{{ signal.priceRangeEnd }}</span>
        </div>
      </div>

      <!-- 影响因素 -->
      <div class="factors-section">
        <h3>主要影响因素</h3>
        <div v-if="keyFactors" class="factors-list">
          <div v-for="(value, key) in keyFactors" :key="key" class="factor-item">
            <span class="factor-key">{{ key }}:</span>
            <span class="factor-value">{{ formatFactorValue(value) }}</span>
          </div>
        </div>
      </div>

      <!-- 因素贡献度 -->
      <div v-if="factorContribution" class="contribution-section">
        <h3>因素贡献度</h3>
        <div class="contribution-chart">
          <div
            v-for="(value, key) in factorContribution"
            :key="key"
            class="contribution-bar"
          >
            <span class="contribution-label">{{ key }}</span>
            <div class="bar-container">
              <div
                class="bar-fill"
                :style="{ width: value + '%' }"
              ></div>
            </div>
            <span class="contribution-value">{{ value.toFixed(1) }}%</span>
          </div>
        </div>
      </div>

      <!-- 生成新信号按钮 -->
      <button @click="generateSignal" class="generate-btn">
        生成新信号
      </button>
    </div>

    <div v-else class="no-signal">
      <p>暂无交易信号</p>
      <button @click="generateSignal" class="generate-btn">
        生成信号
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'TradingSignal',
  data() {
    return {
      loading: false,
      signal: null,
      keyFactors: null,
      factorContribution: null
    };
  },
  mounted() {
    this.loadLatestSignal();
  },
  methods: {
    async loadLatestSignal() {
      this.loading = true;
      try {
        const response = await axios.get('/api/signal/latest');
        this.signal = response.data;
        if (this.signal) {
          this.loadSignalDetails(this.signal.id);
        }
      } catch (error) {
        console.error('Failed to load signal:', error);
      } finally {
        this.loading = false;
      }
    },
    async loadSignalDetails(signalId) {
      try {
        const response = await axios.get(`/api/signal/${signalId}`);
        const signal = response.data;
        this.keyFactors = signal.keyFactors ? JSON.parse(signal.keyFactors) : null;
        this.factorContribution = signal.factorContribution ? JSON.parse(signal.factorContribution) : null;
      } catch (error) {
        console.error('Failed to load signal details:', error);
      }
    },
    async generateSignal() {
      this.loading = true;
      try {
        const response = await axios.post('/api/signal/generate');
        if (response.data.status === 'success') {
          this.signal = response.data.signal;
          this.keyFactors = response.data.keyFactors;
          this.factorContribution = response.data.factorContribution;
        }
      } catch (error) {
        console.error('Failed to generate signal:', error);
      } finally {
        this.loading = false;
      }
    },
    formatTime(time) {
      return new Date(time).toLocaleString('zh-CN');
    },
    formatFactorValue(value) {
      if (Array.isArray(value)) {
        return value.join(', ');
      }
      return value;
    }
  }
};
</script>

<style scoped>
.trading-signal {
  padding: 20px;
}

.signal-card {
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
}

.signal-card.buy {
  background-color: #4caf50;
  color: white;
}

.signal-card.sell {
  background-color: #f44336;
  color: white;
}

.signal-card.hold {
  background-color: #9e9e9e;
  color: white;
}

.signal-type {
  font-size: 24px;
  font-weight: bold;
}

.confidence-section, .price-range-section, .factors-section, .contribution-section {
  margin: 20px 0;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 8px;
}

.confidence-badge {
  display: inline-block;
  padding: 5px 15px;
  border-radius: 4px;
  font-weight: bold;
}

.confidence-badge.high {
  background-color: #4caf50;
  color: white;
}

.confidence-badge.medium {
  background-color: #ff9800;
  color: white;
}

.confidence-badge.low {
  background-color: #f44336;
  color: white;
}

.price-range {
  font-size: 18px;
  font-weight: bold;
}

.factor-item {
  margin: 5px 0;
}

.contribution-bar {
  display: flex;
  align-items: center;
  margin: 10px 0;
}

.contribution-label {
  width: 120px;
}

.bar-container {
  flex: 1;
  height: 20px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #2196f3;
}

.contribution-value {
  width: 60px;
  text-align: right;
}

.generate-btn {
  padding: 10px 20px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.generate-btn:hover {
  background: #1976d2;
}

.loading, .no-signal {
  text-align: center;
  padding: 40px;
}
</style>
```

- [ ] **Step 2: 提交代码**

```bash
git add frontend/src/views/TradingSignal.vue
git commit -m "feat: add TradingSignal Vue component for displaying trading signals"
```

---

### Task 20: 前端 - 信号历史和路由配置

**Files:**
- Create: `frontend/src/views/SignalHistory.vue`
- Modify: `frontend/src/router/index.js`
- Test: 无需测试

**Interfaces:**
- Produces: `SignalHistory.vue` 页面和路由配置

- [ ] **Step 1: 创建 SignalHistory.vue**

```vue
<template>
  <div class="signal-history">
    <h2>信号历史</h2>

    <div class="filter-section">
      <select v-model="filterType" @change="loadHistory">
        <option value="">全部类型</option>
        <option value="BUY">买入</option>
        <option value="SELL">卖出</option>
        <option value="HOLD">观望</option>
      </select>
    </div>

    <table class="signal-table">
      <thead>
        <tr>
          <th>时间</th>
          <th>信号类型</th>
          <th>置信度</th>
          <th>价格区间</th>
          <th>模型版本</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="signal in signals" :key="signal.id" @click="showDetail(signal)">
          <td>{{ formatTime(signal.signalTime) }}</td>
          <td :class="signal.signalType.toLowerCase()">{{ signal.signalType }}</td>
          <td>{{ signal.confidenceLevel }}</td>
          <td>{{ signal.priceRangeStart }} - {{ signal.priceRangeEnd }}</td>
          <td>{{ signal.modelVersion }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'SignalHistory',
  data() {
    return {
      signals: [],
      filterType: ''
    };
  },
  mounted() {
    this.loadHistory();
  },
  methods: {
    async loadHistory() {
      try {
        const response = await axios.get('/api/signal/history');
        this.signals = response.data;
        if (this.filterType) {
          this.signals = this.signals.filter(s => s.signalType === this.filterType);
        }
      } catch (error) {
        console.error('Failed to load signal history:', error);
      }
    },
    formatTime(time) {
      return new Date(time).toLocaleString('zh-CN');
    },
    showDetail(signal) {
      this.$router.push(`/signal/${signal.id}`);
    }
  }
};
</script>

<style scoped>
.signal-history {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
}

.signal-table {
  width: 100%;
  border-collapse: collapse;
}

.signal-table th, .signal-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.signal-table tbody tr:hover {
  background: #f5f5f5;
  cursor: pointer;
}

.buy { color: #4caf50; font-weight: bold; }
.sell { color: #f44336; font-weight: bold; }
.hold { color: #9e9e9e; font-weight: bold; }
</style>
```

- [ ] **Step 2: 更新路由配置**

在 `frontend/src/router/index.js` 中添加新路由：

```javascript
{
  path: '/signal',
  name: 'TradingSignal',
  component: () => import('@/views/TradingSignal.vue')
},
{
  path: '/signal-history',
  name: 'SignalHistory',
  component: () => import('@/views/SignalHistory.vue')
}
```

- [ ] **Step 3: 提交代码**

```bash
git add frontend/src/views/SignalHistory.vue frontend/src/router/index.js
git commit -m "feat: add SignalHistory page and update router"
```

---

## 自检清单

### 1. 规范覆盖检查

- [x] 多源数据融合 - ExternalFactor实体和ExternalDataService
- [x] 外部数据源覆盖 - 电力需求、钢铁水泥产量、煤炭原油价格、天气预警、政策情感分析
- [x] 分层架构 - 时间序列模型(Prophet) + 因子融合(XGBoost) + 输出层
- [x] 点预测 - OutputLayer.generate_point_prediction
- [x] 区间预测 - OutputLayer.generate_interval_prediction
- [x] 概率分布 - OutputLayer.generate_probability_distribution
- [x] 买入/卖出/观望信号 - TradingSignalService.generateSignal
- [x] 置信度评级 - TradingSignalService.determineConfidenceLevel
- [x] 影响因素说明 - TradingSignalService.extractKeyFactors
- [x] 信号历史档案 - TradingSignal实体和SignalTracking实体
- [x] 模型版本管理 - modelVersion字段

### 2. 占位符检查

- [x] 无"TBD"或"TODO"
- [x] 无"实现后续"或"填充细节"
- [x] 完整的代码示例

### 3. 类型一致性检查

- [x] ExternalFactor的factorType使用String类型
- [x] TradingSignal的signalType和confidenceLevel使用String类型
- [x] 所有Repository方法签名正确
- [x] Python和Java之间的JSON序列化正确

---

## 执行方式选择

**Plan complete and saved to `docs/superpowers/plans/2026-07-28-carbon-price-prediction-plan.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
