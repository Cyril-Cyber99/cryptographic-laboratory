package visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Matrice vectorielle comparative Graphics2D synthétisant les propriétés de sécurité fondamentales
 * et la résilience expérimentale d'AES-CBC et AES-GCM.
 */
public class SecurityMatrixView extends JPanel {

    public static class MatrixRow {
        public final String criterion;
        public final String cbcValue;
        public final String gcmValue;
        public final StatusType cbcStatus;
        public final StatusType gcmStatus;

        public MatrixRow(String criterion, String cbcValue, String gcmValue, StatusType cbcStatus, StatusType gcmStatus) {
            this.criterion = criterion;
            this.cbcValue = cbcValue;
            this.gcmValue = gcmValue;
            this.cbcStatus = cbcStatus;
            this.gcmStatus = gcmStatus;
        }
    }

    public enum StatusType {
        SECURE(new Color(16, 185, 129), Color.WHITE),      // Emerald
        WARNING(new Color(245, 158, 11), Color.BLACK),     // Amber
        DANGER(new Color(239, 68, 68), Color.WHITE),       // Red
        NEUTRAL(new Color(100, 116, 139), Color.WHITE);    // Slate

        public final Color bg;
        public final Color fg;

        StatusType(Color bg, Color fg) {
            this.bg = bg;
            this.fg = fg;
        }
    }

    private MatrixRow[] rows;

    private static final Color BG_COLOR = new Color(15, 23, 42);      // Slate 900
    private static final Color CARD_BG = new Color(30, 41, 59);       // Slate 800
    private static final Color ROW_ALT = new Color(24, 33, 47);       // Subtle row alternate
    private static final Color BORDER_COLOR = new Color(51, 65, 85);  // Slate 700
    private static final Color TEXT_LIGHT = new Color(248, 250, 252); // Slate 50
    private static final Color TEXT_MUTED = new Color(148, 163, 184); // Slate 400

