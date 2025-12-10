<template>
  <router-view></router-view>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
body {
  margin: 0;
  font-family: Arial, sans-serif;
}

@media (max-width: 1083px) and (min-width: 844px) {
  /* Планшетный режим */
  body {
    padding: 10px;
  }
}

@media (max-width: 843px) {
  /* Мобильный режим */
  body {
    padding: 5px;
  }
}
</style>