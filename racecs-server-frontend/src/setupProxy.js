const proxy = require("http-proxy-middleware")

module.exports = app => {
  app.use(proxy("/ws", {target: "http://localhost:4000", ws: true}))
}
