import React from 'react';
import { withTranslation } from 'react-i18next';

class Ticker extends React.Component {
    constructor(props) {
        super(props);
        
        let tickerItems = [];

        let availableItems = [
            "Victor Tran becomes manager of McDonald's Pushkin Square (Moscow)",
            "LG leaves smartphone business",
            "Google Maps loses regional town",
            "The FCC releases a new speed test app for Android",
            "The Marquee tag is considered obsolete",
            "The AirCS race of April 2021 announced",
            "Squid Airlines pilot crashes into bridge placed over runway",
            "Elon Musk acquires ElyFly for $1bn",
            "Carrots declared illegal in Brazil",
            "Microsoft reportedly set to aquire theSuite line of software",
            "Arenchant goes Public, worth $1m",
            "Victor says \"crap\", whole server horrified",
            "Heated debate ensues after shopper demands change to Aldi trolley sizes",
            "Glimpse delighted to announce partnership with Adrian",
            "Anders Enger Jensen's DiscoVision reaches #1 on Spotify Charts",
            "Radiation poisoning detected in 54 people after wearing \"Radiation Baby\" mask",
            "Scientific evidence shows that 99% of the time, when you spell words wrong, it's because they're spelled wrong in English!",
            "Clear Dapple Dualies found disposed of in MakoMart",
            "N64 Rainbow Road train derails and crashes onto track",
            "Victor Tran's bank statement found in a trashcan, shows he spent $3000 at Glimpse",
            "Up acquires Down for $200m, changes name to Center",
            "Apple announces the next version of macOS: macOS Weed",
            "Melanie McDonald sues Melanie City for using her name without permission",
            "Lionel Richie sues Adele for reusing the name of his song: Hello",
            "Auckland changes colour scheme, imposing a $1t bill to rebrand the city",
            "Walnut encounters unexpected shortage on walnuts"
        ]
        
        for (let i = 0; i < 10; i++) {
            tickerItems.push(availableItems.splice(Math.floor(Math.random() * (availableItems.length)), 1)[0]);
        }

        this.state = {
            tickerItems: tickerItems
        }
    }

    render() {
        return <div className="ticker">
            <div className={`headerButton selected`}>{this.props.t("TICKER_NEWS")}</div>
            <marquee>{this.state.tickerItems.join(" • ")}</marquee>
        </div>
    }
}

export default withTranslation()(Ticker);