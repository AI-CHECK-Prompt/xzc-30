<template>
  <div class="companies">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>企业管理</span>
          <el-button type="primary" @click="handleAdd">新增企业</el-button>
        </div>
      </template>

      <el-table :data="companies" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="企业名称" />
        <el-table-column prop="unifiedSocialCreditCode" label="统一社会信用代码" width="180" />
        <el-table-column prop="industry.name" label="所属行业" />
        <el-table-column prop="region" label="所在地区" />
        <el-table-column prop="totalEmissions" label="排放总量" />
        <el-table-column prop="quotaHeld" label="持有配额" />
        <el-table-column prop="complianceStatus" label="履约状态">
          <template #default="{ row }">
            <el-tag :type="getComplianceType(row.complianceStatus)">
              {{ getComplianceText(row.complianceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="企业名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="统一社会信用代码">
          <el-input v-model="form.unifiedSocialCreditCode" />
        </el-form-item>
        <el-form-item label="所属行业">
          <el-select v-model="form.industryId" placeholder="请选择">
            <el-option
              v-for="industry in industries"
              :key="industry.id"
              :label="industry.name"
              :value="industry.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所在地区">
          <el-input v-model="form.region" />
        </el-form-item>
        <el-form-item label="排放总量">
          <el-input-number v-model="form.totalEmissions" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="持有配额">
          <el-input-number v-model="form.quotaHeld" :min="0" :precision="2" />
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
import { companyApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const companies = ref([])
const industries = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增企业')
const form = ref({
  id: null,
  name: '',
  unifiedSocialCreditCode: '',
  industryId: null,
  region: '',
  totalEmissions: 0,
  quotaHeld: 0
})

const loadData = async () => {
  try {
    companies.value = await companyApi.getAll()
    industries.value = await companyApi.getIndustries()
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增企业'
  form.value = {
    id: null,
    name: '',
    unifiedSocialCreditCode: '',
    industryId: null,
    region: '',
    totalEmissions: 0,
    quotaHeld: 0
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑企业'
  form.value = { ...row, industryId: row.industry?.id }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    const data = { ...form.value }
    if (form.value.id) {
      await companyApi.update(form.value.id, data)
      ElMessage.success('更新成功')
    } else {
      await companyApi.create(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该企业吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await companyApi.delete(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getComplianceType = (status) => {
  const map = { 'COMPLIANT': 'success', 'NON_COMPLIANT': 'danger', 'PENDING': 'warning' }
  return map[status] || 'info'
}

const getComplianceText = (status) => {
  const map = { 'COMPLIANT': '已履约', 'NON_COMPLIANT': '未履约', 'PENDING': '待审核' }
  return map[status] || '未知'
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
