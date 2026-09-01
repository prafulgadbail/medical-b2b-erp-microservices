import { orderApi } from './axios'
import type { Order, PageResponse } from '../types'

export const ordersApi = {
  create: (data: {
    distributorOrgId: string
    shippingAddress: string
    notes?: string
    items: Array<{ productId: string; quantity: number }>
  }) => orderApi.post<Order>('/orders', data).then(r => r.data),

  getById: (id: string) =>
    orderApi.get<Order>(`/orders/${id}`).then(r => r.data),

  getMyOrders: (page = 0) =>
    orderApi.get<PageResponse<Order>>('/orders/my', { params: { page } }).then(r => r.data),

  getIncoming: (page = 0) =>
    orderApi.get<PageResponse<Order>>('/orders/incoming', { params: { page } }).then(r => r.data),

  approve: (id: string) =>
    orderApi.patch<Order>(`/orders/${id}/approve`).then(r => r.data),

  reject: (id: string, reason: string) =>
    orderApi.patch<Order>(`/orders/${id}/reject`, { reason }).then(r => r.data),

  dispatch: (id: string, trackingNumber: string) =>
    orderApi.patch<Order>(`/orders/${id}/dispatch`, { trackingNumber }).then(r => r.data),

  confirmDelivery: (id: string) =>
    orderApi.patch<Order>(`/orders/${id}/deliver`).then(r => r.data),

  cancel: (id: string) =>
    orderApi.patch<Order>(`/orders/${id}/cancel`).then(r => r.data),
}
