package omg.lol.jplexer.race;

import jdk.internal.vm.compiler.collections.Pair;
import kong.unirest.Unirest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class CollisionDetection {
    static class Collsion {
        public Collsion(Player first, Player second) {
            this.first = first;
            this.second = second;
        }

        Player first;
        Player second;
    }

    ArrayList<Collsion> collisions;
    Server server;

    CollisionDetection(Server server, Plugin plugin) {
        collisions = new ArrayList<>();
        server.getScheduler().scheduleSyncRepeatingTask(plugin, this::detectCollisions, 0, 1);

        this.server = server;
    }

    void detectCollisions() {
        ArrayList<Player> playersInVehicles = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            Entity vehicle = p.getVehicle();
            if (vehicle != null && vehicle.getType() == EntityType.MINECART) playersInVehicles.add(p);
        }

        while (!playersInVehicles.isEmpty()) {
            Player p = playersInVehicles.remove(0);

            for (Player otherPlayer : playersInVehicles) {
                boolean isInCollision = p.getLocation().distanceSquared(otherPlayer.getLocation()) < 3;
                int collisionIndex = -1;
                for (int i = 0; i < collisions.size(); i++) {
                    CollisionDetection.Collsion collision = collisions.get(i);
                    if ((collision.first == p && collision.second == otherPlayer) || (collision.first == otherPlayer && collision.second == p)) {
                        collisionIndex = i;
                    }
                }

                if (isInCollision && collisionIndex == -1) {
                    //The players have entered a collision state.
                    collisions.add(new CollisionDetection.Collsion(p, otherPlayer));
                    server.broadcastMessage(Race.CHAT_PREFIX + "A collision has occurred between " + ChatColor.RED + p.getDisplayName() + ChatColor.WHITE + " and " + ChatColor.RED + otherPlayer.getDisplayName() + ChatColor.WHITE + " has occurred! Turn back now!");

                    Unirest.post(Race.API_BASE + "/collision/{player1}/{player2}")
                            .routeParam("player1", p.getName())
                            .routeParam("player2", otherPlayer.getName())
                            .queryString("auth", Race.AUTH_TOKEN)
                            .asString();
                } else if (!isInCollision && collisionIndex >= 0) {
                    //The players are no longer in a collision state.
                    collisions.remove(collisionIndex);
                }
            }
        }
    }
}
