package Spring2026Team10;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private GameState state;
    private Player player;
    private PrisonMap map;
    private HUD hud;
    private final List<Guard> guards;
    private Powerups powerups;

    public Game() {
        state = GameState.MENU;
        player = new Player();
        map = new PrisonMap();
        hud = new HUD();
        guards = new ArrayList<>();
        powerups = new Powerups();
    }

    public void start() {}
    public void update() {
        if (state == GameState.PLAYING) {
            player.update();
            guards.forEach(Guard::update);
            powerups.update();
            map.update();
        }
    }
    public void render(Graphics g) {}
    public void handleInput(KeyEvent e) {}
    public void resetGame() {
        map.reset();
        player.reset();
        guards.forEach(Guard::reset);
    }

    private void changeState(GameState state) {
        this.state = state;
    }

}
