<template>
  <div class="header-controls absolute top-10 right-0 flex flex-row" @keydown.k="handleOpenModel" tabindex="0">
    <span class="ob-drop-shadow" data-dia="search" @click="handleOpenModel">
      <svg-icon icon-class="search" />
    </span>
    <Dropdown v-if="multiLanguage === 1" @command="handleClick">
      <span class="ob-drop-shadow" data-dia="language">
        <svg-icon icon-class="globe" />
        <span v-if="$i18n.locale == 'cn'">中文</span>
        <span v-if="$i18n.locale == 'en'">EN</span>
      </span>
      <DropdownMenu>
        <DropdownItem name="en">English</DropdownItem>
        <DropdownItem name="cn">中文</DropdownItem>
      </DropdownMenu>
    </Dropdown>
    <template v-if="userInfo === ''">
      <span class="mr-3" @click="openLoginDialog">{{ t('settings.login') }}</span>
    </template>
    <template v-if="userInfo !== ''">
      <Dropdown hover>
        <span class="mr-2">
          <div class="flex-shrink-0">
            <div class="rounded-full ring-gray-100 overflow-hidden shaodw-lg w-9">
              <img class="avatar-img" :src="userInfo.avatar" alt="" />
            </div>
          </div>
        </span>
        <DropdownMenu>
          <template v-if="!isMobile">
            <DropdownItem @click="openUserCenter">{{ t('settings.personal-center') }}</DropdownItem>
          </template>
          <DropdownItem @click="logout">{{ t('settings.logout') }}</DropdownItem>
        </DropdownMenu>
      </Dropdown>
    </template>
    <span no-hover-effect class="ob-drop-shadow" data-dia="light-switch">
      <ThemeToggle />
    </span>
  </div>
  <el-dialog v-model="loginDialogVisible" width="30%" :fullscreen="isMobile">
    <div class="dialog-title">{{ t('auth.login-title') }}</div>
    <el-form @keyup.enter.native="login">
      <el-form-item model="userInfo">
        <div class="input-label">{{ t('auth.email') }}</div>
        <el-input v-model="loginInfo.username" :placeholder="t('auth.email-ph')" />
      </el-form-item>
      <el-form-item model="userInfo" type="password">
        <div class="input-label">{{ t('auth.password') }}</div>
        <el-input v-model="loginInfo.password" type="password" show-password :placeholder="t('auth.password-ph')" />
      </el-form-item>
      <el-form-item class="mt-6">
        <el-button type="primary" @click="login" size="large" class="mx-auto">{{ t('auth.login-btn') }}</el-button>
      </el-form-item>
      <el-form-item v-if="qqLoginEnabled === 1" class="mt-3">
        <el-button @click="qqLogin" size="large" class="mx-auto qq-login-btn">
          <svg-icon icon-class="qq" style="margin-right: 8px;" />
          {{ t('auth.qq-login') }}
        </el-button>
      </el-form-item>
      <div class="mt-5 flex justify-between items-center">
        <span class="text" @click="openRegisterDialog">{{ t('auth.register-now') }}</span>
        <span class="text" @click="openForgetPasswordDialog">{{ t('auth.forget-password') }}</span>
      </div>
    </el-form>
  </el-dialog>
  <el-dialog v-model="registerDialogVisible" width="30%" :fullscreen="isMobile">
    <div class="dialog-title">{{ t('auth.register-title') }}</div>
    <el-form>
      <el-form-item model="userInfo">
        <div class="input-label">{{ t('auth.email') }}</div>
        <el-input v-model="loginInfo.username" :placeholder="t('auth.email-ph')" />
      </el-form-item>
      <el-form-item model="userInfo">
        <div class="input-label">{{ t('auth.captcha') }}</div>
        <div class="flex items-center">
          <el-input v-model="loginInfo.captcha" :placeholder="t('auth.captcha-ph')" class="flex-1 mr-2" />
          <img
            :src="captchaImage"
            @click="getCaptcha"
            class="captcha-img cursor-pointer border rounded"
            alt="验证码"
            title="点击刷新验证码"
          />
        </div>
      </el-form-item>
      <el-form-item model="userInfo">
        <div class="input-label">{{ t('auth.email-code') }}</div>
        <el-input v-model="loginInfo.code" :placeholder="t('auth.email-code-ph')">
          <template #append>
            <span class="text" @click="sendCode">{{ t('auth.send') }}</span>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item model="userInfo" type="password">
        <div class="input-label">{{ t('auth.password') }}</div>
        <el-input v-model="loginInfo.password" type="password" show-password :placeholder="t('auth.password-ph')" />
      </el-form-item>
      <el-form-item class="mt-6">
        <el-button type="primary" @click="register" size="large" class="mx-auto">{{ t('auth.register-btn') }}</el-button>
      </el-form-item>
      <div class="mt-5">
        <span class="text" @click="returnLoginDialog">{{ t('auth.has-account') }}</span>
      </div>
    </el-form>
  </el-dialog>
  <el-dialog v-model="forgetPasswordDialogVisible" width="30%" :fullscreen="isMobile">
    <div class="dialog-title">{{ t('auth.reset-title') }}</div>
    <el-form>
      <el-form-item model="userInfo">
        <div class="input-label">{{ t('auth.email') }}</div>
        <el-input v-model="loginInfo.username" :placeholder="t('auth.email-ph')" />
      </el-form-item>
      <el-form-item model="userInfo">
        <div class="input-label">{{ t('auth.email-code') }}</div>
        <el-input v-model="loginInfo.code" :placeholder="t('auth.email-code-ph')">
          <template #append>
            <span class="text" @click="sendCode">{{ t('auth.send') }}</span>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item model="userInfo" type="password">
        <div class="input-label">{{ t('auth.new-password') }}</div>
        <el-input v-model="loginInfo.password" type="password" show-password :placeholder="t('auth.new-password-ph')" />
      </el-form-item>
      <el-form-item class="mt-6">
        <el-button type="primary" @click="updatePassword" size="large" class="mx-auto">{{ t('auth.confirm-btn') }}</el-button>
      </el-form-item>
      <div class="mt-5">
        <span class="text" @click="returnLoginDialog">{{ t('auth.back-login') }}</span>
      </div>
    </el-form>
  </el-dialog>
  <el-dialog v-model="articlePasswordDialogVisible" width="30%" :fullscreen="isMobile">
    <el-form @submit.native.prevent @keyup.enter.native="accessArticle">
      <el-form-item model="userInfo" class="mt-5">
        <el-input id="article-password-input" v-model="articlePassword" :placeholder="t('auth.article-password-ph')" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="accessArticle" size="large" class="mx-auto mt-3">{{ t('auth.verify-btn') }}</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
  <teleport to="body">
    <SearchModel />
  </teleport>
