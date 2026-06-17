<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">申领管理</h3>
      <el-button type="primary" icon="el-icon-edit" @click="$router.push('/requisitions/create')">提交申领</el-button>
    </div>
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="申领单号/申请人" clearable @keyup.enter.native="loadData"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审批" value="PENDING"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已驳回" value="REJECTED"></el-option>
            <el-option label="已取消" value="CANCELLED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="table-container">
      <el-table :data="tableData" border>
        <el-table-column prop="requisitionNo" label="申领单号" width="160"></el-table-column>
        <el-table-column prop="realName" label="申请人" width="100"></el-table-column>
        <el-table-column prop="departmentName" label="部门" width="120"></el-table-column>
        <el-table-column prop="purpose" label="用途" show-overflow-tooltip></el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="mini">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180"></el-table-column>
        <el-table-column label="操作" width="240">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewDetail(scope.row)">查看</el-button>
            <el-button v-if="canApprove(scope.row)" size="mini" type="success" @click="approveDialog(scope.row)">审批</el-button>
            <el-button v-if="canCancel(scope.row)" size="mini" type="warning" @click="handleCancel(scope.row)">取消</el-button>
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

    <el-dialog title="申领详情" :visible.sync="detailVisible" width="700px">
      <el-descriptions v-if="detailData" :column="2" border>
        <el-descriptions-item label="申领单号">{{ detailData.requisitionNo }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detailData.realName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detailData.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="用途" :span="2">{{ detailData.purpose }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)">{{ getStatusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批人">{{ detailData.approvedByName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批备注" :span="2">{{ detailData.approveRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table v-if="detailData && detailData.items" :data="detailData.items" border style="margin-top: 20px" size="small">
        <el-table-column prop="supplyName" label="物资名称"></el-table-column>
        <el-table-column prop="supplyCode" label="编码" width="100"></el-table-column>
        <el-table-column prop="specification" label="规格" width="120"></el-table-column>
        <el-table-column prop="unit" label="单位" width="80"></el-table-column>
        <el-table-column prop="quantity" label="数量" width="100"></el-table-column>
      </el-table>
      <div slot="footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="审批申领" :visible.sync="approveVisible" width="500px">
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approveForm.status">
            <el-radio value="APPROVED">通过</el-radio>
            <el-radio value="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input type="textarea" v-model="approveForm.remark" rows="3"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" @click="handleApprove">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getRequisitionPage, getRequisitionDetail, approveRequisition, cancelRequisition } from '@/api/requisition'

export default {
  name: 'RequisitionList',
  data() {
    return {
      tableData: [],
      total: 0,
      searchForm: { current: 1, size: 10, keyword: '', status: '' },
      detailVisible: false,
      detailData: null,
      approveVisible: false,
      currentRow: null,
      approveForm: { status: 'APPROVED', remark: '' }
    }
  },
  computed: {
    isAdmin() {
      return this.$store.state.roleCode === 'ADMIN'
    },
    isAdminStaff() {
      return this.$store.state.roleCode === 'ADMIN_STAFF'
    },
    currentUser() {
      return this.$store.state.user
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      const res = await getRequisitionPage(this.searchForm)
      this.tableData = res.records || []
      this.total = res.total || 0
    },
    resetSearch() {
      this.searchForm = { current: 1, size: 10, keyword: '', status: '' }
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
    getStatusText(status) {
      const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', CANCELLED: '已取消' }
      return map[status] || status
    },
    getStatusType(status) {
      const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' }
      return map[status] || 'info'
    },
    canApprove(row) {
      return row.status === 'PENDING' && (this.isAdmin || this.isAdminStaff)
    },
    canCancel(row) {
      return row.status === 'PENDING' && row.userId === this.currentUser.id
    },
    async viewDetail(row) {
      this.detailData = await getRequisitionDetail(row.id)
      this.detailVisible = true
    },
    approveDialog(row) {
      this.currentRow = row
      this.approveForm = { status: 'APPROVED', remark: '' }
      this.approveVisible = true
    },
    async handleApprove() {
      await approveRequisition(this.currentRow.id, this.approveForm)
      this.$message.success('操作成功')
      this.approveVisible = false
      this.loadData()
    },
    handleCancel(row) {
      this.$confirm('确定取消该申领单吗？', '提示', { type: 'warning' }).then(async () => {
        await cancelRequisition(row.id)
        this.$message.success('取消成功')
        this.loadData()
      }).catch(() => {})
    }
  }
}
</script>
