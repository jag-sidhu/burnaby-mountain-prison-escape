package Spring2026Team10;

import java.awt.Point;
import java.util.Arrays;

public class PrisonMap {
    public static final int ROWS = 31;
    public static final int COLS = 42;

    private final TileType[][] tiles = new TileType[ROWS][COLS];
    private final Point startTile = new Point(1, 14);
    private final Point endTile = new Point(40, 14);

    public PrisonMap() {
        reset();
    }

    public static void update() {
        // Static map for now.
    }

    public void reset() {
        for (int row = 0; row < ROWS; row++) {
            Arrays.fill(tiles[row], TileType.FLOOR);
        }

        // Top section
        fillRect(2, 3, 2, 5);
        fillRect(1, 18, 5, 1);
        fillRect(3, 26, 1, 10);
        fillRect(3, 35, 6, 1);

        // Upper middle
        fillRect(5, 12, 7, 1);
        fillRect(7, 4, 4, 2);
        fillRect(8, 13, 1, 4);
        fillRect(7, 27, 3, 5);

        // Center
        fillRect(11, 16, 3, 2);
        fillRect(10, 21, 8, 1);
        fillRect(12, 22, 1, 8);
        fillRect(15, 13, 1, 8);
        fillRect(15, 30, 3, 5);

        // Lower middle / lower left
        fillRect(18, 7, 4, 2);
        fillRect(20, 13, 8, 1);
        fillRect(23, 14, 1, 7);
        fillRect(24, 3, 2, 5);

        // Lower right
        fillRect(21, 29, 1, 10);
        fillRect(21, 29, 8, 2);
        fillRect(27, 20, 2, 11);

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

    private boolean isInside(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    private void fillRect(int topRow, int leftCol, int height, int width) {
        for (int row = topRow; row < topRow + height; row++) {
            for (int col = leftCol; col < leftCol + width; col++) {
                setTile(row, col, TileType.WALL);
            }
        }
    }

    private void setTile(int row, int col, TileType tile) {
        if (!isInside(row, col)) {
            return;
        }
        tiles[row][col] = tile;
    }

    public boolean isWalkable(int row, int col) {
        if (!isInside(row, col)) {
            return false;
        }
        TileType tile = getTile(row, col);
        return tile == TileType.FLOOR || tile == TileType.START || tile == TileType.END;
    }
}
