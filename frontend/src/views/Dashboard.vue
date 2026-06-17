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
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C">
              <i class="el-icon-warning"></i>
            </div>
            <div class="stat-info">
              <p class="stat-label">低库存物资</p>
              <p class="stat-value">{{ dashboard.lowStockCount || 0 }}</p>
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
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-info">
              <p class="stat-label">待审批申领</p>
              <p class="stat-value">{{ dashboard.pendingRequisitionCount || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="mb-20">
          <div slot="header" class="flex-between">
            <span>库存总览</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="overview-item">
                <p class="label">库存总量</p>
                <p class="value">{{ dashboard.totalStock || 0 }}</p>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="overview-item">
                <p class="label">库存总价值</p>
                <p class="value">¥ {{ dashboard.totalValue || 0 }}</p>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <el-card>
          <div slot="header" class="flex-between">
            <span>低库存预警</span>
            <el-tag v-if="dashboard.lowStockList && dashboard.lowStockList.length > 0" type="danger">
              {{ dashboard.lowStockList.length }} 项需要补货
            </el-tag>
          </div>
          <el-table :data="dashboard.lowStockList || []" size="small" v-if="dashboard.lowStockList && dashboard.lowStockList.length > 0">
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

      <el-col :span="12">
        <el-card>
          <div slot="header">
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
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getDashboardData } from '@/api/statistics'

export default {
  name: 'Dashboard',
  data() {
    return {
      dashboard: {}
    }
  },
  computed: {
    isAdmin() {
      return this.$store.state.roleCode === 'ADMIN'
    },
    isAdminStaff() {
      return this.$store.state.roleCode === 'ADMIN_STAFF'
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.dashboard = await getDashboardData()
    }
  }
}
</script>

<style scoped lang="scss">
.stat-card {
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
    }
  }
}
.overview-item {
  text-align: center;
  padding: 20px;
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
</style>
