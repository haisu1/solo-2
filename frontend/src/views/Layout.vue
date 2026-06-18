<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">
        <h3>办公用品申领</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF">
        <el-menu-item index="/dashboard">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
        </el-menu-item>
        <el-submenu index="supply" v-if="isAdmin || isAdminStaff">
          <template slot="title">
            <i class="el-icon-s-goods"></i>
            <span>物资管理</span>
          </template>
          <el-menu-item index="/categories" v-if="isAdmin">
            <i class="el-icon-menu"></i>
            <span slot="title">物资分类</span>
          </el-menu-item>
          <el-menu-item index="/supplies">
            <i class="el-icon-box"></i>
            <span slot="title">物资列表</span>
          </el-menu-item>
          <el-menu-item index="/supplies-catalog">
            <i class="el-icon-document"></i>
            <span slot="title">物资目录</span>
          </el-menu-item>
        </el-submenu>
        <el-menu-item index="/supplies-catalog" v-if="isEmployee">
          <i class="el-icon-document"></i>
          <span slot="title">物资目录</span>
        </el-menu-item>
        <el-submenu index="requisition">
          <template slot="title">
            <i class="el-icon-s-order"></i>
            <span>申领管理</span>
          </template>
          <el-menu-item index="/requisitions">
            <i class="el-icon-tickets"></i>
            <span slot="title">申领列表</span>
          </el-menu-item>
          <el-menu-item index="/requisitions/create">
            <i class="el-icon-edit"></i>
            <span slot="title">提交申领</span>
          </el-menu-item>
        </el-submenu>
        <el-submenu index="purchase" v-if="isAdmin">
          <template slot="title">
            <i class="el-icon-s-shop"></i>
            <span>采购管理</span>
          </template>
          <el-menu-item index="/purchases">
            <i class="el-icon-notebook-2"></i>
            <span slot="title">采购单列表</span>
          </el-menu-item>
          <el-menu-item index="/purchases/create">
            <i class="el-icon-plus"></i>
            <span slot="title">创建采购单</span>
          </el-menu-item>
        </el-submenu>
        <el-submenu index="inventory" v-if="isAdmin || isAdminStaff">
          <template slot="title">
            <i class="el-icon-s-claim"></i>
            <span>库存管理</span>
          </template>
          <el-menu-item index="/inventory/check">
            <i class="el-icon-edit-outline"></i>
            <span slot="title">库存盘点</span>
          </el-menu-item>
          <el-menu-item index="/inventory/logs">
            <i class="el-icon-time"></i>
            <span slot="title">库存流水</span>
          </el-menu-item>
        </el-submenu>
        <el-menu-item index="/statistics" v-if="isAdmin || isAdminStaff">
          <i class="el-icon-s-data"></i>
          <span slot="title">数据统计</span>
        </el-menu-item>
        <el-submenu index="approval">
          <template slot="title">
            <i class="el-icon-s-check"></i>
            <span>审批中心</span>
          </template>
          <el-menu-item index="/approval/tasks">
            <i class="el-icon-message-solid"></i>
            <span slot="title">我的待办</span>
          </el-menu-item>
          <el-menu-item index="/approval/flow" v-if="isAdmin">
            <i class="el-icon-setting"></i>
            <span slot="title">流程配置</span>
          </el-menu-item>
        </el-submenu>
        <el-menu-item index="/system/users" v-if="isAdmin">
          <i class="el-icon-user"></i>
          <span slot="title">用户管理</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <i class="el-icon-setting"></i>
          <span slot="title">个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <span class="breadcrumb">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-popover
            placement="bottom-end"
            width="400"
            v-model="messagePopoverVisible"
            trigger="click">
            <div class="warning-message-popover">
              <div class="popover-header">
                <span>预警消息</span>
                <el-button type="text" size="mini" @click="goToDashboard">查看全部</el-button>
              </div>
              <div class="popover-list" v-loading="messageLoading">
                <div v-for="msg in latestMessages" :key="msg.id" class="popover-item" :class="{ unread: !msg.readFlag }">
                  <el-tag :type="getWarningTagType(msg.warningLevel)" size="mini">{{ msg.warningLevelDesc }}</el-tag>
                  <div class="popover-content">{{ msg.warningContent }}</div>
                  <div class="popover-time">{{ formatTime(msg.createTime) }}</div>
                </div>
                <el-empty v-if="latestMessages.length === 0 && !messageLoading" description="暂无预警消息" :image-size="60"></el-empty>
              </div>
            </div>
            <el-badge slot="reference" :value="unreadTotal" :hidden="unreadTotal === 0" class="warning-badge" :max="99">
              <i class="el-icon-bell" style="font-size: 20px; cursor: pointer; color: #606266;"></i>
            </el-badge>
          </el-popover>
          <el-dropdown @command="handleCommand" style="margin-left: 20px;">
            <span class="user-info">
              <i class="el-icon-user-solid"></i>
              {{ user ? user.realName : '' }}
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { getUnreadStatistics, getWarningMessages } from '@/api/warning'

