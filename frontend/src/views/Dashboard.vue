<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-box">
          <div class="stat-title">重点排放单位</div>
          <div class="stat-value">{{ overview.totalCompanies }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-box">
          <div class="stat-title">覆盖行业</div>
          <div class="stat-value">{{ overview.totalIndustries }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-box">
          <div class="stat-title">待处理预警</div>
          <div class="stat-value" style="color: #e6a23c">{{ overview.pendingAlerts }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-box">
          <div class="stat-title">今日成交额</div>
          <div class="stat-value" style="color: #409eff">¥{{ formatNumber(overview.todayVolume) }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>交易热力图</span>
          </template>
          <div ref="heatMapChart" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>行业分布</span>
          </template>
          <div ref="industryChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>履约进度</span>
          </template>
          <div ref="complianceChart" style="height: 280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>价格走势</span>
          </template>
          <div ref="priceChart" style="height: 280px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>风险预警概览</span>
          </template>
          <el-table :data="riskSummary" stripe>
            <el-table-column prop="alertType" label="预警类型">
              <template #default="{ row }">
                {{ getTypeText(row.alertType) }}
              </template>
            </el-table-column>
            <el-table-column prop="total" label="总数" />
            <el-table-column prop="pending" label="待处理">
              <template #default="{ row }">
                <el-tag type="warning">{{ row.pending }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="processed" label="已处理">
              <template #default="{ row }">
                <el-tag type="success">{{ row.processed }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>排放量TOP10企业</span>
          </template>
          <el-table :data="topCompanies" stripe>
            <el-table-column prop="companyName" label="企业名称" />
            <el-table-column prop="industry" label="行业" />
            <el-table-column prop="totalEmissions" label="排放总量" />
            <el-table-column prop="quotaHeld" label="持有配额" />
            <el-table-column prop="complianceStatus" label="履约状态">
              <template #default="{ row }">
                <el-tag :type="row.complianceStatus === 'COMPLIANT' ? 'success' : 'danger'">
                  {{ row.complianceStatus === 'COMPLIANT' ? '已履约' : '未履约' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { dashboardApi } from '../api'

const overview = ref({})
const riskSummary = ref([])
const topCompanies = ref([])

const heatMapChart = ref(null)
const industryChart = ref(null)
const complianceChart = ref(null)
const priceChart = ref(null)

const formatNumber = (num) => {
  if (!num) return '0'
  return new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 0 }).format(num)
}

const getTypeText = (type) => {
  const map = {
    'LARGE_TRANSACTION': '大额交易',
    'RELATED_PARTY': '关联交易',
    'POSITION_CONCENTRATION': '持仓集中度',
    'ABNORMAL_TRADING': '异常交易',
    'COMPLIANCE_GAP': '履约缺口'
  }
  return map[type] || type
}

const loadData = async () => {
  try {
    const [ov, heatData, indData, compData, priceData, risk, top] = await Promise.all([
      dashboardApi.getOverview(),
      dashboardApi.getTradingHeatMap(),
      dashboardApi.getIndustryDistribution(),
      dashboardApi.getComplianceProgress(),
      dashboardApi.getPriceTrend(30),
      dashboardApi.getRiskAlertsSummary(),
      dashboardApi.getTopCompanies()
    ])

    overview.value = ov
    riskSummary.value = risk
    topCompanies.value = top

    initHeatMapChart(heatData)
    initIndustryChart(indData)
    initComplianceChart(compData)
    initPriceChart(priceData)
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  }
}

const initHeatMapChart = (data) => {
  const chart = echarts.init(heatMapChart.value)
  const hours = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23']
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

  const heatData = []
  const byHour = data.byHour || {}
  const byDay = data.byDay || {}

  for (let d = 0; d < 7; d++) {
    for (let h = 0; h < 24; h++) {
      const value = (byHour[h] || 0) + (byDay[days[d]] || 0)
      heatData.push([h, d, value])
    }
  }

  const option = {
    tooltip: { position: 'top' },
    grid: { height: '70%', top: '10%' },
    xAxis: { type: 'category', data: hours, splitArea: { show: true } },
    yAxis: { type: 'category', data: days, splitArea: { show: true } },
    visualMap: { min: 0, max: 10, calculable: true, orient: 'horizontal', left: 'center', bottom: '0%' },
    series: [{
      type: 'heatmap',
      data: heatData,
      label: { show: true }
    }]
  }
  chart.setOption(option)
}

const initIndustryChart = (data) => {
  const chart = echarts.init(industryChart.value)
  const option = {
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: data.map(item => ({
        name: item.industryName,
        value: item.companyCount
      }))
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
        { name: '已履约', value: data.compliant, itemStyle: { color: '#67c23a' } },
        { name: '未履约', value: data.nonCompliant, itemStyle: { color: '#f56c6c' } },
        { name: '待审核', value: data.pending, itemStyle: { color: '#e6a23c' } }
      ]
    }]
  }
  chart.setOption(option)
}

const initPriceChart = (data) => {
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
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#409eff' }
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
.stat-box {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}
</style>
