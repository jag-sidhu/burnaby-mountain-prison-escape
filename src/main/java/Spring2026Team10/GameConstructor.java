package Spring2026Team10;

import javax.swing.*;

/**
 * Responsible for initializing the game window and creating a nea game instance.
 */
public class GameConstructor {
    /**
     * Creates the main game window. Initializes game components and starts the game loop.
     */
    public void createGame() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setTitle("Escape From The Burnaby Mountain Prison");
        PrisonMap map = new PrisonMap();
        MapPanel mapPanel = new MapPanel(map);
        frame.add(mapPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Game game = new Game(mapPanel);
        game.start();
    }
}
