package Spring2026Team10;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Custom keyboard input listener that tracks the state of the specific movement keys.
 * Implements KeyListener to monitor keyboard events.
 */
public class KeyHandler implements KeyListener {
    public boolean upPressed, downPressed, leftPressed, rightPressed, escapePressed;

    /**
     * Resets all key tracking to a default of false.
     */
    public void clear() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        escapePressed = false;
    }

    /**
     *Method implemented by the keyListener interface. Not used in the game.
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(KeyEvent e) {

    }

    /**
     * Invoked when a key is pressed.
     * Processes the key and sets the corresponding directional boolean to true.
     * Supports WASD and arrow keys for movement.
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            upPressed = true;
        }
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            downPressed = true;
        }
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            leftPressed = true;
        }
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            rightPressed = true;
        }
        if (code == KeyEvent.VK_ESCAPE) {
            escapePressed = true;
        }
    }

    /**
     * Invoked when a key is released.
     * Processes the key and sets the corresponding directional boolean to false.
     * Supports WASD and arrow keys for movement.
     * @param e the event to be processed
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            upPressed = false;
        }
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            downPressed = false;
        }
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            leftPressed = false;
        }
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            rightPressed = false;
        }
        if (code == KeyEvent.VK_ESCAPE) {
            escapePressed = false;
        }
    }
}
