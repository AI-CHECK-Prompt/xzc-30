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
