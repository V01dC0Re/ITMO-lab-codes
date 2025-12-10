import { createRouter, createWebHistory } from 'vue-router'
import Login from './components/Login.vue'
import Main from './components/Main.vue'

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login
  },
  {
    path: '/main',
    name: 'Main',
    component: Main,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory('/lab-app/'), // Критически важно для подкаталога!
  routes
})

// Защита маршрутов
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/')
  } else {
    next()
  }
})

export default router