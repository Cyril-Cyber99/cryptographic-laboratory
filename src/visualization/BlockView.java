package visualization;

import util.HexUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Composant graphique vectoriel Graphics2D représentant un bloc ou une séquence de blocs AES de 16 octets.
 * Met en évidence les octets intègres, altérés, ou sélectionnés par l'utilisateur.
 */
public class BlockView extends JPanel {

    public interface ByteClickListener {
        void onByteClicked(int byteIndex, byte byteValue);
    }

    private byte[] data;
    private int highlightedByteIndex = -1;
    private int corruptedByteIndex = -1;
    private ByteClickListener listener;

    private static final Color BG_COLOR = new Color(30, 41, 59);         // Slate 800
    private static final Color BLOCK_BORDER = new Color(71, 85, 105);    // Slate 600
    private static final Color BYTE_BG = new Color(15, 23, 42);          // Slate 900
    private static final Color BYTE_TEXT = new Color(226, 232, 240);     // Slate 200
    private static final Color HIGHLIGHT_BG = new Color(14, 165, 233);   // Cyan 500
    private static final Color CORRUPT_BG = new Color(239, 68, 68);      // Red 500
    private static final Color CORRUPT_TEXT = Color.WHITE;

    public BlockView() {
        this(new byte[16]);
    }

    public BlockView(byte[] initialData) {
        this.data = initialData != null ? initialData : new byte[16];
        setBackground(new Color(15, 23, 42));
        setPreferredSize(new Dimension(500, 110));
        setMinimumSize(new Dimension(300, 90));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedIdx = calculateByteIndexAt(e.getPoint());
                if (clickedIdx >= 0 && clickedIdx < (data != null ? data.length : 0)) {
                    highlightedByteIndex = clickedIdx;
                    repaint();
                    if (listener != null) {
                        listener.onByteClicked(clickedIdx, data[clickedIdx]);
                    }
                }
            }
        });
    }

    public void setData(byte[] data) {
        this.data = data;
        repaint();
    }

    public void setCorruptedByteIndex(int index) {
        this.corruptedByteIndex = index;
        repaint();
    }

    public void setHighlightedByteIndex(int index) {
        this.highlightedByteIndex = index;
        repaint();
    }

    public void setByteClickListener(ByteClickListener listener) {
        this.listener = listener;
    }

    private int calculateByteIndexAt(Point pt) {
        if (data == null || data.length == 0) return -1;
        int w = getWidth();
        int cols = 16;
        int cellW = Math.max(22, (w - 40) / cols);
        int cellH = 26;
        int startX = 20;
        int startY = 42;

        if (pt.y >= startY && pt.y <= startY + cellH) {
            int col = (pt.x - startX) / cellW;
            if (col >= 0 && col < data.length) {
                return col;
            }
        }
        return -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Cadre extérieur
        g2.setColor(BG_COLOR);
        g2.fill(new RoundRectangle2D.Float(8, 8, w - 16, h - 16, 12, 12));
        g2.setColor(BLOCK_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(8, 8, w - 16, h - 16, 12, 12));

        if (data == null || data.length == 0) {
            g2.setColor(new Color(148, 163, 184));
            g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
            g2.drawString("[Aucune donnée de bloc chargée]", 24, 45);
            g2.dispose();
            return;
        }

        // Titre et métadonnées
        g2.setColor(new Color(248, 250, 252));
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("BLOC VECTORIEL AES (Taille : " + data.length + " octets / " + (data.length * 8) + " bits)", 20, 28);

        int cols = Math.min(data.length, 16);
        int cellW = Math.max(24, (w - 40) / cols);
        int cellH = 28;
        int startX = 20;
        int startY = 40;

        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < cols; i++) {
            int cx = startX + i * cellW;
            int cy = startY;

            boolean isCorrupt = (i == corruptedByteIndex);
            boolean isHighlight = (i == highlightedByteIndex);

            Color cellBg = isCorrupt ? CORRUPT_BG : (isHighlight ? HIGHLIGHT_BG : BYTE_BG);
            Color textC = isCorrupt ? CORRUPT_TEXT : (isHighlight ? Color.BLACK : BYTE_TEXT);

            // Cellule d'octet
            g2.setColor(cellBg);
            g2.fillRoundRect(cx, cy, cellW - 3, cellH, 6, 6);
            g2.setColor(isCorrupt ? Color.WHITE : BLOCK_BORDER);
            g2.drawRoundRect(cx, cy, cellW - 3, cellH, 6, 6);

            // Valeur hex
            String hex = String.format("%02X", data[i]);
            int strW = fm.stringWidth(hex);
            g2.setColor(textC);
            g2.drawString(hex, cx + (cellW - 3 - strW) / 2, cy + (cellH + fm.getAscent()) / 2 - 2);

            // Numéro d'index au-dessus
            g2.setColor(new Color(100, 116, 139));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            String idxStr = String.valueOf(i);
            int idxW = g2.getFontMetrics().stringWidth(idxStr);
            g2.drawString(idxStr, cx + (cellW - 3 - idxW) / 2, cy - 3);
            g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        }

        // Légende inférieure
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(148, 163, 184));
        g2.drawString("Astuce : Cliquez sur un octet pour le cibler dans les expériences d'altération active.", 20, h - 14);

        g2.dispose();
    }
}
