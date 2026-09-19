<template>
  <div class="flex flex-col">
    <div class="main-grid">
      <div class="post-header">
        <span class="post-labels">
          <ob-skeleton v-if="loading" tag="b" height="20px" width="35px" />
          <b v-else-if="!loading && article.categoryName">
            <span>{{ article.categoryName }}</span>
          </b>
          <b v-else>{{ t('settings.default-category') }}</b>
          <ul>
            <ob-skeleton v-if="loading" :count="2" tag="li" height="16px" width="35px" class="mr-2" />
            <template v-else-if="!loading && article.tags && article.tags.length > 0">
              <li v-for="tag in article.tags" :key="tag.id">
                <em class="opacity-50">#</em>
                {{ tag.tagName }}
              </li>
            </template>
            <template v-else>
              <li>
                <b class="opacity-50">#</b>
                {{ t('settings.default-tag') }}
              </li>
            </template>
          </ul>
        </span>
        <h1 v-if="article.articleTitle" class="post-title text-white">
          {{ article.articleTitle }}
        </h1>
        <ob-skeleton
          v-else
          class="post-title text-white uppercase"
          width="100%"
          height="clamp(1.2rem, calc(1rem + 3.5vw), 4rem)" />
        <div class="flex flex-row items-center justify-start mt-8 mb-4">
          <div class="post-footer" v-if="article.author">
            <img
              class="hover:opacity-50 cursor-pointer"
              v-lazy="article.author.avatar || ''"
              alt="author avatar"
              @click="handleAuthorClick(article.author.website)" />
            <span class="text-white opacity-80">
              <strong
                class="text-white pr-1.5 hover:opacity-50 cursor-pointer"
                @click="handleAuthorClick(article.author.website)">
                {{ article.author.nickname }}
              </strong>
              <span class="opacity-70">
                {{ t('settings.shared-on') }} {{ t(`settings.months[${new Date(article.createTime).getMonth()}]`) }}
                {{ new Date(article.createTime).getDate() }}, {{ new Date(article.createTime).getFullYear() }}
              </span>
            </span>
          </div>
          <div class="post-footer" v-else>
            <div class="flex flex-row items-center">
              <ob-skeleton class="mr-2" height="28px" width="28px" :circle="true" />
              <span class="text-ob-dim mt-1">
                <ob-skeleton height="20px" width="150px" />
              </span>
            </div>
          </div>
          <div class="post-stats" v-if="wordNum !== '' && readTime !== ''">
            <span>
              <svg-icon icon-class="text-outline" style="stroke: white" />
              <span class="pl-2 opacity-70">
                {{ wordNum }}
              </span>
            </span>
            <span>
              <svg-icon icon-class="clock-outline" style="stroke: white" />
              <span class="pl-2 opacity-70">
                {{ readTime }}
              </span>
            </span>
            <span v-if="!loading && article.viewCount !== undefined && article.viewCount !== null">
              <svg-icon icon-class="eye" style="stroke: white" />
              <span class="pl-2 opacity-70">
                {{ article.viewCount }}
              </span>
            </span>
          </div>
          <div v-else class="post-stats">
            <span>
              <svg-icon icon-class="clock" />
              <span class="pl-2">
                <ob-skeleton width="40px" height="16px" />
              </span>
            </span>
            <span>
              <svg-icon icon-class="text" />
              <span class="pl-2">
                <ob-skeleton width="40px" height="16px" />
              </span>
            </span>
          </div>
        </div>
      </div>
    </div>


    <div class="main-grid">
      <div>
        <template v-if="article.articleContent">
          <div class="post-html" ref="articleRef" v-html="article.articleContent" /><br/>
          <div class="security post-html">
            <ul>
              <li class="author">
                <svg-icon icon-class="author" size="0.9rem" style="margin-right:0.3rem"></svg-icon>
                <strong>本文作者： </strong>{{ article.author.nickname }}
              </li>
              <li class="link">
                <svg-icon icon-class="article_link" size="0.9rem" style="margin-right:0.3rem"></svg-icon>
                <strong>本文链接：</strong>
                <a :href="void(0)"> {{ article.author.website }}/articles/{{ article.id }}</a>
              </li>
              <li class="license">
                <svg-icon icon-class="article_share" size="0.8rem" style="margin-right:0.3rem"></svg-icon>
                <strong>版权声明：</strong>本站所有文章除特别声明外，均采用
                <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh" target="_blank">CC
                  BY-NC-SA 4.0</a>
                许可协议。转载请注明文章出处！
              </li>
            </ul>
          </div>
          <div class="article-actions">
            <button
              class="action-button"
              :class="{ 'action-active': article.isLiked }"
              @click="handleLike">
              <svg viewBox="0 0 24 24" class="action-icon" :class="{ 'icon-pop': article.isLiked }">
                <path
                  d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
              </svg>
              <span>{{ article.isLiked ? '已点赞' : '点赞' }}</span>
              <span class="action-count">{{ article.likeCount || 0 }}</span>
            </button>
            <button
              class="action-button"
              :class="{ 'action-active': article.isCollected }"
              @click="handleCollect">
              <svg viewBox="0 0 24 24" class="action-icon" :class="{ 'icon-pop': article.isCollected }">
                <path
                  d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z" />
              </svg>
              <span>{{ article.isCollected ? '已收藏' : '收藏' }}</span>
            </button>
          </div>
        </template>
        <div v-else class="bg-ob-deep-800 px-14 py-16 rounded-2xl shadow-xl block min-h-screen">
          <ob-skeleton tag="div" :count="1" height="36px" width="150px" class="mb-6" />
          <br />
          <ob-skeleton tag="div" :count="35" height="16px" width="100px" class="mr-2" />
          <br />
          <br />
          <ob-skeleton tag="div" :count="25" height="16px" width="100px" class="mr-2" />
        </div>
        <div class="article-paginator my-8">
          <div class="paginator-col" v-if="preArticleCard">
            <SubTitle title="settings.paginator.pre" icon="arrow-left-circle" />
            <ArticleCard class="pre-and-next-article" :data="preArticleCard" />
          </div>
          <div class="paginator-col" v-if="nextArticleCard">
            <SubTitle title="settings.paginator.next" :side="!isMobile ? 'right' : 'left'" icon="arrow-right-circle" />
            <ArticleCard class="pre-and-next-article" :data="nextArticleCard" />
          </div>
        </div>
        <Comment />
      </div>
      <div>
        <Sidebar>
          <Profile />
          <Sticky :stickyTop="32" endingElId="footer" dynamicElClass="#sticky-sidebar">
            <div id="sticky-sidebar">
              <transition name="fade-slide-y" mode="out-in">
                <div class="sidebar-box mb-4">
                  <SubTitle :title="'titles.toc'" icon="toc" />
                  <div id="toc1"></div>
                </div>
              </transition>
              <Navigator />
            </div>
          </Sticky>
        </Sidebar>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Sidebar, Profile, Navigator } from '@/components/Sidebar'
