import Vue from 'vue'
import Vuex from 'vuex'
import { login, logout, getUserInfo } from '@/api/auth'

Vue.use(Vuex)

const store = new Vuex.Store({
  state: {
    token: localStorage.getItem('office_token') || '',
    user: JSON.parse(localStorage.getItem('office_user') || 'null'),
    roleCode: localStorage.getItem('office_role_code') || ''
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('office_token', token)
    },
    SET_USER(state, user) {
      state.user = user
      if (user) {
        state.roleCode = user.roleCode
        localStorage.setItem('office_user', JSON.stringify(user))
        localStorage.setItem('office_role_code', user.roleCode)
      } else {
        state.roleCode = ''
        localStorage.removeItem('office_user')
        localStorage.removeItem('office_role_code')
      }
    },
    CLEAR_TOKEN(state) {
      state.token = ''
      state.user = null
      state.roleCode = ''
      localStorage.removeItem('office_token')
      localStorage.removeItem('office_user')
      localStorage.removeItem('office_role_code')
    }
  },
  actions: {
    async login({ commit }, userInfo) {
      const data = await login(userInfo)
      commit('SET_TOKEN', data.token)
      commit('SET_USER', data.user)
      return data
    },
    async getUserInfo({ commit, state }) {
      const data = await getUserInfo()
      commit('SET_USER', data)
      return data
    },
    async logout({ commit }) {
      try {
        await logout()
      } finally {
        commit('CLEAR_TOKEN')
      }
    }
  }
})

export default store
