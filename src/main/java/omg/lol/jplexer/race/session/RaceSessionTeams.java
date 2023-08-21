package omg.lol.jplexer.race.session;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RaceSessionTeams {
    private final RaceSession raceSession;

    public List<Team> getTeams() {
        return teams;
    }

    private final List<Team> teams;

    public RaceSessionTeams(RaceSession raceSession, ArrayList<String> joinedPlayers) throws TeamOrganizationException {
        this.raceSession = raceSession;
        teams = teamUpPlayers(joinedPlayers);
    }

    public Team teamFor(Player player) {
        return teams.stream().filter(x -> x.hasPlayer(player)).findFirst().orElseThrow();
    }

    public static List<Team> teamUpPlayers(ArrayList<String> joinedPlayers) throws TeamOrganizationException {
        int totalPlayers = joinedPlayers.size();
        List<Team> teams = new ArrayList<>();

        if (totalPlayers < 2) {
            throw new TeamOrganizationException("Not enough players to form a team.");
        }

        int optimalTeamSize = 0;

        // Determining the optimal team size
        if(totalPlayers % 2 == 0 && totalPlayers / 2 >= 2 && totalPlayers / 2 <= 4) {
            optimalTeamSize = totalPlayers / 2;
        }
        else if (totalPlayers % 3 == 0 && totalPlayers / 3 >= 2) {
            optimalTeamSize = totalPlayers / 3;
        }
        else if (totalPlayers % 4 == 0 && totalPlayers / 4 >= 2) {
            optimalTeamSize = totalPlayers / 4;
        }
        else {
            throw new TeamOrganizationException("Cannot divide players equally into 2, 3, or 4 teams with at least 2 players in each team.");
        }

        // Shuffle the list of players
        Collections.shuffle(joinedPlayers);

        // Creating teams
        for (int i = 0; i < totalPlayers; i += optimalTeamSize) {
            teams.add(new Team("Team " + ((i / optimalTeamSize) + 1), String.valueOf((i / optimalTeamSize)), new ArrayList<>(joinedPlayers.subList(i, i + optimalTeamSize))));
        }

        return teams;
    }
}
