package Spring2026Team10;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Timer;

/**
 * The core controller for the game. Manages the game loop, state, and entities.
 * Implements runnable to execute the main game loop on a thread.
 * Coordinates the interactions between the player, guards, interactable items (hazards, rewards, powerups), and the map.
 */
public class Game implements Runnable {
    /**
     * Defines the difficulty levels for the game and defines the number of lives the player has for each level.
     */
    public enum Difficulty {
        EASY("Easy", 3),
        MEDIUM("Medium", 2),
        HARD("Hard", 1);

        private final String label;
        private final int lives;

        Difficulty(String label, int lives) {
            this.label = label;
            this.lives = lives;
        }

        public String getLabel() {
            return label;
        }

        public int getLives() {
            return lives;
        }
    }

    private static final int COINS_PER_MATCH = 50;
    private final Difficulty[] difficulties = Difficulty.values();
    private int difficultyIndex = 0;
    private GameState state;
    private final Player player;
    private final PrisonMap map;
    // hud removed until we add functionality
    private final List<Guard> guards;
    private final List<Powerups> powerups;
    private Timer timer;
    static Thread gameThread;
    private final MapPanel mapPanel;
    KeyHandler keyHandler = new KeyHandler();
    private final List<Hazard> hazards;
    private final List<Rewards> rewards;
    private long matchStartMillis;
    private boolean escPressedLastFrame = false;

    //Sound
    Sound music = new Sound();
    Sound sound = new Sound();

    //Set FPS
    int FPS = 30;

    /**
     * Contrasts a new game instance and initializes the game environment
     * @param mapPanel The map panel responsible for rendering the game map.
     */
    public Game(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
        state = GameState.MENU;
        map = mapPanel.getPrisonMap();
        Point start = map.getStartTile();
        player = new Player(start.x, start.y, map);
        guards = new ArrayList<>();
        powerups = new ArrayList<>();
        hazards = new ArrayList<>();
        rewards = new ArrayList<>();

        loadEntitiesFromMap();

        // inform panel about the entities it should draw
        mapPanel.setPlayer(player);
        mapPanel.setGuards(guards);
        mapPanel.setHazards(hazards);
        mapPanel.setRewards(rewards);
        mapPanel.setPowerups(powerups);
        mapPanel.setGame(this);

        //add key listener to mapPanel and focus on receiving key events
        mapPanel.addKeyListener(keyHandler);
        mapPanel.setFocusable(true);
        mapPanel.requestFocusInWindow();
    }

    /**
     * Creates a new game thread and starts the thread.
     */
    public void start() {
        resetGame();
        changeState(GameState.MENU);
        gameThread = new Thread(this);
        gameThread.start();

    }

    public void update() {
        handlePauseToggle();

        if (state != GameState.PLAYING) {
            return;
        }

        int livesBefore = player.getLives();

        player.update(keyHandler);

        if (map.collectCoin(player.getY(), player.getX())) {
            player.gainScore(10);
            playSoundEffect(2);
        }

        if (!player.isGuardsFrozen()) {
            guards.forEach(g -> g.update(player));
        }
        
        PrisonMap.update(); // static for now

        for (Powerups p : powerups) {
            if (p.isActive() && player.getX() == (int)p.getX() && player.getY() == (int)p.getY()) {
                p.applyTo(player);
                if (p.getType() == PowerupType.COFFEE) {
                    playSoundEffect(4);
                } else {
                    playSoundEffect(2);
                }
            }
        }

        for (Hazard h : hazards) {
            // If player is on the exact same tile and hazard is active
            if (h.isActive() && player.getX() == (int)h.getX() && player.getY() == (int)h.getY()) {
                h.applyTo(player);
                if(h.getHazardType() == HazardType.BEAR) {
                    playSoundEffect(1);
                } else if(h.getHazardType() == HazardType.HANDCUFFS) {
                    playSoundEffect(6);
                } else {
                    playSoundEffect(3);
                }
            }
        }

        for (Rewards r : rewards) {
            // If player is on the exact same tile and reward is active
            if (r.isActive() && player.getX() == (int)r.getX() && player.getY() == (int)r.getY()) {
                r.applyTo(player);
                playSoundEffect(2);
            }
        }

        if (player.getLives() < livesBefore) {
            playSoundEffect(3);
        }

        if (player.getLives() <= 0 || player.getScore() < 0) {
            changeState(GameState.GAME_OVER);
            music.stop();
            playSoundEffect(7);
            return;
        }

        if (player.getReward() == 3 && (player.getX() == map.getEndTile().x && player.getY() == map.getEndTile().y)) {
            stopMusic();
            changeState(GameState.LEVEL_COMPLETE);
            playSoundEffect(5);
            return;
        }

        updateHud();
    }

