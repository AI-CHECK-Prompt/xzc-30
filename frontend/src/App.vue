<template>
  <el-container class="app-container">
    <el-aside width="200px">
      <div class="logo">
        <el-icon :size="30"><DataLine /></el-icon>
        <span>碳交易平台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        router
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/companies">
          <el-icon><OfficeBuilding /></el-icon>
          <span>企业管理</span>
        </el-menu-item>
        <el-menu-item index="/emissions">
          <el-icon><DataAnalysis /></el-icon>
          <span>排放数据</span>
        </el-menu-item>
        <el-menu-item index="/quotas">
          <el-icon><Coin /></el-icon>
          <span>配额管理</span>
        </el-menu-item>
        <el-menu-item index="/trading">
          <el-icon><TrendCharts /></el-icon>
          <span>交易大厅</span>
        </el-menu-item>
        <el-menu-item index="/risk">
          <el-icon><Warning /></el-icon>
          <span>风险监控</span>
        </el-menu-item>
        <el-menu-item index="/prediction">
          <el-icon><Odometer /></el-icon>
          <span>价格预测</span>
        </el-menu-item>
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>监管大屏</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-title">全国碳排放权交易平台</div>
        <div class="header-user">
          <el-icon><User /></el-icon>
          <span>管理员</span>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = ref('/')

watch(() => route.path, (path) => {
  activeMenu.value = path
}, { immediate: true })
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.app-container {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #404854;
}

.el-menu {
  border: none;
  background-color: #304156;
}

.el-menu-item {
  color: #bfcbd9;
}

.el-menu-item:hover,
.el-menu-item.is-active {
  background-color: #263445;
  color: #409eff;
}

.el-header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
