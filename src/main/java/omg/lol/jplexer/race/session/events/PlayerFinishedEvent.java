package omg.lol.jplexer.race.session.events;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class PlayerFinishedEvent implements SessionEvent {
    private final Date date;
    private final int place;
    private final UUID playerUuid;

    public PlayerFinishedEvent(Player player, int place) {
        this.playerUuid = player.getUniqueId();
        this.place = place;
        date = new Date(System.currentTimeMillis());
    }

    @Override
    public JsonObject build() {
        var obj = new JsonObject();
        obj.addProperty("type", "finished");
        obj.addProperty("time", date.getTime());
        obj.addProperty("player", playerUuid.toString());
        obj.addProperty("place", place);
        return obj;
    }
}
