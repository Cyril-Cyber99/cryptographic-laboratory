package visualization;

import experiment.PerformanceExperiment;
import util.FormatUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Moteur de graphique vectoriel scientifique 100% Graphics2D (sans bibliothèque tierce).
 *
 * Fonctionnalités :
 * - Double série comparative (AES-CBC vs AES-GCM).
 * - Tracé dynamique d'histogrammes et/ou courbes d'interpolation.
 * - Grille orthogonale, axes gradués dynamiques, étiquettes, unités, valeurs et légende institutionnelle.
 * - Modes de visualisation commutables : Débit (Mo/s), Temps de Chiffrement (ms/µs), Temps de Déchiffrement (ms/µs).
 */
public class PerformanceChart extends JPanel {

    public enum MetricType {
        THROUGHPUT("Débit Cryptographique", "Mo/s"),
        ENCRYPTION_TIME("Temps de Chiffrement", "µs"),
        DECRYPTION_TIME("Temps de Déchiffrement", "µs");

        public final String title;
        public final String unit;

        MetricType(String title, String unit) {
            this.title = title;
            this.unit = unit;
        }
    }

    private MetricType currentMetric = MetricType.THROUGHPUT;
    private List<PerformanceExperiment.OperationMetrics> cbcMetrics = new ArrayList<>();
    private List<PerformanceExperiment.OperationMetrics> gcmMetrics = new ArrayList<>();

