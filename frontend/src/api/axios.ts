import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { useAuthStore } from '../store/authStore'

const createApiClient = (baseURL: string): AxiosInstance => {
  const client = axios.create({ baseURL, timeout: 15_000 })

  client.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = useAuthStore.getState().accessToken
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })

  client.interceptors.response.use(
    (response: AxiosResponse) => response,
    (error) => {
      if (error.response?.status === 401) {
        useAuthStore.getState().logout()
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }
  )

  return client
}

export const userApi = createApiClient(
  import.meta.env.VITE_USER_SERVICE_URL ?? '/api/user'
)

export const productApi = createApiClient(
  import.meta.env.VITE_PRODUCT_SERVICE_URL ?? '/api/product'
)

export const orderApi = createApiClient(
  import.meta.env.VITE_ORDER_SERVICE_URL ?? '/api/order'
)
