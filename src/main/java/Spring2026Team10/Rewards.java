package Spring2026Team10;

public class Rewards {
    private float x;
    private float y;
    private RewardType rewardType;
    private int bonus;
    private boolean active;

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

    public float getX() { return x; }
    public float getY() { return y; }
    public RewardType getRewardType() { return rewardType; }
    public boolean isActive() { return active; }
}
