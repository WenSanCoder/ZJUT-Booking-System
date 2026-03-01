<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">{{ userStore.userInfo?.role === 'SYS_ADMIN' ? '系统管理终端' : '场地管理终端' }}</div>
      <el-menu :default-active="route.path" router background-color="#001529" text-color="#fff">
        
        <!-- 仅场地管理员可见 -->
        <el-menu-item v-if="userStore.userInfo?.role === 'VENUE_ADMIN'" index="/approval">
          <el-icon><Check /></el-icon>
          <span>审批中心</span>
        </el-menu-item>

        <el-menu-item index="/venue">
          <el-icon><Location /></el-icon>
          <span>场地管理</span>
        </el-menu-item>

        <!-- 系统管理员专属 -->
        <template v-if="userStore.userInfo?.role === 'SYS_ADMIN'">
          <el-menu-item index="/sys/user">
            <el-icon><User /></el-icon>
            <span>账号管理</span>
          </el-menu-item>
          <el-menu-item index="/sys/structure">
            <el-icon><House /></el-icon>
            <span>架构管理</span>
          </el-menu-item>
          <el-menu-item index="/sys/announcement">
            <el-icon><Notification /></el-icon>
            <span>发布公告</span>
          </el-menu-item>
        </template>

        <el-menu-item index="/profile">
          <el-icon><Setting /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">控制台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title || '' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="user-name">{{ userStore.userInfo?.realName }}</span>
          <el-button link @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { DataBoard, Check, Location, User, House, Notification, Setting } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const handleLogout = () => {
  userStore.clearAuth();
  router.push('/login');
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.el-aside {
  background-color: #001529;
  color: #fff;
  user-select: none; /* 防止频繁点击时选中文字 */
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
}
.el-header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}
</style>
