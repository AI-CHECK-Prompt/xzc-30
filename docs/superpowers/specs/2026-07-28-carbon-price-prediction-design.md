# 碳市场价格预测系统 - 多源数据融合设计方案

## 1. 项目概述

### 1.1 背景
现有预测模块仅依赖历史成交流价数据，无法捕捉宏观经济指标、行业开工率、天气因素、能源价格等多维外部因素对碳价的影响。

### 1.2 目标
构建多源数据融合的价格预测模型，整合碳市场内部交易数据与外部影响因素数据，生成结构化交易信号并建立完整的信号历史档案。

---

## 2. 系统架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           前端展示层 (Vue.js)                            │
│  预测看板 | 交易信号 | 信号历史 | 模型评估                                │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │ HTTP API
┌─────────────────────────────────┴───────────────────────────────────────┐
│                           API层 (Java Spring Boot)                       │
│  PricePredictionController | SignalController | ExternalDataController  │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
┌─────────────────────────────────┴───────────────────────────────────────┐
│                         业务服务层 (Java Service)                         │
│  PricePredictionService | TradingSignalService | ExternalDataService    │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │ HTTP/REST
┌─────────────────────────────────┴───────────────────────────────────────┐
│                       预测模型层 (Python 微服务)                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────────────┐ │
│  │ 数据预处理  │→ │ 时间序列模型 │→ │ ML因子融合模型                  │ │
│  │ (pandas)   │  │ (ARIMA/Prophet)│ │ (RandomForest/XGBoost)         │ │
│  └─────────────┘  └─────────────┘  └─────────────────────────────────┘ │
│                            ↓                                             │
│                   ┌─────────────────────────────────┐                   │
│                   │ 输出层 (点预测/区间/概率分布)     │                   │
│                   └─────────────────────────────────┘                   │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
┌─────────────────────────────────┴───────────────────────────────────────┐
│                          数据层 (MySQL 扩展)                             │
│  交易数据 | 外部因子数据 | 预测结果 | 信号档案                            │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 分层模型设计

