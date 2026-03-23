package Spring2026Team10;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestHazard {

    @Test
    public void testHazardInitialization() {
        Hazard bear = new Hazard(5.0f, 5.0f, HazardType.BEAR);
        assertEquals(5.0f, bear.getX(), "X Coordinate does not match");
        assertEquals(5.0f, bear.getY(), "Y Coordinate does not match");
        assertEquals(HazardType.BEAR, bear.getHazardType(), "Hazard type does not match");
        assertTrue(bear.isActive(), "Hazard should be active upon initialization");
    }

    @Test
    public void testHandcuffsApply() {
        PrisonMap map = new PrisonMap();
        Player player = new Player(0, 0, map);
        Hazard handcuffs = new Hazard(0, 0, HazardType.HANDCUFFS);
        
        int initialScore = player.getScore();
        handcuffs.applyTo(player);
        
        assertEquals(initialScore - 10, player.getScore(), "Score should decrease by 10");
        assertTrue(player.isHandsTied(), "Player hands should be tied");
        assertEquals(Player.StatusState.HANDS_TIED, player.getStatusState(), "Status state should update");
        assertFalse(handcuffs.isActive(), "Hazard should deactivate after use");
    }

    @Test
    public void testParkingTicketApply() {
        PrisonMap map = new PrisonMap();
        Player player = new Player(0, 0, map);
        Hazard ticket = new Hazard(0, 0, HazardType.PARKING_TICKET);
        
        int initialScore = player.getScore();
        ticket.applyTo(player);
        
        assertEquals(initialScore - 20, player.getScore(), "Score should decrease by 20");
        assertEquals(Player.StatusState.SLOWED, player.getStatusState(), "Player should be slowed");
        assertFalse(ticket.isActive(), "Hazard should deactivate after use");
    }

    @Test
    public void testBearApply() {
        PrisonMap map = new PrisonMap();
        Player player = new Player(0, 0, map);
        Hazard bear = new Hazard(0, 0, HazardType.BEAR);
        
        int initialLives = player.getLives(); // Should be 3
        int initialScore = player.getScore();
        bear.applyTo(player);
        
        assertEquals(initialScore - 50, player.getScore(), "Score should decrease by 50");
        assertEquals(initialLives - 1, player.getLives(), "Player should lose 1 life");
        assertTrue(player.isInvulnerable(), "Player should have a grace period after bear attack");
    }

    @Test
    public void testSpoiledMilkApply() {
        PrisonMap map = new PrisonMap();
        Player player = new Player(0, 0, map);
        Hazard milk = new Hazard(0, 0, HazardType.SPOILED_MILK);
        
        int initialScore = player.getScore();
        milk.applyTo(player);
        
        assertEquals(initialScore - 15, player.getScore(), "Score should decrease by 15");
        assertEquals(Player.StatusState.INVERTED_CONTROLS, player.getStatusState(), "Controls should be inverted");
    }

    @Test
    public void testInactiveHazardDoesNothing() {
        PrisonMap map = new PrisonMap();
        Player player = new Player(0, 0, map);
        Hazard bear = new Hazard(0, 0, HazardType.BEAR);
        
        bear.applyTo(player); // First application
        int scoreAfterFirstHit = player.getScore();
        int livesAfterFirstHit = player.getLives();
        
        bear.applyTo(player); // Second application attempt
        assertEquals(scoreAfterFirstHit, player.getScore(), "Score should not drop again");
        assertEquals(livesAfterFirstHit, player.getLives(), "Lives should not drop again");
    }
}
