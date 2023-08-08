package omg.lol.jplexer.race.session;

import org.bukkit.entity.Player;

import java.util.List;

public class Team {
    private List<String> members;
    private String name;

    public Team(String name, List<String> members) {
        this.name = name;
        this.members = members;
    }

    public List<String> getMembers() {
        return this.members;
    }

    public boolean hasPlayer(Player player) {
        return members.contains(player.getName());
    }

    @Override
    public String toString() {
        return "Team members: " + members;
    }
}