    public void resetGame() {
        map.reset();
        loadEntitiesFromMap();
        map.spawnCoins(COINS_PER_MATCH);
        player.reset();
        player.setLives(getDifficulty().getLives());
        guards.clear();
        int guardCount = switch (getDifficulty()) {
            case EASY -> 5;
            case MEDIUM -> 6;
            case HARD -> 7;
        };
        int zoneCols = (guardCount <= 5) ? 3 : (guardCount == 6) ? 3 : 4;
        int zoneRows = (guardCount <= 6) ? 2 : 2;
        for (int i = 0; i < guardCount; i++) {
            int zoneCol = i % zoneCols;
            int zoneRow = i / zoneCols;
            guards.add(Guard.spawnRandomGuard(
                map, Guard.GuardType.PATROL, guards, player,
                i % 2 == 0, 12,
                zoneCol, zoneRow, zoneCols, zoneRows
            ));
        }
        mapPanel.setGuards(guards);        
        matchStartMillis = System.currentTimeMillis();
        keyHandler.clear();
        updateHud();
    }

    /**
     *Updates the current state of the game and does the necessary transition logic.
     * @param state The new state to transition the game to.
     */
    private void changeState(GameState state) {
        this.state = state;
        if (state != GameState.PLAYING) {
            keyHandler.clear();
            escPressedLastFrame = false;
        }
        mapPanel.repaint();

        if(state == GameState.GAME_OVER && timer != null) {
            timer.stop();
        }

        if (state == GameState.GAME_OVER || state == GameState.LEVEL_COMPLETE || state == GameState.MENU) {
            stopMusic();
        }

    }

