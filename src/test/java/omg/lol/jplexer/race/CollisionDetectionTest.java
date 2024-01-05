package omg.lol.jplexer.race;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CollisionDetectionTest {
    @Test
    void TestX1() {
        var firstTest = CollisionDetection.isInCollision(new Location(null, 0, 0, 0), new Vector(20, 0, 0), new Location(null, 20, 0, 0), new Vector(-20, 0, 0));
        assertTrue(firstTest);
    }

    @Test
    void TestX1Swapped() {
        var firstTest = CollisionDetection.isInCollision(new Location(null, 20, 0, 0), new Vector(-20, 0, 0), new Location(null, 0, 0, 0), new Vector(20, 0, 0));
        assertTrue(firstTest);
    }

    @Test
    void TestNotX1() {
        var firstTest = CollisionDetection.isInCollision(new Location(null, 0, 0, 0), new Vector(20, 0, 0), new Location(null, 20, 0, 1), new Vector(-20, 0, 0));
        assertFalse(firstTest);
    }

    @Test
    void TestZ1() {
        var firstTest = CollisionDetection.isInCollision(new Location(null, 0, 0, 0), new Vector(0, 0, 20), new Location(null, 0, 0, 20), new Vector(0, 0, -20));
        assertTrue(firstTest);
    }

    @Test
    void TestZ1Swapped() {
        var firstTest = CollisionDetection.isInCollision(new Location(null, 0, 0, 20), new Vector(0, 0, -20), new Location(null, 0, 0, 0), new Vector(0, 0, 20));
        assertTrue(firstTest);
    }

    @Test
    void TestNotZ1() {
        var firstTest = CollisionDetection.isInCollision(new Location(null, 1, 0, 0), new Vector(0, 0, 20), new Location(null, 0, 0, 20), new Vector(0, 0, -20));
        assertFalse(firstTest);
    }

    @Test
    void TestXWithY1() {
        var firstTest = CollisionDetection.isInCollision(new Location(null, 0, 0, 0), new Vector(20, 20, 0), new Location(null, 20, 20, 0), new Vector(-20, -20, 0));
        assertTrue(firstTest);
    }

    @Test
    void TestXWithY1AwayFromOrigin() {
        var firstTest = CollisionDetection.isInCollision(new Location(null, 1000, 1000, 1000), new Vector(20, 20, 0), new Location(null, 1020, 1020, 1000), new Vector(-20, -20, 0));
        assertTrue(firstTest);
    }
}
