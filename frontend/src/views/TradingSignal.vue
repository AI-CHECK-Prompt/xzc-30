<template>
  <div class="trading-signal">
    <h2>交易信号</h2>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="signal" class="signal-container">
      <!-- 信号类型卡片 -->
      <div :class="['signal-card', signal.signalType.toLowerCase()]">
        <div class="signal-type">{{ signal.signalType }}</div>
        <div class="signal-time">{{ formatTime(signal.signalTime) }}</div>
      </div>

      <!-- 置信度 -->
      <div class="confidence-section">
        <h3>置信度</h3>
        <div :class="['confidence-badge', signal.confidenceLevel.toLowerCase()]">
          {{ signal.confidenceLevel }}
        </div>
      </div>

      <!-- 价格区间 -->
      <div class="price-range-section">
        <h3>建议价格区间</h3>
        <div class="price-range">
          <span>{{ signal.priceRangeStart }}</span>
          <span> - </span>
          <span>{{ signal.priceRangeEnd }}</span>
        </div>
      </div>

      <!-- 影响因素 -->
      <div class="factors-section">
        <h3>主要影响因素</h3>
        <div v-if="keyFactors" class="factors-list">
          <div v-for="(value, key) in keyFactors" :key="key" class="factor-item">
            <span class="factor-key">{{ key }}:</span>
            <span class="factor-value">{{ formatFactorValue(value) }}</span>
          </div>
        </div>
      </div>

      <!-- 因素贡献度 -->
      <div v-if="factorContribution" class="contribution-section">
        <h3>因素贡献度</h3>
        <div class="contribution-chart">
          <div
            v-for="(value, key) in factorContribution"
            :key="key"
            class="contribution-bar"
          >
            <span class="contribution-label">{{ key }}</span>
            <div class="bar-container">
              <div
                class="bar-fill"
                :style="{ width: value + '%' }"
              ></div>
            </div>
            <span class="contribution-value">{{ value.toFixed(1) }}%</span>
          </div>
        </div>
      </div>

      <!-- 生成新信号按钮 -->
      <button @click="generateSignal" class="generate-btn">
        生成新信号
      </button>
    </div>

    <div v-else class="no-signal">
      <p>暂无交易信号</p>
      <button @click="generateSignal" class="generate-btn">
        生成信号
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'TradingSignal',
  data() {
    return {
      loading: false,
      signal: null,
      keyFactors: null,
      factorContribution: null
    };
  },
  mounted() {
    this.loadLatestSignal();
  },
  methods: {
    async loadLatestSignal() {
      this.loading = true;
      try {
        const response = await axios.get('/api/signal/latest');
        this.signal = response.data;
        if (this.signal) {
          this.loadSignalDetails(this.signal.id);
        }
      } catch (error) {
        console.error('Failed to load signal:', error);
      } finally {
        this.loading = false;
      }
    },
    async loadSignalDetails(signalId) {
      try {
        const response = await axios.get(`/api/signal/${signalId}`);
        const signal = response.data;
        this.keyFactors = signal.keyFactors ? JSON.parse(signal.keyFactors) : null;
        this.factorContribution = signal.factorContribution ? JSON.parse(signal.factorContribution) : null;
      } catch (error) {
        console.error('Failed to load signal details:', error);
      }
    },
    async generateSignal() {
      this.loading = true;
      try {
        const response = await axios.post('/api/signal/generate');
        if (response.data.status === 'success') {
          this.signal = response.data.signal;
          this.keyFactors = response.data.keyFactors;
          this.factorContribution = response.data.factorContribution;
        }
      } catch (error) {
        console.error('Failed to generate signal:', error);
      } finally {
        this.loading = false;
      }
    },
    formatTime(time) {
      return new Date(time).toLocaleString('zh-CN');
    },
    formatFactorValue(value) {
      if (Array.isArray(value)) {
        return value.join(', ');
      }
      return value;
    }
  }
};
</script>

<style scoped>
.trading-signal {
  padding: 20px;
}

.signal-card {
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
}

.signal-card.buy {
  background-color: #4caf50;
  color: white;
}

.signal-card.sell {
  background-color: #f44336;
  color: white;
}

.signal-card.hold {
  background-color: #9e9e9e;
  color: white;
}

.signal-type {
  font-size: 24px;
  font-weight: bold;
}

.confidence-section, .price-range-section, .factors-section, .contribution-section {
  margin: 20px 0;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 8px;
}

.confidence-badge {
  display: inline-block;
  padding: 5px 15px;
  border-radius: 4px;
  font-weight: bold;
}

.confidence-badge.high {
  background-color: #4caf50;
  color: white;
}

.confidence-badge.medium {
  background-color: #ff9800;
  color: white;
}

.confidence-badge.low {
  background-color: #f44336;
  color: white;
}

.price-range {
  font-size: 18px;
  font-weight: bold;
}

.factor-item {
  margin: 5px 0;
}

.contribution-bar {
  display: flex;
  align-items: center;
  margin: 10px 0;
}

.contribution-label {
  width: 120px;
}

.bar-container {
  flex: 1;
  height: 20px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #2196f3;
}

.contribution-value {
  width: 60px;
  text-align: right;
}

.generate-btn {
  padding: 10px 20px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.generate-btn:hover {
  background: #1976d2;
}

.loading, .no-signal {
  text-align: center;
  padding: 40px;
}
</style>
