import React, { Suspense } from 'react';
import { withTranslation } from 'react-i18next';
import Fetch from './Fetch';
import aircs from './aircs.svg';
import './App.css';

import Leaderboard from './Leaderboard';
import Players from './Players';
import Stations from './Stations';
import NotificationDrawer from './NotificationDrawer';
import Wsh from './wsh';
import Ticker from './ticker';
import Settings from './Settings';
import Home from './home';
import Teams from "./Teams";

class App extends React.Component {
  constructor(props) {
    super(props);

    const params = new URLSearchParams(window.location.search);
    
    this.state = {
      currentView: params.get("view") ?? "home",
      state: "load",
      selectPlayer: null,
      playerData: {},
      stationData: {},
      recentEvents: []
    };
  }

  componentDidMount() {
    //Set up the WS connection
    let ws = new Wsh(`ws://${window.location.host}/ws`);
    ws.on("message", async (data) => {
      switch (data.type) {
        case "newPlayer":
          this.setState(state => {
            let pdata = JSON.parse(JSON.stringify(state.playerData));
            pdata[data.user] = {
              "uuid": data.uuid,
              "visited": [],
              "place": -1
            };

            return {
              playerData: pdata
            }
          })
          break;
        case "removePlayer":
          this.setState(state => {
            let pdata = JSON.parse(JSON.stringify(state.playerData));
            let teams = JSON.parse(JSON.stringify(state.teamData));
            delete pdata[data.user];

            if (Object.keys(pdata).length === 0) {
              teams = [];
            }

            return {
              playerData: pdata,
              teamData: teams
            }
          })
          break;
        case "visitation":
          this.setState(state => {
            let pdata = JSON.parse(JSON.stringify(state.playerData));
            let teams = JSON.parse(JSON.stringify(state.teamData));
            pdata[data.user].visited.push(data.station);

            if (data.team) {
              for (let team of teams) {
                if (team.id === data.team) {
                  if (!team.visited) team.visited = [];
                  team.visited.push(data.station);
                }
              }
            }
            
            return {
              playerData: pdata,
              teamData: teams
            }
          });
          break;
        case "completion":
          this.setState(state => {
            let pdata = JSON.parse(JSON.stringify(state.playerData));
            pdata[data.username].place = data.place;
            
            return {
              playerData: pdata
            }
          });
          break;
        case "teaming":
          this.setState({
            teamData: data.teams
          })
          break;
        case "teamRename":
          this.setState(state => {
            let teams = JSON.parse(JSON.stringify(state.teamData));

            for (let team of teams) {
              if (team.id === data.team) {
                team.name = data.name;
              }
            }

            return {
              teamData: teams
            }
          })
          break;
        case "completion-partial":
        case "completion-team":
          this.setState(state => {
            let teams = JSON.parse(JSON.stringify(state.teamData));

            for (let team of teams) {
              if (team.id === data.teamId) {
                if (!team.returned) team.returned = [];
                team.returned.push(data.player)

                if (data.place) {
                  team.place = data.place;
                }
              }
            }

            return {
              teamData: teams
            }
          })
        case "stationChange":
          await Promise.all([
            await this.updatePlayers(),
            await this.updateStations()
          ]);
      }
    });
    ws.on("open", () => {
      this.setState({
        state: "ready"
      });
    })
    ws.on("close", () => {
      this.setState({
        state: "error"
      });
    })

    this.setState({
      ws: ws
    });

    (async () => {
      await Promise.all([
        this.updatePlayers(),
        this.updateStations(),
        this.updateTeams()
      ])
    })();
  }

  async updatePlayers() {
    this.setState({
      playerData: await Fetch.get("/users")
    });
  }

  async updateStations() {
    this.setState({
      stationData: await Fetch.get("/stations")
    })
  }

  async updateTeams() {
    this.setState({
      teamData: await Fetch.get("/teams")
    })
  }

  componentWillUnmount() {
    try {
      if (this.state.ws) this.state.ws.close();
    } catch {

    }
  }

  renderMainView() {
    switch (this.state.currentView) {
      case "home":
        return <Home />
      case "leaderboard":
      case "streamer-leaderboard":
        return <Leaderboard stationData={this.state.stationData} playerData={this.state.playerData} teamData={this.state.teamData} recentEvents={this.state.recentEvents} onPlayerClicked={this.playerClicked.bind(this)} teamData={this.state.teamData} />
      case "players":
        return <Players stationData={this.state.stationData} playerData={this.state.playerData} teamData={this.state.teamData} selectPlayer={this.state.selectPlayer} />
      case "stations":
      case "streamer-stations":
        return <Stations stationData={this.state.stationData} playerData={this.state.playerData} teamData={this.state.teamData} onPlayerClicked={this.playerClicked.bind(this)} />
      case "teams":
      case "streamer-teams":
        return <Teams stationData={this.state.stationData} playerData={this.state.playerData} teamData={this.state.teamData} />
      case "settings":
        return <Settings onLocaleChange={this.onLocaleChange.bind(this)} />
      case "notifications":
        return <NotificationDrawer stationData={this.state.stationData} websocket={this.state.ws} onNotification={this.notificationPosted.bind(this)} />
    }
  }

  onLocaleChange() {
    this.updateStations();
  }

