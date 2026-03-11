package Spring2026Team10;

import java.util.Random;
import java.util.List;

public class Guard extends Entity {

    /**
     * Defines the primary behavior mode for the guard (ex: whether they spawn to patrol or chase).
     */
    public enum GuardType {
        PATROL, CHASE
    }

    /**
     * Defines the current momentary action state of the guard.
     */
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
    private int chaseRange = 25; // max tiles guard will chase before giving up
    private int[] cachedStep = new int[]{0, 0};
    private int pathUpdateTimer = 0;
    private static final int PATH_UPDATE_INTERVAL = 5; // recalculate path every 5 frames

    /**
     * checks if a position has enough open space to patrol
     * returns true if the guard can walk at least minDistance tiles in any direction
     */
    private static boolean hasPatrolSpace(PrisonMap map, int x, int y, int minDistance) {
        int right = 0, left = 0, down = 0, up = 0;
        for (int i = 1; i <= minDistance; i++) {
            if (map.isWalkable(y, x + i)) right++;
            else break;
        }
        for (int i = 1; i <= minDistance; i++) {
            if (map.isWalkable(y, x - i)) left++;
            else break;
        }
        for (int i = 1; i <= minDistance; i++) {
            if (map.isWalkable(y + i, x)) down++;
            else break;
        }
        for (int i = 1; i <= minDistance; i++) {
            if (map.isWalkable(y - i, x)) up++;
            else break;
        }
        return right >= minDistance || left >= minDistance 
            || down >= minDistance || up >= minDistance;
    }

    /**
     * determines the best patrol direction based on available space at spawn
     * returns true if horizontal patrol has more space, false if vertical is better
     */
    private static boolean bestPatrolDirection(PrisonMap map, int x, int y) {
        int right = 0, left = 0, down = 0, up = 0;
        for (int i = 1; i <= 20; i++) {
            if (map.isWalkable(y, x + i)) right++; else break;
        }
        for (int i = 1; i <= 20; i++) {
            if (map.isWalkable(y, x - i)) left++; else break;
        }
        for (int i = 1; i <= 20; i++) {
            if (map.isWalkable(y + i, x)) down++; else break;
        }
        for (int i = 1; i <= 20; i++) {
            if (map.isWalkable(y - i, x)) up++; else break;
        }
        int horizontal = Math.max(right, left);
        int vertical = Math.max(down, up);
        return horizontal >= vertical;
    }

    /**
     * checks if the guard has a clear line of sight to the player
     * returns false if there is a wall between the guard and the player
     * ensures that the guard doesn't get stuck on wall if the player is just out of sight around a corner
     */
    private boolean hasLineOfSight(Player player) {
        int x0 = this.x, y0 = this.y;
        int x1 = player.getX(), y1 = player.getY();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (!map.isWalkable(y0, x0)) return false;
            if (x0 == x1 && y0 == y1) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
        return true;
    }

    /**
     * guard gets stuck on walls when trying to return to it's patrolling position, finds the next step toward a target position using breadth first search pathfinding
     * returns an int array {dx, dy} representing the next move direction
     * returns {0, 0} if no path is found
     */
    private int[] findNextStep(int targetX, int targetY) {
        if (x == targetX && y == targetY) return new int[]{0, 0};

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        boolean[][] visited = new boolean[map.getRows()][map.getCols()];
        int[][] parentX = new int[map.getRows()][map.getCols()];
        int[][] parentY = new int[map.getRows()][map.getCols()];

        for (int[] row : parentX) java.util.Arrays.fill(row, -1);
        for (int[] row : parentY) java.util.Arrays.fill(row, -1);

        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        queue.add(new int[]{x, y});
        visited[y][x] = true;

        boolean found = false;
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int cx = curr[0], cy = curr[1];

            if (cx == targetX && cy == targetY) {
                found = true;
                break;
            }

            for (int[] d : dirs) {
                int nx = cx + d[0];
                int ny = cy + d[1];
                if (map.isWalkable(ny, nx) && !visited[ny][nx]) {
                    visited[ny][nx] = true;
                    parentX[ny][nx] = cx;
                    parentY[ny][nx] = cy;
                    queue.add(new int[]{nx, ny});
                }
            }
        }

