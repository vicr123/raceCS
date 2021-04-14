const express = require("express");
const mojangApi = require('mojang-api');
const User = require('./user');
const WebSocket = require("./ws");

let router = express.Router();

const Stations = {
    "WC": {
        name: "Whale City Central"
    },
    "WCC": {
        name: "Whale City Commercial"
    },
    "WCR": {
        name: "Whale City Residential"
    },
    "WCA": {
        name: "Whale City Airport"
    },
    "LD": {
        name: "Legal District"
    },
    "AKL": {
        name: "Auckland"
    },
    "UFO": {
        name: "RMUFO"
    },
    "SMT": {
        name: "Spawn Memorial Transfer"
    },
    "LI" : {
        name: "Long Island"
    },
    "DT": {
        name: "Downtown Melanie City"
    },
    "AKI": {
        name: "Akiba Island"
    },
    "WOVR": {
        name: "Westover"
    },
    "BRP": {
        name: "Bridgett's Port"
    },
    "UWGK": {
        name: "Underwater Go-Karting"
    },
    "VIC":{
        name: "Victor's Intessting City"
    },
    "ALI": {
        name: "Alee Isle"
    },
    "SIL": {
        name: "Silicon Valley"
    },
    "SKY": {
        name: "SkyCity"
    },
    "BCO": {
        name: "BreadCroust"
    },
    "BBT": {
        name: "Birch Boat Town"
    },
    "SHC": {
        name: "ShiftCity"
    },
    "BLO": {
        name: "Birch Lodges"
    }
};

let users = {};

const password = "goOGHNodif34oindsoifg";

router.post("/addUser/:username", async (req, res) => {
    if (req.query.auth != password) {
        res.send(401);
        return;
    }

    mojangApi.nameToUuid(req.params.username, (err, mojangResponse) => {
        if (mojangResponse.length === 0) {
            res.send(400);
            return;
        }

        users[req.params.username] = new User(req.params.username, mojangResponse[0].id);
    
        WebSocket.broadcast({
            "type": "newPlayer",
            "user": req.params.username,
            "uuid": mojangResponse[0].id
        });

        res.send(200);
    });
});
router.post("/arrive/:username/:location", async (req, res) => {
    try {
        if (req.query.auth != password) {
            res.send(401);
            return;
        }

        //req.params.username
        //req.params.location
        
        users[req.params.username].markVisited(req.params.location);

        res.send(200);
    } catch {
        res.send(500);
    }
});
router.post("/collision/:username1/:username2", async (req, res) => {
    //Don't need to implement right now!
    if (req.query.auth != password) {
        res.send(401);
        return;
    }

    res.send(500);
});
router.post("/removeUser/:username", async (req, res) => {
    if (req.query.auth != password) {
        res.send(401);
        return;
    }

    WebSocket.broadcast({
        "type": "removePlayer",
        "user": req.params.username
    });

    delete users[req.params.username];
    res.send(200);
});
router.get("/userStatus/:username", async (req, res) => {
    try {
        res.send(users[req.params.username].toObject());
    } catch {
        res.send(400);
    }
});
router.get("/users", async (req, res) => {
    //Returns all the users
    let retval = {};
    for (let username of Object.keys(users)) {
        retval[username] = users[username].toObject();
    }
    res.send(retval);
});
router.get("/stations", async (req, res) => {
    //Return a list of all the users
    res.send(Stations);
});

module.exports = router;

/*
    users: {
        "vicr123": {
            visited: [
                "ALI",
                "LI"
            ]
        },
        "JPlexer": {
            visited: [
                "WOVR"
            ]
        }
    }
*/

/*
    {
        "ALI": {
            "name": "Alee Isle"
        },
        ""
    }
*/