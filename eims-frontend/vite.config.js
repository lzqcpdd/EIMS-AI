import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const proxyTarget = env.VITE_PROXY_TARGET || 'http://localhost:8080'
  const apiProxy = {
    '/api': {
      target: proxyTarget,
      changeOrigin: true
    }
  }

  return {
    plugins: [vue()],
    resolve: {
      alias: { '@': resolve(__dirname, 'src') }
    },
    server: {
      port: Number(env.VITE_DEV_PORT) || 5173,
      proxy: apiProxy
    },
    preview: {
      port: Number(env.VITE_DEV_PORT) || 5173,
      proxy: apiProxy
    }
  }
})