</template>

<script lang="ts">
import { computed, defineComponent, toRef, toRefs, reactive, getCurrentInstance, nextTick } from 'vue'
import { Dropdown, DropdownMenu, DropdownItem } from '@/components/Dropdown'
import { useAppStore } from '@/stores/app'
import { useCommonStore } from '@/stores/common'
import { useUserStore } from '@/stores/user'
import { useRoute, useRouter } from 'vue-router'
import ThemeToggle from '@/components/ToggleSwitch/ThemeToggle.vue'
import api from '@/api/api'
import SearchModel from '@/components/SearchModel.vue'
import { useSearchStore } from '@/stores/search'
import config from '@/config/config'
import { useI18n } from 'vue-i18n'
import emitter from '@/utils/mitt'

export default defineComponent({
  name: 'Controls',
  components: {
    Dropdown,
    DropdownMenu,
    DropdownItem,
    ThemeToggle,
    SearchModel
  },
  setup() {
    const { t } = useI18n()
    const proxy: any = getCurrentInstance()?.appContext.config.globalProperties
    const appStore = useAppStore()
    const commonStore = useCommonStore()
    const userStore = useUserStore()
    const searchStore = useSearchStore()
    const route = useRoute()
    const router = useRouter()
    const loginInfo = reactive({
      username: '' as any,
      password: '' as any,
      code: '' as any,
      captcha: '' as any,
      captchaUuid: '' as any
    })
    const reactiveDate = reactive({
      loginDialogVisible: false,
      registerDialogVisible: false,
      forgetPasswordDialogVisible: false,
      articlePasswordDialogVisible: false,
      articlePassword: '',
      articleId: '',
      captchaImage: '' as any
    })
    emitter.on('changeArticlePasswordDialogVisible', (articleId: any) => {
      reactiveDate.articlePasswordDialogVisible = true
      reactiveDate.articlePassword = ''
      reactiveDate.articleId = articleId
      nextTick(() => {
        document.getElementById('article-password-input')?.focus()
      })
    })
    const handleClick = (name: string): void => {
      appStore.changeLocale(name)
    }
    
    // 动态加载QQ SDK
    const loadQQSDK = (): Promise<void> => {
      return new Promise((resolve, reject) => {
        // 检查是否已加载
        //@ts-ignore
        if (window.QC) {
          resolve()
          return
        }
        
        // 创建script标签
        const script = document.createElement('script')
        script.src = 'https://connect.qq.com/qc_jssdk.js'
        script.setAttribute('data-appid', config.qqLogin.QQ_APP_ID)
        script.setAttribute('data-redirecturi', config.qqLogin.QQ_REDIRECT_URI)
        script.onload = () => resolve()
        script.onerror = () => reject(new Error('QQ SDK加载失败'))
        document.head.appendChild(script)
      })
    }
    
    const login = () => {
      if (loginInfo.username.trim().length == 0 || loginInfo.password.trim().length == 0) {
        proxy.$notify({
          title: 'Warning',
          message: '账号或者密码不能为空',
          type: 'warning'
        })
        return
      }
      let params = new URLSearchParams()
      params.append('username', loginInfo.username)
      params.append('password', loginInfo.password)
      api.login(params).then(({ data }) => {
        if (data.flag) {
          // 设置用户信息到store
          userStore.userInfo = data.data
          userStore.token = data.data.token
          sessionStorage.setItem('token', data.data.token)

          // 显示成功通知
          proxy.$notify({
            title: 'Success',
            message: '登录成功',
            type: 'success'
          })

          // 关闭登录弹窗
          reactiveDate.loginDialogVisible = false

          // 清除密码输入
          loginInfo.password = ''
        } else {
          // 登录失败显示错误信息
          proxy.$notify({
            title: 'Error',
            message: data.message || '登录失败',
            type: 'error'
          })
        }
      }).catch((error) => {
        console.error('登录请求失败:', error)
        proxy.$notify({
          title: 'Error',
          message: error.response?.data?.message || '登录请求失败，请检查网络',
          type: 'error'
        })
      })
    }
    const logout = () => {
      api.logout().then(({ data }) => {
        if (data.flag) {
          userStore.userInfo = ''
          userStore.token = ''
          userStore.accessArticles = []
          sessionStorage.removeItem('token')
          proxy.$notify({
            title: 'Success',
            message: '登出成功',
            type: 'success'
          })
        }
      })
    }
    const openUserCenter = () => {
      userStore.userVisible = true
    }
    const openLoginDialog = () => {
      reactiveDate.loginDialogVisible = true
    }
    const openRegisterDialog = () => {
      loginInfo.code = ''
      loginInfo.captcha = ''
      loginInfo.captchaUuid = ''
      reactiveDate.loginDialogVisible = false
      reactiveDate.registerDialogVisible = true
      // 打开注册对话框时自动获取验证码
      getCaptcha()
    }
    const getCaptcha = (): void => {
      api.getCaptcha().then(({ data }: any) => {
        if (data.flag) {
          reactiveDate.captchaImage = data.data.image
          loginInfo.captchaUuid = data.data.uuid
        } else {
          proxy.$notify({
            title: '失败',
            message: '获取验证码失败',
            type: 'error'
          })
        }
      }).catch(() => {
        proxy.$notify({
          title: '失败',
          message: '获取验证码失败',
          type: 'error'
        })
      })
    }
    const sendCode = (): void => {
      console.log('发送验证码 - 当前数据:', {
        username: loginInfo.username,
        captcha: loginInfo.captcha,
        captchaUuid: loginInfo.captchaUuid
      })
      
      if (loginInfo.username.trim() == '') {
        proxy.$notify({
          title: '警告',
          message: '邮箱不能为空',
          type: 'warning'
        })
        return
      }
      
      if (loginInfo.captcha.trim() == '') {
        proxy.$notify({
          title: '警告',
          message: '图形验证码不能为空',
          type: 'warning'
        })
        return
      }
      
      // 发送邮箱验证码时携带图形验证码信息
      const params = {
        username: loginInfo.username,
        captcha: loginInfo.captcha,
        captchaUuid: loginInfo.captchaUuid
      }
      
      console.log('发送验证码请求参数:', params)
      
      api.sendValidationCode(params).then(({ data }: any) => {
        console.log('发送验证码响应:', data)
        if (data.flag) {
          proxy.$notify({
            title: '成功',
            message: data.message,
            type: 'success'
          })
        } else {
          proxy.$notify({
            title: '失败',
            message: data.message,
            type: 'error'
          })
          // 发送失败时刷新验证码
          getCaptcha()
        }
      }).catch((error) => {
        console.error('发送验证码错误:', error)
        proxy.$notify({
          title: '失败',
          message: error.response?.data?.message || '发送验证码失败，请重试',
          type: 'error'
        })
        // 发送失败时刷新验证码
        getCaptcha()
      })
    }
    const register = (): void => {
      console.log('开始注册 - 当前数据:', {
        username: loginInfo.username,
        password: loginInfo.password,
        code: loginInfo.code,
        captcha: loginInfo.captcha,
        captchaUuid: loginInfo.captchaUuid
      })
      
      if (loginInfo.username.trim() == '') {
        proxy.$notify({
          title: '警告',
          message: '邮箱不能为空',
          type: 'warning'
        })
        return
      }
      if (loginInfo.password.trim() == '') {
        proxy.$notify({
          title: '警告',
          message: '密码不能为空',
          type: 'warning'
        })
        return
      }
      if (loginInfo.code.trim() == '') {
        proxy.$notify({
          title: '警告',
          message: '邮箱验证码不能为空',
          type: 'warning'
        })
        return
      }
      if (loginInfo.captcha.trim() == '') {
        proxy.$notify({
          title: '警告',
          message: '图形验证码不能为空',
          type: 'warning'
        })
        return
      }
      console.log('发送注册请求，参数:', loginInfo)
      
      api.register(loginInfo).then(({ data }: any) => {
        console.log('注册响应:', data)
        if (data.flag) {
          proxy.$notify({
            title: '成功',
            message: '注册成功',
            type: 'success'
          })
          reactiveDate.registerDialogVisible = false
          reactiveDate.loginDialogVisible = true
        } else {
          proxy.$notify({
            title: '失败',
            message: data.message,
            type: 'error'
          })
          // 注册失败时刷新验证码
          getCaptcha()
        }
      }).catch((error) => {
        console.error('注册请求错误:', error)
        proxy.$notify({
          title: '失败',
          message: error.response?.data?.message || '注册失败，请重试',
          type: 'error'
        })
        // 注册失败时刷新验证码
        getCaptcha()
      })
    }
    const returnLoginDialog = (): void => {
      reactiveDate.registerDialogVisible = false
      reactiveDate.forgetPasswordDialogVisible = false
      reactiveDate.loginDialogVisible = true
    }
    const openForgetPasswordDialog = (): void => {
      reactiveDate.loginDialogVisible = false
      reactiveDate.forgetPasswordDialogVisible = true
    }
    const handleOpenModel: any = (status: boolean) => {
      searchStore.setOpenModal(status)
    }

    const qqLogin = async () => {
      try {
        // 先加载QQ SDK
        await loadQQSDK()
        
        userStore.currentUrl = route.path
        reactiveDate.loginDialogVisible = false
        
        if (commonStore.isMobile) {
          //@ts-ignore
          QC.Login.showPopup({
            appId: config.qqLogin.QQ_APP_ID,
            redirectURI: config.qqLogin.QQ_REDIRECT_URI
          })
        } else {
          window.open(
            'https://graph.qq.com/oauth2.0/show?which=Login&display=pc&client_id=' +
              config.qqLogin.QQ_APP_ID +
              '&response_type=token&scope=all&redirect_uri=' +
              config.qqLogin.QQ_REDIRECT_URI,
            '_self'
          )
        }
      } catch (error) {
        proxy.$notify({
          title: '错误',
          message: 'QQ登录功能加载失败',
          type: 'error'
        })
      }
    }
    const updatePassword = () => {
      api.updatePassword(loginInfo).then(({ data }) => {
        if (data.flag) {
          proxy.$notify({
            title: 'Success',
            message: '修改成功',
            type: 'success'
          })
          reactiveDate.forgetPasswordDialogVisible = false
          reactiveDate.loginDialogVisible = true
        }
      })
    }
    const accessArticle = () => {
      if (reactiveDate.articlePassword.trim().length == 0) {
        proxy.$notify({
          title: 'Warning',
          message: '密码不能为空',
          type: 'warning'
        })
        return
      }
      api
        .accessArticle({
          articleId: reactiveDate.articleId,
          articlePassword: reactiveDate.articlePassword
        })
        .then(({ data }) => {
          if (data.flag) {
            reactiveDate.articlePasswordDialogVisible = false
            userStore.accessArticles.push(reactiveDate.articleId)
            router.push({ path: '/articles/' + reactiveDate.articleId })
          }
        })
    }
    return {
      handleOpenModel,
      loginInfo,
      ...toRefs(reactiveDate),
      userInfo: toRef(userStore.$state, 'userInfo'),
      isMobile: toRef(commonStore.$state, 'isMobile'),
      login,
      qqLogin,
      logout,
      handleClick,
      openUserCenter,
      openLoginDialog,
      openRegisterDialog,
      returnLoginDialog,
      getCaptcha,
      sendCode,
      register,
      updatePassword,
      openForgetPasswordDialog,
      accessArticle,
      multiLanguage: computed(() => {
        let websiteConfig: any = appStore.websiteConfig
        return websiteConfig.multiLanguage
      }),
      qqLoginEnabled: computed(() => {
        let websiteConfig: any = appStore.websiteConfig
        return websiteConfig.qqLogin
      }),
      t
    }
  }
})
</script>
<style lang="scss">
.my-el-button {
  width: 300px !important;
}

