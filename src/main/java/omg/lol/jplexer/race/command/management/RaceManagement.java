package omg.lol.jplexer.race.command.management;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.j256.ormlite.dao.Dao;
import net.md_5.bungee.api.ChatColor;
import omg.lol.jplexer.race.CommandUtils;
import omg.lol.jplexer.race.Race;
import omg.lol.jplexer.race.session.RaceSession;
import omg.lol.jplexer.race.command.RaceCompleter;
import omg.lol.jplexer.race.models.Station;
import omg.lol.jplexer.race.session.TeamOrganizationException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static omg.lol.jplexer.race.Race.CHAT_PREFIX;

public class RaceManagement {
    public static void RaceManagementCommand(CommandSender sender, String[] args) {
        String verb = args.length == 0 ? "help" : args[0];
        switch (verb) {
            case "help":
                sender.sendMessage("Help will be provided.");
                break;
            case "register":
                if (args.length == 1) {
                    registerSelf(sender);
                } else {
                    registerPlayer(sender, args[1]);
                }
                break;
            case "deregister":
                if (args.length == 1) {
                    deregisterSelf(sender);
                } else {
                    deregisterPlayer(sender, args[1]);
                }
                break;
            case "stations":
                showStations(sender);
                break;
            case "addstation":
                addStation(sender, args[1]);
                break;
            case "addstations":
                addStations(sender, args[1]);
                break;
            case "removestation":
                removeStation(sender, args[1]);
                break;
            case "clearstations":
                clearStations(sender);
                break;
            case "setterminalstation":
                setTerminalStation(sender, args[1]);
                break;
            case "sync":
                syncPulse(sender);
                break;
            case "teamup":
                teamUp(sender);
                break;
            case "setteamname":
                setTeamName(sender, args[1]);
                break;
            case "credit":
                if (args.length == 3) {
                    creditStation(sender, args[1], args[2]);
                } else {
                    sender.sendMessage("Invalid arguments to credit command.");
                }
                break;
            case "close":
                closeRace(sender);
                break;
        }
    }

    private static void addStations(CommandSender sender, String stations) {
        var gson = new Gson();
        var json = gson.fromJson(stations, JsonArray.class);
        for (var station : json) {
            addStation(sender, station.getAsString());
        }
    }

    private static void setTeamName(CommandSender sender, String name) {
        RaceSession currentRace = Race.getPlugin().getCurrentRace();
        if (currentRace == null || currentRace.isEnded()) {
            sender.sendMessage("There is no ongoing race.");
            return;
        }

        if (currentRace.getTeams() == null) {
            sender.sendMessage("Teams have not yet been created.");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Can't run this from the console.");
            return;
        }

        try {
            currentRace.getTeams().teamFor((Player) sender).setName(name);
            sender.sendMessage("Your team has been renamed.");
        } catch (NoSuchElementException ex) {
            sender.sendMessage("You are not in the race");
        }
    }

    private static void teamUp(CommandSender sender) {
        if (!sender.hasPermission("racecs.manage")) {
            sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
            return;
        }

        RaceSession currentRace = Race.getPlugin().getCurrentRace();
        if (currentRace == null || currentRace.isEnded()) {
            sender.sendMessage("There is no ongoing race.");
            return;
        }

        if (currentRace.getTeams() != null) {
            sender.sendMessage("The teams have already been chosen.");
            return;
        }

        try {
            currentRace.teamUp();
        } catch (TeamOrganizationException ex) {
            sender.sendMessage("With the current players, an optimal teaming configuration cannot be created. The reason is: " + ex.getMessage());
        }
    }

    public static List<String> TabCompleteCommand(String[] args) {
        final RaceSession raceSession = Race.getPlugin().getCurrentRace();
        if (args.length == 0) {
            if (raceSession == null || raceSession.isEnded()) return Arrays.asList("help", "register");
            return Arrays.asList("help", "register", "deregister", "stations", "addstation", "addstations", "removestation", "close", "setterminalstation", "credit", "teamup", "setteamname");
        } else {
            Dao<Station, String> stationDao = Race.getPlugin().getStationDao();

            ArrayList<Station> stations = new ArrayList<>();
            stationDao.forEach(stations::add);

            switch (args[0]) {
                case "help":
                case "register":
                case "deregister":
                case "stations":
                case "clearstations":
                case "teamup":
                case "setteamname":
                    return null;
                case "addstation":
                case "setterminalstation":
                    if (raceSession == null || raceSession.isEnded()) return null;
                    return RaceCompleter.completeList(Arrays.copyOfRange(args, 1, args.length), stations.stream().filter(station -> !raceSession.getParticipatingStations().contains(station)).map(Station::getId).toArray(String[]::new));
                case "removestation":
                    if (raceSession == null || raceSession.isEnded()) return null;
                    return RaceCompleter.completeList(Arrays.copyOfRange(args, 1, args.length), stations.stream().filter(station -> raceSession.getParticipatingStations().contains(station)).map(Station::getId).toArray(String[]::new));
                case "credit":
                    if (raceSession == null || raceSession.isEnded()) return null;
                    if (args.length == 1) return RaceCompleter.completeList(Arrays.copyOfRange(args, 1, args.length), stations.stream().filter(station -> raceSession.getParticipatingStations().contains(station)).map(Station::getId).toArray(String[]::new));
                    return null;
                case "addstations":
                    return List.of("<json stations>");
                default:
                    if (raceSession == null || raceSession.isEnded()) return Arrays.asList("help", "register");
                    return Arrays.asList("help", "register", "deregister", "stations", "addstation", "addstations", "removestation", "clearstations", "close", "setterminalstation", "credit", "teamup", "setteamname");
            }
        }
    }


