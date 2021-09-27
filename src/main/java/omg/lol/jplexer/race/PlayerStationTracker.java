package omg.lol.jplexer.race;

import com.google.common.base.Objects;
import com.j256.ormlite.dao.Dao;
import omg.lol.jplexer.race.models.Region;
import omg.lol.jplexer.race.models.Station;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerStationTracker implements Listener {
    interface PlayerStationChangeListener {
        void onPlayerStationChange(Player player, Station station);
    }

    HashMap<Player, Station> currentStations = new HashMap<>();
    ArrayList<PlayerStationChangeListener> stationChangeListeners = new ArrayList<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Dao<Region, Long> regionDao = Race.getPlugin().getRegionDao();
        if (!currentStations.containsKey(event.getPlayer())) currentStations.put(event.getPlayer(), null);

        Station currentStation = currentStations.get(event.getPlayer());
        ArrayList<Station> inStations = new ArrayList<>();
        regionDao.forEach(region -> {
            if (region.inRegion(event.getPlayer().getLocation())) inStations.add(region.getStation());
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
            currentStations.put(event.getPlayer(), newStation);
            for (PlayerStationChangeListener listener : stationChangeListeners) listener.onPlayerStationChange(event.getPlayer(), newStation);
//            event.getPlayer().sendMessage("You have moved to " + (newStation == null ? "the wilderness" : newStation.getHumanReadableName()));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        currentStations.remove(event.getPlayer());
        for (PlayerStationChangeListener listener : stationChangeListeners) listener.onPlayerStationChange(event.getPlayer(), null);
    }

    public void addStationChangeListener(PlayerStationChangeListener listener) {
        stationChangeListeners.add(listener);
    }

    public void removeStationChangeListener(PlayerStationChangeListener listener) {
        stationChangeListeners.remove(listener);
    }
}
