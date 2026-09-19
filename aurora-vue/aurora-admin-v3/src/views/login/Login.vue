<template>
  <div class="login-container" :style="{ backgroundImage: backgroundImage ? `url(${backgroundImage})` : '' }">
    <!-- 左侧品牌区域 -->
    <div class="brand-section" :style="{ backgroundImage: backgroundImage ? `url(${backgroundImage})` : '' }">
      <div class="brand-content">
        <h1 class="brand-title">{{ websiteStore.getName || 'Aurora' }}</h1>
      </div>
      <div class="brand-footer">
        <p>© 2024 Aurora Admin. All rights reserved.</p>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="form-section">
      <div class="form-wrapper">
        <div class="logo-area">
          <div class="logo-icon">
            <img v-if="websiteStore.getLogo" :src="websiteStore.getLogo" alt="logo" />
            <span v-else class="logo-text">{{ (websiteStore.getName || 'Aurora').charAt(0) }}</span>
          </div>
        </div>

        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录到您的管理后台</p>
        </div>

        <n-form ref="formRef" :model="loginForm" :rules="rules" class="login-form">
          <n-form-item path="username" :show-label="false">
            <n-input
              v-model:value="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <n-icon size="20"><PersonOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item>

          <n-form-item path="password" :show-label="false">
            <n-input
              v-model:value="loginForm.password"
              type="password"
              show-password-on="click"
              placeholder="请输入密码"
              size="large"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <n-icon size="20"><LockClosedOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item>

          <n-button
            type="primary"
            size="large"
            block
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '立即登录' }}
          </n-button>
        </n-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5'
import { useUserStore } from '@/store/modules/user'
import { useWebsiteStore } from '@/store/modules/website'
import { useMessage } from 'naive-ui'
import { request } from '@/utils/http'

const router = useRouter()
const userStore = useUserStore()
const websiteStore = useWebsiteStore()
const message = useMessage()

const formRef = ref(null)
const loading = ref(false)
const backgroundImage = ref('')

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: ['blur', 'input']
  },
  password: {
    required: true,
    message: '请输入密码',
    trigger: ['blur', 'input']
  }
}

// 登录页使用公开配置接口（/api/admin/** 在未登录时会被权限拦截）
const getSystemConfig = async () => {
  try {
    const res = await request.get('/api/config/login')
    const data = res.data || {}
    if (data.loginBackgroundImage) {
      backgroundImage.value = data.loginBackgroundImage
    }
    if (data.name) {
      websiteStore.name = data.name
    }
    if (data.logo) {
      websiteStore.logo = data.logo
    }
    if (data.favicon) {
      websiteStore.favicon = data.favicon
    }
  } catch (error) {
    console.error('获取登录页配置失败:', error)
  }
}

const handleLogin = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true

    await userStore.login(loginForm)
    message.success('登录成功')

    router.push('/')
  } catch (error) {
    message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getSystemConfig()
})
</script>

<style scoped>
/* 容器 */
.login-container {
  min-height: 100vh;
  display: flex;
  background-position: center center;
  background-size: cover;
  background-repeat: no-repeat;
  position: relative;
}

/* 默认背景 */
.login-container:not([style*="backgroundImage"]) {
  background: #f1f3f9;
}

/* 当有背景图片时，添加遮罩层（仅在手机端纵向布局时） */
.login-container[style*="backgroundImage"]::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: none;
  z-index: 0;
}

/* 背景遮罩 */
.login-container[style*="backgroundImage"]::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 0;
}

/* 左侧品牌区域 */
.brand-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px 40px;
  background: linear-gradient(130deg, #24c6dc, #5433ff 41.07%, #ff0099 76.05%);
  background-position: center center;
  background-size: cover;
  background-repeat: no-repeat;
  color: white;
  position: relative;
  z-index: 1;
}

/* 当有背景图片时，添加遮罩层 */
.brand-section[style*="backgroundImage"]::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: -1;
}

.brand-content {
  text-align: center;
  max-width: 400px;
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0;
  letter-spacing: -1px;
}

