import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, CheckCircle, XCircle, Truck, Package } from 'lucide-react'
import { ordersApi } from '../api/orders'
import { useAuthStore } from '../store/authStore'
import { statusBadge, statusLabel } from '../utils/orderStatus'
import { format } from 'date-fns'
import toast from 'react-hot-toast'

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const user = useAuthStore(s => s.user)
  const queryClient = useQueryClient()

  const { data: order, isLoading } = useQuery({
    queryKey: ['order', id],
    queryFn: () => ordersApi.getById(id!),
    enabled: !!id,
  })

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['order', id] })

  const approveMutation = useMutation({
    mutationFn: () => ordersApi.approve(id!),
    onSuccess: () => { toast.success('Order approved'); invalidate() },
    onError: () => toast.error('Failed to approve order'),
  })

  const rejectMutation = useMutation({
    mutationFn: () => ordersApi.reject(id!, 'Rejected by distributor'),
    onSuccess: () => { toast.success('Order rejected'); invalidate() },
    onError: () => toast.error('Failed to reject order'),
  })

  const dispatchMutation = useMutation({
    mutationFn: () => ordersApi.dispatch(id!, `TRK-${Date.now()}`),
    onSuccess: () => { toast.success('Order dispatched'); invalidate() },
    onError: () => toast.error('Failed to dispatch order'),
  })

  const deliverMutation = useMutation({
    mutationFn: () => ordersApi.confirmDelivery(id!),
    onSuccess: () => { toast.success('Delivery confirmed'); invalidate() },
    onError: () => toast.error('Failed to confirm delivery'),
  })

  if (isLoading) {
    return (
      <div className="animate-pulse space-y-4">
        <div className="h-8 bg-gray-200 rounded w-1/3" />
        <div className="card h-64 bg-gray-100" />
      </div>
    )
  }

  if (!order) return <div className="text-center py-12 text-gray-500">Order not found</div>

  const isDistributor = user?.role === 'DISTRIBUTOR' || user?.role === 'ADMIN'
  const isHospital = user?.role === 'HOSPITAL'

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center gap-4">
        <button onClick={() => navigate(-1)} className="p-2 hover:bg-gray-100 rounded-lg">
          <ArrowLeft className="w-5 h-5 text-gray-600" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{order.orderNumber}</h1>
          <p className="text-gray-500 text-sm mt-0.5">
            Created {format(new Date(order.createdAt), 'dd MMM yyyy, HH:mm')}
          </p>
        </div>
        <span className={`ml-auto ${statusBadge[order.status]} text-sm px-3 py-1`}>
          {statusLabel[order.status]}
        </span>
      </div>

      {/* Action buttons */}
      {isDistributor && order.status === 'PENDING' && (
        <div className="flex gap-3">
          <button
            onClick={() => approveMutation.mutate()}
            disabled={approveMutation.isPending}
            className="btn-primary flex items-center gap-2"
          >
            <CheckCircle className="w-4 h-4" /> Approve Order
          </button>
          <button
            onClick={() => rejectMutation.mutate()}
            disabled={rejectMutation.isPending}
            className="btn-danger flex items-center gap-2"
          >
            <XCircle className="w-4 h-4" /> Reject
          </button>
        </div>
      )}

      {isDistributor && order.status === 'APPROVED' && (
        <button
          onClick={() => dispatchMutation.mutate()}
          disabled={dispatchMutation.isPending}
          className="btn-primary flex items-center gap-2"
        >
          <Truck className="w-4 h-4" /> Mark as Dispatched
        </button>
      )}

      {isHospital && order.status === 'DISPATCHED' && (
        <button
          onClick={() => deliverMutation.mutate()}
          disabled={deliverMutation.isPending}
          className="btn-primary flex items-center gap-2"
        >
          <CheckCircle className="w-4 h-4" /> Confirm Delivery
        </button>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Order info */}
        <div className="card space-y-4">
          <h3 className="font-semibold text-gray-900">Order Details</h3>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-500">Buyer</span>
              <span className="font-medium">{order.buyerOrgName ?? order.buyerOrgId}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Distributor</span>
              <span className="font-medium">{order.distributorOrgName ?? order.distributorOrgId}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Placed by</span>
              <span className="font-medium">{order.createdByEmail}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Shipping to</span>
              <span className="font-medium text-right max-w-[200px]">{order.shippingAddress}</span>
            </div>
            {order.trackingNumber && (
              <div className="flex justify-between">
                <span className="text-gray-500">Tracking</span>
                <span className="font-mono font-medium text-blue-600">{order.trackingNumber}</span>
              </div>
            )}
          </div>
        </div>

        {/* Invoice */}
        <div className="card space-y-4">
          <h3 className="font-semibold text-gray-900">
            {order.invoice ? `Invoice ${order.invoice.invoiceNumber}` : 'Billing Summary'}
          </h3>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-500">Subtotal</span>
              <span>₹{order.subtotal.toLocaleString()}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">GST</span>
              <span>₹{order.gstAmount.toLocaleString()}</span>
            </div>
            <div className="flex justify-between border-t border-gray-100 pt-2 mt-2">
              <span className="font-semibold">Total</span>
              <span className="font-bold text-lg">₹{order.totalAmount.toLocaleString()}</span>
            </div>
            {order.invoice && (
              <div className="flex justify-between pt-2">
                <span className="text-gray-500">Payment</span>
                <span className={order.invoice.paymentStatus === 'PAID' ? 'text-green-600 font-medium' : 'text-amber-600 font-medium'}>
                  {order.invoice.paymentStatus} · {order.invoice.paymentTerms}
                </span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Order items */}
      <div className="card">
        <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <Package className="w-4 h-4" /> Order Items ({order.items.length})
        </h3>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs text-gray-500 border-b border-gray-100">
                <th className="pb-3 font-medium">Product</th>
                <th className="pb-3 font-medium">SKU</th>
                <th className="pb-3 font-medium text-center">Qty</th>
                <th className="pb-3 font-medium text-right">Unit Price</th>
                <th className="pb-3 font-medium text-right">GST</th>
                <th className="pb-3 font-medium text-right">Line Total</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {order.items.map((item, i) => (
                <tr key={i}>
                  <td className="py-3 font-medium text-gray-900">{item.productName}</td>
                  <td className="py-3 font-mono text-xs text-gray-500">{item.productSku}</td>
                  <td className="py-3 text-center text-gray-700">{item.quantity}</td>
                  <td className="py-3 text-right text-gray-700">₹{item.unitPrice}</td>
                  <td className="py-3 text-right text-gray-500">{item.gstRate}%</td>
                  <td className="py-3 text-right font-semibold text-gray-900">₹{item.lineTotal}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
