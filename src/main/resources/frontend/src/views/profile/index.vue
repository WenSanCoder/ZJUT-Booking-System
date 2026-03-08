<template>
  <div class="profile-container">
    <div class="top-cards">
      <el-card v-if="userStore.userInfo?.role === 'VENUE_ADMIN'" class="profile-card">
        <template #header>
          <span class="card-title">电子签名管理</span>
        </template>
        <div class="signature-upload">
          <SignatureUpload 
            v-model="signaturePath"
            @upload-success="handleUploadSuccess"
          />
          <div class="upload-tip">请上传一张背景透明的 PNG 签名照片 (300*100px)</div>
          <p class="desc">该图片将用于生成 PDF 凭证时的自动化 iText 插入点。</p>
        </div>
      </el-card>

      <el-card class="profile-card info-card">
        <template #header>
          <span class="card-title">账号信息</span>
        </template>
        <el-descriptions :column="1" border class="info-descriptions">
          <el-descriptions-item label="姓名">{{ userStore.userInfo?.realName }}</el-descriptions-item>
          <el-descriptions-item label="工号">{{ userStore.userInfo?.username }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ userRole }}</el-descriptions-item>
          <el-descriptions-item label="密码">
            <div class="password-row">
              <span class="password-placeholder">******</span>
              <el-button type="primary" link @click="handleChangePassword">修改密码</el-button>
            </div>
          </el-descriptions-item>
          <el-descriptions-item v-if="userStore.userInfo?.role === 'STUDENT'" label="信用分">
            <el-tag :type="(userStore.userInfo?.creditScore || 0) >= 80 ? 'success' : 'danger'">
              {{ userStore.userInfo?.creditScore }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>

    <el-card class="announcement-card mt-20">
      <template #header>
        <span class="card-title">系统公告</span>
      </template>
      <div v-if="announcements.length > 0" class="announcement-list">
        <div v-for="item in announcements" :key="item.id" class="announcement-item">
          <div class="announcement-header">
            <span class="announcement-title">{{ item.title }}</span>
            <span class="announcement-time">{{ item.createTime || item.publishTime }}</span>
          </div>
          <p class="announcement-content">{{ item.content }}</p>
          <el-divider v-if="item.id !== announcements[announcements.length - 1].id" />
        </div>
      </div>
      <el-empty v-else description="暂无公告" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { useUserStore } from '@/store/user';
import { ElMessage, ElMessageBox } from 'element-plus';
import SignatureUpload from '@/components/SignatureUpload.vue';
import request from '@/api/request';
import { changePassword } from '@/api/user';
import { getLatestAnnouncements } from '@/api/announcement';

const userStore = useUserStore();
const signaturePath = ref(userStore.userInfo?.signatureUrl || '');
const announcements = ref<any[]>([]);

const fetchUserInfo = async () => {
  if (!userStore.userInfo?.id) return;
  try {
    const res = await request.get(`/users/${userStore.userInfo.id}`);
    const userData = res.data?.data || res.data;
    if (userData) {
      userStore.setUserInfo({ ...userStore.userInfo, ...userData });
      signaturePath.value = userData.signatureUrl || '';
    }
  } catch (error: any) {
    console.error('获取用户信息失败:', error);
  }
};

const fetchAnnouncements = async () => {
  try {
    const res = await getLatestAnnouncements({ limit: 5 });
    announcements.value = res.data?.data || res.data || [];
  } catch (error) {
    console.error('获取公告失败:', error);
  }
};

onMounted(() => {
  fetchUserInfo();
  fetchAnnouncements();
});

watch(() => userStore.userInfo?.signatureUrl, (newVal) => {
  signaturePath.value = newVal || '';
}, { immediate: true });

const handleUploadSuccess = async (data: any) => {
  if (!userStore.userInfo?.id) return;
  try {
    await request.post('/users/signature', {
      userId: userStore.userInfo.id,
      signatureUrl: data.path
    });
    userStore.userInfo.signatureUrl = data.path;
    signaturePath.value = data.path;
    ElMessage.success('电子签名已同步至数据库');
  } catch (error: any) {
    ElMessage.error('数据库同步失败: ' + (error.response?.data?.message || error.message));
  }
};

const handleChangePassword = () => {
  ElMessageBox.prompt('请输入旧密码', '修改密码', {
    confirmButtonText: '下一步',
    cancelButtonText: '取消',
    inputType: 'password',
    inputPattern: /\S+/,
    inputErrorMessage: '请输入旧密码'
  }).then(({ value: oldPassword }) => {
    ElMessageBox.prompt('请输入新密码', '修改密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPattern: /^.{6,20}$/,
      inputErrorMessage: '密码长度需在6-20位之间'
    }).then(async ({ value: newPassword }) => {
      try {
        const res: any = await changePassword({
          userId: userStore.userInfo?.id,
          oldPassword: oldPassword,
          newPassword: newPassword
        });
        if (res.code === 200) {
          ElMessage.success('密码修改成功');
        } else {
          ElMessage.error(res.message || '密码修改失败');
        }
      } catch (error: any) {
        ElMessage.error('密码修改失败: ' + (error.response?.data?.message || error.message));
      }
    });
  }).catch(() => {});
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
.profile-container {
  padding: 20px;
}

.top-cards {
  display: flex;
  gap: 20px;
  align-items: stretch;
}

.profile-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.profile-card .el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.info-descriptions {
  flex: 1;
}

:deep(.info-descriptions .el-descriptions__body),
:deep(.info-descriptions table) {
  height: 100%;
}

.announcement-card {
  margin-top: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.password-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.password-placeholder {
  color: #909399;
}

.mt-20 {
  margin-top: 20px;
}

.signature-upload {
  text-align: center;
}

.upload-tip {
  font-size: 14px;
  color: #606266;
  margin-top: 10px;
}

.desc {
  color: #909399;
  font-size: 12px;
  margin: 10px 0;
}

.announcement-item {
  padding: 10px 0;
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.announcement-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.announcement-time {
  font-size: 13px;
  color: #909399;
}

.announcement-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
}

.el-divider--horizontal {
  margin: 12px 0;
}
</style>