    static void registerSelf(CommandSender sender) {
        registerPlayer(sender, sender.getName());
    }

    static void deregisterSelf(CommandSender sender) {
        deregisterPlayer(sender, sender.getName());
    }

    static void registerPlayer(CommandSender sender, String player) {
        RaceSession currentRace = Race.getPlugin().getCurrentRace();
        if (currentRace != null && !currentRace.isEnded()) {
            if (currentRace.getTeams() != null) {
                sender.sendMessage("The current race is closed to new players.");
                return;
            }
        }

        Entity[] targets = CommandUtils.getTargets(sender, player);
        int failCount = 0;
        for (Entity target : targets) {
            if (!(target instanceof Player)) {
                failCount++;
                continue;
            }

            if (!sender.hasPermission("racecs.manage") && target != sender) {
                sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
                return;
            }

            RaceSession raceSession = Race.getPlugin().getCurrentRace();
            if (raceSession == null || raceSession.isEnded()) {
                Race.getPlugin().createNewRace();
                raceSession = Race.getPlugin().getCurrentRace();
            }
            raceSession.addPlayer((Player) target);

            sender.sendMessage(CHAT_PREFIX + ChatColor.GREEN + target.getName() + " has been added to the race");
            target.sendMessage(CHAT_PREFIX + ChatColor.GREEN + "You have been added to the race");
        }

        if (failCount > 0) sender.sendMessage(CHAT_PREFIX + failCount + " targets could not be registered.");
    }

    static void deregisterPlayer(CommandSender sender, String player) {
        RaceSession currentRace = Race.getPlugin().getCurrentRace();
        if (currentRace == null || currentRace.isEnded()) {
            sender.sendMessage("There is no ongoing race.");
            return;
        }

        Entity[] targets = CommandUtils.getTargets(sender, player);
        int failCount = 0;
        for (Entity target : targets) {
            if (!(target instanceof Player)) {
                failCount++;
                continue;
            }

            if (!sender.hasPermission("racecs.manage") && target != sender) {
                sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
                return;
            }

            Race.getPlugin().getCurrentRace().removePlayer((Player) target);
            target.sendMessage(CHAT_PREFIX + ChatColor.GREEN + "You have been removed from the race");
            sender.sendMessage(CHAT_PREFIX + ChatColor.GREEN + target.getName() + " has been removed from the race!");
        }

        if (failCount > 0) sender.sendMessage(CHAT_PREFIX + failCount + " targets could not be registered.");
    }

    static void closeRace(CommandSender sender) {
        if (!sender.hasPermission("racecs.manage")) {
            sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
            return;
        }

        RaceSession currentRace = Race.getPlugin().getCurrentRace();
        if (currentRace == null || currentRace.isEnded()) {
            sender.sendMessage("There is no ongoing race.");
            return;
        }

        Race.getPlugin().getCurrentRace().endSession();
        sender.sendMessage("The race session has been ended");
    }

    static void showStations(CommandSender sender) {
        RaceSession currentRace = Race.getPlugin().getCurrentRace();
        if (currentRace == null || currentRace.isEnded()) {
            sender.sendMessage("There is no ongoing race.");
            return;
        }

        sender.sendMessage("The current race contains the following stations:");
        currentRace.getParticipatingStations().forEach(station -> sender.sendMessage(station.getHumanReadableName()));
        sender.sendMessage("The terminal station is: " + currentRace.getTerminalStation().getHumanReadableName());
    }

    static void addStation(CommandSender sender, String station) {
        if (!sender.hasPermission("racecs.manage")) {
            sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
            return;
        }

        RaceSession currentRace = Race.getPlugin().getCurrentRace();
        if (currentRace == null || currentRace.isEnded()) {
            sender.sendMessage("There is no ongoing race.");
            return;
        }

        Dao<Station, String> stationDao = Race.getPlugin().getStationDao();

        try {
            Station stationObject = stationDao.queryForId(station.toUpperCase());
            if (stationObject == null) {
                sender.sendMessage("Sorry, that's not a valid station.");
                return;
            }

            currentRace.addParticipatingStation(stationObject);
            sender.sendMessage(stationObject.getHumanReadableName() + " was added to the race.");
        } catch (SQLException e) {
            sender.sendMessage("Couldn't add that station");
        } catch (RaceSession.DuplicateStationException e) {
            sender.sendMessage("That station has already been added.");
        } catch (RaceSession.TerminalStationConflictException e) {
            sender.sendMessage("Sorry, that station can't be added as it is the terminal station for the race.");
        }
    }

