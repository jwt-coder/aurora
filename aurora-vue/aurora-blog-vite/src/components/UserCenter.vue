<template>
  <el-drawer v-model="visible" direction="rtl" :with-header="false" :before-close="handleClose">
    <span class="text font-semibold text-2xl">用户中心</span>
    <template v-if="userInfo !== ''">
      <span class="text font-medium">(该页面的信息,本网站将严格保密)</span>
      <div class="max-w-full mt-10">
        <button id="pick-avatar" @click="showCropper = true">
          <el-avatar :size="110" :src="userInfo.avatar" class="ml-40" />
        </button>
        <avatar-cropper
          v-model="showCropper"
          @uploaded="handleSuccess"
          trigger="#pick-avatar"
          :request-options="options"
          upload-url="/api/users/avatar" />
        <el-form>
          <el-form-item model="userInfo" label="昵称:" class="mt-5">
            <el-input v-model="userInfo.nickname" />
          </el-form-item>
          <el-form-item model="userInfo" label="网址:" class="mt-5">
            <el-input v-model="userInfo.website" placeholder="Please add https:// or http://" />
          </el-form-item>
          <el-form-item model="userInfo" label="描述:" class="mt-5">
            <el-input v-model="userInfo.intro" placeholder="Introduce youself" />
          </el-form-item>
          <el-form-item model="userInfo" label="邮箱:" class="mt-5">
            <el-input disabled :placeholder="userInfo.email">
              <template #append v-if="userInfo.email === null">
                <span class="text" @click="changeEmailDialogVisible">绑定</span>
              </template>
              <template #append v-else>
                <span class="text" @click="changeEmailDialogVisible">修改</span>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="订阅:">
            <el-switch
              v-model="userInfo.isSubscribe"
              :loading="loading"
              :before-change="beforeChange"
              @change="changeSubscribe"
              active-color="#0fb6d6"
              :active-value="1"
              :inactive-value="0" />
          </el-form-item>
          <button
            @click="commit"
            type="button"
            id="submit-button"
            class="mt-5 w-20 text-white p-2 rounded-lg transition transform hover:scale-105 flex float-right">
            <span class="text-center flex-grow commit">提交</span>
          </button>
        </el-form>
        <div class="collect-section mt-12 clear-both">
          <span class="text font-semibold text-lg">我的收藏</span>
          <div v-if="collectList.length > 0" class="mt-3">
            <div
              v-for="item in collectList"
              :key="item.id"
              class="collect-item"
              @click="goArticle(item.id)">
              <span class="collect-title">{{ item.articleTitle }}</span>
              <span class="collect-meta">{{ item.categoryName || '未分类' }} · {{ (item.createTime || '').slice(0, 10) }}</span>
            </div>
          </div>
          <div v-else class="mt-3 text-sm opacity-60 text">暂无收藏，去文章页点「收藏」吧</div>
        </div>
      </div>
    </template>
    <br />
    <br />
  </el-drawer>
  <el-dialog v-model="emailDialogVisible" width="30%">
    <el-form>
      <el-form-item model="userInfo" class="mt-5">
        <el-input v-model="email" placeholder="邮箱号" />
      </el-form-item>
      <el-form-item model="userInfo" class="mt-5">
        <div class="input-label">图形验证码</div>
        <div class="flex items-center">
          <el-input v-model="emailCaptcha" placeholder="图形验证码" class="flex-1 mr-2" />
          <img
            :src="captchaImage"
            @click="getCaptcha"
            class="captcha-img cursor-pointer border rounded"
            alt="验证码"
            title="点击刷新验证码"
          />
        </div>
      </el-form-item>
      <el-form-item model="userInfo" type="password" class="mt-8">
        <el-input v-model="VerificationCode" type="password" placeholder="验证码">
          <template #append>
            <button type="button" style="outline: none">
              <span class="text" @click="sendCode">{{ message }}</span>
            </button>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="bingingEmail" size="large" class="mx-auto mt-3">绑定</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script lang="ts">
