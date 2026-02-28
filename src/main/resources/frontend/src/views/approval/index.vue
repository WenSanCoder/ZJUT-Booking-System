<template>
  <div class="approval-container">
    <el-card>
      <template #header>
        <div class="search-bar">
          <el-input v-model="queryParams.applicant" placeholder="搜索申请人学号..." style="width: 200px" />
          <el-select v-model="queryParams.status" placeholder="审核状态" clearable style="width: 150px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
          <el-date-picker v-model="queryParams.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="applicantName" label="申请人" width="120">
          <template #default="scope">
            {{ scope.row.applicantName }} ({{ scope.row.applicantUsername }})
          </template>
        </el-table-column>
        <el-table-column prop="venueName" label="申请场地" />
        <el-table-column label="活动时间" width="220">
          <template #default="scope">
            {{ formatTimeRange(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="viewDetail(scope.row)">详情</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" link type="success" @click="handleAction(scope.row, 'approve')">通过</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" link type="danger" @click="handleAction(scope.row, 'reject')">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="600px">
      <div v-if="currentRecord" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="活动名称">{{ currentRecord.activityName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentRecord.applicantName }} ({{ currentRecord.applicantUsername }})</el-descriptions-item>
          <el-descriptions-item label="组织单位">{{ currentRecord.organizer }}</el-descriptions-item>
          <el-descriptions-item label="负责人员">{{ currentRecord.contactName || '无' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentRecord.contactPhone || currentRecord.phone }}</el-descriptions-item>
          <el-descriptions-item label="预计人数">{{ currentRecord.expectedPeople }}</el-descriptions-item>
          <el-descriptions-item label="活动内容">{{ currentRecord.description || currentRecord.remark }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRecord.status === 'REJECTED'" label="驳回理由">
            <span style="color: #f56c6c">{{ currentRecord.rejectReason }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="附件材料">
            <el-link v-if="currentRecord.planUrl" type="primary" :href="currentRecord.planUrl" target="_blank">查看策划书</el-link>
            <span v-else>无</span>
            <span v-if="currentRecord.attachment" style="margin-left: 10px;">
              <el-link type="primary" :href="currentRecord.attachment" target="_blank">其他附件</el-link>
            </span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentRecord?.status === 'PENDING'" type="danger" @click="handleAction(currentRecord, 'reject')">驳回申请</el-button>
        <el-button v-if="currentRecord?.status === 'PENDING'" type="primary" @click="handleAction(currentRecord, 'approve')">通过审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import { getBookingPage, approveBooking, rejectBooking } from '@/api/admin';
import { useUserStore } from '@/store/user';
import dayjs from 'dayjs';

const userStore = useUserStore();

const queryParams = reactive({
  current: 1,
  size: 10,
  applicant: '',
  status: 'PENDING',
  dateRange: [] as any[]
});

const total = ref(0);
const tableData = ref<any[]>([]);
const loading = ref(false);

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      ...queryParams,
      userId: userStore.userInfo?.id,
      startDate: queryParams.dateRange?.[0] ? dayjs(queryParams.dateRange[0]).format('YYYY-MM-DD HH:mm:ss') : '',
      endDate: queryParams.dateRange?.[1] ? dayjs(queryParams.dateRange[1]).format('YYYY-MM-DD HH:mm:ss') : ''
    };
    delete params.dateRange;
    
    const res: any = await getBookingPage(params);
    if (res.code === 200) {
      tableData.value = res.data.records;
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

const formatTimeRange = (row: any) => {
  return `${dayjs(row.startTime).format('YYYY-MM-DD HH:mm')} - ${dayjs(row.endTime).format('HH:mm')}`;
};

const statusType = (status: string) => {
  const map: any = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' };
  return map[status];
};

const statusText = (status: string) => {
  const map: any = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', CANCELLED: '已取消' };
  return map[status];
};

const handleSearch = () => {
  queryParams.current = 1;
  fetchData();
};

const detailVisible = ref(false);
const currentRecord = ref<any>(null);

const viewDetail = (row: any) => {
  currentRecord.value = row;
  detailVisible.value = true;
};

const handleAction = (row: any, action: 'approve' | 'reject') => {
  if (action === 'reject') {
    ElMessageBox.prompt('请输入驳回原因', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '驳回原因不能为空'
    }).then(async ({ value }) => {
      const res: any = await rejectBooking(row.id, { reason: value }, userStore.userInfo?.id);
      if (res.code === 200) {
        ElMessage.success('已驳回并推送消息给学生');
        fetchData();
        detailVisible.value = false;
      }
    });
  } else {
    ElMessageBox.confirm('确认通过该申请？系统将自动合成带签名的PDF凭证。', '提示').then(async () => {
      const res: any = await approveBooking(row.id, userStore.userInfo?.id);
      if (res.code === 200) {
        ElMessage.success('审批通过');
        fetchData();
        detailVisible.value = false;
      }
    });
  }
};
</script>

<style scoped>
.approval-container { padding: 20px; }
.search-bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.detail-content p { margin-bottom: 15px; font-size: 16px; }
</style>
