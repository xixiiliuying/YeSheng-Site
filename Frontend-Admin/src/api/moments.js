import http from '@/utils/request'

export const getMomentsPage = (params) => http.get('/admin/moments/page', { params })
export const createMoment = (data) => http.post('/admin/moments', data)
export const updateMoment = (data) => http.put('/admin/moments', data)
export const deleteMoment = (id) => http.delete(`/admin/moments/${id}`)
export const approveMoment = (id, isApproved) =>
  http.put(`/admin/moments/approve/${id}`, null, { params: { isApproved } })
