<template>
  <div class="signature-upload-container">
    <el-upload
      class="signature-uploader"
      action="#"
      :auto-upload="false"
      :show-file-list="false"
      :on-change="handleFileChange"
      accept="image/png,image/jpeg,image/jpg"
    >
      <div v-if="imageUrl" class="signature-preview">
        <img :src="imageUrl" class="signature-image" alt="Signature Preview" />
        <div class="actions">
          <el-button type="danger" :icon="Delete" circle @click.stop="handleRemove" />
          <el-button type="primary" :icon="Edit" circle @click.stop="openCropper" />
        </div>
      </div>
      <el-icon v-else class="signature-uploader-icon"><Plus /></el-icon>
    </el-upload>

    <!-- Cropping Dialog -->
    <el-dialog v-model="dialogVisible" title="签名裁剪 (300x100)" width="500px" append-to-body>
      <div class="cropper-wrapper">
        <vue-cropper
          ref="cropperRef"
          :img="cropImg"
          :outputSize="1"
          outputType="png"
          :autoCrop="true"
          :fixed="true"
          :fixedNumber="[3, 1]"
          :canMove="true"
          :canMoveBox="true"
          :centerBox="true"
          :info="true"
          style="height: 300px"
        />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">确定上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Plus, Delete, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import request from '@/api/request'

const props = defineProps({
  modelValue: String // Existing image URL or path
})

const emit = defineEmits(['update:modelValue', 'upload-success', 'delete-success'])

const imageUrl = ref('')
const dialogVisible = ref(false)

// Helper to convert DB path to full URL
const getFullUrl = (path: string | undefined) => {
  if (!path) return '';
  if (path.startsWith('http') || path.startsWith('data:')) return path;
  // 由于我们已修复 Vite 代理映射，现在直接从 5174 发起请求，且后端已支持 /uploads/**
  return `/api/uploads/${path}`;
}

// Watch modelValue to sync imageUrl
watch(() => props.modelValue, (newVal) => {
  console.log('modelValue changed:', newVal);
  imageUrl.value = getFullUrl(newVal);
  console.log('imageUrl updated to:', imageUrl.value);
}, { immediate: true })

const cropImg = ref('')
const cropperRef = ref()
const uploading = ref(false)

const handleFileChange = (file: any) => {
  console.log('File changed:', file)
  if (!file || !file.raw) return
  
  const isLt1M = file.raw.size / 1024 / 1024 < 1
  if (!isLt1M) {
    ElMessage.error('图片大小不能超过 1MB!')
    return false
  }
  
  const reader = new FileReader()
  reader.onload = (e: any) => {
    console.log('File read success')
    cropImg.value = e.target.result as string
    dialogVisible.value = true
  }
  reader.onerror = (err) => {
    console.error('FileReader error:', err)
  }
  reader.readAsDataURL(file.raw)
}

const openCropper = () => {
  if (imageUrl.value) {
    cropImg.value = imageUrl.value
    dialogVisible.value = true
  }
}

const handleUpload = () => {
  cropperRef.value.getCropBlob(async (blob: Blob) => {
    const formData = new FormData()
    formData.append('file', blob, 'signature.png')
    
    uploading.value = true
    try {
      const res = await request.post('/upload/signature', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      imageUrl.value = res.data.url
      emit('update:modelValue', res.data.path)
      emit('upload-success', res.data)
      dialogVisible.value = false
      ElMessage.success('上传成功')
    } catch (error: any) {
      ElMessage.error('上传失败: ' + (error.response?.data?.message || error.message))
    } finally {
      uploading.value = false
    }
  })
}

const handleRemove = async () => {
  if (!imageUrl.value) return
  
  // Actually delete the file on the server if it's already there
  const path = props.modelValue
  try {
    if (path) {
      await request.delete('/upload/delete', { params: { path } })
    }
    imageUrl.value = ''
    emit('update:modelValue', '')
    emit('delete-success')
    ElMessage.success('已移除并从服务器删除')
  } catch (error) {
    ElMessage.error('移除失败')
  }
}
</script>

<style scoped>
.signature-upload-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  margin-bottom: 15px;
}

.signature-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 300px;
  height: 100px;
  background-color: #f5f7fa;
  transition: border-color 0.3s;
  display: block; /* 确保 el-upload 内部占满 */
}

/* 覆盖 Element Plus 默认样式，确保点击区域涵盖整个框 */
:deep(.el-upload) {
  width: 100% !important;
  height: 100% !important;
  display: flex !important;
  align-items: center;
  justify-content: center;
}

.signature-uploader:hover {
  border-color: var(--el-color-primary);
}

.signature-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.signature-preview {
  width: 300px;
  height: 100px;
  position: relative;
}

.signature-image {
  width: 300px;
  height: 100px;
  object-fit: contain;
}

.actions {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  opacity: 0;
  transition: opacity 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.signature-preview:hover .actions {
  opacity: 1;
}

.cropper-wrapper {
  margin-bottom: 20px;
}
</style>
