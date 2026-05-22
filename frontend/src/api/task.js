import http from './index'

export const createTask = (data) => http.post('/tasks', data)
export const publishTask = (id) => http.put(`/tasks/${id}/publish`)
export const myTasks = () => http.get('/tasks/mine')
export const assignTask = (taskId, patientId) => http.post(`/tasks/${taskId}/assign/${patientId}`)
export const assignedTasks = () => http.get('/tasks/assigned')
