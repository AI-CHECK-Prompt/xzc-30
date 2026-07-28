import { createRouter, createWebHistory } from 'vue-router'
import Home from './views/Home.vue'
import Companies from './views/Companies.vue'
import Emissions from './views/Emissions.vue'
import Quotas from './views/Quotas.vue'
import Trading from './views/Trading.vue'
import Risk from './views/Risk.vue'
import Prediction from './views/Prediction.vue'
import Dashboard from './views/Dashboard.vue'
import TradingSignal from './views/TradingSignal.vue'
import SignalHistory from './views/SignalHistory.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/companies', name: 'Companies', component: Companies },
  { path: '/emissions', name: 'Emissions', component: Emissions },
  { path: '/quotas', name: 'Quotas', component: Quotas },
  { path: '/trading', name: 'Trading', component: Trading },
  { path: '/risk', name: 'Risk', component: Risk },
  { path: '/prediction', name: 'Prediction', component: Prediction },
  { path: '/dashboard', name: 'Dashboard', component: Dashboard },
  { path: '/signal', name: 'TradingSignal', component: TradingSignal },
  { path: '/signal-history', name: 'SignalHistory', component: SignalHistory }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
