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
