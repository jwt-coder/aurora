import { defineStore } from 'pinia'

export const useArticleStore = defineStore('articleStore', {
  state: () => {
    return {
      topArticle: '' as any,
      featuredArticles: [] as any,
      // 置顶/推荐是否已加载完成（用于区分"加载中骨架"与"确实没有数据"）
      topFeaturedLoaded: false as any,
      articles: [] as any,
      categories: [] as any,
      archives: [] as any
    }
  },
  actions: {}
})
