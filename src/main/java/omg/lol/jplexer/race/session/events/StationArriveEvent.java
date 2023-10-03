package omg.lol.jplexer.race.session.events;

import com.google.gson.JsonObject;
import omg.lol.jplexer.race.models.Station;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class StationArriveEvent implements SessionEvent {
    private final Date date;
    private final UUID playerUuid;
    private final Station station;
    
    public StationArriveEvent(OfflinePlayer player, Station station) {
        playerUuid = player.getUniqueId();
        this.station = station;
        date = new Date(System.currentTimeMillis());
    }

    @Override
    public JsonObject build() {
        var obj = new JsonObject();
        obj.addProperty("type", "arrival");
        obj.addProperty("time", date.getTime());
        obj.addProperty("player", playerUuid.toString());
        obj.addProperty("station", station.getId());
        return obj;
    }
}
