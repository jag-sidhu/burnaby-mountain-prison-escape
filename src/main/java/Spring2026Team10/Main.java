package Spring2026Team10;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
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
