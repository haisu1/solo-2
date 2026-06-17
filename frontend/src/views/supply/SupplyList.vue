<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">物资列表</h3>
      <el-button type="primary" icon="el-icon-plus" @click="openDialog()">新增物资</el-button>
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
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
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
        <el-table-column prop="supplyCode" label="编码" width="120"></el-table-column>
        <el-table-column prop="supplyName" label="名称"></el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100"></el-table-column>
        <el-table-column prop="unit" label="单位" width="80"></el-table-column>
        <el-table-column prop="specification" label="规格" width="120"></el-table-column>
        <el-table-column prop="price" label="单价(元)" width="100"></el-table-column>
        <el-table-column label="库存" width="120">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.stock <= scope.row.minStock ? '#F56C6C' : '' }">{{ scope.row.stock }}</span>
            <el-tag v-if="scope.row.stock <= scope.row.minStock" type="danger" size="mini" style="margin-left: 5px">低库存</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="100"></el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="mini" type="success" @click="openStockDialog(scope.row)">入库</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="物资编码" prop="supplyCode">
          <el-input v-model="form.supplyCode"></el-input>
        </el-form-item>
        <el-form-item label="物资名称" prop="supplyName">
          <el-input v-model="form.supplyName"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id"></el-option>
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单位">
              <el-input v-model="form.unit" placeholder="个/支/本..."></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格">
              <el-input v-model="form.specification"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单价">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="启用" :value="1"></el-option>
                <el-option label="禁用" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="当前库存">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最低库存">
              <el-input-number v-model="form.minStock" :min="0" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最高库存">
              <el-input-number v-model="form.maxStock" :min="0" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description" rows="3"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="物资入库" :visible.sync="stockDialogVisible" width="400px">
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="物资名称">
          <span>{{ currentSupply && currentSupply.supplyName }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span>{{ currentSupply && currentSupply.stock }}</span>
        </el-form-item>
        <el-form-item label="入库数量">
          <el-input-number v-model="stockForm.quantity" :min="1" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="stockForm.remark" rows="2"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockIn">确认入库</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getSupplyPage, getCategoryList, createSupply, updateSupply, deleteSupply, addStock
} from '@/api/supply'

export default {
  name: 'SupplyList',
  data() {
    return {
      categories: [],
      tableData: [],
      total: 0,
      searchForm: { current: 1, size: 10, keyword: '', categoryId: null, status: null },
      dialogVisible: false,
      dialogTitle: '新增物资',
      form: {},
      rules: {
        supplyName: [{ required: true, message: '请输入物资名称', trigger: 'blur' }],
        supplyCode: [{ required: true, message: '请输入物资编码', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
      },
      stockDialogVisible: false,
      currentSupply: null,
      stockForm: { quantity: 1, remark: '' }
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
      const res = await getSupplyPage(this.searchForm)
      this.tableData = res.records || []
      this.total = res.total || 0
    },
    resetSearch() {
      this.searchForm = { current: 1, size: 10, keyword: '', categoryId: null, status: null }
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
    openDialog(row) {
      if (row) {
        this.dialogTitle = '编辑物资'
        this.form = { ...row }
      } else {
        this.dialogTitle = '新增物资'
        this.form = { status: 1, stock: 0, minStock: 0, maxStock: 0, price: 0 }
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.clearValidate()
      })
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          if (this.form.id) {
            await updateSupply(this.form.id, this.form)
          } else {
            await createSupply(this.form)
          }
          this.$message.success('操作成功')
          this.dialogVisible = false
          this.loadData()
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定删除该物资吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteSupply(row.id)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    },
    openStockDialog(row) {
      this.currentSupply = row
      this.stockForm = { quantity: 1, remark: '' }
      this.stockDialogVisible = true
    },
    async handleStockIn() {
      await addStock(this.currentSupply.id, this.stockForm)
      this.$message.success('入库成功')
      this.stockDialogVisible = false
      this.loadData()
    }
  }
}
</script>
