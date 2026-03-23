package Spring2026Team10;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers status effects and pickup behavior introduced through hazards and powerups.
 */
public class TestHazardsAndPowerups {

    @Test
    public void testHandcuffsPenaltyBlocksPowerupPickupUntilEffectExpires() {
        PrisonMap map = new PrisonMap();
        Player player = createPlayerOnOpenTile(map, 2, 6);
        Hazard handcuffs = new Hazard(player.getX(), player.getY(), HazardType.HANDCUFFS);
        Powerups coffee = new Powerups(player.getX(), player.getY(), PowerupType.COFFEE);

        handcuffs.applyTo(player);

        assertEquals(-10, player.getScore(), "Handcuffs should deduct their score penalty");
        assertFalse(handcuffs.isActive(), "Handcuffs should deactivate after being triggered");
        assertTrue(player.isHandsTied(), "Handcuffs should tie the player's hands");
        assertEquals(Player.StatusState.HANDS_TIED, player.getStatusState(), "Player status should show hands tied");

        coffee.applyTo(player);
        assertEquals(-10, player.getScore(), "Blocked powerup pickup should not change score");
        assertTrue(coffee.isActive(), "Blocked powerup should remain available");

        advanceFrames(player, 90);

        assertFalse(player.isHandsTied(), "Handcuff effect should expire after its timer");
        assertEquals(Player.StatusState.NORMAL, player.getStatusState(), "Status should return to normal");

        coffee.applyTo(player);
        assertEquals(15, player.getScore(), "Coffee should add its score bonus once hands are free");
        assertFalse(coffee.isActive(), "Coffee should deactivate after a successful pickup");
    }

    @Test
    public void testParkingTicketSlowdownReducesMovementAndExpires() {
        PrisonMap map = new PrisonMap();
        Point lane = findWalkableHorizontalRun(map, 0, 8);
        assertNotNull(lane, "A clear movement lane should exist for slowdown testing");

        Player baselinePlayer = new Player(lane.x, lane.y, map);
        Player slowedPlayer = new Player(lane.x, lane.y, map);
        KeyHandler keyHandler = new KeyHandler();
        keyHandler.rightPressed = true;

        baselinePlayer.update(keyHandler);
        double baselineDelta = baselinePlayer.getPosX() - lane.x;

        Hazard ticket = new Hazard(lane.x, lane.y, HazardType.PARKING_TICKET);
        ticket.applyTo(slowedPlayer);
        slowedPlayer.update(keyHandler);
        double slowedDelta = slowedPlayer.getPosX() - lane.x;

        assertEquals(-20, slowedPlayer.getScore(), "Parking ticket should deduct its score penalty");
        assertEquals(Player.StatusState.SLOWED, slowedPlayer.getStatusState(), "Parking ticket should apply the slowed status");
        assertTrue(slowedDelta < baselineDelta, "Slowed movement should be shorter than normal movement");

        advanceFrames(slowedPlayer, 150);

        assertEquals(Player.StatusState.NORMAL, slowedPlayer.getStatusState(), "Slowdown should expire back to normal");

        double beforeMove = slowedPlayer.getPosX();
        slowedPlayer.update(keyHandler);
        assertEquals(0.25, slowedPlayer.getPosX() - beforeMove, 0.0001, "Normal speed should return after slowdown expires");
    }

    @Test
    public void testSpoiledMilkInvertsControlsUntilTimerExpires() {
        PrisonMap map = new PrisonMap();
        Point lane = findWalkableHorizontalRun(map, 3, 3);
        assertNotNull(lane, "A lane with space on both sides should exist for inverted controls");

        Player player = new Player(lane.x, lane.y, map);
        Hazard milk = new Hazard(lane.x, lane.y, HazardType.SPOILED_MILK);
        KeyHandler keyHandler = new KeyHandler();

        milk.applyTo(player);
        assertEquals(-15, player.getScore(), "Spoiled milk should deduct its score penalty");
        assertEquals(Player.StatusState.INVERTED_CONTROLS, player.getStatusState(), "Spoiled milk should invert controls");

        keyHandler.rightPressed = true;
        player.update(keyHandler);
        assertTrue(player.getPosX() < lane.x, "Pressing right while inverted should move the player left");

        keyHandler.clear();
        advanceFrames(player, 120);

        assertEquals(Player.StatusState.NORMAL, player.getStatusState(), "Inverted controls should expire");

        double beforeMove = player.getPosX();
        keyHandler.rightPressed = true;
        player.update(keyHandler);
        assertTrue(player.getPosX() > beforeMove, "Pressing right after expiry should move the player right");
    }

    @Test
    public void testBearDamageUsesSharedPlayerInvulnerability() {
        Player player = new Player(0, 0, new PrisonMap());
        Hazard firstBear = new Hazard(0, 0, HazardType.BEAR);
        Hazard secondBear = new Hazard(1, 0, HazardType.BEAR);
        Hazard thirdBear = new Hazard(2, 0, HazardType.BEAR);

        firstBear.applyTo(player);
        secondBear.applyTo(player);

        assertEquals(2, player.getLives(), "Only the first bear hit should remove a life during invulnerability");
        assertEquals(-100, player.getScore(), "Both bears should still apply their score penalties");
        assertTrue(player.isInvulnerable(), "Bear damage should trigger player invulnerability");

        advanceFrames(player, 60);
        thirdBear.applyTo(player);

        assertEquals(1, player.getLives(), "A later bear hit should remove another life after invulnerability expires");
    }

