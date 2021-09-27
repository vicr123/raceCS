package omg.lol.jplexer.race.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.bukkit.Location;

import java.util.Objects;

@DatabaseTable(tableName = "regions")
public class Region {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, canBeNull = false)
    private Station station;

    @DatabaseField(canBeNull = false)
    private long x1;
    @DatabaseField(canBeNull = false)
    private long y1;
    @DatabaseField(canBeNull = false)
    private long z1;

    @DatabaseField(canBeNull = false)
    private long x2;
    @DatabaseField(canBeNull = false)
    private long y2;
    @DatabaseField(canBeNull = false)
    private long z2;

    @DatabaseField(canBeNull = false)
    private String world;

    public Region() {

    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public long getX1() {
        return x1;
    }

    public void setX1(long x1) {
        this.x1 = x1;
    }

    public long getY1() {
        return y1;
    }

    public void setY1(long y1) {
        this.y1 = y1;
    }

    public long getZ1() {
        return z1;
    }

    public void setZ1(long z1) {
        this.z1 = z1;
    }

    public long getX2() {
        return x2;
    }

    public void setX2(long x2) {
        this.x2 = x2;
    }

    public long getY2() {
        return y2;
    }

    public void setY2(long y2) {
        this.y2 = y2;
    }

    public long getZ2() {
        return z2;
    }

    public void setZ2(long z2) {
        this.z2 = z2;
    }

    private static boolean inBetween(long a, long b, long value) {
        if (a < b) return value >= a && value <= b; else return value <= a && value >= b;
    }

    public boolean inRegion(long x, long y, long z, String world) {
        if (!Objects.equals(world, this.world)) return false;
        if (!inBetween(x1, x2, x)) return false;
        if (!inBetween(y1, y2, y)) return false;
        if (!inBetween(z1, z2, z)) return false;
        return true;
    }

    public boolean inRegion(Location location) {
        return inRegion(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }
}
