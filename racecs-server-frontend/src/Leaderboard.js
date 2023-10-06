import React from 'react';
import Common from './common';
import heads from './heads';
import { withTranslation } from 'react-i18next';

class Leaderboard extends React.Component {
    renderPlayerData() {
        let els = [];

        let users = []

        for (let user of Object.keys(this.props.playerData)) {
            users.push({
                username: user,
                visited: this.props.playerData[user].visited.length,
                uuid: this.props.playerData[user].uuid,
                place: this.props.playerData[user].place
            });
        }

        users.sort((first, second) => {
            if (first.place === -1 && second.place === -1) {
                if (first.visited == second.visited) return 0;
    
                return first.visited > second.visited ? -1 : 1;
            } else if (first.place === -1 && second.place !== -1) {
                return 1;
            } else if (first.place !== -1 && second.place === -1) {
                return -1;
            } else {
                return first.place < second.place ? -1 : 1;
            }
        });

        for (let user of users) {
            let clickHandler = () => {
                this.props.onPlayerClicked(user);
            };

            let renderPlace = () => {
                if (user.place !== -1) {
                    return Common.getOrdinal(user.place)
                } else {
                    return "---";
                }
            }

            els.push(<div className="leaderboardGridItem" key={`${user.username}-image`} onClick={clickHandler}><img height="30" src={heads(user.uuid)}></img></div>);
            els.push(<div className="leaderboardGridItem placeItem" key={`${user.username}-place`} onClick={clickHandler}>{renderPlace()}</div>)
            els.push(<div className="leaderboardGridItem" key={`${user.username}-username`} onClick={clickHandler}>{user.username}</div>)
            els.push(<div className="leaderboardGridItem" key={`${user.username}-visited`} onClick={clickHandler}>{user.visited}</div>)
        }

        return els;
    }

    renderRecentEvents() {
        let els = [];

        let events = [...this.props.recentEvents];
        events.reverse();

        let i = 0;
        for (let event of events) {
            els.push(<div className="recentEventItem" key={i}>
                <img height="30" src={event.image} style={{paddingRight: "9px"}}></img>
                {event.message}
            </div>)

            i++;
        }

        return els;
    }

    renderTeams() {
        return this.props.teamData.sort((teamA, teamB) => (teamA.place * 1000 + (teamA.visited?.length ?? 0)) - (teamB.place * 1000 + (teamB.visited?.length ?? 0))).map(team => <div className="leaderboardGrid">
            <div className="sectionHeader">{(team.place === -1 || !team.place) ? "" : Common.getOrdinal(team.place)}</div>
            <div className="sectionHeader"></div>
            <div className="sectionHeader">{team.name}</div>
            <div className="sectionHeader">{team.visited?.length || 0}/<span className="leaderboardTotalStations">{Object.keys(this.props.stationData).length}</span></div>

            {team.players.map(playerName => {
                const player = this.props.playerData[playerName];
                return <>
                    <div className="leaderboardGridItem" key={`${playerName}-image`}><img height="30"
                                                                                               src={heads(player.uuid)}></img>
                    </div>
                    <div className="leaderboardGridItem placeItem" key={`${playerName}-place`}>{team.returned?.includes(playerName) ? <img height="30" src={"/login_notification.png"}></img> : <div style={{width: "30px", height: "30px"}} />}</div>
                    <div className="leaderboardGridItem" key={`${playerName}-username`}>{playerName}</div>
                    <div className="leaderboardGridItem" key={`${playerName}-visited`}>{}</div>
                </>;
            })}
        </div>);
    }

    renderMainView() {
        if (Object.keys(this.props.playerData).length === 0) {
            return <div className="errorContainer">
                <h1>{this.props.t("NO_RACE")}</h1>
                <p>{this.props.t("JOIN_RACE_PROMPT")}</p>
            </div>
        } else if (this.props.teamData?.length) {
            return <>
                <div className="leaderboardGridWrapper">
                    {this.renderTeams()}
                    <div className="hspacer"></div>
                </div>
                <div style={{width: "20px", height: "20px"}}></div>
                <div className="recentEventsWrapper">
                    <div className="sectionHeader">{this.props.t("LEADERBOARD_RECENT_EVENTS")}</div>
                    {this.renderRecentEvents()}
                </div>
            </>
        } else {
            return <>
                <div className="leaderboardGridWrapper">
                    <div className="leaderboardGrid">
                        <div className="sectionHeader"></div>
                        <div className="sectionHeader"></div>
                        <div className="sectionHeader">{this.props.t("LEADERBOARD_NAME")}</div>
                        <div className="sectionHeader">{this.props.t("LEADERBOARD_STATIONS_VISITED")}</div>

                        {this.renderPlayerData()}
                    </div>
                    <div className="hspacer"></div>
                </div>
                <div style={{width: "20px", height: "20px"}}></div>
                <div className="recentEventsWrapper">
                    <div className="sectionHeader">{this.props.t("LEADERBOARD_RECENT_EVENTS")}</div>
                    {this.renderRecentEvents()}
                </div>
            </>
        }
    }

    render() {
        return <div className="mainView">
            {this.renderMainView()}
        </div>
    }
};

export default withTranslation()(Leaderboard);