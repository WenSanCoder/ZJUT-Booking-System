<template>
  <div class="profile-container">
    <el-card header="电子签名管理" style="max-width: 600px; margin: 0 auto;">
      <div class="signature-upload">
        <el-upload
          class="signature-uploader"
          action="#"
          :show-file-list="false"
          :auto-upload="false"
          :on-change="handleFileChange"
        >
          <img v-if="signatureUrl" :src="signatureUrl" class="signature-img" />
          <el-icon v-else class="uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">请上传一张背景透明的 PNG 签名照片</div>
        <p class="desc">该图片将用于生成 PDF 凭证时的自动化 iText 插入点。</p>
        <el-button type="primary" @click="saveSignature" :disabled="!newFile">保存上传</el-button>
      </div>
    </el-card>

    <el-card header="账号信息" style="max-width: 600px; margin: 20px auto 0;">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="姓名">{{ userStore.userInfo?.realName }}</el-descriptions-item>
        <el-descriptions-item label="工号">{{ userStore.userInfo?.username }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ userRole }}</el-descriptions-item>
        <el-descriptions-item label="信用分">{{ userStore.userInfo?.creditScore }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useUserStore } from '@/store/user';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();
const signatureUrl = ref(userStore.userInfo?.signatureUrl || '');
const newFile = ref<File | null>(null);

const userRole = userStore.userInfo?.role === 'VENUE_ADMIN' ? '场地管理员' : '普通用户';

const handleFileChange = (file: any) => {
  const isPNG = file.raw.type === 'image/png';
  if (!isPNG) {
    ElMessage.error('请上传 PNG 格式的图片!');
    return false;
  }
  newFile.value = file.raw;
  signatureUrl.value = URL.createObjectURL(file.raw);
};

const saveSignature = () => {
  // 模拟上传
  ElMessage.success('签名上传成功');
  if (userStore.userInfo) {
    userStore.userInfo.signatureUrl = signatureUrl.value;
  }
};
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
