<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">创建采购单</h3>
      <el-button icon="el-icon-back" @click="$router.back()">返回</el-button>
    </div>
    <el-card>
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierName">
              <el-input v-model="form.supplierName" placeholder="请输入供应商名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="选填"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-divider content-position="left">采购物资</el-divider>

      <el-table :data="form.items" border>
        <el-table-column label="物资" min-width="250">
          <template slot-scope="scope">
            <el-select v-model="scope.row.supplyId" placeholder="请选择物资" filterable style="width: 100%">
              <el-option v-for="s in lowStockSupplies" :key="s.id" :label="s.supplyName + ' (' + s.supplyCode + ') - 库存:' + s.stock" :value="s.id"></el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="单位" width="80">
          <template slot-scope="scope">
            <span>{{ getSupplyField(scope.row.supplyId, 'unit') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="采购数量" width="180">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.quantity" :min="1" style="width: 100%"></el-input-number>
          </template>
        </el-table-column>
        <el-table-column label="单价(元)" width="180">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" style="width: 100%"></el-input-number>
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template slot-scope="scope">
            <span>{{ (scope.row.quantity * (scope.row.unitPrice || 0)).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template slot-scope="scope">
            <el-button size="mini" type="danger" icon="el-icon-delete" @click="removeItem(scope.$index)"></el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-button style="margin-top: 15px" type="primary" icon="el-icon-plus" @click="addItem">添加物资</el-button>

      <el-divider></el-divider>

      <div class="text-right mb-10">
        <strong>总金额：¥ {{ totalAmount.toFixed(2) }}</strong>
      </div>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-card>
  </div>
</template>

<script>
import { getLowStockSupplies } from '@/api/supply'
import { createPurchase } from '@/api/purchase'

export default {
  name: 'PurchaseCreate',
  data() {
    return {
      lowStockSupplies: [],
      form: {
        supplierName: '',
        remark: '',
        items: [{ supplyId: null, quantity: 1, unitPrice: 0 }]
      },
      rules: {
        supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }]
      }
    }
  },
  computed: {
    totalAmount() {
      return this.form.items.reduce((sum, item) => sum + item.quantity * (item.unitPrice || 0), 0)
    }
  },
  mounted() {
    this.loadLowStockSupplies()
  },
  methods: {
    async loadLowStockSupplies() {
      this.lowStockSupplies = await getLowStockSupplies()
    },
    getSupplyField(id, field) {
      const s = this.lowStockSupplies.find(i => i.id === id)
      return s ? s[field] : ''
    },
    addItem() {
      this.form.items.push({ supplyId: null, quantity: 1, unitPrice: 0 })
    },
    removeItem(index) {
      if (this.form.items.length > 1) {
        this.form.items.splice(index, 1)
      } else {
        this.$message.warning('至少保留一条物资')
      }
    },
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (!this.form.items.length || this.form.items.some(i => !i.supplyId || !i.quantity)) {
          this.$message.error('请完整填写所有采购物资信息')
          return
        }
        this.form.items.forEach(item => {
          item.totalPrice = item.quantity * (item.unitPrice || 0)
        })
        this.form.totalAmount = this.totalAmount
        await createPurchase(this.form)
        this.$message.success('创建成功')
        this.$router.push('/purchases')
      })
    }
  }
}
</script>
