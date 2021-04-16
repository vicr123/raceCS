const EventEmitter = require('events');
const settings = require('./settings');
const webPush = require('web-push');
const fs = require('fs');

let websockets = [];

const vapidDetails = {
    publicKey: fs.readFileSync("vapid-pubkey", {
        encoding: "utf-8"
    }),
    privateKey: fs.readFileSync("vapid-privkey", {
        encoding: "utf-8"
    }),
    subject: "mailto:vicr12345@gmail.com"
}

class WebSocket extends EventEmitter {
    socket = null;
    interval = null;

    constructor(ws) {
        super();

        this.socket = ws;
        websockets.push(this);
    
        ws.on('close', () => {
            this.emit("closed");

            let idx = websockets.indexOf(this);
            websockets.splice(idx, 1);

            clearInterval(this.interval);
        });

        this.interval = setInterval(() => {
            this.socket.send(JSON.stringify({
                "type": "ping"
            }));
        }, 10000);

        console.log("WS connection established");
    }

    static broadcast(data) {
        for (let socket of websockets) {
            try {
                socket.socket.send(JSON.stringify(data));
            } catch {
                //meh
            }
        }
    }

    static async broadcastNotification(data) {
        let payload = JSON.stringify(data);
        let options = {
            vapidDetails: vapidDetails
        }

        let promises = [];
        for (let subscription of settings.get("pushSubscriptions", [])) {
            promises.push(webPush.sendNotification(subscription, payload, options));
        }

        try {
            await Promise.all(promises);
        } catch {
            
        }
    }
}

module.exports = WebSocket;