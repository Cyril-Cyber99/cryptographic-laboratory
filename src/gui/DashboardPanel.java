package gui;

import experiment.ExperimentResult;
import experiment.ExperimentRunner;
import util.FormatUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Tableau de bord d'accueil du Laboratoire Cryptographique.
 * Distingue formellement et visuellement les connaissances théoriques
 * des métriques empiriques obtenues par expérimentation réelle.
 */
public class DashboardPanel extends JPanel implements ExperimentRunner.HistoryChangeListener {

    private JLabel lblExpCount;
    private JLabel lblLastExp;
    private JLabel lblLastThroughput;
    private JLabel lblTestStatus;

    // Palette institutionnelle
    private static final Color BG_DARK = new Color(15, 23, 42);         // Slate 900
    private static final Color CARD_BG = new Color(30, 41, 59);          // Slate 800
    private static final Color CARD_BORDER = new Color(51, 65, 85);      // Slate 700
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);    // Slate 50
    private static final Color TEXT_MUTED = new Color(148, 163, 184);    // Slate 400
    private static final Color ACCENT_CYAN = new Color(14, 165, 233);    // Cyan 500
    private static final Color ACCENT_EMERALD = new Color(16, 185, 129); // Emerald 500
    private static final Color ACCENT_AMBER = new Color(245, 158, 11);   // Amber 500

    public DashboardPanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        initUI();
        ExperimentRunner.getInstance().addListener(this);
        updateDynamicMetrics(ExperimentRunner.getInstance().getHistory());
    }

    private void initUI() {
        // En-tête institutionnel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("LABORATOIRE EXPÉRIMENTAL — ANALYSE COMPARATIVE AES-CBC VS AES-GCM");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_LIGHT);

        JLabel subLabel = new JLabel("Environnement académique de niveau Master : évaluation empirique, intégrité, attaques actives et benchmarks.");
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLabel.setForeground(TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(subLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Corps central : 2 rangées (Haut: Propriétés théoriques, Bas: Métriques expérimentales réelles)
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 16, 16));
        centerPanel.setOpaque(false);

        // Section 1 : Propriétés Théoriques Normatives
        centerPanel.add(buildTheoreticalSection());

        // Section 2 : Métriques Expérimentales Mesurées
        centerPanel.add(buildExperimentalMetricsSection());

        add(centerPanel, BorderLayout.CENTER);

        // Pied de page méthodologique
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setOpaque(false);
        JLabel noteLabel = new JLabel("Règle méthodologique stricte : Toute valeur expérimentale affichée provient exclusivement d'une exécution réelle capturée via System.nanoTime().");
        noteLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        noteLabel.setForeground(ACCENT_AMBER);
        footerPanel.add(noteLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel buildTheoreticalSection() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JLabel banner = new JLabel("  [ INFORMATION THÉORIQUE — PROPRIÉTÉS FORMELLES DES NORMES NIST ]");
        banner.setFont(new Font("SansSerif", Font.BOLD, 11));
        banner.setForeground(ACCENT_CYAN);
        panel.add(banner, BorderLayout.NORTH);

        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsGrid.setOpaque(false);

        // Carte Théorique AES-CBC
        cardsGrid.add(createCard("AES-CBC (NIST SP 800-38A)", new String[][]{
                {"Confidentialité", "OUI (IND-CPA avec IV aléatoire)"},
                {"Authentification intégrée", "NON (Aucun contrôle d'intégrité)"},
                {"Données associées (AAD)", "NON"},
                {"Tag d'authentification (MAC)", "NON (Inexistant)"},
                {"Gestion IV", "Vecteur 128 bits aléatoire imprévisible"},
                {"Parallélisation", "Déchiffrement seul"}
        }, ACCENT_CYAN));

        // Carte Théorique AES-GCM
        cardsGrid.add(createCard("AES-GCM (NIST SP 800-38D)", new String[][]{
                {"Confidentialité", "OUI (Mode CTR sous-jacent)"},
                {"Authentification intégrée", "OUI (Tag MAC de 128 bits via GHASH)"},
                {"Données associées (AAD)", "OUI (En-têtes en clair protégés)"},
                {"Tag d'authentification (MAC)", "OUI (16 octets / 128 bits)"},
                {"Gestion Nonce", "96 bits strictement unique par clé"},
                {"Parallélisation", "Chiffrement et Déchiffrement"}
        }, ACCENT_EMERALD));

        panel.add(cardsGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildExperimentalMetricsSection() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JLabel banner = new JLabel("  [ RÉSULTAT EXPÉRIMENTAL — STATUT DU BANC D'ESSAI LOCAL ]");
        banner.setFont(new Font("SansSerif", Font.BOLD, 11));
        banner.setForeground(ACCENT_EMERALD);
        panel.add(banner, BorderLayout.NORTH);

        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsGrid.setOpaque(false);

        lblExpCount = new JLabel("0", SwingConstants.CENTER);
        cardsGrid.add(createMetricCard("EXPÉRIENCES EXÉCUTÉES", lblExpCount, "Total archivé"));

        lblLastExp = new JLabel("Aucune", SwingConstants.CENTER);
        cardsGrid.add(createMetricCard("DERNIÈRE EXPÉRIENCE", lblLastExp, "Identifiant / Mode"));

        lblLastThroughput = new JLabel("-- Mo/s", SwingConstants.CENTER);
        cardsGrid.add(createMetricCard("DERNIER DÉBIT MESURÉ", lblLastThroughput, "Système local"));

        lblTestStatus = new JLabel("Prêt", SwingConstants.CENTER);
        cardsGrid.add(createMetricCard("STATUT DU LABORATOIRE", lblTestStatus, "Suite cryptographique"));

        panel.add(cardsGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCard(String title, String[][] items, Color accent) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        titleLbl.setForeground(accent);
        card.add(titleLbl, BorderLayout.NORTH);

        JPanel itemsPanel = new JPanel(new GridLayout(items.length, 2, 6, 6));
        itemsPanel.setOpaque(false);

        for (String[] it : items) {
            JLabel name = new JLabel(it[0] + " :");
            name.setFont(new Font("SansSerif", Font.PLAIN, 11));
            name.setForeground(TEXT_MUTED);

            JLabel val = new JLabel(it[1]);
            val.setFont(new Font("SansSerif", Font.BOLD, 11));
            val.setForeground(TEXT_LIGHT);

            itemsPanel.add(name);
            itemsPanel.add(val);
        }
        card.add(itemsPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(6, 6));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        titleLbl.setForeground(TEXT_MUTED);
        card.add(titleLbl, BorderLayout.NORTH);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        valueLabel.setForeground(ACCENT_CYAN);
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel subLbl = new JLabel(subtitle, SwingConstants.CENTER);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        subLbl.setForeground(TEXT_MUTED);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    private void updateDynamicMetrics(List<ExperimentResult> history) {
        if (history == null || history.isEmpty()) {
            lblExpCount.setText("0");
            lblLastExp.setText("Aucune");
            lblLastThroughput.setText("-- Mo/s");
            lblTestStatus.setText("En attente");
            return;
        }

        lblExpCount.setText(String.valueOf(history.size()));
        ExperimentResult last = history.get(0);
        lblLastExp.setText(last.getExperimentId() + " (" + last.getMode() + ")");

        if (last.getThroughputMBs() > 0) {
            lblLastThroughput.setText(FormatUtils.formatThroughput(last.getThroughputMBs()));
        } else {
            lblLastThroughput.setText("-- Mo/s");
        }

        lblTestStatus.setText(last.getResult());
    }

    @Override
    public void onHistoryUpdated(List<ExperimentResult> updatedHistory) {
        SwingUtilities.invokeLater(() -> updateDynamicMetrics(updatedHistory));
    }
}
