<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>企业办公用品申领系统</h2>
        <p>Enterprise Office Supplies Management System</p>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginForm" class="login-form" @keyup.enter.native="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-tips">
        <p>测试账号：</p>
        <p>管理员：admin / 123456</p>
        <p>行政人员：adminstaff / 123456</p>
        <p>普通员工：employee1 / 123456</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      loginForm: {
        username: '',
        password: ''
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            await this.$store.dispatch('login', this.loginForm)
            this.$message.success('登录成功')
            this.$router.push('/')
          } catch (e) {
            console.error(e)
          } finally {
            this.loading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.login-header {
  text-align: center;
  margin-bottom: 30px;
  h2 {
    margin: 0 0 10px;
    color: #303133;
  }
  p {
    margin: 0;
    color: #909399;
    font-size: 13px;
  }
}
.login-form {
  .login-btn {
    width: 100%;
  }
}
.login-tips {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  p {
    margin: 4px 0;
  }
}
</style>
