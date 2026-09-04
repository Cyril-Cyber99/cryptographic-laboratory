package gui;

import experiment.ExperimentRunner;
import experiment.PerformanceExperiment;
import util.FormatUtils;
import visualization.PerformanceChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Module de Benchmark scientifique comparant rigoureusement les performances d'AES-CBC et AES-GCM.
 * Intègre un moteur vectoriel Graphics2D PerformanceChart et une table statistique d'échantillonnage.
 */
public class PerformancePanel extends JPanel {

    private JComboBox<Integer> cbKeySize;
    private JComboBox<Integer> cbRepetitions;
    private JCheckBox chk1K, chk10K, chk100K, chk1M, chk5M, chk10M;
    private JButton btnRunBenchmark;
    private JProgressBar progressBar;
    private JLabel lblStatus;

    private PerformanceChart performanceChart;
    private JTable metricsTable;
    private DefaultTableModel tableModel;

    private PerformanceExperiment.BenchmarkSuiteResult lastSuiteResult;

    // Palette institutionnelle
    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color CARD_BORDER = new Color(71, 85, 105);
    private static final Color ACCENT_CYAN = new Color(14, 165, 233);
    private static final Color ACCENT_EMERALD = new Color(16, 185, 129);
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    public PerformancePanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
    }

    private void initUI() {
        // En-tête
        JPanel northPanel = new JPanel(new BorderLayout(4, 4));
        northPanel.setOpaque(false);

        JLabel title = new JLabel("BANC D'ESSAI SCIENTIFIQUE & ANALYSE DE DÉBIT (BENCHMARK)");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_LIGHT);

        JLabel desc = new JLabel("Mesure exclusive du temps cryptographique (System.nanoTime()) avec échauffement JIT préalable et répétitions.");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(TEXT_MUTED);

        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(desc, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // Corps : Haut (Paramètres & Contrôle), Centre (Graphique Graphics2D & Tableau statistique)
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);

        centerPanel.add(buildConfigCard(), BorderLayout.NORTH);

        // Zone centrale graphique + tableau
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);

        // Graphique vectoriel
        JPanel chartContainer = new JPanel(new BorderLayout(6, 6));
        chartContainer.setOpaque(false);

        // Barre d'options de métriques du graphique
        JPanel chartControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        chartControls.setOpaque(false);
        JLabel chartModeLbl = new JLabel("Métrique affichée sur le graphique : ");
        chartModeLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        chartModeLbl.setForeground(TEXT_LIGHT);
        chartControls.add(chartModeLbl);

        JRadioButton rbThroughput = new JRadioButton("Débit (Mo/s)", true);
        JRadioButton rbEncTime = new JRadioButton("Temps Chiffrement (µs)");
        JRadioButton rbDecTime = new JRadioButton("Temps Déchiffrement (µs)");

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbThroughput);
        bg.add(rbEncTime);
        bg.add(rbDecTime);

        styleRadio(rbThroughput);
        styleRadio(rbEncTime);
        styleRadio(rbDecTime);

        rbThroughput.addActionListener(e -> performanceChart.setMetricType(PerformanceChart.MetricType.THROUGHPUT));
        rbEncTime.addActionListener(e -> performanceChart.setMetricType(PerformanceChart.MetricType.ENCRYPTION_TIME));
        rbDecTime.addActionListener(e -> performanceChart.setMetricType(PerformanceChart.MetricType.DECRYPTION_TIME));

        chartControls.add(rbThroughput);
        chartControls.add(rbEncTime);
        chartControls.add(rbDecTime);

        chartContainer.add(chartControls, BorderLayout.NORTH);
        performanceChart = new PerformanceChart();
        chartContainer.add(performanceChart, BorderLayout.CENTER);

        splitPane.setTopComponent(chartContainer);

        // Tableau statistique inférieur
        splitPane.setBottomComponent(buildTablePanel());

        centerPanel.add(splitPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildConfigCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CARD_BORDER), "Protocole Expérimental de Mesure",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), ACCENT_CYAN));

        JPanel configRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        configRow.setOpaque(false);

        // Taille Clé
        configRow.add(createFieldLabel("Clé :"));
        cbKeySize = new JComboBox<>(new Integer[]{128, 192, 256});
        cbKeySize.setBackground(CARD_BG);
        cbKeySize.setForeground(TEXT_LIGHT);
        configRow.add(cbKeySize);

        // Répétitions
        configRow.add(createFieldLabel("Répétitions par test :"));
        cbRepetitions = new JComboBox<>(new Integer[]{5, 10, 20});
        cbRepetitions.setBackground(CARD_BG);
        cbRepetitions.setForeground(TEXT_LIGHT);
        configRow.add(cbRepetitions);

        // Checkboxes Tailles
        configRow.add(createFieldLabel("Tailles d'échantillons :"));
        chk1K = createCheckBox("1 Ko", true);
        chk10K = createCheckBox("10 Ko", true);
        chk100K = createCheckBox("100 Ko", true);
        chk1M = createCheckBox("1 Mo", true);
        chk5M = createCheckBox("5 Mo", false);
        chk10M = createCheckBox("10 Mo", false);

        configRow.add(chk1K);
        configRow.add(chk10K);
        configRow.add(chk100K);
        configRow.add(chk1M);
        configRow.add(chk5M);
        configRow.add(chk10M);

        btnRunBenchmark = UiUtils.createPrimaryButton("⚡ Démarrer le Protocole de Benchmark");
        btnRunBenchmark.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRunBenchmark.addActionListener(e -> launchBenchmarkAsync());
        configRow.add(btnRunBenchmark);

        card.add(configRow, BorderLayout.CENTER);

        // Barre d'état et progression
        JPanel statusRow = new JPanel(new BorderLayout(8, 4));
        statusRow.setOpaque(false);
        lblStatus = new JLabel("Prêt pour l'expérimentation.");
        lblStatus.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblStatus.setForeground(TEXT_MUTED);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setBackground(new Color(15, 23, 42));
        progressBar.setForeground(ACCENT_CYAN);
        progressBar.setPreferredSize(new Dimension(200, 16));

        statusRow.add(lblStatus, BorderLayout.CENTER);
        statusRow.add(progressBar, BorderLayout.EAST);

        card.add(statusRow, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setOpaque(false);

        String[] colNames = {
                "Taille Données",
                "CBC Chiff. (µs)", "CBC Déchiff. (µs)", "CBC Débit (Mo/s)",
                "GCM Chiff. (µs)", "GCM Déchiff. (µs)", "GCM Débit (Mo/s)",
                "Ratio Débit (GCM / CBC)"
        };

        tableModel = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        metricsTable = new JTable(tableModel);
        metricsTable.setBackground(new Color(20, 28, 44));
        metricsTable.setForeground(TEXT_LIGHT);
        metricsTable.setGridColor(CARD_BORDER);
        metricsTable.setRowHeight(24);
        metricsTable.setFont(new Font("SansSerif", Font.PLAIN, 11));
        metricsTable.getTableHeader().setBackground(CARD_BG);
        metricsTable.getTableHeader().setForeground(ACCENT_CYAN);
        metricsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));

        JScrollPane scroll = new JScrollPane(metricsTable);
        scroll.getViewport().setBackground(new Color(15, 23, 42));
        scroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void launchBenchmarkAsync() {
        List<Integer> sizesList = new ArrayList<>();
        if (chk1K.isSelected()) sizesList.add(1 * 1024);
        if (chk10K.isSelected()) sizesList.add(10 * 1024);
        if (chk100K.isSelected()) sizesList.add(100 * 1024);
        if (chk1M.isSelected()) sizesList.add(1024 * 1024);
        if (chk5M.isSelected()) sizesList.add(5 * 1024 * 1024);
        if (chk10M.isSelected()) sizesList.add(10 * 1024 * 1024);

        if (sizesList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sélectionnez au moins une taille d'échantillon.", "Configuration", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int[] sizes = sizesList.stream().mapToInt(Integer::intValue).toArray();
        int repetitions = (Integer) cbRepetitions.getSelectedItem();
        int keySize = (Integer) cbKeySize.getSelectedItem();

        btnRunBenchmark.setEnabled(false);
        progressBar.setValue(0);

        SwingWorker<PerformanceExperiment.BenchmarkSuiteResult, String> worker =
                new SwingWorker<>() {
                    @Override
                    protected PerformanceExperiment.BenchmarkSuiteResult doInBackground() {
                        PerformanceExperiment exp = new PerformanceExperiment();
                        return exp.runBenchmark(sizes, repetitions, keySize, (task, pct) -> {
                            publish(task + " (" + pct + "%)");
                            setProgress(pct);
                        });
                    }

                    @Override
                    protected void process(List<String> chunks) {
                        if (!chunks.isEmpty()) {
                            String latest = chunks.get(chunks.size() - 1);
                            lblStatus.setText(latest);
                            progressBar.setValue(getProgress());
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            lastSuiteResult = get();
                            performanceChart.updateData(lastSuiteResult.cbcEncryptMetrics, lastSuiteResult.gcmEncryptMetrics);
                            populateTable(lastSuiteResult);
                            ExperimentRunner.getInstance().addResults(lastSuiteResult.experimentResults);
                            lblStatus.setText("Protocole expérimental achevé avec succès. " +
                                    lastSuiteResult.experimentResults.size() + " résultats enregistrés.");
                        } catch (Exception ex) {
                            lblStatus.setText("Erreur lors de l'exécution : " + ex.getMessage());
                        } finally {
                            btnRunBenchmark.setEnabled(true);
                            progressBar.setValue(100);
                        }
                    }
                };

        worker.execute();
    }

    private void populateTable(PerformanceExperiment.BenchmarkSuiteResult res) {
        tableModel.setRowCount(0);
        int count = res.cbcEncryptMetrics.size();

        for (int i = 0; i < count; i++) {
            var cbcEnc = res.cbcEncryptMetrics.get(i);
            var cbcDec = res.cbcDecryptMetrics.get(i);
            var gcmEnc = res.gcmEncryptMetrics.get(i);
            var gcmDec = res.gcmDecryptMetrics.get(i);

            double ratio = (cbcEnc.throughputMBs > 0) ? (gcmEnc.throughputMBs / cbcEnc.throughputMBs) : 1.0;

            tableModel.addRow(new Object[]{
                    FormatUtils.formatSize(cbcEnc.sizeBytes),
                    FormatUtils.formatDecimal(cbcEnc.meanNanos / 1000.0),
                    FormatUtils.formatDecimal(cbcDec.meanNanos / 1000.0),
                    FormatUtils.formatDecimal(cbcEnc.throughputMBs),
                    FormatUtils.formatDecimal(gcmEnc.meanNanos / 1000.0),
                    FormatUtils.formatDecimal(gcmDec.meanNanos / 1000.0),
                    FormatUtils.formatDecimal(gcmEnc.throughputMBs),
                    String.format("%.2f x", ratio)
            });
        }
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(TEXT_LIGHT);
        return l;
    }

    private JCheckBox createCheckBox(String text, boolean sel) {
        JCheckBox cb = new JCheckBox(text, sel);
        cb.setOpaque(false);
        cb.setForeground(TEXT_LIGHT);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return cb;
    }

    private void styleRadio(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setForeground(TEXT_LIGHT);
        rb.setFont(new Font("SansSerif", Font.PLAIN, 11));
    }
}