    static void removeStation(CommandSender sender, String station) {
        if (!sender.hasPermission("racecs.manage")) {
            sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
            return;
        }

        RaceSession currentRace = Race.getPlugin().getCurrentRace();
        if (currentRace == null || currentRace.isEnded()) {
            sender.sendMessage("There is no ongoing race.");
            return;
        }

        Dao<Station, String> stationDao = Race.getPlugin().getStationDao();

        try {
            Station stationObject = stationDao.queryForId(station.toUpperCase());
            if (stationObject == null) {
                sender.sendMessage("Sorry, that's not a valid station.");
                return;
            }

            currentRace.removeParticipatingStation(stationObject);
            sender.sendMessage(stationObject.getHumanReadableName() + " was removed from the race.");
        } catch (SQLException e) {
            sender.sendMessage("Couldn't remove that station");
        }
    }

    static void clearStations(CommandSender sender) {
        if (!sender.hasPermission("racecs.manage")) {
            sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
            return;
        }

        RaceSession currentRace = Race.getPlugin().getCurrentRace();
        if (currentRace == null || currentRace.isEnded()) {
            sender.sendMessage("There is no ongoing race.");
            return;
        }

        currentRace.removeAllParticipatingStations();
        sender.sendMessage("All participating stations were removed from the race.");
    }

    private static void setTerminalStation(CommandSender sender, String station) {
        if (!sender.hasPermission("racecs.manage")) {
            sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
            return;
        }

        RaceSession currentRace = Race.getPlugin().getCurrentRace();
        if (currentRace == null || currentRace.isEnded()) {
            sender.sendMessage("There is no ongoing race.");
            return;
        }

        Dao<Station, String> stationDao = Race.getPlugin().getStationDao();

        try {
            Station stationObject = stationDao.queryForId(station.toUpperCase());
            if (stationObject == null) {
                sender.sendMessage("Sorry, that's not a valid station.");
                return;
            }

            currentRace.setTerminalStation(stationObject);
            sender.sendMessage(stationObject.getHumanReadableName() + " was set as the terminal station.");
        } catch (SQLException e) {
            sender.sendMessage("Couldn't add that station");
        } catch (RaceSession.TerminalStationConflictException e) {
            sender.sendMessage("Sorry, that station can't be set as the terminal station as it is included in the race.");
        }
    }

    private static void creditStation(CommandSender sender, String station, String player) {
        if (!sender.hasPermission("racecs.manage")) {
            sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
            return;
        }

        RaceSession currentRace = Race.getPlugin().getCurrentRace();
        if (currentRace == null || currentRace.isEnded()) {
            sender.sendMessage("There is no ongoing race.");
            return;
        }

        Dao<Station, String> stationDao = Race.getPlugin().getStationDao();

        try {
            Station stationObject = stationDao.queryForId(station.toUpperCase());
            if (stationObject == null) {
                sender.sendMessage("Sorry, that's not a valid station.");
                return;
            }

//            Entity[] targets = CommandUtils.getTargets(sender, player);
            var offlinePlayer = Bukkit.getOfflinePlayer(player);
            int failCount = 0;
//            for (Entity target : targets) {
//                if (!(target instanceof Player)) {
//                    failCount++;
//                    continue;
//                }

                var penalty = currentRace.isPlayerPenalised(offlinePlayer);
                if (penalty > 0) {
                    sender.sendMessage("Unable to credit %s with the station %s because they have a remaining penalty of %d seconds.".formatted(offlinePlayer.getName(), stationObject.getHumanReadableName(), penalty));
                    failCount++;
                } else {
                    currentRace.processStationArrived(offlinePlayer, stationObject);
                    sender.sendMessage(offlinePlayer.getName() + " has been credited for arriving at " + stationObject.getHumanReadableName() + ".");
                }

//            }
//            if (failCount > 0) sender.sendMessage(failCount + " targets could not be credited.");
        } catch (SQLException e) {
            sender.sendMessage("Couldn't add that station");
        }
    }

    private static void syncPulse(CommandSender sender) {
        if (!sender.hasPermission("racecs.manage")) {
            sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
            return;
        }

        RaceSession currentRace = Race.getPlugin().getCurrentRace();
        if (currentRace == null || currentRace.isEnded()) {
            sender.sendMessage("There is no ongoing race.");
            return;
        }

        currentRace.syncPulse();
    }
}
