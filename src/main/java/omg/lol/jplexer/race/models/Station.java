package omg.lol.jplexer.race.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Objects;

@DatabaseTable(tableName = "stations")
public class Station {
    @DatabaseField(id = true)
    private String id;

    @DatabaseField(canBeNull = false)
    private String name;

    @ForeignCollectionField(eager=false)
    private ForeignCollection<Region> regions;

    public Station() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id.toUpperCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHumanReadableName() {
        return "\"" + this.name + "\" [" + this.id + "]";
    }

    public ForeignCollection<Region> getRegions() {
        return regions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return id.equals(station.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
