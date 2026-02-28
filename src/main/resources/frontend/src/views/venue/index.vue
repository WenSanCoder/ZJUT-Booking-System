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
          </div>
        </div>
      </template>
      <el-table 
        :data="venues" 
        v-loading="loading"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.isVenue" size="small">
              {{ scope.row.type === 'SEMINAR' ? '研讨室' : 
                 scope.row.type === 'STADIUM' ? '体育场馆' : 
                 scope.row.type === 'HALL' ? '报告厅' : scope.row.type }}
            </el-tag>
            <el-tag v-else-if="scope.row.isBuilding" type="warning" size="small">楼宇</el-tag>
            <el-tag v-else type="info" size="small">校区</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="最大容量" width="100">
          <template #default="scope">
            {{ scope.row.isVenue ? scope.row.capacity : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <template v-if="scope.row.isVenue">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                {{ scope.row.status === 1 ? '开放' : '已关闭' }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <template v-if="scope.row.isVenue">
              <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button link type="warning" @click="handleLock(scope.row)">分时锁定</el-button>
              <el-button link type="danger" @click="handleStatus(scope.row)">{{ scope.row.status === 1 ? '禁用' : '启用' }}</el-button>
              <el-button v-if="userStore.userInfo?.role === 'SYS_ADMIN'" link type="info" @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
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
        <el-form-item label="所属楼道">
          <el-select v-model="venueForm.buildingId" placeholder="请选择楼宇" :disabled="userStore.userInfo?.role === 'VENUE_ADMIN'">
            <el-option
              v-for="item in buildings"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="场地类型">
          <el-select v-model="venueForm.type" placeholder="请选择">
            <el-option label="研讨室" value="SEMINAR" />
            <el-option label="体育场馆" value="STADIUM" />
            <el-option label="报告厅" value="HALL" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细位置">
          <el-input v-model="venueForm.address" />
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
    <el-dialog v-model="lockVisible" title="设置场地不可用时间段" width="600px">
      <el-form label-width="80px">
        <el-form-item label="新增锁定">
          <div style="display: flex; gap: 10px; flex-direction: column;">
            <el-date-picker v-model="lockForm.range" type="datetimerange" range-separator="至" />
            <el-input v-model="lockForm.reason" type="textarea" placeholder="锁定原因" />
            <el-button type="primary" @click="submitLock">提交锁定并取消预约</el-button>
          </div>
        </el-form-item>
        <el-divider content-position="left">当前生效的锁定</el-divider>
        <el-table :data="venueLocks" style="width: 100%" size="small">
          <el-table-column prop="startTime" label="开始时间" width="160">
            <template #default="scope">{{ dayjs(scope.row.startTime).format('YYYY-MM-DD HH:mm') }}</template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="160">
            <template #default="scope">{{ dayjs(scope.row.endTime).format('YYYY-MM-DD HH:mm') }}</template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" />
          <el-table-column label="操作" width="80">
            <template #default="scope">
              <el-button link type="danger" @click="handleDeleteLock(scope.row.id)">解除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getVenuePage, saveVenue, deleteVenue, updateVenueStatus, getAuthorizedBuildings, getVenueLocks, deleteVenueLock } from '@/api/admin';
import { useUserStore } from '@/store/user';
import dayjs from 'dayjs';

const userStore = useUserStore();

const venues = ref<any[]>([]);
const buildings = ref<any[]>([]);
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
    const rawVenues = (res.data && res.data.records) || [];
    
    // 加载授权楼宇
    let authorizedBuildings: any[] = [];
    if (userStore.userInfo?.id) {
        const bRes: any = await getAuthorizedBuildings(userStore.userInfo.id);
        if (bRes.code === 200) {
          buildings.value = bRes.data;
          authorizedBuildings = bRes.data;
        }
    }

    // 重构为层级结构
    const tree: any[] = [];
    const campusSet = new Set(authorizedBuildings.map(b => b.location));
    
    // 如果没有楼宇权限但有特定的场地权限，也要把那些场地的校区和楼宇加进来
    rawVenues.forEach((v: any) => {
        if (v.campus) campusSet.add(v.campus);
    });

    [...campusSet].forEach(campusName => {
      const campusNode: any = {
        id: `CAMPUS_${campusName}`,
        name: campusName,
        isCampus: true,
        children: []
      };

      // 找出该校区下的所有授权楼宇
      const campusBuildings = authorizedBuildings.filter(b => b.location === campusName);
      
      // 找出该校区下即便楼宇没授权但其中的场地授权了的那些楼宇ID
      const venueBuildingIds = new Set(rawVenues.filter((v: any) => v.campus === campusName).map((v: any) => v.buildingId));
      
      // 合并楼宇ID
      const allBuildingIds = new Set([...campusBuildings.map(b => b.id), ...venueBuildingIds]);

      allBuildingIds.forEach(bId => {
        // 尝试从授权楼宇列表或场地信息中恢复楼宇名称
        let bName = authorizedBuildings.find(b => b.id === bId)?.name;
        if (!bName) {
            bName = rawVenues.find((v: any) => v.buildingId === bId)?.buildingName || `楼宇ID:${bId}`;
        }

        const buildingNode: any = {
          id: `BUILDING_${bId}`,
          name: bName,
          isBuilding: true,
          buildingId: bId,
          children: []
        };

        const venuesInBuilding = rawVenues.filter((v: any) => v.buildingId === bId);
        venuesInBuilding.forEach((venue: any) => {
          buildingNode.children.push({
            ...venue,
            isVenue: true
          });
        });

        campusNode.children.push(buildingNode);
      });

      if (campusNode.children.length > 0) {
        tree.push(campusNode);
      }
    });

    venues.value = tree;
    total.value = res.data.total;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleAddInBuilding = (building: any) => {
  handleAdd();
  venueForm.buildingId = building.buildingId;
};

onMounted(() => {
  fetchData();
});

const dialogVisible = ref(false);
const venueForm = reactive({
  id: undefined,
  name: '',
  buildingId: undefined,
  type: 'SEMINAR',
  address: '',
  capacity: 0,
  equipment: '',
  status: 1
});

const handleAdd = () => {
  Object.assign(venueForm, { id: undefined, name: '', buildingId: undefined, type: 'SEMINAR', address: '', capacity: 0, equipment: '', status: 0 });
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  Object.assign(venueForm, row);
  // 如果是场地管理员，禁用楼宇修改，防止其将场地移动到非管辖范围
  dialogVisible.value = true;
};

const submitForm = async () => {
  const res: any = await saveVenue(venueForm, userStore.userInfo?.id);
  if (res.code === 200) {
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    fetchData();
  }
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定删除该场地吗？').then(async () => {
    const res: any = await deleteVenue(id, userStore.userInfo?.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      fetchData();
    }
  });
};

