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

import static java.lang.Math.*;

public class CollisionDetection {
    static class Collision {
        public Collision(Player first, Player second) {
            this.first = first;
            this.second = second;
        }

        Player first;
        Player second;
    }

    ArrayList<Collision> collisions;
    Server server;

    public static Vector multiplyMatrixWithVector(double[][] matrix, Vector vector) {
        double x = matrix[0][0] * vector.getX() + matrix[0][1] * vector.getY() + matrix[0][2] * vector.getZ();
        double y = matrix[1][0] * vector.getX() + matrix[1][1] * vector.getY() + matrix[1][2] * vector.getZ();
        double z = matrix[2][0] * vector.getX() + matrix[2][1] * vector.getY() + matrix[2][2] * vector.getZ();

        return new Vector(x, y, z);
    }


    CollisionDetection(Server server, Plugin plugin) {
        collisions = new ArrayList<>();
        server.getScheduler().scheduleSyncRepeatingTask(plugin, this::detectCollisions, 0, 1);

        this.server = server;
    }

    public static boolean isInCollision(Location firstLocation, Vector firstVelocityNotNormalised, Location secondLocation, Vector secondVelocityNotNormalised) {
        var firstSpeed = firstVelocityNotNormalised.length();
        var firstVelocity = firstVelocityNotNormalised.clone().normalize();
        var secondVelocity = secondVelocityNotNormalised.clone().normalize();

        var dotProduct = firstVelocity.dot(secondVelocity);
        if (dotProduct > -0.5) return false;

        var xy = -Math.atan2(firstVelocity.getZ(), firstVelocity.getX()); // u
        var hyp = Math.hypot(firstVelocity.getX(), firstVelocity.getZ());
        var xz = -Math.atan2(firstVelocity.getY(), hyp);

        var matrix = new double[][] {
                {cos(xy) * cos(xz), -sin(xz), sin(xy) * -cos(xz)},
                {cos(xy) * sin(xz), cos(xz), -sin(xy) * sin(xz)},
                {sin(xy), 0, cos(xy)}
        };
        var rotatedVector = multiplyMatrixWithVector(matrix, secondLocation.toVector().clone().subtract(firstLocation.toVector()));

//        Bukkit.getServer().broadcastMessage(String.format("xy, h, xz: %f %f %f pj: %f, %f, %f", xy, hyp, xz, rotatedVector.getX(), rotatedVector.getY(), rotatedVector.getZ()));

        return 0 <= rotatedVector.getX() && rotatedVector.getX() <= firstSpeed * 0.125 * 20 &&
                Math.hypot(rotatedVector.getY(), rotatedVector.getZ()) <= 0.9;
    }

    boolean isInCollision(Player first, Player second) {
        return isInCollision(first.getLocation(), first.getVehicle().getVelocity().clone(), second.getLocation(), second.getVehicle().getVelocity().clone());
    }

    void clearPath(Player player) {
        var nearby = player.getNearbyEntities(2, 2, 2);
        for (var entity : nearby) {
            if (!entity.getPassengers().isEmpty()) continue;
            if (entity instanceof Animals || entity instanceof Vehicle || entity instanceof Monster) {
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
                    Collision collision = collisions.get(i);
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
                        collisions.add(new Collision(p, otherPlayer));
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
