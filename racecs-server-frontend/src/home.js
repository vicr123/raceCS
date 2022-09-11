import React from 'react';
import Common from './common';
import heads from './heads';
import Fetch from './Fetch';
import { withTranslation } from 'react-i18next';

import NewsIcon from './article_black_24dp.svg';
import VideosIcon from './movie_black_24dp.svg';
import EventsIcon from './event_black_24dp.svg';

class Post extends React.Component {
    render() {
        return <div>
            <div className="sectionHeader" style={{gridArea: "header"}}>{this.props.title}</div>
            <div className="postText">
                {this.props.data.text.map(text => <p>{text}</p>)}
            </div>
        </div>
    }
}

class PostList extends React.Component {
    renderList() {
        let els = [];
        
        let i = 0;

        let d = this.props.data.data;
        d.reverse();
        for (let item of d) {
            let click = () => {
                if (!item.click) return;

                switch (item.click.type) {
                    case "url":
                        //Open the URL in another window
                        window.open(item.click.url, "_blank");
                        break;
                    case "post":
                        this.props.onPush(<Post title={item.title} data={item.click} />)
                }
            };

            let parts = [];
            if (item.title) parts.push(<span style={{gridArea: "title"}} className="postTitle">{item.title}</span>);
            if (item.subtitle) parts.push(<span style={{gridArea: "subtitle"}} className="postSubtitle">{item.subtitle}</span>);
            if (item.date) parts.push(<span style={{gridArea: "date"}}  className="postDate">{(new Date(item.date).toLocaleString())}</span>);
            if (item.image) parts.push(<span style={{gridArea: "image"}}  className="postImage"><img src={item.date} /></span>);

            els.push(<div key={i} onClick={click} className="postListItem">
                {parts}
            </div>);
            i++;
        }

        return els;
    }

    render() {
        if (this.props.data.data.length === 0) {
            return <div className="playerStatsNotSelected errorContainer">
                <h1>{this.props.data.empty.title}</h1>
                <p>{this.props.data.empty.subtitle}</p>
            </div>
        } else {
            return <div>
                <div className="sectionHeader" style={{gridArea: "header"}}>{this.props.title}</div>
                {this.renderList()}
            </div>
        }
    }
}

class Home extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            state: "loading",
            pages: [],
            selectedPage: "none"
        }
    }

    async componentDidMount() {
        this.setState({
            state: "loaded",
            data: await Fetch.get("/home"),
            pages: []
        });
    }

    renderPage() {
        if (this.state.pages.length === 0) {
            return <div className="playerStatsNotSelected errorContainer">
                <h1>{this.props.t("HOME_SELECT_PROMPT")}</h1>
                <p>{this.props.t("HOME_SELECT_PROMPT_DESCRIPTION")}</p>
            </div>
        } else {
            return this.state.pages[this.state.pages.length - 1];
        }
    }

    pushPage(page) {
        this.setState(state => {
            let pages = state.pages;
            pages.push(page);
            return {
                pages: pages
            }
        });
    }

    popPage() {

    }

    setPage(root) {
        let page;
        switch (root) {
            case "news":
                page = <PostList onPush={this.pushPage.bind(this)} onPop={this.popPage.bind()} data={this.state.data.news} title={this.props.t("HOME_NEWS")} />
                break;
            case "videos":
                page = <PostList onPush={this.pushPage.bind(this)} onPop={this.popPage.bind()} data={this.state.data.videos} title={this.props.t("HOME_VIDEOS")} />
                break;
            case "events":
                page = <PostList onPush={this.pushPage.bind(this)} onPop={this.popPage.bind()} data={this.state.data.races} title={this.props.t("HOME_EVENTS")} />
                break;
        }

        this.setState({
            selectedPage: root,
            pages: [
                page
            ]
        });
    }

    renderLinks() {
        let els = [];

        for (let link of this.state.data.links) {
            let clickHandler = () => {
                if (link.href) window.open(link.href, "_blank");
            }

            els.push(<div className={`playersListItem`} onClick={clickHandler}><img width="24" src={link.image}></img></div>)
            els.push(<div className={["playersListItem", ...(link.href ? [] : ["itemTitle"])].join(" ")} onClick={clickHandler}>{link.text}</div>)
        }

        return els;
    }

    renderMainView() {
        if (this.state.state === "loading") {
            return <div className="errorContainer">
                <h1>{this.props.t("HOME_GETTING_READY")}</h1>
                <p>{this.props.t("HOME_GETTING_READY_DESCRIPTION")}</p>
            </div>
        } else {
            return <>
                <div className="playersListWrapper" style={{flexGrow: 1}}>
                    <div className="playersListGrid">
                        <div className="sectionHeader" style={{gridArea: "header"}}>{this.props.t("HOME_TITLE")}</div>
                        {/* {this.renderPlayers()} */}

                        <div className={`playersListItem ${this.state.selectedPage === "news" && "selected"}`} onClick={this.setPage.bind(this, "news")}><img width="24" src={NewsIcon}></img></div>
                        <div className={`playersListItem ${this.state.selectedPage === "news" && "selected"}`} onClick={this.setPage.bind(this, "news")}>{this.props.t("HOME_NEWS")}</div>

                        <div className={`playersListItem ${this.state.selectedPage === "videos" && "selected"}`} onClick={this.setPage.bind(this, "videos")}><img width="24" src={VideosIcon}></img></div>
                        <div className={`playersListItem ${this.state.selectedPage === "videos" && "selected"}`} onClick={this.setPage.bind(this, "videos")}>{this.props.t("HOME_VIDEOS")}</div>

                        <div className={`playersListItem ${this.state.selectedPage === "events" && "selected"}`} onClick={this.setPage.bind(this, "events")}><img width="24" src={EventsIcon}></img></div>
                        <div className={`playersListItem ${this.state.selectedPage === "events" && "selected"}`} onClick={this.setPage.bind(this, "events")}>{this.props.t("HOME_EVENTS")}</div>

                        {this.renderLinks()}
                    </div>
                    <div className="hspacer"></div>
                </div>
                <div style={{width: "20px", height: "20px"}}></div>
                <div className="playerStatsWrapper" style={{flexGrow: 4}}>
                    {this.renderPage()}
                </div>
            </>
        }
    }

    render() {
        return <div className="mainView">
            {this.renderMainView()}
        </div>
    }
};

export default withTranslation()(Home);