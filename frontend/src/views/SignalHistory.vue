<template>
  <div class="signal-history">
    <h2>信号历史</h2>

    <div class="filter-section">
      <select v-model="filterType" @change="loadHistory">
        <option value="">全部类型</option>
        <option value="BUY">买入</option>
        <option value="SELL">卖出</option>
        <option value="HOLD">观望</option>
      </select>
    </div>

    <table class="signal-table">
      <thead>
        <tr>
          <th>时间</th>
          <th>信号类型</th>
          <th>置信度</th>
          <th>价格区间</th>
          <th>模型版本</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="signal in signals" :key="signal.id" @click="showDetail(signal)">
          <td>{{ formatTime(signal.signalTime) }}</td>
          <td :class="signal.signalType.toLowerCase()">{{ signal.signalType }}</td>
          <td>{{ signal.confidenceLevel }}</td>
          <td>{{ signal.priceRangeStart }} - {{ signal.priceRangeEnd }}</td>
          <td>{{ signal.modelVersion }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'SignalHistory',
  data() {
    return {
      signals: [],
      filterType: ''
    };
  },
  mounted() {
    this.loadHistory();
  },
  methods: {
    async loadHistory() {
      try {
        const response = await axios.get('/api/signal/history');
        this.signals = response.data;
        if (this.filterType) {
          this.signals = this.signals.filter(s => s.signalType === this.filterType);
        }
      } catch (error) {
        console.error('Failed to load signal history:', error);
      }
    },
    formatTime(time) {
      return new Date(time).toLocaleString('zh-CN');
    },
    showDetail(signal) {
      this.$router.push(`/signal/${signal.id}`);
    }
  }
};
</script>

<style scoped>
.signal-history {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
}

.signal-table {
  width: 100%;
  border-collapse: collapse;
}

.signal-table th, .signal-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.signal-table tbody tr:hover {
  background: #f5f5f5;
  cursor: pointer;
}

.buy { color: #4caf50; font-weight: bold; }
.sell { color: #f44336; font-weight: bold; }
.hold { color: #9e9e9e; font-weight: bold; }
</style>
