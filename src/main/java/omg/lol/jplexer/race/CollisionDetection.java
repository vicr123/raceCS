package omg.lol.jplexer.race;

import kong.unirest.Unirest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.stream.Collectors;

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

    boolean isInCollision(Player first, Player second) {
        Vector firstVelocity = first.getVehicle().getVelocity();
        Vector secondVelocity = second.getVehicle().getVelocity();

        Location firstLocation = first.getVehicle().getLocation();
        Location secondLocation = second.getVehicle().getLocation();

        boolean firstPrimaryIsX = Math.abs(firstVelocity.getX()) > Math.abs(firstVelocity.getZ());
        boolean secondPrimaryIsX = Math.abs(secondVelocity.getX()) > Math.abs(secondVelocity.getZ());

        if (firstPrimaryIsX != secondPrimaryIsX) return false;

        int firstPrimaryCoordinate = firstPrimaryIsX ? firstLocation.getBlockX() : firstLocation.getBlockZ();
        int firstSecondaryCoordinate = firstPrimaryIsX ? firstLocation.getBlockZ() : firstLocation.getBlockX();
        int secondPrimaryCoordinate = secondPrimaryIsX ? secondLocation.getBlockX() : secondLocation.getBlockZ();
        int secondSecondaryCoordinate = secondPrimaryIsX ? secondLocation.getBlockZ() : secondLocation.getBlockX();

        if (firstSecondaryCoordinate != secondSecondaryCoordinate) return false;
        if (firstPrimaryCoordinate - 3 >= secondPrimaryCoordinate || firstPrimaryCoordinate + 3 <= secondPrimaryCoordinate)  return false;

        if (Math.abs(firstLocation.getBlockY() - secondLocation.getBlockY()) > 1) return false;

        return true;
    }

    void clearPath(Player player) {
        var nearby = player.getNearbyEntities(2, 2, 2);
        for (var entity : nearby) {
            if (!entity.getPassengers().isEmpty()) continue;
            if (entity instanceof Animals || entity instanceof Vehicle) {
                entity.remove();
            }
        }
    }

    void detectCollisions() {
        var playersInVehicles = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getVehicle() != null && p.getVehicle().getType() == EntityType.MINECART)
                .collect(Collectors.toList());

        var tracker = Race.getPlugin().getStationTracker();

        for (var player : playersInVehicles) {
            //Clear the path for all players
            clearPath(player);
        }

        while (!playersInVehicles.isEmpty()) {
            Player p = playersInVehicles.remove(0);

            for (Player otherPlayer : playersInVehicles) {
//                boolean isInCollision = p.getLocation().distanceSquared(otherPlayer.getLocation()) < 3;
                boolean isInCollision = this.isInCollision(p, otherPlayer);
                int collisionIndex = -1;
                for (int i = 0; i < collisions.size(); i++) {
                    CollisionDetection.Collsion collision = collisions.get(i);
                    if ((collision.first == p && collision.second == otherPlayer) || (collision.first == otherPlayer && collision.second == p)) {
                        collisionIndex = i;
                    }
                }

                if (isInCollision && collisionIndex == -1) {
                    //The players have entered a collision state.
                    //Ensure that the players are not inside a valid station

                    if (tracker.isInStation(p) || tracker.isInStation(otherPlayer)) {
                        //One of the players are in valid stations, so dismount them from the minecarts immediately
                        p.getVehicle().remove();
                        otherPlayer.getVehicle().remove();
                    } else {
                        //The players are not in a valid station so this is a valid collision
                        collisions.add(new CollisionDetection.Collsion(p, otherPlayer));
                        if (Race.getPlugin().hasCurrentRace()) {
                            Race.getPlugin().getCurrentRace().playersCollided(p, otherPlayer);
                        }
                        server.broadcastMessage(Race.CHAT_PREFIX + "A collision has occurred between " + ChatColor.RED + p.getDisplayName() + ChatColor.WHITE + " and " + ChatColor.RED + otherPlayer.getDisplayName() + ChatColor.WHITE + " has occurred! Turn back now!");

                        Unirest.post(Race.API_BASE + "/collision/{player1}/{player2}")
                                .routeParam("player1", p.getName())
                                .routeParam("player2", otherPlayer.getName())
                                .asString();

                        //Reverse the direction of each minecart
                        for (Player player : new Player[]{p, otherPlayer}) {
                            Minecart minecart = (Minecart) player.getVehicle();

                            Vector initialVelocity = minecart.getVelocity();
                            initialVelocity.setX(-initialVelocity.getX());
                            initialVelocity.setY(-initialVelocity.getY());
                            initialVelocity.setZ(-initialVelocity.getZ());
                            minecart.setVelocity(initialVelocity);
                        }
                    }
                } else if (!isInCollision && collisionIndex >= 0) {
                    //The players are no longer in a collision state.
                    collisions.remove(collisionIndex);
                }
            }
        }
    }
}