    // Palette institutionnelle
    private static final Color BG_COLOR = new Color(15, 23, 42);         // Slate 900
    private static final Color CHART_BG = new Color(24, 32, 47);         // Darker Slate
    private static final Color GRID_COLOR = new Color(51, 65, 85);       // Slate 700
    private static final Color AXIS_COLOR = new Color(148, 163, 184);    // Slate 400
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);    // Slate 50
    private static final Color CBC_COLOR = new Color(14, 165, 233);      // Cyan 500
    private static final Color GCM_COLOR = new Color(16, 185, 129);      // Emerald 500

    public PerformanceChart() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(750, 420));
        setMinimumSize(new Dimension(500, 300));
    }

    public void setMetricType(MetricType type) {
        this.currentMetric = type;
        repaint();
    }

    public void updateData(List<PerformanceExperiment.OperationMetrics> cbc,
                           List<PerformanceExperiment.OperationMetrics> gcm) {
        this.cbcMetrics = cbc != null ? new ArrayList<>(cbc) : new ArrayList<>();
        this.gcmMetrics = gcm != null ? new ArrayList<>(gcm) : new ArrayList<>();
        repaint();
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

        // En-tête : Titre et sous-titre scientifique
        g2.setColor(TEXT_LIGHT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString("BENCHMARK COMPARATIF : " + currentMetric.title.toUpperCase(), 30, 28);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(AXIS_COLOR);
        g2.drawString("Données expérimentales issues de System.nanoTime() [Unité : " + currentMetric.unit + "]", 30, 46);

        // Légende (en haut à droite)
        drawLegend(g2, w - 240, 20);

        // Zone du graphique
        int padLeft = 80;
        int padRight = 40;
        int padTop = 70;
        int padBottom = 60;
        int chartW = w - padLeft - padRight;
        int chartH = h - padTop - padBottom;

        if (chartW <= 50 || chartH <= 50) {
            g2.dispose();
            return;
        }

        // Fond du cadre de tracé
        g2.setColor(CHART_BG);
        g2.fill(new RoundRectangle2D.Float(padLeft, padTop, chartW, chartH, 8, 8));
        g2.setColor(GRID_COLOR);
        g2.draw(new RoundRectangle2D.Float(padLeft, padTop, chartW, chartH, 8, 8));

        int dataCount = Math.max(cbcMetrics.size(), gcmMetrics.size());
        if (dataCount == 0) {
            g2.setColor(AXIS_COLOR);
            g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
            String msg = "Aucune mesure expérimentale enregistrée. Lancez le benchmark pour afficher les résultats réels.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, padLeft + (chartW - fm.stringWidth(msg)) / 2, padTop + chartH / 2);
            g2.dispose();
            return;
        }

        // Détermination de l'échelle maximale Y
        double maxY = 0.0;
        for (int i = 0; i < dataCount; i++) {
            if (i < cbcMetrics.size()) maxY = Math.max(maxY, extractValue(cbcMetrics.get(i)));
            if (i < gcmMetrics.size()) maxY = Math.max(maxY, extractValue(gcmMetrics.get(i)));
        }
        if (maxY <= 0) maxY = 100.0;
        maxY = maxY * 1.15; // Marge supérieure de 15%

        // Tracé de la grille et des graduations de l'axe Y
        int yTicks = 5;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();

        for (int t = 0; t <= yTicks; t++) {
            double tickVal = (maxY / yTicks) * t;
            int y = padTop + chartH - (int) ((tickVal / maxY) * chartH);

            g2.setColor(GRID_COLOR);
            g2.drawLine(padLeft, y, padLeft + chartW, y);

            String tickLabel = String.format("%.1f", tickVal);
            g2.setColor(AXIS_COLOR);
            g2.drawString(tickLabel, padLeft - fm.stringWidth(tickLabel) - 8, y + 4);
        }

        // Tracé des barres comparatives pour chaque taille
        int groupW = chartW / dataCount;
        int barW = Math.max(12, Math.min(36, groupW / 3));

        for (int i = 0; i < dataCount; i++) {
            int groupCenterX = padLeft + i * groupW + (groupW / 2);

            // Données CBC
            if (i < cbcMetrics.size()) {
                double valCbc = extractValue(cbcMetrics.get(i));
                int barH = (int) ((valCbc / maxY) * chartH);
                int bx = groupCenterX - barW - 2;
                int by = padTop + chartH - barH;

                g2.setColor(CBC_COLOR);
                g2.fillRoundRect(bx, by, barW, barH, 4, 4);

                // Valeur au sommet
                String valStr = formatCompact(valCbc);
                g2.setColor(TEXT_LIGHT);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.drawString(valStr, bx + (barW - g2.getFontMetrics().stringWidth(valStr)) / 2, by - 4);
            }

            // Données GCM
            if (i < gcmMetrics.size()) {
                double valGcm = extractValue(gcmMetrics.get(i));
                int barH = (int) ((valGcm / maxY) * chartH);
                int bx = groupCenterX + 2;
                int by = padTop + chartH - barH;

                g2.setColor(GCM_COLOR);
                g2.fillRoundRect(bx, by, barW, barH, 4, 4);

                // Valeur au sommet
                String valStr = formatCompact(valGcm);
                g2.setColor(TEXT_LIGHT);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.drawString(valStr, bx + (barW - g2.getFontMetrics().stringWidth(valStr)) / 2, by - 4);
            }

            // Étiquette axe X (Taille)
            int sizeBytes = (i < cbcMetrics.size()) ? cbcMetrics.get(i).sizeBytes : gcmMetrics.get(i).sizeBytes;
            String sizeLabel = FormatUtils.formatSize(sizeBytes);
            g2.setColor(TEXT_LIGHT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            int strW = g2.getFontMetrics().stringWidth(sizeLabel);
            g2.drawString(sizeLabel, groupCenterX - (strW / 2), padTop + chartH + 18);
        }

        // Axes principaux
        g2.setColor(AXIS_COLOR);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(padLeft, padTop, padLeft, padTop + chartH); // Axe Y
        g2.drawLine(padLeft, padTop + chartH, padLeft + chartW, padTop + chartH); // Axe X

        // Labels des axes
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString("Tailles d'échantillon testées (Mo / Ko)", padLeft + (chartW / 2) - 90, padTop + chartH + 42);

        g2.dispose();
    }

    private double extractValue(PerformanceExperiment.OperationMetrics m) {
        if (m == null) return 0.0;
        switch (currentMetric) {
            case THROUGHPUT:
                return m.throughputMBs;
            case ENCRYPTION_TIME:
            case DECRYPTION_TIME:
                return m.meanNanos / 1000.0; // Conversion en microsecondes (µs)
            default:
                return 0.0;
        }
    }

    private String formatCompact(double val) {
        if (val >= 1000) {
            return String.format("%.0f", val);
        } else if (val >= 10) {
            return String.format("%.1f", val);
        } else {
            return String.format("%.2f", val);
        }
    }

    private void drawLegend(Graphics2D g2, int x, int y) {
        g2.setColor(CHART_BG);
        g2.fillRoundRect(x, y, 220, 32, 6, 6);
        g2.setColor(GRID_COLOR);
        g2.drawRoundRect(x, y, 220, 32, 6, 6);

        // AES-CBC
        g2.setColor(CBC_COLOR);
        g2.fillRect(x + 12, y + 10, 14, 12);
        g2.setColor(TEXT_LIGHT);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("AES-CBC", x + 32, y + 21);

        // AES-GCM
        g2.setColor(GCM_COLOR);
        g2.fillRect(x + 120, y + 10, 14, 12);
        g2.setColor(TEXT_LIGHT);
        g2.drawString("AES-GCM", x + 140, y + 21);
    }
}
