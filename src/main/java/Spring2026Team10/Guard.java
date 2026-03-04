package Spring2026Team10;

import java.util.Random;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Guard extends Entity {

    public enum GuardType {
        PATROL, CHASE
    }

    public enum GuardState {
        IDLE, //standing still
        PATROLLING, //moving along path
        CHASING, //following player, cant move horizontally
        RETURNING //going back to patrol after player moves too far away
    }

    private GuardType type;
    private GuardState state;

    // patrol info
    private int patrolStartX, patrolStartY;
    private int patrolEndX, patrolEndY;
    private boolean patrolForward = true; //direction along patrol path
    private boolean horizontalPatrol; //true = horizontal false = vertical

    // chase info
    private int agroRange = 5; //number of tiles players must be within to trigger chase

    //guard sprite + animation
    private BufferedImage[] walkUp = new BufferedImage[2];
    private BufferedImage[] walkDown = new BufferedImage[2];
    private BufferedImage[] walkLeft = new BufferedImage[2];
    private BufferedImage[] walkRight = new BufferedImage[2];

    private BufferedImage currentSprite;

    private int animationFrame = 0;
    private int animationCounter = 0;
    private final int animationSpeed = 10;

    private int lastDx = 0;
    private int lastDy = 1; //default facing down

    //random spawn area for the guards
    public static Guard spawnRandomGuard(PrisonMap map, GuardType type, List<Guard> existingGuards, Player player, boolean horizontalPatrol, int patrolLength) {
        Random rand = new Random();
        int x, y;
        boolean validSpawn;

        do {
            x = rand.nextInt(map.getCols());
            y = rand.nextInt(map.getRows());

            validSpawn = map.isWalkable(y, x);
            
            //prevents spawning on the player
            if (player != null && player.getX() == x && player.getY() == y){
                validSpawn = false;
            }
            //makes sure guards don't spawn on other guards
            if (existingGuards != null) {
                for (Guard g : existingGuards) {
                    if (g.getX() == x && g.getY() == y) {
                        validSpawn = false;
                        break;
                    }
                }
            }

        } while (!validSpawn);

        return new Guard(x, y, map, type, horizontalPatrol, patrolLength);
    }

    //constructor
    public Guard(int startX, int startY, PrisonMap map, GuardType type, boolean horizontalPatrol, int patrolLength) {
        super(startX, startY, map);
        this.type = type;
        this.state = (type == GuardType.PATROL)? GuardState.PATROLLING: GuardState.CHASING;

        this.patrolStartX = startX;
        this.patrolStartY = startY;
        this.horizontalPatrol = horizontalPatrol;

        //calculate patrol end points based on patrol length and map boundaries
        if (horizontalPatrol) {
            patrolEndX = startX;
            for (int i = 1; i <= patrolLength; i++) {
                if (map.isWalkable(startY, startX + i))
                    patrolEndX = startX + i;
                else break;
            }
            patrolEndY = startY;
        } else {
            patrolEndY = startY;
            for (int i = 1; i <= patrolLength; i++) {
                if (map.isWalkable(startY + i, startX))
                    patrolEndY = startY + i;
                else break;
            }
            patrolEndX = startX;
        }

        loadSprites();
    }

    //loads sprites
    private void loadSprites() {
        try {
            walkUp[0] = ImageIO.read(getClass().getResourceAsStream("/sprites/guard/Guard_Back_1.png"));
            walkUp[1] = ImageIO.read(getClass().getResourceAsStream("/sprites/guard/Guard_Back_2.png"));

            walkDown[0] = ImageIO.read(getClass().getResourceAsStream("/sprites/guard/Guard_Front_1.png"));
            walkDown[1] = ImageIO.read(getClass().getResourceAsStream("/sprites/guard/Guard_Front_2.png"));

            walkLeft[0] = ImageIO.read(getClass().getResourceAsStream("/sprites/guard/Guard_Left_1.png"));
            walkLeft[1] = ImageIO.read(getClass().getResourceAsStream("/sprites/guard/Guard_Left_2.png"));

            walkRight[0] = ImageIO.read(getClass().getResourceAsStream("/sprites/guard/Guard_Right_1.png"));
            walkRight[1] = ImageIO.read(getClass().getResourceAsStream("/sprites/guard/Guard_Right_2.png"));

            currentSprite = walkDown[0];

        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Guard sprites failed to load.");
        }
    }

    //update method to be called every frame, handles state transitions and movement
    public void update(Player player) {
        switch (state) {
            case PATROLLING -> {
                //checks if player is within agro range
                int distanceToPlayer =
                        Math.abs(player.getX() - patrolStartX)
                                + Math.abs(player.getY() - patrolStartY);

                if (distanceToPlayer <= agroRange)
                    state = GuardState.CHASING;
                else
                    patrolMove();
            }
            case CHASING -> chasePlayer(player);
            case RETURNING -> returnToPatrol();
            case IDLE -> {}
        }
    }

    //movement methods
    private void patrolMove() {
        int dx = 0, dy = 0;

        if (horizontalPatrol) {
            dx = patrolForward ? 1 : -1;
            if (!map.isWalkable(y, x + dx)) dx = 0;

            if (x + dx > patrolEndX) patrolForward = false;
            else if (x + dx < patrolStartX) patrolForward = true;
        } else {
            dy = patrolForward ? 1 : -1;
            if (!map.isWalkable(y + dy, x)) dy = 0;

            if (y + dy > patrolEndY) patrolForward = false;
            else if (y + dy < patrolStartY) patrolForward = true;
        }

        animateAndMove(dx, dy);
    }

    private void chasePlayer(Player player) {
        int distanceToPlayer =
                Math.abs(player.getX() - patrolStartX)
                        + Math.abs(player.getY() - y);

        if (distanceToPlayer > agroRange) {
            state = GuardState.RETURNING;
            return;
        }

        int dx = 0, dy = 0;

        if (player.getX() != x)
            dx = (player.getX() > x) ? 1 : -1;
        else if (player.getY() != y)
            dy = (player.getY() > y) ? 1 : -1;

        if (!map.isWalkable(y, x + dx)) dx = 0;
        if (!map.isWalkable(y + dy, x)) dy = 0;

        animateAndMove(dx, dy);
    }

    private void returnToPatrol() {
        int dx = 0, dy = 0;

        if (x < patrolStartX) dx = 1;
        else if (x > patrolStartX) dx = -1;

        if (y < patrolStartY) dy = 1;
        else if (y > patrolStartY) dy = -1;

        if (!map.isWalkable(y, x + dx)) dx = 0;
        if (!map.isWalkable(y + dy, x)) dy = 0;

        animateAndMove(dx, dy);

        if (x == patrolStartX && y == patrolStartY) {
            state = GuardState.PATROLLING;
            patrolForward = true;
        }
    }

    //handling the animation
    private void animateAndMove(int dx, int dy) {

        if (dx != 0 || dy != 0) {
            lastDx = dx;
            lastDy = dy;

            animationCounter++;
            if (animationCounter >= animationSpeed) {
                animationCounter = 0;
                animationFrame = (animationFrame + 1) % 2;
            }

            if (lastDy == -1)
                currentSprite = walkUp[animationFrame];
            else if (lastDy == 1)
                currentSprite = walkDown[animationFrame];
            else if (lastDx == -1)
                currentSprite = walkLeft[animationFrame];
            else if (lastDx == 1)
                currentSprite = walkRight[animationFrame];
        }

        move(dx, dy);
    }

    public BufferedImage getSprite() {
        return currentSprite;
    }

    //getters
    public GuardType getType() { return type; }
    public GuardState getState() { return state; }
    public void setState(GuardState state) { this.state = state; }

    public boolean isAlertState() {
        return state == GuardState.CHASING
                || state == GuardState.RETURNING;
    }
}