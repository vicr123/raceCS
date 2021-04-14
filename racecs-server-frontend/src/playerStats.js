import React from 'react';

class PlayerStats extends React.Component {
    renderVisitedStations() {
        let els = [];

        for (let station of this.props.playerData[this.props.selectedPlayer.username].visited) {
            els.push(<p key={station}>{this.props.stationData[station].name}</p>)
        }

        return els;
    }

    render() {
        return <div className="playerStatsContents">
            <h1>Visited Stations:</h1>
            {this.renderVisitedStations()}
            <h1>Stats:</h1>
            <p>Visited stations: {this.props.playerData[this.props.selectedPlayer.username].visited.length}</p>
            <p>Stations remaining: {Object.keys(this.props.stationData).length - this.props.playerData[this.props.selectedPlayer.username].visited.length}</p>
        </div>
    }
}

export default PlayerStats;