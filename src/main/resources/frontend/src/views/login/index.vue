<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>场地管理系统登录</span>
        </div>
      </template>
      <el-form :model="loginForm" label-width="80px">
        <el-form-item label="学号/工号">
          <el-input v-model="loginForm.username" placeholder="请输入学号/工号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>

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

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const submitting = ref(false);
const showChangePassword = ref(false);

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
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}
.login-card {
  width: 420px;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}
.card-header {
  text-align: center;
  font-size: 1.25rem;
  font-weight: 600;
  color: #303133;
}
.dialog-footer {
  padding: 0 20px 20px;
}
</style>
