package omg.lol.jplexer.race;

import com.google.common.base.Objects;
import com.j256.ormlite.dao.Dao;
import omg.lol.jplexer.race.models.Region;
import omg.lol.jplexer.race.models.Station;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerStationTracker implements Listener {
    public interface PlayerStationChangeListener {
        void onPlayerStationChange(Player player, Station station);
    }

    HashMap<Player, Station> currentStations = new HashMap<>();
    ArrayList<PlayerStationChangeListener> stationChangeListeners = new ArrayList<>();
    ArrayList<String> playerTracking = new ArrayList<>();
    private final HashMap<Player, Location> playerLastLocations = new HashMap<>();

    PlayerStationTracker() {
        addStationChangeListener((player, station) -> {
            if (playerTracking.contains(player.getName())) {
                if (station == null) {
                    player.sendMessage("You have left the station");
                } else {
                    player.sendMessage("You have moved into " + station.getHumanReadableName());
                }
            }
        });
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        event.getVehicle().getPassengers().stream()
                .filter(passenger -> passenger instanceof Player)
                .map(passenger -> (Player) passenger)
                .forEach(this::handlePlayerMove);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        handlePlayerMove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        currentStations.remove(event.getPlayer());
        playerTracking.remove(event.getPlayer().getName());
        for (PlayerStationChangeListener listener : stationChangeListeners) listener.onPlayerStationChange(event.getPlayer(), null);
    }

    private void handlePlayerMove(Player player) {
        // Check for position change
        var previousLocation = playerLastLocations.getOrDefault(player, null);
        var currentLocation = player.getLocation().getBlock().getLocation();  // Ignore sub-block movements

        if (previousLocation != null && previousLocation.equals(currentLocation)) {
            return; // The player's integer position has not changed, do not proceed further
        }

        // Save current position as last known position
        playerLastLocations.put(player, currentLocation);

        Dao<Region, Long> regionDao = Race.getPlugin().getRegionDao();
        if (!currentStations.containsKey(player)) currentStations.put(player, null);

        Station currentStation = currentStations.get(player);
        ArrayList<Station> inStations = new ArrayList<>();
        regionDao.forEach(region -> {
            if (region.inRegion(player.getLocation())) inStations.add(region.getStation());
        });

        Station newStation = null;
        if (!inStations.isEmpty()) {
            if (inStations.contains(currentStation)) {
                newStation = currentStation;
            } else {
                newStation = inStations.get(0);
            }
        }

        if (!Objects.equal(newStation, currentStation)) {
            currentStations.put(player, newStation);
            for (PlayerStationChangeListener listener : stationChangeListeners) listener.onPlayerStationChange(player, newStation);
        }
    }

    public boolean isInStation(Player player) {
        return currentStations.getOrDefault(player, null) != null;
    }

    public void addStationChangeListener(PlayerStationChangeListener listener) {
        stationChangeListeners.add(listener);
    }

    public void removeStationChangeListener(PlayerStationChangeListener listener) {
        stationChangeListeners.remove(listener);
    }

    public void TogglePlayerTracking(Player player) {
        if (playerTracking.contains(player.getName())) {
            playerTracking.remove(player.getName());
            player.sendMessage("You will no longer be told as you move between stations.");
        } else {
            playerTracking.add(player.getName());
            player.sendMessage("You will be told as you move between stations.");
        }
    }
}
