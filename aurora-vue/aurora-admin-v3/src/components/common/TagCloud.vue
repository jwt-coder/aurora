<template>
  <div class="tag-cloud-wrapper">
    <span
      v-for="(tag, index) in tags"
      :key="tag.id ?? index"
      class="tag-pill"
      :class="colorClasses[index % colorClasses.length]"
      :style="{ fontSize: fontSize(tag.count) + 'px' }"
      :title="`${tag.name}：${tag.count ?? 0} 篇文章`"
    >
      {{ tag.name }}
      <b v-if="tag.count != null" class="tag-count">{{ tag.count }}</b>
    </span>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const tags = ref([])

watch(
  () => props.data,
  (newData) => {
    tags.value = newData || []
  },
  { immediate: true }
)

const colorClasses = ['color-pink', 'color-blue', 'color-cyan', 'color-purple']

// 按文章数加权字号：12px ~ 20px
const fontSize = (count) => {
  if (!count || tags.value.length === 0) return 13
  const max = Math.max(...tags.value.map(t => t.count || 0))
  if (max <= 1) return 14
  const ratio = (count || 0) / max
  return Math.round(12 + ratio * 8)
}
</script>

<style scoped>
.tag-cloud-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  flex-wrap: wrap;
  align-content: center;
  justify-content: center;
  align-items: center;
  gap: 12px 10px;
  padding: 8px 4px;
  overflow-y: auto;
}

.tag-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 999px;
  font-weight: 500;
  line-height: 1.2;
  cursor: default;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.tag-pill:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.tag-count {
  font-size: 0.75em;
  font-weight: 700;
  opacity: 0.55;
}

/* Aurora 色板：与前台主题一致（粉/蓝/青/紫） */
.color-pink {
  color: #e93796;
  background: rgba(233, 55, 150, 0.08);
  border: 1px solid rgba(233, 55, 150, 0.25);
}

.color-blue {
  color: #547ce7;
  background: rgba(84, 124, 231, 0.08);
  border: 1px solid rgba(84, 124, 231, 0.25);
}

.color-cyan {
  color: #1a9cb8;
  background: rgba(36, 198, 220, 0.1);
  border: 1px solid rgba(36, 198, 220, 0.3);
}

.color-purple {
  color: #7a52e8;
  background: rgba(84, 51, 255, 0.07);
  border: 1px solid rgba(84, 51, 255, 0.22);
}
</style>
