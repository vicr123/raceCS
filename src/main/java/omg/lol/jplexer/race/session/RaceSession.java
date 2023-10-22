package omg.lol.jplexer.race.session;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.j256.ormlite.dao.Dao;
import kong.unirest.Unirest;
import omg.lol.jplexer.race.PlayerStationTracker;
import omg.lol.jplexer.race.Race;
import omg.lol.jplexer.race.models.Station;
import org.apache.commons.text.similarity.FuzzyScore;
import org.bukkit.*;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RaceSession implements Listener {
    private final ArrayList<String> joinedPlayers = new ArrayList<>();
    private ArrayList<Station> participatingStations = new ArrayList<>();
    private final ArrayList<String> finishedPlayers = new ArrayList<>();
    public final Map<String, LocalDateTime> lastPotionGiven = new HashMap<>();
    public final Map<String, LocalDateTime> penalties = new HashMap<>();
    public final Map<String, Location> respawnPoints = new HashMap<>();
    private Station terminalStation;
    private boolean isActive = true;
    private int nextPlace = 1;
    private final Scoreboard scoreboard;
    private final RaceSessionLogger logger;
    private RaceSessionTeams teams = null;

    static Set<Material> INVENTORY_WHITELIST = new HashSet<>();

    static {
        // Add your approved materials to the whitelist
        INVENTORY_WHITELIST.add(Material.MINECART);
        INVENTORY_WHITELIST.add(Material.BIRCH_BOAT);
        INVENTORY_WHITELIST.add(Material.OAK_BOAT);
        INVENTORY_WHITELIST.add(Material.ACACIA_BOAT);
        INVENTORY_WHITELIST.add(Material.JUNGLE_BOAT);
        INVENTORY_WHITELIST.add(Material.SPRUCE_BOAT);
        INVENTORY_WHITELIST.add(Material.DARK_OAK_BOAT);
        INVENTORY_WHITELIST.add(Material.DIAMOND_SWORD);
        INVENTORY_WHITELIST.add(Material.STONE_SWORD);
        INVENTORY_WHITELIST.add(Material.GOLDEN_SWORD);
        INVENTORY_WHITELIST.add(Material.IRON_SWORD);
        INVENTORY_WHITELIST.add(Material.NETHERITE_BOOTS);
    }

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

        setRespawnPoint(player);
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
            if (!Objects.equals(station, terminalStation) && !station.getId().equals("ACS") && !station.getId().equals("FCO")) participatingStations.add(station);
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
        player.setAllowFlight(false);
        penalties.remove(player.getName());
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

    public long isPlayerPenalised(OfflinePlayer player) {
        if (!penalties.containsKey(player.getName())) {
            return 0;
        }

        var penalty = penalties.get(player.getName());
        if (!LocalDateTime.now().isBefore(penalty)) {
            return 0;
        }

        return ChronoUnit.SECONDS.between(LocalDateTime.now(), penalty);
    }

    public void processStationArrived(OfflinePlayer player, Station station) {
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
                if (team.membersWaitingToReturn() == 0) return; // This team has already finished

                if (team.getVisitedStations().size() >= participatingStations.size()) {
                    team.setMemberReturned(player);
                    if (player instanceof Player onlinePlayer) {
                        onlinePlayer.setAllowFlight(true);
                    }

                    Unirest.post("/arrive/{player}/completion")
                            .routeParam("player", player.getName())
                            .asStringAsync();

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
                    .asStringAsync();

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

    void processPlayerCompletion(OfflinePlayer player) {
        Race.getPlugin().getServer().broadcastMessage(Race.CHAT_PREFIX + ChatColor.GOLD + player.getName() + " has finished as #" + nextPlace + "!");
        logger.appendPlayerFinished(player, nextPlace);

        if (player instanceof Player onlinePlayer) {
            onlinePlayer.setAllowFlight(true);
        }

        Unirest.post("/completion/{player}/{place}")
                .routeParam("player", player.getName())
                .routeParam("place", String.valueOf(nextPlace))
                .asStringAsync();

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
                .asStringAsync();

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

    private boolean pushIntoInventory(Player player, ItemStack item) {
        PlayerInventory inventory = player.getInventory();

        boolean addedSuccessfully = false;
//        for(int i = 0; i < 9; i++) { // Hotbar is slots 0 to 8 in the PlayerInventory
//            var current = inventory.getItem(i);
//
//            if (current != null && current.getType() != Material.AIR) {
//                continue;
//            }
//
//            inventory.setItem(i, item);
//            addedSuccessfully = true;
//            break;
//        }

        // If not added to hotbar, add it to the main inventory
        if (!addedSuccessfully) {
            var remaining = inventory.addItem(item);
             addedSuccessfully = remaining.isEmpty();
        }
        return addedSuccessfully;
    }

    public void issuePotion(Player player) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastRun = lastPotionGiven.getOrDefault(player.getName(), now.minusMinutes(5));
        long minutesSinceLastRun = ChronoUnit.MINUTES.between(lastRun, now);

        if (minutesSinceLastRun >= 5) {
            lastPotionGiven.put(player.getName(), now);

            var itemStack = new ItemStack(Material.POTION);
            var meta = (PotionMeta) itemStack.getItemMeta();

            // Dice roll!
            if (Math.random() < 0.3) {
                meta.setBasePotionData(new PotionData(PotionType.WATER, false, false));
            } else {
                if (Math.random() < 0.05) {
                    // Super potion!!!
                    meta.setBasePotionData(new PotionData(PotionType.SPEED, false, true));
                } else {
                    // Standard potion
                    meta.setBasePotionData(new PotionData(PotionType.SPEED, false, false));
                }
            }
            itemStack.setItemMeta(meta);

            if (pushIntoInventory(player, itemStack)) {
                player.sendMessage(ChatColor.GREEN + "Thank you for choosing FrivoloCo Chocolates! Enjoy your treat and have a fantastic day. We look forward to serving you again soon!");
            } else {
                player.sendMessage(ChatColor.RED + "Sorry, your inventory is full.");
            }
        } else {
            long minutesToWait = 5 - minutesSinceLastRun;
            player.sendMessage(ChatColor.RED + "You can only receive a potion every 5 minutes. You can receive another one in %d minutes.".formatted(minutesToWait));
        }

    }

    public void playersCollided(Player p1, Player p2) {
        logger.appendCollision(p1, p2);
    }

    private void setRespawnPoint(Player player) {
        respawnPoints.put(player.getName(), player.getLocation());
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        if (!isActive) return;
        if (event.getEntity() instanceof Player player) {
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

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!isActive) return;
        // The player who sent the message
        Player player = event.getPlayer();
        if (!joinedPlayers.contains(player.getName())) return; //This player is not in the race

        // The chat message
        String message = event.getMessage();

        FuzzyScore fs = new FuzzyScore(Locale.getDefault());
        String desired = "i want 2 order";
        int score = fs.fuzzyScore(desired, message.toLowerCase());

        // Do something with these...
        if (score > 10)
            Race.getPlugin().getServer().getScheduler().runTaskLater(Race.getPlugin(), () -> {
                if (Race.getPlugin().getStationTracker().isInStation(player, "FCO")) {
                    issuePotion(player);
                } else {
                    player.sendMessage(ChatColor.RED + "*crickets chirp* - no one could hear your order. Please visit a FrivoloCo Chocolates location in order to receive your free coffee!");
                }
            }, 5);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        // The player who dropped the item
        var player = event.getPlayer();
        if (!joinedPlayers.contains(player.getName())) return; //This player is not in the race

        // The item that was dropped
        var item = event.getItemDrop().getItemStack();

        // Do something with these...
        if (item.getItemMeta() instanceof PotionMeta) {
            event.getPlayer().sendMessage("Sorry, you can't drop potions during an AirCS race.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDrinkPotion(PlayerItemConsumeEvent event) {
        var player = event.getPlayer();
        if (!joinedPlayers.contains(player.getName())) return; //This player is not in the race

        var item = event.getItem();

        if (item.getType() == Material.POTION) {
            player.getInventory().removeItem(item);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!isActive) return;
        var player = event.getPlayer();
        if (!joinedPlayers.contains(player.getName())) return; //This player is not in the race

        // Find the distance between the player and Central station
        var loc = player.getLocation().clone();
        if (loc.getWorld().getEnvironment() == World.Environment.NETHER) {
            loc.setX(loc.getX() * 8);
            loc.setZ(loc.getZ() * 8);
        }

        var distance = loc.distance(new Location(loc.getWorld(), 0, loc.getY(), 0));
        var penalty = 25 * Math.pow(distance, 1.0/3.0);
        if (penalty < 0) penalty = 0;

        this.penalties.put(player.getName(), LocalDateTime.now().plusSeconds((long) penalty));
    }

    @EventHandler
    public void OnEntityDamage(EntityDamageEvent event) {
        if (!isActive) return;
        if (event.getEntity() instanceof Villager vil && vil.getCustomName() != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isActive) return;
        if (event.getEntity() instanceof Villager vil && vil.getCustomName() != null && event.getDamager() instanceof Player player) {
            player.playEffect(EntityEffect.HURT);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!respawnPoints.containsKey(player.getName())) {
            return;
        }

        Location respawnLocation = respawnPoints.get(player.getName());

        // Schedule a task to run on the next tick after the death.
        // This is necessary because calling player.spigot().respawn()
        // inside the death event doesn't work.
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Race.getPlugin(), () -> {
            player.spigot().respawn();
            player.teleport(respawnLocation);

            player.sendMessage(ChatColor.RED + "You died!" + ChatColor.RESET + " Respawning you at the last station you entered.");
        });
    }
}
