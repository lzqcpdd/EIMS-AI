import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { API_BASE } from '@/utils/env'

const request = axios.create({
  baseURL: API_BASE,
  timeout: 30000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(data)
    }
    return data
  },
  err => {
    const status = err.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('menus')
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
      ElMessage.error(err.response?.data?.message || '登录已过期，请重新登录')
      return Promise.reject(err)
    }
    if (status === 403) {
      ElMessage.error(err.response?.data?.message || '无权限执行此操作')
      return Promise.reject(err)
    }
    if (!err.response) {
      ElMessage.error('无法连接后端服务，请确认后端已启动后刷新页面')
      return Promise.reject(err)
    }
    ElMessage.error(err.response?.data?.message || '请求失败')
    return Promise.reject(err)
  }
)

export default request
