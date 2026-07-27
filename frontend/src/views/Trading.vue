<template>
  <div class="trading">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>交易订单</span>
          </template>
          <el-table :data="orders" stripe>
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="tradingMode" label="交易模式">
              <template #default="{ row }">
                <el-tag>{{ getModeText(row.tradingMode) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="seller.name" label="卖方" />
            <el-table-column prop="buyer.name" label="买方" />
            <el-table-column prop="quantity" label="数量" />
            <el-table-column prop="unitPrice" label="单价(元)" />
            <el-table-column prop="totalAmount" label="总价(元)" />
            <el-table-column prop="orderStatus" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.orderStatus)">
                  {{ getStatusText(row.orderStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button v-if="row.orderStatus === 'PENDING'" size="small" type="primary" @click="handleMatch(row.id)">匹配</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>发起交易</span>
          </template>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="挂牌交易" name="listing">
              <el-form label-width="80px">
                <el-form-item label="类型">
                  <el-radio-group v-model="listingForm.type">
                    <el-radio label="sell">卖出</el-radio>
                    <el-radio label="buy">买入</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="企业">
                  <el-select v-model="listingForm.companyId" placeholder="请选择">
                    <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="数量">
                  <el-input-number v-model="listingForm.quantity" :min="1" />
                </el-form-item>
                <el-form-item label="单价">
                  <el-input-number v-model="listingForm.unitPrice" :min="1" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitListing">提交</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="协议转让" name="transfer">
              <el-form label-width="80px">
                <el-form-item label="卖方">
                  <el-select v-model="transferForm.sellerId" placeholder="请选择">
                    <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="买方">
                  <el-select v-model="transferForm.buyerId" placeholder="请选择">
                    <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="数量">
                  <el-input-number v-model="transferForm.quantity" :min="1" />
                </el-form-item>
                <el-form-item label="单价">
                  <el-input-number v-model="transferForm.unitPrice" :min="1" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitTransfer">提交</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="竞价拍卖" name="auction">
              <el-form label-width="80px">
                <el-form-item label="卖方">
                  <el-select v-model="auctionForm.sellerId" placeholder="请选择">
                    <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="数量">
                  <el-input-number v-model="auctionForm.quantity" :min="1" />
                </el-form-item>
                <el-form-item label="起拍价">
                  <el-input-number v-model="auctionForm.startPrice" :min="1" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitAuction">发布拍卖</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { tradingApi, companyApi } from '../api'
import { ElMessage } from 'element-plus'

const orders = ref([])
const companies = ref([])
const activeTab = ref('listing')

const listingForm = ref({ type: 'sell', companyId: null, quantity: 100, unitPrice: 50 })
const transferForm = ref({ sellerId: null, buyerId: null, quantity: 100, unitPrice: 50 })
const auctionForm = ref({ sellerId: null, quantity: 100, startPrice: 50 })

const loadData = async () => {
  try {
    orders.value = await tradingApi.getAll()
    companies.value = await companyApi.getAll()
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const submitListing = async () => {
  try {
    if (listingForm.value.type === 'sell') {
      await tradingApi.createSellListing(listingForm.value.companyId, listingForm.value.quantity, listingForm.value.unitPrice)
    } else {
      await tradingApi.createBuyListing(listingForm.value.companyId, listingForm.value.quantity, listingForm.value.unitPrice)
    }
    ElMessage.success('提交成功')
    loadData()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const submitTransfer = async () => {
  try {
    await tradingApi.createTransfer(transferForm.value.sellerId, transferForm.value.buyerId, transferForm.value.quantity, transferForm.value.unitPrice)
    ElMessage.success('提交成功')
    loadData()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const submitAuction = async () => {
  try {
    await tradingApi.createAuction(auctionForm.value.sellerId, auctionForm.value.quantity, auctionForm.value.startPrice)
    ElMessage.success('发布成功')
    loadData()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

const handleMatch = async (id) => {
  try {
    await tradingApi.match(id)
    ElMessage.success('匹配成功')
    loadData()
  } catch (error) {
    ElMessage.error('匹配失败')
  }
}

const getModeText = (mode) => {
  const map = { 'LISTING': '挂牌交易', 'TRANSFER': '协议转让', 'AUCTION': '竞价拍卖' }
  return map[mode] || mode
}

const getStatusText = (status) => {
  const map = { 'PENDING': '待匹配', 'MATCHED': '已匹配', 'COMPLETED': '已完成', 'CANCELLED': '已取消' }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = { 'PENDING': 'warning', 'MATCHED': 'primary', 'COMPLETED': 'success', 'CANCELLED': 'info' }
  return map[status] || 'info'
}

onMounted(() => {
  loadData()
})
</script>
