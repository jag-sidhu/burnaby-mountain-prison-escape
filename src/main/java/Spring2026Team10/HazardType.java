package Spring2026Team10;

/**
 * Represents the different types of hazards available in the game.
 * Each hazard type corresponds to a unique score penalty and status effect.
 */
public enum HazardType {
    /** Ties the player's hands, preventing them from picking up items. */
    HANDCUFFS,
    /** Applies a speed reduction to the player. */
    PARKING_TICKET,
    /** Deducts a massive amount of points and strips away one of the player's lives. */
    BEAR,
    /** Inverts the player's movement controls temporarily. */
    SPOILED_MILK
}