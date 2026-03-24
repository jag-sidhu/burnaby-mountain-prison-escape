package Spring2026Team10;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;



public class TestGuard {

    private PrisonMap map;

    @BeforeEach
    public void setUp() {
        map = new PrisonMap();
    }

    @Test
    public void testGuardConstructorSetsPatrolStateForPatrolType() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        assertEquals(Guard.GuardState.PATROLLING, guard.getState(),
                "PATROL type guard should start in PATROLLING state");
    }

    @Test
    public void testGuardConstructorSetsChaseStateForChaseType() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.CHASE, true, 10);
        assertEquals(Guard.GuardState.CHASING, guard.getState(),
                "CHASE type guard should start in CHASING state");
    }

    @Test
    public void testGuardSpawnsAtCorrectPosition() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        assertEquals(50, guard.getX(), "Guard x position does not match spawn");
        assertEquals(35, guard.getY(), "Guard y position does not match spawn");
    }

    @Test
    public void testIsAlertStateReturnsTrueWhenChasing() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.setState(Guard.GuardState.CHASING);
        assertTrue(guard.isAlertState(), "Guard should be alert when chasing");
    }

    @Test
    public void testIsAlertStateReturnsFalseWhenPatrolling() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.setState(Guard.GuardState.PATROLLING);
        assertFalse(guard.isAlertState(), "Guard should not be alert when patrolling");
    }

    @Test
    public void testResetRestoresPositionAndState() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.setState(Guard.GuardState.CHASING);
        guard.reset();
        assertEquals(50, guard.getX(), "Guard x should reset to spawn x");
        assertEquals(35, guard.getY(), "Guard y should reset to spawn y");
        assertEquals(Guard.GuardState.PATROLLING, guard.getState(),
                "Guard state should reset to PATROLLING");
    }

    @Test
    public void testGuardDoesNotMoveWhenIdle() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.setState(Guard.GuardState.IDLE);
        Player player = new Player(52, 35, map);

        double initialPosX = guard.getPosX();
        double initialPosY = guard.getPosY();

        for (int i = 0; i < 10; i++) {
            guard.update(player);
        }

        assertEquals(initialPosX, guard.getPosX(), 0.0001, "Idle guard x should not change");
        assertEquals(initialPosY, guard.getPosY(), 0.0001, "Idle guard y should not change");
    }

    @Test
    public void testPlayerLosesLifeWhenGuardCatchesThem() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.setState(Guard.GuardState.CHASING);
        Player player = new Player(50, 35, map);
        int livesBefore = player.getLives();

        guard.update(player);

        assertTrue(player.getLives() < livesBefore,
                "Player should lose a life when caught by guard");
    }

    @Test
    public void testPlayerDoesNotLoseLifeWhileInvulnerable() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.setState(Guard.GuardState.CHASING);
        Player player = new Player(50, 35, map);

        // first hit triggers invulnerability
        guard.update(player);
        int livesAfterFirstHit = player.getLives();

        // second hit should not reduce lives
        guard.update(player);

        assertEquals(livesAfterFirstHit, player.getLives(),
                "Player should not lose another life while invulnerable");
    }

    @Test
    public void testSpawnRandomGuardProducesWalkablePosition() {
        PrisonMap map = new PrisonMap();
        Guard guard = Guard.spawnRandomGuard(
                map, Guard.GuardType.PATROL, new java.util.ArrayList<>(), null,
                true, 10, 0, 0, 3, 2);

        assertTrue(map.isWalkable(guard.getY(), guard.getX()),
                "Spawned guard should be on a walkable tile");
    }

    @Test
    public void testSpawnRandomGuardDoesNotSpawnOnPlayer() {
        PrisonMap map = new PrisonMap();
        Player player = new Player(50, 35, map);

        for (int i = 0; i < 10; i++) {
            Guard guard = Guard.spawnRandomGuard(
                    map, Guard.GuardType.PATROL, new java.util.ArrayList<>(), player,
                    true, 10, 1, 0, 3, 2);
            assertFalse(guard.getX() == player.getX() && guard.getY() == player.getY(),
                    "Guard should not spawn on the player");
        }
    }

    @Test
    public void testSetAgroRangeAffectsChaseTriggering() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.setAgroRange(1);
        Player player = new Player(55, 35, map);

        for (int i = 0; i < 10; i++) {
            guard.update(player);
        }

        assertEquals(Guard.GuardState.PATROLLING, guard.getState(),
                "Guard with small agro range should not chase player far away");
    }

    @Test
    public void testGuardStaysOnWalkableTilesWhilePatrolling() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        Player player = new Player(80, 35, map); // far away so no chase

        for (int i = 0; i < 50; i++) {
            guard.update(player);
            assertTrue(map.isWalkable(guard.getY(), guard.getX()),
                    "Guard should always be on a walkable tile");
        }
    }

    @Test
    public void testGuardTransitionsToReturningWhenTooFarFromHome() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.setState(Guard.GuardState.CHASING);
        Player player = new Player(90, 35, map); // very far away

        for (int i = 0; i < 200; i++) {
            guard.update(player);
            if (guard.getState() == Guard.GuardState.RETURNING) break;
        }

        assertEquals(Guard.GuardState.RETURNING, guard.getState(),
                "Guard should transition to RETURNING when too far from home");
    }

    @Test
    public void testGuardDoesNotChaseWhenPlayerIsFarAway() {
        PrisonMap map = new PrisonMap();
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.setAgroRange(3);
        Player player = new Player(70, 35, map); // far outside agro range

        for (int i = 0; i < 10; i++) {
            guard.update(player);
        }

        assertEquals(Guard.GuardState.PATROLLING, guard.getState(),
                "Guard should remain PATROLLING when player is out of agro range");
    }

    @Test
    public void testIsAlertStateReturnsTrueWhenReturning() {
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.setState(Guard.GuardState.RETURNING);
        assertTrue(guard.isAlertState(), "Guard should be alert when returning");
    }

    @Test
    public void testIsAlertStateReturnsFalseWhenIdle() {
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.setState(Guard.GuardState.IDLE);
        assertFalse(guard.isAlertState(), "Guard should not be alert when idle");
    }

    @Test
    public void testResetRestoresPrecisePosition() {
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.reset();
        assertEquals(50, guard.getPosX(), 0.0001, "Guard posX should reset to spawn x");
        assertEquals(35, guard.getPosY(), 0.0001, "Guard posY should reset to spawn y");
    }

    @Test
    public void testGuardTransitionsToChaseWhenPlayerIsClose() {
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        guard.setAgroRange(10);
        Player player = new Player(52, 35, map);

        for (int i = 0; i < 10; i++) {
            guard.update(player);
        }

        assertEquals(Guard.GuardState.CHASING, guard.getState(),
                "Guard should switch to CHASING when player is nearby with line of sight");
    }

    @Test
    public void testGuardMovesWhilePatrolling() {
        Guard guard = new Guard(50, 35, map, Guard.GuardType.PATROL, true, 10);
        Player player = new Player(80, 35, map); // far away so no chase
        double initialPosX = guard.getPosX();

        for (int i = 0; i < 10; i++) {
            guard.update(player);
        }

        assertNotEquals(initialPosX, guard.getPosX(), 0.0001,
                "Guard should move while patrolling");
    }

    @Test
    public void testSpawnRandomGuardDoesNotOverlapExistingGuards() {
        java.util.List<Guard> guards = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int zoneCol = i % 3;
            int zoneRow = i / 3;
            Guard guard = Guard.spawnRandomGuard(
                    map, Guard.GuardType.PATROL, guards, null,
                    true, 10, zoneCol, zoneRow, 3, 2);
            for (Guard existing : guards) {
                assertFalse(guard.getX() == existing.getX() && guard.getY() == existing.getY(),
                        "Guards should not spawn on top of each other");
            }
            guards.add(guard);
        }
    }

    @Test
    public void testSpawnRandomGuardReturnsPatrolType() {
        Guard guard = Guard.spawnRandomGuard(
                map, Guard.GuardType.PATROL, new java.util.ArrayList<>(), null,
                true, 10, 0, 0, 3, 2);
        assertEquals(Guard.GuardType.PATROL, guard.getType(),
                "Spawned guard should have PATROL type");
    }

    @Test
    public void testGameResetSpawnsCorrectNumberOfGuardsForEachDifficulty() {
        MapPanel panel = new MapPanel(map);
        Game game = new Game(panel);

        while (!game.getDifficulty().equals(Game.Difficulty.EASY)) {
            game.decreaseDifficulty();
        }
        game.startMatch();
        assertEquals(GameState.PLAYING, game.getState(),
                "Game should be PLAYING after startMatch on EASY");

        game.increaseDifficulty();
        game.startMatch();
        assertEquals(GameState.PLAYING, game.getState(),
                "Game should be PLAYING after startMatch on MEDIUM");

        game.increaseDifficulty();
        game.startMatch();
        assertEquals(GameState.PLAYING, game.getState(),
                "Game should be PLAYING after startMatch on HARD");
    }
}
