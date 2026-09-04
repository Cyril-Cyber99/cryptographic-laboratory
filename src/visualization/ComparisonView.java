package visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Composant graphique vectoriel Graphics2D offrant une vue synchrone côte-à-côte (Side-by-Side)
 * des mécanismes fondamentaux d'AES-CBC et AES-GCM.
 */
public class ComparisonView extends JPanel {

    private static final Color BG_COLOR = new Color(15, 23, 42);         // Slate 900
    private static final Color CARD_BG = new Color(30, 41, 59);          // Slate 800
    private static final Color BORDER_COLOR = new Color(71, 85, 105);     // Slate 600
    private static final Color CBC_ACCENT = new Color(14, 165, 233);     // Cyan 500
    private static final Color GCM_ACCENT = new Color(16, 185, 129);     // Emerald 500
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);    // Slate 50
    private static final Color TEXT_MUTED = new Color(148, 163, 184);    // Slate 400

    public ComparisonView() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(800, 320));
        setMinimumSize(new Dimension(550, 260));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Fond
        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, w, h);

        int midX = w / 2;
        int halfW = (w - 60) / 2;
        int cardH = h - 60;

        // Panneau Gauche : AES-CBC
        drawAlgorithmCard(g2, 20, 40, halfW, cardH, "AES-CBC (CIPHER BLOCK CHAINING)", CBC_ACCENT, new String[]{
                "• Mode bloc à rétroaction séquentielle",
                "• IV aléatoire imprévisible de 128 bits obligatoire",
                "• Chiffrement strictement non parallélisable",
                "• Exige un bourrage (PKCS#5 / PKCS#7 Padding)",
                "• Aucune intégrité : vulnérable au Bit-Flipping",
                "• Risque élevé d'attaques Padding Oracle en réseau"
        });

        // Panneau Droit : AES-GCM
        drawAlgorithmCard(g2, midX + 10, 40, halfW, cardH, "AES-GCM (GALOIS/COUNTER MODE - AEAD)", GCM_ACCENT, new String[]{
                "• Chiffrement de flux (CTR) + Hachage universel (GHASH)",
                "• Nonce unique de 96 bits recommandé (NIST SP 800-38D)",
                "• Chiffrement et déchiffrement 100% parallélisables",
                "• Zéro padding : longueur ciphertext = longueur clair",
                "• Authentification native intégrée (Tag MAC 128 bits)",
                "• Support des métadonnées authentifiées en clair (AAD)"
        });

        // Séparateur vertical médian
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));
        g2.drawLine(midX, 40, midX, h - 20);

        g2.dispose();
    }

    private void drawAlgorithmCard(Graphics2D g2, int x, int y, int w, int h, String title, Color accent, String[] points) {
        g2.setColor(CARD_BG);
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 10, 10));
        g2.setColor(accent);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(x, y, w, h, 10, 10));

        // En-tête
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35));
        g2.fill(new RoundRectangle2D.Float(x, y, w, 32, 10, 10));
        g2.setColor(accent);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString(title, x + 16, y + 21);

        // Puces
        int textY = y + 54;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (String p : points) {
            g2.setColor(TEXT_LIGHT);
            g2.drawString(p, x + 16, textY);
            textY += 24;
        }
    }
}
