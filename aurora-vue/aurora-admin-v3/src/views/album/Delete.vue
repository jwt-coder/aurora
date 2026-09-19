<template>
  <AppPage>
    <n-card title="照片回收站" rounded-10>
      <n-space vertical size="large">
        <!-- 操作栏 -->
        <n-space>
          <n-button type="primary" @click="handleRestoreAll">
            <template #icon><n-icon><RefreshOutline /></n-icon></template>
            恢复全部
          </n-button>
          <n-button type="error" @click="handleClearAll">
            <template #icon><n-icon><TrashOutline /></n-icon></template>
            清空回收站
          </n-button>
          <n-button @click="fetchPhotos">
            <template #icon><n-icon><RefreshOutline /></n-icon></template>
            刷新
          </n-button>
        </n-space>

        <!-- 搜索栏 -->
        <n-form inline>
          <n-form-item label="关键词">
            <n-input v-model:value="searchForm.keywords" placeholder="搜索照片" clearable />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleSearch">搜索</n-button>
          </n-form-item>
        </n-form>

        <!-- 照片网格 -->
        <n-grid x-gap="16" y-gap="16" :cols="4" responsive="screen">
          <n-gi v-for="photo in photoList" :key="photo.id">
            <n-card class="photo-card" hoverable>
              <div class="photo-container">
                <img :src="photo.url" :alt="photo.name" class="photo-image" />
                <div class="photo-overlay">
                  <n-space vertical>
                    <n-button type="success" size="small" style="width: 80px" @click="handleRestore(photo)">
                      恢复
                    </n-button>
                    <n-button type="error" size="small" style="width: 80px" @click="handleDelete(photo)">
                      彻底删除
                    </n-button>
                  </n-space>
                </div>
              </div>
              <div class="photo-info">
                <p class="photo-name">{{ photo.name }}</p>
                <p class="photo-time">{{ formatDateTime(photo.deleteTime) }}</p>
              </div>
            </n-card>
          </n-gi>
        </n-grid>

        <!-- 分页 -->
        <n-pagination
          v-model:page="pagination.page"
          :page-count="pagination.pageCount"
          @update:page="handlePageChange"
        />
      </n-space>
    </n-card>
  </AppPage>
</template>

<script setup>
import { ref, reactive, onActivated } from 'vue'
import { NIcon, useMessage, useDialog } from 'naive-ui'
import { RefreshOutline, TrashOutline } from '@vicons/ionicons5'
import { getPlaceholder } from '@/utils/placeholder'
import { getPhotosApi, deletePhotoApi, updatePhotosDeleteApi } from '@/api/photo'
import dayjs from 'dayjs'

function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
}

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const photoList = ref([])

const searchForm = reactive({
  keywords: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 16,
  itemCount: 0,
  pageCount: 1
})

function fetchPhotos() {
  loading.value = true
  getPhotosApi({
    isDelete: 1,  // 获取回收站的照片
    current: pagination.page,
    size: pagination.pageSize
  }).then(res => {
    // 处理照片数据，映射字段名
    photoList.value = (res.data.records || []).map(photo => ({
      ...photo,
      // 兼容不同的字段名
      url: photo.photoSrc || photo.url,
      name: photo.photoName || photo.name || '',
      deleteTime: photo.deleteTime || photo.updateTime || ''
    }))
    pagination.itemCount = res.data.count || 0
    pagination.pageCount = Math.ceil(pagination.itemCount / pagination.pageSize)
    loading.value = false
  }).catch(err => {
    console.error('获取回收站照片失败:', err)
    message.error('获取回收站照片失败')
    loading.value = false
  })
}

function handleSearch() {
  pagination.page = 1
  fetchPhotos()
}

function handleRestore(photo) {
  updatePhotosDeleteApi({
    ids: [photo.id],
    isDelete: 0
  }).then(() => {
    message.success('恢复照片成功')
    // 恢复后该照片会从回收站消失，如果当前页只剩这一张且不是第一页，回退一页
    if (photoList.value.length === 1 && pagination.page > 1) pagination.page--
    fetchPhotos()
  }).catch(err => {
    console.error('恢复照片失败:', err)
    message.error('恢复照片失败')
  })
}

