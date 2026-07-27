import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export const companyApi = {
  getAll: () => api.get('/companies'),
  getById: (id) => api.get(`/companies/${id}`),
  create: (data) => api.post('/companies', data),
  update: (id, data) => api.put(`/companies/${id}`, data),
  delete: (id) => api.delete(`/companies/${id}`),
  getIndustries: () => api.get('/companies/industries'),
  initIndustries: () => api.post('/companies/init-industries')
}

export const emissionApi = {
  getAll: () => api.get('/emissions'),
  getById: (id) => api.get(`/emissions/${id}`),
  create: (data) => api.post('/emissions', data),
  update: (id, data) => api.put(`/emissions/${id}`, data),
  delete: (id) => api.delete(`/emissions/${id}`),
  getByCompany: (companyId) => api.get(`/emissions/company/${companyId}`),
  getByYear: (year) => api.get(`/emissions/year/${year}`),
  getPending: () => api.get('/emissions/pending-verification'),
  verify: (id, data) => api.post(`/emissions/${id}/verify`, data)
}

export const quotaApi = {
  getAll: () => api.get('/quotas'),
  getById: (id) => api.get(`/quotas/${id}`),
  create: (data) => api.post('/quotas', data),
  update: (id, data) => api.put(`/quotas/${id}`, data),
  delete: (id) => api.delete(`/quotas/${id}`),
  getByCompany: (companyId) => api.get(`/quotas/company/${companyId}`),
  getByYear: (year) => api.get(`/quotas/year/${year}`),
  getNonCompliant: (year) => api.get(`/quotas/non-compliant/${year}`),
  allocateAdditional: (id, amount) => api.post(`/quotas/${id}/additional-allocation`, { amount }),
  recordTrade: (id, quantity, isBuy) => api.post(`/quotas/${id}/trade`, { quantity, isBuy }),
  useOffset: (id, offsetAmount) => api.post(`/quotas/${id}/offset`, { offsetAmount }),
  surrender: (id, surrenderAmount) => api.post(`/quotas/${id}/surrender`, { surrenderAmount })
}

export const tradingApi = {
  getAll: () => api.get('/trading'),
  getById: (id) => api.get(`/trading/${id}`),
  createSellListing: (sellerId, quantity, unitPrice) => api.post('/trading/listing/sell', { sellerId, quantity, unitPrice }),
  createBuyListing: (buyerId, quantity, unitPrice) => api.post('/trading/listing/buy', { buyerId, quantity, unitPrice }),
  createTransfer: (sellerId, buyerId, quantity, unitPrice) => api.post('/trading/transfer', { sellerId, buyerId, quantity, unitPrice }),
  createAuction: (sellerId, quantity, startPrice) => api.post('/trading/auction', { sellerId, quantity, startPrice }),
  submitBid: (orderId, bidderId, bidPrice) => api.post(`/trading/auction/${orderId}/bid`, { bidderId, bidPrice }),
  match: (orderId) => api.post(`/trading/match/${orderId}`),
  cancel: (orderId) => api.post(`/trading/cancel/${orderId}`),
  getByStatus: (status) => api.get(`/trading/status/${status}`),
  getByMode: (mode) => api.get(`/trading/mode/${mode}`),
  getAvgPrice: (days) => api.get('/trading/avg-price', { params: { days } }),
  getTotalVolume: (days) => api.get('/trading/total-volume', { params: { days } })
}

export const riskApi = {
  getPendingAlerts: () => api.get('/risk/alerts/pending'),
  getAllAlerts: () => api.get('/risk/alerts'),
  getAlertCount: () => api.get('/risk/alerts/count'),
  processAlert: (id, result) => api.post(`/risk/alerts/${id}/process`, { processingResult: result })
}

export const priceApi = {
  getPrediction: () => api.get('/price/prediction'),
  getTrend: () => api.get('/price/trend'),
  getLiquidity: () => api.get('/price/liquidity'),
  getHistory: (days) => api.get('/price/history', { params: { days } }),
  getAnomaly: () => api.get('/price/anomaly')
}

export const dashboardApi = {
  getOverview: () => api.get('/dashboard/overview'),
  getIndustryDistribution: () => api.get('/dashboard/industry-distribution'),
  getTradingHeatMap: () => api.get('/dashboard/trading-heatmap'),
  getComplianceProgress: () => api.get('/dashboard/compliance-progress'),
  getPriceTrend: (days) => api.get('/dashboard/price-trend', { params: { days } }),
  getRiskAlertsSummary: () => api.get('/dashboard/risk-alerts-summary'),
  getLiquidityIndicators: () => api.get('/dashboard/liquidity-indicators'),
  getTopCompanies: () => api.get('/dashboard/top-companies')
}

export default api
