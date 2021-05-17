import React from 'react';
import { withTranslation } from 'react-i18next';

class Ticker extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return <div className="ticker">
            <div className={`headerButton selected`}>AirCS</div>
        </div>
    }
}

export default withTranslation()(Ticker);