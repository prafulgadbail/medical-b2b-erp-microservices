import { useQuery } from '@tanstack/react-query'
import { Package, ShoppingCart, AlertTriangle, Clock } from 'lucide-react'
import StatCard from '../../components/common/StatCard'
import { productsApi } from '../../api/products'
import { ordersApi } from '../../api/orders'
import { formatDistanceToNow } from 'date-fns'
import { statusBadge, statusLabel } from '../../utils/orderStatus'

export default function AdminDashboard() {
  const { data: lowStock } = useQuery({
    queryKey: ['inventory', 'low-stock'],
    queryFn: productsApi.getLowStock,
  })

  const { data: expiring } = useQuery({
    queryKey: ['inventory', 'expiring'],
    queryFn: () => productsApi.getExpiring(30),
  })

  const { data: recentOrders } = useQuery({
    queryKey: ['orders', 'incoming'],
    queryFn: () => ordersApi.getIncoming(0),
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="text-gray-500 text-sm mt-1">System overview and critical alerts</p>
      </div>

      {/* Stats grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatCard title="Low Stock Items" value={lowStock?.length ?? 0}
          icon={AlertTriangle} color="amber" subtitle="Requires restocking" />
        <StatCard title="Expiring Soon" value={expiring?.length ?? 0}
          icon={Clock} color="red" subtitle="Within 30 days" />
        <StatCard title="Total Orders" value={recentOrders?.totalElements ?? 0}
          icon={ShoppingCart} color="blue" />
        <StatCard title="Active Products" value="—"
          icon={Package} color="green" subtitle="Across all distributors" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Low stock alerts */}
        <div className="card">
          <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-amber-500" />
            Low Stock Alerts
          </h3>
          {lowStock?.length === 0 && (
            <p className="text-gray-500 text-sm text-center py-4">All stock levels are healthy</p>
          )}
          <div className="space-y-3">
            {lowStock?.slice(0, 8).map(item => (
              <div key={item.id} className="flex items-center justify-between py-2 border-b border-gray-50 last:border-0">
                <div>
                  <p className="text-sm font-medium text-gray-900">{item.productName}</p>
                  <p className="text-xs text-gray-500">SKU: {item.productSku} · Warehouse: {item.warehouseId}</p>
                </div>
                <div className="text-right">
                  <span className="text-sm font-bold text-amber-600">{item.quantityAvailable} units</span>
                  <p className="text-xs text-gray-400">Reorder at {item.reorderLevel}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Recent orders */}
        <div className="card">
          <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <ShoppingCart className="w-4 h-4 text-blue-500" />
            Recent Orders
          </h3>
          <div className="space-y-3">
            {recentOrders?.content?.slice(0, 8).map(order => (
              <div key={order.id} className="flex items-center justify-between py-2 border-b border-gray-50 last:border-0">
                <div>
                  <p className="text-sm font-medium text-gray-900">{order.orderNumber}</p>
                  <p className="text-xs text-gray-500">
                    {order.buyerOrgName ?? order.buyerOrgId} ·{' '}
                    {formatDistanceToNow(new Date(order.createdAt), { addSuffix: true })}
                  </p>
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
    </div>
  )
}
