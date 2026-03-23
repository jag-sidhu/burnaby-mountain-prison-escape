package Spring2026Team10;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestPowerups {

    @Test
    public void testPowerupInitialization() {
        Powerups coffee = new Powerups(2.0f, 3.0f, PowerupType.COFFEE);
        assertEquals(2.0f, coffee.getX(), "X Coordinate does not match");
        assertEquals(3.0f, coffee.getY(), "Y Coordinate does not match");
        assertEquals(PowerupType.COFFEE, coffee.getType(), "Powerup type does not match");
        assertTrue(coffee.isActive(), "Powerup should be active upon initialization");
    }

    @Test
    public void testCoffeeApply() {
        PrisonMap map = new PrisonMap();
        Player player = new Player(0, 0, map);
        Powerups coffee = new Powerups(0, 0, PowerupType.COFFEE);
        
        int initialScore = player.getScore();
        coffee.applyTo(player);
        
        assertEquals(initialScore + 25, player.getScore(), "Score should increase by 25");
        assertFalse(coffee.isActive(), "Powerup should deactivate after use");
    }

    @Test
    public void testSnowflakeApply() {
        PrisonMap map = new PrisonMap();
        Player player = new Player(0, 0, map);
        Powerups snowflake = new Powerups(0, 0, PowerupType.SNOWFLAKE);
        
        int initialScore = player.getScore();
        snowflake.applyTo(player);
        
        assertEquals(initialScore + 25, player.getScore(), "Score should increase by 25");
        assertTrue(player.isGuardsFrozen(), "Guards should be frozen");
    }

    @Test
    public void testDoctorsNoteApply() {
        PrisonMap map = new PrisonMap();
        Player player = new Player(0, 0, map);
        
        // Damage the player first so they can actually heal
        player.setLives(1); 
        Powerups doctor = new Powerups(0, 0, PowerupType.DOCTORS_NOTE);
        
        int initialScore = player.getScore();
        doctor.applyTo(player);
        
        assertEquals(initialScore + 40, player.getScore(), "Score should increase by 40");
        assertEquals(2, player.getLives(), "Player should gain 1 life back");
    }

    @Test
    public void testCannotCollectPowerupWhileHandsTied() {
        PrisonMap map = new PrisonMap();
        Player player = new Player(0, 0, map);
        Powerups coffee = new Powerups(0, 0, PowerupType.COFFEE);
        
        player.tieHands(90); // Apply handcuffs effect
        int initialScore = player.getScore();
        
        coffee.applyTo(player); // Attempt to collect
        
        assertEquals(initialScore, player.getScore(), "Score should not increase");
        assertTrue(coffee.isActive(), "Powerup should remain active on the ground");
    }
}
