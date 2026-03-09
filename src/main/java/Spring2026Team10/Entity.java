package Spring2026Team10;

/**
 * abstract class for all entities in the game (player and guard)
 * handles movement and position logic
 */
public abstract class Entity {
    /**
     * the four directions an entity can face and move in
     */
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    //tile position
    protected int x; //column
    protected int y; //row

    //precise position for smoother movement
    protected double posX;
    protected double posY;

    /**
     * movement speed in tiles per frame
     */
    protected double speed = 0.25;

    protected PrisonMap map;
    protected Direction facing = Direction.DOWN;
    protected boolean moving;
    
    /**
     * constructor for entity, initializes position and map reference
     * @param startX
     * @param startY
     * @param map
     */
    public Entity(int startX, int startY, PrisonMap map) {
        this.x = startX;
        this.y = startY;
        this.posX = startX;
        this.posY = startY;
        this.map = map;
    }
    
    /**
     * getter methods for position and state
     * @return integer column index, integer row index, double precise x position, double precise y position, direction facing, boolean moving
     */
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public double getPosX() {
        return posX;
    }
    public double getPosY() {
        return posY;
    }

    public Direction getFacing() {
        return facing;
    }

    public boolean isMoving() {
        return moving;
    }

    /**
     * moves the entity by a certain amount in the x and y directions, checking for collisions with walls
     * movement is done in small increments to allow for smooth movement and accurate collision detection
     * @param dx horizontal direction
     * @param dy vertical direction
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

        //scale movement by speed
        double remainingX = dx * speed;
        double remainingY = dy * speed;

        //move step by step in small increments (0.1 tile increments)
        double step = 0.1;
        while (Math.abs(remainingX) > 0 || Math.abs(remainingY) > 0) {
            double stepX = Math.min(step, Math.abs(remainingX)) * Math.signum(remainingX);
            double stepY = Math.min(step, Math.abs(remainingY)) * Math.signum(remainingY);

            double newPosX = posX + stepX;
            double newPosY = posY + stepY;
            
            //checks if the new position is walkable (not a wall) before moving there, if it's not walkable it stops movement in that direction
            if (map.isWalkable((int)newPosY, (int)newPosX)) {
                posX = newPosX;
                posY = newPosY;
                x = (int) posX;
                y = (int) posY;
            } else {
                break; // stop at wall
            }

            remainingX -= stepX;
            remainingY -= stepY;
        }

        moving = Math.abs(posX - startX) > 0.0001 || Math.abs(posY - startY) > 0.0001;
    }
}
