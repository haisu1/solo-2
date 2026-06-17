<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">用户管理</h3>
      <el-button type="primary" icon="el-icon-plus" @click="openDialog()">新增用户</el-button>
    </div>
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="用户名/姓名" clearable @keyup.enter.native="loadData"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="table-container">
      <el-table :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="realName" label="真实姓名" width="100"></el-table-column>
        <el-table-column prop="departmentName" label="部门" width="120"></el-table-column>
        <el-table-column prop="roleName" label="角色" width="120"></el-table-column>
        <el-table-column prop="phone" label="手机号" width="140"></el-table-column>
        <el-table-column prop="email" label="邮箱" width="180"></el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 20px; text-align: right"
        background
        :current-page="searchForm.current"
        :page-size="searchForm.size"
        :total="total"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]">
      </el-pagination>
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" :disabled="!!form.id"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" prop="departmentId">
              <el-select v-model="form.departmentId" style="width: 100%">
                <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="roleId">
              <el-select v-model="form.roleId" style="width: 100%">
                <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="启用" :value="1"></el-option>
                <el-option label="禁用" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, createUser, updateUser, deleteUser, getRoleList, getDepartmentList } from '@/api/system'

export default {
  name: 'UserList',
  data() {
    return {
      tableData: [],
      total: 0,
      roles: [],
      departments: [],
      searchForm: { current: 1, size: 10, keyword: '' },
      dialogVisible: false,
      dialogTitle: '新增用户',
      form: {},
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
        roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadRoles()
    this.loadDepartments()
    this.loadData()
  },
  methods: {
    async loadRoles() {
      this.roles = await getRoleList()
    },
    async loadDepartments() {
      this.departments = await getDepartmentList()
    },
    async loadData() {
      const res = await getUserList(this.searchForm)
      this.tableData = res.records || []
      this.total = res.total || 0
    },
    resetSearch() {
      this.searchForm = { current: 1, size: 10, keyword: '' }
      this.loadData()
    },
    handlePageChange(page) {
      this.searchForm.current = page
      this.loadData()
    },
    handleSizeChange(size) {
      this.searchForm.size = size
      this.searchForm.current = 1
      this.loadData()
    },
    openDialog(row) {
      if (row) {
        this.dialogTitle = '编辑用户'
        this.form = { ...row }
      } else {
        this.dialogTitle = '新增用户'
        this.form = { status: 1, password: '123456' }
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
            await updateUser(this.form.id, this.form)
          } else {
            await createUser(this.form)
          }
          this.$message.success('操作成功')
          this.dialogVisible = false
          this.loadData()
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定删除该用户吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteUser(row.id)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    }
  }
}
</script>
