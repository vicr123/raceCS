package omg.lol.jplexer.race.session;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import omg.lol.jplexer.race.Race;
import omg.lol.jplexer.race.command.management.StationManagement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RaceSessionTeamTest {
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
    void raceSession_CanNotAddPlayerAfterTeaming() throws TeamOrganizationException {
        var session = new RaceSession();
        session.addPlayer(server.getPlayer(0));
        session.addPlayer(server.getPlayer(1));
        session.addPlayer(server.getPlayer(2));
        session.addPlayer(server.getPlayer(3));
        session.teamUp();
        session.addPlayer(server.getPlayer(4));

        assertEquals(4, session.getJoinedPlayers().size());
    }

    @Test
    void raceSession_TeamVictory() throws TeamOrganizationException {
        StationManagement.AddStation(server.getPlayer(0), "acsr", "AirCS Race Lobby");
        StationManagement.AddStation(server.getPlayer(0), "acs", "AirCS Cental");
        StationManagement.AddStation(server.getPlayer(0), "li", "Long Island");
        StationManagement.AddStation(server.getPlayer(0), "vic", "theStation");

        var session = new RaceSession();
        session.addPlayer(server.getPlayer(0));
        session.addPlayer(server.getPlayer(1));
        session.addPlayer(server.getPlayer(2));
        session.addPlayer(server.getPlayer(3));
        session.teamUp();

        var winningTeam = session.getTeams().getTeams().get(0);
        var player1 = server.getPlayer(winningTeam.getMembers().get(0));
        var player2 = server.getPlayer(winningTeam.getMembers().get(1));
        winningTeam.setName("The Winning Team");

        var li = session.getParticipatingStations().stream().filter(station -> station.getId().equals("LI")).findFirst().orElseThrow();
        var vic = session.getParticipatingStations().stream().filter(station -> station.getId().equals("VIC")).findFirst().orElseThrow();
        var acsr = session.getTerminalStation();
        session.processStationArrived(player1, li);
        assertEquals(2, winningTeam.membersWaitingToReturn());

        session.processStationArrived(player2, acsr);
        assertEquals(2, winningTeam.membersWaitingToReturn());

        session.processStationArrived(player2, vic);
        assertEquals(2, winningTeam.membersWaitingToReturn());

        session.processStationArrived(player1, acsr);
        assertEquals(1, winningTeam.membersWaitingToReturn());

        session.processStationArrived(player2, acsr);
        assertEquals(0, winningTeam.membersWaitingToReturn());

        var losingTeam = session.getTeams().getTeams().get(1);
        var player3 = server.getPlayer(losingTeam.getMembers().get(0));
        var player4 = server.getPlayer(losingTeam.getMembers().get(1));
        losingTeam.setName("The Losing Team");
        session.processStationArrived(player3, li);
        session.processStationArrived(player4, acsr);
        session.processStationArrived(player4, vic);
        session.processStationArrived(player4, acsr);
        session.processStationArrived(player3, acsr);
    }
}

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

    @Test
    void raceSession_TracksStations() {
        StationManagement.AddStation(server.getPlayer(0), "acsr", "AirCS Race Lobby");
        StationManagement.AddStation(server.getPlayer(0), "acs", "AirCS Cental");
        StationManagement.AddStation(server.getPlayer(0), "li", "Long Island");
        StationManagement.AddStation(server.getPlayer(0), "vic", "theStation");

        var session = new RaceSession();
        session.addPlayer(server.getPlayer(0));
        session.addPlayer(server.getPlayer(1));

        var li = session.getParticipatingStations().stream().filter(station -> station.getId().equals("LI")).findFirst().orElseThrow();
        session.processStationArrived(server.getPlayer(0), li);

        assertEquals(((RaceSession.StationEvent) session.getEvents().get(session.getEvents().size() - 1)).station, li);
    }

    @Test
    void raceSession_TracksCompletion() {
        StationManagement.AddStation(server.getPlayer(0), "acsr", "AirCS Race Lobby");
        StationManagement.AddStation(server.getPlayer(0), "acs", "AirCS Cental");
        StationManagement.AddStation(server.getPlayer(0), "li", "Long Island");
        StationManagement.AddStation(server.getPlayer(0), "vic", "theStation");

        var session = new RaceSession();
        session.addPlayer(server.getPlayer(0));
        session.addPlayer(server.getPlayer(1));

        var li = session.getParticipatingStations().stream().filter(station -> station.getId().equals("LI")).findFirst().orElseThrow();
        var vic = session.getParticipatingStations().stream().filter(station -> station.getId().equals("VIC")).findFirst().orElseThrow();
        var acsr = session.getTerminalStation();
        session.processStationArrived(server.getPlayer(0), li);
        session.processStationArrived(server.getPlayer(0), vic);
        session.processStationArrived(server.getPlayer(0), acsr);

        assertLinesMatch(session.getFinishedPlayers(), List.of(server.getPlayer(0).getName()));
    }
}