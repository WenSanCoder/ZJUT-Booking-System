<template>
  <div class="structure-mgmt-container">
    <el-row :gutter="20">
      <!-- 校区/校级管理 -->
      <el-col :span="4">
        <el-card>
          <template #header>
            <div class="header-actions">
              <span>1. 选择校区</span>
            </div>
          </template>
          <div 
            v-for="campus in campuses" 
            :key="campus" 
            class="campus-item"
            :class="{ active: currentCampus === campus }"
            @click="handleCampusChange(campus)"
          >
            <div class="campus-content">
              <span>{{ campus }}</span>
              <el-button link type="primary" size="small" @click.stop="openAssignDialog('CAMPUS', campus)">授权</el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 楼宇管理 -->
      <el-col :span="7">
        <el-card>
          <template #header>
            <div class="header-actions">
              <span>2. {{ currentCampus }} - 楼宇列表</span>
              <el-button type="primary" size="small" @click="handleBuildingAdd">新增</el-button>
            </div>
          </template>
          <el-table 
            ref="buildingTable"
            :data="filteredBuildings" 
            highlight-current-row 
            @current-change="handleBuildingCurrentChange"
            style="cursor: pointer; width: 100%"
          >
            <el-table-column prop="name" label="楼宇名称" show-overflow-tooltip />
            <el-table-column label="操作" width="160">
              <template #default="scope">
                <el-button link type="primary" @click.stop="handleBuildingEdit(scope.row)">编辑</el-button>
                <el-button link type="primary" @click.stop="openAssignDialog('BUILDING', scope.row.id)">授权</el-button>
                <el-button link type="danger" @click.stop="handleBuildingDelete(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 场地/教室管理 -->
      <el-col :span="13">
        <el-card>
          <template #header>
            <div class="header-actions">
              <span>3. {{ currentBuilding ? currentBuilding.name : '楼宇' }} - 教室列表</span>
              <el-button 
                type="primary" 
                size="small" 
                @click="handleVenueAdd"
              >
                新增教室
              </el-button>
            </div>
          </template>
          <el-table :data="venues" empty-text="未选中楼宇或该楼宇暂无教室">
            <el-table-column prop="name" label="名称" width="110" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="85">
              <template #default="scope">
                <el-tag size="small" type="info">
                  {{ scope.row.type === 'SEMINAR' ? '研讨室' : 
                     scope.row.type === 'STADIUM' ? '体育场馆' : 
                     scope.row.type === 'HALL' ? '报告厅' : scope.row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="address" label="位置" width="120" show-overflow-tooltip />
            <el-table-column prop="capacity" label="容量" width="65" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="scope">
                <el-button link type="primary" @click="handleVenueEdit(scope.row)">编辑</el-button>
                <el-button link type="primary" @click="openAssignDialog('VENUE', scope.row.id)">授权</el-button>
                <el-button link type="danger" @click="handleVenueDelete(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 授权管理员对话框 -->
    <el-dialog v-model="assignDialogVisible" title="授权管辖权" width="450px">
      <el-form label-width="100px">
        <el-form-item label="名称/ID">
          <el-tag type="info">{{ assignTarget.id }}</el-tag>
        </el-form-item>
        <el-form-item label="类型">
          <el-tag>{{ assignTarget.type }}</el-tag>
        </el-form-item>
        <el-form-item label="选择管理员">
          <el-select v-model="selectedAdminId" placeholder="选择场地管理员" style="width: 100%" filterable>
            <el-option v-for="admin in venueAdmins" :key="admin.id" :label="admin.realName + ' ('+admin.username+')'" :value="admin.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <div style="margin-top: 15px; font-size: 12px; color: #999;">
        * 该操作将把当前选中的对象直接添加至该管理员的管辖列表中。
      </div>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确认授权</el-button>
      </template>
    </el-dialog>

    <!-- 楼宇对话框 -->
    <el-dialog v-model="buildingDialog" :title="buildingForm.id ? '编辑楼宇' : '新增楼宇'" width="400px">
      <el-form :model="buildingForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="buildingForm.name" placeholder="例如：存中楼" />
        </el-form-item>
        <el-form-item label="所属校区">
          <el-select v-model="buildingForm.location" placeholder="请选择校区" style="width: 100%">
            <el-option label="朝晖校区" value="朝晖校区" />
            <el-option label="屏峰校区" value="屏峰校区" />
            <el-option label="莫干山校区" value="莫干山校区" />
            <el-option label="之江学院" value="之江学院" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buildingDialog = false">取消</el-button>
        <el-button type="primary" @click="submitBuilding">保存</el-button>
      </template>
    </el-dialog>

    <!-- 场地对话框 -->
    <el-dialog v-model="venueDialog" :title="venueForm.id ? '编辑教室' : '新增教室'" width="500px">
      <el-form :model="venueForm" label-width="100px">
        <el-form-item label="所属楼宇">
          <el-input :value="currentBuilding?.name" disabled />
        </el-form-item>
        <el-form-item label="教室名称">
          <el-input v-model="venueForm.name" placeholder="例如：一号报告厅" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="venueForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="研讨室" value="SEMINAR" />
            <el-option label="体育场馆" value="STADIUM" />
            <el-option label="报告厅" value="HALL" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细位置">
          <el-input v-model="venueForm.address" placeholder="如：101室" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="venueForm.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="设备清单">
          <el-input v-model="venueForm.equipment" type="textarea" placeholder="如：多媒体设备、空调、实验台..." />
        </el-form-item>
        <el-form-item label="场地图片URL">
          <el-input v-model="venueForm.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="venueForm.status">
            <el-radio :label="1">正常使用</el-radio>
            <el-radio :label="0">预约禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="venueDialog = false">取消</el-button>
        <el-button type="primary" @click="submitVenue">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getBuildingList, saveBuilding, deleteBuilding, getUserPage, addPermission } from '@/api/sys';
import { getVenuePage, saveVenue, deleteVenue } from '@/api/admin';

// 基础定义
const campuses = ['朝晖校区', '屏峰校区', '莫干山校区', '之江学院'];
const currentCampus = ref('朝晖校区');
const buildings = ref<any[]>([]);
const venues = ref<any[]>([]);
const currentBuilding = ref<any>(null);

// 授权管理相关
const assignDialogVisible = ref(false);
const venueAdmins = ref<any[]>([]);
const selectedAdminId = ref<number | null>(null);
const assignTarget = reactive({ type: '', id: '' as any });

const openAssignDialog = async (type: string, id: any) => {
  assignTarget.type = type;
  assignTarget.id = id;
  selectedAdminId.value = null;
  
  if (venueAdmins.value.length === 0) {
    const res: any = await getUserPage({ role: 'VENUE_ADMIN', size: 100 });
    if (res.code === 200) {
      venueAdmins.value = res.data.records;
    }
  }
  assignDialogVisible.value = true;
};

const submitAssign = async () => {
  if (!selectedAdminId.value) return ElMessage.warning('请选择管理员');
  const res: any = await addPermission({
    adminId: selectedAdminId.value,
    targetType: assignTarget.type,
    targetId: `${assignTarget.type}:${assignTarget.id}`
  });
  if (res.code === 200) {
    ElMessage.success('授权管辖范围成功');
    assignDialogVisible.value = false;
  }
};

// 过滤楼宇逻辑
const filteredBuildings = computed(() => {
  return buildings.value.filter(b => b.location === currentCampus.value);
});

// 数据加载机制
const fetchBuildings = async () => {
  const res: any = await getBuildingList();
  if (res.code === 200) {
    buildings.value = res.data;
  }
};

const fetchVenues = async (buildingId: number) => {
  const res: any = await getVenuePage({ current: 1, size: 1000, buildingId });
  if (res.code === 200) {
    venues.value = res.data.records;
  }
};

// 交互逻辑
const handleCampusChange = (campus: string) => {
  currentCampus.value = campus;
  currentBuilding.value = null;
  venues.value = [];
};

const handleBuildingCurrentChange = (row: any) => {
  if (row) {
    currentBuilding.value = row;
    fetchVenues(row.id);
  } else {
    currentBuilding.value = null;
    venues.value = [];
  }
};

// 楼宇管理
const buildingDialog = ref(false);
const buildingForm = reactive({ id: null as any, name: '', location: '' });

const handleBuildingAdd = () => {
  buildingForm.id = null;
  buildingForm.name = '';
  buildingForm.location = currentCampus.value;
  buildingDialog.value = true;
};

const handleBuildingEdit = (row: any) => {
  Object.assign(buildingForm, row);
  buildingDialog.value = true;
};

const submitBuilding = async () => {
  if(!buildingForm.name) return ElMessage.warning('请输入名称');
  const res: any = await saveBuilding(buildingForm);
  if (res.code === 200) {
    ElMessage.success('保存楼宇成功');
    buildingDialog.value = false;
    fetchBuildings();
  }
};

const handleBuildingDelete = (id: number) => {
  ElMessageBox.confirm('这会永久删除该楼宇及其关联信息，确定吗？').then(async () => {
    const res: any = await deleteBuilding(id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      fetchBuildings();
      currentBuilding.value = null;
      venues.value = [];
    }
  });
};

// 教室管理
const venueDialog = ref(false);
const venueForm = reactive({ 
  id: null as any, 
  buildingId: 0, 
  name: '', 
  address: '', 
  type: 'SEMINAR', 
  capacity: 30, 
  equipment: '',
  imageUrl: '',
  status: 1 
});

const handleVenueAdd = () => {
  if (!currentBuilding.value) return ElMessage.warning('请先点击楼宇列表中的某一行进行选择');
  Object.assign(venueForm, { 
    id: null, 
    buildingId: currentBuilding.value.id, 
    name: '', 
    address: '', 
    type: 'SEMINAR', 
    capacity: 30, 
    equipment: '',
    imageUrl: '',
    status: 1 
  });
  venueDialog.value = true;
};

const handleVenueEdit = (row: any) => {
  Object.assign(venueForm, row);
  venueDialog.value = true;
};

const submitVenue = async () => {
  if(!venueForm.name || !venueForm.address) return ElMessage.warning('请填写完整信息');
  const res: any = await saveVenue(venueForm);
  if (res.code === 200) {
    ElMessage.success('保存教室成功');
    venueDialog.value = false;
    if (currentBuilding.value) fetchVenues(currentBuilding.value.id);
  }
};

const handleVenueDelete = (id: number) => {
  ElMessageBox.confirm('确定删除该教室记录吗？').then(async () => {
    const res: any = await deleteVenue(id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      if (currentBuilding.value) fetchVenues(currentBuilding.value.id);
    }
  });
};

onMounted(() => {
  fetchBuildings();
});
</script>

<style scoped>
.structure-mgmt-container { padding: 20px; }
.header-actions { display: flex; justify-content: space-between; align-items: center; }
.campus-item {
  padding: 10px 15px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 5px;
  transition: all 0.2s;
}
.campus-item:hover { background-color: #f5f7fa; }
.campus-item.active { background-color: #ecf5ff; color: #409eff; font-weight: bold; border-left: 4px solid #409eff; }
.campus-content { display: flex; justify-content: space-between; align-items: center; }
</style>
