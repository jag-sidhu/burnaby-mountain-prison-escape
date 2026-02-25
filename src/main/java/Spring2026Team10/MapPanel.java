package Spring2026Team10;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class MapPanel extends JPanel {
    private static final int CELL_SIZE = 20;
    private static final Color GRID_COLOR = new Color(60, 60, 60);
    private static final int HUD_HEIGHT = CELL_SIZE * 2;
    private static final Color HUD_BG = new Color(236, 236, 236);
    private static final Color HUD_BOX_BG = new Color(250, 250, 250);

    private final PrisonMap prisonMap;
    private Player player;
    private java.util.List<Guard> guards = new java.util.ArrayList<>();
    private String timeText = "XXX";
    private String scoreText = "XXX";

    public MapPanel(PrisonMap prisonMap) {
        this.prisonMap = prisonMap;
        int width = prisonMap.getCols() * CELL_SIZE + 1;
        int height = prisonMap.getRows() * CELL_SIZE + HUD_HEIGHT + 1;
        setPreferredSize(new Dimension(width, height));
        setBackground(new Color(214, 214, 214));
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
        repaint();
    }

    public void setScoreText(String scoreText) {
        this.scoreText = scoreText;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintTiles(g2d);
        paintStartEndLabels(g2d); // markers drawn over tiles
        paintGrid(g2d);
        paintPlayer(g2d);         // player & guards on top of markers
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
        g2d.setStroke(new BasicStroke(2.5f));
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

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        g2d.setColor(Color.BLACK);
        drawCenteredText(g2d, text, x + (width / 2), y + (height / 2));
    }

    private void drawHudBox(Graphics2D g2d, int x, int y, int width, String text) {
        g2d.setColor(HUD_BOX_BG);
        g2d.fillRect(x, y, width, HUD_HEIGHT);

        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawRect(x, y, width, HUD_HEIGHT);

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
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
}
