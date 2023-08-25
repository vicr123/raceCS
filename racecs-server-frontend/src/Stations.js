import React from 'react';
import heads from './heads';
import teamHeads from "./teamHeads"
import { withTranslation } from 'react-i18next';
import "core-js/features/object/group-by"

class Stations extends React.Component {
    renderStations() {
        let els = [];

        let stations = [];
        for (let station of Object.keys(this.props.stationData)) {
            stations.push({
                code: station,
                name: this.props.stationData[station].name
            });
        }
        stations.sort((first, second) => {
            return first.name > second.name ? 1 : -1;
        });

        for (let station of stations) {
            els.push(<div className="stationsGridInnerWrapper" key={station.code}>
                <div className="stationsGrid">
                    <div className="sectionHeader" style={{gridArea: "header"}}><div className={"stationHeaderShortcode"}>{station.code}</div>{station.name}</div>

                    {this.renderStation(station.code)}
                </div>
            </div>)
        }
        return els;

        
    }

    renderStation(station) {
        let els = [];

        if (this.props.teamData?.length) {
            const teams = Object.groupBy(Object.keys(this.props.playerData), username => {
                return this.props.teamData.find(team => team.players.includes(username)).id;
            })

            for (const teamId of Object.keys(teams)) {
                const teamClickHandler = () => {

                }

                const team = this.props.teamData.find(team => team.id === teamId);
                if (team.visited.includes(station)) {
                    els.push(<div className="leaderboardGridItem" key={`${teamId}-team-image`} onClick={teamClickHandler}><img height="30" src={teamHeads(teamId)}></img></div>);
                    els.push(<div className="leaderboardGridItem" key={`${teamId}-team-name`} onClick={teamClickHandler}>{team.name}</div>)
                }

                const players = teams[teamId];
                for (const username of players) {
                    let playerClickHandler = () => {
                        this.props.onPlayerClicked({
                            username: username,
                            uuid: user.uuid
                        });
                    };

                    let user = this.props.playerData[username];
                    if (user.visited.includes(station)) {
                        els.push(<div className="leaderboardGridItem teamPlayerPadding" key={`${username}-image`}
                                      onClick={playerClickHandler}><img height="30" src={heads(user.uuid)}></img>
                        </div>);
                        els.push(<div className="leaderboardGridItem" key={`${username}-username`}
                                      onClick={playerClickHandler}>{username}</div>)
                    }
                }
            }

            // els.push(JSON.stringify(teams));
        } else {
            for (let username of Object.keys(this.props.playerData)) {
                let user = this.props.playerData[username];
                if (user.visited.includes(station)) {

                    let playerClickHandler = () => {
                        this.props.onPlayerClicked({
                            username: username,
                            uuid: user.uuid
                        });
                    };
                    els.push(<div className="leaderboardGridItem" key={`${username}-image`} onClick={playerClickHandler}><img height="30" src={heads(user.uuid)}></img></div>);
                    els.push(<div className="leaderboardGridItem" key={`${username}-username`} onClick={playerClickHandler}>{username}</div>)
                }
            }
        }

        if (els.length == 0) {
            els.push(<div style={{justifySelf: "center", alignSelf: "center", padding: "20px"}} key="noArrivals">{this.props.t("STATIONS_NO_ARRIVALS")}</div>)
        }

        return els;
    }


    render() {
        if (Object.keys(this.props.playerData).length === 0) {
            return <div className="mainView">
                <div className="errorContainer">
                    <h1>{this.props.t("NO_RACE")}</h1>
                    <p>{this.props.t("JOIN_RACE_PROMPT")}</p>
                </div>
            </div>
        } else {
            return <div className="mainView" style={{padding: "10px", flexDirection: "row"}}>
                <div className="stationsGridWrapper">
                    {this.renderStations()}
                </div>
            </div>
        }
    }
};

export default withTranslation()(Stations);