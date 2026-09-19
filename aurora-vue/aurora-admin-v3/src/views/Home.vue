<template>
  <AppPage :show-footer="true">
    <div class="home-content">
      <!-- KPI -->
      <n-grid x-gap="14" y-gap="14" :cols="6" responsive="screen" :item-responsive="true" class="kpi-grid">
        <n-gi span="6 m:2 l:1">
          <n-card class="kpi-card" content-class="kpi-card-body">
            <div class="kpi-inner">
              <div class="kpi-icon kpi-views">
                <svg viewBox="0 0 24 24"><path fill="currentColor" d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zm0 12.5a5 5 0 1 1 0-10 5 5 0 0 1 0 10zm0-2.2a2.8 2.8 0 1 0 0-5.6 2.8 2.8 0 0 0 0 5.6z"/></svg>
              </div>
              <div class="kpi-body">
                <div class="kpi-label">访问量</div>
                <div class="kpi-value"><NumberAnimation :from="0" :to="viewsCount" /></div>
                <div class="kpi-hint">累计浏览</div>
              </div>
            </div>
          </n-card>
        </n-gi>
        <n-gi span="6 m:2 l:1">
          <n-card class="kpi-card" content-class="kpi-card-body">
            <div class="kpi-inner">
              <div class="kpi-icon kpi-users">
                <svg viewBox="0 0 24 24"><path fill="currentColor" d="M16 11a3.5 3.5 0 1 0-3.5-3.5A3.5 3.5 0 0 0 16 11zm-8 0a3.5 3.5 0 1 0-3.5-3.5A3.5 3.5 0 0 0 8 11zm0 2c-2.67 0-8 1.34-8 4v2h9.5v-2c0-1.15.44-2.2 1.17-3.06A15.4 15.4 0 0 0 8 13zm8 0c-.29 0-.62.02-.97.05A5.4 5.4 0 0 1 16.5 17v2H24v-2c0-2.66-5.33-4-8-4z"/></svg>
              </div>
              <div class="kpi-body">
                <div class="kpi-label">用户量</div>
                <div class="kpi-value"><NumberAnimation :from="0" :to="userCount" /></div>
                <div class="kpi-hint">注册用户</div>
              </div>
            </div>
          </n-card>
        </n-gi>
        <n-gi span="6 m:2 l:1">
          <n-card class="kpi-card" content-class="kpi-card-body">
            <div class="kpi-inner">
              <div class="kpi-icon kpi-articles">
                <svg viewBox="0 0 24 24"><path fill="currentColor" d="M6 2h9l5 5v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2zm8 1.5V8h4.5L14 3.5zM8 12h8v1.5H8V12zm0 3.5h8V17H8v-1.5zm0-7h5V10H8V8.5z"/></svg>
              </div>
              <div class="kpi-body">
                <div class="kpi-label">文章量</div>
                <div class="kpi-value"><NumberAnimation :from="0" :to="articleCount" /></div>
                <div class="kpi-hint">已发布文章</div>
              </div>
            </div>
          </n-card>
        </n-gi>
        <n-gi span="6 m:2 l:1">
          <n-card class="kpi-card" content-class="kpi-card-body">
            <div class="kpi-inner">
              <div class="kpi-icon kpi-messages">
                <svg viewBox="0 0 24 24"><path fill="currentColor" d="M4 4h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H8l-4 4V6a2 2 0 0 1 2-2zm3 5h10v1.5H7V9zm0 3h7v1.5H7V12z"/></svg>
              </div>
              <div class="kpi-body">
                <div class="kpi-label">留言量</div>
                <div class="kpi-value"><NumberAnimation :from="0" :to="messageCount" /></div>
                <div class="kpi-hint">留言与评论</div>
              </div>
            </div>
          </n-card>
        </n-gi>
        <n-gi span="6 m:2 l:1">
          <n-card class="kpi-card" content-class="kpi-card-body">
            <div class="kpi-inner">
              <div class="kpi-icon kpi-likes">
                <svg viewBox="0 0 24 24"><path fill="currentColor" d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
              </div>
              <div class="kpi-body">
                <div class="kpi-label">点赞量</div>
                <div class="kpi-value"><NumberAnimation :from="0" :to="likeCount" /></div>
                <div class="kpi-hint">文章点赞</div>
              </div>
            </div>
          </n-card>
        </n-gi>
        <n-gi span="6 m:2 l:1">
          <n-card class="kpi-card" content-class="kpi-card-body">
            <div class="kpi-inner">
              <div class="kpi-icon kpi-collects">
                <svg viewBox="0 0 24 24"><path fill="currentColor" d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/></svg>
              </div>
              <div class="kpi-body">
                <div class="kpi-label">收藏量</div>
                <div class="kpi-value"><NumberAnimation :from="0" :to="collectCount" /></div>
                <div class="kpi-hint">用户收藏</div>
              </div>
            </div>
          </n-card>
        </n-gi>
      </n-grid>

      <!-- 趋势 + 热门 -->
      <n-grid x-gap="14" y-gap="14" :cols="3" responsive="screen" :item-responsive="true" class="main-grid">
        <n-gi span="3 m:2">
          <n-card class="panel" content-class="panel-body">
            <template #header>
              <div class="panel-header">
                <span class="panel-title">访问趋势</span>
                <n-tag size="small" round class="panel-tag">近 7 天</n-tag>
              </div>
            </template>
            <div id="visits_per_week" class="chart-box"></div>
          </n-card>
        </n-gi>

        <n-gi span="3 m:1">
          <n-card class="panel" content-class="panel-body">
            <template #header>
              <div class="panel-header">
                <span class="panel-title">热门文章</span>
                <n-tag size="small" round type="warning" class="panel-tag">TOP {{ Math.max(topArticles.length, 1) }}</n-tag>
              </div>
            </template>
            <div class="hot-list">
              <div
                v-for="(item, index) in topArticles"
                :key="item.id || index"
                class="hot-item"
                :class="`rank-${index + 1}`"
              >
                <div class="hot-rank" :class="`r-${index + 1}`">{{ index + 1 }}</div>
                <div class="hot-main">
                  <div class="hot-title" :title="item.articleTitle">{{ item.articleTitle || '—' }}</div>
                  <div class="hot-meta">
                    <span class="hot-stat" title="浏览">
                      <svg viewBox="0 0 24 24"><path fill="currentColor" d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zm0 12.5a5 5 0 1 1 0-10 5 5 0 0 1 0 10z"/></svg>
                      {{ formatNumber(item.viewsCount) }}
                    </span>
                    <span class="hot-stat" title="点赞">
                      <svg viewBox="0 0 24 24"><path fill="currentColor" d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                      {{ formatNumber(item.likeCount) }}
                    </span>
                    <span class="hot-stat" title="收藏">
                      <svg viewBox="0 0 24 24"><path fill="currentColor" d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/></svg>
                      {{ formatNumber(item.collectCount) }}
                    </span>
                    <div class="hot-bar">
                      <div class="hot-bar-inner" :style="{ width: getProgressWidth(item.viewsCount) }"></div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="topArticles.length === 0" class="hot-empty">暂无热门文章数据</div>
            </div>
          </n-card>
        </n-gi>
      </n-grid>

      <!-- 分类 / 地域 / 标签 -->
      <n-grid x-gap="14" y-gap="14" :cols="3" responsive="screen" :item-responsive="true" class="main-grid">
        <n-gi span="3 m:1">
          <n-card class="panel" content-class="panel-body">
            <template #header>
              <div class="panel-header">
                <span class="panel-title">文章分类</span>
                <n-tag size="small" round class="panel-tag">{{ categoryCount }} 个分类</n-tag>
              </div>
            </template>
            <div id="article_category" class="chart-box"></div>
          </n-card>
        </n-gi>

        <n-gi span="3 m:1">
          <n-card class="panel" content-class="panel-body">
            <template #header>
              <div class="panel-header">
                <span class="panel-title">用户地域</span>
                <n-radio-group v-model:value="userAreaType" size="small">
                  <n-radio-button :value="1">用户</n-radio-button>
                  <n-radio-button :value="2">游客</n-radio-button>
                </n-radio-group>
              </div>
            </template>
            <div id="user-area" class="chart-box chart-box-area"></div>
          </n-card>
        </n-gi>

        <n-gi span="3 m:1">
          <n-card class="panel" content-class="panel-body">
            <template #header>
              <div class="panel-header">
                <span class="panel-title">文章标签</span>
                <n-tag size="small" round class="panel-tag">{{ tagList.length }} 个标签</n-tag>
              </div>
            </template>
            <div id="article-tags" class="tag-box">
              <TagCloud v-if="tagList.length > 0" :data="tagList" />
              <div v-else class="hot-empty">暂无标签</div>
            </div>
          </n-card>
        </n-gi>
      </n-grid>
    </div>
  </AppPage>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { getHomeCardData, getUserArea } from './api'
