const express = require('express');
const expressws = require('express-ws');

const router = require("./router");
const WebSocket = require("./ws");

let app = express();
expressws(app);

app.listen(4000, () => {
    console.log("Locked and loaded!");
});

app.use("/api", router);


app.ws("/ws", (ws, req) => {
    new WebSocket(ws);
});
