import { useQuery } from '@tanstack/react-query'
import { AlertTriangle, Clock, CheckCircle } from 'lucide-react'
import { productsApi } from '../api/products'
import { format } from 'date-fns'
import type { InventoryItem } from '../types'

const statusBadge: Record<InventoryItem['status'], string> = {
  IN_STOCK: 'badge-green',
  LOW_STOCK: 'badge-amber',
  OUT_OF_STOCK: 'badge-red',
  EXPIRED: 'badge-red',
  QUARANTINED: 'badge-purple',
}

export default function InventoryPage() {
  const { data: lowStock, isLoading: loadingLow } = useQuery({
    queryKey: ['inventory', 'low-stock'],
    queryFn: productsApi.getLowStock,
  })

  const { data: expiring, isLoading: loadingExp } = useQuery({
    queryKey: ['inventory', 'expiring', 30],
    queryFn: () => productsApi.getExpiring(30),
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Inventory Management</h1>
        <p className="text-gray-500 text-sm mt-1">Monitor stock levels and expiry dates</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
        <div className="card bg-amber-50 border-amber-100">
          <div className="flex items-center gap-3">
            <AlertTriangle className="w-8 h-8 text-amber-500" />
            <div>
              <p className="text-2xl font-bold text-amber-700">{lowStock?.length ?? 0}</p>
              <p className="text-sm text-amber-600">Low Stock Items</p>
            </div>
          </div>
        </div>
        <div className="card bg-red-50 border-red-100">
          <div className="flex items-center gap-3">
            <Clock className="w-8 h-8 text-red-500" />
            <div>
              <p className="text-2xl font-bold text-red-700">{expiring?.length ?? 0}</p>
              <p className="text-sm text-red-600">Expiring in 30 days</p>
            </div>
          </div>
        </div>
        <div className="card bg-green-50 border-green-100">
          <div className="flex items-center gap-3">
            <CheckCircle className="w-8 h-8 text-green-500" />
            <div>
              <p className="text-2xl font-bold text-green-700">
                {Math.max(0, (lowStock?.length ?? 0) === 0 ? 100 : 85)}%
              </p>
              <p className="text-sm text-green-600">Stock Health</p>
            </div>
          </div>
        </div>
      </div>

      {/* Low stock table */}
      <div className="card">
        <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 text-amber-500" />
          Low Stock Items
        </h3>
        {loadingLow ? (
          <div className="animate-pulse space-y-3">
            {[...Array(4)].map((_, i) => <div key={i} className="h-10 bg-gray-100 rounded" />)}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-xs text-gray-500 border-b border-gray-100">
                  <th className="pb-3 font-medium">Product</th>
                  <th className="pb-3 font-medium">Warehouse</th>
                  <th className="pb-3 font-medium">Batch</th>
                  <th className="pb-3 font-medium">Available</th>
                  <th className="pb-3 font-medium">Reserved</th>
                  <th className="pb-3 font-medium">Reorder At</th>
                  <th className="pb-3 font-medium">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {lowStock?.map((item) => (
                  <tr key={item.id} className="hover:bg-gray-50">
                    <td className="py-3">
                      <p className="font-medium text-gray-900">{item.productName}</p>
                      <p className="text-xs text-gray-400">{item.productSku}</p>
                    </td>
                    <td className="py-3 text-gray-600">{item.warehouseId}</td>
                    <td className="py-3 text-gray-600 font-mono text-xs">{item.batchNumber}</td>
                    <td className="py-3 font-bold text-amber-600">{item.quantityAvailable}</td>
                    <td className="py-3 text-gray-600">{item.quantityReserved}</td>
                    <td className="py-3 text-gray-600">{item.reorderLevel}</td>
                    <td className="py-3">
                      <span className={statusBadge[item.status]}>{item.status.replace('_', ' ')}</span>
                    </td>
                  </tr>
                ))}
                {lowStock?.length === 0 && (
                  <tr>
                    <td colSpan={7} className="py-8 text-center text-gray-400">
                      No low stock items — all healthy!
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Expiring soon */}
      <div className="card">
        <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <Clock className="w-4 h-4 text-red-500" />
          Expiring Within 30 Days
        </h3>
        {loadingExp ? (
          <div className="animate-pulse space-y-3">
            {[...Array(3)].map((_, i) => <div key={i} className="h-10 bg-gray-100 rounded" />)}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-xs text-gray-500 border-b border-gray-100">
                  <th className="pb-3 font-medium">Product</th>
                  <th className="pb-3 font-medium">Batch</th>
                  <th className="pb-3 font-medium">Expiry Date</th>
                  <th className="pb-3 font-medium">Quantity</th>
                  <th className="pb-3 font-medium">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {expiring?.map((item) => (
                  <tr key={item.id} className="hover:bg-gray-50">
                    <td className="py-3">
                      <p className="font-medium text-gray-900">{item.productName}</p>
                      <p className="text-xs text-gray-400">{item.productSku}</p>
                    </td>
                    <td className="py-3 font-mono text-xs text-gray-600">{item.batchNumber}</td>
                    <td className="py-3 text-red-600 font-medium">
                      {format(new Date(item.expiryDate), 'dd MMM yyyy')}
                    </td>
                    <td className="py-3 text-gray-700">{item.quantityAvailable}</td>
                    <td className="py-3">
                      <span className={statusBadge[item.status]}>{item.status.replace('_', ' ')}</span>
                    </td>
                  </tr>
                ))}
                {expiring?.length === 0 && (
                  <tr>
                    <td colSpan={5} className="py-8 text-center text-gray-400">
                      No items expiring in the next 30 days
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
