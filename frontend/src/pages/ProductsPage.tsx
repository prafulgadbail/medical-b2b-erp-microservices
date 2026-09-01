import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Package, Search, Filter } from 'lucide-react'
import { productsApi } from '../api/products'
import { useAuthStore } from '../store/authStore'
import type { Product } from '../types'

const categories = ['ALL', 'MEDICINE', 'SURGICAL', 'DIAGNOSTIC', 'EQUIPMENT', 'CONSUMABLE', 'VACCINE']

const categoryBadge: Record<string, string> = {
  MEDICINE: 'badge-blue',
  SURGICAL: 'badge-red',
  DIAGNOSTIC: 'badge-purple',
  EQUIPMENT: 'badge-gray',
  CONSUMABLE: 'badge-amber',
  VACCINE: 'badge-green',
}

export default function ProductsPage() {
  const user = useAuthStore(s => s.user)
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('ALL')
  const [page, setPage] = useState(0)

  const { data, isLoading } = useQuery({
    queryKey: ['products', category, page],
    queryFn: () => productsApi.list({
      category: category === 'ALL' ? undefined : category,
      page,
      size: 12,
    }),
  })

  const { data: searchResults, isLoading: searching } = useQuery({
    queryKey: ['products', 'search', search],
    queryFn: () => productsApi.search(search),
    enabled: search.length > 2,
  })

  const products = search.length > 2 ? searchResults?.content : data?.content

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Product Catalog</h1>
          <p className="text-gray-500 text-sm mt-1">
            {data?.totalElements ?? 0} products available
          </p>
        </div>
        {(user?.role === 'ADMIN' || user?.role === 'DISTRIBUTOR') && (
          <button className="btn-primary">+ Add Product</button>
        )}
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            placeholder="Search products by name…"
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="form-input pl-9"
          />
        </div>
        <div className="flex items-center gap-2 overflow-x-auto pb-1">
          <Filter className="w-4 h-4 text-gray-400 flex-shrink-0" />
          {categories.map(cat => (
            <button
              key={cat}
              onClick={() => { setCategory(cat); setPage(0) }}
              className={`px-3 py-1.5 rounded-full text-xs font-medium whitespace-nowrap transition-colors ${
                category === cat
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Products grid */}
      {isLoading || searching ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {[...Array(8)].map((_, i) => (
            <div key={i} className="card animate-pulse">
              <div className="h-4 bg-gray-200 rounded w-3/4 mb-3" />
              <div className="h-3 bg-gray-100 rounded w-1/2 mb-2" />
              <div className="h-3 bg-gray-100 rounded w-2/3" />
            </div>
          ))}
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {products?.map((product: Product) => (
              <div key={product.id} className="card hover:shadow-md transition-shadow cursor-pointer">
                <div className="flex items-start justify-between mb-3">
                  <div className="w-10 h-10 bg-blue-50 rounded-lg flex items-center justify-center">
                    <Package className="w-5 h-5 text-blue-600" />
                  </div>
                  <span className={categoryBadge[product.category] ?? 'badge-gray'}>
                    {product.category}
                  </span>
                </div>

                <h3 className="font-semibold text-gray-900 text-sm leading-snug mb-1">
                  {product.name}
                </h3>
                {product.genericName && (
                  <p className="text-xs text-gray-500 mb-1">{product.genericName}</p>
                )}
                <p className="text-xs text-gray-400 mb-3">{product.manufacturer}</p>

                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-xs text-gray-400">Wholesale</p>
                    <p className="font-bold text-gray-900">₹{product.wholesalePrice}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-xs text-gray-400">MRP</p>
                    <p className="text-sm text-gray-500">₹{product.mrp}</p>
                  </div>
                </div>

                {product.prescriptionRequired && (
                  <p className="text-xs text-red-500 mt-2 font-medium">℞ Prescription required</p>
                )}
              </div>
            ))}
          </div>

          {/* Pagination */}
          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={data.first}
                className="btn-secondary px-4 py-2 text-sm disabled:opacity-40"
              >
                Previous
              </button>
              <span className="text-sm text-gray-600">
                Page {data.number + 1} of {data.totalPages}
              </span>
              <button
                onClick={() => setPage(p => p + 1)}
                disabled={data.last}
                className="btn-secondary px-4 py-2 text-sm disabled:opacity-40"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
