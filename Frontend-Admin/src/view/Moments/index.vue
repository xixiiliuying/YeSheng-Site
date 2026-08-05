<script setup>
import { ref, onMounted } from 'vue'
import { getMomentsPage, createMoment, updateMoment, deleteMoment, approveMoment } from '@/api/moments'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const filterStatus = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ id: null, content: '', emoji: '💬', tag: '', color: '', nickname: 'YeSheng', isVisible: 1 })

const load = async () => {
  const params = { page: page.value, pageSize: size.value }
  if (filterStatus.value !== '') params.isApproved = filterStatus.value
  const res = await getMomentsPage(params)
  list.value = res.data?.records ?? []
  total.value = res.data?.total ?? 0
}

const openCreate = () => {
  isEdit.value = false
  form.value = { id: null, content: '', emoji: '💬', tag: '', color: '', nickname: 'YeSheng', isVisible: 1 }
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (isEdit.value) {
    await updateMoment(form.value)
  } else {
    await createMoment(form.value)
  }
  ElMessage.success(isEdit.value ? '已更新' : '已创建')
  dialogVisible.value = false
  load()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除？', '确认', { type: 'warning' })
  await deleteMoment(id)
  ElMessage.success('已删除')
  load()
}

const handleApprove = async (row, isApproved) => {
  await approveMoment(row.id, isApproved)
  ElMessage.success(isApproved === 1 ? '已通过' : '已拒绝')
  load()
}

const formatTime = (t) => t?.slice(0, 16).replace('T', ' ')

onMounted(load)
</script>

<template>
  <div class="moments-admin">
    <div class="topbar">
      <el-select v-model="filterStatus" placeholder="审核状态" clearable style="width:140px" @change="load">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-button type="primary" @click="openCreate">新增瞬间</el-button>
    </div>

    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="emoji" label="" width="40" />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="nickname" label="发布人" width="100" />
      <el-table-column prop="tag" label="标签" width="80" />
      <el-table-column label="审核" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.isApproved === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="row.isApproved === 2" type="danger">已拒绝</el-tag>
          <el-tag v-else type="warning">待审核</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="140">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.isApproved !== 1" size="small" type="success" @click="handleApprove(row, 1)">通过</el-button>
          <el-button v-if="row.isApproved !== 2" size="small" type="warning" @click="handleApprove(row, 2)">拒绝</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @change="load"
      style="margin-top:16px;justify-content:flex-end"
    />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑' : '新增'" width="460px">
      <el-form :model="form" label-width="60px">
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="Emoji">
          <el-input v-model="form.emoji" style="width:100px" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tag" placeholder="碎碎念/灵感/心得" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.topbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
