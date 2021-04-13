import React from 'react';
import Fetch from './Fetch';
import logo from './logo.svg';
import './App.css';

import Leaderboard from './Leaderboard';
import Players from './Players';
import Stations from './Stations';
import NotificationDrawer from './NotificationDrawer';
import Wsh from './wsh';

class App extends React.Component {
  constructor(props) {
    super(props);
    
    this.state = {
      currentView: "leaderboard",
      state: "load"
    };
  }

  componentDidMount() {
    //Set up the WS connection
    let ws = new Wsh(`ws://${window.location.host}/ws`);
    ws.on("message", (data) => {
      console.log(data);
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

    Fetch.get("/stations").then(data => {
      console.log(data);
    });
  }

  renderMainView() {
    switch (this.state.currentView) {
      case "leaderboard":
        return <Leaderboard />
      case "players":
        return <Players />
      case "stations":
        return <Stations />
    }
  }

  changeView(view) {
    this.setState({
      currentView: view
    });
  }

  renderState() {
    switch (this.state.state) {
      case "load":
        return <div>
          Loading...
        </div>
      case "ready":
        return <>
          <div className="header">
            <div className="headerButton" onClick={this.changeView.bind(this, "leaderboard")}>Leaderboard</div>
            <div className="headerButton" onClick={this.changeView.bind(this, "players")}>Players</div>
            <div className="headerButton" onClick={this.changeView.bind(this, "stations")}>Stations</div>
          </div>
          {this.renderMainView()}
          <NotificationDrawer websocket={this.state.ws} />
        </>
        case "error":
          return <div>
            Please reload the page
          </div>
    }
  }

  render() {
    return <div className="mainContainer">
      {this.renderState()}
    </div>
  }
}

export default App;
