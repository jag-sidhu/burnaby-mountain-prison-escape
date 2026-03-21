package Spring2026Team10;

import org.junit.jupiter.api.Test;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestPrisonMapAndPanel {

    @Test
    public void testMapInitializationCreatesExpectedGridAndSpecialTiles() {
        PrisonMap map = new PrisonMap();

        assertEquals(78, map.getRows(), "Scaled row count does not match");
        assertEquals(105, map.getCols(), "Scaled column count does not match");

        Point start = map.getStartTile();
        Point end = map.getEndTile();

        assertEquals(new Point(3, 35), start, "Start tile incorrectly placed");
        assertEquals(new Point(100, 35), end, "End tile incorrectly placed");
        assertEquals(TileType.START, map.getTile(start.y, start.x), "Start tile type does not match");
        assertEquals(TileType.END, map.getTile(end.y, end.x), "End tile type does not match");
        assertTrue(map.isWalkable(start.y, start.x), "Start tile should be walkable");
        assertTrue(map.isWalkable(end.y, end.x), "End tile should be walkable");

        start.translate(10, 10);
        end.translate(-10, -10);
        assertEquals(new Point(3, 35), map.getStartTile(), "Start tile changed unexpectedly");
        assertEquals(new Point(100, 35), map.getEndTile(), "End tile changed unexpectedly");
    }

    @Test
    public void testStartCellLayoutBuildsWallsDoorwayAndDecorations() {
        PrisonMap map = new PrisonMap();
        Point start = map.getStartTile();
        int left = Math.max(1, start.x - 2);
        int top = start.y - 2;
        int right = left + 4;

        assertEquals(TileType.WALL, map.getTile(top, left), "Cell wall tile does not match");
        assertFalse(map.isWalkable(top, left), "Cell wall should not be walkable");
        assertEquals(GroundType.FLOOR, map.getGroundType(top, left), "Cell wall ground does not match");
        assertEquals(MapDecoration.CELL_BARS, map.getDecoration(top, left), "Cell wall decoration does not match");

        assertEquals(TileType.FLOOR, map.getTile(start.y, right), "Doorway tile does not match");
        assertTrue(map.isWalkable(start.y, right), "Doorway should be walkable");
        assertEquals(MapDecoration.NONE, map.getDecoration(start.y, right), "Doorway decoration should be empty");

        assertEquals(MapDecoration.BED_TOP, map.getDecoration(top + 1, left + 1), "Bed top decoration does not match");
        assertEquals(MapDecoration.BED_BOTTOM, map.getDecoration(top + 2, left + 1), "Bed bottom decoration does not match");
        assertEquals(MapDecoration.TOILET, map.getDecoration(top + 3, right - 1), "Toilet decoration does not match");
    }

    @Test
    public void testOutOfBoundsQueriesBehaveSafely() {
        PrisonMap map = new PrisonMap();

        assertEquals(TileType.WALL, map.getTile(-1, 0), "Out of bounds tile should be wall");
        assertEquals(GroundType.WALL, map.getGroundType(map.getRows(), 0), "Out of bounds ground should be wall");
        assertEquals(MapDecoration.NONE, map.getDecoration(0, map.getCols()), "Out of bounds decoration should be empty");
        assertFalse(map.isWalkable(0, -1), "Out of bounds tile should not be walkable");
        assertFalse(map.hasCoin(-1, -1), "Out of bounds tile should not have a coin");
        assertFalse(map.collectCoin(map.getRows(), map.getCols()), "Out of bounds coin collect should fail");
    }

    @Test
    public void testPlayerMovementUsesMapCollisionAtStartCellWall() {
        PrisonMap map = new PrisonMap();
        Point start = map.getStartTile();
        Player player = new Player(start.x, start.y, map);

        player.move(-1, 0);

        assertEquals(start.x, player.getX(), "Player x should not change");
        assertEquals(start.y, player.getY(), "Player y should not change");
        assertEquals(start.x, player.getPosX(), 0.0001, "Player precise x should not change");
        assertEquals(start.y, player.getPosY(), 0.0001, "Player precise y should not change");
        assertFalse(player.isMoving(), "Player should not be moving");

        player.move(1, 0);

        assertTrue(player.getPosX() > start.x, "Player should move right");
        assertEquals(Entity.Direction.RIGHT, player.getFacing(), "Facing direction does not match");
        assertTrue(player.isMoving(), "Player should be moving");
    }

    @Test
    public void testSpawnCoinsPlacesRequestedAmountOnlyOnFloorTiles() {
        PrisonMap map = new PrisonMap();
        Point start = map.getStartTile();
        Point end = map.getEndTile();

        map.spawnCoins(40);

        assertEquals(40, countCoins(map), "Coin count does not match");
        assertFalse(map.hasCoin(start.y, start.x), "Start tile should not have a coin");
        assertFalse(map.hasCoin(end.y, end.x), "End tile should not have a coin");

        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                if (map.hasCoin(row, col)) {
                    assertEquals(TileType.FLOOR, map.getTile(row, col), "Coin tile should be floor");
                }
            }
        }
    }

    @Test
    public void testSpawnCoinsClearsPreviousStateAndCollectedCoinIsRemoved() {
        PrisonMap map = new PrisonMap();

        map.spawnCoins(10);
        Point coin = findFirstCoin(map);

        assertNotNull(coin, "Coin should be found");
        assertTrue(map.collectCoin(coin.y, coin.x), "Coin collect should succeed");
        assertFalse(map.hasCoin(coin.y, coin.x), "Coin should be removed");
        assertFalse(map.collectCoin(coin.y, coin.x), "Second coin collect should fail");
        assertEquals(9, countCoins(map), "Coin count after collect does not match");

        map.spawnCoins(1);

        assertEquals(1, countCoins(map), "Coin count after respawn does not match");
    }

    @Test
    public void testSpawnCoinsCapsAtAvailableFloorTileCount() {
        PrisonMap map = new PrisonMap();
        int floorTileCount = countTilesOfType(map, TileType.FLOOR);

        map.spawnCoins(floorTileCount + 250);

        assertEquals(floorTileCount, countCoins(map), "Coin cap does not match");
    }

    @Test
    public void testResetClearsDynamicTilesAndRestoresBaseMapState() {
        PrisonMap map = new PrisonMap();
        Point start = map.getStartTile();
        Point end = map.getEndTile();

        map.spawnCoins(15);
        map.spawnHazards(6);
        map.spawnRewards();
        map.spawnPowerups();

        assertTrue(countCoins(map) > 0, "Setup should add coins");
        assertTrue(countTilesOfType(map, TileType.HAZARD) > 0, "Setup should add hazards");
        assertEquals(3, countTilesOfType(map, TileType.REWARD), "Reward count before reset does not match");
        assertEquals(4, countTilesOfType(map, TileType.POWERUP), "Powerup count before reset does not match");

        map.reset();

        assertEquals(0, countCoins(map), "Coins should be cleared");
        assertEquals(0, countTilesOfType(map, TileType.HAZARD), "Hazards should be cleared");
        assertEquals(0, countTilesOfType(map, TileType.REWARD), "Rewards should be cleared");
        assertEquals(0, countTilesOfType(map, TileType.POWERUP), "Powerups should be cleared");
        assertEquals(TileType.START, map.getTile(start.y, start.x), "Start tile after reset does not match");
        assertEquals(TileType.END, map.getTile(end.y, end.x), "End tile after reset does not match");
        assertTrue(map.isWalkable(start.y, start.x), "Start tile after reset should be walkable");
        assertTrue(map.isWalkable(end.y, end.x), "End tile after reset should be walkable");
    }

    @Test
    public void testMapPanelInitializationTracksMapStateAndRendersBaseScene() {
        PrisonMap map = new PrisonMap();

        assertDoesNotThrow(() -> {
            MapPanel panel = new MapPanel(map);
            panel.addGuardSpawn(7, 9);
            panel.addGuardSpawn(12, 15);
            panel.setTimeText("01:23");
            panel.setScoreText("250");
            panel.setHazards(List.of());
            panel.setRewards(List.of());
            panel.setPowerups(List.of());

            assertSame(map, panel.getPrisonMap(), "MapPanel map does not match");
            assertEquals(new Dimension(map.getCols() * 10 + 1, map.getRows() * 10 + 1), panel.getPreferredSize(),
                    "Panel size does not match");
            assertEquals(List.of(new Point(7, 9), new Point(12, 15)), panel.getGuardSpawns(),
                    "Guard spawns do not match");

            panel.setSize(panel.getPreferredSize());
            BufferedImage canvas = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D graphics = canvas.createGraphics();
            try {
                panel.paint(graphics);
            } finally {
                graphics.dispose();
            }
        }, "MapPanel setup should not throw");
    }

    @Test
    public void testGameStartMatchRespawnsCoinsAndMapPanelRendersLiveGameState() {
        PrisonMap map = new PrisonMap();
        MapPanel panel = new MapPanel(map);
        Game game = new Game(panel);

        game.startMatch();

        assertEquals(GameState.PLAYING, game.getState(), "Game state does not match");
        assertEquals(50, countCoins(map), "Coin count after start does not match");

        Point collectedCoin = findFirstCoin(map);
        assertNotNull(collectedCoin, "Match coin should be found");
        assertTrue(map.collectCoin(collectedCoin.y, collectedCoin.x), "Match coin collect should succeed");
        assertEquals(49, countCoins(map), "Coin count after match collect does not match");

        game.startMatch();

        assertEquals(50, countCoins(map), "Coin count after restart does not match");

        assertDoesNotThrow(() -> {
            panel.setSize(panel.getPreferredSize());
            BufferedImage canvas = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D graphics = canvas.createGraphics();
            try {
                panel.paint(graphics);
            } finally {
                graphics.dispose();
            }
        }, "MapPanel game render should not throw");
    }

    private int countCoins(PrisonMap map) {
        int count = 0;
        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                if (map.hasCoin(row, col)) {
                    count++;
                }
            }
        }
        return count;
    }

    private Point findFirstCoin(PrisonMap map) {
        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                if (map.hasCoin(row, col)) {
                    return new Point(col, row);
                }
            }
        }
        return null;
    }

    private int countTilesOfType(PrisonMap map, TileType tileType) {
        int count = 0;
        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                if (map.getTile(row, col) == tileType) {
                    count++;
                }
            }
        }
        return count;
    }
}
