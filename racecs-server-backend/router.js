const express = require("express");
const mojangApi = require('mojang-api');
const fetch = require('node-fetch');
const settings = require("./settings");
const User = require('./user');
const WebSocket = require("./ws");
const fs = require("fs");
const path = require("path");

let router = express.Router();

//Load all the stations
let Stations = {};
let files = fs.readdirSync("stations");
for (let file of files) {
    let basename = path.basename(file, ".json");
    Stations[basename] = JSON.parse(fs.readFileSync(`./stations/${file}`, {
        encoding: "utf-8"
    }));
}

let usedStations = settings.get("stations", []);
let events = [];

let users = {};
let teams = [];

router.use(express.json({

}));
router.use((req, res, next) => {
    req.authorised = false;

    let auth = req.get("Authorization");
    if (auth === `Bearer ${settings.get("password")}`) {
        req.authorised = true;
    }

    next();
});

router.post("/teams", async (req, res) => {
    if (!req.authorised) {
        res.sendStatus(401);
        return;
    }

    teams = req.body;
    for (let team of req.body) {
        for (let player of team.players) {
            users[player].setTeam(team);
        }
    }

    WebSocket.broadcast({
        "type": "teaming",
        "teams": teams
    });
    res.sendStatus(200);
})

router.post("/teams/:id/name", async (req, res) => {
    if (!req.authorised) {
        res.sendStatus(401);
        return;
    }

    for (let team of teams) {
        if (team.id === req.params.id) {
            team.name = req.body.name;
        }
    }

    WebSocket.broadcast({
        "type": "teamRename",
        "team": req.params.id,
        "name": req.body.name
    });
    res.sendStatus(200);
})

