<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">盘点详情</h3>
      <el-button icon="el-icon-back" @click="$router.back()">返回</el-button>
    </div>
    <el-card v-if="detailData">
      <el-descriptions :column="2" border class="mb-20">
        <el-descriptions-item label="盘点单号">{{ detailData.checkNo }}</el-descriptions-item>
        <el-descriptions-item label="盘点类型">{{ detailData.checkType === 'FULL' ? '全盘' : '抽盘' }}</el-descriptions-item>
        <el-descriptions-item label="盘点人">{{ detailData.checkedByName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 'DRAFT' ? 'warning' : 'success'">
            {{ detailData.status === 'DRAFT' ? '草稿' : '已完成' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <h4>盘点明细</h4>
      <el-table :data="detailData.items" border>
        <el-table-column prop="supplyName" label="物资名称"></el-table-column>
        <el-table-column prop="supplyCode" label="编码" width="120"></el-table-column>
        <el-table-column prop="unit" label="单位" width="80"></el-table-column>
        <el-table-column prop="systemStock" label="系统库存" width="120"></el-table-column>
        <el-table-column label="实际库存" width="160">
          <template slot-scope="scope">
            <el-input-number
              v-model="scope.row.actualStock"
              :min="0"
              :disabled="detailData.status === 'COMPLETED'"
              style="width: 100%">
            </el-input-number>
          </template>
        </el-table-column>
        <el-table-column label="差异" width="100">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.actualStock - scope.row.systemStock !== 0 ? '#F56C6C' : '' }">
              {{ scope.row.actualStock - scope.row.systemStock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="差异原因" min-width="200">
          <template slot-scope="scope">
            <el-input
              v-model="scope.row.diffReason"
              :disabled="detailData.status === 'COMPLETED'"
              placeholder="请填写差异原因">
            </el-input>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="detailData.status === 'DRAFT'" style="margin-top: 20px">
        <el-button type="primary" @click="handleSave">保存修改</el-button>
        <el-button type="success" @click="handleComplete">完成盘点</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getCheckDetail, updateCheckItems, completeCheck } from '@/api/inventory'

export default {
  name: 'InventoryCheckDetail',
  data() {
    return {
      detailData: null
    }
  },
  mounted() {
    this.loadDetail()
  },
  methods: {
    async loadDetail() {
      this.detailData = await getCheckDetail(this.$route.params.id)
    },
    async handleSave() {
      await updateCheckItems(this.detailData.id, this.detailData.items)
      this.$message.success('保存成功')
      this.loadDetail()
    },
    handleComplete() {
      this.$confirm('确认完成本次盘点吗？完成后不可修改。', '提示', { type: 'warning' }).then(async () => {
        await completeCheck(this.detailData.id)
        this.$message.success('盘点已完成')
        this.loadDetail()
      }).catch(() => {})
    }
  }
}
</script>
