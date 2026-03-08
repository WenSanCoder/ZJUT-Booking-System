<template>
  <div class="login-container">
    <div class="login-overlay">
      <div class="login-header">
        <div class="header-content">
          <img src="/zjutlogo.png" alt="ZJUT Logo" class="logo-img">
          <span class="header-divider"></span>
          <span class="header-title">浙江工业大学校园预约系统管理端</span>
        </div>
      </div>
      
      <div class="login-body">
        <el-card class="login-card">
          <div class="card-logo">
            <img src="/zjutlogo.png" alt="ZJUT Logo">
            <span>浙江工业大学校园预约系统管理端</span>
          </div>
          <el-form :model="loginForm">
            <el-form-item>
              <el-input 
                v-model="loginForm.username" 
                placeholder="管理员账号" 
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item>
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="密码" 
                show-password 
                :prefix-icon="Lock"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <div class="form-options">
              <span class="forgot-password">忘记登录密码？</span>
            </div>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleLogin" class="login-submit-btn">登录</el-button>
            </el-form-item>
            <div class="footer-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            </div>
          </el-form>
        </el-card>
      </div>

      <div class="login-footer">
        <p>浙江省杭州市下城区潮王路18号 邮编:310014 版权所有 浙江工业大学</p>
        <p>信息化办公室维护 | 建议浏览器使用IE8以上或者360极速模式</p>
      </div>
    </div>

    <!-- 首次登录强制改密弹窗 -->
    <el-dialog v-model="showChangePassword" title="首次登录密码修改" width="450px" :close-on-click-modal="false" :show-close="false">
      <el-form :model="passwordForm" label-width="100px" style="padding-top: 20px;">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitting" @click="submitPasswordChange" style="width: 100%">确定修改并重新登录</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { login, changePassword } from '@/api/user';
import { useUserStore } from '@/store/user';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const submitting = ref(false);
const showChangePassword = ref(false);
const rememberMe = ref(false);

const loginForm = reactive({
  username: '',
  password: ''
});

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
});

let tempUserId: number | null = null;

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    return ElMessage.warning('请输入用户名和密码');
  }
  loading.value = true;
  try {
    const res: any = await login(loginForm);
    if (res.code === 200) {
      const user = res.data;
      if (user.passwordChanged === 0) {
        tempUserId = user.id;
        showChangePassword.value = true;
      } else {
        completeLogin(user);
      }
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const completeLogin = (user: any) => {
  userStore.setUserInfo(user);
  userStore.setToken('dummy-token-' + user.id);
  ElMessage.success('登录成功');
  
  // 场地管理员重定向到审批中心，系统管理员重定向到账号管理，其他重定向到个人中心
  if (user.role === 'VENUE_ADMIN') {
    router.push('/approval');
  } else if (user.role === 'SYS_ADMIN') {
    router.push('/sys/user');
  } else {
    router.push('/profile');
  }
};

const submitPasswordChange = async () => {
  if (!passwordForm.newPassword) return ElMessage.warning('请输入新密码');
  if (passwordForm.newPassword.length < 6) return ElMessage.warning('密码长度不能少于6位');
  if (passwordForm.newPassword === '123456') return ElMessage.warning('不能使用默认密码');
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    return ElMessage.warning('两次密码输入不一致');
  }
  
  submitting.value = true;
  try {
    const res: any = await changePassword({
      userId: tempUserId,
      oldPassword: loginForm.password,
      newPassword: passwordForm.newPassword
    });
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请使用新密码登录');
      showChangePassword.value = false;
      loginForm.password = ''; // 清空旧密码
      passwordForm.newPassword = '';
      passwordForm.confirmPassword = '';
    }
  } catch (error) {
    console.error(error);
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  max-height: 100vh;
  max-width: 100vw;
  background-image: url('/zjutbg.jpeg');
  background-size: cover;
  background-position: center;
  position: fixed; /* 改为固定定位，确保不被内容撑开 */
  top: 0;
  left: 0;
  display: flex;
  overflow: hidden;
}

.login-overlay {
  width: 100%;
  height: 100vh; /* 强制填满视口高度 */
  background-color: rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.login-header {
  height: 60px; /* 稍微缩小高度 */
  flex-shrink: 0; /* 禁止压缩 */
  background-color: #03488e;
  padding: 0 40px;
  display: flex;
  align-items: center;
}

.header-content {
  display: flex;
  align-items: center;
  color: #fff;
}

.logo-img {
  height: 40px;
}

.header-divider {
  width: 1px;
  height: 30px;
  background-color: rgba(255, 255, 255, 0.4);
  margin: 0 20px;
}

.header-title {
  font-size: 22px;
  font-weight: 500;
  letter-spacing: 1px;
}

.login-body {
  flex: 1;
  display: flex;
  align-items: center;
  padding-left: 10%; /* 将卡片向左侧放置 */
}

.login-card {
  width: 380px;
  padding: 30px 20px;
  background: rgba(255, 255, 255, 0.95);
  border: none;
  border-radius: 4px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.card-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
}

.card-logo img {
  height: 30px;
  margin-right: 10px;
}

.card-logo span {
  font-size: 18px;
  color: #03488e;
  font-weight: bold;
}

:deep(.el-input__wrapper) {
  border-radius: 0;
  border-top: none;
  border-left: none;
  border-right: none;
  box-shadow: none !important;
  border-bottom: 1px solid #dcdfe6;
  padding: 0;
}

:deep(.el-input__inner) {
  height: 45px;
}

.form-options {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.forgot-password {
  color: #909399;
  font-size: 13px;
  cursor: pointer;
}

.login-submit-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  background-color: #409eff;
  border: none;
  border-radius: 4px;
}

.footer-options {
  margin-top: 10px;
  display: flex;
  align-items: center;
}

.login-footer {
  flex-shrink: 0; /* 禁止压缩 */
  background-color: #03488e;
  color: #fff;
  text-align: center;
  padding: 10px 0; /* 减小内边距 */
  font-size: 12px;
  line-height: 1.5;
}

.login-footer p {
  margin: 0;
}

.dialog-footer {
  padding: 0 20px 20px;
}
</style>
