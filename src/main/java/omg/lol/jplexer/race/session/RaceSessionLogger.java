package omg.lol.jplexer.race.session;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import omg.lol.jplexer.race.models.Station;
import omg.lol.jplexer.race.session.events.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class RaceSessionLogger {
    private final List<SessionEvent> events;
    private final List<StationLeaveEvent> leaveEvents;
    private final JsonObject playerNames;
    private final String filename;
    private final HashMap<UUID, Station> lastStation;

    public RaceSessionLogger() {
        events = new LinkedList<>();
        leaveEvents = new LinkedList<>();
        lastStation = new HashMap<>();
        playerNames = new JsonObject();

        var date = new Date(System.currentTimeMillis());
        filename = "race-" + new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss").format(date) + ".json";

        events.add(new RecordStartEvent(date));
        this.write();
    }

    public void playerRegistered(Player player) {
        playerNames.addProperty(player.getUniqueId().toString(), player.getName());
        this.write();
    }

    public void appendCollision(Player p1, Player p2) {
        events.add(new CollisionEvent(p1.getUniqueId(), p2.getUniqueId()));
        for (var event : leaveEvents) event.playerCollision(p1, p2);
        this.write();
    }

    public void appendStationArrive(OfflinePlayer player, Station station) {
        lastStation.put(player.getUniqueId(), station);
        events.add(new StationArriveEvent(player, station));
        for (var event : leaveEvents) event.playerArrivedAtStation(player, station);
        this.write();
    }

    public void appendStationLeave(Player player) {
        var event = new StationLeaveEvent(player, lastStation.getOrDefault(player.getUniqueId(), null));
        events.add(event);
        leaveEvents.add(event);
        this.write();
    }

    public void appendPlayerFinished(OfflinePlayer player, int place) {
        events.add(new PlayerFinishedEvent(player, place));
        this.write();
    }

    public void write() {
        try {
            File dir = new File("racecs-races/");
            dir.mkdirs();

            var gson = new Gson();
            var writer = new FileWriter("racecs-races/" + filename);

            var root = new JsonObject();
            root.add("events", events.stream().map(SessionEvent::build).collect(new Collector<JsonObject, JsonArray, JsonArray>() {
                @Override
                public Supplier<JsonArray> supplier() {
                    return JsonArray::new;
                }

                @Override
                public BiConsumer<JsonArray, JsonObject> accumulator() {
                    return JsonArray::add;
                }

                @Override
                public BinaryOperator<JsonArray> combiner() {
                    return (jsonElements, jsonElements2) -> {
                        jsonElements.addAll(jsonElements2);
                        return jsonElements;
                    };
                }

                @Override
                public Function<JsonArray, JsonArray> finisher() {
                    return array -> array;
                }

                @Override
                public Set<Characteristics> characteristics() {
                    return Sets.immutableEnumSet(Characteristics.CONCURRENT);
                }
            }));
            root.add("players", playerNames);
            writer.write(gson.toJson(root));
            writer.close();
        } catch (IOException ignored) {

        }
    }

    public void appendSyncPulse() {
        events.add(new SyncEvent());
    }
}
