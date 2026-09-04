package gui;

import experiment.*;
import util.FormatUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Console d'expérimentation scientifique et Mode Recherche.
 * Permet de configurer des protocoles de recherche paramétrables et de générer
 * des fiches d'expériences reproductibles et exportables individuellement.
 */
public class ExperimentPanel extends JPanel {

    private JComboBox<String> cbMode;
    private JComboBox<Integer> cbKeySize;
    private JSpinner spMessageSizeKb;
    private JSpinner spRepetitions;
    private JSpinner spExpCount;
    private JTextField txtAad;
    private JButton btnStartResearch;
    private JLabel lblStatus;
    private JProgressBar progressBar;

    private JTextArea txtFicheScientifique;
    private JButton btnSaveFiche;
    private ExperimentResult currentFicheResult;

    // Palette institutionnelle
    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color CARD_BORDER = new Color(71, 85, 105);
    private static final Color ACCENT_CYAN = new Color(14, 165, 233);
    private static final Color ACCENT_EMERALD = new Color(16, 185, 129);
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    public ExperimentPanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
    }

    private void initUI() {
        // En-tête
        JPanel northPanel = new JPanel(new BorderLayout(4, 4));
        northPanel.setOpaque(false);
        JLabel title = new JLabel("MODE RECHERCHE & PROTOCOLES EXPÉRIMENTAUX REPRODUCTIBLES");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_LIGHT);
        JLabel desc = new JLabel("Paramétrez des campagnes de mesures systématiques et éditez des fiches académiques normalisées.");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(TEXT_MUTED);
        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(desc, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // Corps : Gauche (Mode Recherche), Droite (Fiche Scientifique E-XXX)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        centerPanel.setOpaque(false);

        centerPanel.add(buildResearchConfigCard());
        centerPanel.add(buildFicheCard());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildResearchConfigCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CARD_BORDER), "Campagne du Mode Recherche",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), ACCENT_CYAN));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Mode
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(createLabel("Algorithme / Mode :"), gbc);
        gbc.gridx = 1;
        cbMode = new JComboBox<>(new String[]{"AES-GCM (Recommandé)", "AES-CBC", "Les Deux (Comparatif direct)"});
        styleCombo(cbMode);
        form.add(cbMode, gbc);

        // Clé
        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(createLabel("Taille de Clé :"), gbc);
        gbc.gridx = 1;
        cbKeySize = new JComboBox<>(new Integer[]{128, 192, 256});
        styleCombo(cbKeySize);
        form.add(cbKeySize, gbc);

        // Taille message
        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(createLabel("Taille Message (Ko) :"), gbc);
        gbc.gridx = 1;
        spMessageSizeKb = new JSpinner(new SpinnerNumberModel(64, 1, 10240, 64));
        spMessageSizeKb.setFont(new Font("SansSerif", Font.PLAIN, 11));
        form.add(spMessageSizeKb, gbc);

        // AAD
        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(createLabel("AAD (Pour GCM) :"), gbc);
        gbc.gridx = 1;
        txtAad = new JTextField("EXP_PARAM_ID=9021;CTX=RESEARCH");
        txtAad.setBackground(new Color(20, 28, 44));
        txtAad.setForeground(TEXT_LIGHT);
        form.add(txtAad, gbc);

        // Répétitions
        gbc.gridx = 0;
        gbc.gridy = 4;
        form.add(createLabel("Répétitions / Mesure :"), gbc);
        gbc.gridx = 1;
        spRepetitions = new JSpinner(new SpinnerNumberModel(10, 1, 100, 5));
        form.add(spRepetitions, gbc);

        // Nombre d'expériences
        gbc.gridx = 0;
        gbc.gridy = 5;
        form.add(createLabel("Nombre d'Expériences :"), gbc);
        gbc.gridx = 1;
        spExpCount = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
        form.add(spExpCount, gbc);

        card.add(form, BorderLayout.NORTH);

        // Pied : Bouton & Progression
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setOpaque(false);

        lblStatus = new JLabel("Prêt pour la campagne.");
        lblStatus.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblStatus.setForeground(TEXT_MUTED);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setBackground(new Color(15, 23, 42));
        progressBar.setForeground(ACCENT_CYAN);

        btnStartResearch = UiUtils.createPrimaryButton("⚡ Lancer la Campagne de Recherche");
        btnStartResearch.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnStartResearch.addActionListener(e -> runResearchCampaign());

        bottomPanel.add(lblStatus, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(btnStartResearch, BorderLayout.SOUTH);

        card.add(bottomPanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFicheCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CARD_BORDER), "Fiche Expérimentale Reproductible",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), ACCENT_EMERALD));

        txtFicheScientifique = new JTextArea();
        txtFicheScientifique.setEditable(false);
        txtFicheScientifique.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtFicheScientifique.setBackground(new Color(20, 28, 44));
        txtFicheScientifique.setForeground(new Color(226, 232, 240));
        txtFicheScientifique.setBorder(new EmptyBorder(8, 8, 8, 8));
        txtFicheScientifique.setText("[Lancez une expérience ou sélectionnez-en une dans les résultats pour afficher sa fiche.]");

        card.add(new JScrollPane(txtFicheScientifique), BorderLayout.CENTER);

        // Bouton de sauvegarde de la fiche
        btnSaveFiche = UiUtils.createDefaultButton("💾 Sauvegarder cette Fiche Expérimentale (Texte)");
        btnSaveFiche.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnSaveFiche.setEnabled(false);
        btnSaveFiche.addActionListener(e -> saveFicheToFile());
        card.add(btnSaveFiche, BorderLayout.SOUTH);

        return card;
    }

    private void runResearchCampaign() {
        int keySize = (Integer) cbKeySize.getSelectedItem();
        int sizeKb = (Integer) spMessageSizeKb.getValue();
        int repetitions = (Integer) spRepetitions.getValue();
        int expCount = (Integer) spExpCount.getValue();
        int modeIdx = cbMode.getSelectedIndex();
        String aadStr = txtAad.getText().trim();

        btnStartResearch.setEnabled(false);
        lblStatus.setText("Expérience en cours...");
        progressBar.setValue(0);

        SwingWorker<List<ExperimentResult>, Integer> worker = new SwingWorker<>() {
            @Override
            protected List<ExperimentResult> doInBackground() {
                List<ExperimentResult> list = new ArrayList<>();
                byte[] sample = new byte[sizeKb * 1024];
                java.util.Arrays.fill(sample, (byte) 0xAA);
                byte[] aadBytes = aadStr.getBytes(StandardCharsets.UTF_8);

                int totalOperations = expCount * (modeIdx == 2 ? 2 : 1);
                int progress = 0;

                for (int i = 1; i <= expCount; i++) {
                    if (modeIdx == 0 || modeIdx == 2) {
                        // GCM
                        GcmExperiment expGcm = new GcmExperiment("RES-GCM-" + String.format("%03d", i), keySize, sample, aadBytes);
                        ExperimentResult res = expGcm.run();
                        list.add(res);
                        progress++;
                        publish((progress * 100) / totalOperations);
                    }
                    if (modeIdx == 1 || modeIdx == 2) {
                        // CBC
                        CbcExperiment expCbc = new CbcExperiment("RES-CBC-" + String.format("%03d", i), keySize, sample);
                        ExperimentResult res = expCbc.run();
                        list.add(res);
                        progress++;
                        publish((progress * 100) / totalOperations);
                    }
                }
                return list;
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    progressBar.setValue(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    List<ExperimentResult> results = get();
                    ExperimentRunner.getInstance().addResults(results);
                    lblStatus.setText("Expérience terminée. " + results.size() + " fiches archivées.");
                    if (!results.isEmpty()) {
                        currentFicheResult = results.get(0);
                        txtFicheScientifique.setText(currentFicheResult.toAcademicReport());
                        btnSaveFiche.setEnabled(true);
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Erreur : " + ex.getMessage());
                } finally {
                    btnStartResearch.setEnabled(true);
                    progressBar.setValue(100);
                }
            }
        };

        worker.execute();
    }

    private void saveFicheToFile() {
        if (currentFicheResult == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(currentFicheResult.getExperimentId() + "_fiche.txt"));
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(target, StandardCharsets.UTF_8)) {
                fw.write(currentFicheResult.toAcademicReport());
                JOptionPane.showMessageDialog(this, "Fiche sauvegardée avec succès :\n" + target.getAbsolutePath(),
                        "Export Réussi", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Erreur lors de la sauvegarde : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(TEXT_LIGHT);
        return l;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(CARD_BG);
        cb.setForeground(TEXT_LIGHT);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 11));
    }
}
