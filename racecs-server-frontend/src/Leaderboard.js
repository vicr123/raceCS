import React from 'react';
import Common from './common';

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

            els.push(<div className="leaderboardGridItem" key={`${user.username}-image`} onClick={clickHandler}><img height="30" src={`https://crafatar.com/avatars/${user.uuid}?overlay=true`}></img></div>);
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

    render() {
        return <div className="mainView">
            <div className="leaderboardGridWrapper">
                <div className="leaderboardGrid">
                    <div className="sectionHeader"></div>
                    <div className="sectionHeader"></div>
                    <div className="sectionHeader">Name</div>
                    <div className="sectionHeader">Stations Visited</div>

                    {this.renderPlayerData()}
                </div>
                <div className="hspacer"></div>
            </div>
            <div style={{width: "20px", height: "20px"}}></div>
            <div className="recentEventsWrapper">
                <div className="sectionHeader">Recent Events</div>
                {this.renderRecentEvents()}
            </div>
        </div>
    }
};

export default Leaderboard;