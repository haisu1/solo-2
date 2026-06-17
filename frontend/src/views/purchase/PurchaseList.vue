<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">采购管理</h3>
      <el-button type="primary" icon="el-icon-plus" @click="$router.push('/purchases/create')">创建采购单</el-button>
    </div>
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="采购单号/供应商" clearable @keyup.enter.native="loadData"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审批" value="PENDING"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已驳回" value="REJECTED"></el-option>
            <el-option label="已入库" value="STOCKED"></el-option>
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
        <el-table-column prop="purchaseNo" label="采购单号" width="160"></el-table-column>
        <el-table-column prop="supplierName" label="供应商"></el-table-column>
        <el-table-column prop="totalAmount" label="采购金额" width="120"></el-table-column>
        <el-table-column prop="createdByName" label="创建人" width="100"></el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="mini">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="260">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewDetail(scope.row)">查看</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="success" @click="approveDialog(scope.row)">审批</el-button>
            <el-button v-if="scope.row.status === 'APPROVED'" size="mini" type="primary" @click="handleStockIn(scope.row)">入库</el-button>
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

    <el-dialog title="采购详情" :visible.sync="detailVisible" width="700px">
      <el-descriptions v-if="detailData" :column="2" border>
        <el-descriptions-item label="采购单号">{{ detailData.purchaseNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ detailData.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="采购金额">{{ detailData.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createdByName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)">{{ getStatusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table v-if="detailData && detailData.items" :data="detailData.items" border style="margin-top: 20px" size="small">
        <el-table-column prop="supplyName" label="物资名称"></el-table-column>
        <el-table-column prop="supplyCode" label="编码" width="100"></el-table-column>
        <el-table-column prop="unit" label="单位" width="80"></el-table-column>
        <el-table-column prop="quantity" label="数量" width="100"></el-table-column>
        <el-table-column prop="unitPrice" label="单价" width="100"></el-table-column>
        <el-table-column prop="totalPrice" label="小计" width="120"></el-table-column>
      </el-table>
      <div slot="footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="审批采购单" :visible.sync="approveVisible" width="500px">
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approveForm.status">
            <el-radio value="APPROVED">通过</el-radio>
            <el-radio value="REJECTED">驳回</el-radio>
          </el-radio-group>
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
import { getPurchasePage, getPurchaseDetail, approvePurchase, stockIn } from '@/api/purchase'

export default {
  name: 'PurchaseList',
  data() {
    return {
      tableData: [],
      total: 0,
      searchForm: { current: 1, size: 10, keyword: '', status: '' },
      detailVisible: false,
      detailData: null,
      approveVisible: false,
      currentRow: null,
      approveForm: { status: 'APPROVED' }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      const res = await getPurchasePage(this.searchForm)
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
      const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', STOCKED: '已入库' }
      return map[status] || status
    },
    getStatusType(status) {
      const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', STOCKED: 'primary' }
      return map[status] || 'info'
    },
    async viewDetail(row) {
      this.detailData = await getPurchaseDetail(row.id)
      this.detailVisible = true
    },
    approveDialog(row) {
      this.currentRow = row
      this.approveForm = { status: 'APPROVED' }
      this.approveVisible = true
    },
    async handleApprove() {
      await approvePurchase(this.currentRow.id, this.approveForm)
      this.$message.success('操作成功')
      this.approveVisible = false
      this.loadData()
    },
    handleStockIn(row) {
      this.$confirm('确认将该采购单物资入库吗？', '提示', { type: 'warning' }).then(async () => {
        await stockIn(row.id)
        this.$message.success('入库成功')
        this.loadData()
      }).catch(() => {})
    }
  }
}
</script>