    /**
     * The main execution loop for the game thread.
     * <p>
     *     Core game running logic. Continuosly cycles as long as GameThread is active. Updaes the game's logic and
     *     entitites, and calls the repaint method to render the uopdated frame. Maintains a consistent framrate
     *     defined by FPS. Calculates how long the update and render took and pauses the thread for the remainder of
     *     the time interval.
     * </p>
     */
    @Override
    @SuppressWarnings("BusyWait") // suppress warning about Thread.sleep in loop
    public void run() {
        double drawInterval = 1000000000 / FPS; // 1 second/60FPS
        double nextdraw = System.nanoTime() + drawInterval;

        while(gameThread != null) {
            //Update character positions
            update();

            //Redraw the map with updated positions
            mapPanel.repaint();

            //Pause game loop from rendering until next draw interval
            try {
                double remainingTime = nextdraw - System.nanoTime();
                remainingTime = remainingTime/1000000; // convert to ms

                if(remainingTime < 0) {
                    remainingTime = 0;
                }
                Thread.sleep((long) remainingTime);
                nextdraw += drawInterval;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateHud() {
        long elapsedSeconds = Math.max(0L, (System.currentTimeMillis() - matchStartMillis) / 1000L);
        long minutes = elapsedSeconds / 60L;
        long seconds = elapsedSeconds % 60L;
        mapPanel.setTimeText(String.format("%02d:%02d", minutes, seconds));
        mapPanel.setScoreText(Integer.toString(player.getScore()));
    }

    private void loadEntitiesFromMap() {
        hazards.clear();
        rewards.clear();
        powerups.clear();
        java.util.Random rand = new java.util.Random();
        java.util.Random rand2 = new java.util.Random();

        // Generate select amounts of each hazard (can edit this)
        int bearCount = 3; // 3
        int milkCount = rand.nextInt(3) + 5;   // 5-7
        int cuffCount = rand.nextInt(2) + 2;   // 2-3
        int ticketCount = rand.nextInt(2) + 4; // 4-5

        int totalHazards = bearCount + milkCount + cuffCount + ticketCount;

        map.spawnHazards(totalHazards);
        map.spawnRewards();
        map.spawnPowerups();

        // Create the bag and fill it with the exact counts
        java.util.List<HazardType> bag = new java.util.ArrayList<>();
        for (int i = 0; i < bearCount; i++) bag.add(HazardType.BEAR);
        for (int i = 0; i < milkCount; i++) bag.add(HazardType.SPOILED_MILK);
        for (int i = 0; i < cuffCount; i++) bag.add(HazardType.HANDCUFFS);
        for (int i = 0; i < ticketCount; i++) bag.add(HazardType.PARKING_TICKET);

        List<RewardType> rewardBag = new ArrayList<>();
        rewardBag.add(RewardType.LAPTOP);
        rewardBag.add(RewardType.STUDENT_ID);
        rewardBag.add(RewardType.RACCOON);

        List<PowerupType> powerupBag = new ArrayList<>();
        powerupBag.add(PowerupType.COFFEE);
        powerupBag.add(PowerupType.COFFEE); // 2 Coffees
        powerupBag.add(PowerupType.SNOWFLAKE);
        powerupBag.add(PowerupType.DOCTORS_NOTE);

        // Shuffle bags
        java.util.Collections.shuffle(bag);
        int bagIndex = 0;

        Collections.shuffle(rewardBag);
        int rewardIndex = 0;

        Collections.shuffle(powerupBag);
        int powerupIndex = 0;

        // Assign them to the tiles generated by the map
        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                if (map.getTile(row, col) == TileType.HAZARD) {
                    if (bagIndex < bag.size()) {
                        hazards.add(new Hazard(col, row, bag.get(bagIndex)));
                        bagIndex++;
                    }
                }
            }
        }

        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                if (map.getTile(row, col) == TileType.REWARD) {
                    if (rewardIndex < rewardBag.size()) {
                        rewards.add(new Rewards(col, row, rewardBag.get(rewardIndex)));
                        rewardIndex++;
                    }
                }
            }
        }

        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                if (map.getTile(row, col) == TileType.POWERUP) {
                    if (powerupIndex < powerupBag.size()) {
                        powerups.add(new Powerups(col, row, powerupBag.get(powerupIndex)));
                        powerupIndex++;
                    }
                }
            }
        }
    }

    /**
     * Transitions the game state to display the story line screen.
     */
    public void displayStory() {
        changeState(GameState.STORY);
        playMusic();
    }

    public void startMatch() {
        resetGame();
        changeState(GameState.PLAYING);
        mapPanel.requestFocusInWindow();
    }

    public void restartMatch() {
        playMusic();
        startMatch();
    }

    public void resumeMatch() {
        if (state == GameState.FROZEN) {
            changeState(GameState.PLAYING);
            mapPanel.requestFocusInWindow();
        }
    }

    public void returnToMenu() {
        changeState(GameState.MENU);
        mapPanel.requestFocusInWindow();
    }

    public void increaseDifficulty() {
        if (difficultyIndex < difficulties.length - 1) {
            difficultyIndex++;
            mapPanel.repaint();
        }
    }

    public void decreaseDifficulty() {
        if (difficultyIndex > 0) {
            difficultyIndex--;
            mapPanel.repaint();
        }
    }

    public Difficulty getDifficulty() {
        return difficulties[difficultyIndex];
    }

    public String getDifficultyLabel() {
        return getDifficulty().getLabel();
    }

    public GameState getState() {
        return state;
    }

    public void exitGame() {
        stopMusic();
        System.exit(0);
    }

    private void handlePauseToggle() {
        boolean escDown = keyHandler.escapePressed;
        if (escDown && !escPressedLastFrame) {
            if (state == GameState.PLAYING) {
                changeState(GameState.FROZEN);
            } else if (state == GameState.FROZEN) {
                changeState(GameState.PLAYING);
                mapPanel.requestFocusInWindow();
            }
        }
        escPressedLastFrame = escDown;
    }

    /**
     * Begins playing the background music
     */
    public void playMusic() {
        music.setFile(0);
        music.play();
        music.loop();
    }

    /**
     * Stops playing the background music
     */
    public void stopMusic() {
        music.stop();
    }

    /**
     * Plays a sound effect based on the specified array index
     * @param i The index of the audio file to load and play from the Sound class.
     */
    public void playSoundEffect(int i) {
        sound.setFile(i);
        sound.play();
    }
}
