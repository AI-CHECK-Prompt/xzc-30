<template>
  <div class="emissions">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>排放数据管理</span>
          <el-button type="primary" @click="handleAdd">新增排放数据</el-button>
        </div>
      </template>

      <el-table :data="emissions" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="company.name" label="企业名称" />
        <el-table-column prop="year" label="年度" width="80" />
        <el-table-column prop="directEmissions" label="直接排放" />
        <el-table-column prop="indirectEmissions" label="间接排放" />
        <el-table-column prop="totalEmissions" label="总排放量" />
        <el-table-column prop="dataStatus" label="数据状态">
          <template #default="{ row }">
            <el-tag>{{ getDataStatusText(row.dataStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="verificationStatus" label="核查状态">
          <template #default="{ row }">
            <el-tag :type="getVerifyType(row.verificationStatus)">
              {{ getVerifyText(row.verificationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleVerify(row)">核查</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增排放数据" width="600px">
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
        <el-form-item label="直接排放">
          <el-input-number v-model="form.directEmissions" :min="0" :precision="6" />
        </el-form-item>
        <el-form-item label="间接排放">
          <el-input-number v-model="form.indirectEmissions" :min="0" :precision="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="verifyDialogVisible" title="排放数据核查" width="400px">
      <el-form label-width="80px">
        <el-form-item label="核查结果">
          <el-radio-group v-model="verifyForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="verifyForm.remarks" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="verifyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitVerify">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { emissionApi, companyApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const emissions = ref([])
const companies = ref([])
const dialogVisible = ref(false)
const verifyDialogVisible = ref(false)
const form = ref({
  companyId: null,
  year: new Date().getFullYear(),
  directEmissions: 0,
  indirectEmissions: 0
})
const verifyForm = ref({
  id: null,
  status: 'APPROVED',
  remarks: ''
})

const loadData = async () => {
  try {
    emissions.value = await emissionApi.getAll()
    companies.value = await companyApi.getAll()
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handleAdd = () => {
  form.value = {
    companyId: null,
    year: new Date().getFullYear(),
    directEmissions: 0,
    indirectEmissions: 0
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await emissionApi.create(form.value)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const handleVerify = (row) => {
  verifyForm.value = { id: row.id, status: 'APPROVED', remarks: '' }
  verifyDialogVisible.value = true
}

const submitVerify = async () => {
  try {
    await emissionApi.verify(verifyForm.value.id, {
      verifierId: 1,
      status: verifyForm.value.status,
      remarks: verifyForm.value.remarks
    })
    ElMessage.success('核查完成')
    verifyDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('核查失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除吗?', '提示', { type: 'warning' })
    await emissionApi.delete(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const getDataStatusText = (status) => {
  const map = { 'SUBMITTED': '已提交', 'VERIFIED': '已核查', 'MODIFIED': '已修改' }
  return map[status] || status
}

const getVerifyText = (status) => {
  const map = { 'PENDING': '待核查', 'APPROVED': '已通过', 'REJECTED': '已驳回' }
  return map[status] || status
}

const getVerifyType = (status) => {
  const map = { 'PENDING': 'warning', 'APPROVED': 'success', 'REJECTED': 'danger' }
  return map[status] || 'info'
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