    public SecurityMatrixView() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(820, 450));
        setMinimumSize(new Dimension(650, 380));
        initDefaultData();
    }

    private void initDefaultData() {
        this.rows = new MatrixRow[]{
                new MatrixRow("Confidentialité", "OUI (IND-CPA avec IV imprévisible)", "OUI (Mode CTR sous-jacent)", StatusType.SECURE, StatusType.SECURE),
                new MatrixRow("Authentification Native", "NON (Requiert HMAC externe)", "OUI (Tag MAC GHASH 128b intégré)", StatusType.DANGER, StatusType.SECURE),
                new MatrixRow("Données Associées (AAD)", "NON (Inexistant)", "OUI (Authentifiées en clair via GHASH)", StatusType.DANGER, StatusType.SECURE),
                new MatrixRow("Tag d'Authentification", "AUCUN", "128 BITS (16 octets systématiques)", StatusType.DANGER, StatusType.SECURE),
                new MatrixRow("Détection Altération Ciphertext", "AUCUNE (Propagation aveugle)", "DÉTECTION IMMÉDIATE & REJET", StatusType.DANGER, StatusType.SECURE),
                new MatrixRow("Résistance Bit-Flipping", "VULNÉRABLE (P[i] altéré sans alerte)", "RÉSISTANT (Rejet avant déchiffrement)", StatusType.DANGER, StatusType.SECURE),
                new MatrixRow("Contrainte IV / Nonce", "IV Aléatoire imprévisible (128b)", "Nonce Unique (96b) — Fuite si réutilisé", StatusType.WARNING, StatusType.WARNING),
                new MatrixRow("Parallélisation Matérielle", "Déchiffrement seul (Chiffrement séquentiel)", "Chiffrement & Déchiffrement parallèles (CTR)", StatusType.WARNING, StatusType.SECURE),
                new MatrixRow("Complexité d'implémentation", "Modérée (gestion Padding PKCS#5)", "Élevée (arithmétique GF(2^128) GHASH)", StatusType.SECURE, StatusType.WARNING),
                new MatrixRow("Sensibilité aux canaux auxiliaires", "Attaques Padding Oracle fréquentes", "Vulnérable aux implémentations GHASH non constantes", StatusType.WARNING, StatusType.WARNING)
        };
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

        // En-tête institutionnel
        g2.setColor(TEXT_LIGHT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString("MATRICE COMPARATIVE VECTORIELLE DES PROPRIÉTÉS DE SÉCURITÉ", 24, 28);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(TEXT_MUTED);
        g2.drawString("Analyse comparative des garanties formelles et limites opérationnelles", 24, 46);

        int startY = 65;
        int tableW = w - 48;
        int rowH = 30;

        // En-têtes de colonnes
        int col1W = (int) (tableW * 0.30);
        int col2W = (int) (tableW * 0.35);
        int col3W = tableW - col1W - col2W;

        g2.setColor(CARD_BG);
        g2.fill(new RoundRectangle2D.Float(24, startY, tableW, rowH, 6, 6));
        g2.setColor(BORDER_COLOR);
        g2.draw(new RoundRectangle2D.Float(24, startY, tableW, rowH, 6, 6));

        g2.setColor(TEXT_LIGHT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("PROPRIÉTÉ / CRITÈRE", 36, startY + 20);
        g2.drawString("AES-CBC", 24 + col1W + 12, startY + 20);
        g2.drawString("AES-GCM (AEAD)", 24 + col1W + col2W + 12, startY + 20);

        // Lignes de données
        int y = startY + rowH + 4;
        Font cellFont = new Font("SansSerif", Font.PLAIN, 11);
        Font boldCell = new Font("SansSerif", Font.BOLD, 11);

        for (int i = 0; i < rows.length; i++) {
            MatrixRow r = rows[i];
            Color rowBg = (i % 2 == 0) ? CARD_BG : ROW_ALT;

            g2.setColor(rowBg);
            g2.fillRoundRect(24, y, tableW, rowH, 4, 4);
            g2.setColor(BORDER_COLOR);
            g2.drawRoundRect(24, y, tableW, rowH, 4, 4);

            // Nom du critère
            g2.setColor(TEXT_LIGHT);
            g2.setFont(boldCell);
            g2.drawString(r.criterion, 36, y + 19);

            // CBC Badge & Valeur
            drawBadgeValue(g2, 24 + col1W + 8, y + 5, col2W - 16, rowH - 10, r.cbcValue, r.cbcStatus);

            // GCM Badge & Valeur
            drawBadgeValue(g2, 24 + col1W + col2W + 8, y + 5, col3W - 16, rowH - 10, r.gcmValue, r.gcmStatus);

            y += rowH + 3;
        }

        // Avertissement réglementaire
        g2.setColor(new Color(245, 158, 11));
        g2.setFont(new Font("SansSerif", Font.ITALIC, 10));
        g2.drawString("Note méthodologique : Indicateur pédagogique synthétique — ne constitue pas une métrique cryptographique officielle.", 24, h - 14);

        g2.dispose();
    }

    private void drawBadgeValue(Graphics2D g2, int x, int y, int w, int h, String text, StatusType status) {
        g2.setColor(new Color(status.bg.getRed(), status.bg.getGreen(), status.bg.getBlue(), 40));
        g2.fillRoundRect(x, y, w, h, 4, 4);
        g2.setColor(status.bg);
        g2.drawRoundRect(x, y, w, h, 4, 4);

        g2.setColor(status.fg);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();

        // Tronquer élégamment si trop long
        String disp = text;
        if (fm.stringWidth(disp) > w - 12) {
            while (disp.length() > 3 && fm.stringWidth(disp + "...") > w - 12) {
                disp = disp.substring(0, disp.length() - 1);
            }
            disp += "...";
        }

        g2.drawString(disp, x + 8, y + (h + fm.getAscent()) / 2 - 2);
    }
}
