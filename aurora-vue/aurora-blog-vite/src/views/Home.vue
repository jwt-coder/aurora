<template>
  <div class="block">
    <!-- 置顶/推荐加载完成后若一篇都没有，隐藏整个推荐区，避免骨架永远转圈 -->
    <Feature v-if="themeConfig.feature && !(topFeaturedLoaded && featuredArticles.length === 0)">
      <FeatureList />
    </Feature>
    <span v-if="themeConfig.feature">
      <Title id="article-list" :title="'titles.articles'" icon="article" />
    </span>
    <div class="main-grid">
      <div class="flex flex-col relative">
        <ul :class="tabClass">
          <li :class="{ active: activeTab === 0 }" @click="handleTabChange(0)">
            <span class="first-tab" :style="activeTabStyle(0)">
              {{ t('settings.button-all') }}
            </span>
          </li>
          <template v-if="categories && categories.length > 0">
            <li
              v-for="category in categories"
              :key="category.id"
              :class="{ active: activeTab === category.id }"
              @click="handleTabChange(category.id)">
              <span :style="activeTabStyle(category.id)">
                {{ category.categoryName }}
              </span>
              <b>
                {{ category.articleCount }}
              </b>
            </li>
          </template>
          <template v-else-if="categories.length === 0">
            <li v-for="i in 6" :key="i" style="position: relative; top: -4px">
              <ob-skeleton tag="span" width="60px" height="33px" />
            </li>
          </template>
        </ul>
        <span :class="expanderClass" @click="expandHandler">
          <svg-icon icon-class="chevron" />
        </span>
        <ul class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8 items-stretch">
          <template v-if="haveArticles === true">
            <li class="flex h-full min-w-0" v-for="article in articles" :key="article.id">
              <ArticleCard class="home-article w-full h-full" :data="article" />
            </li>
            <li v-if="articles.length === 0" class="col-span-full text-center text-ob-dim py-16">
              {{ t('settings.no-articles') }}
            </li>
          </template>
          <template v-else-if="articleLoadFailed">
            <li class="col-span-full text-center text-ob-dim py-16">
              加载失败，请刷新重试
            </li>
          </template>
          <template v-else>
            <li class="flex h-full min-w-0" v-for="n in 12" :key="n">
              <ArticleCard class="home-article w-full h-full" :data="{}" />
            </li>
          </template>
        </ul>
        <Paginator
          :pageSize="pagination.size"
          :pageTotal="pagination.total"
          :page="pagination.current"
          @pageChange="pageChangeHanlder" />
      </div>
      <div>
        <Sidebar>
          <Profile />
          <RecentComment  v-if="true" />
          <TagBox />
          <Notice />
          <WebsiteInfo />
          <SidebarBox />
        </Sidebar>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, ref, toRefs, toRef, reactive } from 'vue'
import { Feature, FeatureList } from '@/components/Feature'
import { ArticleCard, HorizontalArticle } from '@/components/ArticleCard'
import { Title } from '@/components/Title'
import { Sidebar, Profile, RecentComment, TagBox, Notice, WebsiteInfo } from '@/components/Sidebar'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { useArticleStore } from '@/stores/article'
import { useCategoryStore } from '@/stores/Category'
import { useI18n } from 'vue-i18n'
import Paginator from '@/components/Paginator.vue'
import api from '@/api/api'
import { stripHtml } from '@/utils/markdown'
import SidebarBox from "@/components/Sidebar/src/SidebarBox.vue";