import AppPage from '@/components/common/AppPage.vue'
import NumberAnimation from '@/components/common/NumberAnimation.vue'
import TagCloud from '@/components/common/TagCloud.vue'
import { Line, Pie, Bar } from '@antv/g2plot'

const userStore = useUserStore()

const PRIMARY = '#e93796'
const PALETTE = ['#e93796', '#f062b0', '#f79ad3', '#9b7bdb', '#5b8def', '#3ec6b6']

const viewsCount = ref(0)
const userCount = ref(0)
const articleCount = ref(0)
const messageCount = ref(0)
const likeCount = ref(0)
const collectCount = ref(0)
const tagList = ref([])
const userAreaType = ref(1)
const categoryCount = ref(0)
const topArticles = ref([])
let userAreaChart = null
let visitWeekChart = null
let articleCategoryChart = null

const fetchHomeData = async () => {
  try {
    const res = await getHomeCardData()
    const data = res.data

    viewsCount.value = data.viewsCount || 0
    userCount.value = data.userCount || 0
    articleCount.value = data.articleCount || 0
    messageCount.value = data.messageCount || 0
    likeCount.value = data.likeCount || 0
    collectCount.value = data.collectCount || 0

    if (data.uniqueViewDTOs && data.uniqueViewDTOs.length > 0) {
      initVisitWeek(data.uniqueViewDTOs)
    }

    if (data.articleRankDTOs && data.articleRankDTOs.length > 0) {
      initArticleVisits(data.articleRankDTOs)
    }

    if (data.categoryDTOs && data.categoryDTOs.length > 0) {
      categoryCount.value = data.categoryDTOs.length
      initArticleCategory(data.categoryDTOs)
    }

    if (data.tagDTOs && data.tagDTOs.length > 0) {
      tagList.value = data.tagDTOs.map(tag => ({
        name: tag.tagName,
        id: tag.id,
        count: tag.count
      }))
    }
    return data
  } catch (error) {
    console.error('获取首页数据失败:', error)
    return null
  }
}