router.post("/addUser/:username/:uuid", async (req, res) => {
    if (!req.authorised) {
        res.sendStatus(401);
        return;
    }

    if (users[req.params.username]) {
        console.log(`Adding ${req.params.username} not possible because the user is already in the race.`);
        res.sendStatus(400);
        return;
    }

    console.log(`Adding ${req.params.username} to race!`);

    users[req.params.username] = new User(req.params.username, req.params.uuid);
    
    WebSocket.broadcast({
        "type": "newPlayer",
        "user": req.params.username,
        "uuid": req.params.uuid
    });

    res.sendStatus(200);
});
router.post("/addUser/:username", async (req, res) => {
    if (!req.authorised) {
        res.sendStatus(401);
        return;
    }

    if (users[req.params.username]) {
        console.log(`Adding ${req.params.username} not possible because the user is already in the race.`);
        res.sendStatus(400);
        return;
    }

    console.log(`Adding ${req.params.username} to race!`);

    fetch("https://api.mojang.com/profiles/minecraft", {
        method: "POST",
        body: JSON.stringify([req.params.username]),
        headers: {
            "Content-Type": "application/json"
        }
    })
    .then(response => response.json())
    .then(response => {
        if (Object.keys(users).length === 0) events = [];

        users[req.params.username] = new User(req.params.username, response[0].id);
    
        WebSocket.broadcast({
            "type": "newPlayer",
            "user": req.params.username,
            "uuid": response[0].id
        });

        res.sendStatus(200);
    }).catch(err => {
        console.log(`Failed to add to race!`);
        console.log(err);
        res.sendStatus(400);
    });
});
router.post("/arrive/:player/completion", async (req, res) => {
    if (!req.authorised) {
        res.sendStatus(401);
        return;
    }

    events.push({
        type: "completion-partial",
        player: req.params.player,
        time: (new Date()).getTime()
    })

    const teamIndex = teams.findIndex(team => team.players.includes(req.params.player));
    if (teamIndex === -1) {
        console.log(`Could not process partial completion: player ${req.params.player} not found in teams`);
        console.log(`Teams: ${JSON.stringify(teams, null, 4)}`);
        res.sendStatus(400);
        return;
    }

    const team = teams[teamIndex];
    if (!team.returned) team.returned = [];
    team.returned.push(req.params.player);

    const remaining = team.players.length - team.returned.length;
    if (remaining === 0) {
        // Team wins!
        const place = teams.length - teams.filter(team => !team.place).length + 1;
        teams[teamIndex].place = place;

        const body = `${req.params.player} has returned to the terminal station! Team "${team.name}" has finished as #${place}!`;
        WebSocket.broadcastDiscord({
            author: {
                name: "Finished!",
                icon_url: "https://aircs.racing/finish_notification.png"
            },
            description: body,
            color: 16753920
        });
        WebSocket.broadcastNotification({
            body: body,
            icon: "finish_notification.png"
        });
        WebSocket.broadcast({
            "type": "completion-team",
            "player": req.params.player,
            "team": team.name,
            "place": place
        });
    } else {
        const body = `${req.params.player} from team "${team.name}" has returned to the terminal station! ${remaining} more from the team required to return!`;
        WebSocket.broadcastDiscord({
            author: {
                name: "Player Returned!",
                icon_url: "https://aircs.racing/returned_notification.png"
            },
            description: body,
            color: 4360181
        });
        WebSocket.broadcastNotification({
            body: body,
            icon: "returned_notification.png"
        });
        WebSocket.broadcast({
            "type": "completion-partial",
            "player": req.params.player,
            "team": team.name,
            "remaining": remaining
        });
    }

    res.sendStatus(200);
});
router.post("/arrive/:username/:location", async (req, res) => {
    try {
        if (!req.authorised) {
            res.sendStatus(401);
            return;
        }

        if (!users[req.params.username]) {
            res.sendStatus(400);
            return;
        }

        let location = req.params.location.toUpperCase();
        
        users[req.params.username].markVisited(location, Stations["en"][location]);

        events.push({
            type: "arrival",
            player: req.params.username,
            location: location,
            time: (new Date()).getTime()
        })

        res.sendStatus(200);
    } catch (err) {
        res.sendStatus(500);
    }
});
router.post("/collision/:username1/:username2", async (req, res) => {
    if (!req.authorised) {
        res.sendStatus(401);
        return;
    }

    if (!users[req.params.username1] || !users[req.params.username2]) {
        res.sendStatus(400);
        return;
    }

    WebSocket.broadcast({
        "type": "collision",
        "player1" : req.params.username1,
        "player2": req.params.username2
    });
    WebSocket.broadcastNotification({
        body: `${req.params.username1} has collided with ${req.params.username2}!`,
        icon: "collision_notification.png"
    });
    WebSocket.broadcastDiscord({
        author: {
            name: "Collision!",
            icon_url: "https://aircs.racing/collision_notification.png"
        },
        description: `${req.params.username1} has collided with ${req.params.username2}!`,
        color: 16711680
    });
    events.push({
        type: "collision",
        player1: req.params.username1,
        player2: req.params.username2,
        time: (new Date()).getTime()
    })
    res.sendStatus(200);
});
router.post("/completion/:username/:place", async (req, res) => {
    if (!req.authorised) {
        res.sendStatus(401);
        return;
    }


    if (!users[req.params.username]) {
        res.sendStatus(400);
        return;
    }

    users[req.params.username].setPlace(parseInt(req.params.place));

    events.push({
        type: "completion",
        player: req.params.username,
        place: req.params.place,
        time: (new Date()).getTime()
    })

    res.sendStatus(200);
});
router.post("/completion/team/:team/:place", async (req, res) => {
    if (!req.authorised) {
        res.sendStatus(401);
        return;
    }

    if (!users[req.params.username]) {
        res.sendStatus(400);
        return;
    }

    for (let team of teams) {
        if (team.id === req.params.team) {
            team.place = parseInt(req.params.place);
        }
    }

    events.push({
        type: "completion",
        player: req.params.team,
        place: req.params.place,
        time: (new Date()).getTime()
    })

    res.sendStatus(200);
});
router.post("/removeUser/:username", async (req, res) => {
    if (!req.authorised) {
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

    // Also reset the teams
    if (Object.keys(users).length === 0) teams = [];
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
router.get("/teams", async (req, res) => {
    res.send(teams);
})
router.get("/stations", async (req, res) => {
    //Return a list of all the stations
    // res.send(Stations);
    let localised = Stations["en"];
    let english = Stations["en"]
    let requestedLang = req.acceptsLanguages(Object.keys(Stations));
    if (requestedLang) localised = Stations[requestedLang];

    let stations = {}
    for (let station of usedStations) {
        stations[station] = {
            name: localised[station] || english[station]
        }
    }

    res.send(stations);
});

router.post("/stations", async (req, res) => {
    if (!req.authorised) {
        res.sendStatus(401);
        return;
    }

    if (!Array.isArray(req.body)) {
        res.status(400).send("Invalid Syntax");
        return;
    }

    let stations = req.body.map(station => station.toUpperCase());
    for (let station of stations) {
        if (!Object.keys(Stations["en"]).includes(station)) {
            res.status(400).send(`${station} is not a valid station!`);
            return;
        }
    }

    console.log(`Setting stations to ${stations}!`);
    usedStations = stations;
    settings.set("stations", usedStations);

    for (let user of Object.keys(users)) {
        users[user].clear();
    }

    WebSocket.broadcast({
        "type": "stationChange"
    });

    res.sendStatus(200);
});

router.post("/registerpush", (req, res) => {
    let subscription = req.body.subscription;
    let oldSubscriptions = settings.get("pushSubscriptions", []);
    oldSubscriptions.push(subscription);
    settings.set("pushSubscriptions", oldSubscriptions);

    res.sendStatus(200);
});

router.get("/events", async (req, res) => {
    res.send(events);
});

router.get("/home", async (req, res) => {
    // res.send(settings)
    fs.readFile("./homedata.json", {
        encoding: "utf-8"
    }, (err, data) => {
        if (err) {
            res.sendStatus(500);
            return;
        }

        res.send(JSON.parse(data));
    });
})

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