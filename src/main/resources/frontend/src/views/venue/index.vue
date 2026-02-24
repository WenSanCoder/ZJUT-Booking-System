<template>
  <div class="venue-container">
    <el-card>
      <template #header>
        <div class="header-actions">
          <span>管辖场地列表</span>
          <div class="search-bar">
            <el-input 
              v-model="queryParams.name" 
              placeholder="搜索场地名称" 
              clearable 
              style="width: 200px; margin-right: 10px;"
              @clear="fetchData"
              @keyup.enter="fetchData"
            />
            <el-button type="primary" @click="fetchData">查询</el-button>
            <el-button type="success" @click="handleAdd">新增场地</el-button>
          </div>
        </div>
      </template>
      <el-table :data="venues" v-loading="loading">
        <el-table-column prop="name" label="场地名称" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="capacity" label="最大容量" />
        <el-table-column label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '开放' : '已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="warning" @click="handleLock(scope.row)">分时锁定</el-button>
            <el-button link type="danger" @click="handleStatus(scope.row)">{{ scope.row.status === 1 ? '禁用' : '启用' }}</el-button>
            <el-button link type="info" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="venueForm.id ? '编辑场地' : '新增场地'" width="600px">
      <el-form :model="venueForm" label-width="100px">
        <el-form-item label="场地名称">
          <el-input v-model="venueForm.name" />
        </el-form-item>
        <el-form-item label="场地类型">
          <el-select v-model="venueForm.type" placeholder="请选择">
            <el-option label="报告厅" value="报告厅" />
            <el-option label="体育场" value="体育场" />
            <el-option label="研讨室" value="研讨室" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细位置">
          <el-input v-model="venueForm.location" />
        </el-form-item>
        <el-form-item label="最大容量">
          <el-input-number v-model="venueForm.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="设备清单">
          <el-input v-model="venueForm.equipment" type="textarea" placeholder="如：多媒体投影、音响、麦克风" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 锁定/维护弹窗 -->
    <el-dialog v-model="lockVisible" title="设置场地不可用时间段" width="500px">
      <el-form label-width="80px">
        <el-form-item label="时间范围">
          <el-date-picker v-model="lockForm.range" type="datetimerange" range-separator="至" />
        </el-form-item>
        <el-form-item label="不可用原因">
          <el-input v-model="lockForm.reason" type="textarea" placeholder="如：周五 14:00-16:00 维修" />
        </el-form-item>
        <div class="tips" style="color: #f56c6c; font-size: 12px; margin-bottom: 10px;">
          * 提交后，系统将自动取消该时段内的预约，并发送通知给用户。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="lockVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLock">启动异步维护逻辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getVenuePage, saveVenue, deleteVenue, updateVenueStatus } from '@/api/admin';
import { useUserStore } from '@/store/user';
import dayjs from 'dayjs';

const userStore = useUserStore();

const venues = ref<any[]>([]);
const total = ref(0);
const loading = ref(false);
const queryParams = reactive({
  current: 1,
  size: 10,
  name: ''
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res: any = await getVenuePage({ ...queryParams, userId: userStore.userInfo?.id });
    if (res.code === 200) {
      venues.value = res.data.records;
      total.value = res.data.total;
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
});

const dialogVisible = ref(false);
const venueForm = reactive({
  id: undefined,
  name: '',
  type: '',
  location: '',
  capacity: 0,
  equipment: '',
  status: 1
});

const handleAdd = () => {
  Object.assign(venueForm, { id: undefined, name: '', type: '', location: '', capacity: 0, equipment: '', status: 1 });
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  Object.assign(venueForm, row);
  dialogVisible.value = true;
};

const submitForm = async () => {
  const res: any = await saveVenue(venueForm);
  if (res.code === 200) {
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    fetchData();
  }
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定删除该场地吗？').then(async () => {
    const res: any = await deleteVenue(id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      fetchData();
    }
  });
};

const lockVisible = ref(false);
const currentVenueId = ref<number | null>(null);
const lockForm = reactive({ range: [] as any[], reason: '' });

const handleLock = (row: any) => {
  currentVenueId.value = row.id;
  lockForm.range = [];
  lockForm.reason = '';
  lockVisible.value = true;
};

const submitLock = async () => {
  if (!lockForm.range || lockForm.range.length < 2) return ElMessage.warning('请选择时间范围');
  const res: any = await updateVenueStatus({
    id: currentVenueId.value,
    startTime: dayjs(lockForm.range[0]).format('YYYY-MM-DDTHH:mm:ss'),
    endTime: dayjs(lockForm.range[1]).format('YYYY-MM-DDTHH:mm:ss'),
    reason: lockForm.reason
  });
  if (res.code === 200) {
    ElMessage.success('分时锁定成功，异步任务已启动');
    lockVisible.value = false;
    fetchData();
  }
};

const handleStatus = async (row: any) => {
  const newStatus = row.status === 1 ? 0 : 1;
  const statusStr = newStatus === 1 ? '启用' : '禁用';
  ElMessageBox.confirm(`确认${statusStr}${row.name}？`).then(async () => {
    const res: any = await updateVenueStatus({ id: row.id, status: newStatus });
    if (res.code === 200) {
      ElMessage.success('切换成功');
      fetchData();
    }
  });
};
</script>

<style scoped>
.venue-container { padding: 20px; }
.header-actions { display: flex; justify-content: space-between; align-items: center; }
</style>
