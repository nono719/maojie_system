import http from './index'

export const completeTraining = (taskId, data) => http.post(`/training/${taskId}/complete`, data)
export const myHistory = () => http.get('/training/history')
export const patientHistory = (patientId) => http.get(`/training/history/${patientId}`)

export const verifyOnChain = (recordId) => http.get(`/chain/verify/${recordId}`)
export const queryBalance = (address) => http.get(`/chain/balance/${address}`)

export const patientHome = () => http.get('/patient/home')
export const myRewards = () => http.get('/patient/rewards')

export const doctorDashboard = () => http.get('/doctor/dashboard')
export const myPatients = () => http.get('/doctor/patients')
