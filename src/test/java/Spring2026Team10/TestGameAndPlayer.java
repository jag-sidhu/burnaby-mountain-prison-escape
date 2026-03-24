package Spring2026Team10;

import org.junit.jupiter.api.Test;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the newer game-state flow and player HUD-facing status behavior.
 */
public class TestGameAndPlayer {

    @Test
    public void testDifficultySelectionClampsAndStartMatchUsesSelectedLives() {
        PrisonMap map = new PrisonMap();
        MapPanel panel = new MapPanel(map);
        Game game = new SilentGame(panel);
        Player player = getPlayer(game);

        assertEquals(Game.Difficulty.EASY, game.getDifficulty(), "Default difficulty should be easy");
        assertEquals("Easy", game.getDifficultyLabel(), "Default difficulty label does not match");

        game.decreaseDifficulty();
        assertEquals(Game.Difficulty.EASY, game.getDifficulty(), "Difficulty should stay at easy");

        game.increaseDifficulty();
        assertEquals(Game.Difficulty.MEDIUM, game.getDifficulty(), "Difficulty should move to medium");

        game.increaseDifficulty();
        game.increaseDifficulty();
        assertEquals(Game.Difficulty.HARD, game.getDifficulty(), "Difficulty should clamp at hard");
        assertEquals("Hard", game.getDifficultyLabel(), "Hard difficulty label does not match");

        game.startMatch();
        assertEquals(1, player.getLives(), "Hard mode should start with one life");

        game.decreaseDifficulty();
        game.startMatch();
        assertEquals(2, player.getLives(), "Medium mode should start with two lives");

        game.decreaseDifficulty();
        game.startMatch();
        assertEquals(3, player.getLives(), "Easy mode should start with three lives");
    }

    @Test
    public void testPauseToggleRequiresKeyReleaseBetweenTransitions() {
        PrisonMap map = new PrisonMap();
        MapPanel panel = new MapPanel(map);
        Game game = new SilentGame(panel);

        game.startMatch();
        assertEquals(GameState.PLAYING, game.getState(), "Match should begin in playing state");

        game.keyHandler.escapePressed = true;
        game.update();
        assertEquals(GameState.FROZEN, game.getState(), "Escape should pause the game");

        game.update();
        assertEquals(GameState.FROZEN, game.getState(), "Holding escape should not instantly unpause");

        game.keyHandler.escapePressed = false;
        game.update();
        assertEquals(GameState.FROZEN, game.getState(), "Releasing escape should keep the game paused");

        game.keyHandler.escapePressed = true;
        game.update();
        assertEquals(GameState.PLAYING, game.getState(), "Pressing escape again should resume the game");
    }

    @Test
    public void testMenuAndPauseMouseClicksRouteToExpectedStates() {
        PrisonMap map = new PrisonMap();
        MapPanel panel = new MapPanel(map);
        panel.setSize(panel.getPreferredSize());
        Game game = new SilentGame(panel);

        int centerX = panel.getWidth() / 2;
        int centerY = panel.getHeight() / 2;

        click(panel, centerX + 110, centerY + 95);
        assertEquals(Game.Difficulty.MEDIUM, game.getDifficulty(), "Right difficulty button should increase difficulty");

        click(panel, centerX, centerY);
        assertEquals(GameState.STORY, game.getState(), "Start button should open the story screen");

        click(panel, 10, 10);
        assertEquals(GameState.PLAYING, game.getState(), "Story click should start the match");

        game.keyHandler.escapePressed = true;
        game.update();
        assertEquals(GameState.FROZEN, game.getState(), "Escape should open the pause menu");

        game.keyHandler.escapePressed = false;
        game.update();
        click(panel, centerX, centerY);
        assertEquals(GameState.PLAYING, game.getState(), "Resume button should return to gameplay");

        game.keyHandler.escapePressed = true;
        game.update();
        game.keyHandler.escapePressed = false;
        game.update();
        click(panel, centerX, centerY + 55);
        assertEquals(GameState.MENU, game.getState(), "Pause menu exit button should return to the main menu");
    }

    @Test
    public void testPlayerHitInvulnerabilityBlocksExtraDamageAndExpires() {
        Player player = new Player(10, 10, new PrisonMap());
        KeyHandler keyHandler = new KeyHandler();

        player.loseLife();
        assertEquals(2, player.getLives(), "First hit should remove one life");
        assertTrue(player.isInvulnerable(), "Player should become invulnerable after taking damage");

        player.loseLife();
        assertEquals(2, player.getLives(), "Second hit during invulnerability should be ignored");

        boolean sawVisibleFlash = player.isHitFlashVisible();
        boolean sawHiddenFlash = !player.isHitFlashVisible();
        for (int i = 0; i < 60; i++) {
            player.update(keyHandler);
            sawVisibleFlash |= player.isHitFlashVisible();
            sawHiddenFlash |= !player.isHitFlashVisible();
        }

        assertFalse(player.isInvulnerable(), "Hit invulnerability should expire after its timer runs out");
        assertTrue(sawVisibleFlash, "Flash state should become visible during invulnerability");
        assertTrue(sawHiddenFlash, "Flash state should also hide on alternating frames");

        player.loseLife();
        assertEquals(1, player.getLives(), "Player should take damage again after invulnerability expires");
    }