import {
  computed,
  defineComponent,
  nextTick,
  onUnmounted,
  onMounted,
  reactive,
  ref,
  toRefs,
  provide,
  getCurrentInstance
} from 'vue'
import { useRoute, useRouter, onBeforeRouteUpdate } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Comment } from '@/components/Comment'
import { SubTitle } from '@/components/Title'
import { ArticleCard } from '@/components/ArticleCard'
import '@/styles/prism-aurora-future.css'
import { useCommonStore } from '@/stores/common'
import { useCommentStore } from '@/stores/comment'
import { useUserStore } from '@/stores/user'
import Sticky from '@/components/Sticky.vue'
import Prism from 'prismjs'
import 'prismjs/components/prism-markup'
import 'prismjs/components/prism-css'
import 'prismjs/components/prism-clike'
import 'prismjs/components/prism-javascript'
import 'prismjs/components/prism-typescript'
import 'prismjs/components/prism-java'
import 'prismjs/components/prism-bash'
import 'prismjs/components/prism-json'
import 'prismjs/components/prism-yaml'
import 'prismjs/components/prism-python'
import 'prismjs/components/prism-sql'
import tocbot from 'tocbot'
import emitter from '@/utils/mitt'
import { v3ImgPreviewFn } from 'v3-img-preview'
import api from '@/api/api'
import markdownToHtml, { stripHtml } from '@/utils/markdown'

