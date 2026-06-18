<template>
  <div class="page-container">
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF">
              <i class="el-icon-s-goods"></i>
            </div>
            <div class="stat-info">
              <p class="stat-label">物资种类</p>
              <p class="stat-value">{{ dashboard.totalSupplyCount || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning-card" @click="showWarningPanel = true">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: warningBgColor }">
              <i class="el-icon-warning"></i>
            </div>
            <div class="stat-info">
              <p class="stat-label">预警物资</p>
              <p class="stat-value">{{ warningStats.warningTotalCount || 0 }}</p>
              <div class="warning-sub-info">
                <span v-if="warningStats.urgentCount" class="urgent">紧急{{ warningStats.urgentCount }}</span>
                <span v-if="warningStats.warningCount" class="warning">警告{{ warningStats.warningCount }}</span>
                <span v-if="warningStats.attentionCount" class="attention">关注{{ warningStats.attentionCount }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A">
              <i class="el-icon-s-order"></i>
            </div>
            <div class="stat-info">
              <p class="stat-label">申领总数</p>
              <p class="stat-value">{{ dashboard.totalRequisitionCount || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="showMessagePanel = true">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: unreadCount.total > 0 ? '#F56C6C' : '#909399' }">
              <i class="el-icon-bell"></i>
            </div>
            <div class="stat-info">
              <p class="stat-label">未读预警消息</p>
              <p class="stat-value">{{ unreadCount.total || 0 }}</p>
              <div class="warning-sub-info">
                <span v-if="unreadCount.urgent" class="urgent">紧急{{ unreadCount.urgent }}</span>
                <span v-if="unreadCount.warning" class="warning">警告{{ unreadCount.warning }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb-20">
      <el-col :span="6">
        <el-card>
          <div slot="header" class="flex-between">
            <span>库存总览</span>
          </div>
          <div class="overview-item">
            <p class="label">库存总量</p>
            <p class="value">{{ dashboard.totalStock || 0 }}</p>
          </div>
          <el-divider></el-divider>
          <div class="overview-item">
            <p class="label">库存总价值</p>
            <p class="value">¥ {{ dashboard.totalValue || 0 }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="9">
        <el-card>
          <div slot="header" class="flex-between">
            <span>库存预警分布</span>
          </div>
          <div ref="warningChart" style="height: 200px;"></div>
        </el-card>
      </el-col>
      <el-col :span="9">
        <el-card>
          <div slot="header" class="flex-between">
            <span>快捷操作</span>
          </div>
          <div class="quick-actions">
            <el-button type="primary" icon="el-icon-edit" @click="$router.push('/requisitions/create')">
              提交申领
            </el-button>
            <el-button type="success" icon="el-icon-tickets" @click="$router.push('/requisitions')">
              申领列表
            </el-button>
            <el-button type="warning" icon="el-icon-document" @click="$router.push('/supplies-catalog')">
              查看物资
            </el-button>
            <el-button v-if="isAdmin" type="info" icon="el-icon-s-shop" @click="$router.push('/purchases')">
              采购管理
            </el-button>
            <el-button v-if="isAdmin || isAdminStaff" type="danger" icon="el-icon-s-claim" @click="$router.push('/inventory/check')">
              库存盘点
            </el-button>
            <el-button v-if="isAdmin" icon="el-icon-user" @click="$router.push('/system/users')">
              用户管理
            </el-button>
            <el-button v-if="isAdmin || isAdminStaff" type="warning" icon="el-icon-lightbulb" @click="goToPurchaseSuggestions">
              采购建议
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="mb-20">
          <div slot="header" class="flex-between">
            <span>库存预警清单</span>
            <el-tag v-if="dashboard.warningSupplyList && dashboard.warningSupplyList.length > 0" type="danger">
              {{ dashboard.warningSupplyList.length }} 项预警
            </el-tag>
          </div>
          <el-table :data="dashboard.warningSupplyList || []" size="small" v-if="dashboard.warningSupplyList && dashboard.warningSupplyList.length > 0" max-height="350">
            <el-table-column prop="supplyName" label="物资名称"></el-table-column>
            <el-table-column prop="unit" label="单位" width="70"></el-table-column>
            <el-table-column prop="stock" label="当前库存" width="90"></el-table-column>
            <el-table-column prop="safeStockQuantity" label="安全库存" width="90"></el-table-column>
            <el-table-column label="可用天数" width="80">
              <template slot-scope="scope">
                <span>{{ scope.row.stockDays >= 999 ? '∞' : scope.row.stockDays + '天' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="预警等级" width="90">
              <template slot-scope="scope">
                <el-tag :type="getWarningTagType(scope.row.warningLevel)" size="mini">
                  {{ scope.row.warningLevelDesc }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无预警物资"></el-empty>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <div slot="header" class="flex-between">
            <span>低库存预警（原）</span>
            <el-tag v-if="dashboard.lowStockList && dashboard.lowStockList.length > 0" type="danger">
              {{ dashboard.lowStockList.length }} 项需要补货
            </el-tag>
          </div>
          <el-table :data="dashboard.lowStockList || []" size="small" v-if="dashboard.lowStockList && dashboard.lowStockList.length > 0" max-height="350">
            <el-table-column prop="supplyName" label="物资名称"></el-table-column>
            <el-table-column prop="unit" label="单位" width="80"></el-table-column>
            <el-table-column prop="stock" label="当前库存" width="100"></el-table-column>
            <el-table-column prop="minStock" label="最低库存" width="100"></el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag type="danger" size="mini">库存不足</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无低库存物资"></el-empty>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog :visible.sync="showMessagePanel" title="预警消息中心" width="70%" top="5vh">
      <div class="message-panel">
        <div class="panel-header">
          <div>
            <el-button type="primary" size="small" :disabled="unreadCount.total === 0" @click="handleMarkAllRead">全部标记已读</el-button>
          </div>
          <el-tabs v-model="messageFilter" size="small">
            <el-tab-pane label="全部" name="all"></el-tab-pane>
            <el-tab-pane label="未读" name="unread"></el-tab-pane>
          </el-tabs>
        </div>
        <div class="message-list" v-loading="messageLoading">
          <div v-for="msg in filteredMessages" :key="msg.id" class="message-item" :class="{ unread: !msg.readFlag }">
            <div class="message-header">
              <el-tag :type="getWarningTagType(msg.warningLevel)" size="mini">{{ msg.warningLevelDesc }}</el-tag>
              <span class="message-time">{{ formatTime(msg.createTime) }}</span>
            </div>
            <div class="message-content">{{ msg.warningContent }}</div>
            <div class="message-footer" v-if="!msg.readFlag">
              <el-button type="text" size="mini" @click="handleMarkRead(msg)">标记已读</el-button>
            </div>
          </div>
          <el-empty v-if="filteredMessages.length === 0" description="暂无消息"></el-empty>
        </div>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="showWarningPanel" title="库存预警详情" width="80%" top="5vh">
      <div>
        <el-row :gutter="20" class="mb-20">
          <el-col :span="8">
            <div class="trend-card" style="background: #67C23A">
              <div class="trend-label">正常</div>
              <div class="trend-value">{{ warningStats.normalCount || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="trend-card" style="background: #E6A23C">
              <div class="trend-label">关注/警告</div>
              <div class="trend-value">{{ (warningStats.attentionCount || 0) + (warningStats.warningCount || 0) }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="trend-card" style="background: #F56C6C">
              <div class="trend-label">紧急</div>
              <div class="trend-value">{{ warningStats.urgentCount || 0 }}</div>
            </div>
          </el-col>
        </el-row>
        <el-table :data="dashboard.warningSupplyList || []" size="small">
          <el-table-column prop="supplyName" label="物资名称" width="150"></el-table-column>
          <el-table-column prop="categoryName" label="分类" width="100"></el-table-column>
          <el-table-column prop="stock" label="当前库存" width="90"></el-table-column>
          <el-table-column prop="safeStockQuantity" label="安全库存" width="90"></el-table-column>
          <el-table-column prop="dailyConsumption" label="日均消耗" width="90"></el-table-column>
          <el-table-column label="可用天数" width="80">
            <template slot-scope="scope">
              <span>{{ scope.row.stockDays >= 999 ? '充足' : scope.row.stockDays + '天' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="7天预测" width="90">
            <template slot-scope="scope">
              <span :style="{ color: scope.row.predictedStockIn7Days < scope.row.safeStockQuantity ? '#F56C6C' : '' }">
                {{ scope.row.predictedStockIn7Days }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="30天预测" width="90">
            <template slot-scope="scope">
              <span :style="{ color: scope.row.predictedStockIn30Days < scope.row.safeStockQuantity ? '#F56C6C' : '' }">
                {{ scope.row.predictedStockIn30Days }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="预警等级" width="90">
            <template slot-scope="scope">
              <el-tag :type="getWarningTagType(scope.row.warningLevel)" size="mini">
                {{ scope.row.warningLevelDesc }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getDashboardData } from '@/api/statistics'
import {
  getUnreadStatistics,
  getWarningMessages,
  markMessageAsRead,
  markAllMessagesAsRead
} from '@/api/warning'
import * as echarts from 'echarts'

export default {
  name: 'Dashboard',
  data() {
    return {
      dashboard: {},
      warningStats: {},
      unreadCount: {},
      showMessagePanel: false,
      showWarningPanel: false,
      messageLoading: false,
      messageFilter: 'all',
      messages: [],
      warningChart: null
    }
  },
  computed: {
    isAdmin() {
      return this.$store.state.roleCode === 'ADMIN'
    },
    isAdminStaff() {
      return this.$store.state.roleCode === 'ADMIN_STAFF'
    },
    warningBgColor() {
      if (this.warningStats.urgentCount > 0) return '#F56C6C'
      if (this.warningStats.warningCount > 0) return '#E6A23C'
      if (this.warningStats.attentionCount > 0) return '#409EFF'
      return '#909399'
    },
    filteredMessages() {
      if (this.messageFilter === 'unread') {
        return this.messages.filter(m => !m.readFlag)
      }
      return this.messages
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.dashboard = await getDashboardData()
      this.warningStats = this.dashboard.warningStatistics || {}
      this.unreadCount = this.dashboard.unreadWarningCount || {}
      this.$nextTick(() => {
        this.initWarningChart()
      })
    },
    initWarningChart() {
      if (!this.$refs.warningChart) return
      if (this.warningChart) {
        this.warningChart.dispose()
      }
      this.warningChart = echarts.init(this.$refs.warningChart)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'center'
        },
        series: [
          {
            name: '预警等级',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '16',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              { value: this.warningStats.normalCount || 0, name: '正常', itemStyle: { color: '#67C23A' } },
              { value: this.warningStats.attentionCount || 0, name: '关注', itemStyle: { color: '#409EFF' } },
              { value: this.warningStats.warningCount || 0, name: '警告', itemStyle: { color: '#E6A23C' } },
              { value: this.warningStats.urgentCount || 0, name: '紧急', itemStyle: { color: '#F56C6C' } }
            ]
          }
        ]
      }
      this.warningChart.setOption(option)
    },
    getWarningTagType(level) {
      const map = {
        NORMAL: 'success',
        ATTENTION: '',
        WARNING: 'warning',
        URGENT: 'danger'
      }
      return map[level] || ''
    },
    async handleMarkRead(msg) {
      await markMessageAsRead(msg.id)
      msg.readFlag = true
      this.unreadCount.total = Math.max(0, this.unreadCount.total - 1)
    },
    async handleMarkAllRead() {
      await markAllMessagesAsRead()
      this.messages.forEach(m => m.readFlag = true)
      this.unreadCount = { total: 0, urgent: 0, warning: 0, attention: 0 }
      this.$message.success('已全部标记为已读')
    },
    async loadMessages() {
      this.messageLoading = true
      try {
        this.messages = await getWarningMessages({})
      } finally {
        this.messageLoading = false
      }
    },
    formatTime(time) {
      if (!time) return ''
      return time.replace('T', ' ').substring(0, 16)
    },
    goToPurchaseSuggestions() {
      this.$router.push('/statistics')
    }
  },
  watch: {
    showMessagePanel(val) {
      if (val) {
        this.loadMessages()
      }
    }
  },
  beforeDestroy() {
    if (this.warningChart) {
      this.warningChart.dispose()
    }
  }
}
</script>

<style scoped lang="scss">
.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
  &:hover {
    transform: translateY(-2px);
  }
  .stat-content {
    display: flex;
    align-items: center;
    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 28px;
      margin-right: 16px;
    }
    .stat-info {
      flex: 1;
      .stat-label {
        color: #909399;
        font-size: 14px;
        margin: 0 0 6px;
      }
      .stat-value {
        color: #303133;
        font-size: 24px;
        font-weight: 600;
        margin: 0;
      }
      .warning-sub-info {
        margin-top: 4px;
        font-size: 12px;
        .urgent {
          color: #F56C6C;
          margin-right: 8px;
        }
        .warning {
          color: #E6A23C;
          margin-right: 8px;
        }
        .attention {
          color: #409EFF;
        }
      }
    }
  }
}
.overview-item {
  text-align: center;
  padding: 10px 0;
  .label {
    color: #909399;
    margin: 0 0 8px;
  }
  .value {
    color: #303133;
    font-size: 28px;
    font-weight: 600;
    margin: 0;
  }
}
.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.message-panel {
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }
  .message-list {
    max-height: 500px;
    overflow-y: auto;
  }
  .message-item {
    padding: 12px 16px;
    border-bottom: 1px solid #EBEEF5;
    border-radius: 4px;
    margin-bottom: 8px;
    background: #fff;
    &.unread {
      background: #fef0f0;
      border-left: 3px solid #F56C6C;
    }
    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      .message-time {
        font-size: 12px;
        color: #909399;
      }
    }
    .message-content {
      font-size: 14px;
      color: #303133;
      line-height: 1.6;
    }
    .message-footer {
      text-align: right;
      margin-top: 8px;
    }
  }
}
.trend-card {
  color: #fff;
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  .trend-label {
    font-size: 14px;
    opacity: 0.9;
  }
  .trend-value {
    font-size: 32px;
    font-weight: 600;
    margin-top: 8px;
  }
}
.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mb-20 {
  margin-bottom: 20px;
}
</style>
