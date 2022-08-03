package omg.lol.jplexer.race.command;

import static omg.lol.jplexer.race.Race.CHAT_PREFIX;
import static omg.lol.jplexer.race.Race.getPlugin;

import kong.unirest.json.JSONObject;
import omg.lol.jplexer.race.CommandUtils;
import omg.lol.jplexer.race.Race;
import omg.lol.jplexer.race.session.RaceSession;
import omg.lol.jplexer.race.command.management.RaceManagement;
import omg.lol.jplexer.race.command.management.RegionManagement;
import omg.lol.jplexer.race.command.management.StationManagement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.md_5.bungee.api.ChatColor;
import kong.unirest.Unirest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Arrays;

// All command classes need to implement the CommandExecutor interface to be a proper command!
public class RaceCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		args = CommandUtils.getArgs(args);

		// checks if there are no arguments at all (/command)
		if (args.length == 0) {
			sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid usage - there are no arguments.");
			return false;
		}
		switch (args[0].toLowerCase()) {
//			case "players":
//				viewUsers(sender);
//				return true;
			case "race":
				RaceManagement.RaceManagementCommand(sender, Arrays.copyOfRange(args, 1, args.length));
				return true;
//			case "adduser":
//				addUser(sender, args);
//				return true;
//			case "removeuser":
//				removeUser(sender, args);
//				return true;
//			case "arrived":
//				arrive(sender, args);
//				return true;
//			case "completion":
//				complete(sender, args);
//				return true;
//			case "setstations":
//				setStations(sender, args);
//				return true;
			case "stations":
				StationManagement.StationManagementCommand(sender, Arrays.copyOfRange(args, 1, args.length));
				return true;
			case "regions":
				RegionManagement.RegionManagementCommand(sender, Arrays.copyOfRange(args, 1, args.length));
				return true;
			default:
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, \"" + args[0] + "\" is not a valid verb.");
				return true;
		}
	}

	void setStations(CommandSender sender, String[] args) {
		if(sender.hasPermission("racecs.jplexer")) {
			if (args.length != 2) {
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you'll need to specify the stations as a JSON Array.");
			} else {
				Unirest.post("/stations")
						.contentType("application/json")
						.body(args[1])
						.asStringAsync((response) -> {
					if (response.isSuccess()) {
						sender.sendMessage(CHAT_PREFIX + "The stations were set.");
					} else {
						sender.sendMessage(CHAT_PREFIX + "Could not set stations: " + response.getBody());
					}
				});
			}
		} else {
			sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
		}
	}

	void viewUsers(CommandSender sender) {
		JSONObject response = Unirest.get("/users").asJson().getBody().getObject();
		sender.sendMessage(response.toString());
	}

	void addUser(CommandSender sender, String[] args) {
		if (sender.hasPermission("racecs.jplexer")) {
			if (args.length != 2) {
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you'll need to specify Name.");

			} else {
				Entity target = CommandUtils.getTarget(sender, args[1]);
				if (!(target instanceof Player)) {
					sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can only add players.");
					return;
				}

				String user = target.getName();

				Unirest.post("/addUser/{player}/{uuid}")
						.routeParam("player", user)
						.routeParam("uuid", target.getUniqueId().toString())
						.asString();
				sender.sendMessage(CHAT_PREFIX + user + " was added.");

				RaceSession raceSession = Race.getPlugin().getCurrentRace();
				if (raceSession == null || raceSession.isEnded()) {
					Race.getPlugin().createNewRace();
					raceSession = Race.getPlugin().getCurrentRace();
				}
				raceSession.addPlayer((Player) target);
			}
		} else {
			sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
		}
	}

	void removeUser(CommandSender sender, String[] args)  {
		if(sender.hasPermission("racecs.jplexer")) {
			if (args.length != 2) {
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you'll need to specify Name.");

			} else {
				Entity target = CommandUtils.getTarget(sender, args[1]);
				if (!(target instanceof Player)) {
					sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can only remove players.");
					return;
				}
				String user = target.getName();

				Unirest.post("/removeUser/{player}")
						.routeParam("player", user)
						.asString();
				sender.sendMessage(CHAT_PREFIX + user + " was removed.");

				Race.getPlugin().getCurrentRace().removePlayer((Player) target);
			}
		} else {
			sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
		}
	}
	void arrive(CommandSender sender, String[] args)  {
		if (sender.hasPermission("racecs.jplexer")) {
			if (args.length <= 2) {
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you'll need to specify Name and Location.");

			} else if (args.length != 3) {
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you'll need to specify Location.");

			} else {
				String user = CommandUtils.getTarget(sender, args[1]).getName();

				Unirest.post("/arrive/{player}/{location}")
						.routeParam("player", user)
						.routeParam("location", args[2])
						.asString();
			}
		} else {
			sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
		}

	}
	void complete(CommandSender sender, String[] args)  {
		if (sender.hasPermission("racecs.jplexer")) {
			if (args.length <= 1) {
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you'll need to specify Name.");
			} else {
				String user = CommandUtils.getTarget(sender, args[1]).getName();

				Scoreboard sb = getPlugin().getServer().getScoreboardManager().getMainScoreboard();
				Objective objective = sb.getObjective("acsfinish");
				Score score = objective.getScore("finish");
				int place = score.getScore();

				Unirest.post("/completion/{player}/{place}")
						.routeParam("player", user)
						.routeParam("place", String.valueOf(place))
						.asString();
			}
		} else {
			sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Sorry, you can't use this.");
		}
	}
}


