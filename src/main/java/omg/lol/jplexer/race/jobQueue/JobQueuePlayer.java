package omg.lol.jplexer.race.jobQueue;

import omg.lol.jplexer.race.models.Station;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class JobQueuePlayer {
    private final String name;
    private final Supplier<Station> currentStation;
    private final Location location;

    public JobQueuePlayer(Player player, Supplier<Station> currentStation) {
        name = player.getName();
        this.currentStation = currentStation;

        Location playerLocation;
        playerLocation = player.getLocation().getBlock().getLocation();  // Ignore sub-block movements
        if (player.getVehicle() != null) {
            playerLocation = player.getVehicle().getLocation().getBlock().getLocation().add(0, 1, 0);
        }
        location = playerLocation;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public Station getCurrentStation() {
        return currentStation.get();
    }
}
