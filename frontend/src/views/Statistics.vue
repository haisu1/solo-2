<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">数据统计 / 智能预警</h3>
    </div>

    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="采购建议" name="suggestions">
        <div class="suggestion-panel">
          <el-row :gutter="20" class="mb-20">
            <el-col :span="6">
              <div class="stat-box" style="background: #F56C6C">
                <div class="stat-label">紧急采购</div>
                <div class="stat-value">{{ suggestionStats.urgentCount || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-box" style="background: #E6A23C">
                <div class="stat-label">警告采购</div>
                <div class="stat-value">{{ suggestionStats.warningCount || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-box" style="background: #409EFF">
                <div class="stat-label">关注采购</div>
                <div class="stat-value">{{ suggestionStats.attentionCount || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-box" style="background: #67C23A">
                <div class="stat-label">预计总金额</div>
                <div class="stat-value">¥ {{ suggestionStats.totalAmount || 0 }}</div>
              </div>
            </el-col>
          </el-row>

          <el-card>
            <div slot="header" class="flex-between">
              <span>采购建议清单</span>
              <div>
                <el-button type="primary" size="small" icon="el-icon-refresh" @click="loadSuggestions">
                  刷新建议
                </el-button>
                <el-button
                  type="success"
                  size="small"
                  icon="el-icon-document-add"
                  :disabled="selectedSuggestions.length === 0"
                  @click="handleGeneratePurchase">
                  生成采购单 ({{ selectedSuggestions.length }})
                </el-button>
              </div>
            </div>
            <el-table :data="suggestions" v-loading="loadingSuggestions" @selection-change="handleSelectionChange" size="small">
              <el-table-column type="selection" width="50"></el-table-column>
              <el-table-column prop="supplyName" label="物资名称" width="150"></el-table-column>
              <el-table-column prop="categoryName" label="分类" width="100"></el-table-column>
              <el-table-column prop="unit" label="单位" width="60"></el-table-column>
              <el-table-column prop="currentStock" label="当前库存" width="90"></el-table-column>
              <el-table-column prop="inTransitQuantity" label="在途数量" width="90"></el-table-column>
              <el-table-column prop="safeStockQuantity" label="安全库存" width="90"></el-table-column>
              <el-table-column prop="dailyConsumption" label="日均消耗" width="90"></el-table-column>
              <el-table-column prop="predictedConsumption30Days" label="30天预测" width="90"></el-table-column>
              <el-table-column prop="eoqQuantity" label="EOQ批量" width="90"></el-table-column>
              <el-table-column prop="suggestedQuantity" label="建议采购量" width="100">
                <template slot-scope="scope">
                  <span style="color: #F56C6C; font-weight: 600;">{{ scope.row.suggestedQuantity }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="estimatedAmount" label="预估金额" width="110">
                <template slot-scope="scope">
                  ¥ {{ scope.row.estimatedAmount || 0 }}
                </template>
              </el-table-column>
              <el-table-column label="预警等级" width="90">
                <template slot-scope="scope">
                  <el-tag :type="getWarningTagType(scope.row.warningLevel)" size="mini">
                    {{ getWarningLevelText(scope.row.warningLevel) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="领用趋势分析" name="trend">
        <el-card class="mb-20">
          <div slot="header" class="flex-between">
            <span>领用趋势分析</span>
            <div>
              <el-select v-model="trendSupplyId" placeholder="选择物资" style="width: 200px; margin-right: 10px;" @change="loadTrendData">
                <el-option
                  v-for="s in allSupplies"
                  :key="s.id"
                  :label="s.supplyName"
                  :value="s.id">
                </el-option>
              </el-select>
              <el-radio-group v-model="trendPeriod" size="small" @change="loadTrendData">
                <el-radio-button label="WEEK">周</el-radio-button>
                <el-radio-button label="MONTH">月</el-radio-button>
                <el-radio-button label="QUARTER">季度</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div ref="trendChart" style="height: 350px;"></div>
          <el-row :gutter="20" v-if="trendData" style="margin-top: 20px;">
            <el-col :span="8">
              <div class="trend-stat">
                <div class="trend-stat-label">总消耗量</div>
                <div class="trend-stat-value">{{ trendData.totalConsumption || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="trend-stat">
                <div class="trend-stat-label">日均消耗</div>
                <div class="trend-stat-value">{{ trendData.avgDailyConsumption || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="trend-stat">
                <div class="trend-stat-label">趋势变化率</div>
                <div class="trend-stat-value" :style="{ color: (trendData.trendRate || 0) >= 0 ? '#F56C6C' : '#67C23A' }">
                  {{ ((trendData.trendRate || 0) * 100).toFixed(1) }}%
                  <i :class="(trendData.trendRate || 0) >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="预警统计" name="warning">
        <el-row :gutter="20" class="mb-20">
          <el-col :span="12">
            <el-card>
              <div slot="header">库存预警分布</div>
              <div ref="warningPieChart" style="height: 300px;"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <div slot="header" class="flex-between">
                <span>预警等级统计</span>
                <el-button type="primary" size="small" icon="el-icon-refresh" @click="recalculateWarnings">
                  重新计算预警
                </el-button>
              </div>
              <el-table :data="warningStatsList" border size="small">
                <el-table-column prop="level" label="预警等级"></el-table-column>
                <el-table-column prop="count" label="数量">
                  <template slot-scope="scope">
                    <el-tag :type="scope.row.type" size="small">{{ scope.row.count }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="desc" label="说明"></el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="申领统计" name="requisition">
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
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { getRequisitionStatistics } from '@/api/statistics'
import {
  getPurchaseSuggestions,
  getPurchaseSuggestionStatistics,
  getConsumptionTrend,
  getWarningStatistics,
  generatePurchaseFromSuggestions,
  recalculateWarnings
} from '@/api/warning'
import { getSupplyList } from '@/api/supply'
import * as echarts from 'echarts'

export default {
  name: 'Statistics',
  data() {
    return {
      activeTab: 'suggestions',
      statistics: {},
      statusStatsList: [],
      suggestions: [],
      selectedSuggestions: [],
      suggestionStats: {},
      loadingSuggestions: false,
      allSupplies: [],
      trendSupplyId: null,
      trendPeriod: 'MONTH',
      trendData: null,
      trendChart: null,
      warningPieChart: null,
      warningStats: {},
      warningStatsList: []
    }
  },
  mounted() {
    this.loadData()
    this.loadAllSupplies()
  },
  watch: {
    activeTab(val) {
      if (val === 'suggestions') {
        this.loadSuggestions()
      } else if (val === 'warning') {
        this.loadWarningStats()
      } else if (val === 'requisition') {
        this.loadRequisitionStats()
      }
    }
  },
  methods: {
    async loadData() {
      await this.loadSuggestions()
    },
    async loadRequisitionStats() {
      this.statistics = await getRequisitionStatistics()
      if (this.statistics.statusStats) {
        this.statusStatsList = Object.keys(this.statistics.statusStats).map(key => ({
          status: key,
          count: this.statistics.statusStats[key]
        }))
      }
    },
    async loadSuggestions() {
      this.loadingSuggestions = true
      try {
        this.suggestions = await getPurchaseSuggestions()
        this.suggestionStats = await getPurchaseSuggestionStatistics()
      } finally {
        this.loadingSuggestions = false
      }
    },
    async loadAllSupplies() {
      this.allSupplies = await getSupplyList()
      if (this.allSupplies && this.allSupplies.length > 0) {
        this.trendSupplyId = this.allSupplies[0].id
        this.$nextTick(() => {
          this.loadTrendData()
        })
      }
    },
    async loadTrendData() {
      if (!this.trendSupplyId) return
      this.trendData = await getConsumptionTrend(this.trendSupplyId, this.trendPeriod)
      this.$nextTick(() => {
        this.initTrendChart()
      })
    },
    initTrendChart() {
      if (!this.$refs.trendChart || !this.trendData) return
      if (this.trendChart) {
        this.trendChart.dispose()
      }
      this.trendChart = echarts.init(this.$refs.trendChart)
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.trendData.dateLabels || []
        },
        yAxis: {
          type: 'value',
          name: '消耗量'
        },
        series: [
          {
            name: '消耗量',
            type: 'line',
            smooth: true,
            data: this.trendData.consumptionData || [],
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(64,158,255,0.5)' },
                { offset: 1, color: 'rgba(64,158,255,0.05)' }
              ])
            },
            lineStyle: {
              color: '#409EFF',
              width: 2
            },
            itemStyle: {
              color: '#409EFF'
            }
          }
        ]
      }
      this.trendChart.setOption(option)
    },
    async loadWarningStats() {
      this.warningStats = await getWarningStatistics()
      this.warningStatsList = [
        { level: '正常', count: this.warningStats.normalCount || 0, type: 'success', desc: '库存充足' },
        { level: '关注', count: this.warningStats.attentionCount || 0, type: '', desc: '接近安全库存' },
        { level: '警告', count: this.warningStats.warningCount || 0, type: 'warning', desc: '低于安全库存' },
        { level: '紧急', count: this.warningStats.urgentCount || 0, type: 'danger', desc: '即将缺货或已缺货' }
      ]
      this.$nextTick(() => {
        this.initWarningPieChart()
      })
    },
    initWarningPieChart() {
      if (!this.$refs.warningPieChart) return
      if (this.warningPieChart) {
        this.warningPieChart.dispose()
      }
      this.warningPieChart = echarts.init(this.$refs.warningPieChart)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          bottom: '0%',
          left: 'center'
        },
        series: [
          {
            name: '预警等级',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: true,
              formatter: '{b}\n{c}'
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
      this.warningPieChart.setOption(option)
    },
    async recalculateWarnings() {
      await recalculateWarnings()
      this.$message.success('预警重新计算完成')
      this.loadWarningStats()
    },
    handleSelectionChange(val) {
      this.selectedSuggestions = val
    },
    handleGeneratePurchase() {
      this.$confirm('确定要为选中的 ' + this.selectedSuggestions.length + ' 项物资生成采购单吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const remark = '根据智能采购建议生成'
        const result = await generatePurchaseFromSuggestions(this.selectedSuggestions, remark)
        this.$message.success('采购单生成成功，单号：' + result.purchaseNo)
        this.loadSuggestions()
        this.$router.push('/purchases')
      }).catch(() => {})
    },
    getStatusText(status) {
      const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', CANCELLED: '已取消' }
      return map[status] || status
    },
    getStatusType(status) {
      const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' }
      return map[status] || 'info'
    },
    getWarningLevelText(level) {
      const map = { NORMAL: '正常', ATTENTION: '关注', WARNING: '警告', URGENT: '紧急' }
      return map[level] || level
    },
    getWarningTagType(level) {
      const map = { NORMAL: 'success', ATTENTION: '', WARNING: 'warning', URGENT: 'danger' }
      return map[level] || ''
    }
  },
  beforeDestroy() {
    if (this.trendChart) this.trendChart.dispose()
    if (this.warningPieChart) this.warningPieChart.dispose()
  }
}
</script>

<style scoped lang="scss">
.stat-box {
  color: #fff;
  padding: 20px;
  border-radius: 8px;
  .stat-label {
    font-size: 14px;
    opacity: 0.9;
  }
  .stat-value {
    font-size: 28px;
    font-weight: 600;
    margin-top: 8px;
  }
}
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
.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mb-20 {
  margin-bottom: 20px;
}
.trend-stat {
  text-align: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  .trend-stat-label {
    color: #909399;
    font-size: 14px;
    margin-bottom: 5px;
  }
  .trend-stat-value {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }
}
</style>
