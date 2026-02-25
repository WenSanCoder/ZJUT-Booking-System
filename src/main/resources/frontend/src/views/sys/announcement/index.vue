<template>
  <div class="announcement-mgmt-container">
    <el-card>
      <template #header>
        <div class="header-actions">
          <span class="title">公告管理</span>
          <el-button type="primary" @click="openAddDialog">发布公告</el-button>
        </div>
      </template>

      <el-table :data="announcementList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'URGENT' ? 'danger' : 'success'">
              {{ scope.row.type === 'URGENT' ? '紧急' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="scope">
            {{ scope.row.createdAt?.replace('T', ' ').substring(0, 19) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="openEditDialog(scope.row)">修改</el-button>
            <el-popconfirm title="确定删除该公告吗？" @confirm="handleDelete(scope.row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改公告' : '发布公告'"
      width="600px"
    >
      <el-form :model="form" label-width="80px" ref="formRef">
        <el-form-item label="公告标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告类型">
          <el-radio-group v-model="form.type">
            <el-radio label="NORMAL">普通公告</el-radio>
            <el-radio label="URGENT">紧急通知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="详细内容" required>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import {
  getAnnouncementPage,
  publishAnnouncement,
  updateAnnouncement,
  deleteAnnouncement
} from '@/api/announcement';
import { useUserStore } from '@/store/user';

const userStore = useUserStore();
const loading = ref(false);
const submitting = ref(false);
const announcementList = ref([]);
const total = ref(0);
const dialogVisible = ref(false);
const isEdit = ref(false);

const queryParams = reactive({
  current: 1,
  size: 10,
  title: ''
});

const form = reactive({
  id: null,
  title: '',
  content: '',
  type: 'NORMAL',
  publishedBy: userStore.userInfo?.id
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res: any = await getAnnouncementPage(queryParams);
    if (res.code === 200) {
      announcementList.value = res.data.records;
      total.value = res.data.total;
    }
  } finally {
    loading.value = false;
  }
};

const openAddDialog = () => {
  isEdit.value = false;
  form.id = null;
  form.title = '';
  form.content = '';
  form.type = 'NORMAL';
  dialogVisible.value = true;
};

const openEditDialog = (row: any) => {
  isEdit.value = true;
  Object.assign(form, row);
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  if (!form.title || !form.content) {
    return ElMessage.warning('请填写完整内容');
  }
  submitting.value = true;
  try {
    // 确保发布人ID在提交前是最新的
    if (!isEdit.value) {
      form.publishedBy = userStore.userInfo?.id;
    }
    const api = isEdit.value ? updateAnnouncement : publishAnnouncement;
    const res: any = await api(form);
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '发布成功');
      dialogVisible.value = false;
      fetchData();
    }
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (id: number) => {
  const res: any = await deleteAnnouncement(id);
  if (res.code === 200) {
    ElMessage.success('删除成功');
    fetchData();
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.announcement-mgmt-container {
  padding: 20px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 18px;
  font-weight: bold;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