const lockVisible = ref(false);
const currentVenueId = ref<number | null>(null);
const venueLocks = ref<any[]>([]);
const lockForm = reactive({ range: [] as any[], reason: '' });

const handleLock = async (row: any) => {
  currentVenueId.value = row.id;
  lockForm.range = [];
  lockForm.reason = '';
  await fetchLocks();
  lockVisible.value = true;
};

const fetchLocks = async () => {
  if (!currentVenueId.value) return;
  const res: any = await getVenueLocks(currentVenueId.value);
  if (res.code === 200) {
    venueLocks.value = res.data;
  }
};

const submitLock = async () => {
  if (!lockForm.range || lockForm.range.length < 2) return ElMessage.warning('请选择时间范围');
  const res: any = await updateVenueStatus({
    id: currentVenueId.value,
    startTime: dayjs(lockForm.range[0]).format('YYYY-MM-DDTHH:mm:ss'),
    endTime: dayjs(lockForm.range[1]).format('YYYY-MM-DDTHH:mm:ss'),
    reason: lockForm.reason
  }, userStore.userInfo?.id);
  if (res.code === 200) {
    ElMessage.success('分时锁定成功，受影响的预约已取消');
    lockForm.range = [];
    lockForm.reason = '';
    fetchLocks();
  }
};

const handleDeleteLock = (lockId: number) => {
  ElMessageBox.confirm('确定解除该时间段的锁定吗？').then(async () => {
    const res: any = await deleteVenueLock(lockId);
    if (res.code === 200) {
      ElMessage.success('解除成功');
      fetchLocks();
    }
  });
};

const handleStatus = async (row: any) => {
  const newStatus = row.status === 1 ? 0 : 1;
  const statusStr = newStatus === 1 ? '启用' : '禁用';
  ElMessageBox.confirm(`确认${statusStr}${row.name}？`).then(async () => {
    const res: any = await updateVenueStatus({ id: row.id, status: newStatus }, userStore.userInfo?.id);
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
