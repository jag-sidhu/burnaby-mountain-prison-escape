package Spring2026Team10;

/**
 * Represents the player character and tracks movement, score, lives, rewards, and status effects.
 */
public class Player extends Entity {
    private static final int HIT_INVULNERABILITY_FRAMES = 60;

    /**
     * Describes whether the player is currently moving for sprite animation.
     */
    public enum MovementState {
        IDLE, MOVING
    }

    /**
     * Describes the current hazard effect being shown on the player.
     */
    public enum StatusState {
        NORMAL, HANDS_TIED, SLOWED, INVERTED_CONTROLS
    }

    // Player's stats
    private int lives = 3;
    private int score = 0;
    private int rewards = 0;
    private final java.util.EnumSet<RewardType> collectedRewards = java.util.EnumSet.noneOf(RewardType.class);
    private boolean handsTiedActive = false;
    private int handsTiedDuration = 0;
    private boolean isSlowed = false;
    private int slowedDuration = 0;
    private boolean controlsInverted = false;
    private int invertedDuration = 0;
    private boolean guardsFrozen = false;
    private int frozenDuration = 0;
    private int hitInvulnerabilityFrames = 0;
    private String popupMessage = "";
    private int popupTimer = 0;

    // When he grabs the powerups
    private boolean speedBoostActive = false;
    private int speedBoostDuration = 0;

    public Player(int startX, int startY, PrisonMap map) {
        super(startX, startY, map);
    }

    /**
     * Updates the player once per frame using the current keyboard input.
     * @param keyHandler The active keyboard input handler.
     */
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

    /**
     * Resets the player back to the start tile and clears all temporary effects.
     */
    public void reset() {
        posX = map.getStartTile().x;
        posY = map.getStartTile().y;
        x = (int) posX;
        y = (int) posY;
        
        lives = 3;
        score = 0;
        rewards = 0;
        collectedRewards.clear();
        speedBoostActive = false;
        speedBoostDuration = 0;
        handsTiedActive = false;
        handsTiedDuration = 0;
        isSlowed = false;
        slowedDuration = 0;
        controlsInverted = false;
        invertedDuration = 0;
        guardsFrozen = false;
        frozenDuration = 0;
        hitInvulnerabilityFrames = 0;
        popupMessage = "";
        popupTimer = 0;
        speed = 0.25; // reset to slow default
        moving = false;
    }

    //collisions with guards
    public boolean collidesWithGuard(Guard guard) {
        return this.x == guard.getX() && this.y == guard.getY();
    }

    /**
     * Activates the temporary speed boost granted by certain pickups.
     * @param duration The number of update frames the boost should last.
     */
    public void activateSpeedBoost(int duration) {
        speedBoostActive = true;
        speedBoostDuration = duration;
        speed = 0.5; // double speed while active
    }

    /**
     * Decrements timers for all temporary player effects and clears them when they expire.
     */
    private void updatePowerUps() {
        if (guardsFrozen) {
            if (--frozenDuration <= 0) guardsFrozen = false;
        }
        
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

        if (hitInvulnerabilityFrames > 0) {
            hitInvulnerabilityFrames--;
        }

        if (popupTimer > 0) {
            popupTimer--;
        }
    }

    // Player lives and score
    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    /**
     * Removes one life from the player and starts the hit invulnerability window.
     */
    public void loseLife() {
        if (hitInvulnerabilityFrames > 0 || lives <= 0) {
            return;
        }
        lives--;
        hitInvulnerabilityFrames = HIT_INVULNERABILITY_FRAMES;
    }

    /**
     * Sets the player's life total, clamped so it never goes below zero.
     * @param lives The new life total.
     */
    public void setLives(int lives) {
        this.lives = Math.max(0, lives);
    }

    /**
     * Adds points to the player's score.
     * @param amount The number of points to add.
     */
    public void gainScore(int amount) {
        score += amount;
    }

    /**
     * Increments the player's total collected rewards count by one.
     */
    public void addReward() {
        rewards++;
    }

    /**
     * Marks a reward type as collected and increments the completion count once.
     * @param rewardType The reward that was picked up.
     */
    public void collectReward(RewardType rewardType) {
        if (collectedRewards.add(rewardType)) {
            rewards++;
        }
    }

    /**
     * Returns the amount of rewards the player has collected, 0 - 3.
     * @return The total number of rewards the player has calculated.
     */
    public int getReward() {
        return rewards;
    }

    /**
     * Checks whether a specific reward has already been collected.
     * @param rewardType The reward to check.
     * @return True if the player has already picked up that reward.
     */
    public boolean hasCollectedReward(RewardType rewardType) {
        return collectedRewards.contains(rewardType);
    }

    /**
     * Applies the handcuff effect for a set duration.
     * @param duration The number of update frames the effect should last.
     */
    public void tieHands(int duration) {
        handsTiedActive = true;
        handsTiedDuration = duration;
    }

    /**
     * Checks if the player's hands are currently tied by the Handcuffs hazard.
     * @return true if hands are tied, false otherwise.
     */
    public boolean isHandsTied() {
        return handsTiedActive;
    }

    /**
     * Checks whether the player is currently protected from another hit.
     * @return True while hit invulnerability is active.
     */
    public boolean isInvulnerable() {
        return hitInvulnerabilityFrames > 0;
    }

    /**
     * Checks whether the red damage flash should be visible on the current frame.
     * @return True when the player should be drawn with the hit flash.
     */
    public boolean isHitFlashVisible() {
        return hitInvulnerabilityFrames > 0 && ((hitInvulnerabilityFrames / 4) % 2 == 0);
    }

    /**
     * Applies the slowdown effect and reduces movement speed until it expires.
     * @param duration The number of update frames the slowdown should last.
     */
    public void applySlowdown(int duration) {
        isSlowed = true;
        slowedDuration = duration;
        speed = 0.25 * 0.70; // 30% reduction
    }

    /**
     * Reverses movement controls for a set duration.
     * @param duration The number of update frames the effect should last.
     */
    public void invertControls(int duration) {
        controlsInverted = true;
        invertedDuration = duration;
    }

    /**
     * Gets the movement state used by the sprite renderer.
     * @return MOVING when the player changed position this frame, otherwise IDLE.
     */
    public MovementState getMovementState() {
        return isMoving() ? MovementState.MOVING : MovementState.IDLE;
    }

    /**
     * Gets the status effect currently being shown on the player.
     * @return The highest priority active status effect, or NORMAL if none are active.
     */
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

    /**
     * Restores one life, up to the maximum HUD limit of three.
     */
    public void gainLife() {
        if (lives < 3) {
            lives++;
        }
    }

    /**
     * Freezes guard movement for a set duration.
     * @param duration The number of update frames the freeze should last.
     */
    public void freezeGuards(int duration) {
        guardsFrozen = true;
        frozenDuration = duration;
    }

    /**
     * Checks whether guards are currently frozen by the player.
     * @return True while the freeze effect is active.
     */
    public boolean isGuardsFrozen() {
        return guardsFrozen;
    }

    /**
     * Sets a temporary popup message to be displayed on the HUD.
     * @param message The text to display.
     * @param duration The number of frames the message should remain visible.
     */
    public void showMessage(String message, int duration) {
        this.popupMessage = message;
        this.popupTimer = duration;
    }

    public String getPopupMessage() { return popupMessage; }
    
    public boolean isPopupVisible() { return popupTimer > 0; }
}
