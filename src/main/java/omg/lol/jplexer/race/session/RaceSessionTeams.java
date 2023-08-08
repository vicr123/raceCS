package omg.lol.jplexer.race.session;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RaceSessionTeams {
    private final RaceSession raceSession;
    private final List<Team> teams;

    public RaceSessionTeams(RaceSession raceSession, ArrayList<String> joinedPlayers) throws TeamOrganizationException {
        this.raceSession = raceSession;
        teams = teamUpPlayers(joinedPlayers);

        for (var team : teams) {

        }
    }

    public Team teamFor(Player player) {
        return teams.stream().filter(x -> x.hasPlayer(player)).findFirst().orElseThrow();
    }

    public List<Team> teamUpPlayers(ArrayList<String> joinedPlayers) throws TeamOrganizationException {
        int totalPlayers = joinedPlayers.size();
        List<Team> teams = new ArrayList<>();

        if (totalPlayers < 2) {
            throw new TeamOrganizationException("Not enough players to form a team.");
        }

        int optimalTeamSize = 0;

        // Determining the optimal team size
        if(totalPlayers % 4 == 0) {
            optimalTeamSize = totalPlayers / 4;
        }
        else if (totalPlayers % 3 == 0) {
            optimalTeamSize = totalPlayers / 3;
        }
        else if (totalPlayers % 2 == 0) {
            optimalTeamSize = totalPlayers / 2;
        }
        else {
            throw new TeamOrganizationException("Cannot divide players equally into 2, 3, or 4 teams.");
        }

        // Shuffle the list of players
        Collections.shuffle(joinedPlayers);

        // Creating teams
        for (int i = 0; i < totalPlayers; i += optimalTeamSize) {
            teams.add(new Team("Team " + (i + 1), new ArrayList<>(joinedPlayers.subList(i, i + optimalTeamSize))));
        }

        return teams;
    }
}
