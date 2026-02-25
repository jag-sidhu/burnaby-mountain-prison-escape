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

    //Set FPS
    int FPS = 60;


    public Game(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
        state = GameState.MENU;
        map = new PrisonMap();
        Point start = map.getStartTile();
        player = new Player(start.x, start.y, map);
        guards = new ArrayList<>();
        powerups = new Powerups();

        // inform panel about the entities it should draw
        mapPanel.setPlayer(player);
        mapPanel.setGuards(guards);

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
}