        if (!found) return new int[]{0, 0};

        // trace back to find first step
        int cx = targetX, cy = targetY;
        while (parentX[cy][cx] != x || parentY[cy][cx] != y) {
            int px = parentX[cy][cx];
            int py = parentY[cy][cx];
            cx = px;
            cy = py;
        }

        return new int[]{cx - x, cy - y};
    }

    /**
     * spawns a guard at a random walkable tile within a specific zone of the map.
     * ensures the guard does not spawn on the player or other guards.
     *
     * @param map              the prison map
     * @param type             the guard type (PATROL or CHASE)
     * @param existingGuards   list of already spawned guards to avoid overlap
     * @param player           the player to avoid spawning on
     * @param horizontalPatrol true for horizontal patrol, false for vertical
     * @param patrolLength     number of tiles in the patrol path
     * @param zoneCol          the column zone index
     * @param zoneRow          the row zone index
     * @param totalZoneCols    total number of column zones
     * @param totalZoneRows    total number of row zones
     * @return a new Guard at a valid random position within the zone
     */
    public static Guard spawnRandomGuard(PrisonMap map, GuardType type, List<Guard> existingGuards,
        Player player, boolean horizontalPatrol, int patrolLength,
        int zoneCol, int zoneRow, int totalZoneCols, int totalZoneRows) {

    Random rand = new Random();
    int x, y;
    boolean validSpawn;

    // calculate the bounds of this zone
    int zoneWidth = map.getCols() / totalZoneCols;
    int zoneHeight = map.getRows() / totalZoneRows;
    int margin = 10; // keep guards away from map edges
    int minX = Math.max(margin, zoneCol * zoneWidth);
    int maxX = Math.min(map.getCols() - margin, minX + zoneWidth);
    int minY = Math.max(margin, zoneRow * zoneHeight);
    int maxY = Math.min(map.getRows() - margin, minY + zoneHeight);

    int attempts = 0;
    do {
        x = minX + rand.nextInt(Math.max(1, maxX - minX));
        y = minY + rand.nextInt(Math.max(1, maxY - minY));

        validSpawn = map.isWalkable(y, x);

        // prevents spawning on the player
        if (player != null && player.getX() == x && player.getY() == y) {
            validSpawn = false;
        }
        // makes sure guards don't spawn on other guards
        if (existingGuards != null) {
            for (Guard g : existingGuards) {
                if (g.getX() == x && g.getY() == y) {
                    validSpawn = false;
                    break;
                }
            }
        }
        // make sure guard has enough space to patrol
        if (validSpawn && !hasPatrolSpace(map, x, y, 5)) {
            validSpawn = false;
        }
        attempts++;
        // if no valid spot found in zone after 200 attempts, expand to full map
        if (attempts > 200) {
            x = rand.nextInt(map.getCols());
            y = rand.nextInt(map.getRows());
            validSpawn = map.isWalkable(y, x) && hasPatrolSpace(map, x, y, 5);
        }

    } while (!validSpawn);

    boolean bestDirection = bestPatrolDirection(map, x, y);
    return new Guard(x, y, map, type, bestDirection, patrolLength);
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
        this.speed = 0.2; // guards move .2 tile per update
        this.patrolStartX = startX;
        this.patrolStartY = startY;
        this.horizontalPatrol = horizontalPatrol;

        //calculate patrol end points based on patrol length and map boundaries
        if (horizontalPatrol) {
            patrolEndX = startX;
            patrolEndY = startY;
            
            // try going right first
            for (int i = 1; i <= patrolLength; i++) {
                if (map.isWalkable(startY, startX + i))
                    patrolEndX = startX + i;
                else break;
            }
            
            // if we couldn't move right at all, try going left
            if (patrolEndX == startX) {
                for (int i = 1; i <= patrolLength; i++) {
                    if (map.isWalkable(startY, startX - i)) {
                        patrolStartX = startX - i;
                    } else break;
                }
                patrolEndX = startX;
                patrolForward = false; // start moving left
            }
        } else {
            patrolEndX = startX;
            patrolEndY = startY;
            
            // try going down first
            for (int i = 1; i <= patrolLength; i++) {
                if (map.isWalkable(startY + i, startX))
                    patrolEndY = startY + i;
                else break;
            }
            
            // if we couldn't move down at all, try going up
            if (patrolEndY == startY) {
                for (int i = 1; i <= patrolLength; i++) {
                    if (map.isWalkable(startY - i, startX)) {
                        patrolStartY = startY - i;
                    } else break;
                }
                patrolEndY = startY;
                patrolForward = false; // start moving up
            }
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
            
                if (distanceToPlayer <= agroRange && hasLineOfSight(player))
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
     * moves the guard along its patrol path, reversing direction at its endpoints or when hitting a wall
     */
    private void patrolMove() {
        int dx = 0, dy = 0;

        if (horizontalPatrol) {
            dx = patrolForward ? 1 : -1;

            // reverse if hitting a wall or reached endpoint
            if (!map.isWalkable(y, x + dx) || x + dx > patrolEndX || x + dx < patrolStartX) {
                patrolForward = !patrolForward;
                dx = patrolForward ? 1 : -1;
            }

            // final wall check after reversal
            if (!map.isWalkable(y, x + dx)) dx = 0;

        } else {
            dy = patrolForward ? 1 : -1;

            // reverse if hitting a wall or reached endpoint
            if (!map.isWalkable(y + dy, x) || y + dy > patrolEndY || y + dy < patrolStartY) {
                patrolForward = !patrolForward;
                dy = patrolForward ? 1 : -1;
            }

            // final wall check after reversal
            if (!map.isWalkable(y + dy, x)) dy = 0;
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
        int distanceFromHome =
            Math.abs(x - patrolStartX)
                    + Math.abs(y - patrolStartY);

        if (distanceFromHome > chaseRange) {
            state = GuardState.RETURNING;
            return;
        }

        // catch the player if on the same tile
        if (x == player.getX() && y == player.getY()) {
            player.loseLife();
            return;
        }

        pathUpdateTimer++;
        if (pathUpdateTimer >= PATH_UPDATE_INTERVAL) {
            cachedStep = findNextStep(player.getX(), player.getY());
            pathUpdateTimer = 0;
        }
        move(cachedStep[0], cachedStep[1]);
    }

    /**
     * moves the guard back to its patrol start position using pathfinding
     * transitions to PATROLLING once the start position is reached
     */
    private void returnToPatrol() {
        if (x == patrolStartX && y == patrolStartY) {
            state = GuardState.PATROLLING;
            patrolForward = true;
            return;
        }

        pathUpdateTimer++;
        if (pathUpdateTimer >= PATH_UPDATE_INTERVAL) {
            cachedStep = findNextStep(patrolStartX, patrolStartY);
            pathUpdateTimer = 0;
        }
        move(cachedStep[0], cachedStep[1]);
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

    /**
     * Gets the guard's behavior type.
     * @return The GuardType (PATROL or CHASE).
     */
    public GuardType getType() { return type; }

    /**
     * Gets the current action state of the guard.
     * @return The GuardState representing what the guard is currently doing.
     */
    public GuardState getState() { return state; }

    /**
     * Overrides the current state of the guard.
     * @param state The new GuardState to apply.
     */
    public void setState(GuardState state) { this.state = state; }

    /**
     * Checks if the guard is currently engaged in a hostile action (chasing or returning).
     * @return True if the guard is alert, false if idle or patrolling.
     */
    public boolean isAlertState() {
        return state == GuardState.CHASING
                || state == GuardState.RETURNING;
    }
}
