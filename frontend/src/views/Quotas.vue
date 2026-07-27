<template>
  <div class="quotas">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配额管理</span>
          <el-button type="primary" @click="handleAdd">新增配额</el-button>
        </div>
      </template>

      <el-table :data="quotas" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="company.name" label="企业名称" />
        <el-table-column prop="year" label="年度" width="80" />
        <el-table-column prop="initialAllocation" label="初始分配" />
        <el-table-column prop="additionalAllocation" label="追加分配" />
        <el-table-column prop="tradedIn" label="买入" />
        <el-table-column prop="tradedOut" label="卖出" />
        <el-table-column prop="offsetUsed" label="抵消信用" />
        <el-table-column prop="currentBalance" label="当前余额">
          <template #default="{ row }">
            <span style="font-weight: bold; color: #409eff">{{ row.currentBalance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="complianceStatus" label="履约状态">
          <template #default="{ row }">
            <el-tag :type="row.complianceStatus === 'COMPLIANT' ? 'success' : 'danger'">
              {{ row.complianceStatus === 'COMPLIANT' ? '已履约' : '未履约' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增配额" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="企业">
          <el-select v-model="form.companyId" placeholder="请选择">
            <el-option
              v-for="company in companies"
              :key="company.id"
              :label="company.name"
              :value="company.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="年度">
          <el-input-number v-model="form.year" :min="2020" :max="2030" />
        </el-form-item>
        <el-form-item label="初始分配">
          <el-input-number v-model="form.initialAllocation" :min="0" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { quotaApi, companyApi } from '../api'
import { ElMessage } from 'element-plus'

const quotas = ref([])
const companies = ref([])
const dialogVisible = ref(false)
const form = ref({
  companyId: null,
  year: new Date().getFullYear(),
  initialAllocation: 0
})

const loadData = async () => {
  try {
    quotas.value = await quotaApi.getAll()
    companies.value = await companyApi.getAll()
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handleAdd = () => {
  form.value = {
    companyId: null,
    year: new Date().getFullYear(),
    initialAllocation: 0
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await quotaApi.create(form.value)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
