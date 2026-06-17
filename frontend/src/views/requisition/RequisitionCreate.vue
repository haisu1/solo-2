<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">提交申领</h3>
      <el-button icon="el-icon-back" @click="$router.back()">返回</el-button>
    </div>
    <el-card>
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用途" prop="purpose">
              <el-input v-model="form.purpose" placeholder="请输入申领用途"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="选填"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-divider content-position="left">申领物资</el-divider>

      <el-table :data="form.items" border>
        <el-table-column label="物资" min-width="250">
          <template slot-scope="scope">
            <el-select v-model="scope.row.supplyId" placeholder="请选择物资" filterable style="width: 100%" @change="onSupplyChange(scope.$index)">
              <el-option v-for="s in supplies" :key="s.id" :label="s.supplyName + ' (' + s.supplyCode + ') - 库存:' + s.stock" :value="s.id"></el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="规格" width="120">
          <template slot-scope="scope">
            <span>{{ getSupplyField(scope.row.supplyId, 'specification') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单位" width="80">
          <template slot-scope="scope">
            <span>{{ getSupplyField(scope.row.supplyId, 'unit') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="100">
          <template slot-scope="scope">
            <span>{{ getSupplyField(scope.row.supplyId, 'stock') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申领数量" width="180">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.quantity" :min="1" style="width: 100%"></el-input-number>
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

      <el-form-item>
        <el-button type="primary" @click="handleSubmit">提交申请</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-card>
  </div>
</template>

<script>
import { getSupplyList } from '@/api/supply'
import { createRequisition } from '@/api/requisition'

export default {
  name: 'RequisitionCreate',
  data() {
    return {
      supplies: [],
      form: {
        purpose: '',
        remark: '',
        items: [{ supplyId: null, quantity: 1 }]
      },
      rules: {
        purpose: [{ required: true, message: '请输入用途', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadSupplies()
    if (this.$route.query.supplyId) {
      this.form.items[0].supplyId = Number(this.$route.query.supplyId)
    }
  },
  methods: {
    async loadSupplies() {
      this.supplies = await getSupplyList({})
    },
    getSupplyField(id, field) {
      const s = this.supplies.find(i => i.id === id)
      return s ? s[field] : ''
    },
    onSupplyChange(index) {
      const item = this.form.items[index]
      const s = this.supplies.find(i => i.id === item.supplyId)
      if (s) {
        item.unitPrice = s.price
      }
    },
    addItem() {
      this.form.items.push({ supplyId: null, quantity: 1 })
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
          this.$message.error('请完整填写所有申领物资信息')
          return
        }
        this.form.items.forEach(item => {
          const s = this.supplies.find(i => i.id === item.supplyId)
          if (s) {
            item.unitPrice = s.price
            item.totalPrice = s.price * item.quantity
          }
        })
        await createRequisition(this.form)
        this.$message.success('提交成功')
        this.$router.push('/requisitions')
      })
    }
  }
}
</script>
