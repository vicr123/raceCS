package omg.lol.jplexer.race.command;

import static omg.lol.jplexer.race.Race.CHAT_PREFIX;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

// All command classes need to implement the CommandExecutor interface to be a proper command!
public class RaceCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {


		// checks if there are no arguments at all (/command)
		if (args.length == 0) {
			sender.sendMessage(CHAT_PREFIX + ChatColor.WHITE + " > " + ChatColor.RED + "Invalid usage - there are no arguments.");
			return false;
		}
		switch (args[0]) {
			case "players":
				viewUsers(sender);
				return true;
			default:
				sender.sendMessage(ChatColor.RED + "Sorry, \"" + args[0] + "\" is not a valid verb.");
				return true;
		}

	}
	void viewUsers(CommandSender sender) {
		sender.sendMessage("Hi");
	}
}
