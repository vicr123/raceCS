import React from 'react';

class NotificationDrawer extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            notifications: [
            ]
        }
    }

    componentDidMount() {
        this.props.websocket.on("message", data => {
            switch (data.type) {
                case "visitation":
                    this.pushNotification({
                        "title": "Station Accomplished",
                        "message": `${data.user} has just arrived at ${data.station}`,
                        "color": "white",
                        "backgroundColor": "green",
                        "key": `visit${data.user}:${data.station}`
                    })
                    break;
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
            els.push(<div className="notification" key={notification.key} style={{
                backgroundColor: notification.backgroundColor,
                color: notification.color
            }}>
                <p>{notification.title}</p>
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
    }

    render() {
        return <div className="notificationDrawer">
            {this.renderNotifications()}
        </div>
    }
}

export default NotificationDrawer;