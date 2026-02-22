import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  withCredentials: true
});

export default {
  login(login, password) {
    return api.post('/auth/login', { login, password }, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });
  },
  logout() {
    return api.post('/auth/logout');
  },
  checkHit(x, y, r) {
    return api.post('/hits', { x, y, r });
  },
  getHistory() {
    return api.get('/hits');
  }
};
