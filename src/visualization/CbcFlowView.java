package visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

/**
 * Composant graphique vectoriel Graphics2D modélisant le flux de chiffrement séquentiel AES-CBC.
 *
 * Chaînage visualisé :
 * Bloc 1 : P1 XOR IV  -> AES(K) -> C1
 * Bloc 2 : P2 XOR C1  -> AES(K) -> C2
 * Bloc 3 : P3 XOR C2  -> AES(K) -> C3
 *
 * Fournit une animation pas-à-pas pédagogique vectorielle avec contrôleur temporel interactif.
 */
public class CbcFlowView extends JPanel {

    private int currentStep = 0;
    private final int totalSteps = 6;
    private Timer animationTimer;
    private boolean isPlaying = false;

    // Palette institutionnelle
    private static final Color BG_COLOR = new Color(15, 23, 42);         // Slate 900
    private static final Color CARD_BG = new Color(30, 41, 59);          // Slate 800
    private static final Color BORDER_COLOR = new Color(71, 85, 105);     // Slate 600
    private static final Color ACCENT_CYAN = new Color(14, 165, 233);    // Cyan 500
    private static final Color ACCENT_EMERALD = new Color(16, 185, 129); // Emerald 500
    private static final Color ACCENT_AMBER = new Color(245, 158, 11);   // Amber 500
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);    // Slate 50
    private static final Color TEXT_MUTED = new Color(148, 163, 184);    // Slate 400

    public CbcFlowView() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(800, 195));
        setMinimumSize(new Dimension(500, 150));

        animationTimer = new Timer(1200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentStep < totalSteps) {
                    currentStep++;
                } else {
                    currentStep = 1;
                }
                repaint();
            }
        });
    }

    public void nextStep() {
        if (currentStep < totalSteps) {
            currentStep++;
            repaint();
        }
    }

    public void previousStep() {
        if (currentStep > 0) {
            currentStep--;
            repaint();
        }
    }

    public void play() {
        isPlaying = true;
        if (currentStep >= totalSteps) {
            currentStep = 0;
        }
        animationTimer.start();
    }

    public void pause() {
        isPlaying = false;
        animationTimer.stop();
    }

    public void reset() {
        pause();
        currentStep = 0;
        repaint();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Activation de l'anti-aliasing haute fidélité
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Fond structuré avec dégradé subtil
        GradientPaint gp = new GradientPaint(0, 0, BG_COLOR, 0, h, new Color(10, 15, 30));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        // Titre et indicateur d'étape
        g2.setColor(TEXT_LIGHT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString("MÉCANISME VECTORIEL AES-CBC (CIPHER BLOCK CHAINING)", 24, 28);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(ACCENT_CYAN);
        String stepText = getStepDescription(currentStep);
        g2.drawString(stepText, 24, 48);

        // Dessin des 3 blocs séquentiels
        int blockCount = 3;
        int paddingX = 40;
        int usableW = w - (paddingX * 2);
        int colW = usableW / blockCount;

        for (int i = 0; i < blockCount; i++) {
            int cx = paddingX + i * colW + (colW / 2);
            drawCbcColumn(g2, cx, h, i + 1, currentStep);
        }

        g2.dispose();
    }

    private void drawCbcColumn(Graphics2D g2, int cx, int totalH, int blockNum, int step) {
        int baseY = 80;
        int ptY = baseY + 20;
        int xorY = baseY + 80;
        int aesY = baseY + 140;
        int ctY = baseY + 210;

        int boxW = 100;
        int boxH = 34;

        Font fontBold = new Font("SansSerif", Font.BOLD, 12);
        Font fontPlain = new Font("SansSerif", Font.PLAIN, 11);

        // 1. BLOC PLAINTEXT P[i]
        boolean showPt = step >= 1;
        drawBox(g2, cx - (boxW / 2), ptY, boxW, boxH,
                showPt ? CARD_BG : new Color(20, 28, 44),
                showPt ? ACCENT_CYAN : BORDER_COLOR,
                "Plaintext P" + blockNum,
                showPt ? TEXT_LIGHT : TEXT_MUTED);

        // Flèche Plaintext -> XOR
        if (step >= 1) {
            drawArrow(g2, cx, ptY + boxH, cx, xorY - 12, ACCENT_CYAN);
        }

        // 2. OPÉRATEUR XOR
        boolean showXor = (step >= 3) || (blockNum == 1 && step >= 2);
        int xorRadius = 14;
        g2.setColor(showXor ? CARD_BG : new Color(20, 28, 44));
        g2.fillOval(cx - xorRadius, xorY - xorRadius, xorRadius * 2, xorRadius * 2);
        g2.setColor(showXor ? ACCENT_AMBER : BORDER_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(cx - xorRadius, xorY - xorRadius, xorRadius * 2, xorRadius * 2);
        g2.drawLine(cx, xorY - xorRadius + 4, cx, xorY + xorRadius - 4);
        g2.drawLine(cx - xorRadius + 4, xorY, cx + xorRadius - 4, xorY);

        // Flèche IV (pour bloc 1) ou Chaînage C[i-1] (pour bloc > 1) vers XOR
        if (blockNum == 1) {
            boolean showIv = step >= 2;
            int ivBoxW = 70;
            int ivBoxH = 26;
            int ivX = cx - boxW - 20;
            int ivY = xorY - (ivBoxH / 2);
            drawBox(g2, ivX, ivY, ivBoxW, ivBoxH,
                    showIv ? CARD_BG : new Color(20, 28, 44),
                    showIv ? ACCENT_AMBER : BORDER_COLOR,
                    "IV (128b)",
                    showIv ? ACCENT_AMBER : TEXT_MUTED);
            if (showIv) {
                drawArrow(g2, ivX + ivBoxW, xorY, cx - xorRadius, xorY, ACCENT_AMBER);
            }
        }

        // Flèche XOR -> AES
        if (step >= 3) {
            drawArrow(g2, cx, xorY + xorRadius, cx, aesY, ACCENT_AMBER);
        }

        // 3. BOÎTE AES(K)
        boolean showAes = step >= 4;
        drawBox(g2, cx - (boxW / 2), aesY, boxW, boxH,
                showAes ? new Color(14, 116, 144) : new Color(20, 28, 44),
                showAes ? ACCENT_CYAN : BORDER_COLOR,
                "AES [Clé K]",
                TEXT_LIGHT);

        // Flèche AES -> Ciphertext
        if (step >= 4) {
            drawArrow(g2, cx, aesY + boxH, cx, ctY, ACCENT_EMERALD);
        }

        // 4. BLOC CIPHERTEXT C[i]
        boolean showCt = step >= 5;
        drawBox(g2, cx - (boxW / 2), ctY, boxW, boxH,
                showCt ? CARD_BG : new Color(20, 28, 44),
                showCt ? ACCENT_EMERALD : BORDER_COLOR,
                "Ciphertext C" + blockNum,
                showCt ? ACCENT_EMERALD : TEXT_MUTED);

        // 5. RÉTROACTION / CHAÎNAGE C[i] -> XOR du bloc suivant
        if (step >= 6 && blockNum < 3) {
            int nextCx = cx + (getWidth() - 80) / 3;
            g2.setColor(ACCENT_EMERALD);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int branchY = ctY + (boxH / 2);
            int dropY = ctY + boxH + 16;
            int nextXorY = baseY + 80;

            // Ligne de dérivation vectorielle
            g2.drawLine(cx + (boxW / 2), branchY, cx + (boxW / 2) + 20, branchY);
            g2.drawLine(cx + (boxW / 2) + 20, branchY, cx + (boxW / 2) + 20, dropY);
            g2.drawLine(cx + (boxW / 2) + 20, dropY, nextCx - (boxW / 2) - 16, dropY);
            g2.drawLine(nextCx - (boxW / 2) - 16, dropY, nextCx - (boxW / 2) - 16, nextXorY);
            drawArrow(g2, nextCx - (boxW / 2) - 16, nextXorY, nextCx - xorRadius, nextXorY, ACCENT_EMERALD);
        }
    }

    private void drawBox(Graphics2D g2, int x, int y, int w, int h, Color bg, Color border, String text, Color textColor) {
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 8, 8));
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(x, y, w, h, 8, 8));

        g2.setColor(textColor);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int strW = fm.stringWidth(text);
        int strH = fm.getAscent();
        g2.drawString(text, x + (w - strW) / 2, y + (h + strH) / 2 - 2);
    }

    private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x1, y1, x2, y2);

        // Flèche directionnelle
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 6;
        int ax1 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
        int ay1 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
        int ax2 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
        int ay2 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(x2, y2);
        arrowHead.addPoint(ax1, ay1);
        arrowHead.addPoint(ax2, ay2);
        g2.fill(arrowHead);
    }

    private String getStepDescription(int step) {
        switch (step) {
            case 0:
                return "État initial : Prêt à lancer l'animation du flux AES-CBC.";
            case 1:
                return "Étape 1/6 : Réception du texte clair (Plaintext P) divisé en blocs de 128 bits.";
            case 2:
                return "Étape 2/6 : Injection du vecteur d'initialisation aléatoire (IV) pour le premier bloc.";
            case 3:
                return "Étape 3/6 : Opération XOR entre le bloc de clair et l'IV (ou le bloc chiffré précédent).";
            case 4:
                return "Étape 4/6 : Chiffrement par le bloc AES sous la clé secrète K.";
            case 5:
                return "Étape 5/6 : Émission du bloc chiffré (Ciphertext C).";
            case 6:
                return "Étape 6/6 : Réinjection séquentielle (chaînage) de C[i] pour le XOR du bloc P[i+1].";
            default:
                return "";
        }
    }
}
