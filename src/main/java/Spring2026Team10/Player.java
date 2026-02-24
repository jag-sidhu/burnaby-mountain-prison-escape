package Spring2026Team10;

public class Player extends Entity {

    //player's stats
    private int lives = 3;
    private int score = 0;

    //when he grabs the powerups
    private boolean speedBoostActive = false;
    private int speedBoostDuration = 0;

    public Player(int startX, int startY, PrisonMap map) {
        super(startX, startY, map);
    }

    //called each frame
    public void update(KeyHandler keyHandler) {
        //checks the powerups
        updatePowerUps();
        double dx = 0, dy = 0;
        //currently can't move diagonally on the tiles, can change it if we want player to be able to do so
        if (keyHandler.upPressed) dy = -1; 
        else if (keyHandler.downPressed) dy = 1;
        else if (keyHandler.leftPressed) dx = -1;
        else if (keyHandler.rightPressed) dx = 1;

        move(dx, dy); //moves the player based on the input and speed
        

    }
    //resets the player to the starting point
    public void reset() {
        posX = map.getStartTile().x;
        posY = map.getStartTile().y;
        x = (int) posX;
        y = (int) posY;
        
        lives = 3;
        score = 0;
        speedBoostActive = false;
        speedBoostDuration = 0;
        speed = 1;
    }

    //collisions with guards
    public boolean collidesWithGuard(Guard guard) {
        return this.x == guard.getX() && this.y == guard.getY();
    }

    public void activateSpeedBoost(int duration) {
        speedBoostActive = true;
        speedBoostDuration = duration;
        speed = 2; // double speed while active
    }

    private void updatePowerUps() {
        if (speedBoostActive) {
            speedBoostDuration--;
            if (speedBoostDuration <= 0) {
                speedBoostActive = false;
                speed = 1; // reset to normal speed
            }
        }
    }

    // Player lives and score
    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public void loseLife() {
        lives--;
    }

    public void gainScore(int amount) {
        score += amount;
    }
}
