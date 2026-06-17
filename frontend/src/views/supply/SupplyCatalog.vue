<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">物资目录</h3>
    </div>
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="物资名称/编码" clearable @keyup.enter.native="loadData"></el-input>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id"></el-option>
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
        <el-table-column prop="supplyCode" label="编码" width="120"></el-table-column>
        <el-table-column prop="supplyName" label="物资名称"></el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100"></el-table-column>
        <el-table-column prop="unit" label="单位" width="80"></el-table-column>
        <el-table-column prop="specification" label="规格" width="150"></el-table-column>
        <el-table-column prop="price" label="单价(元)" width="100"></el-table-column>
        <el-table-column label="库存状态" width="140">
          <template slot-scope="scope">
            <el-tag :type="scope.row.stock <= scope.row.minStock ? 'danger' : 'success'" size="mini">
              {{ scope.row.stock <= scope.row.minStock ? '库存不足' : '库存充足' }}
            </el-tag>
            <span style="margin-left: 5px">{{ scope.row.stock }} {{ scope.row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" icon="el-icon-s-order" @click="quickRequisition(scope.row)">申领</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { getSupplyList, getCategoryList } from '@/api/supply'

export default {
  name: 'SupplyCatalog',
  data() {
    return {
      categories: [],
      tableData: [],
      searchForm: { keyword: '', categoryId: null }
    }
  },
  mounted() {
    this.loadCategories()
    this.loadData()
  },
  methods: {
    async loadCategories() {
      this.categories = await getCategoryList()
    },
    async loadData() {
      this.tableData = await getSupplyList(this.searchForm)
    },
    resetSearch() {
      this.searchForm = { keyword: '', categoryId: null }
      this.loadData()
    },
    quickRequisition(row) {
      this.$router.push({
        path: '/requisitions/create',
        query: { supplyId: row.id, supplyName: row.supplyName }
      })
    }
  }
}
</script>
