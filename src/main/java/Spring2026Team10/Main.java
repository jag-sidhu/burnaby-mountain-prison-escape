package Spring2026Team10;

import javax.swing.JFrame;

/**
 * Main function of the game. Constructs a new game instance to run.
 *
 */
public class Main {
    /**
     * The standard execution entry point for the application.
     * @param args Command line arguments (which we do not use).
     */
    public static void main(String[] args) {
      GameConstructor game = new GameConstructor();
      game.createGame();

    }
}
