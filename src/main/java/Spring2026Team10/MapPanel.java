package Spring2026Team10;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JPanel;

/**
 * Renders the map, entities, HUD, and menu overlays for the game.
 * <p>
 * This panel also handles mouse clicks for menu, pause, story, and end-screen buttons.
 * </p>
 */
public class MapPanel extends JPanel {
    // Half edge length -> quarter area per tile.
    private static final int CELL_SIZE = 10;
    private static final Color GRID_COLOR = new Color(60, 60, 60);
    private static final Color COIN_COLOR = new Color(242, 188, 34);
    private static final Color COIN_BORDER_COLOR = new Color(178, 131, 22);
    private static final Color HUD_PANEL_BG = new Color(10, 14, 20, 215);
    private static final Color HUD_PANEL_BORDER = new Color(210, 220, 235, 150);
    private static final Color HUD_PANEL_SHADOW = new Color(0, 0, 0, 120);
    private static final Color HUD_TEXT_COLOR = new Color(243, 246, 251, 235);
    private static final int HUD_HEIGHT = 0;
    private static final int UI_FONT_SIZE = Math.max(15, Math.round(CELL_SIZE * 1.75f));
    private static final float BORDER_STROKE = Math.max(1.25f, CELL_SIZE * 0.125f);
    private static final float CAMERA_ZOOM = 5.5f;
    private static final float VISIBILITY_RADIUS_TILES = 10.5f;

    private final PrisonMap prisonMap;
    private Game game;
    private Player player;
    private java.util.List<Guard> guards = new java.util.ArrayList<>();
    private java.util.List<java.awt.Point> guardSpawns = new java.util.ArrayList<>();
    private String timeText = "XXX";
    private String scoreText = "XXX";
    private float cameraX = 0f;
    private float cameraY = 0f;
    private long storyStartTime = 0;

    private java.util.List<Hazard> hazards = new java.util.ArrayList<>();
    private java.util.List<Rewards> rewards = new java.util.ArrayList<>();
    private java.util.List<Powerups> powerups = new java.util.ArrayList<>();
    private final Map<Entity.Direction, BufferedImage[]> playerSprites = new EnumMap<>(Entity.Direction.class);
    private final Map<Entity.Direction, BufferedImage[]> guardSprites = new EnumMap<>(Entity.Direction.class);
    private final Map<GroundType, BufferedImage> groundSprites = new EnumMap<>(GroundType.class);
    private final Map<MapDecoration, BufferedImage> decorationSprites = new EnumMap<>(MapDecoration.class);
    private final Map<HazardType, BufferedImage> hazardSprites = new EnumMap<>(HazardType.class);
    private final Map<RewardType, BufferedImage> rewardSprites = new EnumMap<>(RewardType.class);
    private final Map<RewardType, BufferedImage> rewardEmptySprites = new EnumMap<>(RewardType.class);
    private final Map<PowerupType, BufferedImage> powerupSprites = new EnumMap<>(PowerupType.class);
    private BufferedImage heartFilledSprite;
    private BufferedImage heartEmptySprite;

    /**
     * Adds a specific map coordinate to the list of potential guard spawn points.
     * @param col The x-coordinate column.
     * @param row The y-coordinate row.
     */
    public void addGuardSpawn(int col, int row) {
        guardSpawns.add(new java.awt.Point(col, row));
    }
    
    /**
     * Retrieves the list of all valid guard spawn points on the map.
     * @return A List of points representing spawn coordinates.
     */
    public java.util.List<java.awt.Point> getGuardSpawns() {
        return guardSpawns;
    }

    public void setHazards(java.util.List<Hazard> hazards) {
        this.hazards = hazards;
    }

    /**
     * Updates the list of active rewards to be rendered on the map.
     * @param rewards The lust of Rewards objects to be tracked and displayed on the map.
     */
    public void setRewards(java.util.List<Rewards> rewards) {
        this.rewards = rewards;
    }

    public void setPowerups(java.util.List<Powerups> powerups) {
        this.powerups = powerups;
    }

