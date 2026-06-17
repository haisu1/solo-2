<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">物资分类</h3>
      <el-button type="primary" icon="el-icon-plus" @click="openDialog()">新增分类</el-button>
    </div>
    <div class="table-container">
      <el-table :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="categoryName" label="分类名称"></el-table-column>
        <el-table-column prop="categoryCode" label="分类编码" width="120"></el-table-column>
        <el-table-column prop="supplyCount" label="物资数量" width="100"></el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80"></el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName"></el-input>
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="form.categoryCode"></el-input>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description" rows="3"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCategoryList, getCategoryPage, createCategory, updateCategory, deleteCategory } from '@/api/supply'

export default {
  name: 'CategoryList',
  data() {
    return {
      tableData: [],
      dialogVisible: false,
      dialogTitle: '新增分类',
      form: {},
      rules: {
        categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
        categoryCode: [{ required: true, message: '请输入分类编码', trigger: 'blur' }]
      },
      query: { current: 1, size: 100 }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      const res = await getCategoryPage(this.query)
      this.tableData = res.records || []
    },
    openDialog(row) {
      if (row) {
        this.dialogTitle = '编辑分类'
        this.form = { ...row }
      } else {
        this.dialogTitle = '新增分类'
        this.form = { sortOrder: 0 }
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.clearValidate()
      })
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          if (this.form.id) {
            await updateCategory(this.form.id, this.form)
          } else {
            await createCategory(this.form)
          }
          this.$message.success('操作成功')
          this.dialogVisible = false
          this.loadData()
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定删除该分类吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteCategory(row.id)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    }
  }
}
</script>