const fetchUserArea = async () => {
  try {
    const res = await getUserArea({ type: userAreaType.value })
    if (res && res.data && res.data.length > 0) {
      initUserArea(res.data)
    }
  } catch (error) {
    console.error('获取用户地域分布失败:', error)
  }
}

const initVisitWeek = (data) => {
  const container = document.getElementById('visits_per_week')
  if (!container) return
  if (visitWeekChart) visitWeekChart.destroy()

  visitWeekChart = new Line(container, {
    data,
    xField: 'day',
    yField: 'viewsCount',
    smooth: true,
    color: [PRIMARY],
    area: {
      style: {
        fill: `l(270) 0:#ffffff 0.45:${PRIMARY} 1:${PRIMARY}`,
        fillOpacity: 0.16
      }
    },
    lineStyle: { lineWidth: 2.5 },
    point: {
      size: 3.5,
      style: { fill: '#fff', stroke: PRIMARY, lineWidth: 2 }
    },
    xAxis: {
      label: { style: { fill: '#8b8f9a', fontSize: 11 } },
      line: { style: { stroke: '#ececf2' } },
      tickLine: null
    },
    yAxis: {
      label: { style: { fill: '#8b8f9a', fontSize: 11 } },
      grid: {
        line: { style: { stroke: '#f0f1f6', lineDash: [4, 4] } }
      }
    },
    tooltip: {
      showCrosshairs: true,
      shared: true
    },
    animation: true,
    padding: [16, 12, 24, 12]
  })
  visitWeekChart.render()
}

const initArticleVisits = (data) => {
  topArticles.value = data.slice(0, 5).map((item, index) => ({
    ...item,
    id: item.id || `rank-${index}`
  }))
}