export default defineComponent({
  name: 'Home',
  components: {
    SidebarBox,
    Feature,
    FeatureList,
    ArticleCard,
    HorizontalArticle,
    Title,
    Paginator,
    Sidebar,
    Profile,
    RecentComment,
    TagBox,
    Notice,
    WebsiteInfo,
  },
  setup() {
    const appStore = useAppStore()
    const userStore = useUserStore()
    const articleStore = useArticleStore()
    const categoryStore = useCategoryStore()
    const { t } = useI18n()
    const expanderClass = ref({
      'tab-expander': true,
      expanded: false
    })
    const tabClass = ref({
      tab: true,
      'expanded-tab': false
    })
    const activeTab = ref(0)
    const articleOffset = ref(0)
    const reactiveData = reactive({
      haveArticles: false,
      articleLoadFailed: false
    })
    const pagination = reactive({
      size: 15,
      total: 0,
      current: 1
    })
    let nowCategoryId = 0
    onMounted(() => {
      fetchTopAndFeatured()
      fetchCategories()
      fetchArticles()
      const articleListEl = document.getElementById('article-list')
      articleOffset.value = articleListEl && articleListEl instanceof HTMLElement ? articleListEl.offsetTop + 120 : 0
    })
    const fetchTopAndFeatured = () => {
      api.getTopAndFeaturedArticles().then(({ data }) => {
        if (data.flag && data.data) {
          // 没有任何置顶/推荐文章时后端返回 null 字段，逐个判空，不能裸取
          if (data.data.topArticle) {
            data.data.topArticle.articleContent = stripHtml(data.data.topArticle.articleContent)
          }
          ;(data.data.featuredArticles || []).forEach((item: any) => {
            item.articleContent = stripHtml(item.articleContent)
          })
          articleStore.topArticle = data.data.topArticle || ''
          articleStore.featuredArticles = data.data.featuredArticles || []
        }
      }).catch(() => {
        // 网络失败也要结束骨架，避免一直转圈
      }).finally(() => {
        articleStore.topFeaturedLoaded = true
      })
    }
    const fetchArticles = () => {
      activeTab.value = userStore.tab
      nowCategoryId = userStore.tab
      pagination.current = userStore.page
      if (userStore.tab === 0) {
        reactiveData.haveArticles = false
        reactiveData.articleLoadFailed = false
        api
          .getArticles({
            current: pagination.current,
            size: pagination.size
          })
          .then(({ data }) => {
            if (data.flag && data.data) {
              data.data.records.forEach((item: any) => {
                item.articleContent = stripHtml(item.articleContent)
              })
              articleStore.articles = data.data.records
              pagination.total = data.data.count
              reactiveData.haveArticles = true
            } else {
              reactiveData.articleLoadFailed = true
            }
          })
          .catch(() => {
            reactiveData.articleLoadFailed = true
          })
      } else {
        fetchArticlesByCategoryId(userStore.tab)
      }
    }
    const fetchArticlesByCategoryId = (categoryId: any) => {
      reactiveData.haveArticles = false
      reactiveData.articleLoadFailed = false
      api
        .getArticlesByCategoryId({
          current: pagination.current,
          size: pagination.size,
          categoryId: categoryId
        })
        .then(({ data }) => {
          if (data.flag && data.data) {
            data.data.records.forEach((item: any) => {
              item.articleContent = stripHtml(item.articleContent)
            })
            articleStore.articles = data.data.records
            pagination.total = data.data.count
            reactiveData.haveArticles = true
          } else {
            reactiveData.articleLoadFailed = true
          }
        })
        .catch(() => {
          reactiveData.articleLoadFailed = true
        })
    }
    const fetchCategories = () => {
      categoryStore.categories = []
      api.getAllCategories().then(({ data }) => {
        categoryStore.categories.push(...(data.data || []))
      })
    }
    const expandHandler = () => {
      expanderClass.value.expanded = !expanderClass.value.expanded
      tabClass.value['expanded-tab'] = !tabClass.value['expanded-tab']
    }
    const handleTabChange = (categoryId: any) => {
      userStore.tab = categoryId
      userStore.page = 1
      pagination.current = 1
      activeTab.value = categoryId
      nowCategoryId = categoryId
      toArticleOffset()
      if (categoryId === 0) {
        fetchArticles()
      } else {
        fetchArticlesByCategoryId(categoryId)
      }
    }
    const toArticleOffset = () => {
      window.scrollTo({
        top: articleOffset.value
      })
    }
    const activeTabStyle = (catagoryId: any) => {
      if (catagoryId === activeTab.value) return { background: appStore.themeConfig.header_gradient_css }
      return {}
    }
    const pageChangeHanlder = (current: number) => {
      userStore.page = current
      pagination.current = current
      toArticleOffset()
      if (nowCategoryId === 0) {
        fetchArticles()
      } else {
        fetchArticlesByCategoryId(nowCategoryId)
      }
    }
    return {
      ...toRefs(reactiveData),
      ...toRefs(articleStore.$state),
      categories: toRef(categoryStore.$state, 'categories'),
      gradientText: computed(() => appStore.themeConfig.background_gradient_style),
      gradientBackground: computed(() => {
        return { background: appStore.themeConfig.header_gradient_css }
      }),
      themeConfig: computed(() => appStore.themeConfig),
      expanderClass,
      tabClass,
      expandHandler,
      handleTabChange,
      activeTabStyle,
      activeTab,
      pagination,
      pageChangeHanlder,
      t
    }
  }
})
</script>
<style lang="scss">
/* 首页文章列表：同行卡片等高，标题/摘要定高，作者信息贴底 */
.home-article.article-container {
  height: 100%;
  display: flex;
  flex-direction: column;

  > .article {
    height: 100%;
    flex: 1 1 auto;
    min-height: 0;
  }

  .article-content {
    h2.article-title {
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
      min-height: calc(1.25em * 2 + 0.5em);
      max-height: calc(1.25em * 2 + 0.5em);
      margin-bottom: 0.75rem;
    }

    p {
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      word-break: break-word;
      /* 无论摘要长短，占位高度一致 */
      min-height: calc(1.4em * 3);
      max-height: calc(1.4em * 3);
    }

    p.encrypted-content {
      min-height: calc(1.4em * 3);
      max-height: calc(1.4em * 3);
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 3;
      overflow: hidden;
    }

    .article-footer {
      margin-top: auto;
      padding-top: 12px;
      flex: 0 0 auto;
    }
  }
}
//调整文章显示行数
</style>
