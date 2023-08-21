package omg.lol.jplexer.race.session;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import omg.lol.jplexer.race.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

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
    void setName() {
        Team team = new Team("name", "1", List.of());
        team.setName("New Name");
        assertEquals("New Name", team.getName());
    }
}
