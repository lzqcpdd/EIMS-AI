/** API 基础路径，开发环境走 Vite 代理，生产环境可配置完整地址 */
export const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'
