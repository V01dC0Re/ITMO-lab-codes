module.exports = {
  publicPath: './',
  outputDir: 'dist',
  assetsDir: 'static',
  
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/lab-app/api'
        }
      }
    }
  },
  
  configureWebpack: {
    optimization: {
      splitChunks: false
    }
  }
}