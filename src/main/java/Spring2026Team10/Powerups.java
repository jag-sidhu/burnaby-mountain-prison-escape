package Spring2026Team10;

public class Powerups {
    private float x;
    private float y;
    private PowerupType type;
    private int bonus;
    private boolean active;

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

    public void applyTo(Player player) {
        if (!active) return;

        // Ensure the player cannot pick up powerups if handcuffed
        if (player.isHandsTied()) {
            System.out.println("Your hands are tied! Cannot pick up the " + type + "!");
            return;
        }

        switch (type) {
            case COFFEE:
                player.gainScore(bonus);
                player.activateSpeedBoost(150); // 5 seconds of double speed (at 30 FPS)
                System.out.println("Drank Renaissance Coffee! Speed doubled for 5 seconds.");
                break;

            case SNOWFLAKE:
                player.gainScore(bonus);
                player.freezeGuards(150); // Freeze guards for 5 seconds
                System.out.println("All Guards are frozen for 5 seconds!");
                break;

            case DOCTORS_NOTE:
                player.gainScore(bonus);
                player.gainLife(); // +1 life
                System.out.println("Doctor's Note applied! Restored 1 life.");
                break;
        }

        this.active = false;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public PowerupType getType() { return type; }
    public boolean isActive() { return active; }
}