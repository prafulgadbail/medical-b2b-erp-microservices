import type { OrderStatus } from '../types'

export const statusBadge: Record<OrderStatus, string> = {
  DRAFT:      'badge-gray',
  PENDING:    'badge-amber',
  APPROVED:   'badge-blue',
  REJECTED:   'badge-red',
  PROCESSING: 'badge-purple',
  DISPATCHED: 'badge-blue',
  DELIVERED:  'badge-green',
  CANCELLED:  'badge-red',
}

export const statusLabel: Record<OrderStatus, string> = {
  DRAFT:      'Draft',
  PENDING:    'Pending',
  APPROVED:   'Approved',
  REJECTED:   'Rejected',
  PROCESSING: 'Processing',
  DISPATCHED: 'Dispatched',
  DELIVERED:  'Delivered',
  CANCELLED:  'Cancelled',
}
