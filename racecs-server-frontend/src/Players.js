import React from 'react';
import PlayerStats from './playerStats';
import { withTranslation } from 'react-i18next';

class Players extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            selectedPlayer: props.selectPlayer
        }
    }

    renderPlayers() {
        let els = [];
        
        let users = [];

        for (let username of Object.keys(this.props.playerData)) {
            users.push({
                username: username,
                uuid: this.props.playerData[username].uuid
            })
        }
        users.sort((first, second) => {
            return first.username > second.username ? 1 : -1;
        });

        for (let user of users) {
            let clickHandler = () => {
                this.setState({
                    selectedPlayer: user
                });
            };

            els.push(<div className={`playersListItem ${this.state.selectedPlayer?.username === user.username && "selected"}`} key={`${user.username}-image`} onClick={clickHandler}><img height="30" src={`https://crafatar.com/avatars/${user.uuid}?overlay=true`}></img></div>);
            els.push(<div className={`playersListItem ${this.state.selectedPlayer?.username === user.username && "selected"}`} key={`${user.username}-username`} onClick={clickHandler}>{user.username}</div>)
        }

        return els;
    }

    renderPlayerStats() {
        if (this.state.selectedPlayer) {
            return <div className="playerStats">
                <div className="sectionHeader" style={{zIndex: "1000"}}><img height="30" src={`https://crafatar.com/avatars/${this.state.selectedPlayer.uuid}?overlay=true`} style={{paddingRight: "9px"}}></img>{this.state.selectedPlayer.username}</div>
                <PlayerStats stationData={this.props.stationData} playerData={this.props.playerData} selectedPlayer={this.state.selectedPlayer} />
            </div>
        } else {
            return <div className="playerStatsNotSelected">
                {this.props.t("PLAYERS_SELECT_PROMPT")}
            </div>
        }
    }

    renderMainView() {
        if (Object.keys(this.props.playerData).length === 0) {
            return <div className="errorContainer">
                <h1>{this.props.t("NO_RACE")}</h1>
                <p>{this.props.t("JOIN_RACE_PROMPT")}</p>
            </div>
        } else {
            return <>
                <div className="playersListWrapper" style={{flexGrow: 1}}>
                    <div className="playersListGrid">
                        <div className="sectionHeader" style={{gridArea: "header"}}>{this.props.t("PLAYERS_PLAYERS")}</div>
                        {this.renderPlayers()}
                    </div>
                    <div className="hspacer"></div>
                </div>
                <div style={{width: "20px", height: "20px"}}></div>
                <div className="playerStatsWrapper" style={{flexGrow: 4}}>
                    {this.renderPlayerStats()}
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

export default withTranslation()(Players);