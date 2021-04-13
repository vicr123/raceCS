package omg.lol.jplexer.race;

import omg.lol.jplexer.race.command.RaceCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;


public class Race extends JavaPlugin {

	// Feel free to change this to your own plugin's name and color of your choice.
	public static final String CHAT_PREFIX = ChatColor.RED + "fake" + ChatColor.GRAY + "CS" + ChatColor.WHITE + " > ";

	private static Race plugin; // This is a static plugin instance that is private. Use getPlugin() as seen
									// further below.

	PluginDescriptionFile pdfFile; // plugin.yml

	// Called when the plugin is disabled, such as when you reload the server.
	public void onDisable() {
		
	}

	public static Race getPlugin() { // getter for the static plugin instance
		return plugin;
	}
	
	// Called when the plugin is enabled. It is used to set up variables and to register things such as commands.
	@Override
	public void onEnable() {
		plugin = getPlugin(Race.class);
		PluginManager pm = getServer().getPluginManager();

		/*
		 * Register a command to the list of usable commands. If you don't register the
		 * command, it won't work! Also if you change the command name, make sure to
		 * also change in the plugin.yml file.
		 */
		this.getCommand("raceCS").setExecutor(new RaceCommand());


		/*
		 * This line lets you send out information to the console. In this case it would
		 * say: Yay, Template-Plugin is now enabled!
		 */
		this.getLogger()
				.info("raceCS is now enabled! May the races begin!");
	}

}
