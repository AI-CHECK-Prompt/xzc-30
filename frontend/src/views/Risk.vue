<template>
  <div class="risk">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>风险预警</span>
          </template>
          <el-table :data="alerts" stripe>
            <el-table-column prop="alertType" label="预警类型">
              <template #default="{ row }">
                <el-tag :type="getType(row.alertType)">{{ getTypeText(row.alertType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="alertName" label="预警名称" />
            <el-table-column prop="company.name" label="涉及企业" />
            <el-table-column prop="alertLevel" label="预警级别">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.alertLevel)">{{ row.alertLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="alertContent" label="预警内容" />
            <el-table-column prop="thresholdValue" label="阈值" />
            <el-table-column prop="currentValue" label="当前值" />
            <el-table-column prop="alertTime" label="预警时间" />
            <el-table-column prop="alertStatus" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.alertStatus === 'PENDING' ? 'warning' : 'success'">
                  {{ row.alertStatus === 'PENDING' ? '待处理' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button v-if="row.alertStatus === 'PENDING'" size="small" type="primary" @click="handleProcess(row)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" title="处理预警" width="400px">
      <el-form label-width="80px">
        <el-form-item label="处理结果">
          <el-input v-model="processForm.result" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { riskApi } from '../api'
import { ElMessage } from 'element-plus'

const alerts = ref([])
const dialogVisible = ref(false)
const processForm = ref({ id: null, result: '' })

const loadData = async () => {
  try {
    alerts.value = await riskApi.getAllAlerts()
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handleProcess = (row) => {
  processForm.value = { id: row.id, result: '' }
  dialogVisible.value = true
}

const submitProcess = async () => {
  try {
    await riskApi.processAlert(processForm.value.id, processForm.value.result)
    ElMessage.success('处理成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('处理失败')
  }
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

const getType = (type) => {
  const map = {
    'LARGE_TRANSACTION': 'danger',
    'RELATED_PARTY': 'warning',
    'POSITION_CONCENTRATION': 'warning',
    'ABNORMAL_TRADING': 'danger',
    'COMPLIANCE_GAP': 'danger'
  }
  return map[type] || 'info'
}

const getLevelType = (level) => {
  const map = { 'HIGH': 'danger', 'MEDIUM': 'warning', 'LOW': 'info' }
  return map[level] || 'info'
}

onMounted(() => {
  loadData()
})
</script>
