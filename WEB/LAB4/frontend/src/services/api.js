import axios from 'axios'

const api = axios.create({
  baseURL: '/lab-app/api'
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const AuthService = {
  async login(login, password) {
    const response = await api.post('/auth/login', new URLSearchParams({
      login: login,
      password: password
    }), {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
    return response.data
  },

  logout() {
    localStorage.removeItem('token')
  }
}

export const HitService = {
  async checkHit(x, y, r) {
    const response = await api.post('/hits', { x, y, r })
    return response.data
  },

  async getHistory() {
    const response = await api.get('/hits')
    return response.data
  }
}