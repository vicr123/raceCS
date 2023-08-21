package omg.lol.jplexer.race.session;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import omg.lol.jplexer.race.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RaceSessionTeamsTest {

    private ServerMock server;
    private Race plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Race.class);

        server.setPlayers(12);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void teamUpPlayers_FourPlayers() throws TeamOrganizationException {
        var players = new ArrayList<>(List.of("Player 1", "Player 2", "Player 3", "Player 4"));

        var results = RaceSessionTeams.teamUpPlayers(players);
        assertEquals(2, results.size());
        assertEquals(2, results.get(0).getMembers().size());
        assertEquals(2, results.get(1).getMembers().size());
    }

    @Test
    void teamUpPlayers_OnePlayer() {
        var players = new ArrayList<>(List.of("Player 1"));

        assertThrowsExactly(TeamOrganizationException.class, () -> RaceSessionTeams.teamUpPlayers(players));
    }

    @Test
    void teamUpPlayers_FivePlayers() {
        var players = new ArrayList<>(List.of("Player 1", "Player 2", "Player 3", "Player 4", "Player 5"));

        assertThrowsExactly(TeamOrganizationException.class, () -> RaceSessionTeams.teamUpPlayers(players));
    }


    @Test
    void teamUpPlayers_EightPlayers() throws TeamOrganizationException {
        var players = new ArrayList<>(List.of("Player 1", "Player 2", "Player 3", "Player 4", "Player 5", "Player 6", "Player 7", "Player 8"));

        var results = RaceSessionTeams.teamUpPlayers(players);
        assertEquals(2, results.size());
        assertEquals(4, results.get(0).getMembers().size());
        assertEquals(4, results.get(1).getMembers().size());
    }


    @Test
    void teamUpPlayers_NinePlayers() throws TeamOrganizationException {
        var players = new ArrayList<>(List.of("Player 1", "Player 2", "Player 3", "Player 4", "Player 5", "Player 6", "Player 7", "Player 8", "Player 9"));

        var results = RaceSessionTeams.teamUpPlayers(players);
        assertEquals(3, results.size());
        assertEquals(3, results.get(0).getMembers().size());
        assertEquals(3, results.get(1).getMembers().size());
        assertEquals(3, results.get(2).getMembers().size());
    }
}