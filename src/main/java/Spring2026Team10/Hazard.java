package Spring2026Team10;

public class Hazard {
    private float x;
    private float y;
    private HazardType hazardType;
    private int penalty;
    private boolean active;

    public Hazard(float x, float y, HazardType hazardType) {
        this.x = x;
        this.y = y;
        this.hazardType = hazardType;
        this.active = true;

        switch (hazardType) {
            case HANDCUFFS:
                this.penalty = 50; 
                break;
            case PARKING_TICKET:
                this.penalty = 100;
                break;
            case BEAR:
                this.penalty = 200;
                break;
            case SPOILED_MILK:
                this.penalty = 75;
                break;
        }
    }

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

    public float getX() { return x; }
    public float getY() { return y; }
    public HazardType getHazardType() { return hazardType; }
    public boolean isActive() { return active; }
}