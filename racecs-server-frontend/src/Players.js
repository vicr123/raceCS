import React from 'react';
import PlayerStats from './playerStats';

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
                <div className="sectionHeader"><img height="30" src={`https://crafatar.com/avatars/${this.state.selectedPlayer.uuid}`} style={{paddingRight: "9px"}}></img>{this.state.selectedPlayer.username}</div>
                <PlayerStats stationData={this.props.stationData} playerData={this.props.playerData} selectedPlayer={this.state.selectedPlayer} />
            </div>
        } else {
            return <div className="playerStatsNotSelected">
                Select a player to get started.
            </div>
        }
    }

    render() {
        return <div className="mainView">
            <div className="playersListWrapper" style={{flexGrow: 1}}>
                <div className="playersListGrid">
                    <div className="sectionHeader" style={{gridArea: "header"}}>Players</div>
                    {this.renderPlayers()}
                </div>
                <div className="hspacer"></div>
            </div>
            <div style={{width: "20px", height: "20px"}}></div>
            <div className="playerStatsWrapper" style={{flexGrow: 4}}>
                {this.renderPlayerStats()}
            </div>
        </div>
    }
};

export default Players;