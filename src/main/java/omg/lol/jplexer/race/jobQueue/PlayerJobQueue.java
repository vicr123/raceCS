package omg.lol.jplexer.race.jobQueue;

import com.google.common.base.Objects;
import com.j256.ormlite.dao.Dao;
import omg.lol.jplexer.race.PlayerStationTracker;
import omg.lol.jplexer.race.Race;
import omg.lol.jplexer.race.models.Region;
import omg.lol.jplexer.race.models.Station;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerJobQueue extends BukkitRunnable {
    private final ConcurrentLinkedQueue<JobQueuePlayer> queue = new ConcurrentLinkedQueue<>();
    private final Set<String> queuedPlayerNames = ConcurrentHashMap.newKeySet();
    private final PlayerStationTracker tracker;
    private final ExecutorService executorService;
    private final Object lock = new Object();
    private final HashMap<JobQueuePlayer, Location> playerLastLocations = new HashMap<>();

    public PlayerJobQueue(PlayerStationTracker tracker, int threadPoolSize) {
        this.tracker = tracker;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);

        this.runTaskTimer(Race.getPlugin(), 0L, 1L);
    }

    public void enqueuePlayer(JobQueuePlayer player) {
        if (queuedPlayerNames.add(player.getName())) {
            queue.add(player);
        }
    }

    @Override
    public void run() {
        JobQueuePlayer player = queue.poll();
        if (player != null) {
            queuedPlayerNames.remove(player.getName());
            executorService.execute(() -> processPlayer(player));
        }
    }

    private void processPlayer(JobQueuePlayer player) {
        synchronized (lock) {
            // Check for position change
            var previousLocation = playerLastLocations.getOrDefault(player, null);
            var currentLocation = player.getLocation();

            if (previousLocation != null && previousLocation.equals(currentLocation)) {
                return; // The player's integer position has not changed, do not proceed further
            }

            // Save current position as last known position
            playerLastLocations.put(player, currentLocation);

            Dao<Region, Long> regionDao = Race.getPlugin().getRegionDao();

            Station currentStation = player.getCurrentStation();
            ArrayList<Station> inStations = new ArrayList<>();
            regionDao.forEach(region -> {
                if (region.inRegion(player.getLocation())) inStations.add(region.getStation());
            });

            Station newStation;
            if (!inStations.isEmpty()) {
                if (inStations.contains(currentStation)) {
                    newStation = currentStation;
                } else {
                    newStation = inStations.get(0);
                }
            } else {
                newStation = null;
            }

            if (!Objects.equal(newStation, currentStation)) {
                Race.getPlugin().getServer().getScheduler().runTask(Race.getPlugin(), () -> {
                    tracker.triggerPlayerStationChange(player.getName(), newStation);
                });
            }
        }
    }
}