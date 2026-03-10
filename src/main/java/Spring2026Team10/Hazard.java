package Spring2026Team10;

/**
 * Represents a static hazard entity on the game map.
 * Hazards apply negative status effects and score penalties to the player upon collision.
 */
public class Hazard {
    private float x;
    private float y;
    private HazardType hazardType;
    private int penalty;
    private boolean active;

    /**
     * Constructs a new Hazard at the specified map coordinates.
     * The penalty value is automatically assigned based on the provided HazardType.
     *
     * @param x The x-coordinate on the map grid.
     * @param y The y-coordinate on the map grid.
     * @param hazardType The specific type of hazard to be created.
     */
    public Hazard(float x, float y, HazardType hazardType) {
        this.x = x;
        this.y = y;
        this.hazardType = hazardType;
        this.active = true;

        switch (hazardType) {
            case HANDCUFFS:
                this.penalty = 10; 
                break;
            case PARKING_TICKET:
                this.penalty = 20;
                break;
            case BEAR:
                this.penalty = 50;
                break;
            case SPOILED_MILK:
                this.penalty = 15;
                break;
        }
    }

    /**
     * Applies the hazard's specific penalty and status effect to the player.
     * Once applied, the hazard is deactivated and cannot be triggered again.
     *
     * @param player The player entity that triggered the hazard.
     */
    public void applyTo(Player player) {
        if (!active) {
            return; 
        }

        switch (hazardType) {
            case HANDCUFFS:
                player.gainScore(-penalty); 
                player.tieHands(90); // 3 seconds at 30 FPS
                System.out.println("You just got put in Handcuffs! Lost " + penalty + " points. Unable to collect rewards for 3 seconds.");
                break;

            case PARKING_TICKET:
                player.gainScore(-penalty);
                player.applySlowdown(150); // 5 seconds at 30 FPS
                System.out.println("You just received a Parking Ticket, slow down! Lost " + penalty + " points. Speed reduced by 30% for 5 seconds.");
                break;

            case BEAR:
                player.gainScore(-penalty);
                player.loseLife(); // Player loses 1 out of 3 lives
                System.out.println("A Bear just attacked you! Lost " + penalty + " points and 1 life!");
                break;

            case SPOILED_MILK:
                player.gainScore(-penalty);
                player.invertControls(120); // 4 seconds at 30 FPS
                System.out.println("You just drank Spoiled Milk! Lost " + penalty + " points. Controls inverted for 4 seconds.");
                break;
        }
        
        this.active = false;
    }

    /**
     * Gets the x-coordinate of the hazard.
     * @return The x-coordinate on the map grid.
     */
    public float getX() { return x; }

    /**
     * Gets the y-coordinate of the hazard.
     * @return The y-coordinate on the map grid.
     */
    public float getY() { return y; }

    /**
     * Gets the specific type of this hazard.
     * @return The HazardType enum representing this hazard.
     */
    public HazardType getHazardType() { return hazardType; }

    /**
     * Checks if the hazard is currently active and can be triggered.
     * @return true if the hazard is active, false if it has already been triggered.
     */
    public boolean isActive() { return active; }
}