    @Test
    public void testPlayerResetClearsCollectedRewardsAndTemporaryHudState() {
        PrisonMap map = new PrisonMap();
        Point start = map.getStartTile();
        Player player = new Player(start.x, start.y, map);

        player.collectReward(RewardType.LAPTOP);
        player.collectReward(RewardType.STUDENT_ID);
        player.gainScore(125);
        player.loseLife();
        player.tieHands(20);
        player.applySlowdown(20);
        player.invertControls(20);
        player.freezeGuards(20);
        player.showMessage("Testing", 20);
        player.move(1, 0);

        player.reset();

        assertEquals(start.x, player.getX(), "Player x should reset to the start tile");
        assertEquals(start.y, player.getY(), "Player y should reset to the start tile");
        assertEquals(3, player.getLives(), "Lives should reset to the default value");
        assertEquals(0, player.getScore(), "Score should reset to zero");
        assertEquals(0, player.getReward(), "Collected reward count should reset to zero");
        assertFalse(player.hasCollectedReward(RewardType.LAPTOP), "Laptop reward should be cleared on reset");
        assertFalse(player.hasCollectedReward(RewardType.STUDENT_ID), "Student ID reward should be cleared on reset");
        assertEquals(Player.StatusState.NORMAL, player.getStatusState(), "Status should reset to normal");
        assertFalse(player.isHandsTied(), "Hands tied should clear on reset");
        assertFalse(player.isGuardsFrozen(), "Guard freeze should clear on reset");
        assertFalse(player.isInvulnerable(), "Hit invulnerability should clear on reset");
        assertFalse(player.isPopupVisible(), "Popup messages should clear on reset");
    }

    @Test
    public void testDuplicateRewardTypeOnlyCountsTowardCompletionOnce() {
        Player player = new Player(0, 0, new PrisonMap());
        Rewards firstLaptop = new Rewards(5, 5, RewardType.LAPTOP);
        Rewards secondLaptop = new Rewards(6, 6, RewardType.LAPTOP);

        firstLaptop.applyTo(player);
        secondLaptop.applyTo(player);

        assertEquals(100, player.getScore(), "Duplicate reward pickups should still award their score");
        assertEquals(1, player.getReward(), "Duplicate reward types should only count once toward completion");
        assertTrue(player.hasCollectedReward(RewardType.LAPTOP), "Laptop should be marked as collected");
    }

    @Test
    public void testGameTransitionsToLoseAndWinStatesAndPanelRendersOverlays() {
        PrisonMap map = new PrisonMap();
        MapPanel panel = new MapPanel(map);
        panel.setSize(panel.getPreferredSize());
        Game game = new SilentGame(panel);
        Player player = getPlayer(game);

        game.startMatch();
        player.setLives(1);
        player.loseLife();
        game.update();
        assertEquals(GameState.GAME_OVER, game.getState(), "Zero lives should trigger the lose screen");
        assertPanelPaints(panel);

        game.startMatch();
        player.collectReward(RewardType.LAPTOP);
        player.collectReward(RewardType.STUDENT_ID);
        player.collectReward(RewardType.RACCOON);
        Point end = map.getEndTile();
        player.x = end.x;
        player.y = end.y;
        player.posX = end.x;
        player.posY = end.y;

        game.update();
        assertEquals(GameState.LEVEL_COMPLETE, game.getState(), "All rewards plus exit tile should trigger the win screen");
        assertPanelPaints(panel);
    }

    @Test
    public void testAudio() {
        //Test music and sound effects run without execptions
        PrisonMap map = new PrisonMap();
        MapPanel panel = new MapPanel(map);
        panel.setSize(panel.getPreferredSize());
        Game game = new Game(panel);

        assertDoesNotThrow(() -> {
            game.playMusic();
            game.stopMusic();
            game.playSoundEffect(2);
        }, "Should not throw exception when playing audio");
    }

    @Test
    public void testStartGame() {
        //Test game startup and ensure game is initialized on the correct state
        PrisonMap map = new PrisonMap();
        MapPanel panel = new MapPanel(map);
        panel.setSize(panel.getPreferredSize());
        Game game = new SilentGame(panel);
        game.start();
        assertEquals(GameState.MENU, game.getState(), "GameState should be menu on start");
    }

    @Test
    public void testRestart() {
        //Test the restart method for any execptions
        PrisonMap map = new PrisonMap();
        MapPanel panel = new MapPanel(map);
        panel.setSize(panel.getPreferredSize());
        Game game = new SilentGame(panel);

        assertDoesNotThrow(() -> game.restartMatch(), "Should not throw exception when restarting");
    }

    @Test void testThreadInterrupt() {
        //Interrupt the game thread to test the catch stament
        PrisonMap map = new PrisonMap();
        MapPanel panel = new MapPanel(map);
        panel.setSize(panel.getPreferredSize());
        Game game = new SilentGame(panel);

        game.start();
        assertDoesNotThrow(() -> {
            Game.gameThread.interrupt();
            Thread.sleep(15);
        });
    }

    private Player getPlayer(Game game) {
        try {
            Field field = Game.class.getDeclaredField("player");
            field.setAccessible(true);
            return (Player) field.get(game);
        } catch (ReflectiveOperationException e) {
            fail("Unable to access the game player for testing");
            return null;
        }
    }

    private void click(MapPanel panel, int x, int y) {
        MouseEvent event = new MouseEvent(
                panel,
                MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                x,
                y,
                1,
                false,
                MouseEvent.BUTTON1
        );
        panel.dispatchEvent(event);
    }

    private void assertPanelPaints(MapPanel panel) {
        assertDoesNotThrow(() -> {
            Dimension size = panel.getPreferredSize();
            panel.setSize(size);
            BufferedImage canvas = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D graphics = canvas.createGraphics();
            try {
                panel.paint(graphics);
            } finally {
                graphics.dispose();
            }
        }, "Panel render should not throw");
    }

    private static class SilentGame extends Game {
        SilentGame(MapPanel mapPanel) {
            super(mapPanel);
        }

        @Override
        public void playMusic() {
        }

        @Override
        public void stopMusic() {
        }

        @Override
        public void playSoundEffect(int i) {
        }

        @Override
        public void exitGame() {
        }
    }
}
