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
            max_price = max(prices) * 1.2
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
            'pointPrediction': point_prediction,
            'interval80': {
                'lower': interval_80[0],
                'upper': interval_80[1]
            },
            'interval95': {
                'lower': interval_95[0],
                'upper': interval_95[1]
            },
            'probabilityDistribution': probability_dist,
            'modelVersion': 'v1.0.0'
        }
