import { createRouter, createWebHistory } from 'vue-router'
import Home from './views/Home.vue'
import Companies from './views/Companies.vue'
import Emissions from './views/Emissions.vue'
import Quotas from './views/Quotas.vue'
import Trading from './views/Trading.vue'
import Risk from './views/Risk.vue'
import Prediction from './views/Prediction.vue'
import Dashboard from './views/Dashboard.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/companies', name: 'Companies', component: Companies },
  { path: '/emissions', name: 'Emissions', component: Emissions },
  { path: '/quotas', name: 'Quotas', component: Quotas },
  { path: '/trading', name: 'Trading', component: Trading },
  { path: '/risk', name: 'Risk', component: Risk },
  { path: '/prediction', name: 'Prediction', component: Prediction },
  { path: '/dashboard', name: 'Dashboard', component: Dashboard }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
