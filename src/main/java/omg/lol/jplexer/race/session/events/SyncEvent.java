package omg.lol.jplexer.race.session.events;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Date;

public class SyncEvent implements SessionEvent {
    private final Date date;

    public SyncEvent() {
        date = new Date(System.currentTimeMillis());
    }

    @Override
    public JsonObject build() {
        var obj = new JsonObject();
        obj.addProperty("type", "sync");
        obj.addProperty("time", date.getTime());
        return obj;
    }
}
