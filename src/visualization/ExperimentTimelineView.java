package visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Composant graphique vectoriel Graphics2D traçant la progression pas-à-pas des 13 étapes
 * du mode démonstration pour soutenance de Master devant un jury.
 */
public class ExperimentTimelineView extends JPanel {

    private int currentStep = 1;
    private final int totalSteps = 13;
    private final String[] stepLabels = {
            "Présentation CBC",
            "Présentation GCM",
            "Chiffrement Commun",
            "Flux Vectoriel CBC",
            "Flux Vectoriel GCM",
            "Altération Ciphertext",
            "Vérification GCM",
            "Altération AAD",
            "Altération Tag",
            "Nonce Reuse",
            "Benchmark Réel",
            "Graphiques Graphics2D",
            "Conclusion Scientifique"
    };

    private static final Color BG_COLOR = new Color(15, 23, 42);
    private static final Color NODE_DONE = new Color(16, 185, 129);
    private static final Color NODE_ACTIVE = new Color(14, 165, 233);
    private static final Color NODE_PENDING = new Color(51, 65, 85);
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    public ExperimentTimelineView() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(800, 75));
        setMinimumSize(new Dimension(600, 65));
    }

    public void setCurrentStep(int step) {
        this.currentStep = Math.max(1, Math.min(totalSteps, step));
        repaint();
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public String getCurrentStepLabel() {
        return stepLabels[currentStep - 1];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, w, h);

        int padX = 30;
        int usableW = w - (padX * 2);
        int stepSpacing = usableW / (totalSteps - 1);
        int lineY = 28;

        // Ligne de liaison arrière
        g2.setColor(NODE_PENDING);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(padX, lineY, padX + usableW, lineY);

        // Ligne de liaison achevée
        if (currentStep > 1) {
            int doneX = padX + (currentStep - 1) * stepSpacing;
            g2.setColor(NODE_DONE);
            g2.drawLine(padX, lineY, doneX, lineY);
        }

        // Nœuds d'étapes
        int nodeRadius = 10;
        for (int i = 0; i < totalSteps; i++) {
            int cx = padX + i * stepSpacing;
            int stepNum = i + 1;

            Color nodeColor;
            if (stepNum < currentStep) {
                nodeColor = NODE_DONE;
            } else if (stepNum == currentStep) {
                nodeColor = NODE_ACTIVE;
            } else {
                nodeColor = NODE_PENDING;
            }

            // Cercle du nœud
            g2.setColor(nodeColor);
            g2.fillOval(cx - nodeRadius, lineY - nodeRadius, nodeRadius * 2, nodeRadius * 2);
            g2.setColor(TEXT_LIGHT);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(cx - nodeRadius, lineY - nodeRadius, nodeRadius * 2, nodeRadius * 2);

            // Numéro d'étape
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            FontMetrics fm = g2.getFontMetrics();
            String numStr = String.valueOf(stepNum);
            g2.drawString(numStr, cx - fm.stringWidth(numStr) / 2, lineY + fm.getAscent() / 2 - 1);

            // Libellé sous le nœud (pour le nœud actif en priorité, et les extrêmes)
            if (stepNum == currentStep) {
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.setColor(NODE_ACTIVE);
                String lbl = "Étape " + stepNum + " : " + stepLabels[i];
                int strW = g2.getFontMetrics().stringWidth(lbl);
                int lx = Math.max(10, Math.min(w - strW - 10, cx - (strW / 2)));
                g2.drawString(lbl, lx, lineY + 28);
            }
        }

        g2.dispose();
    }
}
