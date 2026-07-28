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
        output['tsFeatures'] = {
            'trend': ts_result.get('trend'),
            'seasonality': ts_result.get('seasonality')
        }

        output['mlFeatures'] = {
            'featureImportance': ml_result.get('feature_importance'),
            'factorContribution': self.fusion_model.get_factor_contribution(X)
        }

        logger.info(f"Prediction completed: {output['pointPrediction']}")
        return output

    def get_model_info(self) -> Dict:
        """获取模型信息"""
        return {
            'model_version': self.config.MODEL_VERSION,
            'is_trained': self.is_trained,
            'ts_model': 'Prophet',
            'fusion_model': self.fusion_model.model_type
        }