```
┌──────────────────────────────────────────────────────────────┐
│  上层输出层                                                      │
│  - 点预测：未来N天价格预测值                                    │
│  - 区间预测：80%/95%置信区间                                   │
│  - 概率分布：价格区间概率                                       │
└──────────────────────────────────────────────────────────────┘
                              ↑
┌──────────────────────────────────────────────────────────────┐
│  中层因子融合层 (机器学习模型)                                  │
│  输入：时间序列特征 + 外部因子特征                               │
│  模型：RandomForest / XGBoost / LightGBM                      │
│  输出：外部因子调整后的价格预测                                  │
└──────────────────────────────────────────────────────────────┘
                              ↑
┌──────────────────────────────────────────────────────────────┐
│  底层时间序列层                                                  │
│  模型：ARIMA / SARIMA / Prophet                               │
│  捕捉价格自相关性、季节性、趋势                                  │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. 数据模型设计

### 3.1 现有表（保持不变）
- `t_carbon_order` - 碳交易订单数据
- `t_company` - 企业信息

### 3.2 新增表

#### 3.2.1 外部因子数据表 (t_external_factor)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| factor_date | DATE | 数据日期 |
| factor_type | VARCHAR(50) | 因子类型 |
| factor_value | DECIMAL(18,4) | 因子值 |
| data_source | VARCHAR(100) | 数据来源 |
| create_time | DATETIME | 创建时间 |

**因子类型枚举**：
- `POWER_DEMAND` - 电力需求系数
- `STEEL_OUTPUT` - 钢铁产量指数
- `CEMENT_OUTPUT` - 水泥产量指数
- `COAL_PRICE` - 煤炭价格指数
- `CRUDE_OIL_PRICE` - 原油价格指数
- `WEATHER_LEVEL` - 天气预警等级 (1-5级)
- `POLICY_SENTIMENT` - 政策公告情感分析 (-1到1)

#### 3.2.2 预测结果表 (t_prediction_result)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| prediction_time | DATETIME | 预测时间 |
| prediction_horizon | INT | 预测期限(天) |
| base_price | DECIMAL(18,2) | 基准价格 |
| predicted_price | DECIMAL(18,2) | 点预测价格 |
| lower_bound_80 | DECIMAL(18,2) | 80%置信区间下限 |
| upper_bound_80 | DECIMAL(18,2) | 80%置信区间上限 |
| lower_bound_95 | DECIMAL(18,2) | 95%置信区间下限 |
| upper_bound_95 | DECIMAL(18,2) | 95%置信区间上限 |
| probability_distribution | JSON | 概率分布 |
| model_version | VARCHAR(50) | 模型版本 |
| input_snapshot | JSON | 输入数据快照 |
| create_time | DATETIME | 创建时间 |

#### 3.2.3 交易信号表 (t_trading_signal)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| signal_time | DATETIME | 信号生成时间 |
| signal_type | VARCHAR(20) | 信号类型 (BUY/SELL/HOLD) |
| confidence_level | VARCHAR(10) | 置信度 (HIGH/MEDIUM/LOW) |
| price_range_start | DECIMAL(18,2) | 建议价格区间起点 |
| price_range_end | DECIMAL(18,2) | 建议价格区间终点 |
| threshold_description | VARCHAR(200) | 阈值说明 |
| key_factors | JSON | 主要影响因素 |
| factor_contribution | JSON | 各因素贡献度 |
| prediction_id | BIGINT | 关联的预测结果ID |
| model_version | VARCHAR(50) | 模型版本 |
| create_time | DATETIME | 创建时间 |

#### 3.2.4 信号跟踪表 (t_signal_tracking)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| signal_id | BIGINT | 关联的交易信号ID |
| actual_price | DECIMAL(18,2) | 实际价格 |
| price_check_time | DATETIME | 价格检查时间 |
| signal_result | VARCHAR(20) | 信号结果 (PROFIT/LOSS/HOLD) |
| return_rate | DECIMAL(10,4) | 收益率 |
| evaluation_notes | VARCHAR(500) | 评估备注 |
| create_time | DATETIME | 创建时间 |

---

## 4. 功能模块设计

### 4.1 外部数据管理模块

**功能**：
- 外部因子数据的增删改查
- 支持手动导入和API接入
- 数据质量校验

**核心接口**：
```
POST   /api/external-factors          - 添加外部因子数据
GET    /api/external-factors          - 查询外部因子数据
GET    /api/external-factors/latest   - 获取最新因子数据
POST   /api/external-factors/batch    - 批量导入
```

### 4.2 预测模型模块

**功能**：
- 时间序列建模（ARIMA/SARIMA/Prophet）
- 外部因子特征工程
- ML模型融合
- 多形式输出（点预测/区间/概率分布）

**核心接口**：
```
POST   /api/prediction/run            - 执行预测
GET    /api/prediction/result/{id}    - 获取预测结果
GET    /api/prediction/history        - 历史预测记录
```

**模型版本管理**：
- 版本号格式：v1.0.0, v1.1.0
- 每次预测记录模型版本
- 支持模型回滚和版本对比

### 4.3 交易信号模块

**功能**：
- 基于预测结果生成交易信号
- 置信度评级
- 影响因素说明
- 信号历史存档

**信号规则**：
```
BUY  (买入):  预测价格 > 当前价格 + 阈值 且 置信度 >= MEDIUM
SELL (卖出):  预测价格 < 当前价格 - 阈值 且 置信度 >= MEDIUM
HOLD (观望):  其他情况
```

**置信度评级**：
- HIGH:   区间宽度 < 均值 * 10% 且 外部因子信号一致
- MEDIUM: 区间宽度 < 均值 * 20%
- LOW:    其他情况

**核心接口**：
```
POST   /api/signal/generate           - 生成交易信号
GET    /api/signal/latest            - 最新交易信号
GET    /api/signal/history           - 信号历史
GET    /api/signal/{id}              - 信号详情
POST   /api/signal/{id}/track         - 更新信号跟踪结果
GET    /api/signal/evaluation        - 信号评估统计
```

### 4.4 模型评估模块

**功能**：
- 信号准确率统计
- 收益率分析
- 模型效果对比

**核心指标**：
- 准确率 (Direction Accuracy)
- 平均绝对误差 (MAE)
- 均方根误差 (RMSE)
- 信号胜率
- 平均收益率

---

## 5. Python预测服务设计

### 5.1 目录结构

```
python-service/
├── prediction_service/
│   ├── __init__.py
│   ├── main.py                 # FastAPI入口
│   ├── config.py               # 配置
│   ├── models/
│   │   ├── __init__.py
│   │   ├── time_series.py      # 时间序列模型
│   │   ├── factor_fusion.py    # 因子融合模型
│   │   └── output_layer.py     # 输出层
│   ├── preprocessing/
│   │   ├── __init__.py
│   │   ├── data_loader.py      # 数据加载
│   │   └── feature_engineering.py  # 特征工程
│   ├── services/
│   │   ├── __init__.py
│   │   └── prediction.py       # 预测服务
│   └── utils/
│       ├── __init__.py
│       └── logger.py           # 日志工具
├── requirements.txt
└── Dockerfile
```

### 5.2 核心模型类

**TimeSeriesModel** (时间序列层)：
```python
class TimeSeriesModel:
    def fit(self, price_data: pd.Series) -> None
    def predict(self, horizon: int) -> Dict:
        # 返回: {'base_price': float, 'residuals': []}