    @Test
    public void testCoffeeSpeedBoostIncreasesMovementThenReturnsToNormal() {
        PrisonMap map = new PrisonMap();
        Point lane = findWalkableHorizontalRun(map, 0, 8);
        assertNotNull(lane, "A clear lane should exist for speed testing");

        Player player = new Player(lane.x, lane.y, map);
        Powerups coffee = new Powerups(lane.x, lane.y, PowerupType.COFFEE);
        KeyHandler keyHandler = new KeyHandler();
        keyHandler.rightPressed = true;

        coffee.applyTo(player);
        assertEquals(25, player.getScore(), "Coffee should grant its score bonus");
        assertFalse(coffee.isActive(), "Coffee should deactivate after pickup");

        double boostedStart = player.getPosX();
        player.update(keyHandler);
        assertEquals(0.5, player.getPosX() - boostedStart, 0.0001, "Coffee should double the player's movement speed");

        advanceFrames(player, 150);

        double normalStart = player.getPosX();
        player.update(keyHandler);
        assertEquals(0.25, player.getPosX() - normalStart, 0.0001, "Speed should return to normal after the boost expires");
    }

    @Test
    public void testSnowflakeFreezesGuardsInGameUntilEffectEnds() {
        PrisonMap map = new PrisonMap();
        MapPanel panel = new MapPanel(map);
        Game game = new SilentGame(panel);
        game.startMatch();

        Player player = getPlayer(game);
        List<Guard> guards = getGuards(game);
        assertFalse(guards.isEmpty(), "A started match should spawn guards");

        Guard guard = guards.get(0);
        double startX = guard.getPosX();
        double startY = guard.getPosY();

        Powerups snowflake = new Powerups(player.getX(), player.getY(), PowerupType.SNOWFLAKE);
        snowflake.applyTo(player);

        for (int i = 0; i < 4; i++) {
            game.update();
        }

        assertEquals(startX, guard.getPosX(), 0.0001, "Frozen guards should not move while the effect is active");
        assertEquals(startY, guard.getPosY(), 0.0001, "Frozen guards should not move while the effect is active");

        for (int i = 0; i < 10; i++) {
            game.update();
        }
        assertTrue(player.isGuardsFrozen(), "Freeze effect should still be active after a short duration");

        for (int i = 0; i < 150; i++) {
            game.update();
            if (guard.getPosX() != startX || guard.getPosY() != startY) {
                break;
            }
        }

        assertFalse(player.isGuardsFrozen(), "Freeze effect should expire after its timer");
        assertTrue(guard.getPosX() != startX || guard.getPosY() != startY, "Guard should move again once freeze expires");
    }

    @Test
    public void testDoctorsNoteRestoresLifeButNeverAboveThree() {
        Player player = new Player(0, 0, new PrisonMap());
        player.setLives(2);

        Powerups firstNote = new Powerups(0, 0, PowerupType.DOCTORS_NOTE);
        firstNote.applyTo(player);

        assertEquals(3, player.getLives(), "Doctor's note should restore one life");
        assertEquals(40, player.getScore(), "Doctor's note should grant its score bonus");

        Powerups secondNote = new Powerups(1, 0, PowerupType.DOCTORS_NOTE);
        secondNote.applyTo(player);

        assertEquals(3, player.getLives(), "Lives should stay capped at three");
        assertEquals(80, player.getScore(), "Additional doctor's notes should still award score");
    }

    private Player createPlayerOnOpenTile(PrisonMap map, int leftSpace, int rightSpace) {
        Point tile = findWalkableHorizontalRun(map, leftSpace, rightSpace);
        assertNotNull(tile, "A suitable walkable tile should exist for the test");
        return new Player(tile.x, tile.y, map);
    }

    private Point findWalkableHorizontalRun(PrisonMap map, int leftSpace, int rightSpace) {
        for (int row = 1; row < map.getRows() - 1; row++) {
            for (int col = leftSpace + 1; col < map.getCols() - rightSpace - 1; col++) {
                if (!map.isWalkable(row, col)) {
                    continue;
                }

                boolean clear = true;
                for (int i = 1; i <= leftSpace; i++) {
                    if (!map.isWalkable(row, col - i)) {
                        clear = false;
                        break;
                    }
                }
                for (int i = 1; clear && i <= rightSpace; i++) {
                    if (!map.isWalkable(row, col + i)) {
                        clear = false;
                    }
                }

                if (clear) {
                    return new Point(col, row);
                }
            }
        }
        return null;
    }

    private void advanceFrames(Player player, int frames) {
        KeyHandler keyHandler = new KeyHandler();
        for (int i = 0; i < frames; i++) {
            player.update(keyHandler);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Guard> getGuards(Game game) {
        try {
            Field field = Game.class.getDeclaredField("guards");
            field.setAccessible(true);
            return (List<Guard>) field.get(game);
        } catch (ReflectiveOperationException e) {
            fail("Unable to access the game guards for testing");
            return List.of();
        }
    }

    private Player getPlayer(Game game) {
        try {
            Field field = Game.class.getDeclaredField("player");
            field.setAccessible(true);
            return (Player) field.get(game);
        } catch (ReflectiveOperationException e) {
            fail("Unable to access the game player for testing");
            return null;
        }
    }

    private static class SilentGame extends Game {
        SilentGame(MapPanel mapPanel) {
            super(mapPanel);
        }

        @Override
        public void playMusic() {
        }

        @Override
        public void stopMusic() {
        }

        @Override
        public void playSoundEffect(int i) {
        }

        @Override
        public void exitGame() {
        }
    }
}
