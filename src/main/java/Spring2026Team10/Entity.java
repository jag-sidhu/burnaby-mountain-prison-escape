package Spring2026Team10;

/**
 * Abstract class for all entities in the game (player and guard).
 * Handles movement and position logic.
 */
public abstract class Entity {
    /**
     * The four directions an entity can face and move in.
     */
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    // Tile position
    protected int x;
    protected int y;

    // Precise position for smoother movement
    protected double posX;
    protected double posY;

    /**
     * Movement speed in tiles per frame.
     */
    protected double speed = 0.25;

    protected PrisonMap map;
    protected Direction facing = Direction.DOWN;
    protected boolean moving;
    
    /**
     * Constructor for entity, initializes position and map reference.
     * @param startX The starting column on the map grid.
     * @param startY The starting row on the map grid.
     * @param map The prison map the entity exists within.
     */
    public Entity(int startX, int startY, PrisonMap map) {
        this.x = startX;
        this.y = startY;
        this.posX = startX;
        this.posY = startY;
        this.map = map;
    }
    
    /**
     * Gets the grid column index of the entity.
     * @return The x-coordinate on the grid.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the grid row index of the entity.
     * @return The y-coordinate on the grid.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the precise sub-grid x-coordinate for smooth rendering.
     * @return The precise double x-position.
     */
    public double getPosX() {
        return posX;
    }

    /**
     * Gets the precise sub-grid y-coordinate for smooth rendering.
     * @return The precise double y-position.
     */
    public double getPosY() {
        return posY;
    }

    /**
     * Gets the current direction the entity is facing.
     * @return The Direction enum representing the entity's orientation.
     */
    public Direction getFacing() {
        return facing;
    }

    /**
     * Checks if the entity is currently in motion.
     * @return True if moving, false if idle.
     */
    public boolean isMoving() {
        return moving;
    }

    /**
     * Moves the entity by a certain amount in the x and y directions, checking for collisions with walls.
     * Movement is done in small increments to allow for smooth movement and accurate collision detection.
     * @param dx Horizontal direction.
     * @param dy Vertical direction.
     */
    public void move(double dx, double dy) {
        if (dx > 0) {
            facing = Direction.RIGHT;
        } else if (dx < 0) {
            facing = Direction.LEFT;
        } else if (dy > 0) {
            facing = Direction.DOWN;
        } else if (dy < 0) {
            facing = Direction.UP;
        }

        double startX = posX;
        double startY = posY;

        // Scale movement by speed
        double remainingX = dx * speed;
        double remainingY = dy * speed;

        // Move step by step in small increments (0.1 tile increments)
        double step = 0.1;
        while (Math.abs(remainingX) > 0 || Math.abs(remainingY) > 0) {
            double stepX = Math.min(step, Math.abs(remainingX)) * Math.signum(remainingX);
            double stepY = Math.min(step, Math.abs(remainingY)) * Math.signum(remainingY);

            double newPosX = posX + stepX;
            double newPosY = posY + stepY;
            
            // Checks if the new position is walkable (not a wall) before moving there, if it isn't walkable it stops movement in that direction.
            if (map.isWalkable((int)newPosY, (int)newPosX)) {
                posX = newPosX;
                posY = newPosY;
                x = (int) posX;
                y = (int) posY;
            } else {
                break; // Stop at wall
            }

            remainingX -= stepX;
            remainingY -= stepY;
        }

        moving = Math.abs(posX - startX) > 0.0001 || Math.abs(posY - startY) > 0.0001;
    }
}
