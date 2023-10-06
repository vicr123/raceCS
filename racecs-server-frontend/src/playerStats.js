import React from 'react';
import { withTranslation } from 'react-i18next';
import ArcProgress from 'react-arc-progress';

class Section extends React.Component {
    render() {
        return <div className="playerStatsSection">
            <div className="sectionHeader">{this.props.header}</div>
            <div className={`playerStatsSectionContents ${this.props.className}`}>
                {this.props.children}
            </div>
        </div>
    }
}

class Speedo extends React.Component {
    render() {
        return <div>
            <ArcProgress progress={this.props.value / this.props.max} customText={[
                {
                    text: this.props.value.toString(),
                    x: 100,
                    y: 100,
                    size: "40pt",
                    font: "Montserrat"
                },
                {
                    text: this.props.title,
                    x: 100,
                    y: 180,
                    font: "Montserrat"
                }
            ]} arcStart={150} arcEnd={390} animation={100} lineCap={"butt"} />
        </div>
    }
}

export class StationItem extends React.Component {
    render() {
        return <>
            <div className={"playerStationListShortcode"}>{this.props.station}</div>
            <div>{this.props.stationData.name}</div>
        </>
    }
}

class PlayerStats extends React.Component {
    renderVisitedStations() {
        let els = this.props.playerData[this.props.selectedPlayer.username].visited.map(station => <StationItem key={station} station={station} stationData={this.props.stationData[station]} />);

        if (els.length === 0) {
            els.push(<div>{this.props.t("PLAYERSTATS_NONE_VISITED")}</div>)
        }

        return els;
    }

    renderUnvisitedStations() {
        let stations = Object.keys(this.props.stationData);

        for (let station of this.props.playerData[this.props.selectedPlayer.username].visited) {
            stations.splice(stations.indexOf(station), 1);
        }

        stations.sort((first, second) => {
            return first.name > second.name ? 1 : -1;
        });

        let els = stations.map(station => <StationItem key={station} station={station} stationData={this.props.stationData[station]} />);

        if (els.length === 0) {
            els.push(<div>{this.props.t("PLAYERSTATS_ALL_VISITED")}</div>)
        }

        return els;
    }

    render() {
        return <div className="playerStatsContents">
            {!this.props.teamData?.length &&
            <Section className={"playerStatsOverview"} header={this.props.t("PLAYERSTATS_OVERVIEW")}>
                <Speedo title={this.props.t("PLAYERSTATS_VISITED")} max={Object.keys(this.props.stationData).length} value={this.props.playerData[this.props.selectedPlayer.username].visited.length} />
                <Speedo title={this.props.t("PLAYERSTATS_REMAINING")} max={Object.keys(this.props.stationData).length} value={Object.keys(this.props.stationData).length - this.props.playerData[this.props.selectedPlayer.username].visited.length} />
            </Section>}
            <Section className={"playerStationList"} header={this.props.t("PLAYERSTATS_VISITED")}>
                {this.renderVisitedStations()}
            </Section>
            <Section className={"playerStationList"}  header={this.props.t("PLAYERSTATS_REMAINING")}>
                {this.renderUnvisitedStations()}
            </Section>
        </div>
    }
}

export default withTranslation()(PlayerStats);