package Spring2026Team10;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

public class Game implements Runnable {
    private GameState state;
    private final Player player;
    private final PrisonMap map;
    // hud removed until we add functionality
    private final List<Guard> guards;
    private final Powerups powerups;
    private Timer timer;
    static Thread gameThread;
    private final MapPanel mapPanel;
    KeyHandler keyHandler = new KeyHandler();
    private final List<Hazard> hazards;

    //Set FPS
    int FPS = 30;


    public Game(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
        state = GameState.MENU;
        map = new PrisonMap();
        Point start = map.getStartTile();
        player = new Player(start.x, start.y, map);
        guards = new ArrayList<>();
        powerups = new Powerups();
        hazards = new ArrayList<>();

        loadEntitiesFromMap();

        // inform panel about the entities it should draw
        mapPanel.setPlayer(player);
        mapPanel.setGuards(guards);
        mapPanel.setHazards(hazards);

        //add key listener to mapPanel and focus on receiving key events
        mapPanel.addKeyListener(keyHandler);
        mapPanel.setFocusable(true);
        mapPanel.requestFocusInWindow();
    }

    public void start() {
        resetGame();
        changeState(GameState.PLAYING);
        gameThread = new Thread(this);
        gameThread.start();

    }
    public void update() {
        if (state == GameState.PLAYING) {
            player.update(keyHandler);
            guards.forEach(g -> g.update(player));
            powerups.update();
            PrisonMap.update(); // static for now

            for (Hazard h : hazards) {
                // If player is on the exact same tile and hazard is active
                if (h.isActive() && player.getX() == (int)h.getX() && player.getY() == (int)h.getY()) {
                    h.applyTo(player);
                }
            }
        }
    }

    public void render(Graphics g) {
        switch(state) {
            case MENU -> {
                //drawMenu(g);
            }
            case READY -> {
                //maybe render countdown
            }
            case PLAYING -> {
                //drawGame();
            }
            case DYING -> {
                //animation maybe
            }
            case FROZEN -> {
                //paused state
            }
            case LEVEL_COMPLETE -> {
                //drawLevelComplete(g);
            }
            case GAME_OVER -> {
                //drawGameOver(g);
            }
        }
    }

    public void handleInput(KeyEvent e) {}

    public void resetGame() {
        map.reset();
        player.reset();
        guards.forEach(g -> {}); // placeholder in case guards have reset logic later
    }

    private void changeState(GameState state) {
        this.state = state;

        if(state == GameState.GAME_OVER && timer != null) {
            timer.stop();
        }
    }

    @Override
    @SuppressWarnings("BusyWait") // suppress warning about Thread.sleep in loop
    public void run() {
        double drawInterval = 1000000000 / FPS; // 1 second/60FPS
        double nextdraw = System.nanoTime() + drawInterval;

        while(gameThread != null) {
            System.out.println("Game loop running");

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

    private void loadEntitiesFromMap() {
        hazards.clear();
        java.util.Random rand = new java.util.Random();

        // Generate select amounts of each hazard (can edit this)
        int bearCount = 3; // 3
        int milkCount = rand.nextInt(3) + 5;   // 5-7
        int cuffCount = rand.nextInt(2) + 2;   // 2-3
        int ticketCount = rand.nextInt(2) + 4; // 4-5

        int totalHazards = bearCount + milkCount + cuffCount + ticketCount;

        map.spawnHazards(totalHazards);

        // Create the bag and fill it with the exact counts
        java.util.List<HazardType> bag = new java.util.ArrayList<>();
        for (int i = 0; i < bearCount; i++) bag.add(HazardType.BEAR);
        for (int i = 0; i < milkCount; i++) bag.add(HazardType.SPOILED_MILK);
        for (int i = 0; i < cuffCount; i++) bag.add(HazardType.HANDCUFFS);
        for (int i = 0; i < ticketCount; i++) bag.add(HazardType.PARKING_TICKET);

        // Shuffle the bag so they are distributed randomly across the map
        java.util.Collections.shuffle(bag);
        int bagIndex = 0;

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
    }
}
