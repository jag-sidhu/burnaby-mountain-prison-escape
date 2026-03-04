package Spring2026Team10;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PrisonMap {
    private static final int BASE_ROWS = 31;
    private static final int BASE_COLS = 42;
    private static final double MAP_SCALE = 2.5;
    public static final int ROWS = scaleCoord(BASE_ROWS);
    public static final int COLS = scaleCoord(BASE_COLS);

    private static final Point BASE_START_TILE = new Point(1, 14);
    private static final Point BASE_END_TILE = new Point(40, 14);

    private static final int[][] BASE_WALL_RECTS = {
            // Top section
            {2, 3, 2, 5},
            {1, 18, 5, 1},
            {3, 26, 1, 10},
            {3, 35, 6, 1},

            // Upper middle
            {5, 12, 7, 1},
            {7, 4, 4, 2},
            {8, 13, 1, 4},
            {7, 27, 3, 5},

            // Center
            {11, 16, 3, 2},
            {10, 21, 8, 1},
            {12, 22, 1, 8},
            {15, 13, 1, 8},
            {15, 30, 3, 5},

            // Lower middle / lower left
            {18, 7, 4, 2},
            {20, 13, 8, 1},
            {23, 14, 1, 7},
            {24, 3, 2, 5},

            // Lower right
            {21, 29, 1, 10},
            {21, 29, 8, 2},
            {27, 20, 2, 11}
    };

    private final TileType[][] tiles = new TileType[ROWS][COLS];
    private final boolean[][] coins = new boolean[ROWS][COLS];
    private final Point startTile = scalePoint(BASE_START_TILE);
    private final Point endTile = scalePoint(BASE_END_TILE);
    private final Random random = new Random();

    public PrisonMap() {
        reset();
    }

    public static void update() {
        // Static map for now.
    }

    public final void reset() {
        for (int row = 0; row < ROWS; row++) {
            Arrays.fill(tiles[row], TileType.FLOOR);
            Arrays.fill(coins[row], false);
        }

        for (int[] rect : BASE_WALL_RECTS) {
            fillScaledRect(rect[0], rect[1], rect[2], rect[3], TileType.WALL);
        }

        setTile(startTile.y, startTile.x, TileType.START);
        setTile(endTile.y, endTile.x, TileType.END);
    }

    public int getRows() {
        return ROWS;
    }

    public int getCols() {
        return COLS;
    }

    public TileType getTile(int row, int col) {
        if (!isInside(row, col)) {
            return TileType.WALL;
        }
        return tiles[row][col];
    }

    public Point getStartTile() {
        return new Point(startTile);
    }

    public Point getEndTile() {
        return new Point(endTile);
    }

    public void spawnCoins(int coinCount) {
        clearCoins();

        List<Point> candidates = new ArrayList<>();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (tiles[row][col] == TileType.FLOOR) {
                    candidates.add(new Point(col, row));
                }
            }
        }

        Collections.shuffle(candidates, random);
        int amountToPlace = Math.min(coinCount, candidates.size());
        for (int i = 0; i < amountToPlace; i++) {
            Point tile = candidates.get(i);
            coins[tile.y][tile.x] = true;
        }
    }

    public boolean hasCoin(int row, int col) {
        return isInside(row, col) && coins[row][col];
    }

    public boolean collectCoin(int row, int col) {
        if (!hasCoin(row, col)) {
            return false;
        }
        coins[row][col] = false;
        return true;
    }

    private boolean isInside(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    private void fillRect(int topRow, int leftCol, int height, int width, TileType tileType) {
        for (int row = topRow; row < topRow + height; row++) {
            for (int col = leftCol; col < leftCol + width; col++) {
                setTile(row, col, tileType);
            }
        }
    }

    private void fillScaledRect(int topRow, int leftCol, int height, int width, TileType tileType) {
        int scaledTop = scaleCoord(topRow);
        int scaledLeft = scaleCoord(leftCol);
        int scaledBottom = scaleCoord(topRow + height);
        int scaledRight = scaleCoord(leftCol + width);

        int scaledHeight = Math.max(1, scaledBottom - scaledTop);
        int scaledWidth = Math.max(1, scaledRight - scaledLeft);
        fillRect(scaledTop, scaledLeft, scaledHeight, scaledWidth, tileType);
    }

    private static int scaleCoord(int value) {
        return (int) Math.round(value * MAP_SCALE);
    }

    private static Point scalePoint(Point point) {
        return new Point(scaleCoord(point.x), scaleCoord(point.y));
    }

    private void setTile(int row, int col, TileType tile) {
        if (!isInside(row, col)) {
            return;
        }
        tiles[row][col] = tile;
    }

    private void clearCoins() {
        for (int row = 0; row < ROWS; row++) {
            Arrays.fill(coins[row], false);
        }
    }

    public boolean isWalkable(int row, int col) {
        if (!isInside(row, col)) {
            return false;
        }
        return getTile(row, col).isWalkable();
    }

    public void spawnHazards(int count) {
        List<Point> candidates = new ArrayList<>();
        
        // Finds every empty floor tile on the map
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (tiles[row][col] == TileType.FLOOR) {
                    candidates.add(new Point(col, row));
                }
            }
        }

        // Shuffles the list to randomize them
        Collections.shuffle(candidates, random);
        
        int amountToPlace = Math.min(count, candidates.size());
        for (int i = 0; i < amountToPlace; i++) {
            Point p = candidates.get(i);
            tiles[p.y][p.x] = TileType.HAZARD;
        }
    }

    public void spawnRewards() {
        List<Point> candidates = new ArrayList<>();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (tiles[row][col] == TileType.FLOOR) {
                    candidates.add(new Point(col, row));
                }
            }
        }

        Collections.shuffle(candidates, random);
        for (int i = 0; i < 3; i++) {
            Point p = candidates.get(i);
            tiles[p.y][p.x] = TileType.REWARD;
        }
    }

}
