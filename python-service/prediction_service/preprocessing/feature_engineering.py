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
