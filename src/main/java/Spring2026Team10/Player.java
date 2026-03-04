package Spring2026Team10;

public class Player extends Entity {
    public enum MovementState {
        IDLE, MOVING
    }

    public enum StatusState {
        NORMAL, HANDS_TIED, SLOWED, INVERTED_CONTROLS
    }

    //player's stats
    private int lives = 3;
    private int score = 0;
    private int rewards = 0;
    private boolean handsTiedActive = false;
    private int handsTiedDuration = 0;
    private boolean isSlowed = false;
    private int slowedDuration = 0;
    private boolean controlsInverted = false;
    private int invertedDuration = 0;

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
        // Updated to handle inverted controls
        if (keyHandler.upPressed) dy = controlsInverted ? 1 : -1; 
        else if (keyHandler.downPressed) dy = controlsInverted ? -1 : 1;
        else if (keyHandler.leftPressed) dx = controlsInverted ? 1 : -1;
        else if (keyHandler.rightPressed) dx = controlsInverted ? -1 : 1;

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
        handsTiedActive = false;
        handsTiedDuration = 0;
        isSlowed = false;
        slowedDuration = 0;
        controlsInverted = false;
        invertedDuration = 0;
        speed = 0.25; // reset to slow default
        moving = false;
    }

    //collisions with guards
    public boolean collidesWithGuard(Guard guard) {
        return this.x == guard.getX() && this.y == guard.getY();
    }

    public void activateSpeedBoost(int duration) {
        speedBoostActive = true;
        speedBoostDuration = duration;
        speed = 0.5; // double speed while active
    }

    private void updatePowerUps() {
        if (speedBoostActive) {
            speedBoostDuration--;
            if (speedBoostDuration <= 0) {
                speedBoostActive = false;
                speed = 0.25; // reset to normal speed
            }
        }

        if (handsTiedActive) {
            if (--handsTiedDuration <= 0) handsTiedActive = false;
        }

        if (isSlowed) {
            if (--slowedDuration <= 0) {
                isSlowed = false;
                speed = 0.25; // Reset to normal speed
            }
        }

        if (controlsInverted) {
            if (--invertedDuration <= 0) controlsInverted = false;
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

    public void setLives(int lives) {
        this.lives = Math.max(0, lives);
    }

    public void gainScore(int amount) {
        score += amount;
    }

    public void addReward() {
        rewards++;
    }

    // Hazard methods
    public void tieHands(int duration) {
        handsTiedActive = true;
        handsTiedDuration = duration;
    }

    public boolean isHandsTied() {
        return handsTiedActive;
    }

    public void applySlowdown(int duration) {
        isSlowed = true;
        slowedDuration = duration;
        speed = 0.25 * 0.70; // 30% reduction
    }

    public void invertControls(int duration) {
        controlsInverted = true;
        invertedDuration = duration;
    }

    public MovementState getMovementState() {
        return isMoving() ? MovementState.MOVING : MovementState.IDLE;
    }

    public StatusState getStatusState() {
        if (handsTiedActive) {
            return StatusState.HANDS_TIED;
        }
        if (isSlowed) {
            return StatusState.SLOWED;
        }
        if (controlsInverted) {
            return StatusState.INVERTED_CONTROLS;
        }
        return StatusState.NORMAL;
    }
}
