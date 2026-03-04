package Spring2026Team10;

import java.util.Random;
import java.util.List;

public class Guard extends Entity {

    public enum GuardType {
        PATROL, CHASE
    }

    public enum GuardState {
        IDLE, // standing still
        PATROLLING, // moving along path
        CHASING, // following player
        RETURNING // going back to patrol after player moves too far away
    }

    private GuardType type;
    private GuardState state;

    //patrol info
    private int patrolStartX, patrolStartY;
    private int patrolEndX, patrolEndY;
    private boolean patrolForward = true; // direction along patrol path
    private boolean horizontalPatrol; // true = horizontal, false = vertical

    //chase info
    private int agroRange = 5; // number of tiles player must be within to trigger chase

    /**
     * spawns a guard at a random walkable tile on the map
     * makes sure the guard doesn't spawn on the player or other guards
     *
     * @param map              the prison map
     * @param type             the guard type (PATROL or CHASE)
     * @param existingGuards   list of already spawned guards to avoid overlap
     * @param player           the player to avoid spawning on
     * @param horizontalPatrol true for horizontal patrol, false for vertical
     * @param patrolLength     number of tiles in the patrol path
     * @return a new Guard at a valid random position
     */
    public static Guard spawnRandomGuard(PrisonMap map, GuardType type, List<Guard> existingGuards, Player player, boolean horizontalPatrol, int patrolLength) {
        Random rand = new Random();
        int x, y;
        boolean validSpawn;

        do {
            x = rand.nextInt(map.getCols());
            y = rand.nextInt(map.getRows());

            validSpawn = map.isWalkable(y, x);

            //prevents spawning on the player
            if (player != null && player.getX() == x && player.getY() == y) {
                validSpawn = false;
            }
            //makes sure guards don't spawn on other guards
            if (existingGuards != null) {
                for (Guard g : existingGuards) {
                    if (g.getX() == x && g.getY() == y) {
                        validSpawn = false;
                        break;
                    }
                }
            }

        } while (!validSpawn);

        return new Guard(x, y, map, type, horizontalPatrol, patrolLength);
    }

    /**
     * constructs a Guard at the given position
     *
     * @param startX           starting column
     * @param startY           starting row
     * @param map              the prison map
     * @param type             the guard type (PATROL or CHASE)
     * @param horizontalPatrol true for horizontal patrol, false for vertical
     * @param patrolLength     number of tiles in the patrol path
     */
    public Guard(int startX, int startY, PrisonMap map, GuardType type, boolean horizontalPatrol, int patrolLength) {
        super(startX, startY, map);
        this.type = type;
        this.state = (type == GuardType.PATROL) ? GuardState.PATROLLING : GuardState.CHASING;

        this.patrolStartX = startX;
        this.patrolStartY = startY;
        this.horizontalPatrol = horizontalPatrol;

        //calculate patrol end points based on patrol length and map boundaries
        if (horizontalPatrol) {
            patrolEndX = startX;
            for (int i = 1; i <= patrolLength; i++) {
                if (map.isWalkable(startY, startX + i))
                    patrolEndX = startX + i;
                else break;
            }
            patrolEndY = startY;
        } else {
            patrolEndY = startY;
            for (int i = 1; i <= patrolLength; i++) {
                if (map.isWalkable(startY + i, startX))
                    patrolEndY = startY + i;
                else break;
            }
            patrolEndX = startX;
        }
    }

    /**
     * updates the guard's state and position each frame
     * handles transitions between patrolling, chasing, and returning states.
     *
     * @param player the player entity to track
     */
    public void update(Player player) {
        switch (state) {
            case PATROLLING -> {
                int distanceToPlayer =
                        Math.abs(player.getX() - x)
                                + Math.abs(player.getY() - y);

                if (distanceToPlayer <= agroRange)
                    state = GuardState.CHASING;
                else
                    patrolMove();
            }
            case CHASING -> chasePlayer(player);
            case RETURNING -> returnToPatrol();
            case IDLE -> {}
        }
    }

    /**
     * moves the guard along its patrol path, reversing direction at its endpoints
     */
    private void patrolMove() {
        int dx = 0, dy = 0;

        if (horizontalPatrol) {
            dx = patrolForward ? 1 : -1;
            if (!map.isWalkable(y, x + dx)) dx = 0;

            if (x + dx > patrolEndX) patrolForward = false;
            else if (x + dx < patrolStartX) patrolForward = true;
        } else {
            dy = patrolForward ? 1 : -1;
            if (!map.isWalkable(y + dy, x)) dy = 0;

            if (y + dy > patrolEndY) patrolForward = false;
            else if (y + dy < patrolStartY) patrolForward = true;
        }

        move(dx, dy);
    }

    /**
     * chases the player if within agro range
     * causes the player to lose a life if the guard catches them
     * transitions to RETURNING if the player moves out of range
     *
     * @param player the player to chase
     */
    private void chasePlayer(Player player) {
        int distanceToPlayer =
                Math.abs(player.getX() - x)
                        + Math.abs(player.getY() - y);

        if (distanceToPlayer > agroRange) {
            state = GuardState.RETURNING;
            return;
        }

        //catch the player if on the same tile
        if (x == player.getX() && y == player.getY()) {
            player.loseLife();
            return;
        }

        int dx = 0, dy = 0;

        if (player.getX() != x)
            dx = (player.getX() > x) ? 1 : -1;
        else if (player.getY() != y)
            dy = (player.getY() > y) ? 1 : -1;

        if (!map.isWalkable(y, x + dx)) dx = 0;
        if (!map.isWalkable(y + dy, x)) dy = 0;

        move(dx, dy);
    }

    /**
     * moves the guard back to its patrol start position
     * transitions to PATROLLING once the start position is reached
     */
    private void returnToPatrol() {
        int dx = 0, dy = 0;

        if (x < patrolStartX) dx = 1;
        else if (x > patrolStartX) dx = -1;

        if (y < patrolStartY) dy = 1;
        else if (y > patrolStartY) dy = -1;

        if (!map.isWalkable(y, x + dx)) dx = 0;
        if (!map.isWalkable(y + dy, x)) dy = 0;

        move(dx, dy);

        if (x == patrolStartX && y == patrolStartY) {
            state = GuardState.PATROLLING;
            patrolForward = true;
        }
    }

    /**
     * resets the guard to its spawn position and patrolling state
     * should be called when the game is reset
     */
    public void reset() {
        x = patrolStartX;
        y = patrolStartY;
        posX = patrolStartX;
        posY = patrolStartY;
        state = GuardState.PATROLLING;
        patrolForward = true;
    }

    /**
     * sets the agro range of the guard, which determines how close the player must be to trigger a chase
     *
     * @param range number of tiles the player must be within to trigger a chase
     */
    public void setAgroRange(int range) {
        this.agroRange = range;
    }

    //getters
    public GuardType getType() { return type; }
    public GuardState getState() { return state; }
    public void setState(GuardState state) { this.state = state; }

    public boolean isAlertState() {
        return state == GuardState.CHASING
                || state == GuardState.RETURNING;
    }
}