package Spring2026Team10;

public abstract class Entity {
    //main entity logic, player and guard will extend this
    //tile position
    protected int x; //column
    protected int y; //row

    protected double posX; //for smoother movement
    protected double posY; //for smoother movement

    protected int speed = 1; // default number of tiles per frame

    protected PrisonMap map;

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

    //movement
    public void move(double dx, double dy) {
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
    }
}
