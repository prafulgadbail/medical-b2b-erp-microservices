import { useQuery } from '@tanstack/react-query'
import { ShoppingCart, Package, CheckCircle, Clock } from 'lucide-react'
import StatCard from '../../components/common/StatCard'
import { ordersApi } from '../../api/orders'
import { productsApi } from '../../api/products'
import { statusBadge, statusLabel } from '../../utils/orderStatus'
import { formatDistanceToNow } from 'date-fns'
import { useNavigate } from 'react-router-dom'

export default function HospitalDashboard() {
  const navigate = useNavigate()

  const { data: myOrders } = useQuery({
    queryKey: ['orders', 'my'],
    queryFn: () => ordersApi.getMyOrders(0),
  })

  const { data: products } = useQuery({
    queryKey: ['products'],
    queryFn: () => productsApi.list({ size: 6 }),
  })

  const pending = myOrders?.content?.filter(o => o.status === 'PENDING') ?? []
  const delivered = myOrders?.content?.filter(o => o.status === 'DELIVERED') ?? []

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Hospital Dashboard</h1>
        <p className="text-gray-500 text-sm mt-1">Track your orders and browse products</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatCard title="My Orders" value={myOrders?.totalElements ?? 0}
          icon={ShoppingCart} color="blue" />
        <StatCard title="Pending" value={pending.length}
          icon={Clock} color="amber" subtitle="Awaiting approval" />
        <StatCard title="Delivered" value={delivered.length}
          icon={CheckCircle} color="green" />
        <StatCard title="Available Products" value={products?.totalElements ?? 0}
          icon={Package} color="purple" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* My recent orders */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold text-gray-900">My Recent Orders</h3>
            <button onClick={() => navigate('/orders')} className="text-sm text-blue-600 hover:underline">
              View all
            </button>
          </div>
          {myOrders?.content?.length === 0 && (
            <div className="text-center py-8">
              <ShoppingCart className="w-10 h-10 text-gray-300 mx-auto mb-2" />
              <p className="text-gray-500 text-sm">No orders yet</p>
              <button onClick={() => navigate('/products')} className="btn-primary mt-3 text-sm">
                Browse Products
              </button>
            </div>
          )}
          <div className="divide-y divide-gray-100">
            {myOrders?.content?.slice(0, 8).map(order => (
              <div key={order.id}
                className="py-3 flex items-center justify-between cursor-pointer hover:bg-gray-50 -mx-4 px-4 rounded-lg"
                onClick={() => navigate(`/orders/${order.id}`)}>
                <div>
                  <p className="text-sm font-medium text-gray-900">{order.orderNumber}</p>
                  <p className="text-xs text-gray-500">
                    {order.items.length} item(s) ·{' '}
                    {formatDistanceToNow(new Date(order.createdAt), { addSuffix: true })}
                  </p>
                </div>
                <div className="text-right">
                  <span className={statusBadge[order.status]}>{statusLabel[order.status]}</span>
                  <p className="text-xs font-medium text-gray-700 mt-1">₹{order.totalAmount.toLocaleString()}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Quick product browse */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold text-gray-900">Available Products</h3>
            <button onClick={() => navigate('/products')} className="text-sm text-blue-600 hover:underline">
              View all
            </button>
          </div>
          <div className="grid grid-cols-1 gap-3">
            {products?.content?.slice(0, 6).map(product => (
              <div key={product.id} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                <div className="w-9 h-9 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                  <Package className="w-4 h-4 text-blue-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">{product.name}</p>
                  <p className="text-xs text-gray-500">{product.manufacturer} · {product.category}</p>
                </div>
                <p className="text-sm font-semibold text-gray-900">₹{product.wholesalePrice}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
