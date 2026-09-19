<template>
  <div>
    <Breadcrumb :current="t('menu.friends')" />
    <div class="flex flex-col">
      <div class="post-header">
        <h1 class="post-title text-white uppercase">{{ t('titles.friends') }}</h1>
      </div>
      <div class="main-grid">
        <div class="relative space-y-5">
          <div class="bg-ob-deep-800 p-4 lg:p-14 rounded-2xl shadow-xl mb-8 lg:mb-0">
            <el-row :gutter="36">
              <template v-for="link in links" :key="link.id">
                <el-col :span="8" :xs="{ span: 20, offset: 2 }" class="mb-3">
                  <el-card shadow="never" class="shadow-md">
                    <div class="block">
                      <el-avatar :size="60" :src="link.linkAvatar" />
                    </div>
                    <div class="info">
                      <a :href="link.linkAddress" target="_blank">
                        <div class="link-name font-semibold">{{ link.linkName }}</div>
                      </a>
                      <div class="link-intro truncate">{{ link.linkIntro }}</div>
                    </div>
                  </el-card>
                </el-col>
              </template>
            </el-row>
          </div>
          <div class="post-html text-center">
            <p>{{ t('friends.tip-1') }}</p>
            <p>{{ t('friends.tip-2') }}</p>
            <button class="apply-button" @click="linkState.showLinkForm = true">
              {{ t('friends.apply-btn') }}
            </button>
          </div>
          <Comment />
        </div>
        <div class="col-span-1">
          <Sidebar>
            <Profile />
          </Sidebar>
        </div>
      </div>
    </div>
    <!-- 申请友链对话框 -->
    <el-dialog v-model="linkState.showLinkForm" width="30%" :fullscreen="isMobile">
      <div class="dialog-title">{{ t('friends.apply-title') }}</div>
      <el-form ref="applyForm" :model="linkState.linkForm" :rules="linkState.rules">
        <el-form-item prop="name">
          <div class="input-label">{{ t('friends.name') }}</div>
          <el-input v-model="linkState.linkForm.name" :placeholder="t('friends.name-ph')"></el-input>
        </el-form-item>
        <el-form-item prop="avatar">
          <div class="input-label">{{ t('friends.avatar') }}</div>
          <div class="flex items-center gap-2">
            <el-input 
              v-model="linkState.linkForm.avatar" 
              :placeholder="t('friends.avatar-ph')"
              class="flex-1">
            </el-input>
            <el-upload
              action="/api/links/avatar"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :on-success="handleAvatarSuccess"
              :on-error="handleUploadError"
              accept="image/jpeg,image/jpg,image/png,image/gif">
              <el-button type="primary" size="default" :loading="linkState.uploading">
                {{ linkState.uploading ? t('friends.uploading') : t('friends.upload') }}
              </el-button>
            </el-upload>
          </div>
          <div v-if="linkState.linkForm.avatar" class="mt-2">
            <el-avatar :size="60" :src="linkState.linkForm.avatar" />
          </div>
        </el-form-item>
        <el-form-item prop="address">
          <div class="input-label">{{ t('friends.address') }}</div>
          <el-input v-model="linkState.linkForm.address" :placeholder="t('friends.address-ph')"></el-input>
        </el-form-item>
        <el-form-item prop="intro">
          <div class="input-label">{{ t('friends.intro') }}</div>
          <el-input v-model="linkState.linkForm.intro" :placeholder="t('friends.intro-ph')"></el-input>
        </el-form-item>
        <el-form-item class="mt-6">
          <el-button
              type="primary"
              size="large"
              class="mx-auto"
              :disabled="linkState.isSubmitting"
              @click="submitLink"
          >
            {{ linkState.isSubmitting ? t('friends.submitting') : t('friends.submit') }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button class="mx-auto cancel-btn" @click="linkState.showLinkForm = false">{{ t('friends.cancel') }}</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, provide, computed, toRefs, onMounted, onUnmounted, getCurrentInstance } from 'vue';
