import React from 'react';


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
            "Microsoft reportedly set to aquire theSuite line of software"
        ]
        
        for (let i = 0; i < 5; i++) {
            tickerItems.push(availableItems.splice(Math.floor(Math.random() * (availableItems.length)), 1)[0]);
        }

        this.state = {
            tickerItems: tickerItems
        }
    }

    render() {
        return <div className="ticker">
            <div className={`headerButton selected`}>NEWS</div>
            <marquee>{this.state.tickerItems.join(" • ")}</marquee>
        </div>
    }
}

export default Ticker;