import { defineComponent, toRef, ref, reactive, toRefs, getCurrentInstance, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import AvatarCropper from 'vue-avatar-cropper'
import api from '@/api/api'

export default defineComponent({
  name: 'UserCenter',
  components: { AvatarCropper },
  setup() {
    const proxy: any = getCurrentInstance()?.appContext.config.globalProperties
    const userStore = useUserStore()
    const router = useRouter()
    const reactiveData = reactive({
      message: '发送',
      emailDialogVisible: false,
      email: '' as any,
      VerificationCode: '' as any,
      emailCaptcha: '' as any,
      captchaUuid: '' as any,
      captchaImage: '' as any,
      loading: false,
      switchState: false,
      collectList: [] as any
    })
    let showCropper = ref(false)
    const handleClose = () => {
      userStore.userVisible = false
    }
    // 抽屉每次打开时拉取最新收藏列表
    watch(
      () => userStore.userVisible,
      (visible) => {
        if (visible && userStore.userInfo && userStore.userInfo !== '') {
          fetchCollectList()
        }
      }
    )
    const fetchCollectList = () => {
      api.getCollectedArticles().then(({ data }: any) => {
        if (data.flag && data.data) {
          reactiveData.collectList = data.data
        }
      })
    }
    const goArticle = (articleId: any) => {
      userStore.userVisible = false
      router.push('/articles/' + articleId)
    }
    const changeEmailDialogVisible = () => {
      reactiveData.emailDialogVisible = true
      reactiveData.emailCaptcha = ''
      reactiveData.captchaUuid = ''
      getCaptcha()
    }
    const getCaptcha = (): void => {
      api.getCaptcha().then(({ data }: any) => {
        if (data.flag) {
          reactiveData.captchaImage = data.data.image
          reactiveData.captchaUuid = data.data.uuid
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
    const bingingEmail = () => {
      let params = {
        email: reactiveData.email,
        code: reactiveData.VerificationCode
      }
      api.bindingEmail(params).then(({ data }) => {
        if (data.flag) {
          proxy.$notify({
            title: 'Success',
            message: '绑定成功',
            type: 'success'
          })
          userStore.userInfo.email = reactiveData.email
          reactiveData.emailDialogVisible = false
        }
      })
    }
    const handleSuccess = (data: any) => {
      data.response.json().then((data: any) => {
        if (data.flag) {
          userStore.userInfo.avatar = data.data
          proxy.$notify({
            title: 'Success',
            message: '上传成功',
            type: 'success'
          })
        }
      })
    }
    const changeSubscribe = () => {
      if (reactiveData.switchState) {
        let params = {
          userId: userStore.userInfo.userInfoId,
          isSubscribe: userStore.userInfo.isSubscribe
        }
        api.updateUserSubscribe(params).then(({ data }) => {
          if (data.flag) {
            proxy.$notify({
              title: 'Success',
              message: '修改成功',
              type: 'success'
            })
          }
        })
      }
    }
    const commit = () => {
      let params = {
        nickname: userStore.userInfo.nickname,
        website: userStore.userInfo.website,
        intro: userStore.userInfo.intro
      }
      api.submitUserInfo(params).then(({ data }) => {
        if (data.flag) {
          proxy.$notify({
            title: 'Success',
            message: '修改成功',
            type: 'success'
          })
        }
      })
    }
    const sendCode = (): void => {
      if (reactiveData.email.trim() == '') {
        proxy.$notify({
          title: '警告',
          message: '邮箱不能为空',
          type: 'warning'
        })
        return
      }
      if (reactiveData.emailCaptcha.trim() == '') {
        proxy.$notify({
          title: '警告',
          message: '图形验证码不能为空',
          type: 'warning'
        })
        return
      }
      // 发送邮箱验证码时携带图形验证码信息
      const params = {
        username: reactiveData.email,
        captcha: reactiveData.emailCaptcha,
        captchaUuid: reactiveData.captchaUuid
      }
      api.sendValidationCode(params).then(({ data }: any) => {
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
      }).catch(() => {
        proxy.$notify({
          title: '失败',
          message: '发送验证码失败，请重试',
          type: 'error'
        })
        // 发送失败时刷新验证码
        getCaptcha()
      })
    }
    const beforeChange = () => {
      reactiveData.switchState = true
      reactiveData.loading = true
      return new Promise((resolve, reject) => {
        if (userStore.userInfo.email === '' || userStore.userInfo.email === null) {
          reactiveData.loading = false
          proxy.$notify({
            title: 'Warning',
            message: '邮箱未绑定,尽快绑定哦',
            type: 'warning'
          })
          return reject(new Error('Error'))
        } else {
          reactiveData.loading = false
          return resolve(true)
        }
      })
    }
    return {
      userInfo: toRef(userStore.$state, 'userInfo'),
      ...toRefs(reactiveData),
      visible: toRef(userStore.$state, 'userVisible'),
      showCropper,
      handleClose,
      bingingEmail,
      changeEmailDialogVisible,
      changeSubscribe,
      handleSuccess,
      sendCode,
      getCaptcha,
      commit,
      beforeChange,
      goArticle,
      options: computed(() => {
        return {
          method: 'POST',
          headers: {
            Authorization: 'Bearer ' + userStore.token
          }
        }
      })
    }
  }
})
</script>
<style lang="scss" scoped>
#submit-button {
  outline: none;
  background: #0fb6d6;
}
.text {
  color: var(--text-normal);
  cursor: pointer;
}
#pick-avatar {
  outline: none;
}
.collect-section {
  border-top: 1px solid var(--text-faint);
  padding-top: 1rem;
}
.collect-item {
  display: flex;
  flex-direction: column;
  padding: 0.5rem 0.6rem;
  margin-bottom: 0.4rem;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: background 160ms ease;
  &:hover {
    background: var(--bg-accent-05);
    .collect-title {
      color: var(--text-accent);
    }
  }
}
.collect-title {
  font-size: 0.95rem;
  font-weight: 500;
  color: var(--text-normal);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 160ms ease;
}
.collect-meta {
  font-size: 0.78rem;
  color: var(--text-dim);
  margin-top: 0.15rem;
}
</style>
<style lang="scss">
/* Element Plus 的表单/弹窗主题已统一在 src/styles/element-plus.scss 中维护 */
</style>
