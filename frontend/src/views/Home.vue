<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409eff">
            <el-icon :size="30"><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalCompanies }}</div>
            <div class="stat-label">重点排放单位</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67c23a">
            <el-icon :size="30"><DataLine /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalIndustries }}</div>
            <div class="stat-label">覆盖行业</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c">
            <el-icon :size="30"><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pendingAlerts }}</div>
            <div class="stat-label">待处理预警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #f56c6c">
            <el-icon :size="30"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ formatNumber(stats.todayVolume) }}</div>
            <div class="stat-label">今日成交额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>行业分布</span>
          </template>
          <div ref="industryChart" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>价格走势</span>
          </template>
          <div ref="priceChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>履约进度</span>
          </template>
          <div ref="complianceChart" style="height: 200px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import * as echarts from 'echarts'
import { dashboardApi } from '../api'

const stats = reactive({
  totalCompanies: 0,
  totalIndustries: 0,
  pendingAlerts: 0,
  todayVolume: 0,
  todayAvgPrice: 0
})

const industryChart = ref(null)
const priceChart = ref(null)
const complianceChart = ref(null)

const formatNumber = (num) => {
  if (!num) return '0'
  return new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 0 }).format(num)
}

const loadData = async () => {
  try {
    const overview = await dashboardApi.getOverview()
    Object.assign(stats, overview)

    const industryData = await dashboardApi.getIndustryDistribution()
    initIndustryChart(industryData)

    const priceData = await dashboardApi.getPriceTrend(30)
    initPriceChart(priceData)

    const complianceData = await dashboardApi.getComplianceProgress()
    initComplianceChart(complianceData)
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  }
}

const initIndustryChart = (data) => {
  const chart = echarts.init(industryChart.value)
  const option = {
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '50%',
      data: data.map(item => ({
        name: item.industryName,
        value: item.companyCount
      }))
    }]
  }
  chart.setOption(option)
}

const initPriceChart = (data) => {
  const chart = echarts.init(priceChart.value)
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date)
    },
    yAxis: {
      type: 'value',
      name: '价格(元)'
    },
    series: [{
      type: 'line',
      data: data.map(d => d.avgPrice),
      smooth: true,
      areaStyle: { opacity: 0.3 }
    }]
  }
  chart.setOption(option)
}

const initComplianceChart = (data) => {
  const chart = echarts.init(complianceChart.value)
  const option = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { name: '已履约', value: data.compliant },
        { name: '未履约', value: data.nonCompliant },
        { name: '待审核', value: data.pending }
      ]
    }]
  }
  chart.setOption(option)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}
</style>
