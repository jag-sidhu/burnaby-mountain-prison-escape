package Spring2026Team10;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

public class Game {
    private GameState state;
    private Player player;
    private PrisonMap map;
    private HUD hud;
    private final List<Guard> guards;
    private Powerups powerups;
    private Timer timer;


    public Game() {
        state = GameState.MENU;
        player = new Player();
        map = new PrisonMap();
        hud = new HUD();
        guards = new ArrayList<>();
        powerups = new Powerups();
    }

    public void start() {
        resetGame();
        changeState(GameState.PLAYING);

        timer = new Timer(16, e -> {
            update();
            PrisonMap.update();
        });
        timer.start();
    }
    public void update() {
        if (state == GameState.PLAYING) {
            player.update();
            guards.forEach(Guard::update);
            powerups.update();
            map.update();
        }
    }

    public void render(Graphics g) {
        switch(state) {
            case MENU:
                drawMenu(g);
                break;
            case PLAYING:
                drawGame();
                break;
            case GAME_OVER:
                drawGameOver(g);
                break;
            case LEVEL_COMPLETE:
                drawLevelComplete(g);
                break;
        }
    }

    public void handleInput(KeyEvent e) {}

    public void resetGame() {
        map.reset();
        player.reset();
        guards.forEach(Guard::reset);
    }

    private void changeState(GameState state) {
        this.state = state;

        if(state == GameState.GAME_OVER && timer != null) {
            timer.stop();
        }
    }

}
