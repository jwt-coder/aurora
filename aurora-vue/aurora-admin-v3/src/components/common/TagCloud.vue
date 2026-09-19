<template>
  <div class="tag-list" v-if="sorted.length > 0">
    <div
      v-for="(tag, index) in sorted"
      :key="tag.id ?? index"
      class="tag-row"
      :class="`rank-${index + 1}`"
      :title="`${tag.name} · ${tag.count ?? 0} 篇`"
    >
      <div class="tag-rank" :class="`r-${index + 1}`">{{ index + 1 }}</div>
      <div class="tag-main">
        <div class="tag-name">
          <span class="tag-hash">#</span>{{ tag.name }}
        </div>
        <div class="tag-meta">
          <span class="tag-count">{{ tag.count ?? 0 }} 篇</span>
          <div class="tag-bar">
            <div class="tag-bar-inner" :style="{ width: barWidth(tag.count) }"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="tag-list tag-list-empty">暂无标签数据</div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const sorted = computed(() => {
  return [...(props.data || [])].sort((a, b) => {
    const ca = a.count || 0
    const cb = b.count || 0
    if (cb !== ca) return cb - ca
    return String(a.name || a.tagName || '').localeCompare(String(b.name || ''), 'zh-CN')
  })
})

const barWidth = (count) => {
  if (!sorted.value.length) return '0%'
  const max = Math.max(...sorted.value.map(t => t.count || 0), 1)
  return Math.max(((count || 0) / max) * 100, 6) + '%'
}
</script>

<style scoped>
.tag-list {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 2px 2px 4px;
}

.tag-list::-webkit-scrollbar {
  width: 4px;
}

.tag-list::-webkit-scrollbar-thumb {
  background: #f0c1dd;
  border-radius: 4px;
}

.tag-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fafbfd;
  border: 1px solid #f0f2f7;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.tag-row:hover {
  border-color: #f7c4de;
  background: #fff7fb;
}

.tag-rank {
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

.tag-rank.r-1 {
  background: #ffe4f0;
  color: #c2185b;
}

.tag-rank.r-2 {
  background: #f3e8ff;
  color: #7a4fc0;
}

.tag-rank.r-3 {
  background: #e8f3ff;
  color: #3d7dd8;
}

.tag-main {
  flex: 1;
  min-width: 0;
}

.tag-name {
  font-size: 13px;
  font-weight: 500;
  color: #2c2f38;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag-hash {
  color: #e93796;
  font-weight: 700;
  margin-right: 2px;
}

.tag-row.rank-1 .tag-name {
  color: #c2185b;
  font-weight: 600;
}

.tag-meta {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-count {
  font-size: 12px;
  color: #8b8f9a;
  flex-shrink: 0;
  min-width: 36px;
}

.tag-bar {
  flex: 1;
  height: 4px;
  border-radius: 999px;
  background: #f0f2f7;
  overflow: hidden;
}

.tag-bar-inner {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #f79ad3 0%, #e93796 100%);
  transition: width 0.4s ease;
}

.tag-list-empty {
  align-items: center;
  justify-content: center;
  color: #b0b4c0;
  font-size: 13px;
}
</style>
