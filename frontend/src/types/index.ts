export type UserRole = 'ADMIN' | 'DISTRIBUTOR' | 'HOSPITAL'

export interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  phone?: string
  role: UserRole
  organizationId: string
  active: boolean
  createdAt: string
  lastLoginAt?: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface Organization {
  id: string
  name: string
  registrationNumber: string
  type: 'HOSPITAL' | 'DISTRIBUTOR' | 'VENDOR'
  address: {
    street: string
    city: string
    state: string
    pincode: string
    country: string
  }
  contactEmail: string
  contactPhone: string
  active: boolean
  licenseNumber?: string
  licenseExpiry?: string
}

export interface Product {
  id: string
  sku: string
  name: string
  genericName?: string
  description?: string
  manufacturer: string
  category: 'MEDICINE' | 'SURGICAL' | 'DIAGNOSTIC' | 'EQUIPMENT' | 'CONSUMABLE' | 'VACCINE'
  type: 'BRANDED' | 'GENERIC'
  dosageForm?: string
  strength?: string
  unit: string
  mrp: number
  wholesalePrice: number
  prescriptionRequired: boolean
  controlledSubstance: boolean
  hsnCode?: string
  gstRate?: number
  distributorId: string
  active: boolean
  createdAt: string
}

export interface InventoryItem {
  id: string
  productId: string
  productSku: string
  productName: string
  warehouseId: string
  warehouseLocation?: string
  batchNumber: string
  manufacturingDate?: string
  expiryDate: string
  quantityAvailable: number
  quantityReserved: number
  reorderLevel: number
  distributorId: string
  status: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK' | 'EXPIRED' | 'QUARANTINED'
}

export type OrderStatus =
  | 'DRAFT'
  | 'PENDING'
  | 'APPROVED'
  | 'REJECTED'
  | 'PROCESSING'
  | 'DISPATCHED'
  | 'DELIVERED'
  | 'CANCELLED'

export interface OrderItem {
  productId: string
  productSku: string
  productName: string
  quantity: number
  unitPrice: number
  gstRate: number
  lineTotal: number
}

export interface Order {
  id: string
  orderNumber: string
  buyerOrgId: string
  buyerOrgName?: string
  distributorOrgId: string
  distributorOrgName?: string
  createdByUserId: string
  createdByEmail: string
  items: OrderItem[]
  status: OrderStatus
  subtotal: number
  gstAmount: number
  totalAmount: number
  shippingAddress: string
  notes?: string
  rejectionReason?: string
  approvedAt?: string
  dispatchedAt?: string
  trackingNumber?: string
  deliveredAt?: string
  invoice?: {
    invoiceNumber: string
    invoiceDate: string
    subtotal: number
    gstAmount: number
    totalAmount: number
    paymentTerms: string
    paymentStatus: string
  }
  createdAt: string
  updatedAt: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export interface ApiError {
  status: number
  title: string
  detail: string
  errors?: Record<string, string>
}
