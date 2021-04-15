import React from 'react';

class Section extends React.Component {
    render() {
        return <div className="playerStatsSection">
            <div className="sectionHeader">{this.props.header}</div>
            <div className="playerStatsSectionContents">
                {this.props.children}
            </div>
        </div>
    }
}

class PlayerStats extends React.Component {
    renderVisitedStations() {
        let els = [];

        for (let station of this.props.playerData[this.props.selectedPlayer.username].visited) {
            els.push(<div key={station}>{this.props.stationData[station].name}</div>)
        }

        if (els.length === 0) {
            els.push(<div>No stations visited yet!</div>)
        }

        return els;
    }

    renderUnvisitedStations() {
        let els = [];

        let stations = Object.keys(this.props.stationData);

        for (let station of this.props.playerData[this.props.selectedPlayer.username].visited) {
            stations.splice(stations.indexOf(station), 1);
        }

        stations.sort((first, second) => {
            return first.name > second.name ? 1 : -1;
        });

        for (let station of stations) {
            els.push(<div key={station}>{this.props.stationData[station].name}</div>)
        }

        if (els.length === 0) {
            els.push(<div>All stations visited!</div>)
        }

        return els;
    }

    render() {
        return <div className="playerStatsContents">
            {/* <h1>Visited Stations:</h1>
            {this.renderVisitedStations()}
            <h1>Stats:</h1>
            <p>Visited stations: {this.props.playerData[this.props.selectedPlayer.username].visited.length}</p>
            <p>Stations remaining: {Object.keys(this.props.stationData).length - this.props.playerData[this.props.selectedPlayer.username].visited.length}</p> */}
            <Section header="Overview">
                <div>Visited stations: {this.props.playerData[this.props.selectedPlayer.username].visited.length}</div>
                <div>Stations remaining: {Object.keys(this.props.stationData).length - this.props.playerData[this.props.selectedPlayer.username].visited.length}</div>
            </Section>
            <Section header="Visited Stations">
                {this.renderVisitedStations()}
            </Section>
            <Section header="Stations Remaining">
                {this.renderUnvisitedStations()}
            </Section>
        </div>
    }
}

export default PlayerStats;