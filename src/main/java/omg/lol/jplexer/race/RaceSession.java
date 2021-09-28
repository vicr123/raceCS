package omg.lol.jplexer.race;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.j256.ormlite.dao.Dao;
import jdk.nashorn.internal.ir.Terminal;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import omg.lol.jplexer.race.models.Station;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static omg.lol.jplexer.race.Race.CHAT_PREFIX;

public class RaceSession {
    private final ArrayList<String> joinedPlayers = new ArrayList<>();
    private ArrayList<Station> participatingStations = new ArrayList<>();
    private ArrayList<String> finishedPlayers = new ArrayList<>();
    private Station terminalStation;
    private boolean isActive = true;
    private int nextPlace = 1;
    private Scoreboard scoreboard;

    class RaceEvent {

    }

    class StationEvent extends RaceEvent {
        String player;
        Station station;
    }

    private ArrayList<RaceEvent> events = new ArrayList<>();

    private final PlayerStationTracker.PlayerStationChangeListener stationChangeListener = (player, station) -> {
        if (station != null) {
            processStationArrived(player, station);
        }
    };

    public static class TerminalStationConflictException extends Exception {}

    RaceSession() {
        Dao<Station, String> stationDao = Race.getPlugin().getStationDao();
        try {
            terminalStation = stationDao.queryForId("ACS");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<Station> participatingStations = new ArrayList<>();
        stationDao.forEach(station -> {
            if (!Objects.equals(station, terminalStation)) participatingStations.add(station);
        });

        try {
            setParticipatingStations(participatingStations);
        } catch (TerminalStationConflictException e) {
            e.printStackTrace();
        }

        Race.getPlugin().getStationTracker().addStationChangeListener(stationChangeListener);

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("racecs", "dummy");
        objective.setDisplayName("RaceCS Leaderboard");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public boolean isEnded() {
        return !isActive;
    }

    public void setParticipatingStations(ArrayList<Station> stations) throws TerminalStationConflictException {
        if (stations.contains(terminalStation)) throw new TerminalStationConflictException();
        this.participatingStations = stations;
        updateParticipatingStations();
    }

    public void addParticipatingStation(Station station) throws TerminalStationConflictException {
        if (station == terminalStation) throw new TerminalStationConflictException();
        participatingStations.add(station);
        updateParticipatingStations();
    }

    public void removeParticipatingStation(Station station) {
        participatingStations.remove(station);
        updateParticipatingStations();
    }

    public ArrayList<Station> getParticipatingStations() {
        return this.participatingStations;
    }

    void updateParticipatingStations() {
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonArray();
        participatingStations.stream().map(Station::getId).forEach(jsonArray::add);

        Unirest.post("/stations")
                .contentType("application/json")
                .body(gson.toJson(jsonArray));
    }

    public void endSession() {
        if (!isActive) return;

        if (joinedPlayers.isEmpty()) {
            Race.getPlugin().getStationTracker().removeStationChangeListener(stationChangeListener);
            isActive = false;
        } else {
            while (!joinedPlayers.isEmpty()) {
                removePlayer(joinedPlayers.get(0));
            }
        }
    }

    public void addPlayer(Player player) {
        joinedPlayers.add(player.getName());
        player.setScoreboard(scoreboard);

        updateScoreboards();

        Unirest.post("/addUser/{player}/{uuid}")
                .routeParam("player", player.getName())
                .routeParam("uuid", player.getUniqueId().toString())
                .asString();
    }

    public void removePlayer(Player player) {
        removePlayer(player.getName());
    }


    public void removePlayer(String playerName) {
        Player player = Race.getPlugin().getServer().getPlayer(playerName);
        if (player != null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        joinedPlayers.remove(playerName);
        updateScoreboards();

        Unirest.post("/removeUser/{player}")
                .routeParam("player", playerName)
                .asString();

        if (joinedPlayers.isEmpty()) endSession();
    }

    public void processStationArrived(Player player, Station station) {
        long visitedCount = events.stream().filter(event -> event instanceof StationEvent).filter(event -> Objects.equals(((StationEvent) event).player, player.getName())).count();

        if (Objects.equals(station, terminalStation)) {
            if (finishedPlayers.contains(player.getName())) return; //This player has already finished

            //Count the number of stations this player has visited
            if (visitedCount == participatingStations.size()) {
                //Completion!
                Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GOLD + player.getName() + " has finished as #" + nextPlace + "!");

                Unirest.post("/completion/{player}/{place}")
                        .routeParam("player", player.getName())
                        .routeParam("place", String.valueOf(nextPlace))
                        .asString();

                finishedPlayers.add(player.getName());

                nextPlace++;
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

            Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GREEN + player.getName() + " has arrived at " + station.getHumanReadableName() + "!");
            if (visitedCount + 1 == participatingStations.size()) {
                Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GOLD + player.getName() + " has visited all the required stations and is now returning to the terminal station!");
            } else if (visitedCount + 1 == (participatingStations.size() + 1) / 2) {
                Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GOLD + player.getName() + " has visited half the required stations!");
            }

            updateScoreboards();
        }
    }

    void updateScoreboards() {
        Player[] scoreboardPlayers = joinedPlayers.stream().map(player -> Race.getPlugin().getServer().getPlayer(player)).filter(Objects::nonNull).toArray(Player[]::new);
        ArrayList<String> zeroPlayers = new ArrayList<>(joinedPlayers);
        events.stream().filter(event -> event instanceof StationEvent)
                .collect(Collectors.groupingBy(event -> ((StationEvent) event).player, Collectors.counting()))
                .forEach((player, score) -> {
                    for (Player scoreboardPlayer : scoreboardPlayers) {
                        Objective objective = scoreboardPlayer.getScoreboard().getObjective("racecs");
                        objective.getScore(player).setScore(Math.toIntExact(score));
                    }

                    zeroPlayers.remove(player);
                });

        for (String player : zeroPlayers) {
            for (Player scoreboardPlayer : scoreboardPlayers) {
                Objective objective = scoreboardPlayer.getScoreboard().getObjective("racecs");
                objective.getScore(player).setScore(0);
            }
        }
    }

    public Station getTerminalStation() {
        return terminalStation;
    }

    public void setTerminalStation(Station terminalStation) throws TerminalStationConflictException {
        if (participatingStations.contains(terminalStation)) throw new TerminalStationConflictException();
        this.terminalStation = terminalStation;
    }
}
