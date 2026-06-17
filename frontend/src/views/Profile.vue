<template>
  <div class="page-container">
    <el-card>
      <div slot="header" class="flex-between">
        <span>个人中心</span>
      </div>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="info">
          <el-form :model="userInfo" :rules="rules" ref="userForm" label-width="100px" style="max-width: 500px">
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled></el-input>
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="userInfo.realName"></el-input>
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userInfo.phone"></el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userInfo.email"></el-input>
            </el-form-item>
            <el-form-item label="部门">
              <el-input v-model="userInfo.departmentName" disabled></el-input>
            </el-form-item>
            <el-form-item label="角色">
              <el-input v-model="userInfo.roleName" disabled></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px" style="max-width: 500px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updatePassword">确认修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { getProfile, updateProfile } from '@/api/profile'
import { updatePassword } from '@/api/auth'

export default {
  name: 'Profile',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      activeTab: 'info',
      userInfo: {},
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
      },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadProfile()
  },
  methods: {
    async loadProfile() {
      this.userInfo = await getProfile()
    },
    async updateProfile() {
      this.$refs.userForm.validate(async valid => {
        if (valid) {
          await updateProfile(this.userInfo)
          this.$message.success('修改成功')
          await this.$store.dispatch('getUserInfo')
        }
      })
    },
    async updatePassword() {
      this.$refs.passwordForm.validate(async valid => {
        if (valid) {
          await updatePassword(this.passwordForm)
          this.$message.success('密码修改成功')
          this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
        }
      })
    }
  }
}
</script>
