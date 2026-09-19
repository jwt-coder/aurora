<template>
  <div>
    <div class="flex space-x-3 xl:space-x-5">
      <Avatar :url="reply.avatar" />
      <div class="reply bg-white flex flex-col p-3 rounded-md relative shadow-md">
        <p class="commentContent">
          <a
            v-if="showReplyLink && reply.replyWebsite"
            :href="reply.replyWebsite"
            target="_blank"
            class="reply-link">@{{ reply.replyNickname }}&nbsp;</a>
          <span v-else-if="showReplyLink">@{{ reply.replyNickname }}&nbsp;</span>
          <span v-html="(commentContent || '').replaceAll('\n', '<br>')" />
        </p>
        <div class="flex justify-between mt-2 text-xs text-gray-400 space-x-3 md:space-x-16">
          <span> {{ reply.nickname }} | {{ time }}</span>
          <div>
            <span @click="clickOnSonReply" class="cursor-pointer reply-button">Reply</span>
          </div>
        </div>
      </div>
    </div>
    <a href="" target="_blank"></a>
    <CommentReplyForm
      class="mt-5"
      v-show="show"
      :replyUserId="reply.userId"
      :initialContent="replyContent"
      @changeShow="changeShow" />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, toRefs } from 'vue'
import Avatar from '@/components/Avatar.vue'
import CommentReplyForm from './CommentReplyForm.vue'

export default defineComponent({
  components: {
    Avatar,
    CommentReplyForm
  },
  props: ['reply', 'commentUserId'],
  setup(props) {
    const formatTime = (time: any): any => {
      let date = new Date(time)
      let year = date.getFullYear()
      let month = date.getMonth() + 1
      let day = date.getDate()
      return year + '-' + month + '-' + day
    }
    const reactiveData = reactive({
      replyContent: '' as any,
      time: formatTime(props.reply.createTime) as any,
      show: false as any
    })
    const clickOnSonReply = () => {
      reactiveData.replyContent = '@' + props.reply.nickname
      reactiveData.show = true
    }
    const changeShow = () => {
      reactiveData.show = false
    }
    const showReplyLink = computed(() => props.reply.replyUserId !== props.commentUserId)
    const commentContent = computed(() => props.reply.commentContent)
    return {
      ...toRefs(reactiveData),
      commentContent,
      showReplyLink,
      clickOnSonReply,
      changeShow
    }
  }
})
</script>
<style lang="scss" scoped>
.reply::before {
  content: '';
  position: absolute;
  width: 0;
  height: 0;
  border-right: 8px solid var(--background-primary);
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  left: -8px;
  top: 14px;
}
.reply {
  background: var(--background-primary);
}
.reply-button {
  color: var(--text-accent);
}
.commentContent {
  line-height: 26px;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: break-all;
}
</style>
<style lang="scss">
.reply-link {
  color: var(--text-accent);
}
</style>
