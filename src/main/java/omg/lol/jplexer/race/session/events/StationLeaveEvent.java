package omg.lol.jplexer.race.session.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import omg.lol.jplexer.race.Race;
import omg.lol.jplexer.race.models.Station;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class StationLeaveEvent implements SessionEvent {
    private final Date date;
    private final UUID playerUuid;
    private final Station station;
    private Station possibleTarget;

    private static final HashMap<UUID, Station> lastStations = new HashMap<>();

    public StationLeaveEvent(Player player, Station station) {
        this.playerUuid = player.getUniqueId();
        this.station = station;

        lastStations.put(player.getUniqueId(), station);

        this.possibleTarget = null;
        date = new Date(System.currentTimeMillis());
    }

    public void playerArrivedAtStation(OfflinePlayer player, Station station) {
        if (this.possibleTarget == null && player.getUniqueId() == playerUuid && (this.station == null || !station.getId().equals(this.station.getId()))) this.possibleTarget = station;
    }

    public void playerCollision(Player player1, Player player2) {
        if (this.possibleTarget == null && (player1.getUniqueId() == playerUuid || player2.getUniqueId() == playerUuid)) {
            var otherPlayer = player1.getUniqueId() == playerUuid ? player2.getUniqueId() : player1.getUniqueId();
            this.possibleTarget = lastStations.get(otherPlayer);

            //Swap stations!
            Bukkit.getScheduler().runTaskLater(Race.getPlugin(), () -> {
                lastStations.put(playerUuid, this.possibleTarget);
            }, 1);
        }
    }

    @Override
    public JsonObject build() {
        var obj = new JsonObject();
        obj.addProperty("type", "leave");
        obj.addProperty("time", date.getTime());
        obj.addProperty("player", playerUuid.toString());
        obj.add("station", station == null ? JsonNull.INSTANCE : new JsonPrimitive(station.getId()));
        obj.add("possibleTarget", possibleTarget == null ? JsonNull.INSTANCE : new JsonPrimitive(possibleTarget.getId()));
        return obj;
    }
}
