package Spring2026Team10;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

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

    private final PrisonMap prisonMap;
    private Player player;
    private java.util.List<Guard> guards = new java.util.ArrayList<>();
    private String timeText = "XXX";
    private String scoreText = "XXX";

    private java.util.List<Hazard> hazards = new java.util.ArrayList<>();

    private BufferedImage handcuffsImg;
    private BufferedImage ticketImg;
    private BufferedImage bearImg;
    private BufferedImage milkImg;
    
    public void setHazards(java.util.List<Hazard> hazards) {
        this.hazards = hazards;
    }

    public MapPanel(PrisonMap prisonMap) {
        this.prisonMap = prisonMap;
        int width = prisonMap.getCols() * CELL_SIZE + 1;
        int height = prisonMap.getRows() * CELL_SIZE + HUD_HEIGHT + 1;
        setPreferredSize(new Dimension(width, height));
        setBackground(new Color(214, 214, 214));

        // Loading sprites from src/main/java/ execution directory
        try { handcuffsImg = ImageIO.read(new File("../../res/Hazards/Handcuffs.png")); } 
        catch (IOException e) { System.out.println("Missing: Handcuffs.png"); }

        try { ticketImg = ImageIO.read(new File("../../res/Hazards/ParkingTicket.png")); } 
        catch (IOException e) { System.out.println("Missing: ParkingTicket.png"); }

        try { bearImg = ImageIO.read(new File("../../res/Hazards/Bear.png")); } 
        catch (IOException e) { System.out.println("Missing: Bear.png"); }

        try { milkImg = ImageIO.read(new File("../../res/Hazards/Milk.png")); } 
        catch (IOException e) { System.out.println("Missing: Milk.png"); }
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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintTiles(g2d);
        paintStartEndLabels(g2d); // markers drawn over tiles
        paintCoins(g2d);
        paintGrid(g2d);
        paintPlayer(g2d);         // player & guards on top of markers
        paintHazards(g2d);
        paintGuards(g2d);
        paintBottomHud(g2d);

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

    private void paintBottomHud(Graphics2D g2d) {
        int maxX = prisonMap.getCols() * CELL_SIZE;
        int mapMaxY = prisonMap.getRows() * CELL_SIZE;
        int hudY = mapMaxY;

        g2d.setColor(HUD_BG);
        g2d.fillRect(0, hudY, maxX, HUD_HEIGHT);

        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(0, hudY, maxX, HUD_HEIGHT);

        int labelWidth = CELL_SIZE * 6;
        int valueWidth = CELL_SIZE * 5;
        int gapWidth = CELL_SIZE * 2;
        int totalWidth = (labelWidth * 2) + (valueWidth * 2) + gapWidth;
        int startX = (maxX - totalWidth) / 2;

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
        int size = CELL_SIZE - 4;
        g2d.setColor(Color.BLUE);
        g2d.fillOval(px + 2, py + 2, size, size);
    }

    private void paintGuards(Graphics2D g2d) {
        if (guards == null) return;
        for (Guard guard : guards) {
            int gx = guard.getX() * CELL_SIZE;
            int gy = guard.getY() * CELL_SIZE;
            int size = CELL_SIZE - 4;
            // choose color based on type
            if (guard.getType() == Guard.GuardType.PATROL) {
                g2d.setColor(Color.ORANGE);
            } else {
                g2d.setColor(Color.RED);
            }
            g2d.fillOval(gx + 2, gy + 2, size, size);
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
                
                BufferedImage imgToDraw = null;
                
                switch (hazard.getHazardType()) {
                    case HANDCUFFS: imgToDraw = handcuffsImg; break;
                    case PARKING_TICKET: imgToDraw = ticketImg; break;
                    case BEAR: imgToDraw = bearImg; break;
                    case SPOILED_MILK: imgToDraw = milkImg; break;
                }
                
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
}
