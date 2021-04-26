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
        WebSocket.broadcastDiscord({
            author: {
                name: "Arrival!",
                icon_url: "https://aircs.racing/login_notification.png"
            },
            description: `${this.username} has arrived at ${stationName}!`,
            color: 32768
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
        WebSocket.broadcastDiscord({
            author: {
                name: "Finished!",
                icon_url: "https://aircs.racing/finish_notification.png"
            },
            description: `${this.username} has finished as #${place}!`,
            color: 16753920
        });
    }

    clear() {
        this.visited = [];
        this.place = -1;
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