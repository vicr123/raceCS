package omg.lol.jplexer.race;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import kong.unirest.Unirest;
import net.md_5.bungee.api.ChatColor;
import omg.lol.jplexer.race.command.RaceCommand;
import omg.lol.jplexer.race.command.RaceCompleter;
import omg.lol.jplexer.race.models.Region;
import omg.lol.jplexer.race.models.Station;
import omg.lol.jplexer.race.session.RaceSession;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;


public class Race extends JavaPlugin {

	// Feel free to change this to your own plugin's name and color of your choice.
	public static final String CHAT_PREFIX = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Air" + ChatColor.WHITE + "" + ChatColor.BOLD + "CS" + ChatColor.GOLD + "" + ChatColor.BOLD + " Race Update: " + ChatColor.WHITE;
	public static final String API_BASE = "http://localhost:3000/api";
	public static final String AUTH_TOKEN = "kJYhqvDRnWLUHpMVfXmkUiRqrrEmhCgz";

	private static Race plugin; // This is a static plugin instance that is private. Use getPlugin() as seen
									// further below.

	private Dao<Station, String> stationDao;
	private Dao<Region, Long> regionDao;

	private PlayerStationTracker stationTracker;

	private RaceSession currentRace = null;

	// Called when the plugin is disabled, such as when you reload the server.
	public void onDisable() {
		if (currentRace != null) currentRace.endSession();
	}

	public static Race getPlugin() { // getter for the static plugin instance
		return plugin;
	}
	
	// Called when the plugin is enabled. It is used to set up variables and to register things such as commands.
	@Override
	public void onEnable() {
		plugin = getPlugin(Race.class);

		try {
			JdbcPooledConnectionSource connectionSource = new JdbcPooledConnectionSource("jdbc:sqlite:racecs.db");

			TableUtils.createTableIfNotExists(connectionSource, Station.class);
			stationDao = DaoManager.createDao(connectionSource, Station.class);

			TableUtils.createTableIfNotExists(connectionSource, Region.class);
			regionDao = DaoManager.createDao(connectionSource, Region.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String baseUrl = API_BASE;
		if (System.getenv("RACECS_BASE_URL") != null) baseUrl = System.getenv("RACECS_BASE_URL");

		String authToken = AUTH_TOKEN;
		if (System.getenv("RACECS_AUTH_TOKEN") != null) authToken = System.getenv("RACECS_AUTH_TOKEN");

		//Set Unirest default settings
		Unirest.config()
				.defaultBaseUrl(baseUrl)
				.setDefaultHeader("Authorization", "Bearer " + authToken);

		/*
		 * Register a command to the list of usable commands. If you don't register the
		 * command, it won't work! Also if you change the command name, make sure to
		 * also change in the plugin.yml file.
		 */
		this.getCommand("raceCS").setExecutor(new RaceCommand());
		this.getCommand("raceCS").setTabCompleter(new RaceCompleter());

		// Set up a timer for collision detection
		new CollisionDetection(getServer(), plugin);

		// Start tracking players
		stationTracker = new PlayerStationTracker();
		getServer().getPluginManager().registerEvents(stationTracker, this);

		this.getLogger().info("raceCS is now enabled! May the races begin!");
	}

	public Dao<Station, String> getStationDao() {
		return stationDao;
	}

	public Dao<Region, Long> getRegionDao() {
		return regionDao;
	}

	public boolean hasCurrentRace() {
		if (this.currentRace == null) return false;
		if (this.currentRace.isEnded()) return false;
		return true;
	}

	public void createNewRace() {
		if (this.currentRace != null) this.currentRace.endSession();
		this.currentRace = new RaceSession();
	}

	public RaceSession getCurrentRace() {
		return currentRace;
	}

	public PlayerStationTracker getStationTracker() {
		return stationTracker;
	}
}
