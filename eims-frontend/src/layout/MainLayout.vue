<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">
        <el-icon><Monitor /></el-icon>
        <span>EIMS 管理平台</span>
      </div>
      <el-menu :default-active="route.path" router background-color="#0f172a" text-color="#94a3b8" active-text-color="#fff">
        <template v-for="menu in store.menus" :key="menu.id">
          <el-sub-menu v-if="menu.children?.length" :index="menu.path || String(menu.id)">
            <template #title>
              <el-icon v-if="resolveIcon(menu.icon)"><component :is="resolveIcon(menu.icon)" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.path">
              <el-icon v-if="resolveIcon(child.icon)"><component :is="resolveIcon(child.icon)" /></el-icon>
              {{ child.name }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="menu.path">
            <el-icon v-if="resolveIcon(menu.icon)"><component :is="resolveIcon(menu.icon)" /></el-icon>
            {{ menu.name }}
          </el-menu-item>
        </template>
      </el-menu>
    </aside>
    <div class="main">
      <header class="header">
        <div class="header-left">
          <h2>{{ currentTitle }}</h2>
        </div>
        <div class="header-right">
          <el-tag type="info">{{ store.user?.nickName || store.user?.userName }}</el-tag>
          <el-button type="danger" link @click="handleLogout">退出</el-button>
        </div>
      </header>
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const titleMap = {
  '/dashboard': '工作台',
  '/base/supplier': '供应商管理',
  '/wms/scan': '扫码出入库'
}

const currentTitle = computed(() => {
  for (const menu of store.menus) {
    if (menu.path === route.path) return menu.name
    for (const child of menu.children || []) {
      if (child.path === route.path) return child.name
    }
  }
  return titleMap[route.path] || 'EIMS 工作台'
})

const iconAlias = {
  Sell: 'ShoppingCartFull',
  Connection: 'Link',
  Opportunity: 'TrendCharts',
  Service: 'Headset'
}

function resolveIcon(name) {
  if (!name) return null
  return iconAlias[name] || name
}

function handleLogout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { display: flex; height: 100vh; background: var(--eims-bg); }
.sidebar { width: 240px; background: var(--eims-sidebar); display: flex; flex-direction: column; }
.logo { height: 60px; display: flex; align-items: center; gap: 10px; padding: 0 20px; color: #fff; font-size: 16px; font-weight: 600; border-bottom: 1px solid #1e293b; }
.sidebar .el-menu { border-right: none; flex: 1; overflow-y: auto; }
.main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.header { height: 60px; background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; box-shadow: 0 1px 3px rgba(0,0,0,.06); }
.header-right { display: flex; align-items: center; gap: 16px; }
.content { flex: 1; padding: 20px; overflow-y: auto; }
</style>