import { useI18n } from 'vue-i18n';
import { Sidebar, Profile } from '../components/Sidebar';
import Breadcrumb from '@/components/Breadcrumb.vue';
import { Comment } from '../components/Comment';
import { useCommentStore } from '@/stores/comment';
import { useCommonStore } from '@/stores/common';
import emitter from '@/utils/mitt';
import api from '@/api/api';
export default defineComponent({
  name: 'FriendLink',
  components: { Sidebar, Profile, Breadcrumb, Comment },
  setup() {
    const { t } = useI18n();
    const commentStore = useCommentStore();
    const commonStore = useCommonStore();
    const proxy: any = getCurrentInstance()?.appContext.config.globalProperties;
    const reactiveData = reactive({
      links: '' as any,
      comments: [] as any,
      haveMore: false as any,
      isReload: false as any
    });
    const linkState = reactive({
      isSubmitting: false, // 添加提交状态
      uploading: false, // 上传状态
      showLinkForm: false,
      linkForm: {
        name: '',
        avatar: '',
        address: '',
        intro: '',
        isreview: 0
      },
      rules: {
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' },
          { min: 2, max: 20, message: '名称长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        avatar: [
          { required: true, message: '请输入头像链接', trigger: 'blur' },
          {
            pattern: /^https?:\/\/.+/i,
            message: '请输入有效的图片URL（http://或https://开头）',
            trigger: 'blur'
          }
        ],
        address: [
          { required: true, message: '请输入链接', trigger: 'blur' },
          {
            pattern: /^https?:\/\/.+$/i,
            message: '请输入有效的URL或者域名（支持http://或https://）',
            trigger: 'blur'
          }
        ],
        intro: [
          { required: true, message: '请输入介绍', trigger: 'blur' },
          { min: 5, max: 25, message: '介绍长度在 5 到 25 个字符', trigger: 'blur' }
        ]
      }
    });

    const pageInfo = reactive({
      current: 1,
      size: 7
    });
    commentStore.type = 4;
    onMounted(() => {
      fetchLinks();
      fetchComments();
    });
    provide(
        'comments',
        computed(() => reactiveData.comments)
    );
    provide(
        'haveMore',
        computed(() => reactiveData.haveMore)
    );
    const handleFetchComment = () => {
      pageInfo.current = 1;
      reactiveData.isReload = true;
      fetchComments();
    };
    const handleFetchReplies = (index: any) => {
      fetchReplies(index);
    };
    const handleLoadMore = () => {
      fetchComments();
    };
    emitter.on('friendLinkFetchComment', handleFetchComment);
    emitter.on('friendLinkFetchReplies', handleFetchReplies);
    emitter.on('friendLinkLoadMore', handleLoadMore);
    onUnmounted(() => {
      emitter.off('friendLinkFetchComment', handleFetchComment);
      emitter.off('friendLinkFetchReplies', handleFetchReplies);
      emitter.off('friendLinkLoadMore', handleLoadMore);
    });

    const fetchLinks = () => {
      api.getFriendLink().then(({ data }) => {
        reactiveData.links = data.data;
      });
    };

    const fetchComments = () => {
      const params = {
        type: 4,
        topicId: null,
        current: pageInfo.current,
        size: pageInfo.size
      };
      api.getComments(params).then(({ data }) => {
        if (reactiveData.isReload) {
          reactiveData.comments = data.data.records;
          reactiveData.isReload = false;
        } else {
          reactiveData.comments.push(...data.data.records);
        }
        if (data.data.count <= reactiveData.comments.length) {
          reactiveData.haveMore = false;
        } else {
          reactiveData.haveMore = true;
        }
        pageInfo.current++;
      });
    };

    const fetchReplies = (index: any) => {
      api.getRepliesByCommentId(reactiveData.comments[index].id).then(({ data }) => {
        reactiveData.comments[index].replyDTOs = data.data;
      });
    };
    const submitLink = () => {
      const { name, avatar, address, intro } = linkState.linkForm;

      // 简单的表单验证
      if (!name || name.length < 2 || name.length > 20) {
        proxy.$notify({
          title: 'Warning',
          message: '名称长度在 2 到 20 个字符',
          type: 'warning'
        });
        return;
      }

      if (!avatar || !/^https?:\/\/.+/i.test(avatar)) {
        proxy.$notify({
          title: 'Warning',
          message: '请输入有效的图片URL（http://或https://开头）',
          type: 'warning'
        });
        return;
      }

      if (!address || !/^https?:\/\/.+$/i.test(address)) {
        proxy.$notify({
          title: 'Warning',
          message: '请输入有效的URL或者域名',
          type: 'warning'
        });
        return;
      }

      if (!intro || intro.length < 5 || intro.length > 25) {
        proxy.$notify({
          title: 'Warning',
          message: '介绍长度在 5 到 25 个字符',
          type: 'warning'
        });
        return;
      }
      // 表单验证通过，设置提交状态
      linkState.isSubmitting = true;

      // 表单验证通过，提交申请
      const params = {
        linkName: name,
        linkAvatar: avatar,
        linkAddress: address,
        linkIntro: intro,
        isReview: linkState.linkForm.isreview
      };

      api.submitFriendLink(params).then((response) => {
        if (response.data.flag) {
          linkState.showLinkForm = false;
          // 重置表单
          linkState.linkForm.name = '';
          linkState.linkForm.avatar = '';
          linkState.linkForm.address = '';
          linkState.linkForm.intro = '';
          proxy.$notify({
            title: 'Success',
            message: '申请成功，我们会尽快处理您的申请。',
            type: 'success'
          });
        } else {
          proxy.$notify({
            title: 'Error',
            message: '申请失败，请稍后再试。',
            type: 'error'
          });
        }
      }).catch((error) => {
        console.error('提交申请时发生错误:', error);
        proxy.$notify({
          title: 'Error',
          message: '申请失败，请稍后再试。',
          type: 'error'
        });
      }).finally(() => {
        // 无论成功或失败，都重置提交状态
        linkState.isSubmitting = false;
      });
    };

    // 上传前验证
    const beforeAvatarUpload = (file: any) => {
      const isImage = /^image\/(jpeg|jpg|png|gif)$/.test(file.type);
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isImage) {
        proxy.$notify({
          title: 'Warning',
          message: '只能上传 JPG/PNG/GIF 格式的图片',
          type: 'warning'
        });
        return false;
      }
      if (!isLt2M) {
        proxy.$notify({
          title: 'Warning',
          message: '图片大小不能超过 2MB',
          type: 'warning'
        });
        return false;
      }
      linkState.uploading = true;
      return true;
    };

    // 上传成功
    const handleAvatarSuccess = (response: any) => {
      linkState.uploading = false;
      if (response.flag) {
        linkState.linkForm.avatar = response.data;
        proxy.$notify({
          title: 'Success',
          message: '图片上传成功',
          type: 'success'
        });
      } else {
        proxy.$notify({
          title: 'Error',
          message: response.message || '图片上传失败',
          type: 'error'
        });
      }
    };

    // 上传失败
    const handleUploadError = () => {
      linkState.uploading = false;
      proxy.$notify({
        title: 'Error',
        message: '图片上传失败，请重试',
        type: 'error'
      });
    };

    return {
      ...toRefs(reactiveData),
      t,
      linkState,
      isMobile: computed(() => commonStore.isMobile),
      submitLink,
      beforeAvatarUpload,
      handleAvatarSuccess,
      handleUploadError
    };
  }
});
</script>

<style lang="scss" scoped>
.block {
  display: inline-block;
  width: 24%;
}
.info {
  display: inline-block;
  width: 76%;
  height: 100%;
}
.link-name {
  margin-left: 20px;
  margin-bottom: 5px;
  margin-top: 2px;
  color: var(--text-normal);
  font-size: large;
}
.link-intro {
  margin-left: 20px;
  margin-bottom: 1px;
  color: var(--text-normal);
}
.el-card {
  background: var(--background-primary);
  border-radius: 10px;
  border: 0;
}
.apply-button {
  display: block;
  margin: 20px auto;
  padding: 10px 32px;
  background: var(--strong-gradient);
  color: #fff;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.apply-button:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}
</style>

