module.exports = {
  entry: [
    './src'
  ],
  output: {
    path: __dirname,
    publicPath: '/',
    filename: 'bundle.js'
  },
  module: {
    preLoaders: [
        { test: /\.json$/, loader: 'json'},
    ],
    loaders: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loader: 'babel',
        query: {
          presets: ['react', 'es2015', 'stage-1']
        }
      },
      {
        test: /\.woff2?$|\.ttf$|\.eot$|\.svg$/,
        loader: 'file?name=fonts/[name].[ext]'
      },
      {
        test: /\.scss$/,
        loaders: ["style", "css", "sass"]
      },
      {
        test: /\.png?$|\.jpeg$|\.jpg$/,
        loader: 'file?name=imgs/[name].[ext]'
      },
    ]
  },
  devServer: {
    historyApiFallback: true,
    contentBase: './'
  }
};
