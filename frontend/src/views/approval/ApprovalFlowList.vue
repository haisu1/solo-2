<template>
  <div class="approval-flow-list">
    <el-card>
      <div slot="header" class="header">
        <span>审批流程配置</span>
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增流程</el-button>
      </div>

      <div class="filter-bar">
        <el-input v-model="keyword" placeholder="搜索流程名称" clearable style="width: 250px; margin-right: 10px;" @clear="loadData" @keyup.enter.native="loadData" />
        <el-select v-model="bizType" placeholder="业务类型" clearable style="width: 200px; margin-right: 10px;" @change="loadData">
          <el-option label="申领单" value="REQUISITION" />
          <el-option label="采购单" value="PURCHASE" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="flowName" label="流程名称" min-width="180" />
        <el-table-column prop="bizType" label="业务类型" width="120">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.bizType === 'REQUISITION' ? '' : 'warning'">
              {{ scope.row.bizType === 'REQUISITION' ? '申领单' : '采购单' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="适用部门" width="140" />
        <el-table-column prop="categoryName" label="物资类别" width="140" />
        <el-table-column label="金额范围" width="180">
          <template slot-scope="scope">
            {{ scope.row.minAmount }} ~ {{ scope.row.maxAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" />
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批节点" min-width="250">
          <template slot-scope="scope">
            <el-steps :active="0" finish-status="success" align-center size="small">
              <el-step v-for="(node, idx) in scope.row.nodes" :key="idx" :title="node.nodeName" />
            </el-steps>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 20px; text-align: right;"
        :current-page="current"
        :page-size="size"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="current => { $data.current = current; loadData() }"
        @size-change="size => { $data.size = size; loadData() }"
      />
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="900px" top="5vh">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="流程名称" prop="flowName">
              <el-input v-model="form.flowName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务类型" prop="bizType">
              <el-select v-model="form.bizType" style="width: 100%">
                <el-option label="申领单" value="REQUISITION" />
                <el-option label="采购单" value="PURCHASE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="适用部门">
              <el-select v-model="form.departmentId" clearable style="width: 100%" placeholder="不填则所有部门">
                <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物资类别">
              <el-select v-model="form.categoryId" clearable style="width: 100%" placeholder="不填则所有类别">
                <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="最小金额">
              <el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大金额">
              <el-input-number v-model="form.maxAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级" prop="priority">
              <el-input-number v-model="form.priority" :min="0" :max="99" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-divider content-position="left">审批节点配置</el-divider>
        <div class="node-list">
          <div v-for="(node, index) in form.nodes" :key="index" class="node-item">
            <el-card shadow="hover">
              <div class="node-header">
                <span class="node-level">第 {{ index + 1 }} 级</span>
                <el-button size="mini" type="text" class="move-up" :disabled="index === 0" @click="moveNode(index, -1)">上移</el-button>
                <el-button size="mini" type="text" class="move-down" :disabled="index === form.nodes.length - 1" @click="moveNode(index, 1)">下移</el-button>
                <el-button size="mini" type="text" class="delete-node" @click="removeNode(index)" :disabled="form.nodes.length <= 1">删除</el-button>
              </div>
              <el-form :model="node" label-width="100px" size="small">
                <el-row :gutter="15">
                  <el-col :span="8">
                    <el-form-item label="节点名称">
                      <el-input v-model="node.nodeName" placeholder="如：部门主管审批" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="审批人类型">
                      <el-select v-model="node.approverType" style="width: 100%" @change="onApproverTypeChange(index)">
                        <el-option label="指定用户" value="USER" />
                        <el-option label="指定角色" value="ROLE" />
                        <el-option label="部门主管" value="DEPARTMENT_LEADER" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item v-if="node.approverType === 'USER'" label="审批用户">
                      <el-select v-model="node.approverIds" multiple filterable style="width: 100%" placeholder="选择审批人">
                        <el-option v-for="u in users" :key="u.id" :label="u.realName" :value="String(u.id)" />
                      </el-select>
                    </el-form-item>
                    <el-form-item v-if="node.approverType === 'ROLE'" label="审批角色">
                      <el-select v-model="node.roleCode" style="width: 100%">
                        <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.roleCode" />
                      </el-select>
                    </el-form-item>
                    <el-form-item v-if="node.approverType === 'DEPARTMENT_LEADER'" label="">
                      <span class="tip">各部门主管自动审批</span>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="15">
                  <el-col :span="8">
                    <el-form-item label="超时时间">
                      <el-input-number v-model="node.timeoutHours" :min="1" :max="168" style="width: 100%" />
                      <span class="tip">小时</span>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="允许转交">
                      <el-switch v-model="node.canTransfer" :active-value="1" :inactive-value="0" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="允许撤回">
                      <el-switch v-model="node.canWithdraw" :active-value="1" :inactive-value="0" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-card>
          </div>
          <el-button type="dashed" style="width: 100%; margin-top: 10px;" icon="el-icon-plus" @click="addNode">添加审批节点</el-button>
        </div>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getApprovalFlowPage, getApprovalFlowDetail, createApprovalFlow, updateApprovalFlow, deleteApprovalFlow } from '@/api/approval'

export default {
  name: 'ApprovalFlowList',
  data() {
    return {
      loading: false,
      keyword: '',
      bizType: '',
      current: 1,
      size: 10,
      total: 0,
      tableData: [],
      dialogVisible: false,
      dialogTitle: '新增审批流程',
      form: {
        id: null,
        flowName: '',
        bizType: 'REQUISITION',
        departmentId: null,
        categoryId: null,
        minAmount: 0,
        maxAmount: 99999999.99,
        priority: 0,
        status: 1,
        nodes: [{
          nodeName: '部门主管审批',
          approverType: 'DEPARTMENT_LEADER',
          approverIds: '',
          roleCode: '',
          timeoutHours: 24,
          canTransfer: 1,
          canWithdraw: 1
        }]
      },
      rules: {
        flowName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
        bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }]
      },
      departments: [],
      categories: [],
      users: [],
      roles: []
    }
  },
  created() {
    this.loadData()
    this.loadDicts()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getApprovalFlowPage({
          current: this.current,
          size: this.size,
          keyword: this.keyword,
          bizType: this.bizType
        })
        if (res.code === 200) {
          this.tableData = res.data.records || []
          this.total = res.data.total || 0
        }
      } finally {
        this.loading = false
      }
    },
    async loadDicts() {
      try {
        const [deptRes, catRes, userRes, roleRes] = await Promise.all([
          this.$http.get('/api/system/departments'),
          this.$http.get('/api/supplies/categories'),
          this.$http.get('/api/system/users'),
          this.$http.get('/api/system/roles')
        ])
        if (deptRes.data.code === 200) this.departments = deptRes.data.data || []
        if (catRes.data.code === 200) this.categories = catRes.data.data || []
        if (userRes.data.code === 200) this.users = userRes.data.data?.records || userRes.data.data || []
        if (roleRes.data.code === 200) this.roles = roleRes.data.data || []
      } catch (e) {
        console.warn('字典加载失败', e)
      }
    },
    handleAdd() {
      this.dialogTitle = '新增审批流程'
      this.form = {
        id: null,
        flowName: '',
        bizType: 'REQUISITION',
        departmentId: null,
        categoryId: null,
        minAmount: 0,
        maxAmount: 99999999.99,
        priority: 0,
        status: 1,
        nodes: [{
          nodeName: '部门主管审批',
          approverType: 'DEPARTMENT_LEADER',
          approverIds: '',
          roleCode: '',
          timeoutHours: 24,
          canTransfer: 1,
          canWithdraw: 1
        }]
      }
      this.dialogVisible = true
    },
    async handleEdit(row) {
      this.dialogTitle = '编辑审批流程'
      this.loading = true
      try {
        const res = await getApprovalFlowDetail(row.id)
        if (res.code === 200) {
          const data = res.data
          this.form = {
            id: data.id,
            flowName: data.flowName,
            bizType: data.bizType,
            departmentId: data.departmentId,
            categoryId: data.categoryId,
            minAmount: data.minAmount,
            maxAmount: data.maxAmount,
            priority: data.priority,
            status: data.status,
            nodes: (data.nodes || [{
              nodeName: '部门主管审批',
              approverType: 'DEPARTMENT_LEADER',
              approverIds: '',
              roleCode: '',
              timeoutHours: 24,
              canTransfer: 1,
              canWithdraw: 1
            }]).map(n => ({
              id: n.id,
              flowId: n.flowId,
              nodeLevel: n.nodeLevel,
              nodeName: n.nodeName,
              approverType: n.approverType || 'USER',
              approverIds: n.approverIds || '',
              roleCode: n.roleCode || '',
              timeoutHours: n.timeoutHours || 24,
              canTransfer: n.canTransfer != null ? n.canTransfer : 1,
              canWithdraw: n.canWithdraw != null ? n.canWithdraw : 1
            }))
          }
          this.dialogVisible = true
        }
      } finally {
        this.loading = false
      }
    },
    handleDelete(row) {
      this.$confirm(`确定删除流程「${row.flowName}」吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await deleteApprovalFlow(row.id)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.loadData()
        }
      }).catch(() => {})
    },
    addNode() {
      this.form.nodes.push({
        nodeName: `第${this.form.nodes.length + 1}级审批`,
        approverType: 'USER',
        approverIds: '',
        roleCode: '',
        timeoutHours: 24,
        canTransfer: 1,
        canWithdraw: 1
      })
    },
    removeNode(index) {
      if (this.form.nodes.length <= 1) return
      this.form.nodes.splice(index, 1)
      this.form.nodes.forEach((n, i) => { n.nodeLevel = i + 1 })
    },
    moveNode(index, direction) {
      const target = index + direction
      if (target < 0 || target >= this.form.nodes.length) return
      const temp = this.form.nodes[index]
      this.$set(this.form.nodes, index, this.form.nodes[target])
      this.$set(this.form.nodes, target, temp)
    },
    onApproverTypeChange(index) {
      this.form.nodes[index].approverIds = ''
      this.form.nodes[index].roleCode = ''
    },
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (!this.form.nodes.length) {
          this.$message.warning('请至少配置一个审批节点')
          return
        }
        const submitData = { ...this.form }
        submitData.nodes = submitData.nodes.map((n, i) => ({
          ...n,
          nodeLevel: i + 1,
          approverIds: Array.isArray(n.approverIds) ? n.approverIds.join(',') : n.approverIds
        }))
        let res
        if (this.form.id) {
          res = await updateApprovalFlow(this.form.id, submitData)
        } else {
          res = await createApprovalFlow(submitData)
        }
        if (res.code === 200) {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadData()
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter-bar {
  margin-bottom: 15px;
}
.node-list {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}
.node-item {
  margin-bottom: 12px;
}
.node-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  .node-level {
    font-weight: bold;
    color: #409EFF;
    margin-right: auto;
  }
  .move-up, .move-down, .delete-node {
    margin-left: 10px;
  }
  .delete-node {
    color: #F56C6C;
  }
}
.tip {
  color: #909399;
  font-size: 12px;
  margin-left: 5px;
}
</style>
