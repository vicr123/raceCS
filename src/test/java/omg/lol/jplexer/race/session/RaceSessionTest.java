package omg.lol.jplexer.race.session;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import omg.lol.jplexer.race.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RaceSessionTest {
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
    void raceSession_CanAddPlayer() {
        var session = new RaceSession();
        session.addPlayer(server.getPlayer(0));

        assertLinesMatch(session.getJoinedPlayers(), List.of(server.getPlayer(0).getName()));
    }

    @Test
    void raceSession_CanAddMultiplePlayers() {
        var session = new RaceSession();
        session.addPlayer(server.getPlayer(0));
        session.addPlayer(server.getPlayer(1));

        assertLinesMatch(session.getJoinedPlayers(), List.of(server.getPlayer(0).getName(), server.getPlayer(1).getName()));
    }

    @Test
    void raceSession_CanRemovePlayers() {
        var session = new RaceSession();
        session.addPlayer(server.getPlayer(0));
        session.addPlayer(server.getPlayer(1));
        session.removePlayer(server.getPlayer(0));

        assertLinesMatch(session.getJoinedPlayers(), List.of(server.getPlayer(1).getName()));
    }
}