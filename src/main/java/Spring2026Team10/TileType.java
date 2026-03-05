package Spring2026Team10;

import java.awt.Color;

public enum TileType {
    WALL(new Color(36, 46, 58), false),
    FLOOR(new Color(212, 220, 228), true),
    START(new Color(89, 174, 93), true),
    END(new Color(219, 81, 81), true),
    HAZARD(new Color(150, 50, 200), true),
    REWARD(new Color(0, 137, 65), true ),
    POWERUP(new Color(0, 200, 255), true);

    private final Color color;
    private final boolean walkable;

    TileType(Color color, boolean walkable) {
        this.color = color;
        this.walkable = walkable;
    }

    public Color getColor() {
        return color;
    }

    public boolean isWalkable() {
        return walkable;
    }
}
