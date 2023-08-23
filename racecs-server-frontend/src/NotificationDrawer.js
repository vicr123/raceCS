import React from 'react';
import Common from './common';
import { withTranslation } from 'react-i18next';

import LoginIcon from './login_black_24dp.svg';
import RailwayAlertIcon from './railway_alert_black_24dp.svg';
import FlagIcon from './sports_score_black_24dp.svg';
import WhiteLoginIcon from './login_white_24dp.svg';
import WhiteRailwayAlertIcon from './railway_alert_white_24dp.svg';
import WhiteFlagIcon from './sports_score_white_24dp.svg';

class NotificationDrawer extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            notifications: []
        }
    }

    componentDidMount() {
        this.props.websocket.on("message", data => {
            switch (data.type) {
                case "visitation": {
                    this.pushNotification({
                        "title": this.props.t("NOTIFICATION_ARRIVAL"),
                        "message": this.props.t("NOTIFICATION_ARRIVAL_MESSAGE", {
                            user: data.user,
                            stationName: this.props.stationData[data.station].name
                        }),
                        "color": "white",
                        "backgroundColor": "green",
                        "key": `visit${data.user}:${data.station}`,
                        "image": LoginIcon,
                        "whiteIcon": WhiteLoginIcon,
                        "type": "visitation"
                    });
                    break;
                }
                case "collision":
                    this.pushNotification({
                        "title": this.props.t("NOTIFICATION_COLLISION"),
                        "message": this.props.t("NOTIFICATION_COLLISION_MESSAGE", {
                            player1: data.player1,
                            player2: data.player2
                        }),
                        "color": "white",
                        "backgroundColor": "red",
                        "key": `collide${data.player1}:${data.player2}`,
                        "image": RailwayAlertIcon,
                        "whiteIcon": WhiteRailwayAlertIcon,
                        "type": "collision"
                    });
                    break;
                case "completion": {
                    this.pushNotification({
                        "title": this.props.t("NOTIFICATION_FINISH"),
                        "message": this.props.t("NOTIFICATION_FINISH_MESSAGE", {
                            player: data.username,
                            place: Common.getOrdinal(data.place)
                        }),
                        "color": "white",
                        "backgroundColor": "orange",
                        "key": `completion${data.username}`,
                        "image": FlagIcon,
                        "whiteIcon": WhiteFlagIcon,
                        "type": "completion"
                    });
                    break;
                }
                case "completion-partial": {
                    this.pushNotification({
                        "title": this.props.t("NOTIFICATION_PARTIAL_COMPLETION"),
                        "message": this.props.t("NOTIFICATION_PARTIAL_COMPLETION_MESSAGE", {
                            player: data.player,
                            team: data.team,
                            remaining: data.remaining
                        }),
                        "color": "white",
                        "backgroundColor": "#4287f5",
                        "key": `completion-partial-${data.player}`,
                        "image": FlagIcon,
                        "whiteIcon": WhiteFlagIcon,
                        "type": "completionPartial"
                    });
                    break;
                }
                case "completion-team": {
                    this.pushNotification({
                        "title": this.props.t("NOTIFICATION_FINISH"),
                        "message": this.props.t("NOTIFICATION_FINISH_TEAM_MESSAGE", {
                            player: data.player,
                            team: data.team,
                            place: Common.getOrdinal(data.place)
                        }),
                        "color": "white",
                        "backgroundColor": "orange",
                        "key": `completion-team-${data.player}`,
                        "image": FlagIcon,
                        "whiteIcon": WhiteFlagIcon,
                        "type": "completion"
                    });
                    break;
                }
                case "newPlayer":
                case "removePlayer":
                    break;
            }
        });
    }

    renderNotifications() {
        let els = [<span></span>];

        for (let notification of this.state.notifications) {
            console.log(notification);
            els.push(<div className="notification" key={notification.key}>
                <div className="notificationSplash" style={{
                    backgroundColor: notification.backgroundColor,
                    color: notification.color,
                    gridArea: "splash"
                }}>
                    <img src={notification.whiteIcon} />
                    {notification.title}
                </div>
                <p>{notification.message}</p>
            </div>)
        }

        return els;
    }

    pushNotification(notification) {
        this.setState(state => {
            return {
                notifications: [...state.notifications, notification]
            }
        });

        setTimeout(() => {
            this.setState(state => {
                return {
                    notifications: state.notifications.slice(1)
                }
            })
        }, 5000);

        this.props.onNotification(notification);
    }

    render() {
        return <div className="notificationDrawer">
            {this.renderNotifications()}
        </div>
    }
}

export default withTranslation()(NotificationDrawer);