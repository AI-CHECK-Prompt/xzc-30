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
            price_data: DataFrame with 'date' (ds) and 'avgPrice' (y) columns
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
