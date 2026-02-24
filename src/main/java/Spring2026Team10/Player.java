package Spring2026Team10;

public class Player extends Entity {

    public Player(int startX, int startY, PrisonMap map) {
        super(startX, startY, map);
    }

    public void update(KeyHandler keyHandler) {
        //currently can't move diagonally on the tiles, can change it if we want player to be able to do so
        if (keyHandler.upPressed) {
            move(0, -1);
        } else if (keyHandler.downPressed) {
            move(0, 1);
        } else if (keyHandler.leftPressed) {
            move(-1, 0);
        } else if (keyHandler.rightPressed) {
            move(1, 0);
        }

    }

    public void reset() {
        this.x = map.getStartTile().x;
        this.y = map.getStartTile().y;
    }
}