export default defineComponent({
  name: 'Article',
  components: { Sidebar, Comment, SubTitle, ArticleCard, Profile, Sticky, Navigator },
  setup() {
    const proxy: any = getCurrentInstance()?.appContext.config.globalProperties
    const commonStore = useCommonStore()
    const commentStore = useCommentStore()
    const userStore = useUserStore()
    const route = useRoute()
    const router = useRouter()
    const { t } = useI18n()
    const loading = ref(true)
    const articleRef = ref()
    const reactiveData = reactive({
      articleId: '' as any,
      article: '' as any,
      wordNum: '' as any,
      readTime: '' as any,
      comments: [] as any,
      images: [] as any,
      preArticleCard: '' as any,
      nextArticleCard: '' as any,
      haveMore: false as any,
      isReload: false as any
    })
    const pageInfo = reactive({
      current: 1,
      size: 7
    })
    commentStore.type = 1
    onMounted(() => {
      reactiveData.articleId = route.params.articleId
      toPageTop()
      fetchArticle()
      fetchComments()
    })
    onUnmounted(() => {
      commonStore.resetHeaderImage()
      reactiveData.article = ''
      tocbot.destroy()
      emitter.off('articleFetchComment', handleFetchComment)
      emitter.off('articleFetchReplies', handleFetchReplies)
      emitter.off('articleLoadMore', handleLoadMore)
    })
    onBeforeRouteUpdate((to) => {
      reactiveData.article = ''
      reactiveData.readTime = ''
      reactiveData.wordNum = ''
      reactiveData.comments = []
      reactiveData.images = []
      reactiveData.preArticleCard = ''
      reactiveData.nextArticleCard = ''
      reactiveData.articleId = to.params.articleId
      pageInfo.current = 1
      reactiveData.isReload = true
      toPageTop()
      fetchArticle()
      fetchComments()
    })
    provide(
      'comments',
      computed(() => reactiveData.comments)
    )
    provide(
      'haveMore',
      computed(() => reactiveData.haveMore)
    )
    const handleFetchComment = () => {
      pageInfo.current = 1
      reactiveData.isReload = true
      fetchComments()
    }
    const handleFetchReplies = (index: any) => {
      fetchReplies(index)
    }
    const handleLoadMore = () => {
      fetchComments()
    }
    emitter.on('articleFetchComment', handleFetchComment)
    emitter.on('articleFetchReplies', handleFetchReplies)

    emitter.on('articleLoadMore', handleLoadMore)
    const handlePreview = (index: any) => {
      v3ImgPreviewFn({ images: reactiveData.images, index: reactiveData.images.indexOf(index) })
    }
    /** 代码块：标题栏（mac 圆点 + 语言 + 复制），与正文代码区分开 */
    const decorateCodeBlocks = () => {
      const root = articleRef.value
      if (!root) return
      const pres = root.querySelectorAll('pre')
      pres.forEach((pre) => {
        if (pre.querySelector('.code-block-header')) return
        const codeEl = pre.querySelector('code')
        const classStr = `${pre.className || ''} ${codeEl?.className || ''}`
        const langMatch = classStr.match(/language-([\w+#-]+)/)
        const lang = langMatch ? langMatch[1] : 'code'
        const header = document.createElement('div')
        header.className = 'code-block-header'
        const dots = document.createElement('span')
        dots.className = 'code-block-dots'
        dots.setAttribute('aria-hidden', 'true')
        const langEl = document.createElement('span')
        langEl.className = 'code-block-lang'
        langEl.textContent = lang
        const btn = document.createElement('button')
        btn.type = 'button'
        btn.className = 'code-block-copy'
        btn.textContent = '复制'
        btn.addEventListener('click', async () => {
          const text = codeEl ? codeEl.innerText : pre.innerText
          try {
            await navigator.clipboard.writeText(text)
            btn.textContent = '已复制'
          } catch (e) {
            btn.textContent = '复制失败'
          }
          setTimeout(() => {
            btn.textContent = '复制'
          }, 1600)
        })
        header.appendChild(dots)
        header.appendChild(langEl)
        header.appendChild(btn)
        pre.insertBefore(header, pre.firstChild)
        pre.classList.add('code-block')
      })
    }

    const initTocbot = () => {
      let nodes = articleRef.value.children
      if (nodes.length) {
        for (let i = 0; i < nodes.length; i++) {
          let node = nodes[i]
          let reg = /^H[1-4]{1}$/
          if (reg.exec(node.tagName)) {
            node.id = i
          }
        }
      }
      tocbot.init({
        tocSelector: '#toc1',
        contentSelector: '.post-html',
        headingSelector: 'h1, h2, h3',
        collapseDepth: 3,
        disableTocScrollSync: true,
        onClick: function (e) {
          e.preventDefault()
        }
      })
      const imgs = articleRef.value.getElementsByTagName('img')
      for (var i = 0; i < imgs.length; i++) {
        reactiveData.images.push(imgs[i].src)
        imgs[i].addEventListener('click', function (e: any) {
          handlePreview(e.target.currentSrc)
        })
      }
    }
    const fetchArticle = () => {
      loading.value = true
      api.getArticeById(reactiveData.articleId).then(({ data }) => {
        try {
          if (data.code === 52003) {
            proxy.$notify({
              title: 'Error',
              message: '文章密码认证未通过',
              type: 'error'
            })
            router.push({ path: '/出错啦' })
            return
          }
          if (data.flag && data.data) {
            commonStore.setHeaderImage(data.data.articleCover)
            const raw = data.data
            // 上下篇只需短摘要：无摘要时后端会回 SUBSTR(正文,500)，这里截断避免卡片被撑很高
            if (raw.preArticleCard) {
              raw.preArticleCard.articleContent = stripHtml(raw.preArticleCard.articleContent, 60)
              reactiveData.preArticleCard = raw.preArticleCard
            }
            if (raw.nextArticleCard) {
              raw.nextArticleCard.articleContent = stripHtml(raw.nextArticleCard.articleContent, 60)
              reactiveData.nextArticleCard = raw.nextArticleCard
            }
            raw.articleContent = markdownToHtml(raw.articleContent)
            reactiveData.article = raw
            reactiveData.wordNum = Math.round(deleteHTMLTag(raw.articleContent).length / 100) / 10 + 'k'
            reactiveData.readTime = Math.round(deleteHTMLTag(raw.articleContent).length / 400) + 'mins'
            loading.value = false
            nextTick(() => {
              Prism.highlightAll()
              decorateCodeBlocks()
              initTocbot()
            })
          } else {
            loading.value = false
            router.push({ path: '/出错啦' })
          }
        } catch (error) {
          // 拦截器已弹出错误提示，这里不再重复弹窗
          loading.value = false
        }
      })
    }
    let likePending = false
    const handleLike = () => {
      if (!reactiveData.article || likePending) return
      const article = reactiveData.article
      const prevLiked = !!article.isLiked
      const prevCount = article.likeCount || 0
      likePending = true
      // 乐观更新：点击立刻反馈，接口返回后再对齐或回滚
      article.isLiked = !prevLiked
      article.likeCount = prevLiked ? Math.max(prevCount - 1, 0) : prevCount + 1
      api.likeArticle(reactiveData.articleId)
        .then(({ data }) => {
          if (data.flag && data.data) {
            article.likeCount = data.data.likeCount
            article.isLiked = data.data.isLiked
          } else {
            article.isLiked = prevLiked
            article.likeCount = prevCount
          }
        })
        .catch(() => {
          article.isLiked = prevLiked
          article.likeCount = prevCount
        })
        .finally(() => {
          likePending = false
        })
    }
    const handleCollect = () => {
      if (!userStore.userInfo || userStore.userInfo === '') {
        proxy.$notify({
          title: 'Warning',
          message: '请登录后收藏',
          type: 'warning'
        })
        return
      }
      if (!reactiveData.article) return
      api.collectArticle(reactiveData.articleId).then(({ data }) => {
        if (data.flag && data.data !== null && data.data !== undefined) {
          reactiveData.article.isCollected = data.data
          proxy.$notify({
            title: 'Success',
            message: data.data ? '收藏成功，可在用户中心查看' : '已取消收藏',
            type: 'success'
          })
        }
      })
    }
    const fetchComments = () => {
      const params = {
        type: 1,
        topicId: reactiveData.articleId,
        current: pageInfo.current,
        size: pageInfo.size
      }
      api.getComments(params).then(({ data }) => {
        if (data.flag && data.data) {
          if (reactiveData.isReload) {
            reactiveData.comments = data.data.records
            reactiveData.isReload = false
          } else {
            reactiveData.comments.push(...data.data.records)
          }
          if (data.data.count <= reactiveData.comments.length) {
            reactiveData.haveMore = false
          } else {
            reactiveData.haveMore = true
          }
          pageInfo.current++
        }
      })
    }
    const fetchReplies = (index: any) => {
      api.getRepliesByCommentId(reactiveData.comments[index].id).then(({ data }) => {
        reactiveData.comments[index].replyDTOs = data.data
      })
    }
    const handleAuthorClick = (link: string) => {
      if (!link) link = window.location.href
      window.location.href = link
    }
    const toPageTop = () => {
      window.scrollTo({
        top: 0
      })
    }
    const deleteHTMLTag = (content: any) => {
      return content
        .replace(/<\/?[^>]*>/g, '')
        .replace(/[|]*\n/, '')
        .replace(/&npsp;/gi, '')
    }
    return {
      articleRef,
      ...toRefs(reactiveData),
      isMobile: computed(() => commonStore.isMobile),
      handleAuthorClick,
      handleLike,
      handleCollect,
      loading,
      t
    }
  }
})
</script>
<style lang="scss">
.post-html {
  word-wrap: break-word;
  word-break: break-all;
}
#toc1 {
  max-height: 470px;
  overflow: hidden scroll;
}
#toc1 > ol {
  list-style: none;
  counter-reset: li;
  padding-left: 1.5rem;

  > li {
    @apply font-medium pb-1;
    &.is-active-li > .node-name--H1 {
      @apply text-ob;
    }
    &.is-active-li > .node-name--H2 {
      @apply text-ob;
    }
    &.is-active-li > .node-name--H3 {
      @apply text-ob;
    }
  }

  ol li {
    @apply font-medium mt-1.5 mb-1.5;
    padding-left: 1.5rem;
    &.is-active-li > .node-name--H2 {
      @apply text-ob;
    }
    &.is-active-li > .node-name--H3 {
      @apply text-ob;
    }
    ol li {
      @apply font-medium mt-1.5 mb-1.5;
      padding-left: 1.5rem;
      &.is-active-li .node-name--H3 {
        @apply text-ob;
      }
    }
  }

  ol,
  ol ol {
    position: relative;
  }

  > li::before,
  ol > li::before,
  ol ol > li::before,
  ol ol ol > li::before,
  ol ol ol ol > li::before {
    content: '•';
    color: var(--text-accent);
    display: inline-block;
    width: 1em;
    margin-left: -1.15em;
    padding: 0;
    font-weight: medium;
    text-shadow: 0 0 0.5em var(--accent-2);
  }

  > li::before {
    @apply text-xl;
  }

  > li > ol::before,
  > li > ol > li > ol::before {
    content: '';
    border-left: 1px solid var(--text-accent);
    position: absolute;
    opacity: 0.35;
    left: -1em;
    top: 0;
    bottom: 0;
  }

  > li > ol::before {
    left: -1.25em;
    border-left: 2px solid var(--text-accent);
  }
}
.article-paginator {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;

  @media (min-width: 1024px) {
    flex-direction: row;
    align-items: stretch;
  }
}

.paginator-col {
  display: flex;
  flex-direction: column;
  width: 100%;
  min-width: 0;

  @media (min-width: 1024px) {
    flex: 1 1 0%;
  }

  > * {
    min-width: 0;
  }
}

/* class 落在 ArticleCard 根节点，与 .article-container 是同一元素 */
.pre-and-next-article.article-container {
  height: auto;
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;

  > .article {
    height: auto;
    flex: 1 1 auto;
    min-height: 0;
    display: flex;
    flex-direction: column;
    grid-template-rows: none;
    grid-template-columns: none;
    overflow: hidden;

    > .article-thumbnail {
      flex: 0 0 auto;
      height: 120px;
      min-height: 0;
      max-height: 120px;

      img,
      .thumbnail-screen {
        top: 0;
        left: 0;
        height: 100%;
        width: 100%;
        object-fit: cover;
      }
    }

    > .article-content {
      flex: 1 1 auto;
      min-height: 0;
      display: flex;
      flex-direction: column;
      /* 不要用 overflow:hidden 整块裁掉，否则头像/作者会看不见 */
      overflow: visible;

      > h2,
      > h1,
      > .article-title {
        margin-top: 0.75rem;
        margin-bottom: 0.5rem;
        font-size: 1rem;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      > p {
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        word-break: break-word;
        /* 无摘要时不留大段空白，避免把 footer 挤出可视区 */
        min-height: 0;
        max-height: 2.8em;
        margin-bottom: 0;
        font-size: 0.875rem;
        line-height: 1.4;
      }

      > p.encrypted-content {
        display: block;
        text-align: left;
        font-style: normal;
        min-height: 0;
        max-height: 2.8em;
        line-height: 1.4;
        -webkit-line-clamp: 2;
        display: -webkit-box;
        -webkit-box-orient: vertical;
      }

      > .article-footer {
        margin-top: auto;
        padding-top: 8px;
        flex: 0 0 auto;
      }
    }
  }
}
.article-actions {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin: 2.5rem 0 1rem;
}
.action-button {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.55rem 1.4rem;
  border: 1px solid var(--text-accent);
  border-radius: 9999px;
  background: var(--background-secondary);
  color: var(--text-normal);
  font-size: 0.95rem;
  cursor: pointer;
  transition: transform 180ms cubic-bezier(0.165, 0.84, 0.44, 1), background 180ms ease, color 180ms ease;
  &:hover {
    transform: translateY(-2px);
  }
}
.action-button.action-active {
  background: var(--main-gradient);
  border-color: transparent;
  color: #ffffff;
}
.action-icon {
  width: 1.15em;
  height: 1.15em;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linejoin: round;
}
.action-button.action-active .action-icon {
  fill: currentColor;
}
.action-count {
  font-weight: 600;
}
.icon-pop {
  animation: icon-pop 360ms cubic-bezier(0.165, 0.84, 0.44, 1);
}
@keyframes icon-pop {
  0% {
    transform: scale(1);
  }
  45% {
    transform: scale(1.45);
  }
  100% {
    transform: scale(1);
  }
}
.markdown-body .hljs-center {
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
<style lang="scss" scoped>
.my-gap {
  gap: 1rem;
}

.security ul li::before {
  content: none;
}
.security ul li, .security ol li {
  margin-left: 0.3rem;
  list-style: none;
  line-height: 1;
  margin-bottom: 0.75rem;
}
.security .svg-icon{
  width: 0em;
  height: 0em;
}

.security{
  padding: 1rem 2rem;
  margin-bottom: 2.5rem;
  border-radius: 0.625rem;
  background: var(--grey-2);
  color: var(--grey-6);
  font-family: monospace;
  background-color: var(--background-secondary) !important;
  font-family: 华文细黑;
  font-size: medium;
  font-weight: bold;
  list-style:none;
  line-height: 1;
  margin-bottom: 0.75rem;
  color: var(--text-accent);
}
</style>
