<template>
  <div class="announcement-mgmt-container">
    <el-card style="max-width: 800px; margin: 0 auto;">
      <template #header>
        <div class="header-actions">
          <span>发布全校公告</span>
        </div>
      </template>
      <el-form :model="form" label-width="80px">
        <el-form-item label="公告标题">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio label="NORMAL">普通公告</el-radio>
            <el-radio label="URGENT">紧急通知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="详细内容">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="请输入公告详细内容..." />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="publishing" @click="handlePublish">立即发布</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { publishAnnouncement } from '@/api/sys';
import { useUserStore } from '@/store/user';

const userStore = useUserStore();
const publishing = ref(false);
const form = reactive({
  title: '',
  content: '',
  type: 'NORMAL',
  publishedBy: userStore.userInfo?.id
});

const handlePublish = async () => {
  if (!form.title || !form.content) return ElMessage.warning('请填写完整内容');
  publishing.value = true;
  try {
    const res: any = await publishAnnouncement(form);
    if (res.code === 200) {
      ElMessage.success('公告已成功发布给全校所有用户');
      form.title = '';
      form.content = '';
    }
  } finally {
    publishing.value = false;
  }
};
</script>

<style scoped>
.announcement-mgmt-container { padding: 20px; }
</style>