// 分页拉取回收站全部照片id
async function fetchAllRecyclePhotoIds() {
  const ids = []
  const pageSize = 100
  let page = 1
  let total = 0
  while (true) {
    const res = await getPhotosApi({
      isDelete: 1,  // 获取回收站的照片
      current: page,
      size: pageSize
    })
    total = res.data.count || 0
    const records = res.data.records || []
    ids.push(...records.map(photo => photo.id))
    if (records.length === 0 || ids.length >= total) break
    page++
  }
  return ids
}

// 大批量id按每批500个分批提交，避免单次请求id过多
function chunkIds(ids, size = 500) {
  const chunks = []
  for (let i = 0; i < ids.length; i += size) {
    chunks.push(ids.slice(i, i + size))
  }
  return chunks
}

function handleRestoreAll() {
  if (photoList.value.length === 0) {
    message.warning('回收站为空')
    return
  }

  dialog.warning({
    title: '确认恢复',
    content: `确定要恢复回收站中的所有照片吗？(${pagination.itemCount}张)`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        // 先拉取回收站全部照片id，再批量恢复
        const photoIds = await fetchAllRecyclePhotoIds()
        if (photoIds.length === 0) {
          message.warning('回收站为空')
          return
        }
        let done = 0
        for (const chunk of chunkIds(photoIds)) {
          await updatePhotosDeleteApi({
            ids: chunk,
            isDelete: 0
          })
          done += chunk.length
        }
        message.success(`已恢复${done}张照片`)
        // 全部恢复后回收站已清空，回到第一页
        pagination.page = 1
        fetchPhotos()
      } catch (err) {
        console.error('恢复照片失败:', err)
        // 分批提交中途失败：部分已生效，提示实际情况并刷新列表
        message.error('部分照片恢复失败，列表已刷新，请重试剩余部分')
        pagination.page = 1
        fetchPhotos()
      }
    }
  })
}

function handleDelete(photo) {
  dialog.warning({
    title: '确认删除',
    content: '确定要彻底删除这张照片吗？此操作不可恢复！',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      deletePhotoApi([photo.id]).then(() => {
        message.success(`已删除：${photo.name}`)
        // 删除后如果当前页只剩这一张且不是第一页，回退一页
        if (photoList.value.length === 1 && pagination.page > 1) pagination.page--
        fetchPhotos()
      }).catch(err => {
        console.error('删除照片失败:', err)
        message.error('删除照片失败')
      })
    }
  })
}

function handleClearAll() {
  if (photoList.value.length === 0) {
    message.warning('回收站为空')
    return
  }

  dialog.warning({
    title: '确认清空',
    content: `确定要清空回收站吗？此操作将永久删除 ${pagination.itemCount} 张照片！`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        // 先拉取回收站全部照片id，再分批彻底删除
        const photoIds = await fetchAllRecyclePhotoIds()
        if (photoIds.length === 0) {
          message.warning('回收站为空')
          return
        }
        for (const chunk of chunkIds(photoIds)) {
          await deletePhotoApi(chunk)
        }
        message.success('已清空回收站')
        // 回收站已清空，回到第一页
        pagination.page = 1
        fetchPhotos()
      } catch (err) {
        console.error('清空回收站失败:', err)
        // 分批删除中途失败：部分已生效，提示实际情况并刷新列表
        message.error('部分照片删除失败，列表已刷新，请重试剩余部分')
        pagination.page = 1
        fetchPhotos()
      }
    }
  })
}

function handlePageChange(page) {
  pagination.page = page
  fetchPhotos()
}

// keep-alive 缓存下每次激活都刷新列表数据
onActivated(() => {
  fetchPhotos()
})
</script>

<style scoped>
.photo-card {
  height: 100%;
}

.photo-container {
  position: relative;
  width: 100%;
  height: 150px;
  margin-bottom: 12px;
  border-radius: 8px;
  overflow: hidden;
}

.photo-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.photo-container:hover .photo-overlay {
  opacity: 1;
}

.photo-info {
  text-align: center;
}

.photo-name {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.photo-time {
  font-size: 12px;
  color: #999;
  margin: 0;
}
</style>