<template>
  <div class="profile-container">
    <el-card v-if="userStore.userInfo?.role === 'VENUE_ADMIN'" header="电子签名管理" style="max-width: 600px; margin: 0 auto;">
      <div class="signature-upload">
        <SignatureUpload 
          v-model="signaturePath"
          @upload-success="handleUploadSuccess"
        />
        <div class="upload-tip">请上传一张背景透明的 PNG 签名照片 (300*100px)</div>
        <p class="desc">该图片将用于生成 PDF 凭证时的自动化 iText 插入点。</p>
      </div>
    </el-card>

    <el-card header="账号信息" style="max-width: 600px; margin: 20px auto 0;">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="姓名">{{ userStore.userInfo?.realName }}</el-descriptions-item>
        <el-descriptions-item label="工号">{{ userStore.userInfo?.username }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ userRole }}</el-descriptions-item>
        <el-descriptions-item v-if="userStore.userInfo?.role === 'STUDENT'" label="信用分">{{ userStore.userInfo?.creditScore }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useUserStore } from '@/store/user';
import { ElMessage } from 'element-plus';
import SignatureUpload from '@/components/SignatureUpload.vue';
import axios from 'axios';

const userStore = useUserStore();
const signaturePath = ref(userStore.userInfo?.signatureUrl || '');

// 监听 store 变化，确保初始化
watch(() => userStore.userInfo?.signatureUrl, (newVal) => {
  if (newVal) signaturePath.value = newVal;
}, { immediate: true });

const handleUploadSuccess = async (data: any) => {
  if (!userStore.userInfo?.id) return;
  
  try {
    // 调用更新数据库接口
    await axios.post('/api/users/signature', {
      userId: userStore.userInfo.id,
      signatureUrl: data.path
    });
    
    // 更新本地 store 状态
    userStore.userInfo.signatureUrl = data.path;
    signaturePath.value = data.path; // 同步更新本地 ref
    ElMessage.success('电子签名已同步至数据库');
  } catch (error: any) {
    ElMessage.error('数据库同步失败: ' + (error.response?.data?.message || error.message));
  }
};

const getRoleName = (role: string) => {
  const map: any = {
    SYS_ADMIN: '系统管理员',
    VENUE_ADMIN: '场地管理员',
    STUDENT: '学生',
    TEACHER: '教师'
  };
  return map[role] || role;
};

const userRole = getRoleName(userStore.userInfo?.role || '');
</script>

<style scoped>
.profile-container { padding: 20px; }
.signature-upload { text-align: center; }
.signature-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  margin: 0 auto 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.signature-img { width: 100%; height: auto; display: block; }
.uploader-icon { font-size: 28px; color: #8c939d; }
.upload-tip { font-size: 14px; color: #606266; margin-top: 10px; }
.desc { color: #909399; font-size: 12px; margin: 10px 0; }
</style>
