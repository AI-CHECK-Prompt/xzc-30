<template>
  <div class="prediction">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>价格区间预测</span>
          </template>
          <div class="prediction-content">
            <div class="prediction-item">
              <span class="label">基准价格：</span>
              <span class="value">¥{{ prediction.basePrice }}</span>
            </div>
            <div class="prediction-item">
              <span class="label">预测区间：</span>
              <span class="value">¥{{ prediction.lowerBound }} - ¥{{ prediction.upperBound }}</span>
            </div>
            <div class="prediction-item">
              <span class="label">波动率：</span>
              <span class="value">{{ (prediction.volatility * 100).toFixed(2) }}%</span>
            </div>
            <div v-if="prediction.volatilityAlert" class="alert-box">
              <el-alert type="warning" :closable="false">{{ prediction.volatilityAlert }}</el-alert>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>价格趋势分析</span>
          </template>
          <div class="prediction-content">
            <div class="prediction-item">
              <span class="label">趋势方向：</span>
              <el-tag :type="getTrendType(trend.trend)">{{ getTrendText(trend.trend) }}</el-tag>
            </div>
            <div class="prediction-item">
              <span class="label">趋势描述：</span>
              <span class="value">{{ trend.trendDescription }}</span>
            </div>
            <div class="prediction-item">
              <span class="label">变化率：</span>
              <span class="value">{{ trend.changeRate }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>流动性分析</span>
          </template>
          <div class="prediction-content">
            <div class="prediction-item">
              <span class="label">流动性等级：</span>
              <el-tag :type="getLiquidityType(liquidity.liquidityLevel)">{{ liquidity.liquidityDescription }}</el-tag>
            </div>
            <div class="prediction-item">
              <span class="label">30天总成交额：</span>
              <span class="value">¥{{ formatNumber(liquidity.totalVolume) }}</span>
            </div>
            <div class="prediction-item">
              <span class="label">日均成交额：</span>
              <span class="value">¥{{ formatNumber(liquidity.avgDailyVolume) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>价格异常检测</span>
          </template>
          <div class="prediction-content">
            <div class="prediction-item">
              <span class="label">是否异常：</span>
              <el-tag :type="anomaly.isAnomaly ? 'danger' : 'success'">
                {{ anomaly.isAnomaly ? '异常' : '正常' }}
              </el-tag>
            </div>
            <div class="prediction-item">
              <span class="label">当前价格：</span>
              <span class="value">¥{{ anomaly.currentPrice }}</span>
            </div>
            <div class="prediction-item">
              <span class="label">历史均价：</span>
              <span class="value">¥{{ anomaly.historicalAvgPrice }}</span>
            </div>
            <div class="prediction-item">
              <span class="label">偏离度：</span>
              <span class="value">{{ anomaly.deviation }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>历史价格走势</span>
          </template>
          <div ref="priceChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { priceApi } from '../api'

const prediction = ref({})
const trend = ref({})
const liquidity = ref({})
const anomaly = ref({})
const priceChart = ref(null)

const formatNumber = (num) => {
  if (!num) return '0'
  return new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 0 }).format(num)
}

const getTrendType = (trend) => {
  const map = { 'UP': 'danger', 'DOWN': 'success', 'STABLE': 'info' }
  return map[trend] || 'info'
}

const getTrendText = (trend) => {
  const map = { 'UP': '上涨', 'DOWN': '下跌', 'STABLE': '稳定' }
  return map[trend] || '未知'
}

const getLiquidityType = (level) => {
  const map = { 'HIGH': 'success', 'MEDIUM': 'warning', 'LOW': 'danger' }
  return map[level] || 'info'
}

const loadData = async () => {
  try {
    const [pred, tr, liq, anom, history] = await Promise.all([
      priceApi.getPrediction(),
      priceApi.getTrend(),
      priceApi.getLiquidity(),
      priceApi.getAnomaly(),
      priceApi.getHistory(30)
    ])
    prediction.value = pred
    trend.value = tr
    liquidity.value = liq
    anomaly.value = anom
    initChart(history)
  } catch (error) {
    console.error('Failed to load data:', error)
  }
}

const initChart = (data) => {
  const chart = echarts.init(priceChart.value)
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.date) },
    yAxis: { type: 'value', name: '价格(元)' },
    series: [
      {
        type: 'line',
        data: data.map(d => d.avgPrice),
        smooth: true,
        areaStyle: { opacity: 0.3 }
      }
    ]
  }
  chart.setOption(option)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.prediction-content {
  padding: 20px;
}

.prediction-item {
  margin-bottom: 15px;
}

.prediction-item .label {
  color: #666;
  margin-right: 10px;
}

.prediction-item .value {
  font-weight: bold;
  font-size: 16px;
}

.alert-box {
  margin-top: 20px;
}
</style>
