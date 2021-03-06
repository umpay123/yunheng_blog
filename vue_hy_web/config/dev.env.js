'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',

  VUE_HY_WEB: '"http://localhost:9527"',
  PICTURE_API: '"http://localhost:8602"',
  WEB_API: '"http://localhost:8603"',
  Search_API: '"http://localhost:8605"',
	ELASTICSEARCH: '"http://localhost:8605"',

})
