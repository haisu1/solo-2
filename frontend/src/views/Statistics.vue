<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">数据统计</h3>
    </div>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>申领状态统计</span>
          </div>
          <el-table :data="statusStatsList" border>
            <el-table-column prop="status" label="状态">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="数量"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>统计汇总</span>
          </div>
          <div class="summary">
            <div class="summary-item">
              <p class="label">申领单总数</p>
              <p class="value">{{ statistics.totalCount || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getRequisitionStatistics } from '@/api/statistics'

export default {
  name: 'Statistics',
  data() {
    return {
      statistics: {},
      statusStatsList: []
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.statistics = await getRequisitionStatistics()
      if (this.statistics.statusStats) {
        this.statusStatsList = Object.keys(this.statistics.statusStats).map(key => ({
          status: key,
          count: this.statistics.statusStats[key]
        }))
      }
    },
    getStatusText(status) {
      const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', CANCELLED: '已取消' }
      return map[status] || status
    },
    getStatusType(status) {
      const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' }
      return map[status] || 'info'
    }
  }
}
</script>

<style scoped lang="scss">
.summary {
  .summary-item {
    text-align: center;
    padding: 20px;
    .label {
      color: #909399;
      margin: 0 0 8px;
    }
    .value {
      color: #303133;
      font-size: 32px;
      font-weight: 600;
      margin: 0;
    }
  }
}
</style>
