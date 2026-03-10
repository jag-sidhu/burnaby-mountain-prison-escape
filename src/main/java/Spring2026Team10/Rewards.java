package Spring2026Team10;

/**
 * represents the collectible reward within the game.
 * <p>
 *     Rewards grant the player with points, a temporary speed boost, and are required to complete the game.
 *     Each reward has a specific point value. Rewards cannot be collected if the player is suffering from a hands-tied
 *     effect.
 * </p>
 */
public class Rewards {
    private float x;
    private float y;
    private RewardType rewardType;
    private int bonus;
    private boolean active;

    /**
     * Constructs a new Rewards instance at the specified map coordinates.
     * @param x The x coordinate of the reward on the map grid.
     * @param y The y coordinate of the reward on the map grid.
     * @param rewardType The type of reward.
     */
    public Rewards(float x, float y, RewardType rewardType) {
        this.x = x;
        this.y = y;
        this.rewardType = rewardType;
        this.active = true;

        switch (rewardType) {
            case LAPTOP:
                this.bonus = 50;
                break;
            case STUDENT_ID:
                this.bonus = 75;
                break;
            case RACCOON:
                this.bonus = 100;
                break;
        }
    }

    /**
     * Applies the rewards points and speed boost to the player.
     * @param player The player attempting to collect the reward.
     */
    public void applyTo(Player player) {
        if (!active) {
            return;
        }

        if (player.isHandsTied()) {
            System.out.println("Your hands are cuffed! You cannot pick up the " + rewardType + " right now.");
            return;
        }

        switch (rewardType) {
            case LAPTOP:
                player.gainScore(bonus);
                player.activateSpeedBoost(30); //Apply a 1-second speed boost
                player.collectReward(RewardType.LAPTOP);
                System.out.println("Congratulations! You got the laptop! You have earned " + bonus + " points!");
                break;

            case STUDENT_ID:
                player.gainScore(bonus);
                player.activateSpeedBoost(30); //Apply a 1-second speed boost
                player.collectReward(RewardType.STUDENT_ID);
                System.out.println("Congratulations! You got your student id! You have earned " + bonus + " points!");
                break;

            case RACCOON:
                player.gainScore(bonus);
                player.activateSpeedBoost(30); //Apply a 1-second speed boost
                player.collectReward(RewardType.RACCOON);
                System.out.println("Congratulations! You got found a pet raccoon! You have earned " + bonus + " points!");
                break;
        }

        this.active = false;
    }

    /**
     * Gets the x coordinate of the reward.
     * @return The x coordinate position of the reward on the map grid.
     */
    public float getX() { return x; }

    /**
     * Gets the y coordinate of the reward.
     * @return The y coordinate position of the reward on the map grid.
     */
    public float getY() { return y; }

    /**
     * Gets the specific rewardType of the reward item.
     * @return The rewardType representing the item.
     */
    public RewardType getRewardType() { return rewardType; }

    /**
     * Checks if the reward is currently active and available to be collected.
     * @return True if the reward has not yet been collected. False if the reward has already been collected.
     */
    public boolean isActive() { return active; }
}
