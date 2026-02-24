<template>
  <div class="structure-mgmt-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="header-actions">
              <span>楼宇/楼栋列表</span>
              <el-button type="primary" size="small" @click="handleBuildingAdd">新增楼宇</el-button>
            </div>
          </template>
          <el-table :data="buildings" highlight-current-row @current-change="handleBuildingChange">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="location" label="校区/区域" />
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button link type="primary" @click.stop="handleBuildingEdit(scope.row)">编辑</el-button>
                <el-button link type="danger" @click.stop="handleBuildingDelete(scope.row.id)">删</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="header-actions">
              <span>{{ currentBuilding ? currentBuilding.name : '请选择楼宇' }} - 场地列表</span>
              <el-button :disabled="!currentBuilding" type="success" size="small" @click="handleVenueAdd">新增场地</el-button>
            </div>
          </template>
          <el-table :data="venues">
            <el-table-column prop="name" label="场地名称" />
            <el-table-column prop="type" label="类型" />
            <el-table-column prop="location" label="详细位置" />
            <el-table-column prop="capacity" label="容量" />
            <el-table-column label="操作">
              <template #default="scope">
                <el-button link type="primary" @click="handleVenueEdit(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="handleVenueDelete(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 楼宇对话框 -->
    <el-dialog v-model="buildingDialog" :title="buildingForm.id ? '编辑楼宇' : '新增楼宇'" width="400px">
      <el-form :model="buildingForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="buildingForm.name" />
        </el-form-item>
        <el-form-item label="校区/区域">
          <el-input v-model="buildingForm.location" placeholder="如：屏峰校区" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buildingDialog = false">取消</el-button>
        <el-button type="primary" @click="submitBuilding">保存</el-button>
      </template>
    </el-dialog>

    <!-- 场地对话框 (复用之前的逻辑) -->
    <el-dialog v-model="venueDialog" title="场地编辑" width="500px">
      <el-form :model="venueForm" label-width="100px">
        <el-form-item label="所属楼宇">
          <el-select v-model="venueForm.buildingId" disabled>
             <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="场地名称">
          <el-input v-model="venueForm.name" />
        </el-form-item>
        <el-form-item label="详细位置">
          <el-input v-model="venueForm.location" placeholder="如：101室" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="venueForm.type" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="venueForm.capacity" />
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
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getBuildingList, saveBuilding, deleteBuilding } from '@/api/sys';
import { getVenuePage, saveVenue, deleteVenue } from '@/api/admin';

const buildings = ref<any[]>([]);
const venues = ref<any[]>([]);
const currentBuilding = ref<any>(null);

const fetchData = async () => {
  const res: any = await getBuildingList();
  if (res.code === 200) buildings.value = res.data;
};

onMounted(() => {
  fetchData();
});

const handleBuildingChange = async (row: any) => {
  if (!row) return;
  currentBuilding.value = row;
  const res: any = await getVenuePage({ current: 1, size: 100, buildingId: row.id });
  if (res.code === 200) venues.value = res.data.records;
};

const buildingDialog = ref(false);
const buildingForm = reactive({ id: undefined, name: '', location: '' });
const handleBuildingAdd = () => {
  Object.assign(buildingForm, { id: undefined, name: '', location: '' });
  buildingDialog.value = true;
};
const handleBuildingEdit = (row: any) => {
  Object.assign(buildingForm, row);
  buildingDialog.value = true;
};
const submitBuilding = async () => {
  const res: any = await saveBuilding(buildingForm);
  if (res.code === 200) {
    ElMessage.success('操作成功');
    buildingDialog.value = false;
    fetchData();
  }
};
const handleBuildingDelete = (id: number) => {
  ElMessageBox.confirm('这会删除该楼宇记录，确定吗？').then(async () => {
    const res: any = await deleteBuilding(id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      fetchData();
    }
  });
};

const venueDialog = ref(false);
const venueForm = reactive({ id: undefined, name: '', buildingId: 0, location: '', type: '', capacity: 0 });
const handleVenueAdd = () => {
  Object.assign(venueForm, { id: undefined, name: '', buildingId: currentBuilding.value.id, location: '', type: '', capacity: 10 });
  venueDialog.value = true;
};
const handleVenueEdit = (row: any) => {
  Object.assign(venueForm, row);
  venueDialog.value = true;
};
const submitVenue = async () => {
  const res: any = await saveVenue(venueForm);
  if (res.code === 200) {
    ElMessage.success('保存成功');
    venueDialog.value = false;
    handleBuildingChange(currentBuilding.value);
  }
};
const handleVenueDelete = (id: number) => {
  ElMessageBox.confirm('确定删除该场地吗？').then(async () => {
    const res: any = await deleteVenue(id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      handleBuildingChange(currentBuilding.value);
    }
  });
};
</script>

<style scoped>
.structure-mgmt-container { padding: 20px; }
.header-actions { display: flex; justify-content: space-between; align-items: center; }
</style>
