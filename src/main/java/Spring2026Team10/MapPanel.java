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

public class MapPanel extends JPanel {
    // Half edge length -> quarter area per tile.
    private static final int CELL_SIZE = 10;
    private static final Color GRID_COLOR = new Color(60, 60, 60);
    private static final Color COIN_COLOR = new Color(242, 188, 34);
    private static final Color COIN_BORDER_COLOR = new Color(178, 131, 22);
    private static final int HUD_HEIGHT = CELL_SIZE * 2;
    private static final Color HUD_BG = new Color(236, 236, 236);
    private static final Color HUD_BOX_BG = new Color(250, 250, 250);
    private static final int UI_FONT_SIZE = Math.max(12, Math.round(CELL_SIZE * 1.2f));
    private static final float BORDER_STROKE = Math.max(1.25f, CELL_SIZE * 0.125f);
    private static final float CAMERA_ZOOM = 3.0f;
    private static final float VISIBILITY_RADIUS_TILES = 10.5f;

    private final PrisonMap prisonMap;
    private Game game;
    private Player player;
    private java.util.List<Guard> guards = new java.util.ArrayList<>();
    private String timeText = "XXX";
    private String scoreText = "XXX";
    private float cameraX = 0f;
    private float cameraY = 0f;

    private java.util.List<Hazard> hazards = new java.util.ArrayList<>();
    private final Map<Entity.Direction, BufferedImage[]> playerSprites = new EnumMap<>(Entity.Direction.class);
    private final Map<Entity.Direction, BufferedImage[]> guardSprites = new EnumMap<>(Entity.Direction.class);
    private final Map<HazardType, BufferedImage> hazardSprites = new EnumMap<>(HazardType.class);
    
    public void setHazards(java.util.List<Hazard> hazards) {
        this.hazards = hazards;
    }

    public MapPanel(PrisonMap prisonMap) {
        this.prisonMap = prisonMap;
        int width = prisonMap.getCols() * CELL_SIZE + 1;
        int height = prisonMap.getRows() * CELL_SIZE + HUD_HEIGHT + 1;
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

    public void setTimeText(String timeText) {
        this.timeText = timeText;
        repaint();
    }

    public void setScoreText(String scoreText) {
        this.scoreText = scoreText;
        repaint();
    }

    public PrisonMap getPrisonMap() {
        return prisonMap;
    }

    public void setGame(Game game) {
        this.game = game;
        repaint();
    }

    private void loadSprites() {
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
        paintPlayer(worldG2d);         // player & guards on top of markers
        paintHazards(worldG2d);
        paintGuards(worldG2d);
        worldG2d.dispose();

        if (currentState == GameState.PLAYING || currentState == GameState.FROZEN) {
            paintFogOfWar(g2d, mapViewWidth, mapViewHeight);
        }
        paintBottomHud(g2d, mapViewWidth, mapViewHeight);
        paintOverlay(g2d, currentState);
        g2d.dispose();
    }

    private void paintTiles(Graphics2D g2d) {
        for (int row = 0; row < prisonMap.getRows(); row++) {
            for (int col = 0; col < prisonMap.getCols(); col++) {
                TileType tile = prisonMap.getTile(row, col);
                g2d.setColor(tile.getColor());
                g2d.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
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

    private void paintBottomHud(Graphics2D g2d, int mapViewWidth, int mapViewHeight) {
        int hudY = mapViewHeight;

        g2d.setColor(HUD_BG);
        g2d.fillRect(0, hudY, mapViewWidth, HUD_HEIGHT);

        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(0, hudY, mapViewWidth, HUD_HEIGHT);

        int labelWidth = CELL_SIZE * 6;
        int valueWidth = CELL_SIZE * 5;
        int gapWidth = CELL_SIZE * 2;
        int totalWidth = (labelWidth * 2) + (valueWidth * 2) + gapWidth;
        int startX = (mapViewWidth - totalWidth) / 2;

        drawHudBox(g2d, startX, hudY, labelWidth, "Time");
        drawHudBox(g2d, startX + labelWidth, hudY, valueWidth, timeText);

        int scoreX = startX + labelWidth + valueWidth + gapWidth;
        drawHudBox(g2d, scoreX, hudY, labelWidth, "Score");
        drawHudBox(g2d, scoreX + labelWidth, hudY, valueWidth, scoreText);
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

    public void setPlayer(Player player) {
        this.player = player;
        repaint();
    }

    public void setGuards(java.util.List<Guard> guards) {
        this.guards = guards;
        repaint();
    }

    private void paintStartEndLabels(Graphics2D g2d) {
        Point start = prisonMap.getStartTile();
        Point end = prisonMap.getEndTile();

        drawLabelZone(g2d, "Start", 0, start.y - 1, 4, 2);
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

    private void drawHudBox(Graphics2D g2d, int x, int y, int width, String text) {
        g2d.setColor(HUD_BOX_BG);
        g2d.fillRect(x, y, width, HUD_HEIGHT);

        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawRect(x, y, width, HUD_HEIGHT);

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, UI_FONT_SIZE));
        g2d.setColor(Color.BLACK);
        drawCenteredText(g2d, text, x + (width / 2), y + (HUD_HEIGHT / 2));
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

        if (sprite != null) {
            g2d.drawImage(sprite, px, py, CELL_SIZE, CELL_SIZE, null);
        } else {
            int size = CELL_SIZE - 4;
            g2d.setColor(Color.BLUE);
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

    private void paintOverlay(Graphics2D g2d, GameState state) {
        if (state == GameState.MENU) {
            paintMenuOverlay(g2d);
        } else if (state == GameState.FROZEN) {
            paintPauseOverlay(g2d);
        } else if (state == GameState.GAME_OVER) {
            paintEndOverlay(g2d, "You Lost");
        } else if (state == GameState.LEVEL_COMPLETE) {
            paintEndOverlay(g2d, "You Win");
        }
    }

    private void paintMenuOverlay(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - HUD_HEIGHT) / 2;

        g2d.setColor(new Color(0, 0, 0, 145));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        drawCenteredText(g2d, "Escape From The Prison", centerX, centerY - 120);

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

    private void handleScreenClick(int x, int y) {
        if (game == null) {
            return;
        }

        GameState state = game.getState();
        if (state == GameState.MENU) {
            if (getMenuStartButtonRect().contains(x, y)) {
                game.startMatch();
            } else if (getMenuExitButtonRect().contains(x, y)) {
                game.exitGame();
            } else if (getDifficultyLeftButtonRect().contains(x, y)) {
                game.decreaseDifficulty();
            } else if (getDifficultyRightButtonRect().contains(x, y)) {
                game.increaseDifficulty();
            }
        } else if (state == GameState.FROZEN) {
            if (getPauseResumeButtonRect().contains(x, y)) {
                game.resumeMatch();
            } else if (getPauseMenuButtonRect().contains(x, y)) {
                game.returnToMenu();
            }
        } else if (state == GameState.GAME_OVER || state == GameState.LEVEL_COMPLETE) {
            if (getEndRestartButtonRect().contains(x, y)) {
                game.restartMatch();
            } else if (getEndMenuButtonRect().contains(x, y)) {
                game.returnToMenu();
            }
        }
    }

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
}
