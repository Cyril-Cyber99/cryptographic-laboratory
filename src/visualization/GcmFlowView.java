package visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Composant graphique vectoriel Graphics2D modélisant le flux AES-GCM (Galois/Counter Mode).
 *
 * Représente fidèlement :
 * 1. Nonce (96 bits) + Compteur CTR 32 bits.
 * 2. Génération du flux de chiffrement (Keystream) via AES_K(CTR_i).
 * 3. Chiffrement par XOR : Plaintext XOR Keystream -> Ciphertext.
 * 4. Traitement GHASH des données associées (AAD) et des blocs de Ciphertext dans GF(2^128).
 * 5. Chiffrement du bloc initial J_0 pour dériver le Tag MAC de 128 bits.
 *
 * Supporte le clic interactif sur chaque bloc avec inspection descriptive.
 */
public class GcmFlowView extends JPanel {

    public interface BlockSelectionListener {
        void onBlockSelected(String blockName, String description);
    }

    private int currentStep = 0;
    private final int totalSteps = 6;
    private Timer animationTimer;
    private boolean isPlaying = false;
    private String selectedBlockInfo = "Cliquez sur un composant pour afficher son rôle dans AES-GCM.";
    private BlockSelectionListener selectionListener;

    // Palette institutionnelle
    private static final Color BG_COLOR = new Color(15, 23, 42);         // Slate 900
    private static final Color CARD_BG = new Color(30, 41, 59);          // Slate 800
    private static final Color BORDER_COLOR = new Color(71, 85, 105);     // Slate 600
    private static final Color ACCENT_CYAN = new Color(14, 165, 233);    // Cyan 500
    private static final Color ACCENT_EMERALD = new Color(16, 185, 129); // Emerald 500
    private static final Color ACCENT_AMBER = new Color(245, 158, 11);   // Amber 500
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);  // Purple 500
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);    // Slate 50
    private static final Color TEXT_MUTED = new Color(148, 163, 184);    // Slate 400

    private static class ClickableRegion {
        Rectangle bounds;
        String name;
        String description;

        ClickableRegion(Rectangle bounds, String name, String description) {
            this.bounds = bounds;
            this.name = name;
            this.description = description;
        }
    }

    private final List<ClickableRegion> clickableRegions = new ArrayList<>();

    public GcmFlowView() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(800, 380));
        setMinimumSize(new Dimension(600, 320));

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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (ClickableRegion r : clickableRegions) {
                    if (r.bounds.contains(e.getPoint())) {
                        selectedBlockInfo = "Composant : " + r.name + "\n" + r.description;
                        if (selectionListener != null) {
                            selectionListener.onBlockSelected(r.name, r.description);
                        }
                        repaint();
                        break;
                    }
                }
            }
        });
    }

    public void setSelectionListener(BlockSelectionListener listener) {
        this.selectionListener = listener;
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Fond
        g2.setPaint(new GradientPaint(0, 0, BG_COLOR, 0, h, new Color(12, 18, 36)));
        g2.fillRect(0, 0, w, h);

        clickableRegions.clear();

        // Titre
        g2.setColor(TEXT_LIGHT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString("MÉCANISME VECTORIEL AES-GCM (GALOIS/COUNTER MODE - AEAD)", 24, 28);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(ACCENT_CYAN);
        g2.drawString(getStepDescription(currentStep), 24, 48);

        int midX = w / 2;

        // --- 1. NONCE & COMPTEUR CTR (Haut) ---
        int nonceW = 160;
        int nonceH = 34;
        int nonceX = midX - (nonceW / 2);
        int nonceY = 70;
        boolean showNonce = currentStep >= 1;
        Rectangle nonceRect = new Rectangle(nonceX, nonceY, nonceW, nonceH);
        drawBox(g2, nonceRect, showNonce ? CARD_BG : new Color(20, 28, 44),
                showNonce ? ACCENT_AMBER : BORDER_COLOR,
                "NONCE (96b) + CTR (32b)", showNonce ? ACCENT_AMBER : TEXT_MUTED);
        clickableRegions.add(new ClickableRegion(nonceRect, "NONCE / CTR",
                "Le Nonce (96 bits) doit être strictement unique par clé.\nLe compteur de 32 bits incrémente pour chaque bloc de 16 octets traité."));

        // Flèche Nonce -> AES CTR
        int aesCtrW = 140;
        int aesCtrH = 32;
        int aesCtrX = midX - (aesCtrW / 2);
        int aesCtrY = nonceY + nonceH + 30;
        boolean showAes = currentStep >= 2;
        if (showNonce) {
            drawArrow(g2, midX, nonceY + nonceH, midX, aesCtrY, ACCENT_AMBER);
        }

        // --- 2. AES / CTR ENGINE ---
        Rectangle aesRect = new Rectangle(aesCtrX, aesCtrY, aesCtrW, aesCtrH);
        drawBox(g2, aesRect, showAes ? new Color(14, 116, 144) : new Color(20, 28, 44),
                showAes ? ACCENT_CYAN : BORDER_COLOR,
                "AES(CTR) -> Keystream", TEXT_LIGHT);
        clickableRegions.add(new ClickableRegion(aesRect, "Moteur AES-CTR",
                "Le mode CTR chiffre le compteur avec la clé K pour produire un masque jetable (Keystream). Parallélisable nativement."));

        // --- 3. PLAINTEXT & CIPHERTEXT (Milieu) ---
        int ptW = 120;
        int ptH = 32;
        int ptX = 60;
        int ptY = aesCtrY + aesCtrH + 30;
        boolean showPt = currentStep >= 3;
        Rectangle ptRect = new Rectangle(ptX, ptY, ptW, ptH);
        drawBox(g2, ptRect, showPt ? CARD_BG : new Color(20, 28, 44),
                showPt ? ACCENT_CYAN : BORDER_COLOR,
                "PLAINTEXT P", showPt ? TEXT_LIGHT : TEXT_MUTED);
        clickableRegions.add(new ClickableRegion(ptRect, "Plaintext (Texte clair)",
                "Données confidentielles à chiffrer et authentifier. N'exige aucun bourrage (padding) en mode CTR."));

        // Opérateur XOR
        int xorX = midX;
        int xorY = ptY + (ptH / 2);
        int xorRadius = 14;
        if (showPt) {
            drawArrow(g2, ptX + ptW, xorY, xorX - xorRadius, xorY, ACCENT_CYAN);
        }
        if (showAes) {
            drawArrow(g2, midX, aesCtrY + aesCtrH, midX, xorY - xorRadius, ACCENT_AMBER);
        }

        // Dessin XOR
        g2.setColor(showPt ? CARD_BG : new Color(20, 28, 44));
        g2.fillOval(xorX - xorRadius, xorY - xorRadius, xorRadius * 2, xorRadius * 2);
        g2.setColor(showPt ? ACCENT_AMBER : BORDER_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(xorX - xorRadius, xorY - xorRadius, xorRadius * 2, xorRadius * 2);
        g2.drawLine(xorX, xorY - xorRadius + 4, xorX, xorY + xorRadius - 4);
        g2.drawLine(xorX - xorRadius + 4, xorY, xorX + xorRadius - 4, xorY);

        // Ciphertext C
        int ctW = 130;
        int ctH = 32;
        int ctX = w - 60 - ctW;
        int ctY = ptY;
        boolean showCt = currentStep >= 3;
        Rectangle ctRect = new Rectangle(ctX, ctY, ctW, ctH);
        drawBox(g2, ctRect, showCt ? CARD_BG : new Color(20, 28, 44),
                showCt ? ACCENT_EMERALD : BORDER_COLOR,
                "CIPHERTEXT C", showCt ? ACCENT_EMERALD : TEXT_MUTED);
        clickableRegions.add(new ClickableRegion(ctRect, "Ciphertext (Texte chiffré)",
                "Résultat du XOR entre le clair et le Keystream AES. Aucune expansion de taille."));

        if (showCt) {
            drawArrow(g2, xorX + xorRadius, xorY, ctX, ctY + (ctH / 2), ACCENT_EMERALD);
        }

        // --- 4. AAD (Données associées) & GHASH (Bas) ---
        int aadW = 130;
        int aadH = 32;
        int aadX = 60;
        int ghashY = ptY + ptH + 40;
        int aadY = ghashY;
        boolean showAad = currentStep >= 4;
        Rectangle aadRect = new Rectangle(aadX, aadY, aadW, aadH);
        drawBox(g2, aadRect, showAad ? CARD_BG : new Color(20, 28, 44),
                showAad ? ACCENT_PURPLE : BORDER_COLOR,
                "AAD (Auth Data)", showAad ? ACCENT_PURPLE : TEXT_MUTED);
        clickableRegions.add(new ClickableRegion(aadRect, "AAD (Additional Authenticated Data)",
                "Données annexes transmises en clair (ex: en-têtes IP, métadonnées) mais rigoureusement authentifiées via GHASH."));

        // GHASH
        int ghashW = 160;
        int ghashH = 36;
        int ghashX = midX - (ghashW / 2);
        boolean showGhash = currentStep >= 4;
        Rectangle ghashRect = new Rectangle(ghashX, ghashY, ghashW, ghashH);
        drawBox(g2, ghashRect, showGhash ? new Color(76, 29, 149) : new Color(20, 28, 44),
                showGhash ? ACCENT_PURPLE : BORDER_COLOR,
                "GHASH [GF(2^128)]", TEXT_LIGHT);
        clickableRegions.add(new ClickableRegion(ghashRect, "GHASH (Galois Hash)",
                "Fonction de hachage universelle dans le corps fini GF(2^128). Absorbe l'AAD et le Ciphertext avec la clé H = AES_K(0)."));

        if (showAad) {
            drawArrow(g2, aadX + aadW, aadY + (aadH / 2), ghashX, ghashY + (ghashH / 2), ACCENT_PURPLE);
        }
        if (showCt) {
            // Ligne descendante de C vers GHASH
            g2.setColor(ACCENT_EMERALD);
            g2.drawLine(ctX + (ctW / 2), ctY + ctH, ctX + (ctW / 2), ghashY + (ghashH / 2));
            drawArrow(g2, ctX + (ctW / 2), ghashY + (ghashH / 2), ghashX + ghashW, ghashY + (ghashH / 2), ACCENT_EMERALD);
        }

        // --- 5. AUTHENTICATION TAG (128 bits) ---
        int tagW = 180;
        int tagH = 34;
        int tagX = midX - (tagW / 2);
        int tagY = ghashY + ghashH + 30;
        boolean showTag = currentStep >= 5;
        Rectangle tagRect = new Rectangle(tagX, tagY, tagW, tagH);
        drawBox(g2, tagRect, showTag ? CARD_BG : new Color(20, 28, 44),
                showTag ? ACCENT_EMERALD : BORDER_COLOR,
                "TAG MAC (128 bits)", showTag ? ACCENT_EMERALD : TEXT_MUTED);
        clickableRegions.add(new ClickableRegion(tagRect, "Tag d'Authentification (MAC)",
                "Tag de 128 bits garantissant l'intégrité et l'authenticité. Émis avec le ciphertext. Rejet immédiat si un seul bit est altéré."));

        if (showGhash && showTag) {
            drawArrow(g2, midX, ghashY + ghashH, midX, tagY, ACCENT_EMERALD);
        }

        // Bandeau d'information contextuelle sélectionnée
        g2.setColor(new Color(15, 23, 42, 220));
        g2.fillRect(10, h - 32, w - 20, 26);
        g2.setColor(BORDER_COLOR);
        g2.drawRect(10, h - 32, w - 20, 26);
        g2.setColor(TEXT_LIGHT);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("ⓘ Info bloc sélectionné : " + selectedBlockInfo.replace('\n', ' '), 20, h - 15);

        g2.dispose();
    }

    private void drawBox(Graphics2D g2, Rectangle r, Color bg, Color border, String text, Color textColor) {
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 8, 8));
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 8, 8));

        g2.setColor(textColor);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int strW = fm.stringWidth(text);
        int strH = fm.getAscent();
        g2.drawString(text, r.x + (r.width - strW) / 2, r.y + (r.height + strH) / 2 - 2);
    }

    private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x1, y1, x2, y2);

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
                return "État initial : Prêt à lancer l'animation AES-GCM (AEAD).";
            case 1:
                return "Étape 1/5 : Instanciation du Nonce aléatoire unique (96 bits) et du compteur CTR initial J_0.";
            case 2:
                return "Étape 2/5 : Chiffrement AES des blocs de compteur pour produire le Keystream (masque de flux).";
            case 3:
                return "Étape 3/5 : Opération XOR entre le Plaintext et le Keystream -> Émission du Ciphertext.";
            case 4:
                return "Étape 4/5 : Alimentation du multiplicateur GHASH avec l'AAD et les blocs de Ciphertext dans GF(2^128).";
            case 5:
                return "Étape 5/5 : Masquage final avec AES_K(J_0) pour forger le Tag d'authentification MAC (128 bits).";
            default:
                return "";
        }
    }
}
