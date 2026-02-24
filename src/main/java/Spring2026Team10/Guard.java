package Spring2026Team10;

public class Guard extends Entity {

    public enum GuardType {
        PATROL, CHASE
    }

    public enum GuardState {
        IDLE, // standing still
        PATROLLING, // moving along patrol path
        CHASING, // following player, can't move diagonally
        RETURNING // going back to patrol start after player moves too far
    }

    private GuardType type;
    private GuardState state;

    //patrol info
    private int patrolStartX, patrolStartY;
    private int patrolEndX, patrolEndY;
    private boolean patrolForward = true; // direction along patrol path
    private boolean horizontalPatrol;     // true = horizontal, false = vertical

    //chase info
    private int agroRange = 5; // number of tiles player must be within to trigger chase

    public Guard(int startX, int startY, PrisonMap map, GuardType type, boolean horizontalPatrol, int patrolLength) {
        super(startX, startY, map);
        this.type = type;
        this.state = (type == GuardType.PATROL) ? GuardState.PATROLLING : GuardState.IDLE;

        this.patrolStartX = startX;
        this.patrolStartY = startY;
        this.horizontalPatrol = horizontalPatrol;

        //calculate patrol end points based on patrol length and map boundaries
        if (horizontalPatrol) {
            patrolEndX = startX;
            for (int i = 1; i <= patrolLength; i++) {
                if (map.isWalkable(startY, startX + i)) patrolEndX = startX + i;
                else break;
            }
            patrolEndY = startY;
        } else {
            patrolEndY = startY;
            for (int i = 1; i <= patrolLength; i++) {
                if (map.isWalkable(startY + i, startX)) patrolEndY = startY + i;
                else break;
            }
            patrolEndX = startX;
        }
    }

    public void update(Player player) {
        switch (state) {
            case PATROLLING -> {
                // check if player is within agro range
                int distanceToPlayer = Math.abs(player.getX() - patrolStartX) + Math.abs(player.getY() - patrolStartY);
                if (distanceToPlayer <= agroRange) {
                    state = GuardState.CHASING;
                } else {
                    patrolMove();
                }
            }
            case CHASING -> chasePlayer(player);
            case RETURNING -> returnToPatrol();
            case IDLE -> {} // do nothing
        }
    }

    private void patrolMove() {
        int dx = 0, dy = 0;

        if (horizontalPatrol) {
            dx = patrolForward ? 1 : -1;
            if (!map.isWalkable(y, x + dx)) dx = 0;
            if (x + dx > patrolEndX) patrolForward = false;
            else if (x + dx < patrolStartX) patrolForward = true;
        } else { // vertical patrol
            dy = patrolForward ? 1 : -1;
            if (!map.isWalkable(y + dy, x)) dy = 0;
            if (y + dy > patrolEndY) patrolForward = false;
            else if (y + dy < patrolStartY) patrolForward = true;
        }

        move(dx, dy);
    }

    private void chasePlayer(Player player) {
        //stop chasing if player is outside agro range, determined by how far the player is from the guard's patrol start point (not current position, so guard doesn't lose player if they chase them a bit)
        int distanceToPlayer = Math.abs(player.getX() - patrolStartX) + Math.abs(player.getY() - patrolStartY);
        if (distanceToPlayer > agroRange) {
            state = GuardState.RETURNING;
            return;
        }

        int dx = 0, dy = 0;

        //non diagonal chase, prioritize horizontal first
        if (player.getX() != x) dx = (player.getX() > x) ? 1 : -1;
        else if (player.getY() != y) dy = (player.getY() > y) ? 1 : -1;

        // check walls
        if (!map.isWalkable(y, x + dx)) dx = 0;
        if (!map.isWalkable(y + dy, x)) dy = 0;

        move(dx, dy);
    }

    private void returnToPatrol() {
        int dx = 0, dy = 0;

        if (x < patrolStartX) dx = 1;
        else if (x > patrolStartX) dx = -1;

        if (y < patrolStartY) dy = 1;
        else if (y > patrolStartY) dy = -1;

        // check walls
        if (!map.isWalkable(y, x + dx)) dx = 0;
        if (!map.isWalkable(y + dy, x)) dy = 0;

        move(dx, dy);

        if (x == patrolStartX && y == patrolStartY) {
            state = GuardState.PATROLLING;
            patrolForward = true; // reset patrol direction when returning
        }
    }

    //getters & setters
    public GuardType getType() { return type; }
    public GuardState getState() { return state; }
    public void setState(GuardState state) { this.state = state; }
}