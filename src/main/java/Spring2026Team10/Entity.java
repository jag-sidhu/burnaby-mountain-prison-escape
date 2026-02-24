package Spring2026Team10;

public abstract class Entity {
    //main entity logic, player and guard will extend this
    protected int x; //column
    protected int y; //row

    protected int speed = 1; // default speed can be overwritten by subclass, 1 for 1 tile

    protected PrisonMap map;

    public Entity(int StartX, int StartY, PrisonMap map) {
        this.x = StartX;
        this.y = StartY;
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
    public void move(int dx, int dy) {
        int newX = x + dx * speed;
        int newY = y + dy * speed;

        if (map.isWalkable(newX,newY)) {
            this.x = newX;
            this.y = newY;
        }
    }

}
