import { useQuery } from '@tanstack/react-query'
import { ShoppingCart, Package, TrendingUp, AlertTriangle } from 'lucide-react'
import StatCard from '../../components/common/StatCard'
import { productsApi } from '../../api/products'
import { ordersApi } from '../../api/orders'
import { statusBadge, statusLabel } from '../../utils/orderStatus'
import { formatDistanceToNow } from 'date-fns'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import type { Order } from '../../types'

export default function DistributorDashboard() {
  const navigate = useNavigate()

  const { data: incomingOrders, refetch } = useQuery({
    queryKey: ['orders', 'incoming'],
    queryFn: () => ordersApi.getIncoming(0),
  })

  const { data: lowStock } = useQuery({
    queryKey: ['inventory', 'low-stock'],
    queryFn: productsApi.getLowStock,
  })

  const pending = incomingOrders?.content?.filter(o => o.status === 'PENDING') ?? []
  const dispatched = incomingOrders?.content?.filter(o => o.status === 'DISPATCHED') ?? []

  const handleApprove = async (order: Order) => {
    try {
      await ordersApi.approve(order.id)
      toast.success(`Order ${order.orderNumber} approved`)
      refetch()
    } catch {
      toast.error('Failed to approve order')
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Distributor Dashboard</h1>
        <p className="text-gray-500 text-sm mt-1">Manage incoming orders and inventory</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatCard title="Pending Orders" value={pending.length}
          icon={ShoppingCart} color="amber" subtitle="Awaiting approval" />
        <StatCard title="Dispatched" value={dispatched.length}
          icon={TrendingUp} color="blue" subtitle="In transit" />
        <StatCard title="Low Stock" value={lowStock?.length ?? 0}
          icon={AlertTriangle} color="red" subtitle="Items below reorder level" />
        <StatCard title="Total Orders" value={incomingOrders?.totalElements ?? 0}
          icon={Package} color="green" />
      </div>

      {/* Pending approvals */}
      <div className="card">
        <h3 className="font-semibold text-gray-900 mb-4">Pending Approvals</h3>
        {pending.length === 0 && (
          <p className="text-gray-500 text-sm text-center py-6">No pending orders</p>
        )}
        <div className="divide-y divide-gray-100">
          {pending.map(order => (
            <div key={order.id} className="py-4 flex items-center justify-between">
              <div>
                <p className="font-medium text-gray-900">{order.orderNumber}</p>
                <p className="text-sm text-gray-500">
                  From: {order.buyerOrgName ?? order.buyerOrgId} ·{' '}
                  {order.items.length} item(s) ·{' '}
                  {formatDistanceToNow(new Date(order.createdAt), { addSuffix: true })}
                </p>
                <p className="text-sm font-semibold text-gray-900 mt-1">
                  ₹{order.totalAmount.toLocaleString()}
                </p>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => navigate(`/orders/${order.id}`)}
                  className="btn-secondary text-xs px-3 py-1.5"
                >
                  View
                </button>
                <button
                  onClick={() => handleApprove(order)}
                  className="btn-primary text-xs px-3 py-1.5"
                >
                  Approve
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* All recent orders */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-semibold text-gray-900">Recent Orders</h3>
          <button onClick={() => navigate('/orders')} className="text-sm text-blue-600 hover:underline">
            View all
          </button>
        </div>
        <div className="divide-y divide-gray-100">
          {incomingOrders?.content?.slice(0, 10).map(order => (
            <div key={order.id}
              className="py-3 flex items-center justify-between cursor-pointer hover:bg-gray-50 -mx-4 px-4 rounded-lg"
              onClick={() => navigate(`/orders/${order.id}`)}>
              <div>
                <p className="text-sm font-medium text-gray-900">{order.orderNumber}</p>
                <p className="text-xs text-gray-500">{order.buyerOrgName ?? order.buyerOrgId}</p>
              </div>
              <div className="text-right">
                <span className={statusBadge[order.status]}>{statusLabel[order.status]}</span>
                <p className="text-xs text-gray-500 mt-1">₹{order.totalAmount.toLocaleString()}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
