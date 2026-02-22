package Spring2026Team10;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class MapPreviewFrame extends JFrame {
    public MapPreviewFrame() {
        super("Escape Map Preview");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(new MapPanel(new PrisonMap()), BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MapPreviewFrame frame = new MapPreviewFrame();
            frame.setVisible(true);
        });
    }
}
