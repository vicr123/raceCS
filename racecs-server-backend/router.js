const express = require("express");
const mojangApi = require('mojang-api');
const fetch = require('node-fetch');
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
        name: "Westover ❤️"
    },
    "BRP": {
        name: "Bridgett's Port"
    },
    "UWGK": {
        name: "Underwater Go-Karting"
    },
    "VIC":{
        name: "Victor's Interesting City"
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

router.post("/addUser/:username/:uuid", async (req, res) => {
    if (req.query.auth != password) {
        res.sendStatus(401);
        return;
    }

    if (users[req.params.username]) {
        console.log(`Adding ${req.params.username} not possible because the user is already in the race.`);
        res.sendStatus(400);
        return;
    }

    console.log(`Adding ${req.params.username} to race!`);

    // fetch("https://api.mojang.com/profiles/minecraft", {
    //     method: "POST",
    //     body: JSON.stringify([req.params.username]),
    //     headers: {
    //         "Content-Type": "application/json"
    //     }
    // })
    // .then(response => response.json())
    // .then(response => {
    //     users[req.params.username] = new User(req.params.username, response[0].id);
    
    //     WebSocket.broadcast({
    //         "type": "newPlayer",
    //         "user": req.params.username,
    //         "uuid": response[0].id
    //     });

    //     res.sendStatus(200);
    // }).catch(err => {
    //     console.log(`Failed to add to race!`);
    //     console.log(err);
    //     res.sendStatus(400);
    // });


    users[req.params.username] = new User(req.params.username, req.params.uuid);
    
    WebSocket.broadcast({
        "type": "newPlayer",
        "user": req.params.username,
        "uuid": req.params.uuid
    });

    res.sendStatus(200);
});
router.post("/arrive/:username/:location", async (req, res) => {
    try {
        if (req.query.auth != password) {
            res.sendStatus(401);
            return;
        }

        if (!users[req.params.username]) {
            res.sendStatus(400);
            return;
        }

        //req.params.username
        //req.params.location
        
        users[req.params.username].markVisited(req.params.location);

        res.sendStatus(200);
    } catch {
        res.sendStatus(500);
    }
});
router.post("/collision/:username1/:username2", async (req, res) => {
    if (req.query.auth != password) {
        res.sendStatus(401);
        return;
    }

    WebSocket.broadcast({
        "type": "collision",
        "player1" : req.params.username1,
        "player2": req.params.username2
    });
    res.sendStatus(200);
});
router.post("/completion/:username/:place", async (req, res) => {
    if (req.query.auth != password) {
        res.sendStatus(401);
        return;
    }


    if (!users[req.params.username]) {
        res.sendStatus(400);
        return;
    }

    users[req.params.username].setPlace(parseInt(req.params.place));
    res.sendStatus(200);
});
router.post("/removeUser/:username", async (req, res) => {
    if (req.query.auth != password) {
        res.sendStatus(401);
        return;
    }

    if (!users[req.params.username]) {
        console.log(`Removing ${req.params.username} not possible because the user is not in the race.`);
        res.sendStatus(400);
        return;
    }

    console.log(`Removing ${req.params.username} to race!`);

    WebSocket.broadcast({
        "type": "removePlayer",
        "user": req.params.username
    });

    delete users[req.params.username];
    res.sendStatus(200);
});
router.get("/userStatus/:username", async (req, res) => {
    try {
        res.send(users[req.params.username].toObject());
    } catch {
        res.sendStatus(400);
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