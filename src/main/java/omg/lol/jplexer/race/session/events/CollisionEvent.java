package omg.lol.jplexer.race.session.events;

import com.google.gson.JsonObject;

import java.util.Date;
import java.util.UUID;

public class CollisionEvent implements SessionEvent {
    private final Date date;
    private final UUID player1uuid;
    private final UUID player2uuid;

    public CollisionEvent(UUID player1name, UUID player2uuid) {
        this.player1uuid = player1name;
        this.player2uuid = player2uuid;
        date = new Date(System.currentTimeMillis());
    }

    @Override
    public JsonObject build() {
        var obj = new JsonObject();
        obj.addProperty("type", "collision");
        obj.addProperty("time", date.getTime());
        obj.addProperty("p1", player1uuid.toString());
        obj.addProperty("p2", player2uuid.toString());
        return obj;
    }
}
