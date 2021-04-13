const WebSocket = require("./ws");

class User {
    visited = [];
    username = null;

    constructor(username) {
        this.username = username;
    }

    markVisited(station) {
        this.visited.push(station);
        WebSocket.broadcast({
            "type": "visitation",
            "user": this.username,
            "station": station
        });
    }

    toObject() {
        return {
            visited: this.visited
        }
    }
}

module.exports = User;