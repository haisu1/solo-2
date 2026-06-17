<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">库存盘点</h3>
      <el-button type="primary" icon="el-icon-plus" @click="handleCreate">发起盘点</el-button>
    </div>
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="盘点单号" clearable @keyup.enter.native="loadData"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="草稿" value="DRAFT"></el-option>
            <el-option label="已完成" value="COMPLETED"></el-option>
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
        <el-table-column prop="checkNo" label="盘点单号" width="160"></el-table-column>
        <el-table-column prop="checkType" label="盘点类型" width="100">
          <template slot-scope="scope">
            {{ scope.row.checkType === 'FULL' ? '全盘' : '抽盘' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkedByName" label="盘点人" width="100"></el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'DRAFT' ? 'warning' : 'success'" size="mini">
              {{ scope.row.status === 'DRAFT' ? '草稿' : '已完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="$router.push('/inventory/check/' + scope.row.id)">详情</el-button>
            <el-button v-if="scope.row.status === 'DRAFT'" size="mini" type="success" @click="handleComplete(scope.row)">完成盘点</el-button>
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
  </div>
</template>

<script>
import { getCheckPage, createCheck, completeCheck } from '@/api/inventory'

export default {
  name: 'InventoryCheckList',
  data() {
    return {
      tableData: [],
      total: 0,
      searchForm: { current: 1, size: 10, keyword: '', status: '' }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      const res = await getCheckPage(this.searchForm)
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
    async handleCreate() {
      this.$confirm('确认发起一次全盘盘点吗？', '提示', { type: 'warning' }).then(async () => {
        const res = await createCheck({ checkType: 'FULL' })
        this.$message.success('创建成功')
        this.$router.push('/inventory/check/' + res.id)
      }).catch(() => {})
    },
    handleComplete(row) {
      this.$confirm('确认完成本次盘点吗？完成后不可修改。', '提示', { type: 'warning' }).then(async () => {
        await completeCheck(row.id)
        this.$message.success('盘点已完成')
        this.loadData()
      }).catch(() => {})
    }
  }
}
</script>
