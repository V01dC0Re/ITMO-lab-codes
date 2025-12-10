<template>
  <div class="canvas-container">
    <canvas ref="canvas" @click="handleCanvasClick"></canvas>
  </div>
</template>

<script>
export default {
  props: {
    points: Array,
    radius: Number
  },
  mounted() {
    this.drawCanvas();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    drawCanvas() {
      const canvas = this.$refs.canvas;
      const ctx = canvas.getContext('2d');
      const size = Math.min(canvas.width, canvas.height);
      
      // Очистка и масштабирование
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      ctx.scale(canvas.width / 200, canvas.height / 200);
      
      // Отрисовка сетки
      this.drawGrid(ctx);
      
      // Отрисовка области (пример для варианта)
      this.drawArea(ctx, this.radius);
      
      // Отрисовка точек
      this.points.forEach(point => {
        ctx.fillStyle = point.hit ? 'green' : 'red';
        ctx.beginPath();
        ctx.arc(point.canvasX, point.canvasY, 3, 0, Math.PI * 2);
        ctx.fill();
      });
    },
    drawGrid(ctx) {
      // Реализация сетки координат
    },
    drawArea(ctx, r) {
      // Отрисовка области (круг в 3-й четверти + треугольник)
      ctx.strokeStyle = '#00f';
      
      // Круг
      ctx.beginPath();
      ctx.arc(100, 100, r * 5, Math.PI, 1.5 * Math.PI);
      ctx.lineTo(100, 100);
      ctx.closePath();
      ctx.stroke();
      
      // Треугольник
      ctx.beginPath();
      ctx.moveTo(100, 100);
      ctx.lineTo(100 + r * 2.5, 100);
      ctx.lineTo(100, 100 - r * 5);
      ctx.closePath();
      ctx.stroke();
    },
    handleCanvasClick(e) {
      const canvas = this.$refs.canvas;
      const rect = canvas.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      
      // Преобразование в координаты области
      const coordX = (x / canvas.width) * 200 - 100;
      const coordY = 100 - (y / canvas.height) * 200;
      
      this.$emit('point-clicked', { x: coordX, y: coordY });
    },
    handleResize() {
      this.$refs.canvas.width = this.$refs.canvas.clientWidth;
      this.$refs.canvas.height = this.$refs.canvas.clientHeight;
      this.drawCanvas();
    }
  },
  watch: {
    radius() {
      this.drawCanvas();
    },
    points() {
      this.drawCanvas();
    }
  }
};
</script>

<style scoped>
.canvas-container {
  position: relative;
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
}
canvas {
  width: 100%;
  height: auto;
  border: 1px solid #000;
  display: block;
}
</style>
