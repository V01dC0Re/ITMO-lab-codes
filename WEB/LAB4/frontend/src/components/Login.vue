<template>
  <div class="login-container">
    <header class="header">
      <h1>Иван Иванов</h1>
      <p>Группа: P3212, Вариант: 12345</p>
    </header>
    
    <form @submit.prevent="handleSubmit" class="login-form">
      <div class="form-group">
        <label for="login">Логин:</label>
        <input type="text" id="login" v-model="credentials.login" required>
      </div>
      <div class="form-group">
        <label for="password">Пароль:</label>
        <input type="password" id="password" v-model="credentials.password" required>
      </div>
      <button type="submit" class="submit-btn">Войти</button>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  data() {
    return {
      credentials: {
        login: 'student',
        password: 'password'
      },
      error: ''
    };
  },
  methods: {
    async handleSubmit() {
      try {
        await api.login(this.credentials.login, this.credentials.password);
        this.$router.push('/main');
      } catch (err) {
        this.error = 'Неверный логин или пароль';
      }
    }
  }
};
</script>

<style scoped>
/* Адаптивные стили для 3 режимов */
@media (min-width: 1084px) {
  .login-container { /* Десктопный */ }
}
@media (max-width: 1083px) and (min-width: 844px) {
  .login-container { /* Планшетный */ }
}
@media (max-width: 843px) {
  .login-container { /* Мобильный */ }
}
</style>