  playerClicked(player) {
    this.setState({
      currentView: "players",
      selectPlayer: player
    });
  }

  changeView(view) {
    this.setState({
      selectPlayer: null,
      currentView: view
    });
  }

  notificationPosted(notification) {
    this.setState(state => {
      return {
        recentEvents: [...state.recentEvents, notification]
      }
    });
  }

  renderRaceOnlyItems() {
    if (Object.keys(this.state.playerData).length === 0) return null;
    
    return <>
      <div className={`headerButton ${this.state.currentView === "leaderboard" && "selected"}`} onClick={this.changeView.bind(this, "leaderboard")}>{this.props.t("APP_LEADERBOARD")}</div>
      <div className={`headerButton ${this.state.currentView === "stations" && "selected"}`} onClick={this.changeView.bind(this, "stations")}>{this.props.t("APP_STATIONS")}</div>
      {this.state.teamData?.length !== 0 && <div className={`headerButton ${this.state.currentView === "teams" && "selected"}`} onClick={this.changeView.bind(this, "teams")}>{this.props.t("APP_TEAMS")}</div>}
      <div className={`headerButton ${this.state.currentView === "players" && "selected"}`} onClick={this.changeView.bind(this, "players")}>{this.props.t("APP_PLAYERS")}</div>
    </>
  }

  renderState() {
    switch (this.state.state) {
      case "load":
        return <div className="errorContainer">
          <h1>{this.props.t("APP_LOADING")}</h1>
          <p>{this.props.t("APP_PLEASE_WAIT")}</p>
        </div>
      case "ready":
        if (this.state.currentView.includes("streamer") || this.state.currentView === "notifications") {
          return <>
            {this.renderMainView()}
            <iframe src="https://map.aircs.racing/" style={{flexGrow: 1, border: "none", display: this.state.currentView === "aircsmap" ? "block" : "none"}}/>
            <div style={{flexGrow: 1, border: "none", display: this.state.currentView === "sqtrmap" ? "block" : "none", background: "url(https://sqtr.aircs.racing/assets/images/sqtrmap2023.png) center center/contain no-repeat"}} />
            <div style={{flexGrow: 1, border: "none", display: this.state.currentView === "clyrailmap" ? "block" : "none", background: "url(/clyrailmap.svg) center center/contain no-repeat, white"}} />
          </>
        }
        return <>
          <div className="header">
            <div className="headerButtons">
              <div className={`headerButton ${this.state.currentView === "home" && "selected"}`} onClick={this.changeView.bind(this, "home")}>{this.props.t("APP_HOME")}</div>
              {this.renderRaceOnlyItems()}
              <div className={`headerButton ${this.state.currentView === "aircsmap" && "selected"}`} onClick={this.changeView.bind(this, "aircsmap")}>{this.props.t("APP_AIRCS_MAP")}</div>
              <div className={`headerButton ${this.state.currentView === "sqtrmap" && "selected"}`} onClick={this.changeView.bind(this, "sqtrmap")}>{this.props.t("APP_SQTR_MAP")}</div>
              <div className={`headerButton ${this.state.currentView === "clyrailmap" && "selected"}`} onClick={this.changeView.bind(this, "clyrailmap")}>{this.props.t("APP_CLYRAIL_MAP")}</div>
              <div className={`headerButton ${this.state.currentView === "settings" && "selected"}`} onClick={this.changeView.bind(this, "settings")}>{this.props.t("APP_SETTINGS")}</div>
            </div>
            <img src={aircs} style={{height: "100%", padding: "9px", boxSizing: "border-box"}}></img>
          </div>
          {this.renderMainView()}
          <iframe src="https://map.aircs.racing/" style={{flexGrow: 1, border: "none", display: this.state.currentView === "aircsmap" ? "block" : "none"}}/>
          <div style={{flexGrow: 1, border: "none", display: this.state.currentView === "sqtrmap" ? "block" : "none", background: "url(https://sqtr.aircs.racing/assets/images/sqtrmap2023.png) center center/contain no-repeat"}} />
          <div style={{flexGrow: 1, border: "none", display: this.state.currentView === "clyrailmap" ? "block" : "none", background: "url(/clyrailmap.svg) center center/contain no-repeat, white"}} />
          <NotificationDrawer stationData={this.state.stationData} websocket={this.state.ws} onNotification={this.notificationPosted.bind(this)} />
        </>
        case "error":
          return <div className="errorContainer">
            <h1>{this.props.t("APP_DISCONNECTED")}</h1>
            <p>{this.props.t("APP_PLEASE_RELOAD")}</p>
          </div>
    }
  }

  render() {
    return <div className="mainContainer">
        {this.renderState()}
      </div>
  }
}

const AppT = withTranslation()(App);

class AppContainer extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      hasError: false
    }
  }

  render() {
    return this.state.hasError ? 
        <div className="errorContainer">
        <h1>Ouch!</h1>
        <p>That hurt :(</p>
        <button onClick={() => window.location.reload()}>Reload</button>
      </div> : <Suspense fallback={
        <div className="errorContainer">
          <h1>Loading...</h1>
          <p>Please wait a second.</p>
        </div>
      }><AppT /></Suspense>
  }

  static getDerivedStateFromError(error) {
    // Update state so the next render will show the fallback UI.
    return { hasError: true };
  }
}

export default AppContainer;
