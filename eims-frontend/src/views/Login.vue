<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <div class="brand-icon">E</div>
        <h1>EIMS 企业综合管理平台</h1>
        <p>Enterprise Integrated Management System</p>
      </div>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" native-type="submit">登 录</el-button>
      </el-form>
      <div class="tips">
        <p>演示账号：admin / Admin@123456</p>
        <p>采购：purchaser | 销售：sales | 仓储：warehouse</p>
      </div>
    </div>
    <div class="bg-decoration"></div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'Admin@123456' })

async function handleLogin() {
  loading.value = true
  try {
    await store.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // 错误信息已由 request 拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 50%, #2563eb 100%); position: relative; overflow: hidden; }
.login-card { width: 420px; background: rgba(255,255,255,.97); border-radius: 16px; padding: 40px; box-shadow: 0 25px 50px rgba(0,0,0,.25); z-index: 1; }
.brand { text-align: center; margin-bottom: 32px; }
.brand-icon { width: 56px; height: 56px; background: linear-gradient(135deg, #2563eb, #7c3aed); border-radius: 14px; color: #fff; font-size: 28px; font-weight: 700; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; }
.brand h1 { font-size: 22px; color: #0f172a; margin-bottom: 6px; }
.brand p { color: #64748b; font-size: 13px; }
.tips { margin-top: 20px; text-align: center; font-size: 12px; color: #94a3b8; line-height: 1.8; }
.bg-decoration { position: absolute; width: 600px; height: 600px; border-radius: 50%; background: radial-gradient(circle, rgba(37,99,235,.3) 0%, transparent 70%); top: -200px; right: -100px; }
</style>
