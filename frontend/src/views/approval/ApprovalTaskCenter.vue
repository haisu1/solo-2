<template>
  <div class="approval-task-center">
    <el-card>
      <div slot="header" class="header">
        <span>我的待办审批</span>
        <div>
          <el-button-group>
            <el-button :type="activeTab === 'all'" @click="switchTab('all')">全部</el-button>
            <el-button :type="activeTab === 'REQUISITION'" @click="switchTab('REQUISITION')">申领单</el-button>
            <el-button :type="activeTab === 'PURCHASE'" @click="switchTab('PURCHASE')">采购单</el-button>
          </el-button-group>
          <el-button type="danger" :disabled="selectedRows.length === 0" @click="batchDialogVisible = true">
            批量审批 ({{ selectedRows.length }})</el-button>
        </div>
      </div>

      <div class="filter-bar">
        <span>排序方式：</span>
        <el-radio-group v-model="sortBy" size="small" @change="loadData">
          <el-radio-button label="createTime">提交时间</el-radio-button>
          <el-radio-button label="urgent">紧急程度</el-radio-button>
          <el-radio-button label="timeout">超时时间</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="taskList" v-loading="loading" border stripe @selection-change="rows => selectedRows = rows">
        <el-table-column type="selection" width="55" />
        <el-table-column label="单号" min-width="180">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.bizType === 'REQUISITION' ? '' : 'warning'" style="margin-right: 5px;">
              {{ scope.row.bizType === 'REQUISITION' ? '申领' : '采购' }}
            </el-tag>
            <span style="font-weight: bold;">{{ scope.row.bizNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请人" width="100">
          <template slot-scope="scope">
            <span>{{ scope.row.applicantName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="当前节点" min-width="160">
          <template slot-scope="scope">
            <el-steps :active="0" size="mini" finish-status="success" align-center style="max-width: 300px;">
              <el-step v-for="i in scope.row.totalLevels" :key="i"
                :title="getStepTitle(scope, i)" />
            </el-steps>
          </template>
        </el-table-column>
        <el-table-column label="审批进度" width="120">
          <template slot-scope="scope">
            <el-progress :percentage="Math.round((scope.row.currentLevel - 1) / scope.row.totalLevels * 100)"
              :status="scope.row.isUrgent ? 'exception' : 'success'" />
          </template>
        </el-table-column>
        <el-table-column label="紧急程度" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isUrgent" type="danger" size="small">紧急</el-tag>
            <el-tag v-else size="small">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="超时状态" width="160">
          <template slot-scope="scope">
            <span v-if="scope.row.timeoutMinutes != null && scope.row.timeoutMinutes < 0"
              style="color: #F56C6C;">
              已超时 {{ Math.abs(Math.floor(scope.row.timeoutMinutes / 60)) }}小时
            </span>
            <span v-else-if="scope.row.timeoutMinutes != null && scope.row.timeoutMinutes < 60"
              style="color: #E6A23C;">
              剩余 {{ scope.row.timeoutMinutes }}分钟
            </span>
            <span v-else style="color: #67C23A;">
              剩余 {{ scope.row.timeoutMinutes != null ? Math.floor(scope.row.timeoutMinutes / 60) + '小时' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleView(scope.row)">查看</el-button>
            <el-button size="mini" type="success" @click="handleApprove(scope.row, 'APPROVE')">通过</el-button>
            <el-button size="mini" type="danger" @click="handleApprove(scope.row, 'REJECT')">驳回</el-button>
            <el-dropdown size="mini" @command="cmd => handleDropdown(cmd, scope.row)">
              <el-button size="mini">更多<i class="el-icon-arrow-down el-icon--right"></i></el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="transfer">转交</el-dropdown-item>
                <el-dropdown-item command="withdraw">撤回</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="'审批详情 - ' + (currentRow ? currentRow.bizNo : '')" :visible.sync="detailVisible" width="1000px" top="5vh">
      <div v-if="currentRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="业务类型">
            {{ currentRow.bizType === 'REQUISITION' ? '申领单' : '采购单' }}
          </el-descriptions-item>
          <el-descriptions-item label="单据编号">{{ currentRow.bizNo }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentRow.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="所属部门">{{ currentRow.departmentName }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRow.totalAmount" label="总金额">¥ {{ currentRow.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDateTime(currentRow.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">审批进度</el-divider>
        <el-steps :active="currentRow.currentLevel" finish-status="success" align-center>
          <el-step v-for="(record, idx) in historyRecords" :key="idx"
            :title="record.nodeName || ('第' + (idx + 1) + '级')"
            :description="getStepDesc(record)">
            <template slot="icon">
              <i v-if="record.approverName" :class="getStepIconClass(record)"></i>
            </template>
          </el-step>
        </el-steps>

        <el-divider content-position="left">审批历史</el-divider>
        <el-timeline>
          <el-timeline-item
            v-for="(record, idx) in historyRecords"
            :key="'h-' + idx"
            :timestamp="formatDateTime(record.approveTime || record.startTime)"
            :type="getTimelineType(record)"
            placement="top">
            <el-card shadow="never" class="history-card">
              <div class="history-header">
                <span class="history-node">{{ record.nodeName }}</span>
                <span class="history-action">
                  <el-tag size="small" :type="getActionTagType(record.action)">{{ getActionText(record.action) }}</el-tag>
                </span>
              </div>
              <div class="history-content">
                <el-descriptions :column="3" size="mini" border>
                  <el-descriptions-item label="审批人">{{ record.approverName || '等待中' }}
                    <span v-if="record.transferFromName" style="color:#909399;">(转自{{ record.transferFromName }})</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="开始时间">{{ formatDateTime(record.startTime) }}</el-descriptions-item>
                  <el-descriptions-item label="完成时间">{{ formatDateTime(record.approveTime) || '-' }}</el-descriptions-item>
                </el-descriptions>
                <div class="duration" v-if="record.durationText">
                  <i class="el-icon-time"></i> 处理耗时：{{ record.durationText }}
                </div>
                <div class="remark" v-if="record.remark">
                  <i class="el-icon-document"></i> 审批意见：{{ record.remark }}
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>

    <el-dialog title="审批操作" :visible.sync="approveDialogVisible" width="500px">
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="审批动作">
          <el-radio-group v-model="approveForm.action">
            <el-radio label="APPROVE">通过</el-radio>
            <el-radio label="REJECT">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input type="textarea" v-model="approveForm.remark" :rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApprove">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="转交审批" :visible.sync="transferDialogVisible" width="500px">
      <el-form :model="transferForm" label-width="100px">
        <el-form-item label="转交用户" required>
          <el-select v-model="transferForm.userId" filterable placeholder="选择转交用户" style="width: 100%;">
            <el-option v-for="u in users" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="转交原因">
          <el-input type="textarea" v-model="transferForm.remark" :rows="3" placeholder="请输入转交原因" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTransfer">确定转交</el-button>
      </span>
    </el-dialog>

    <el-dialog title="批量审批" :visible.sync="batchDialogVisible" width="500px">
      <el-alert
        :title="'将对所选的 ' + selectedRows.length + ' 条单据执行相同的审批操作'"
        type="warning" show-icon :closable="false" style="margin-bottom:15px;">
      </el-alert>
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="审批动作">
          <el-radio-group v-model="batchForm.action">
            <el-radio label="APPROVE">全部通过</el-radio>
            <el-radio label="REJECT">全部驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="统一意见">
          <el-input type="textarea" v-model="batchForm.remark" :rows="3" placeholder="统一审批意见" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatch">确定执行</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getMyApprovalTasks, getApprovalHistory, approveTask, batchApproveTasks, withdrawApproval } from '@/api/approval'

export default {
  name: 'ApprovalTaskCenter',
  data() {
    return {
      loading: false,
      activeTab: 'all',
      sortBy: 'createTime',
      taskList: [],
      selectedRows: [],
      currentRow: null,
      historyRecords: [],
      detailVisible: false,
      approveDialogVisible: false,
      approveForm: { action: 'APPROVE', remark: '' },
      transferDialogVisible: false,
      transferForm: { userId: null, remark: '' },
      batchDialogVisible: false,
      batchForm: { action: 'APPROVE', remark: '' },
      approveRow: null,
      users: []
    }
  },
  created() {
    this.loadData()
    this.loadUsers()
  },
  methods: {
    switchTab(tab) {
      this.activeTab = tab
      this.loadData()
    },
    async loadData() {
      this.loading = true
      try {
        const bizType = this.activeTab === 'all' ? null : this.activeTab
        const res = await getMyApprovalTasks({ bizType, sortBy: this.sortBy })
        if (res.code === 200) {
          this.taskList = res.data || []
        }
      } finally {
        this.loading = false
      }
    },
    async loadUsers() {
      try {
        const res = await this.$http.get('/api/system/users', { params: { size: 999 } })
        if (res.data.code === 200) {
          this.users = res.data.data?.records || res.data.data || []
        }
      } catch (e) {}
    },
    getStepTitle(scope, i) {
      if (i <= scope.currentLevel - 1) return '已完成'
      if (i === scope.currentLevel) return scope.nodeName
      return '待审批'
    },
    getStepDesc(record) {
      if (!record) return ''
      if (record.action === 'APPROVE') return record.approverName + ' 已通过'
      if (record.action === 'REJECT') return record.approverName + ' 已驳回'
      if (record.action === 'TRANSFER') return '已转交'
      if (record.action === 'SKIPPED') return '已跳过'
      return record.approverName || '待处理'
    },
    getStepIconClass(record) {
      if (!record) return 'el-icon-time'
      if (record.action === 'APPROVE') return 'el-icon-circle-check'
      if (record.action === 'REJECT') return 'el-icon-circle-close'
      if (record.action === 'TRANSFER') return 'el-icon-switch-button'
      return 'el-icon-loading'
    },
    getTimelineType(record) {
      if (!record || !record.action) return 'primary'
      if (record.action === 'APPROVE') return 'success'
      if (record.action === 'REJECT') return 'danger'
      if (record.action === 'TRANSFER') return 'warning'
      return 'info'
    },
    getActionTagType(action) {
      const map = {
        APPROVE: 'success', REJECT: 'danger', TRANSFER: 'warning',
        WITHDRAW: 'info', SKIPPED: 'info', TIMEOUT_REMIND: 'warning'
      }
      return map[action] || ''
    },
    getActionText(action) {
      const map = {
        APPROVE: '通过', REJECT: '驳回', TRANSFER: '转交',
        WITHDRAW: '撤回', SKIPPED: '跳过',
        TIMEOUT_REMIND: '超时提醒', SUBMIT: '提交'
      }
      return map[action] || '待审批'
    },
    formatDateTime(dt) {
      if (!dt) return ''
      const d = new Date(dt)
      const pad = n => (n < 10 ? '0' + n : n)
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    },
    async handleView(row) {
      this.currentRow = row
      try {
        const res = await getApprovalHistory({ bizType: row.bizType, bizId: row.bizId })
        if (res.code === 200) {
          this.historyRecords = (res.data || []).map(r => {
            r.calculateDuration && r.calculateDuration()
            return r
          })
        }
      } catch (e) {
        this.historyRecords = []
      }
      this.detailVisible = true
    },
    handleApprove(row, action) {
      this.approveRow = row
      this.approveForm = { action, remark: '' }
      this.approveDialogVisible = true
    },
    async submitApprove() {
      const params = {
        bizType: this.approveRow.bizType,
        bizId: this.approveRow.bizId,
        action: this.approveForm.action,
        remark: this.approveForm.remark
      }
      const res = await approveTask(params)
      if (res.code === 200) {
        this.$message.success(this.approveForm.action === 'APPROVE' ? '审批通过' : '已驳回')
        this.approveDialogVisible = false
        this.loadData()
      }
    },
    handleDropdown(cmd, row) {
      if (cmd === 'transfer') {
        this.approveRow = row
        this.transferForm = { userId: null, remark: '' }
        this.transferDialogVisible = true
      } else if (cmd === 'withdraw') {
        this.$confirm('确定撤回上一级的审批结果吗？', '撤回确认', {
          type: 'warning'
        }).then(async () => {
            const res = await withdrawApproval({ bizType: row.bizType, bizId: row.bizId })
            if (res.code === 200) {
              this.$message.success('撤回成功')
              this.loadData()
            }
          }).catch(() => {})
      }
    },
    async submitTransfer() {
      const params = {
        bizType: this.approveRow.bizType,
        bizId: this.approveRow.bizId,
        action: 'TRANSFER',
        remark: this.transferForm.remark,
        transferToUserId: this.transferForm.userId
      }
      const res = await approveTask(params)
      if (res.code === 200) {
        this.$message.success('转交成功')
        this.transferDialogVisible = false
        this.loadData()
      }
    },
    async submitBatch() {
      const data = {
        bizType: this.activeTab === 'all' ? (this.selectedRows[0] && this.selectedRows[0].bizType) : this.activeTab,
        bizIds: this.selectedRows.map(r => r.bizId),
        action: this.batchForm.action,
        remark: this.batchForm.remark
      }
      const res = await batchApproveTasks(data)
      if (res.code === 200) {
        this.$message.success('批量审批完成')
        this.batchDialogVisible = false
        this.loadData()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  .el-button-group {
    margin-right: 15px;
  }
}
.filter-bar {
  margin-bottom: 15px;
  padding: 10px 0;
}
.history-card {
  ::v-deep .el-card__body {
    padding: 12px;
  }
}
.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  .history-node {
    font-weight: bold;
    color: #303133;
  }
}
.duration {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}
.remark {
  margin-top: 5px;
  color: #606266;
  padding: 5px 10px;
  background: #f4f4f5;
  border-radius: 3px;
}
</style>
