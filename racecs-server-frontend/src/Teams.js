export default function Teams({stationData, playerData, teamData}) {
    return <div>
        <span>Current state of race:</span>
        <span>{JSON.stringify(teamData)}</span>
    </div>
}