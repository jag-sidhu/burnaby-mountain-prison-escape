package Spring2026Team10;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestGuard {

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
}
