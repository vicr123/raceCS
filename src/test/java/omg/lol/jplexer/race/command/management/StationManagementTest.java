package omg.lol.jplexer.race.command.management;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import omg.lol.jplexer.race.Race;
import omg.lol.jplexer.race.models.Station;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationManagementTest {

    private ServerMock server;
    private Race plugin;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Race.class);

        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void addStation() throws SQLException {
        StationManagement.AddStation(player, "acsr", "AirCS Race Lobby");
        StationManagement.AddStation(player, "acs", "AirCS Central");

        assertLinesMatch(plugin.getStationDao().queryForAll().stream().map(Station::getId).toList(), List.of("ACSR", "ACS"));
        assertLinesMatch(plugin.getStationDao().queryForAll().stream().map(Station::getName).toList(), List.of("AirCS Race Lobby", "AirCS Central"));
    }

    @Test
    void renameStation() throws SQLException {
        StationManagement.AddStation(player, "acsr", "AirCS Race Lobby");
        StationManagement.AddStation(player, "acs", "AirCS Cental");
        StationManagement.RenameStation(player, "acs", "AirCS Central");

        assertLinesMatch(plugin.getStationDao().queryForAll().stream().map(Station::getId).toList(), List.of("ACSR", "ACS"));
        assertLinesMatch(plugin.getStationDao().queryForAll().stream().map(Station::getName).toList(), List.of("AirCS Race Lobby", "AirCS Central"));
    }

    @Test
    void removeStation() throws SQLException {
        StationManagement.AddStation(player, "acsr", "AirCS Race Lobby");
        StationManagement.AddStation(player, "acs", "AirCS Central");
        StationManagement.RemoveStation(player, "acs");

        assertLinesMatch(plugin.getStationDao().queryForAll().stream().map(Station::getId).toList(), List.of("ACSR"));
        assertLinesMatch(plugin.getStationDao().queryForAll().stream().map(Station::getName).toList(), List.of("AirCS Race Lobby"));
    }
}