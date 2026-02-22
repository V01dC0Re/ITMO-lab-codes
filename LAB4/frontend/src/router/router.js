import Vue from 'vue';
import Router from 'vue-router';
import Login from '@/components/Login.vue';
import Main from '@/components/Main.vue';

Vue.use(Router);

const router = new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/',
      name: 'login',
      component: Login
    },
    {
      path: '/main',
      name: 'main',
      component: Main,
      meta: { requiresAuth: true }
    }
  ]
});

router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('user');
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/');
  } else if (to.path === '/' && isAuthenticated) {
    next('/main');
  } else {
    next();
  }
});

export default router;