```

**FactorFusionModel** (因子融合层)：
```python
class FactorFusionModel:
    def __init__(self, model_type: str = 'xgboost')
    def fit(self, X: pd.DataFrame, y: pd.Series) -> None
    def predict(self, X: pd.DataFrame) -> Dict:
        # 返回: {'adjusted_price': float, 'feature_importance': {}}
```

**OutputLayer** (输出层)：
```python
class OutputLayer:
    def generate_point_prediction(self, ts_result: Dict, ml_result: Dict) -> float
    def generate_interval_prediction(self, ts_result: Dict, ml_result: Dict, confidence: float) -> Tuple[float, float]
    def generate_probability_distribution(self, ts_result: Dict, ml_result: Dict, bins: int) -> List[Dict]
```

### 5.3 API接口 (FastAPI)

```python
@app.post("/predict")
async def predict(request: PredictionRequest) -> PredictionResponse:
    """
    执行价格预测
    """
    # 1. 加载数据
    # 2. 时间序列预测
    # 3. 因子融合调整
    # 4. 输出多形式结果
```

---

## 6. 前端页面设计

### 6.1 预测看板 (Prediction.vue 扩展)

**内容**：
- 当前价格 + 预测价格对比图
- 区间预测可视化
- 置信度指示器
- 关键外部因子展示

### 6.2 交易信号页面

**内容**：
- 最新信号展示（买入/卖出/观望）
- 信号置信度和有效期
- 影响因素分解图
- 操作建议说明

### 6.3 信号历史页面

**内容**：
- 历史信号列表
- 信号筛选（时间、类型、模型版本）
- 信号详情（包含当时的输入快照）

### 6.4 模型评估页面

**内容**：
- 准确率趋势图
- 信号胜率统计
- 模型版本对比
- 收益分析

---

## 7. 实施计划

### Phase 1: 数据基础设施
1. 创建外部因子数据表
2. 创建预测结果表
3. 创建交易信号表
4. 创建信号跟踪表

### Phase 2: Python预测服务
1. 搭建Python微服务框架
2. 实现时间序列模型
3. 实现因子融合模型
4. 实现输出层
5. 暴露REST API

### Phase 3: Java集成
1. 外部数据管理功能
2. 预测服务调用集成
3. 交易信号生成逻辑
4. 信号历史存档

### Phase 4: 前端展示
1. 预测看板增强
2. 交易信号页面
3. 信号历史页面
4. 模型评估页面

---

## 8. 关键技术选型

| 组件 | 选型 | 理由 |
|------|------|------|
| 预测服务框架 | FastAPI | 高性能、自动化文档、Python原生 |
| 时间序列模型 | Prophet | 易于使用、处理季节性、自动调参 |
| 因子融合模型 | XGBoost | 性能优秀、特征重要性分析 |
| 机器学习框架 | scikit-learn | 完整生态、易于集成 |
| 数据处理 | pandas | Python数据分析标准库 |
| 序列化 | pydantic | 数据验证、类型提示 |

---

## 9. 后续扩展

- 支持更多外部数据源（碳市场国际行情、排放权期货等）
- 模型自动化训练和更新
- 实时预警推送
- 移动端适配
