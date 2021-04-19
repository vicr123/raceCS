import fetch from 'node-fetch';
import React from 'react';
import i18n from './i18n';
import { withTranslation } from 'react-i18next';

class Settings extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            notificationsRegistered: false,
            processing: false,
            locale: localStorage.getItem("locale") || "sys",
            stationLocale: localStorage.getItem("stationLocale") || "sys"
        }
    }

    componentDidMount() {
        this.updateNotificationState();
    }

    renderNotificationsButtons() {
        if (!window.PushManager) {
            return <span>{this.props.t("SETTINGS_NOTIFICATIONS_NOT_SUPPORTED")}</span>
        }
        if (this.state.processing) {
            return <span>{this.props.t("SETTINGS_NOTIFICATIONS_ENABLING")}</span>
        }

        switch (Notification.permission) {
            case "granted": {
                if (this.state.notificationsRegistered) {
                    return <div>
                        <button onClick={this.disableNotifications.bind(this)}>{this.props.t("SETTINGS_NOTIFICATIONS_DISABLE")}</button>
                    </div>
                } else {
                    return <div>
                        <button onClick={this.enableNotifications.bind(this)}>{this.props.t("SETTINGS_NOTIFICATIONS_ENABLE")}</button>
                    </div>
                }
            }
            case "denied": {
                return <span>{this.props.t("SETTINGS_NOTIFICATIONS_DECLINED")}</span>
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
                    <button onClick={enableNotifications}>{this.props.t("SETTINGS_NOTIFICATIONS_ENABLE")}</button>
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

    localeChanged(event) {
        this.setState({
            locale: event.target.value
        });

        localStorage.setItem("locale", event.target.value);
        if (event.target.value === "sys") {
            i18n.changeLanguage();
        } else {
            i18n.changeLanguage(event.target.value);
        }
        
        this.props.onLocaleChange();
    }

    renderLocale() {
        return <select value={this.state.locale} onChange={this.localeChanged.bind(this)}>
            <option value="sys">{this.props.t("SETTINGS_LOCALE_SYSTEM")}</option>
            <option value="en">English</option>
            <option value="de">Deutsch</option>
            <option value="pt_BR">português</option>
            <option value="vi">Tiếng Việt</option>
        </select>
    }

    stationLocaleChanged(event) {
        this.setState({
            stationLocale: event.target.value
        });

        localStorage.setItem("stationLocale", event.target.value);
        this.props.onLocaleChange();
    }

    renderStationLocale() {
        return <select value={this.state.stationLocale} onChange={this.stationLocaleChanged.bind(this)}>
            <option value="sys">{this.props.t("SETTINGS_LOCALE_SYSTEM")}</option>
            <option value="en">English</option>
            <option value="de">Deutsch</option>
            <option value="pt_BR">português</option>
            <option value="vi">Tiếng Việt</option>
        </select>
    }

    render() {
        return <div className="mainView">
            <div className="settingsWrapper">
                <div className="sectionHeader">{this.props.t("APP_SETTINGS")}</div>
                <div className="settingsInnerWrapper">
                    <b>{this.props.t("SETTINGS_NOTIFICATIONS")}</b>
                    <span>{this.props.t("SETTINGS_NOTIFICATIONS_DESCRIPTION")}</span>
                    {this.renderNotificationsButtons()}

                    <b>{this.props.t("SETTINGS_LOCALE")}</b>
                    <span>{this.props.t("SETTINGS_LOCALE_DESCRIPTION")}</span>
                    {this.renderLocale()}

                    <b>{this.props.t("SETTINGS_STATION_LOCALE")}</b>
                    <span>{this.props.t("SETTINGS_STATION_LOCALE_DESCRIPTION")}</span>
                    {this.renderStationLocale()}
                </div>
            </div>
        </div>
    }
};

export default withTranslation()(Settings);