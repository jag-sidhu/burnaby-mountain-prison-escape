package Spring2026Team10;

/**
 * Represents a collectible powerup entity on the game map.
 * Powerups grant positive status effects, survival bonuses and add to the player's score.
 */
public class Powerups {
    private float x;
    private float y;
    private PowerupType type;
    private int bonus;
    private boolean active;

    /**
     * Constructs a new Powerup at the specified map coordinates.
     * The score bonus is automatically assigned based on the provided PowerupType.
     *
     * @param x The x-coordinate on the map grid.
     * @param y The y-coordinate on the map grid.
     * @param type The specific type of powerup to be created.
     */
    public Powerups(float x, float y, PowerupType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.active = true;

        switch (type) {
            case COFFEE: this.bonus = 25; break;
            case SNOWFLAKE: this.bonus = 25; break;
            case DOCTORS_NOTE: this.bonus = 40; break;
        }
    }

    /**
     * Applies the powerup's effect and score bonus to the player.
     * Fails if the player's hands are currently tied by handcuffs.
     * Once successfully applied, the powerup is deactivated.
     *
     * @param player The player entity collecting the powerup.
     */
    public void applyTo(Player player) {
        if (!active) return;

        // Ensure the player cannot pick up powerups if handcuffed
        if (player.isHandsTied()) {
            player.showMessage("Your hands are tied! Cannot pick up the " + type + ".", 90);
            return;
        }

        switch (type) {
            case COFFEE:
                player.gainScore(bonus);
                player.activateSpeedBoost(150); // 5 seconds of double speed (at 30 FPS)
                player.showMessage("Drank Renaissance Coffee! Speed doubled for 5 seconds.", 150);
                break;

            case SNOWFLAKE:
                player.gainScore(bonus);
                player.freezeGuards(150); // Freeze guards for 5 seconds
                player.showMessage("All Guards are frozen for 5 seconds!", 150);
                break;

            case DOCTORS_NOTE:
                player.gainScore(bonus);
                player.gainLife(); // +1 life
                player.showMessage("Doctor's Note applied! Restored 1 life.", 90);
                break;
        }

        this.active = false;
    }

    /**
     * Gets the x-coordinate of the powerup.
     * @return The x-coordinate on the map grid.
     */
    public float getX() { return x; }

    /**
     * Gets the y-coordinate of the powerup.
     * @return The y-coordinate on the map grid.
     */
    public float getY() { return y; }

    /**
     * Gets the specific type of this powerup.
     * @return The PowerupType enum representing this powerup.
     */
    public PowerupType getType() { return type; }
    
    /**
     * Checks if the powerup is currently active and available to be collected.
     * @return true if the powerup is active, false if it has already been collected.
     */
    public boolean isActive() { return active; }
}