const initArticleCategory = (data) => {
  const container = document.getElementById('article_category')
  if (!container) return
  if (articleCategoryChart) articleCategoryChart.destroy()

  articleCategoryChart = new Pie(container, {
    data,
    angleField: 'articleCount',
    colorField: 'categoryName',
    radius: 0.82,
    innerRadius: 0.62,
    color: PALETTE,
    label: {
      type: 'inner',
      offset: '-40%',
      content: '{value}',
      style: { textAlign: 'center', fontSize: 12, fill: '#fff' }
    },
    statistic: {
      title: {
        offsetY: -6,
        content: '文章总数',
        style: { fontSize: '12px', color: '#8b8f9a' }
      },
      content: {
        offsetY: 6,
        style: { fontSize: '22px', color: '#2c2f38', fontWeight: '700' }
      }
    },
    legend: {
      position: 'bottom',
      layout: { justifyContent: 'center', alignItems: 'center' },
      itemName: { style: { fill: '#5c6070', fontSize: 12 } }
    },
    interactions: [{ type: 'element-active' }],
    animation: true
  })
  articleCategoryChart.render()
}

const initUserArea = (data) => {
  const container = document.getElementById('user-area')
  if (!container) return
  if (userAreaChart) userAreaChart.destroy()

  const chartData = data
    .map(item => ({
      name: item.name || item.provinceName || item.areaName || item.regionName || '未知',
      value: item.value || item.count || item.userCount || 0
    }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 10)

  if (chartData.length === 0) return
  const maxValue = Math.max(...chartData.map(item => item.value), 1)

  userAreaChart = new Bar(container, {
    data: chartData,
    xField: 'value',
    yField: 'name',
    seriesField: 'value',
    color: ({ value }) => {
      const ratio = value / maxValue
      if (ratio > 0.75) return '#c2185b'
      if (ratio > 0.5) return PRIMARY
      if (ratio > 0.3) return '#f062b0'
      if (ratio > 0.15) return '#f79ad3'
      return '#fbc6e3'
    },
    legend: false,
    barStyle: { radius: [0, 4, 4, 0] },
    tooltip: {
      formatter: datum => ({ name: datum.name, value: datum.value + ' 人' })
    },
    label: {
      position: 'right',
      style: { fill: '#8b8f9a', fontSize: 11 },
      formatter: ({ value }) => value
    },
    xAxis: {
      label: { style: { fill: '#8b8f9a', fontSize: 11 } },
      grid: { line: { style: { stroke: '#f0f1f6' } } }
    },
    yAxis: {
      label: { style: { fill: '#5c6070', fontSize: 12 } }
    },
    responsive: true,
    appendPadding: [8, 28, 0, 4],
    animation: true
  })
  userAreaChart.render()
}

const formatNumber = (num) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return String(num || 0)
}

const getProgressWidth = (views) => {
  if (!topArticles.value || topArticles.value.length === 0) return '0%'
  const maxViews = Math.max(...topArticles.value.map(item => item.viewsCount || 0))
  if (maxViews === 0) return '0%'
  return Math.max((views / maxViews) * 100, 8) + '%'
}

watch(userAreaType, () => {
  fetchUserArea()
})

const handleResize = () => {
  if (userAreaChart) userAreaChart.changeSize()
}

onMounted(() => {
  // 两个接口并行拉取，避免串行等待
  Promise.all([fetchHomeData(), fetchUserArea()])
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (visitWeekChart) {
    visitWeekChart.destroy()
    visitWeekChart = null
  }
  if (articleCategoryChart) {
    articleCategoryChart.destroy()
    articleCategoryChart = null
  }
  if (userAreaChart) {
    userAreaChart.destroy()
    userAreaChart = null
  }
})
</script>

<style scoped>
.home-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 0;
}

.kpi-grid,
.main-grid {
  margin: 0 !important;
}

.panel,
.kpi-card {
  border-radius: 14px;
  border: 1px solid #eceef5;
  box-shadow: 0 1px 2px rgba(30, 34, 48, 0.04);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  overflow: hidden;
}

.panel:hover,
.kpi-card:hover {
  box-shadow: 0 8px 24px rgba(233, 55, 150, 0.08);
}

.panel-body {
  padding: 12px 14px 14px !important;
}

.kpi-card-body {
  padding: 16px 18px !important;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c2f38;
}

.panel-tag {
  background: #fff0f7 !important;
  color: #c2185b !important;
  border: none !important;
}

.kpi-inner {
  display: flex;
  align-items: center;
  gap: 14px;
}