/* 弹窗基础：跟随博客主题变量，自动适配深色模式 */
.el-dialog {
  border-radius: 12px !important;
  background: var(--background-secondary) !important;
  box-shadow: var(--accent-shadow) !important;
  overflow: hidden !important;
}

.el-dialog__header {
  padding: 0 !important;
  margin: 0 !important;
}

.el-dialog__body {
  padding: 28px !important;
}

.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-title-h2);
  margin-bottom: 20px;
}

.el-dialog__headerbtn {
  outline: none !important;
  top: 16px !important;
  right: 16px !important;
  color: var(--text-dim) !important;
  transition: color 0.2s ease !important;
}

.el-dialog__headerbtn:hover {
  color: var(--text-normal) !important;
}

.el-button {
  width: 100% !important;
  height: 42px !important;
  border-radius: 8px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
}

.el-button--primary {
  background: var(--text-accent) !important;
  color: #fff !important;
  border: none !important;
}

.el-button--primary:hover {
  opacity: 0.85;
}

.el-form-item {
  margin-bottom: 20px !important;
}

.el-form-item:last-of-type {
  margin-bottom: 0 !important;
}

.input-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-dim);
  margin-bottom: 6px;
}

.el-input-group__append {
  background: transparent !important;
  cursor: pointer !important;
  box-shadow: none !important;
  padding: 0 14px !important;
}

