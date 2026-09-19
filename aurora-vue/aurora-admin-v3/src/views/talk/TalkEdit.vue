<template>
  <AppPage>
    <n-card :title="isNewTalk ? '发布说说' : '编辑说说'" rounded-10>
      <n-space vertical size="large">
        <!-- 编辑器 -->
        <div class="editor-wrapper">
          <n-input
            v-model:value="talk.content"
            type="textarea"
            placeholder="说点什么吧"
            :autosize="{ minRows: 6, maxRows: 15 }"
            @input="handleContentChange"
          />
        </div>

        <!-- 操作栏 -->
        <div class="operation-wrapper">
          <div class="left-wrapper">
            <n-upload
              :action="uploadAction"
              :headers="uploadHeaders"
              :max="10"
              :show-file-list="false"
              accept="image/*"
              multiple
              @before-upload="handleBeforeUpload"
              @finish="handleUploadFinish"
            >
              <n-button>
                <template #icon><n-icon><ImageOutline /></n-icon></template>
                上传图片
              </n-button>
            </n-upload>
          </div>

          <div class="right-wrapper">
            <n-switch
              v-model:value="isTop"
              :checked-value="1"
              :unchecked-value="0"
              style="margin-right: 16px"
            >
              <template #checked>置顶</template>
              <template #unchecked>置顶</template>
            </n-switch>

            <n-dropdown trigger="click" :options="statusOptions" @select="handleStatusSelect" style="margin-right: 16px">
              <n-button text>
                {{ statusText }}
                <template #icon><n-icon><CaretDownOutline /></n-icon></template>
              </n-button>
            </n-dropdown>

            <n-button type="primary" @click="handleSave" :disabled="!talk.content.trim()">
              发布
            </n-button>
          </div>
        </div>

        <!-- 图片列表 -->
        <div v-if="uploads.length > 0" class="image-list">
          <n-upload
            :action="uploadAction"
            :headers="uploadHeaders"
            v-model:file-list="uploads"
            :max="10"
            list-type="image-card"
            accept="image/*"
            multiple
            @before-upload="handleBeforeUpload"
            @finish="handleUploadFinish"
            @remove="handleRemove"
          />
        </div>
      </n-space>
    </n-card>
  </AppPage>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NButton, NIcon, NSwitch, NDropdown, NUpload, useMessage } from 'naive-ui'
import { ImageOutline, CaretDownOutline } from '@vicons/ionicons5'
import { getTalkDetailApi, saveTalkApi } from '@/api/talk'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const talk = reactive({
  id: null,
  content: '',
  isTop: 0,
  status: 1,
  images: ''
})

const isTop = ref(0)
const uploads = ref([])
const statusText = ref('公开')

const statusOptions = [
  { label: '公开', value: 1 },
  { label: '私密', value: 2 }
]

const uploadAction = '/api/admin/talks/images'
// 上传请求头（computed 动态读取，避免 token 变化后 headers 过期）
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + (sessionStorage.getItem('token') || '')
}))

// 是否是新建模式
const isNewTalk = computed(() => {
  return route.params.id === 'new' || !route.params.id
})

// 获取说说详情
function fetchTalkDetail() {
  const talkId = route.params.id

  // 如果是新建模式，跳过详情获取
  if (talkId === 'new' || !talkId) {
    return
  }

  getTalkDetailApi(talkId).then(res => {
    Object.assign(talk, res.data)
    isTop.value = talk.isTop
    statusText.value = talk.status === 1 ? '公开' : '私密'

    // 处理图片列表（后端 images 为 JSON 字符串，容错解析）
    let imgUrls = []
    try {
      const parsed = res.data.images ? JSON.parse(res.data.images) : []
      if (Array.isArray(parsed)) imgUrls = parsed
    } catch (e) {
      imgUrls = []
    }
    if (imgUrls.length > 0) {
      uploads.value = imgUrls.map(url => ({
        id: url,
        name: url.split('/').pop(),
        status: 'finished',
        url: url
      }))
    }
  }).catch(err => {
    console.error('获取说说详情失败:', err)
    message.error('获取说说详情失败')
  })
}

function handleContentChange() {
  // 内容变化时的处理
}

function handleBeforeUpload(data) {
  const file = data.file
  const isImage = file.type?.startsWith('image/')
  const isLt5M = file.file?.size / 1024 / 1024 < 5

  if (!isImage) {
    message.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    message.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

function handleUploadFinish({ file, event }) {
  try {
    const response = JSON.parse(event.target.response)
    if (response.flag) {
      const entry = {
        id: file.id,
        name: file.name,
        status: 'finished',
        url: response.data
      }
      // v-model:file-list 下组件已把 file 加入列表，原地替换；顶部按钮上传则 push
      const index = uploads.value.findIndex(item => item.id === file.id)
      if (index > -1) {
        uploads.value.splice(index, 1, entry)
      } else {
        uploads.value.push(entry)
      }
      message.success('上传成功')
    } else if (response.code === 401 || response.code === 40001 || event.target.status === 401) {
      message.error('登录已过期，请重新登录')
    } else {
      message.error(response.message || '上传失败')
    }
  } catch (err) {
    console.error('上传失败:', err)
    message.error('上传失败')
  }
}

function handleRemove(file) {
  const index = uploads.value.findIndex(item => item.id === file.id || item.url === file.url)
  if (index > -1) {
    uploads.value.splice(index, 1)
  }
}

function handleStatusSelect(value) {
  talk.status = value
  statusText.value = value === 1 ? '公开' : '私密'
}

function handleSave() {
  if (!talk.content.trim()) {
    message.error('说说内容不能为空')
    return
  }

  // 处理图片
  if (uploads.value.length > 0) {
    const imgUrls = uploads.value.map(item => item.url)
    talk.images = JSON.stringify(imgUrls)
  } else {
    talk.images = ''
  }

  talk.isTop = isTop.value

  saveTalkApi(talk).then(res => {
    message.success(isNewTalk.value ? '发布成功' : '保存成功')
    router.push('/talks')
  }).catch(err => {
    console.error('保存失败:', err)
    message.error('保存失败')
  })
}

onMounted(() => {
  fetchTalkDetail()
})
</script>

<style scoped>
.editor-wrapper {
  margin-top: 20px;
}

.operation-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.left-wrapper {
  display: flex;
  gap: 12px;
}

.right-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.image-list {
  margin-top: 8px;
}
</style>
