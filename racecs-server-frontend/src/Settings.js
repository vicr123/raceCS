import fetch from 'node-fetch';
import React from 'react';

class Settings extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            notificationsRegistered: false,
            processing: false
        }
    }

    componentDidMount() {
        this.updateNotificationState();
    }

    renderNotificationsButtons() {
        if (!window.PushManager) {
            return <span>Notifications are not supported on this browser.</span>
        }
        if (this.state.processing) {
            return <span>Enabling notifications...</span>
        }

        switch (Notification.permission) {
            case "granted": {
                if (this.state.notificationsRegistered) {
                    return <div>
                        <button onClick={this.disableNotifications.bind(this)}>Disable Notifications</button>
                    </div>
                } else {
                    return <div>
                        <button onClick={this.enableNotifications.bind(this)}>Enable Notifications</button>
                    </div>
                }
            }
            case "denied": {
                return <span>Check the settings for notifications for this webpage in your browser, and reload to try again.</span>
            }
            default: {
                let enableNotifications = async () => {
                    try {
                        if (await Notification.requestPermission() == "granted") {
                            this.enableNotifications();
                        }
                    } finally {
                        this.forceUpdate();
                    }
                };

                return <div>
                    <button onClick={enableNotifications}>Enable Notifications</button>
                </div>
            }
        }
    }

    enableNotifications() {
        this.setState({
            processing: true
        }, async () => {
            let worker = await navigator.serviceWorker.register("/NotificationsServiceWorker.js");
            await navigator.serviceWorker.ready;
            let subscription = await worker.pushManager.subscribe({
                userVisibleOnly: true,
                applicationServerKey: process.env.REACT_APP_VAPID_SERVER_KEY
            });
    
            //Send the subscription to the server
            await fetch("/api/registerpush", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    subscription: JSON.parse(JSON.stringify(subscription))
                })
            });
    
            await this.updateNotificationState();
            this.setState({
                processing: false
            });
        });
    }

    async disableNotifications() {
        let registration = await navigator.serviceWorker.getRegistration("/NotificationsServiceWorker.js");
        if (registration) await registration.unregister();
        this.updateNotificationState();
    }

    async updateNotificationState() {
        let registration = await navigator.serviceWorker.getRegistration("/NotificationsServiceWorker.js");
        this.setState({
            notificationsRegistered: registration ? true : false
        });
    }

    render() {
        return <div className="mainView">
            <div className="settingsWrapper">
                <div className="sectionHeader">Settings</div>
                <div className="settingsInnerWrapper">
                    <b>NOTIFICATIONS</b>
                    <span>Enable notifications to get AirCS Race updates in the background.</span>
                    {this.renderNotificationsButtons()}
                </div>
            </div>
        </div>
    }
};

export default Settings;