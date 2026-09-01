import { productApi } from './axios'
import type { Product, InventoryItem, PageResponse } from '../types'

export const productsApi = {
  list: (params?: { category?: string; distributorId?: string; page?: number; size?: number }) =>
    productApi.get<PageResponse<Product>>('/products', { params }).then(r => r.data),

  search: (q: string, page = 0) =>
    productApi.get<PageResponse<Product>>('/products/search', { params: { q, page } }).then(r => r.data),

  getById: (id: string) =>
    productApi.get<Product>(`/products/${id}`).then(r => r.data),

  create: (data: Omit<Product, 'id' | 'active' | 'createdAt'>) =>
    productApi.post<Product>('/products', data).then(r => r.data),

  getInventory: (productId: string) =>
    productApi.get<InventoryItem[]>(`/products/${productId}/inventory`).then(r => r.data),

  getLowStock: () =>
    productApi.get<InventoryItem[]>('/products/inventory/low-stock').then(r => r.data),

  getExpiring: (days = 30) =>
    productApi.get<InventoryItem[]>('/products/inventory/expiring', { params: { days } }).then(r => r.data),

  addStock: (data: {
    productId: string
    warehouseId: string
    warehouseLocation?: string
    batchNumber: string
    manufacturingDate?: string
    expiryDate: string
    quantity: number
    reorderLevel: number
    distributorId: string
  }) => productApi.post<InventoryItem>('/products/inventory', data).then(r => r.data),
}