.kpi-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.kpi-icon svg {
  width: 24px;
  height: 24px;
}

.kpi-views {
  background: linear-gradient(135deg, #e93796 0%, #f062b0 100%);
  color: #fff;
  box-shadow: 0 6px 14px rgba(233, 55, 150, 0.28);
}

.kpi-users {
  background: linear-gradient(135deg, #5b8def 0%, #7aa8ff 100%);
  color: #fff;
  box-shadow: 0 6px 14px rgba(91, 141, 239, 0.25);
}

.kpi-articles {
  background: linear-gradient(135deg, #3ec6b6 0%, #5ed4c6 100%);
  color: #fff;
  box-shadow: 0 6px 14px rgba(62, 198, 182, 0.25);
}

.kpi-messages {
  background: linear-gradient(135deg, #9b7bdb 0%, #b39be8 100%);
  color: #fff;
  box-shadow: 0 6px 14px rgba(155, 123, 219, 0.25);
}

.kpi-likes {
  background: linear-gradient(135deg, #f25c54 0%, #ff7b72 100%);
  color: #fff;
  box-shadow: 0 6px 14px rgba(242, 92, 84, 0.25);
}

.kpi-collects {
  background: linear-gradient(135deg, #f0a020 0%, #f5c15a 100%);
  color: #fff;
  box-shadow: 0 6px 14px rgba(240, 160, 32, 0.25);
}

.kpi-body {
  min-width: 0;
  flex: 1;
}

.kpi-label {
  font-size: 13px;
  color: #8b8f9a;
  margin-bottom: 4px;
}

.kpi-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2230;
  line-height: 1.1;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.kpi-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #b0b4c0;
}

.chart-box {
  height: 280px;
  width: 100%;
}

.chart-box-area {
  height: 280px;
}

#user-area {
  min-height: 280px !important;
  height: 280px !important;
  width: 100% !important;
}

#user-area canvas {
  width: 100% !important;
  height: 100% !important;
}

.tag-box {
  height: 280px;
  width: 100%;
  display: block;
  overflow: hidden;
}

.tag-box :deep(.tag-list) {
  height: 248px;
}

#article-tags {
  height: 280px !important;
}

.hot-list {
  height: 280px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-right: 4px;
}

.hot-list::-webkit-scrollbar {
  width: 4px;
}

.hot-list::-webkit-scrollbar-thumb {
  background: #f0c1dd;
  border-radius: 4px;
}

.hot-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fafbfd;
  border: 1px solid #f0f2f7;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.hot-item:hover {
  border-color: #f7c4de;
  background: #fff7fb;
}

.hot-rank {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 1px;
  background: #eef0f5;
  color: #8b8f9a;
}

.hot-rank.r-1 {
  background: #ffe4f0;
  color: #c2185b;
}

.hot-rank.r-2 {
  background: #f3e8ff;
  color: #7a4fc0;
}

.hot-rank.r-3 {
  background: #e8f3ff;
  color: #3d7dd8;
}

.hot-main {
  flex: 1;
  min-width: 0;
}

.hot-title {
  font-size: 13px;
  font-weight: 500;
  color: #2c2f38;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-item.rank-1 .hot-title {
  color: #c2185b;
  font-weight: 600;
}

.hot-meta {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.hot-views {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8b8f9a;
  flex-shrink: 0;
}

.hot-views svg {
  width: 13px;
  height: 13px;
}

.hot-stat {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8b8f9a;
  flex-shrink: 0;
}

.hot-stat svg {
  width: 13px;
  height: 13px;
}

.hot-bar {
  flex: 1;
  height: 4px;
  border-radius: 999px;
  background: #f0f2f7;
  overflow: hidden;
}

.hot-bar-inner {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #f79ad3 0%, #e93796 100%);
  transition: width 0.5s ease;
}

.hot-item.rank-1 .hot-bar-inner {
  background: linear-gradient(90deg, #f79ad3 0%, #c2185b 100%);
}

.hot-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #b0b4c0;
  font-size: 13px;
}

@media (max-width: 960px) {
  .chart-box,
  .chart-box-area,
  .hot-list,
  .tag-box,
  #user-area,
  #article-tags {
    height: 240px !important;
    min-height: 240px !important;
  }

  .kpi-value {
    font-size: 22px;
  }
}
</style>
