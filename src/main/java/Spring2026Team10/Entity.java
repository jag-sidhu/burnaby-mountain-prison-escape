package Spring2026Team10;

public abstract class Entity {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    //main entity logic, player and guard will extend this
    //tile position
    protected int x; //column
    protected int y; //row

    protected double posX; //for smoother movement
    protected double posY; //for smoother movement

    protected double speed = 0.25; // default tiles per frame (reduced for slower movement)

    protected PrisonMap map;
    protected Direction facing = Direction.DOWN;
    protected boolean moving;

    public Entity(int startX, int startY, PrisonMap map) {
        this.x = startX;
        this.y = startY;
        this.posX = startX;
        this.posY = startY;
        this.map = map;
    }
    //getter methods so that only Guard and Player can change the x and y values
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

    //movement
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
        double remainingX = dx * speed;
        double remainingY = dy * speed;

        // Move step by step in small increments (0.1 tile increments)
        double step = 0.1;
        while (Math.abs(remainingX) > 0 || Math.abs(remainingY) > 0) {
            double stepX = Math.min(step, Math.abs(remainingX)) * Math.signum(remainingX);
            double stepY = Math.min(step, Math.abs(remainingY)) * Math.signum(remainingY);

            double newPosX = posX + stepX;
            double newPosY = posY + stepY;

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