.el-input-group__append:hover {
  background: var(--bg-accent-05) !important;
}

.el-input-group__append .text {
  color: var(--text-accent) !important;
  font-weight: 500 !important;
}

.el-form-item__label {
  text-align: left;
  width: 70px;
  color: var(--text-normal) !important;
}

.el-input__inner {
  color: var(--text-normal) !important;
  height: 40px !important;
  line-height: 40px !important;
}

.el-input__wrapper {
  background: var(--background-primary) !important;
  border-radius: 8px !important;
  box-shadow: 0 0 0 1px var(--background-trans) inset !important;
  transition: box-shadow 0.2s ease !important;
  padding: 1px 14px !important;
}

.el-input__wrapper:hover {
  box-shadow: 0 0 0 1px var(--text-dim) inset !important;
}

.el-input.is-focus .el-input__wrapper {
  box-shadow: 0 0 0 1px var(--text-accent) inset, 0 0 0 3px var(--bg-accent-05) !important;
}
</style>
<style lang="scss" scoped>
.text {
  color: var(--text-accent);
  cursor: pointer;
  font-size: 14px;
  transition: opacity 0.2s ease;
}

.text:hover {
  opacity: 0.7;
}

#submit-button {
  outline: none;
  background: var(--text-accent);
}
.header-controls {
  span {
    display: flex;
    justify-content: center;
    align-items: center;
    color: #fff;
    cursor: pointer;
    transition: opacity 250ms ease;
    padding-right: 0.5rem;
    &[no-hover-effect] {
      &:hover {
        opacity: 1;
      }
    }
    &:hover {
      opacity: 0.5;
    }
    .svg-icon {
      stroke: #fff;
      height: 2rem;
      width: 2rem;
      margin-right: 0.5rem;
      pointer-events: none;
    }
  }
  .search-bar {
    @apply bg-transparent flex flex-row px-0 mr-2 rounded-full;
    opacity: 0;
    width: 0;
    transition: 300ms all ease-out;
    &.active {
      @apply bg-ob-deep-800;
      opacity: 0.95;
      width: 200px;
      imput {
        width: initial;
      }
    }
    &:focus {
      appearance: none;
      outline: none;
    }
    input {
      @apply flex flex-1 bg-transparent text-ob-normal px-6 box-border;
      width: 0;
      appearance: none;
      outline: none;
    }
    svg {
      @apply float-right;
    }
  }
}
.avatar-img {
  transition-property: transform;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 800ms;
  transform: rotate(-360deg);
}
.avatar-img:hover {
  transform: rotate(360deg);
}
.captcha-img {
  width: 110px;
  height: 40px;
  border: 1px solid var(--background-trans);
  border-radius: 8px;
  transition: border-color 0.2s ease;
  cursor: pointer;
  object-fit: cover;
}

.captcha-img:hover {
  border-color: var(--text-accent);
}

.qq-login-btn {
  background: #12b7f5 !important;
  color: #fff !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.qq-login-btn:hover {
  background: #00a1e0 !important;
}
</style>
