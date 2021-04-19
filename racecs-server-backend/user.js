const WebSocket = require("./ws");

class User {
    visited = [];
    username = null;
    id = null;
    place = -1;

    constructor(username, id) {
        this.username = username;
        this.id = id;
    }

    markVisited(station, stationName) {
        this.visited.push(station);
        WebSocket.broadcast({
            "type": "visitation",
            "user": this.username,
            "uuid": this.id,
            "station": station
        });

        WebSocket.broadcastNotification({
            body: `${this.username} has arrived at ${stationName}!`,
            icon: "login_notification.png"
        });
    }

    setPlace(place) {
        this.place = place;
        WebSocket.broadcast({
            "type": "completion",
            "username" : this.username,
            "place": place
        });

        WebSocket.broadcastNotification({
            body: `${this.username} has finished as #${place}!`,
            icon: "finish_notification.png"
        });
    }

    toObject() {
        return {
            uuid: this.id,
            visited: this.visited,
            place: this.place
        }
    }
}

module.exports = User;