<template>
  <div class="user-mgmt-container">
    <el-card>
      <template #header>
        <div class="header-actions">
          <span>全校账号管理</span>
          <div class="search-bar">
            <el-input v-model="queryParams.keyword" placeholder="搜索学工号/姓名" clearable style="width: 250px; margin-right: 10px;" @keyup.enter="fetchData" />
            <el-button type="primary" @click="fetchData">查询</el-button>
            <el-button type="success" @click="handleAdd">新增用户</el-button>
          </div>
        </div>
      </template>

      <el-table :data="users" v-loading="loading">
        <el-table-column prop="username" label="学号/工号" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            <el-tag :type="getRoleTag(scope.row.role)">{{ scope.row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="creditScore" label="信用分" width="100" />
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="warning" @click="handleNotify(scope.row)">发通知</el-button>
            <el-button v-if="scope.row.role === 'VENUE_ADMIN'" link type="success" @click="handlePermission(scope.row)">管辖范围</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size" :total="total" layout="total, prev, pager, next" @current-change="fetchData" />
      </div>
    </el-card>

    <!-- 用户编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="userForm.id ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="userForm" label-width="100px">
        <el-form-item label="学工号">
          <el-input v-model="userForm.username" :disabled="!!userForm.id" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="场地管理员" value="VENUE_ADMIN" />
            <el-option label="系统管理员" value="SYS_ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="userForm.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- 发送通知弹窗 -->
    <el-dialog v-model="notifyVisible" title="单发通知" width="400px">
      <el-form :model="notifyForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="notifyForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="notifyForm.content" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="notifyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNotify">发送</el-button>
      </template>
    </el-dialog>

    <!-- 管辖范围设置弹窗 -->
    <el-dialog v-model="permVisible" title="设置管辖楼宇" width="400px">
      <el-checkbox-group v-model="selectedBuildings">
        <el-checkbox v-for="b in buildings" :key="b.id" :label="b.id">
          {{ b.name }} ({{ b.location }})
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPerm">保存分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getUserPage, saveUser, deleteUser, notifyUser, getBuildingList, getAdminPermissions, assignPermissions } from '@/api/sys';

const users = ref<any[]>([]);
const total = ref(0);
const loading = ref(false);
const buildings = ref<any[]>([]);

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: ''
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res: any = await getUserPage(queryParams);
    if (res.code === 200) {
      users.value = res.data.records;
      total.value = res.data.total;
    }
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
  loadBuildings();
});

const loadBuildings = async () => {
  const res: any = await getBuildingList();
  if (res.code === 200) buildings.value = res.data;
};

const getRoleTag = (role: string) => {
  if (role === 'SYS_ADMIN') return 'danger';
  if (role === 'VENUE_ADMIN') return 'warning';
  return 'info';
};

const dialogVisible = ref(false);
const userForm = reactive({ id: undefined, username: '', realName: '', role: 'STUDENT', phone: '' });

const handleAdd = () => {
  Object.assign(userForm, { id: undefined, username: '', realName: '', role: 'STUDENT', phone: '' });
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  Object.assign(userForm, row);
  dialogVisible.value = true;
};

const submitUser = async () => {
  const res: any = await saveUser(userForm);
  if (res.code === 200) {
    ElMessage.success('操作成功');
    dialogVisible.value = false;
    fetchData();
  }
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定删除该用户吗？').then(async () => {
    const res: any = await deleteUser(id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      fetchData();
    }
  });
};

const notifyVisible = ref(false);
const notifyForm = reactive({ userId: 0, title: '', content: '' });
const handleNotify = (row: any) => {
  notifyForm.userId = row.id;
  notifyForm.title = '系统通知';
  notifyForm.content = '';
  notifyVisible.value = true;
};
const submitNotify = async () => {
  const res: any = await notifyUser(notifyForm);
  if (res.code === 200) {
    ElMessage.success('发送成功');
    notifyVisible.value = false;
  }
};

const permVisible = ref(false);
const currentAdminId = ref(0);
const selectedBuildings = ref<any[]>([]);
const handlePermission = async (row: any) => {
  currentAdminId.value = row.id;
  const res: any = await getAdminPermissions(row.id);
  if (res.code === 200) {
    selectedBuildings.value = res.data.map((item: any) => item.buildingId);
    permVisible.value = true;
  }
};
const submitPerm = async () => {
  const res: any = await assignPermissions({
    adminId: currentAdminId.value,
    buildingIds: selectedBuildings.value
  });
  if (res.code === 200) {
    ElMessage.success('管辖范围分配成功');
    permVisible.value = false;
  }
};
</script>

<style scoped>
.user-mgmt-container { padding: 20px; }
.header-actions { display: flex; justify-content: space-between; align-items: center; }
.search-bar { display: flex; align-items: center; }
</style>
