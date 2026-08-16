<template>
  <n-config-provider :theme-overrides="naiveThemeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <router-view />
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider } from 'naive-ui'
import { useWebsiteStore } from '@/store/modules/website'

const route = useRoute()
const websiteStore = useWebsiteStore()

const naiveThemeOverrides = {
  common: {
    primaryColor: '#e93796',
    primaryColorHover: '#f2539f',
    primaryColorPressed: '#cf2d84',
    primaryColorSuppl: '#e93796',
    borderRadius: '8px',
    borderRadiusSmall: '6px'
  }
}

// 根据路由和网站配置应用标题与favicon
// watch 而非 onMounted：登录后是 SPA 内部跳转，onMounted 不会再触发，配置到位时需自动应用
const applyBranding = () => {
  const name = websiteStore.getName || '后台管理系统'
  document.title = route.path === '/login' ? `登录 - ${name}` : `${name} - 后台管理`

  const faviconUrl = websiteStore.getFavicon
  if (faviconUrl) {
    document.querySelectorAll("link[rel*='icon']").forEach(link => link.remove())
    const link = document.createElement('link')
    link.rel = 'icon'
    link.href = faviconUrl
    document.getElementsByTagName('head')[0].appendChild(link)
  }
}

watch(
  () => [route.path, websiteStore.getName, websiteStore.getFavicon],
  applyBranding,
  { immediate: true }
)

// 动态获取网站配置（登录页配置接口未授权会静默失败，登录后由侧边栏拉取，watcher 自动应用）
onMounted(async () => {
  if (route.path === '/login') return

  try {
    await websiteStore.fetchWebsiteConfig()
  } catch (error) {
    // 对于40001（未登录）错误，静默处理
    if (error.code !== 40001) {
      console.error('初始化网站配置失败:', error)
    }
  }
})
</script>

<style>
#app {
  width: 100%;
  height: 100%;
}
</style>
