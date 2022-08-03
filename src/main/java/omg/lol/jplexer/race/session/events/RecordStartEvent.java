package omg.lol.jplexer.race.session.events;

import com.google.gson.JsonObject;

import java.util.Date;

public class RecordStartEvent implements SessionEvent {
    private final Date startDate;

    public RecordStartEvent(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public JsonObject build() {
        var obj = new JsonObject();
        obj.addProperty("type", "start");
        obj.addProperty("time", startDate.getTime());
        return obj;
    }
}
