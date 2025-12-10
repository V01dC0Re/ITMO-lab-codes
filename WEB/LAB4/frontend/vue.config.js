module.exports = {
  publicPath: process.env.NODE_ENV === 'production' ? '/lab-app/' : '/',
  outputDir: 'dist',
  assetsDir: 'static',
  indexPath: 'index.html',

  configureWebpack: {
    output: {
      publicPath: '/lab-app/'
    }
  }
}