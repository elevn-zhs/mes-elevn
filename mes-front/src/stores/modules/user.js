import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/auth'
import { getToken, setToken, removeToken, getUser, setUser, clearAuth } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref(getUser())

  async function login(loginForm) {
    try {
      const res = await loginApi(loginForm)
      token.value = res.data.token
      setToken(res.data.token)
      userInfo.value = res.data.user
      setUser(res.data.user)
      return res
    } catch (error) {
      throw error
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // ignore
    }
    clearAuth()
    token.value = null
    userInfo.value = null
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfoApi()
      userInfo.value = res.data
      setUser(res.data)
      return res.data
    } catch (error) {
      throw error
    }
  }

  function resetToken() {
    removeToken()
    token.value = null
  }

  return {
    token,
    userInfo,
    login,
    logout,
    fetchUserInfo,
    resetToken
  }
})