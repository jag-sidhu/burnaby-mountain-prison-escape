package Spring2026Team10;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

public class Game implements Runnable {
    private GameState state;
    private Player player;
    private PrisonMap map;
    private HUD hud;
    private final List<Guard> guards;
    private Powerups powerups;
    private Timer timer;
    static Thread gameThread;
    private MapPanel mapPanel;
    KeyHandler keyHandler = new KeyHandler();

    //Set FPS
    int FPS = 60;


    public Game(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
        state = GameState.MENU;
        player = new Player();
        map = new PrisonMap();
        hud = new HUD();
        guards = new ArrayList<>();
        powerups = new Powerups();

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
            player.update();
            guards.forEach(Guard::update);
            powerups.update();
            map.update();
        }
        if (keyHandler.upPressed) {
            //Move player up
        }
        else if (keyHandler.downPressed) {
            //Move player down
        }
        else if (keyHandler.leftPressed) {
            //Move player left
        }
        else if (keyHandler.rightPressed) {
            //Move player right
        }
    }

    public void render(Graphics g) {
        switch(state) {
            case MENU:
                //drawMenu(g);
                break;
            case PLAYING:
                //drawGame();
                break;
            case GAME_OVER:
                //drawGameOver(g);
                break;
            case LEVEL_COMPLETE:
                //drawLevelComplete(g);
                break;
        }
    }

    public void handleInput(KeyEvent e) {}

    public static void resetGame() {
        //map.reset();
        //player.reset();
        //guards.forEach(Guard::reset);
    }

    private void changeState(GameState state) {
        this.state = state;

        if(state == GameState.GAME_OVER && timer != null) {
            timer.stop();
        }
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS; // 1 second/60FPS
        double nextdraw = System.nanoTime() + drawInterval;

        while(gameThread != null) {
            System.out.println("Game loop running");
            long currentTime = System.nanoTime();

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