.brand-footer {
  position: absolute;
  bottom: 40px;
  left: 0;
  right: 0;
  text-align: center;
  padding: 0 40px;
}

.brand-footer p {
  font-size: 14px;
  opacity: 0.7;
  margin: 0;
}

/* 右侧表单区域 */
.form-section {
  flex: 0 0 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #ffffff;
  position: relative;
  z-index: 1;
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
}

.logo-area {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(130deg, #24c6dc, #5433ff 41.07%, #ff0099 76.05%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(233, 55, 150, 0.25);
}

.logo-icon img {
  max-width: 60px;
  max-height: 60px;
  object-fit: contain;
}

.logo-text {
  font-size: 36px;
  font-weight: 700;
  color: white;
  letter-spacing: -0.5px;
}

.form-header {
  margin-bottom: 40px;
  text-align: center;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.form-header p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* 表单样式 */
.login-form :deep(.n-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.n-input) {
  height: 50px;
}

.login-form :deep(.n-input__border) {
  border: 1.5px solid #e5e7eb !important;
  border-radius: 10px;
}

.login-form :deep(.n-input__state-border) {
  border: 1.5px solid #e5e7eb !important;
  border-radius: 10px;
}

.login-form :deep(.n-input:hover .n-input__border),
.login-form :deep(.n-input:hover .n-input__state-border) {
  border-color: #e93796 !important;
}

.login-form :deep(.n-input.n-input--focus .n-input__border),
.login-form :deep(.n-input.n-input--focus .n-input__state-border) {
  border-color: #e93796 !important;
  box-shadow: 0 0 0 3px rgba(233, 55, 150, 0.08);
}

.login-form :deep(.n-icon) {
  color: #9ca3af;
}

/* 按钮 */
.login-form :deep(.n-button) {
  height: 50px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  background: #e93796;
  border: none;
  margin-top: 8px;
  transition: all 0.2s ease;
}

.login-form :deep(.n-button:hover) {
  background: #f2539f;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(233, 55, 150, 0.25);
}

.login-form :deep(.n-button:active) {
  transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .brand-section {
    flex: 1;
  }

  .form-section {
    flex: 0 0 400px;
  }
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    justify-content: center;
    padding: 20px;
  }

  /* 手机端显示背景遮罩 */
  .login-container[style*="backgroundImage"]::before {
    display: block;
    background: rgba(0, 0, 0, 0.5);
  }

  .brand-section {
    display: none;
  }

  .form-section {
    flex: none;
    width: 100%;
    max-width: 400px;
    margin: 0 auto;
    padding: 40px 32px;
    background: rgba(255, 255, 255, 0.98) !important;
    backdrop-filter: blur(20px);
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
    position: relative;
    z-index: 1;
  }

  .logo-area {
    margin-bottom: 28px;
  }

  .logo-icon {
    width: 64px;
    height: 64px;
  }

  .logo-text {
    font-size: 28px;
  }

  .form-header {
    margin-bottom: 32px;
  }

  .form-header h2 {
    font-size: 26px;
    margin-bottom: 6px;
  }

  .form-header p {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 16px;
  }

  .form-section {
    max-width: 100%;
    padding: 32px 24px;
  }

  .logo-icon {
    width: 56px;
    height: 56px;
  }

  .logo-text {
    font-size: 24px;
  }

  .logo-area {
    margin-bottom: 24px;
  }

  .form-wrapper {
    max-width: 100%;
  }

  .form-header {
    margin-bottom: 28px;
  }

  .form-header h2 {
    font-size: 22px;
  }

  .login-form :deep(.n-input) {
    height: 48px;
  }

  .login-form :deep(.n-button) {
    height: 48px;
  }
}

@media (max-width: 360px) {
  .form-section {
    padding: 28px 20px;
  }

  .logo-icon {
    width: 48px;
    height: 48px;
  }

  .logo-text {
    font-size: 20px;
  }

  .form-header h2 {
    font-size: 20px;
  }
}
</style>
