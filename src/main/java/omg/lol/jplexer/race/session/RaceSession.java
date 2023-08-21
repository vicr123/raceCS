package omg.lol.jplexer.race.session;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.j256.ormlite.dao.Dao;
import kong.unirest.Unirest;
import omg.lol.jplexer.race.PlayerStationTracker;
import omg.lol.jplexer.race.Race;
import omg.lol.jplexer.race.models.Station;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class RaceSession implements Listener {
    private final ArrayList<String> joinedPlayers = new ArrayList<>();
    private ArrayList<Station> participatingStations = new ArrayList<>();
    private final ArrayList<String> finishedPlayers = new ArrayList<>();
    private Station terminalStation;
    private boolean isActive = true;
    private int nextPlace = 1;
    private final Scoreboard scoreboard;
    private final RaceSessionLogger logger;
    private RaceSessionTeams teams = null;

    public void teamUp() throws TeamOrganizationException {
        teams = new RaceSessionTeams(this, joinedPlayers);

        var gson = new Gson();
        var array = new JsonArray();
        for (var team : teams.getTeams()) {
            JsonObject jsonObj = new JsonObject();

            jsonObj.addProperty("name", team.getName());
            jsonObj.addProperty("id", team.getId());

            JsonArray playersArray = new JsonArray();
            for (String player : team.getMembers()) {
                playersArray.add(player);
            }
            jsonObj.add("players", playersArray);
            array.add(jsonObj);
        }

        Unirest.post("/teams")
                .contentType("application/json")
                .body(gson.toJson(array))
                .asString();
    }

    public RaceSessionTeams getTeams() {
        return teams;
    }

    public static class RaceEvent {

    }

    public static class StationEvent extends RaceEvent {
        String player;
        Station station;
    }

    private final ArrayList<RaceEvent> events = new ArrayList<>();

    private final PlayerStationTracker.PlayerStationChangeListener stationChangeListener = (player, station) -> {
        if (station == null) {
            processStationLeft(player);
        } else {
            processStationArrived(player, station);
        }
    };

    public static class TerminalStationConflictException extends Exception {}
    public static class DuplicateStationException extends Exception {}

    public RaceSession() {
        logger = new RaceSessionLogger();
        Dao<Station, String> stationDao = Race.getPlugin().getStationDao();
        try {
            terminalStation = stationDao.queryForId("ACSR");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<Station> participatingStations = new ArrayList<>();
        stationDao.forEach(station -> {
            if (!Objects.equals(station, terminalStation) && !station.getId().equals("ACS")) participatingStations.add(station);
        });

        try {
            setParticipatingStations(participatingStations);
        } catch (TerminalStationConflictException e) {
            e.printStackTrace();
        }

        Race.getPlugin().getStationTracker().addStationChangeListener(stationChangeListener);
        Race.getPlugin().getServer().getPluginManager().registerEvents(this, Race.getPlugin());

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("racecs", "dummy");
        objective.setDisplayName("RaceCS Leaderboard");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        var team = scoreboard.registerNewTeam("aircs-race");
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

        updateParticipatingStations();
    }

    public boolean isEnded() {
        return !isActive;
    }

    public void setParticipatingStations(ArrayList<Station> stations) throws TerminalStationConflictException {
        if (stations.contains(terminalStation)) throw new TerminalStationConflictException();
        this.participatingStations = stations;
        updateParticipatingStations();
    }

    public void addParticipatingStation(Station station) throws TerminalStationConflictException, DuplicateStationException {
        if (station == terminalStation) throw new TerminalStationConflictException();
        if (participatingStations.contains(station)) throw new DuplicateStationException();
        participatingStations.add(station);
        updateParticipatingStations();
    }

    public void removeParticipatingStation(Station station) {
        participatingStations.remove(station);
        updateParticipatingStations();
    }

    public void removeAllParticipatingStations() {
        participatingStations.clear();
        updateParticipatingStations();
    }

    public ArrayList<Station> getParticipatingStations() {
        return this.participatingStations;
    }

    public ArrayList<String> getFinishedPlayers() {
        return finishedPlayers;
    }

    public ArrayList<RaceEvent> getEvents() {
        return this.events;
    }

    void updateParticipatingStations() {
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonArray();
        participatingStations.stream().map(Station::getId).forEach(jsonArray::add);

        Unirest.post("/stations")
                .contentType("application/json")
                .body(gson.toJson(jsonArray))
                .asString();
    }

    public void endSession() {
        if (!isActive) return;

        logger.write();

        if (joinedPlayers.isEmpty()) {
            Race.getPlugin().getStationTracker().removeStationChangeListener(stationChangeListener);
            isActive = false;
        } else {
            while (!joinedPlayers.isEmpty()) {
                removePlayer(joinedPlayers.get(0));
            }
        }
    }

    private void setupPlayer(Player player) {
        scoreboard.getTeam("aircs-race").addEntry(player.getName());
    }

    public void addPlayer(Player player) {
        if (teams != null) return;

        joinedPlayers.add(player.getName());
        player.setAllowFlight(false);
        setupPlayer(player);

        logger.playerRegistered(player);

        Unirest.post("/addUser/{player}/{uuid}")
                .routeParam("player", player.getName())
                .routeParam("uuid", player.getUniqueId().toString())
                .asString();
    }

    public void removePlayer(Player player) {
        removePlayer(player.getName());
    }

    public ArrayList<String> getJoinedPlayers() {
        return joinedPlayers;
    }

    public void removePlayer(String playerName) {
        Player player = Race.getPlugin().getServer().getPlayer(playerName);
        if (player != null && player.isOnline()) {
            player.setAllowFlight(true);
        }
        scoreboard.getTeam("aircs-race").removeEntry(playerName);
        joinedPlayers.remove(playerName);

        Unirest.post("/removeUser/{player}")
                .routeParam("player", playerName)
                .asString();

        if (joinedPlayers.isEmpty()) endSession();
    }

    public void processStationLeft(Player player) {
        if (!joinedPlayers.contains(player.getName())) return; //This player is not in the race
        if (finishedPlayers.contains(player.getName())) return; //This player has already finished
        logger.appendStationLeave(player);
    }

    public void processStationArrived(Player player, Station station) {
        if (!joinedPlayers.contains(player.getName())) return; //This player is not in the race
        if (finishedPlayers.contains(player.getName())) return; //This player has already finished
        long visitedCount = events.stream().filter(event -> event instanceof StationEvent).filter(event -> Objects.equals(((StationEvent) event).player, player.getName())).count();

        logger.appendStationArrive(player, station);
        if (Objects.equals(station, terminalStation)) {
            if (teams == null) {
                //Count the number of stations this player has visited
                if (visitedCount >= participatingStations.size()) {
                    //Completion!
                    processPlayerCompletion(player);
                }
            } else {
                var team = teams.teamFor(player);
                if (team.memberReturned(player)) return; // This player has already finished

                if (team.getVisitedStations().size() >= participatingStations.size()) {
                    team.setMemberReturned(player);
                    player.setAllowFlight(true);

                    Unirest.post("/arrive/{player}/completion")
                            .routeParam("player", player.getName())
                            .asString();

                    if (team.membersWaitingToReturn() == 0) {
                        // Team completion!
                        processTeamCompletion(team);
                    } else {
                        Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GREEN + player.getName() + " has returned to the terminal station. " + team.membersWaitingToReturn() + " more team members from " + team.getName() + " still need to return!");
                    }
                }
            }
        } else {
            if (!participatingStations.contains(station)) return;

            for (RaceEvent event : events) {
                if (event instanceof StationEvent) {
                    StationEvent se = (StationEvent) event;
                    if (Objects.equals(se.player, player.getName()) && se.station.equals(station)) return; //Nothing interesting has happened
                }
            }

            Unirest.post("/arrive/{player}/{location}")
                    .routeParam("player", player.getName())
                    .routeParam("location", station.getId())
                    .asString();

            StationEvent newEvent = new StationEvent();
            newEvent.player = player.getName();
            newEvent.station = station;
            events.add(newEvent);

            if (teams == null) {
                Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GREEN + player.getName() + " has arrived at " + station.getHumanReadableName() + "!");
                if (visitedCount + 1 == participatingStations.size()) {
                    Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GOLD + player.getName() + " has visited all the required stations and is now returning to the terminal station!");
                } else if (visitedCount + 1 == (participatingStations.size() + 1) / 2) {
                    Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GOLD + player.getName() + " has visited half the required stations!");
                }
            } else {
                var team = teams.teamFor(player);
                if (team.pushStation(station)) {
                        Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GREEN + player.getName() + " has arrived at " + station.getHumanReadableName() + " and claimed it for " + team.getName() + "!");
                    if (team.getVisitedStations().size() == participatingStations.size()) {
                        Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GREEN + team.getName() + " has visited all the required stations and is now returning to the terminal station!");
                    } else if (team.getVisitedStations().size() == (participatingStations.size() + 1) / 2) {
                        Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GOLD + team.getName() + " has visited half the required stations!");
                    }
                } else {
                    Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GREEN + player.getName() + " has arrived at " + station.getHumanReadableName() + "!");
                }
            }
        }
    }

    void processPlayerCompletion(Player player) {
        Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GOLD + player.getName() + " has finished as #" + nextPlace + "!");
        logger.appendPlayerFinished(player, nextPlace);
        player.setAllowFlight(true);

        Unirest.post("/completion/{player}/{place}")
                .routeParam("player", player.getName())
                .routeParam("place", String.valueOf(nextPlace))
                .asString();

        //Play the SFX
        joinedPlayers.stream().map(player1 -> Race.getPlugin().getServer().getPlayer(player1)).filter(Objects::nonNull).forEach(player1 -> player1.playSound(player1.getLocation(), finishedPlayers.isEmpty() ? Sound.UI_TOAST_CHALLENGE_COMPLETE : Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1));
        finishedPlayers.add(player.getName());

        nextPlace++;
    }

    void processTeamCompletion(omg.lol.jplexer.race.session.Team team) {
        Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GOLD + team.getName() + " has finished as #" + nextPlace + "!");

        Unirest.post("/completion/team/{team}/{place}")
                .routeParam("team", team.getId())
                .routeParam("place", String.valueOf(nextPlace))
                .asString();

        //Play the SFX
        joinedPlayers.stream().map(player1 -> Race.getPlugin().getServer().getPlayer(player1)).filter(Objects::nonNull).forEach(player1 -> player1.playSound(player1.getLocation(), finishedPlayers.isEmpty() ? Sound.UI_TOAST_CHALLENGE_COMPLETE : Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1));

        nextPlace++;
    }

    public void syncPulse() {
        logger.appendSyncPulse();
    }

    public Station getTerminalStation() {
        return terminalStation;
    }

    public void setTerminalStation(Station terminalStation) throws TerminalStationConflictException {
        if (participatingStations.contains(terminalStation)) throw new TerminalStationConflictException();
        this.terminalStation = terminalStation;
    }

    public void playersCollided(Player p1, Player p2) {
        logger.appendCollision(p1, p2);
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        if (!isActive) return;
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (joinedPlayers.contains(player.getName())) {
                if (event.getDismounted() instanceof Minecart || event.getDismounted() instanceof Boat) {
                    Bukkit.getScheduler().runTaskLater(Race.getPlugin(), () -> event.getDismounted().remove(), 1);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isActive) return;
        setupPlayer(event.getPlayer());
    }
}