    /**
     * Constructs the main map rendering panel and loads the sprite assets used by the game.
     * @param prisonMap The map to render.
     */
    public MapPanel(PrisonMap prisonMap) {
        this.prisonMap = prisonMap;
        int width = prisonMap.getCols() * CELL_SIZE + 1;
        int height = prisonMap.getRows() * CELL_SIZE + 1;
        setPreferredSize(new Dimension(width, height));
        setBackground(new Color(214, 214, 214));
        loadSprites();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleScreenClick(e.getX(), e.getY());
            }
        });
    }

    /**
     * Updates the HUD to display the current formatted time.
     * @param timeText The string representing elapsed time.
     */
    public void setTimeText(String timeText) {
        this.timeText = timeText;
        repaint();
    }

    /**
     * Updates the HUD to display the player's current score.
     * @param scoreText The string representing the score.
     */
    public void setScoreText(String scoreText) {
        this.scoreText = scoreText;
        repaint();
    }

    /**
     * Retrieves the current prison map data structure.
     * @return The PrisonMap instance being rendered.
     */
    public PrisonMap getPrisonMap() {
        return prisonMap;
    }

    /**
     * Links the panel to the main game engine.
     * @param game The Game instance controlling the logic.
     */
    public void setGame(Game game) {
        this.game = game;
        repaint();
    }

    /**
     * Loads all sprite assets needed for characters, pickups, hazards, and HUD elements.
     */
    private void loadSprites() {
        groundSprites.put(GroundType.FLOOR, loadSprite("/sprites/Map/floor.png"));
        groundSprites.put(GroundType.GRASS, loadSprite("/sprites/Map/grass.png"));
        groundSprites.put(GroundType.WALL, loadSprite("/sprites/Map/wall.png"));
        decorationSprites.put(MapDecoration.CELL_BARS, loadSprite("/sprites/Map/cellBars.png"));
        decorationSprites.put(MapDecoration.BED_TOP, loadSprite("/sprites/Map/bed_top.png"));
        decorationSprites.put(MapDecoration.BED_BOTTOM, loadSprite("/sprites/Map/bed_bottom.png"));
        decorationSprites.put(MapDecoration.TOILET, loadSprite("/sprites/Map/toilet.png"));

        playerSprites.put(Entity.Direction.DOWN, loadFrames("/sprites/player/Front_1.png", "/sprites/player/Front_2.png"));
        playerSprites.put(Entity.Direction.UP, loadFrames("/sprites/player/Back_1.png", "/sprites/player/Back_2.png"));
        playerSprites.put(Entity.Direction.LEFT, loadFrames("/sprites/player/Left_1.png", "/sprites/player/Left_2.png"));
        playerSprites.put(Entity.Direction.RIGHT, loadFrames("/sprites/player/Right_1.png", "/sprites/player/Right_2.png"));

        guardSprites.put(Entity.Direction.DOWN, loadFrames("/sprites/guard/Guard_Front_1.png", "/sprites/guard/Guard_Front_2.png"));
        guardSprites.put(Entity.Direction.UP, loadFrames("/sprites/guard/Guard_Back_1.png", "/sprites/guard/Guard_Back_2.png"));
        guardSprites.put(Entity.Direction.LEFT, loadFrames("/sprites/guard/Guard_Left_1.png", "/sprites/guard/Guard_Left_2.png"));
        guardSprites.put(Entity.Direction.RIGHT, loadFrames("/sprites/guard/Guard_Right_1.png", "/sprites/guard/Guard_Right_2.png"));

        hazardSprites.put(HazardType.HANDCUFFS, loadSprite("/sprites/hazards/Handcuffs.png"));
        hazardSprites.put(HazardType.PARKING_TICKET, loadSprite("/sprites/hazards/ParkingTicket.png"));
        hazardSprites.put(HazardType.BEAR, loadSprite("/sprites/hazards/Bear.png"));
        hazardSprites.put(HazardType.SPOILED_MILK, loadSprite("/sprites/hazards/Milk.png"));

        rewardSprites.put(RewardType.LAPTOP, loadSprite("/sprites/rewards/laptop.png"));
        rewardSprites.put(RewardType.STUDENT_ID, loadSprite("/sprites/rewards/studentID.png"));
        rewardSprites.put(RewardType.RACCOON, loadSprite("/sprites/rewards/raccoon.png"));
        rewardEmptySprites.put(RewardType.LAPTOP, brightenEmptySprite(loadSprite("/sprites/rewards/laptop_empty.png")));
        rewardEmptySprites.put(RewardType.STUDENT_ID, brightenEmptySprite(loadSprite("/sprites/rewards/studentID_empty.png")));
        rewardEmptySprites.put(RewardType.RACCOON, brightenEmptySprite(loadSprite("/sprites/rewards/raccoon_empty.png")));

        powerupSprites.put(PowerupType.COFFEE, loadSprite("/sprites/powerups/coffee.png"));
        powerupSprites.put(PowerupType.SNOWFLAKE, loadSprite("/sprites/powerups/snowflake.png"));
        powerupSprites.put(PowerupType.DOCTORS_NOTE, loadSprite("/sprites/powerups/doctorsNote.png"));

        heartFilledSprite = loadSprite("/sprites/HUD/Heart_Fill.png");
        heartEmptySprite = brightenEmptySprite(loadSprite("/sprites/HUD/Heart_Empty.png"));
    }

    private BufferedImage[] loadFrames(String frame1, String frame2) {
        return new BufferedImage[]{loadSprite(frame1), loadSprite(frame2)};
    }

    private BufferedImage loadSprite(String resourcePath) {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                System.out.println("Missing: " + resourcePath);
                return null;
            }
            return ImageIO.read(in);
        } catch (IOException e) {
            System.out.println("Missing: " + resourcePath);
            return null;
        }
    }

    /**
     * Recolors dark pixels in empty HUD icons so they remain visible over the dark overlay panels.
     * @param source The original sprite image.
     * @return A recolored copy of the sprite, or null if the source image was missing.
     */
    private BufferedImage brightenEmptySprite(BufferedImage source) {
        if (source == null) {
            return null;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = source.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    output.setRGB(x, y, argb);
                    continue;
                }
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;
                if (r <= 70 && g <= 70 && b <= 70) {
                    int white = (a << 24) | 0xFFFFFF;
                    output.setRGB(x, y, white);
                } else {
                    output.setRGB(x, y, argb);
                }
            }
        }
        return output;
    }

    /**
     * Selects the correct idle or walking frame for the requested entity direction.
     * @param spriteSet The directional sprite map to read from.
     * @param facing The direction the entity is facing.
     * @param moving Whether the entity is currently moving.
     * @return The sprite frame to draw, or null if no sprite is available.
     */
    private BufferedImage selectDirectionalSprite(Map<Entity.Direction, BufferedImage[]> spriteSet, Entity.Direction facing, boolean moving) {
        Entity.Direction direction = (facing == null) ? Entity.Direction.DOWN : facing;
        BufferedImage[] frames = spriteSet.get(direction);
        if (frames == null) {
            frames = spriteSet.get(Entity.Direction.DOWN);
        }
        if (frames == null || frames.length == 0) {
            return null;
        }

        int frameIndex = moving ? (int) ((System.currentTimeMillis() / 180L) % 2L) : 0;
        if (frameIndex >= frames.length || frames[frameIndex] == null) {
            frameIndex = 0;
        }
        return frames[frameIndex];
    }

    private BufferedImage tintSprite(BufferedImage source, Color tint) {
        if (source == null) {
            return null;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha == 0) {
                    output.setRGB(x, y, argb);
                    continue;
                }

                int red = (argb >>> 16) & 0xFF;
                int green = (argb >>> 8) & 0xFF;
                int blue = argb & 0xFF;

                int tintedRed = Math.min(255, (int) (red * 0.35f + tint.getRed() * 0.65f));
                int tintedGreen = Math.min(255, (int) (green * 0.35f + tint.getGreen() * 0.65f));
                int tintedBlue = Math.min(255, (int) (blue * 0.35f + tint.getBlue() * 0.65f));
                int tintedArgb = (alpha << 24) | (tintedRed << 16) | (tintedGreen << 8) | tintedBlue;
                output.setRGB(x, y, tintedArgb);
            }
        }
        return output;
    }

    /**
     * Renders the complete game scene, including tiles, entities, lighting, and UI.
     * @param g The Graphics context provided by Swing.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int mapViewWidth = getWidth();
        int mapViewHeight = Math.max(1, getHeight() - HUD_HEIGHT);
        GameState currentState = game == null ? GameState.PLAYING : game.getState();

        updateCamera(mapViewWidth, mapViewHeight);

        Graphics2D worldG2d = (Graphics2D) g2d.create();
        worldG2d.setClip(0, 0, mapViewWidth, mapViewHeight);
        worldG2d.scale(CAMERA_ZOOM, CAMERA_ZOOM);
        worldG2d.translate(-cameraX, -cameraY);

        paintTiles(worldG2d);
        paintStartEndLabels(worldG2d); // markers drawn over tiles
        paintCoins(worldG2d);
        paintGrid(worldG2d);
        paintMapDecorations(worldG2d);
        paintPlayer(worldG2d);         // player & guards on top of markers
        paintHazards(worldG2d);
        paintRewards(worldG2d);
        paintPowerups(worldG2d);
        paintGuards(worldG2d);
        worldG2d.dispose();

        if (currentState == GameState.PLAYING || currentState == GameState.FROZEN) {
            paintFogOfWar(g2d, mapViewWidth, mapViewHeight);
        }
        if (currentState == GameState.PLAYING || currentState == GameState.FROZEN) {
            paintScreenHud(g2d, mapViewWidth, mapViewHeight);
        }
        paintOverlay(g2d, currentState);
        g2d.dispose();
    }

    /**
     * Draws the map's visual ground layer independently from gameplay tile state.
     * @param g2d The graphics context.
     */
    private void paintTiles(Graphics2D g2d) {
        for (int row = 0; row < prisonMap.getRows(); row++) {
            for (int col = 0; col < prisonMap.getCols(); col++) {
                TileType tile = prisonMap.getTile(row, col);
                GroundType groundType = prisonMap.getGroundType(row, col);
                BufferedImage sprite = groundSprites.get(groundType);
                int x = col * CELL_SIZE;
                int y = row * CELL_SIZE;

                if (sprite != null) {
                    g2d.drawImage(sprite, x, y, CELL_SIZE, CELL_SIZE, null);
                } else {
                    g2d.setColor(tile.getColor());
                    g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    /**
     * Draws decorative sprites that sit on top of the ground layer, such as the starting jail cells.
     * @param g2d The graphics context.
     */
    private void paintMapDecorations(Graphics2D g2d) {
        for (int row = 0; row < prisonMap.getRows(); row++) {
            for (int col = 0; col < prisonMap.getCols(); col++) {
                MapDecoration decoration = prisonMap.getDecoration(row, col);
                if (decoration == MapDecoration.NONE) {
                    continue;
                }

                int x = col * CELL_SIZE;
                int y = row * CELL_SIZE;
                BufferedImage sprite = decorationSprites.get(decoration);
                if (sprite != null) {
                    g2d.drawImage(sprite, x, y, CELL_SIZE, CELL_SIZE, null);
                } else {
                    paintDecorationFallback(g2d, decoration, x, y);
                }
            }
        }
    }

    private void paintDecorationFallback(Graphics2D g2d, MapDecoration decoration, int x, int y) {
        switch (decoration) {
            case CELL_BARS -> {
                g2d.setColor(new Color(58, 62, 68));
                g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
            }
            case BED_TOP, BED_BOTTOM -> {
                g2d.setColor(new Color(95, 115, 150));
                g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
            }
            case TOILET -> {
                g2d.setColor(new Color(205, 210, 215));
                g2d.fillOval(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
            }
            default -> {
            }
        }
    }

    private void paintGrid(Graphics2D g2d) {
        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(1f));

        int maxX = prisonMap.getCols() * CELL_SIZE;
        int maxY = prisonMap.getRows() * CELL_SIZE;

        for (int row = 0; row <= prisonMap.getRows(); row++) {
            int y = row * CELL_SIZE;
            g2d.drawLine(0, y, maxX, y);
        }
        for (int col = 0; col <= prisonMap.getCols(); col++) {
            int x = col * CELL_SIZE;
            g2d.drawLine(x, 0, x, maxY);
        }

        g2d.setColor(new Color(30, 30, 30));
        g2d.setStroke(new BasicStroke(BORDER_STROKE));
        g2d.drawRect(0, 0, maxX, maxY);
    }

    /**
     * Draws the timer, score, rewards, and health overlays on top of the world view.
     * @param g2d The graphics context.
     * @param mapViewWidth The visible width of the map area in pixels.
     * @param mapViewHeight The visible height of the map area in pixels.
     */
    private void paintScreenHud(Graphics2D g2d, int mapViewWidth, int mapViewHeight) {
        int pad = 12;
        int boxHeight = 38;

        int timeBoxWidth = 112;
        int timeBoxX = (mapViewWidth - timeBoxWidth) / 2;
        int timeBoxY = pad;
        drawHudBox(g2d, timeBoxX, timeBoxY, timeBoxWidth, boxHeight, timeText);

        int scoreBoxWidth = 96;
        int scoreIconSize = Math.max(12, boxHeight - 10);
        int scoreBoxX = mapViewWidth - scoreBoxWidth - scoreIconSize - 10 - pad;
        int scoreBoxY = pad;
        drawScoreBox(g2d, scoreBoxX, scoreBoxY, scoreBoxWidth, boxHeight, scoreText);

        paintRewardsHud(g2d, mapViewWidth, mapViewHeight);
        paintLivesHud(g2d, mapViewWidth, mapViewHeight);
        paintPopupMessage(g2d, mapViewWidth, mapViewHeight);
    }

    private void paintLivesHud(Graphics2D g2d, int mapViewWidth, int mapViewHeight) {
        int maxLives = 3;
        int currentLives = 3;

        if (game != null) {
            maxLives = game.getDifficulty().getLives();
        }
        if (player != null) {
            currentLives = Math.max(0, player.getLives());
        }

        int heartSize = 34;
        int spacing = 6;
        int totalWidth = (maxLives * heartSize) + ((maxLives - 1) * spacing);
        int startX = mapViewWidth - totalWidth - 12;
        int drawY = mapViewHeight - heartSize - 12;
        drawHudPanel(g2d, startX - 8, drawY - 6, totalWidth + 16, heartSize + 12);

        for (int i = 0; i < maxLives; i++) {
            int x = startX + i * (heartSize + spacing);
            BufferedImage heart = i < currentLives ? heartFilledSprite : heartEmptySprite;
            if (heart != null) {
                g2d.drawImage(heart, x, drawY, heartSize, heartSize, null);
            } else {
                g2d.setColor(i < currentLives ? new Color(220, 60, 60) : new Color(120, 120, 120));
                g2d.fillOval(x, drawY, heartSize, heartSize);
            }
        }
    }

    /**
     * Draws the reward collection tracker in the bottom-left corner.
     * Filled icons represent collected rewards and empty icons represent missing ones.
     * @param g2d The graphics context.
     * @param mapViewWidth The visible width of the map area in pixels.
     * @param mapViewHeight The visible height of the map area in pixels.
     */
    private void paintRewardsHud(Graphics2D g2d, int mapViewWidth, int mapViewHeight) {
        if (player == null) {
            return;
        }

        int iconSize = 34;
        int spacing = 6;
        RewardType[] order = {RewardType.LAPTOP, RewardType.STUDENT_ID, RewardType.RACCOON};
        int totalWidth = (order.length * iconSize) + ((order.length - 1) * spacing);
        int startX = 12;
        int drawY = mapViewHeight - iconSize - 12;
        drawHudPanel(g2d, startX - 8, drawY - 6, totalWidth + 16, iconSize + 12);

        for (int i = 0; i < order.length; i++) {
            RewardType rewardType = order[i];
            int x = startX + i * (iconSize + spacing);
            BufferedImage icon = player.hasCollectedReward(rewardType)
                    ? rewardSprites.get(rewardType)
                    : rewardEmptySprites.get(rewardType);
            if (icon != null) {
                g2d.drawImage(icon, x, drawY, iconSize, iconSize, null);
            } else {
                g2d.setColor(player.hasCollectedReward(rewardType)
                        ? new Color(96, 220, 128)
                        : new Color(120, 120, 120));
                g2d.fillRoundRect(x, drawY, iconSize, iconSize, 8, 8);
            }
        }
    }

    private void drawHudPanel(Graphics2D g2d, int x, int y, int width, int height) {
        int arc = 12;
        g2d.setColor(HUD_PANEL_SHADOW);
        g2d.fillRoundRect(x + 2, y + 3, width, height, arc, arc);
        g2d.setColor(HUD_PANEL_BG);
        g2d.fillRoundRect(x, y, width, height, arc, arc);
        g2d.setColor(HUD_PANEL_BORDER);
        g2d.drawRoundRect(x, y, width, height, arc, arc);
    }

    private void paintCoins(Graphics2D g2d) {
        int inset = Math.max(1, CELL_SIZE / 5);
        int size = Math.max(2, CELL_SIZE - (inset * 2));
        for (int row = 0; row < prisonMap.getRows(); row++) {
            for (int col = 0; col < prisonMap.getCols(); col++) {
                if (!prisonMap.hasCoin(row, col)) {
                    continue;
                }
                int x = col * CELL_SIZE + inset;
                int y = row * CELL_SIZE + inset;
                g2d.setColor(COIN_COLOR);
                g2d.fillOval(x, y, size, size);
                g2d.setColor(COIN_BORDER_COLOR);
                g2d.drawOval(x, y, size, size);
            }
        }
    }

    /**
     * Links the panel to the player entity for rendering and camera tracking.
     * @param player The Player instance to render.
     */
    public void setPlayer(Player player) {
        this.player = player;
        repaint();
    }

    /**
     * Updates the list of active guards to be rendered on the map.
     * @param guards The list of Guard objects to track and display.
     */
    public void setGuards(java.util.List<Guard> guards) {
        this.guards = guards;
        repaint();
    }

    private void paintStartEndLabels(Graphics2D g2d) {
        Point start = prisonMap.getStartTile();
        Point end = prisonMap.getEndTile();

        drawLabelZone(g2d, "End", prisonMap.getCols() - 4, end.y - 1, 4, 2);
        drawMarker(g2d, start, TileType.START.getColor());
        drawMarker(g2d, end, TileType.END.getColor());
    }

    private void drawLabelZone(Graphics2D g2d, String text, int leftCol, int topRow, int widthTiles, int heightTiles) {
        int x = leftCol * CELL_SIZE;
        int y = topRow * CELL_SIZE;
        int width = widthTiles * CELL_SIZE;
        int height = heightTiles * CELL_SIZE;

        g2d.setColor(new Color(250, 250, 250));
        g2d.fillRect(x, y, width, height);

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, UI_FONT_SIZE));
        g2d.setColor(Color.BLACK);
        drawCenteredText(g2d, text, x + (width / 2), y + (height / 2));
    }

    private void drawHudBox(Graphics2D g2d, int x, int y, int width, int height, String text) {
        int arc = 12;
        g2d.setColor(HUD_PANEL_SHADOW);
        g2d.fillRoundRect(x + 2, y + 3, width, height, arc, arc);
        g2d.setColor(HUD_PANEL_BG);
        g2d.fillRoundRect(x, y, width, height, arc, arc);
        g2d.setColor(HUD_PANEL_BORDER);
        g2d.drawRoundRect(x, y, width, height, arc, arc);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, UI_FONT_SIZE));
        g2d.setColor(HUD_TEXT_COLOR);
        drawCenteredText(g2d, text, x + (width / 2), y + (height / 2));
    }

    private void drawScoreBox(Graphics2D g2d, int x, int y, int width, int height, String text) {
        drawHudBox(g2d, x, y, width, height, "");

        int iconSize = Math.max(12, height - 10);
        int iconX = x + width + 6;
        int iconY = y + ((height - iconSize) / 2);
        g2d.setColor(COIN_COLOR);
        g2d.fillOval(iconX, iconY, iconSize, iconSize);
        g2d.setColor(COIN_BORDER_COLOR);
        g2d.drawOval(iconX, iconY, iconSize, iconSize);

        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, UI_FONT_SIZE));
        g2d.setColor(HUD_TEXT_COLOR);
        int textAreaCenterX = x + (width / 2);
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        int textX = textAreaCenterX - (textWidth / 2);
        int textY = y + (height / 2) + (g2d.getFontMetrics().getAscent() / 3);
        g2d.drawString(text, textX, textY);
    }

    private void drawCenteredText(Graphics2D g2d, String text, int centerX, int centerY) {
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        int textHeight = g2d.getFontMetrics().getAscent();
        int drawX = centerX - (textWidth / 2);
        int drawY = centerY + (textHeight / 3);
        g2d.drawString(text, drawX, drawY);
    }

    private void paintPlayer(Graphics2D g2d) {
        if (player == null) return;
        int px = player.getX() * CELL_SIZE;
        int py = player.getY() * CELL_SIZE;
        BufferedImage sprite = selectDirectionalSprite(playerSprites, player.getFacing(), player.getMovementState() == Player.MovementState.MOVING);
        boolean hitFlashVisible = player.isHitFlashVisible();

        if (sprite != null) {
            BufferedImage spriteToDraw = hitFlashVisible ? tintSprite(sprite, new Color(255, 45, 45)) : sprite;
            g2d.drawImage(spriteToDraw, px, py, CELL_SIZE, CELL_SIZE, null);
        } else {
            int size = CELL_SIZE - 4;
            g2d.setColor(hitFlashVisible ? new Color(255, 70, 70) : Color.BLUE);
            g2d.fillOval(px + 2, py + 2, size, size);
        }

        Player.StatusState statusState = player.getStatusState();
        if (statusState != Player.StatusState.NORMAL) {
            Color statusColor = switch (statusState) {
                case HANDS_TIED -> new Color(190, 190, 190);
                case SLOWED -> new Color(90, 190, 255);
                case INVERTED_CONTROLS -> new Color(210, 120, 255);
                default -> null;
            };
            if (statusColor != null) {
                g2d.setColor(statusColor);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval(px + 1, py + 1, CELL_SIZE - 2, CELL_SIZE - 2);
            }
        }
    }

    private void paintGuards(Graphics2D g2d) {
        if (guards == null) return;
        for (Guard guard : guards) {
            int gx = guard.getX() * CELL_SIZE;
            int gy = guard.getY() * CELL_SIZE;
            BufferedImage sprite = selectDirectionalSprite(guardSprites, guard.getFacing(), guard.isMoving());

            if (sprite != null) {
                g2d.drawImage(sprite, gx, gy, CELL_SIZE, CELL_SIZE, null);
            } else {
                int size = CELL_SIZE - 4;
                if (guard.getType() == Guard.GuardType.PATROL) {
                    g2d.setColor(Color.ORANGE);
                } else {
                    g2d.setColor(Color.RED);
                }
                g2d.fillOval(gx + 2, gy + 2, size, size);
            }

            if (guard.isAlertState()) {
                g2d.setColor(new Color(255, 70, 70, 220));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval(gx + 1, gy + 1, CELL_SIZE - 2, CELL_SIZE - 2);
            }
        }
    }

    private void drawMarker(Graphics2D g2d, Point tile, Color color) {
        int x = tile.x * CELL_SIZE;
        int y = tile.y * CELL_SIZE;
        int inset = CELL_SIZE / 4;

        g2d.setColor(color);
        g2d.fillRect(x + inset, y + inset, CELL_SIZE - (inset * 2), CELL_SIZE - (inset * 2));
    }

    /**
     * Renders all active hazards onto the map using their specific sprites.
     * If a sprite is missing, falls back to drawing a yellow circle.
     * * @param g2d The Graphics2D context used for drawing.
     */
    private void paintHazards(Graphics2D g2d) {
        if (hazards == null) return;
        for (Hazard hazard : hazards) {
            if (hazard.isActive()) {
                int hx = (int)hazard.getX() * CELL_SIZE;
                int hy = (int)hazard.getY() * CELL_SIZE;

                BufferedImage imgToDraw = hazardSprites.get(hazard.getHazardType());

                if (imgToDraw != null) {
                    g2d.drawImage(imgToDraw, hx, hy, CELL_SIZE, CELL_SIZE, null);
                } else {
                    // If images are missing, fallback to original yellow circles
                    int size = CELL_SIZE - 6;
                    g2d.setColor(Color.YELLOW);
                    g2d.fillOval(hx + 3, hy + 3, size, size); 
                }
            }
        }
    }

    private void paintRewards(Graphics2D g2d) {
        if (rewards == null) return;
        for (Rewards reward : rewards) {
            if(reward.isActive()) {
                int rx = (int)reward.getX() * CELL_SIZE;
                int ry = (int)reward.getY() * CELL_SIZE;

                BufferedImage imgToDraw = rewardSprites.get(reward.getRewardType());

                if(imgToDraw != null) {
                    g2d.drawImage(imgToDraw, rx, ry, CELL_SIZE, CELL_SIZE, null);
                }
                else {
                    int size = CELL_SIZE - 6;
                    g2d.setColor(Color.GREEN);
                    g2d.fillOval(rx + 3, ry + 3, size, size);
                }
            }
        }
    }

    /**
     * Renders all active powerups onto the map using their specific sprites.
     * If a sprite is missing, falls back to drawing a cyan circle.
     * * @param g2d The Graphics2D context used for drawing.
     */
    private void paintPowerups(Graphics2D g2d) {
        if (powerups == null) return;
        
        for (Powerups p : powerups) {
            if (p.isActive()) {
                int px = (int)p.getX() * CELL_SIZE;
                int py = (int)p.getY() * CELL_SIZE;

                BufferedImage imgToDraw = powerupSprites.get(p.getType());

                if (imgToDraw != null) {
                    g2d.drawImage(imgToDraw, px, py, CELL_SIZE, CELL_SIZE, null);
                } else {
                    // Fallback cyan circles if the images are missing
                    int size = CELL_SIZE - 6;
                    g2d.setColor(Color.CYAN);
                    g2d.fillOval(px + 3, py + 3, size, size);
                }
            }
        }
    }

    private void paintOverlay(Graphics2D g2d, GameState state) {
        if (state != GameState.STORY) {
            storyStartTime = 0;
        }

        if (state == GameState.MENU) {
            paintMenuOverlay(g2d);
        } else if (state == GameState.FROZEN) {
            paintPauseOverlay(g2d);
        } else if (state == GameState.GAME_OVER) {
            paintEndOverlay(g2d, "You Lost");
        } else if (state == GameState.LEVEL_COMPLETE) {
            paintEndOverlay(g2d, "You Win");
        } else if (state == GameState.STORY) {
            paintStoryLine(g2d);
        }
    }

    private void paintMenuOverlay(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;

        g2d.setColor(new Color(0, 0, 0, 145));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        drawCenteredText(g2d, "Escape From The Burnaby Mountain Prison", centerX, centerY - 120);

        drawButton(g2d, getMenuStartButtonRect(), "Start");
        drawButton(g2d, getMenuExitButtonRect(), "Exit");

        Rectangle leftRect = getDifficultyLeftButtonRect();
        Rectangle rightRect = getDifficultyRightButtonRect();
        Rectangle valueRect = getDifficultyValueRect();
        drawButton(g2d, leftRect, "<");
        drawButton(g2d, rightRect, ">");

        g2d.setColor(new Color(240, 240, 240, 230));
        g2d.fillRoundRect(valueRect.x, valueRect.y, valueRect.width, valueRect.height, 10, 10);
        g2d.setColor(new Color(40, 40, 40));
        g2d.drawRoundRect(valueRect.x, valueRect.y, valueRect.width, valueRect.height, 10, 10);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        String difficultyText;
        if (game == null) {
            difficultyText = "Easy (3 lives)";
        } else {
            difficultyText = game.getDifficultyLabel() + " (" + game.getDifficulty().getLives() + " lives)";
        }
        drawCenteredText(g2d, difficultyText, valueRect.x + (valueRect.width / 2), valueRect.y + (valueRect.height / 2));
    }

    private void paintEndOverlay(Graphics2D g2d, String title) {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;

        g2d.setColor(new Color(0, 0, 0, 145));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        drawCenteredText(g2d, title, centerX, centerY - 90);

        drawButton(g2d, getEndRestartButtonRect(), "Restart");
        drawButton(g2d, getEndMenuButtonRect(), "Exit To Menu");
    }

    private void paintPauseOverlay(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;

        g2d.setColor(new Color(0, 0, 0, 145));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        drawCenteredText(g2d, "Paused", centerX, centerY - 90);

        drawButton(g2d, getPauseResumeButtonRect(), "Resume");
        drawButton(g2d, getPauseMenuButtonRect(), "Exit To Menu");
    }

    /**
     * Renders the story line overlay with a typewritter effect.
     * <p>
     *     This darkens the background and calculates how many charecters of the story should be visible based on the
     *     time elsapsed since the method was called.
     * </p>
     * @param g2d The Graphics2D context used for drawing the overlay and text.
     */
    public void paintStoryLine(Graphics2D g2d) {
        if (storyStartTime == 0) {
            storyStartTime = System.currentTimeMillis();
        }

        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;

        //Darken background
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int msPerChar = 35;
        long elapsed = System.currentTimeMillis() - storyStartTime;
        int currentChars = (int) (elapsed / msPerChar);

        String l1 = "It's been months.";
        String l2a = "You were charged with multiple counts of using A.I on assignments...";
        String l2b = "and one count of cheating on your midterm.";
        String l3 = "But the summer is approaching... you cant miss it.";
        String l4 = "YOU MUST ESCAPE";
        String l5 = "Collect your belonging and find the exit! Watch out for guards.";
        String exitPrompt = "Click anywhere to continue...";

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        currentChars = drawTypeText(g2d, l1, currentChars, centerX, centerY - 100);
        currentChars = drawTypeText(g2d, l2a, currentChars, centerX, centerY - 50);
        currentChars = drawTypeText(g2d, l2b, currentChars, centerX, centerY - 20);
        currentChars = drawTypeText(g2d, l3, currentChars, centerX, centerY + 20);

        g2d.setColor(Color.RED);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 45));
        currentChars = drawTypeText(g2d, l4, currentChars, centerX, centerY + 80);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        currentChars = drawTypeText(g2d, l5, currentChars, centerX, centerY + 130);

        if (currentChars > 0) {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));
            int textWidth = g2d.getFontMetrics().stringWidth(exitPrompt);
            int textHeight = g2d.getFontMetrics().getAscent();

            int padX = 20;
            int padY = 10;
            int boxWidth = textWidth + padX * 2;
            int boxHeight = textHeight + padY * 2;
            int boxX = centerX - boxWidth / 2;
            int boxY = centerY + 165;

            // Box Background
            g2d.setColor(new Color(30, 30, 30, 200));
            g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);

            // Box Border
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);

            // Type out the prompt text perfectly centered inside the box
            drawTypeText(g2d, exitPrompt, currentChars, centerX, boxY + (boxHeight / 2));
        }
    }

    /**
     * Helper method to draw a singe line of text with the typewriter effect, while keeping it centered based on the
     * text's width.
     * @param g2d The Graphics2D context used for drawing the overlay and text.
     * @param text The string to be typed
     * @param currentChars The number of charecters allowed to be drawn
     * @param centerX The X-coordinate where the center of the full text should be placed
     * @param centerY The Y-coordinate where the text should be vertically centered.
     * @return The remaining character allowance after this line's length is subtracted.
     */
    private int drawTypeText(Graphics2D g2d, String text, int currentChars, int centerX, int centerY) {
        int remaining = currentChars - text.length();

        if (currentChars > 0) {
            int chars = Math.min(currentChars, text.length());
            String textDraw = text.substring(0, chars);

            int textWidth = g2d.getFontMetrics().stringWidth(text);
            int startX = centerX - (textWidth / 2);
            int drawY = centerY + (g2d.getFontMetrics().getAscent() / 3);

            g2d.drawString(textDraw, startX, drawY);
        }

        return remaining;
    }

    private void drawButton(Graphics2D g2d, Rectangle rect, String text) {
        g2d.setColor(new Color(240, 240, 240, 230));
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        g2d.setColor(new Color(40, 40, 40));
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        drawCenteredText(g2d, text, rect.x + (rect.width / 2), rect.y + (rect.height / 2));
    }

    private Rectangle getMenuStartButtonRect() {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;
        return new Rectangle(centerX - 90, centerY - 25, 180, 40);
    }

    private Rectangle getMenuExitButtonRect() {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;
        return new Rectangle(centerX - 90, centerY + 25, 180, 40);
    }

    private Rectangle getDifficultyLeftButtonRect() {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;
        return new Rectangle(centerX - 140, centerY + 85, 40, 34);
    }

    private Rectangle getDifficultyValueRect() {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;
        return new Rectangle(centerX - 95, centerY + 85, 190, 34);
    }

    private Rectangle getDifficultyRightButtonRect() {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;
        return new Rectangle(centerX + 100, centerY + 85, 40, 34);
    }

    private Rectangle getEndRestartButtonRect() {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;
        return new Rectangle(centerX - 90, centerY - 10, 180, 40);
    }

    private Rectangle getEndMenuButtonRect() {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;
        return new Rectangle(centerX - 90, centerY + 40, 180, 40);
    }

    private Rectangle getPauseResumeButtonRect() {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;
        return new Rectangle(centerX - 90, centerY - 10, 180, 40);
    }

    private Rectangle getPauseMenuButtonRect() {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;
        return new Rectangle(centerX - 90, centerY + 40, 180, 40);
    }

    /**
     * Routes a mouse click to the active overlay buttons for the current game state.
     * @param x The click x-coordinate in panel space.
     * @param y The click y-coordinate in panel space.
     */
    private void handleScreenClick(int x, int y) {
        if (game == null) {
            return;
        }

        GameState state = game.getState();
        if (state == GameState.MENU) {
            if (getMenuStartButtonRect().contains(x, y)) {
                game.playSoundEffect(8);
                game.displayStory();
            } else if (getMenuExitButtonRect().contains(x, y)) {
                game.playSoundEffect(8);
                game.exitGame();
            } else if (getDifficultyLeftButtonRect().contains(x, y)) {
                game.playSoundEffect(8);
                game.decreaseDifficulty();
            } else if (getDifficultyRightButtonRect().contains(x, y)) {
                game.playSoundEffect(8);
                game.increaseDifficulty();
            }
        } else if (state == GameState.FROZEN) {
            if (getPauseResumeButtonRect().contains(x, y)) {
                game.playSoundEffect(8);
                game.resumeMatch();
            } else if (getPauseMenuButtonRect().contains(x, y)) {
                game.playSoundEffect(8);
                game.returnToMenu();
            }
        } else if (state == GameState.GAME_OVER || state == GameState.LEVEL_COMPLETE) {
            if (getEndRestartButtonRect().contains(x, y)) {
                game.playSoundEffect(8);
                game.restartMatch();
            } else if (getEndMenuButtonRect().contains(x, y)) {
                game.playSoundEffect(8);
                game.returnToMenu();
            }
        } else if (state == GameState.STORY) {
            game.startMatch();
        }
    }

    /**
     * Keeps the camera centered on the player by converting the player's tile position into world-space offsets.
     * @param mapViewWidth The visible width of the map area in pixels.
     * @param mapViewHeight The visible height of the map area in pixels.
     */
    private void updateCamera(int mapViewWidth, int mapViewHeight) {
        if (player == null) {
            cameraX = 0f;
            cameraY = 0f;
            return;
        }

        float viewWidthInWorld = mapViewWidth / CAMERA_ZOOM;
        float viewHeightInWorld = mapViewHeight / CAMERA_ZOOM;
        float playerCenterX = (player.getX() * CELL_SIZE) + (CELL_SIZE / 2f);
        float playerCenterY = (player.getY() * CELL_SIZE) + (CELL_SIZE / 2f);
        cameraX = playerCenterX - (viewWidthInWorld / 2f);
        cameraY = playerCenterY - (viewHeightInWorld / 2f);
    }

    /**
     * Draws the dark radial visibility mask centered on the player's screen position.
     * @param g2d The graphics context.
     * @param mapViewWidth The visible width of the map area in pixels.
     * @param mapViewHeight The visible height of the map area in pixels.
     */
    private void paintFogOfWar(Graphics2D g2d, int mapViewWidth, int mapViewHeight) {
        if (player == null) {
            g2d.setColor(new Color(0, 0, 0, 235));
            g2d.fillRect(0, 0, mapViewWidth, mapViewHeight);
            return;
        }

        float playerCenterX = (player.getX() * CELL_SIZE) + (CELL_SIZE / 2f);
        float playerCenterY = (player.getY() * CELL_SIZE) + (CELL_SIZE / 2f);
        float playerScreenX = (playerCenterX - cameraX) * CAMERA_ZOOM;
        float playerScreenY = (playerCenterY - cameraY) * CAMERA_ZOOM;
        float visibilityRadius = CELL_SIZE * CAMERA_ZOOM * VISIBILITY_RADIUS_TILES;

        RadialGradientPaint fog = new RadialGradientPaint(
                new Point2D.Float(playerScreenX, playerScreenY),
                visibilityRadius,
                new float[]{0f, 0.5f, 0.85f, 1f},
                new Color[]{
                        new Color(0, 0, 0, 0),
                        new Color(0, 0, 0, 190),
                        new Color(0, 0, 0, 230),
                        new Color(0, 0, 0, 250)
                }
        );

        g2d.setPaint(fog);
        g2d.fillRect(0, 0, mapViewWidth, mapViewHeight);
    }

    private void paintPopupMessage(Graphics2D g2d, int mapViewWidth, int mapViewHeight) {
        if (player != null && player.isPopupVisible()) {
            String msg = player.getPopupMessage();
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, UI_FONT_SIZE));
            
            // Calculate box size dynamically based on the string length
            int textWidth = g2d.getFontMetrics().stringWidth(msg);
            int boxWidth = textWidth + 40; 
            int boxHeight = 40;
            
            // Center it horizontally, place it just below the top HUD
            int x = (mapViewWidth - boxWidth) / 2;
            int y = 70; 

            drawHudBox(g2d, x, y, boxWidth, boxHeight, msg);
        }
    }
}
