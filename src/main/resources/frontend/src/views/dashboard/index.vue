<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statsCards" :key="item.title">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon" :style="{ color: item.color }">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-title">{{ item.title }}</div>
            <div class="stats-value">{{ item.value }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card header="待审批趋势">
          <div ref="lineChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card header="待完成分类占比">
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import * as echarts from 'echarts';
import { Calendar, User, Warning, Memo } from '@element-plus/icons-vue';

const statsCards = ref([
  { title: '今日预约场次', value: 128, icon: Calendar, color: '#409EFF' },
  { title: '当前在场人数', value: 14, icon: User, color: '#67C23A' },
  { title: '异常违约统计', value: '4,291', icon: Warning, color: '#F56C6C' },
  { title: '待办审批数', value: '03', icon: Memo, color: '#E6A23C' }
]);

const lineChartRef = ref<HTMLElement | null>(null);
const pieChartRef = ref<HTMLElement | null>(null);

onMounted(() => {
  initCharts();
});

const initCharts = () => {
  if (lineChartRef.value) {
    const lineChart = echarts.init(lineChartRef.value);
    lineChart.setOption({
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
      yAxis: { type: 'value' },
      series: [{ data: [150, 230, 224, 218, 135, 147, 260], type: 'bar', barWidth: '40%' }],
      color: ['#409EFF']
    });
  }

  if (pieChartRef.value) {
    const pieChart = echarts.init(pieChartRef.value);
    pieChart.setOption({
      legend: { bottom: '0%' },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          data: [
            { value: 45, name: '办公室' },
            { value: 25, name: '报告厅' },
            { value: 30, name: '实验室' }
          ]
        }
      ]
    });
  }
};
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}
.stats-card {
  display: flex;
  align-items: center;
}
.stats-icon {
  font-size: 40px;
  margin-right: 20px;
}
.stats-title {
  color: #909399;
  font-size: 14px;
}
.stats-value {
  font-size: 24px;
  font-weight: bold;
  margin-top: 5px;
}
.chart-row {
  margin-top: 20px;
}
.chart-box {
  height: 350px;
}
</style>
