import Styles from "./Teams.module.css"
import {useTranslation} from "react-i18next";
import {useState} from "react";
import {StationItem} from "./playerStats";
import heads from "./heads";

function Headed({title, children, className}) {
    return <div className={`${Styles.headed} ${className}`}>
        <div className={Styles.headedTitle}>{title}</div>
        <div className={Styles.headedChildren}>
            {children}
        </div>
    </div>
}

function TeamItem({team, onSetCurrentTeam, currentTeam}) {
    const setTeam = () => {
        onSetCurrentTeam(team.id)
    };

    return <div className={`${Styles.teamListItem} ${currentTeam === team.id && Styles.teamListItemSelected}`} onClick={setTeam}>
        {team.name}
    </div>
}

function TeamProgress({team, claimed, total}) {
    const {t} = useTranslation();

    return <div className={Styles.teamProgress}>
        <div className={Styles.teamProgressBar}>
            <div className={Styles.teamProgressBarFill} style={{width: `${claimed / total * 100}%`}} />
        </div>
        <div className={Styles.teamProgressClaimed}>
            <span className={Styles.teamProgressBigNum}>{claimed}</span>
            <span>{t("STATIONS_CLAIMED")}</span>
        </div>
        <div className={Styles.teamProgressRemaining}>
            <span className={Styles.teamProgressBigNum}>{total - claimed}</span>
            <span>{t("STATIONS_REMAINING")}</span>
        </div>
    </div>
}

function Interstitial({icon, text}) {
    return <div className={Styles.interstitial}>
        {icon && <img height={64} src={icon} />}
        {text}
    </div>
}

function TeamData({team, playerData, stationData}) {
    const {t} = useTranslation();

    let claimed = [];
    let unclaimed = [];
    for (const station of Object.keys(stationData)) {
        if (team.visited?.includes(station)) {
            claimed.push(station)
        } else {
            unclaimed.push(station)
        }
    }

    return <div className={Styles.teamData}>
        <span className={Styles.teamDataHeader}>{t("STATION_PROGRESS")}</span>
        <TeamProgress team={team} claimed={claimed.length} total={claimed.length + unclaimed.length} />
        <div className={Styles.teamDataGrid}>
            <Headed className={Styles.teamDataClaimed} title={t("CLAIMED_STATIONS")}>
                {claimed.length ? <div className={"playerStatsSectionContents playerStationList"}>
                    {claimed.map(shortcode => <StationItem key={shortcode} station={shortcode} stationData={stationData[shortcode]} />)}
                </div> : <Interstitial text={t("NO_CLAIMED_STATIONS")} /> }
            </Headed>
            <Headed className={Styles.teamDataRemain} title={t("PLAYERSTATS_REMAINING")}>
                {unclaimed.length ? <div className={"playerStatsSectionContents playerStationList"}>
                    {unclaimed.map(shortcode => <StationItem key={shortcode} station={shortcode} stationData={stationData[shortcode]} />)}
                </div> : <Interstitial text={t("NO_UNCLAIMED_STATIONS")} icon={"/login_notification.png"} /> }
            </Headed>
            <Headed className={Styles.teamDataMembers} title={t("TEAM_DATA_MEMBERS")}>
                <div className={Styles.teamDataMembersContent}>
                    {team.players.map(player => {
                        const clickHandler = () => {

                        }

                        const user = playerData[player];
                        return <>
                            <div className={Styles.teamDataMembersContentItem}>
                                {team.returned?.includes(player) ? <img height="30" src={"/login_notification.png"}></img> : <div style={{width: "30px", height: "30px"}} />}
                            </div>
                            <div className={Styles.teamDataMembersContentItem} onClick={clickHandler}><img height="30" src={heads(user.uuid)}></img></div>
                            <div className={Styles.teamDataMembersContentItem} onClick={clickHandler}>{player}</div>
                        </>
                    })}
                </div>
            </Headed>
        </div>
    </div>
}

export default function Teams({stationData, playerData, teamData}) {
    const [currentTeam, setCurrentTeam] = useState();
    const {t} = useTranslation();

    const currentTeamData = teamData.find(team => team.id === currentTeam);

    return <div className={Styles.root}>
        <Headed title={t("TEAMS_LIST")} className={Styles.teamList}>
            {/*{JSON.stringify(teamData)}*/}
            {/*{JSON.stringify(playerData)}*/}
            {teamData.map(team => <TeamItem team={team} onSetCurrentTeam={setCurrentTeam} currentTeam={currentTeam} />)}
        </Headed>
        {currentTeamData && <Headed title={t(`TEAM_DATA_TITLE`, {
                teamName: currentTeamData.name
            })}>
            <TeamData team={currentTeamData} playerData={playerData} stationData={stationData} />
        </Headed>}
    </div>
}

