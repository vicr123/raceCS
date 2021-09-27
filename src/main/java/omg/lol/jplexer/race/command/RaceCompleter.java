package omg.lol.jplexer.race.command;

import net.md_5.bungee.api.ChatColor;
import omg.lol.jplexer.race.CommandUtils;
import omg.lol.jplexer.race.command.management.RaceManagement;
import omg.lol.jplexer.race.command.management.RegionManagement;
import omg.lol.jplexer.race.command.management.StationManagement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static omg.lol.jplexer.race.Race.CHAT_PREFIX;

public class RaceCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        String[] args = CommandUtils.getArgs(strings);

        // checks if there are no arguments at all (/command)
        if (args.length == 0) {
            return Arrays.asList("race", "stations", "regions");
        } else {
            switch (args[0].toLowerCase()) {
                case "race":
                    return RaceManagement.TabCompleteCommand(Arrays.copyOfRange(args, 1, args.length));
                case "stations":
                    return StationManagement.TabCompleteCommand(Arrays.copyOfRange(args, 1, args.length));
                case "regions":
                    return RegionManagement.TabCompleteCommand(Arrays.copyOfRange(args, 1, args.length));
                default:
                    return Arrays.asList("race", "stations", "regions");
            }
        }
    }

    public static List<String> completeList(String[] currentInput, String[] nextArgs) {
        return Arrays.asList(nextArgs);
    }
}
