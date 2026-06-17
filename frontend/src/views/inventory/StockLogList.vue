<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">库存流水</h3>
    </div>
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="物资">
          <el-select v-model="searchForm.supplyId" placeholder="全部" clearable filterable style="width: 220px">
            <el-option v-for="s in supplies" :key="s.id" :label="s.supplyName + ' (' + s.supplyCode + ')'" :value="s.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="全部" clearable style="width: 140px">
            <el-option label="入库" value="IN"></el-option>
            <el-option label="出库" value="OUT"></el-option>
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
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="supplyName" label="物资名称"></el-table-column>
        <el-table-column label="操作类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.operationType === 'IN' ? 'success' : 'danger'" size="mini">
              {{ scope.row.operationType === 'IN' ? '入库' : '出库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100"></el-table-column>
        <el-table-column prop="beforeStock" label="变动前库存" width="120"></el-table-column>
        <el-table-column prop="afterStock" label="变动后库存" width="120"></el-table-column>
        <el-table-column prop="relatedNo" label="关联单号" width="160"></el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100"></el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180"></el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { getStockLogs } from '@/api/inventory'
import { getSupplyList } from '@/api/supply'

export default {
  name: 'StockLogList',
  data() {
    return {
      tableData: [],
      supplies: [],
      searchForm: { supplyId: null, operationType: '' }
    }
  },
  mounted() {
    this.loadSupplies()
    this.loadData()
  },
  methods: {
    async loadSupplies() {
      this.supplies = await getSupplyList({})
    },
    async loadData() {
      this.tableData = await getStockLogs(this.searchForm)
    },
    resetSearch() {
      this.searchForm = { supplyId: null, operationType: '' }
      this.loadData()
    }
  }
}
</script>
