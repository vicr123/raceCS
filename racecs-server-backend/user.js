const WebSocket = require("./ws");

class User {
    visited = [];
    username = null;
    id = null;

    constructor(username, id) {
        this.username = username;
        this.id = id;
    }

    markVisited(station) {
        this.visited.push(station);
        WebSocket.broadcast({
            "type": "visitation",
            "user": this.username,
            "uuid": this.id,
            "station": station
        });
    }

    toObject() {
        return {
            uuid: this.id,
            visited: this.visited
        }
    }
}

module.exports = User;