package Spring2026Team10;

import javax.swing.*;

public class GameConstructor {
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
