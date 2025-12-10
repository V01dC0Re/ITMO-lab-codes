<template>
  <div class="main-container">
    <h1>Проверка попадания точки</h1>
    
    <div class="controls">
      <div class="coordinate-group">
        <label>X:</label>
        <div class="button-group">
          <button v-for="val in [-3,-2,-1,0,1,2,3,4,5]" :key="val" 
                  @click="setX(val)" :class="{active: x === val}">
            {{ val }}
          </button>
        </div>
      </div>
      
      <div class="coordinate-group">
        <label>Y:</label>
        <input type="number" step="0.1" v-model="y" min="-5" max="3">
        <span class="error" v-if="yError">{{ yError }}</span>
      </div>
      
      <div class="coordinate-group">
        <label>R:</label>
        <div class="button-group">
          <button v-for="val in [-3,-2,-1,0,1,2,3,4,5]" :key="val" 
                  @click="setR(val)" :class="{active: r === val, disabled: val <= 0}"
                  :disabled="val <= 0">
            {{ val }}
          </button>
        </div>
      </div>
      
      <button @click="checkPoint" class="check-btn" :disabled="!isValid">Проверить</button>
      <button @click="logout" class="logout-btn">Выйти</button>
    </div>
    
    <ResponsiveCanvas 
      :points="points" 
      :radius="r" 
      @point-clicked="handleCanvasClick"
    />
    
    <table class="results-table">
      <thead>
        <tr>
          <th>X</th>
          <th>Y</th>
          <th>R</th>
          <th>Результат</th>
          <th>Время</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(point, index) in points" :key="index" :class="{hit: point.hit, miss: !point.hit}">
          <td>{{ point.x.toFixed(1) }}</td>
          <td>{{ point.y.toFixed(1) }}</td>
          <td>{{ point.r }}</td>
          <td>{{ point.hit ? 'Попадание' : 'Промах' }}</td>
          <td>{{ formatDate(point.timestamp) }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import ResponsiveCanvas from '@/components/ResponsiveCanvas.vue';
import api from '@/services/api';

export default {
  components: { ResponsiveCanvas },
  data() {
    return {
      x: 0,
      y: 0,
      r: 1,
      points: [],
      yError: ''
    };
  },
  computed: {
    isValid() {
      return this.r > 0 && !this.yError && this.y >= -5 && this.y <= 3;
    }
  },
  watch: {
    y(newVal) {
      const num = parseFloat(newVal);
      if (isNaN(num)) {
        this.yError = 'Введите число';
      } else if (num < -5 || num > 3) {
        this.yError = 'Y должен быть от -5 до 3';
      } else {
        this.yError = '';
      }
    }
  },
  async mounted() {
    await this.loadHistory();
  },
  methods: {
    setX(val) { this.x = val; },
    setR(val) { if (val > 0) this.r = val; },
    async checkPoint() {
      if (!this.isValid) return;
      
      try {
        const response = await api.checkHit(this.x, parseFloat(this.y), this.r);
        this.points.unshift(response.data);
      } catch (error) {
        console.error('Ошибка проверки точки:', error);
      }
    },
    async handleCanvasClick({ x, y }) {
      this.x = parseFloat(x.toFixed(1));
      this.y = parseFloat(y.toFixed(1));
      this.checkPoint();
    },
    async loadHistory() {
      try {
        const response = await api.getHistory();
        this.points = response.data.map(point => ({
          ...point,
          canvasX: 100 + point.x * 5,
          canvasY: 100 - point.y * 5
        }));
      } catch (error) {
        console.error('Ошибка загрузки истории:', error);
      }
    },
    async logout() {
      await api.logout();
      this.$router.push('/');
    },
    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleTimeString('ru-RU', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    }
  }
};
</script>

<style scoped>
/* Адаптивные стили для 3 режимов */
@media (min-width: 1084px) {
  .main-container { /* Десктопный */ }
}
@media (max-width: 1083px) and (min-width: 844px) {
  .main-container { /* Планшетный */ }
}
@media (max-width: 843px) {
  .main-container { /* Мобильный */ }
}
</style>