export default {
  name: 'Layout',
  data() {
    return {
      messagePopoverVisible: false,
      messageLoading: false,
      latestMessages: [],
      unreadTotal: 0,
      refreshTimer: null
    }
  },
  computed: {
    user() {
      return this.$store.state.user
    },
    isAdmin() {
      return this.$store.state.roleCode === 'ADMIN'
    },
    isAdminStaff() {
      return this.$store.state.roleCode === 'ADMIN_STAFF'
    },
    isEmployee() {
      return this.$store.state.roleCode === 'EMPLOYEE'
    },
    activeMenu() {
      return this.$route.path
    },
    currentTitle() {
      return this.$route.meta.title || ''
    }
  },
  mounted() {
    this.loadUnreadCount()
    this.refreshTimer = setInterval(() => {
      this.loadUnreadCount()
    }, 60000)
  },
  beforeDestroy() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
    }
  },
  watch: {
    messagePopoverVisible(val) {
      if (val) {
        this.loadLatestMessages()
      }
    }
  },
  methods: {
    async loadUnreadCount() {
      try {
        const stats = await getUnreadStatistics()
        this.unreadTotal = stats.total || 0
      } catch (e) {}
    },
    async loadLatestMessages() {
      this.messageLoading = true
      try {
        this.latestMessages = (await getWarningMessages({})).slice(0, 5)
      } finally {
        this.messageLoading = false
      }
    },
    goToDashboard() {
      this.messagePopoverVisible = false
      this.$router.push('/dashboard')
    },
    formatTime(time) {
      if (!time) return ''
      return time.replace('T', ' ').substring(0, 16)
    },
    getWarningTagType(level) {
      const map = { NORMAL: 'success', ATTENTION: '', WARNING: 'warning', URGENT: 'danger' }
      return map[level] || ''
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          await this.$store.dispatch('logout')
          this.$message.success('退出成功')
          this.$router.push('/login')
        }).catch(() => {})
      } else if (command === 'profile') {
        this.$router.push('/profile')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}
.layout-aside {
  background: #304156;
  overflow: hidden;
  .logo {
    height: 60px;
    line-height: 60px;
    text-align: center;
    background: #2b2f3a;
    h3 {
      color: #fff;
      margin: 0;
      font-size: 16px;
    }
  }
  .el-menu {
    border-right: none;
  }
}
.layout-header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  .header-left {
    .breadcrumb {
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
  }
  .header-right {
    display: flex;
    align-items: center;
    .warning-badge {
      margin-right: 5px;
    }
    .user-info {
      cursor: pointer;
      color: #606266;
      display: flex;
      align-items: center;
      gap: 5px;
    }
  }
}
.layout-main {
  background: #f0f2f5;
  padding: 0;
  overflow-y: auto;
}
.warning-message-popover {
  .popover-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 10px;
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 10px;
    font-weight: 600;
    font-size: 14px;
  }
  .popover-list {
    max-height: 350px;
    overflow-y: auto;
  }
  .popover-item {
    padding: 10px 0;
    border-bottom: 1px solid #f5f7fa;
    &:last-child {
      border-bottom: none;
    }
    &.unread {
      background: #fef0f0;
      margin: 0 -12px;
      padding: 10px 12px;
      border-radius: 4px;
    }
    .popover-content {
      font-size: 13px;
      color: #303133;
      margin: 6px 0;
      line-height: 1.5;
    }
    .popover-time {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>
