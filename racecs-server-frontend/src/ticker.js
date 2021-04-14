import React from 'react';

class Ticker extends React.Component {
    render() {
        return <div className="ticker">
            <div className={`headerButton selected`}>NEWS</div>
            <marquee>The AirCS Race of April 2021 has been announced! &bull; LG pulls out of smartphone business &bull; Google Maps loses regional town &bull; The FCC has released a new speed test app for Android &bull; The Marquee tag is considered obsolete</marquee>
            <div style={{flexGrow: 1}}></div>
        </div>
    }
}

export default Ticker;