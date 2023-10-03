package omg.lol.jplexer.race.session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kong.unirest.Unirest;
import omg.lol.jplexer.race.models.Station;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private final String id;
    private List<String> members;
    private List<String> membersReturning;

    public List<Station> getVisitedStations() {
        return visitedStations;
    }

    private List<Station> visitedStations;
    private String name;

    public Team(String name, String id, List<String> members) {
        this.name = name;
        this.id = id;
        this.members = members;
        this.membersReturning = new ArrayList<>(members);
        this.visitedStations = new ArrayList<>();
    }

    public List<String> getMembers() {
        return this.members;
    }

    public boolean hasPlayer(OfflinePlayer player) {
        return members.contains(player.getName());
    }

    public boolean pushStation(Station station) {
        if (visitedStations.contains(station)) return false;
        visitedStations.add(station);
        return true;
    }

    @Override
    public String toString() {
        return "Team members: " + members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

        var gson = new Gson();
        var obj = new JsonObject();
        obj.addProperty("name", name);

        Unirest.post("/teams/{team}/name")
                .routeParam("team", this.getId())
                .contentType("application/json")
                .body(gson.toJson(obj))
                .asString();
    }

    public void setMemberReturned(OfflinePlayer player) {
        membersReturning.remove(player.getName());
    }

    public boolean memberReturned(OfflinePlayer player) {
        return !membersReturning.contains(player.getName());
    }

    public int membersWaitingToReturn() {
        return membersReturning.size();
    }

    public String getId() {
        return id;
    }
}
