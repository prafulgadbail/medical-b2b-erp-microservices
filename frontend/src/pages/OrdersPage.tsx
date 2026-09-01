import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { ShoppingCart, Plus } from 'lucide-react'
import { ordersApi } from '../api/orders'
import { useAuthStore } from '../store/authStore'
import { statusBadge, statusLabel } from '../utils/orderStatus'
import { formatDistanceToNow } from 'date-fns'
import { useState } from 'react'

export default function OrdersPage() {
  const navigate = useNavigate()
  const user = useAuthStore(s => s.user)
  const [page, setPage] = useState(0)

  const isDistributor = user?.role === 'DISTRIBUTOR' || user?.role === 'ADMIN'

  const { data, isLoading } = useQuery({
    queryKey: ['orders', isDistributor ? 'incoming' : 'my', page],
    queryFn: () => isDistributor ? ordersApi.getIncoming(page) : ordersApi.getMyOrders(page),
  })

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {isDistributor ? 'Incoming Orders' : 'My Orders'}
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            {data?.totalElements ?? 0} total orders
          </p>
        </div>
        {user?.role === 'HOSPITAL' && (
          <button className="btn-primary flex items-center gap-2">
            <Plus className="w-4 h-4" /> New Order
          </button>
        )}
      </div>

      {isLoading ? (
        <div className="card animate-pulse space-y-4">
          {[...Array(5)].map((_, i) => <div key={i} className="h-16 bg-gray-100 rounded" />)}
        </div>
      ) : (
        <>
          <div className="card p-0 overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-100">
                <tr>
                  <th className="text-left px-6 py-3 text-xs font-medium text-gray-500">Order #</th>
                  <th className="text-left px-4 py-3 text-xs font-medium text-gray-500">
                    {isDistributor ? 'Hospital' : 'Distributor'}
                  </th>
                  <th className="text-left px-4 py-3 text-xs font-medium text-gray-500">Items</th>
                  <th className="text-left px-4 py-3 text-xs font-medium text-gray-500">Amount</th>
                  <th className="text-left px-4 py-3 text-xs font-medium text-gray-500">Status</th>
                  <th className="text-left px-4 py-3 text-xs font-medium text-gray-500">Created</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {data?.content?.map(order => (
                  <tr key={order.id} className="hover:bg-gray-50 cursor-pointer"
                    onClick={() => navigate(`/orders/${order.id}`)}>
                    <td className="px-6 py-4 font-medium text-blue-600">{order.orderNumber}</td>
                    <td className="px-4 py-4 text-gray-700">
                      {isDistributor
                        ? (order.buyerOrgName ?? order.buyerOrgId)
                        : (order.distributorOrgName ?? order.distributorOrgId)}
                    </td>
                    <td className="px-4 py-4 text-gray-600">{order.items.length}</td>
                    <td className="px-4 py-4 font-semibold text-gray-900">
                      ₹{order.totalAmount.toLocaleString()}
                    </td>
                    <td className="px-4 py-4">
                      <span className={statusBadge[order.status]}>{statusLabel[order.status]}</span>
                    </td>
                    <td className="px-4 py-4 text-gray-500 text-xs">
                      {formatDistanceToNow(new Date(order.createdAt), { addSuffix: true })}
                    </td>
                    <td className="px-4 py-4">
                      <button className="text-blue-600 text-xs hover:underline">View</button>
                    </td>
                  </tr>
                ))}
                {data?.content?.length === 0 && (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center">
                      <ShoppingCart className="w-10 h-10 text-gray-300 mx-auto mb-2" />
                      <p className="text-gray-500">No orders found</p>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2">
              <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={data.first}
                className="btn-secondary text-sm disabled:opacity-40">Previous</button>
              <span className="text-sm text-gray-600">Page {data.number + 1} of {data.totalPages}</span>
              <button onClick={() => setPage(p => p + 1)} disabled={data.last}
                className="btn-secondary text-sm disabled:opacity-40">Next</button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
