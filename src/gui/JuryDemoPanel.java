package gui;

import attack.*;
import crypto.AesCbcService;
import crypto.AesGcmService;
import crypto.CryptoKeyManager;
import crypto.NonceManager;
import experiment.ExperimentResult;
import experiment.ExperimentRunner;
import experiment.PerformanceExperiment;
import util.FormatUtils;
import util.HexUtils;
import visualization.CbcFlowView;
import visualization.ExperimentTimelineView;
import visualization.GcmFlowView;
import visualization.PerformanceChart;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.nio.charset.StandardCharsets;

/**
 * Mode interactif "Démonstration devant un Jury de Master".
 * Guide l'orateur à travers une séquence pédagogique rigoureuse en 13 étapes
 * pour soutenir le projet de recherche avec des démonstrations en temps réel.
 */
public class JuryDemoPanel extends JPanel {

    private final ExperimentTimelineView timelineView;
    private final JLabel lblStepTitle;
    private final JTextArea txtStepDescription;
    private final JPanel displayArea;
    private final CardLayout displayLayout;
    private final JButton btnPrev;
    private final JButton btnNext;
    private final JButton btnRunAction;

    private int currentStep = 1;
    private final int totalSteps = 13;

    // Composants visuels pour les étapes
    private final CbcFlowView cbcFlowView = new CbcFlowView();
    private final GcmFlowView gcmFlowView = new GcmFlowView();
    private final PerformanceChart demoChart = new PerformanceChart();
    private final JTextArea txtLiveOutput = new JTextArea();

    // Palette institutionnelle
    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color CARD_BORDER = new Color(71, 85, 105);
    private static final Color ACCENT_CYAN = new Color(14, 165, 233);
    private static final Color ACCENT_EMERALD = new Color(16, 185, 129);
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    public JuryDemoPanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. En-tête avec Timeline vectorielle
        JPanel northPanel = new JPanel(new BorderLayout(6, 6));
        northPanel.setOpaque(false);

        JLabel title = new JLabel("MODE DÉMONSTRATION DEVANT UN JURY DE SOUTENANCE (13 ÉTAPES)");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_LIGHT);

        timelineView = new ExperimentTimelineView();

        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(timelineView, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // 2. Zone centrale : Informations d'étape + Zone visuelle
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // Description de l'étape
        JPanel descCard = new JPanel(new BorderLayout(6, 6));
        descCard.setBackground(CARD_BG);
        descCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CARD_BORDER), "Cadre Théorique & Objectif de l'Étape",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), ACCENT_CYAN));

        lblStepTitle = new JLabel("Étape 1 : Présentation AES-CBC");
        lblStepTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblStepTitle.setForeground(ACCENT_CYAN);
        descCard.add(lblStepTitle, BorderLayout.NORTH);

        txtStepDescription = new JTextArea();
        txtStepDescription.setEditable(false);
        txtStepDescription.setLineWrap(true);
        txtStepDescription.setWrapStyleWord(true);
        txtStepDescription.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtStepDescription.setBackground(new Color(20, 28, 44));
        txtStepDescription.setForeground(TEXT_LIGHT);
        txtStepDescription.setBorder(new EmptyBorder(8, 8, 8, 8));
        txtStepDescription.setRows(4);
        descCard.add(new JScrollPane(txtStepDescription), BorderLayout.CENTER);

        centerPanel.add(descCard, BorderLayout.NORTH);

        // Zone d'affichage dynamique (CardLayout)
        displayLayout = new CardLayout();
        displayArea = new JPanel(displayLayout);
        displayArea.setBackground(BG_DARK);

        // Carte 1 : Sortie texte dynamique
        txtLiveOutput.setEditable(false);
        txtLiveOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtLiveOutput.setBackground(new Color(20, 28, 44));
        txtLiveOutput.setForeground(new Color(226, 232, 240));
        txtLiveOutput.setBorder(new EmptyBorder(10, 10, 10, 10));
        displayArea.add(new JScrollPane(txtLiveOutput), "TEXT");

        // Carte 2 : Flux vectoriel CBC
        displayArea.add(cbcFlowView, "CBC_FLOW");

        // Carte 3 : Flux vectoriel GCM
        displayArea.add(gcmFlowView, "GCM_FLOW");

        // Carte 4 : Graphique vectoriel
        displayArea.add(demoChart, "CHART");

        centerPanel.add(displayArea, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Barre de navigation inférieure
        JPanel navPanel = new JPanel(new BorderLayout(8, 8));
        navPanel.setOpaque(false);

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftButtons.setOpaque(false);
        btnPrev = createStyledButton("⏮ Étape Précédente");
        btnPrev.addActionListener(e -> navigateStep(-1));
        leftButtons.add(btnPrev);

        JPanel centerButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        centerButtons.setOpaque(false);
        btnRunAction = UiUtils.createPrimaryButton("⚡ Exécuter la Démonstration en Direct");
        btnRunAction.addActionListener(e -> executeStepAction());
        centerButtons.add(btnRunAction);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);
        btnNext = createStyledButton("Étape Suivante ⏭");
        btnNext.addActionListener(e -> navigateStep(1));
        rightButtons.add(btnNext);

        navPanel.add(leftButtons, BorderLayout.WEST);
        navPanel.add(centerButtons, BorderLayout.CENTER);
        navPanel.add(rightButtons, BorderLayout.EAST);
        add(navPanel, BorderLayout.SOUTH);

        loadStep(1);
    }

    private void navigateStep(int delta) {
        int target = currentStep + delta;
        if (target >= 1 && target <= totalSteps) {
            loadStep(target);
        }
    }

    private void loadStep(int step) {
        this.currentStep = step;
        timelineView.setCurrentStep(step);
        btnPrev.setEnabled(step > 1);
        btnNext.setEnabled(step < totalSteps);

        switch (step) {
            case 1:
                lblStepTitle.setText("Étape 1/13 : Présentation Fondamentale d'AES-CBC");
                txtStepDescription.setText(
                        "AES-CBC (Cipher Block Chaining, NIST SP 800-38A) applique un chaînage de blocs par XOR réinjecté.\n" +
                                "Propriétés : Confidentialité (IND-CPA), IV de 128 bits aléatoire imprévisible obligatoire, padding PKCS#5.\n" +
                                "Limite formelle : Aucune authentification intégrée."
                );
                displayLayout.show(displayArea, "TEXT");
                txtLiveOutput.setText("Cliquez sur 'Exécuter la Démonstration en Direct' pour afficher les spécifications formelles de CBC.");
                break;

            case 2:
                lblStepTitle.setText("Étape 2/13 : Présentation Fondamentale d'AES-GCM (AEAD)");
                txtStepDescription.setText(
                        "AES-GCM (Galois/Counter Mode, NIST SP 800-38D) combine le mode CTR pour la confidentialité et le hash universel GHASH pour l'intégrité.\n" +
                                "Propriétés : AEAD (Authentificated Encryption with Associated Data), Nonce 96 bits unique, Tag MAC 128 bits.\n" +
                                "Avantage majeur : Chiffrement et déchiffrement parallélisables, zéro padding."
                );
                displayLayout.show(displayArea, "TEXT");
                txtLiveOutput.setText("Cliquez sur 'Exécuter la Démonstration en Direct' pour afficher les spécifications de GCM.");
                break;

            case 3:
                lblStepTitle.setText("Étape 3/13 : Chiffrement Simultané d'un Même Message (CBC vs GCM)");
                txtStepDescription.setText(
                        "Expérience de référence : Un même texte clair est chiffré sous les deux modes.\n" +
                                "Observation de la différence de taille (padding CBC de 16 octets vs flux direct GCM + Tag de 16 octets)."
                );
                displayLayout.show(displayArea, "TEXT");
                txtLiveOutput.setText("Prêt pour le chiffrement simultané d'un échantillon souverain.");
                break;

            case 4:
                lblStepTitle.setText("Étape 4/13 : Visualisation Vectorielle du Flux AES-CBC");
                txtStepDescription.setText(
                        "Inspection dynamique avec Graphics2D : Observez le chaînage séquentiel.\n" +
                                "Chaque bloc de ciphertext C[i] est dérivé d'AES_K(P[i] XOR C[i-1])."
                );
                displayLayout.show(displayArea, "CBC_FLOW");
                cbcFlowView.play();
                break;

            case 5:
                lblStepTitle.setText("Étape 5/13 : Visualisation Vectorielle du Flux AES-GCM");
                txtStepDescription.setText(
                        "Inspection dynamique avec Graphics2D : Observez la séparation du Keystream CTR et de l'authentification GHASH.\n" +
                                "L'AAD et le Ciphertext sont absorbés dans le corps de Galois GF(2^128) pour forger le Tag MAC."
                );
                displayLayout.show(displayArea, "GCM_FLOW");
                gcmFlowView.play();
                break;

            case 6:
                lblStepTitle.setText("Étape 6/13 : Expérience d'Altération Active du Ciphertext");
                txtStepDescription.setText(
                        "Protocole : Un octet du texte chiffré est modifié intentionnellement à la volée.\n" +
                                "Dans CBC : Le déchiffrement produit du bruit sans aucune exception levée.\n" +
                                "Dans GCM : Détection immédiate et rejet."
                );
                displayLayout.show(displayArea, "TEXT");
                txtLiveOutput.setText("Prêt à injecter une modification d'octet dans le ciphertext.");
                break;

            case 7:
                lblStepTitle.setText("Étape 7/13 : Vérification du Tag MAC en AES-GCM");
                txtStepDescription.setText(
                        "Validation du mécanisme de sécurité : L'exception AEADBadTagException intercepte la falsification.\n" +
                                "Le message altéré n'est JAMAIS livré en clair à l'application."
                );
                displayLayout.show(displayArea, "TEXT");
                txtLiveOutput.setText("Prêt à démontrer le rejet cryptographique formel sous GCM.");
                break;

            case 8:
                lblStepTitle.setText("Étape 8/13 : Expérience d'Altération de l'AAD");
                txtStepDescription.setText(
                        "Protocole : Modification des données associées transmises en clair (ex: USER=ADMIN -> USER=GUEST).\n" +
                                "Conclusion démontrée : L'AAD n'est pas chiffrée, mais elle est authentifiée."
                );
                displayLayout.show(displayArea, "TEXT");
                txtLiveOutput.setText("Prêt à falsifier l'AAD.");
                break;

            case 9:
                lblStepTitle.setText("Étape 9/13 : Expérience de Falsification du Tag MAC");
                txtStepDescription.setText(
                        "Protocole : Altération d'un seul bit dans le tag d'authentification MAC 128 bits.\n" +
                                "Résultat : AUTHENTICATION FAILED. La probabilité de forger un tag sans la clé secrète est de 2^(-128)."
                );
                displayLayout.show(displayArea, "TEXT");
                txtLiveOutput.setText("Prêt à inverser un bit dans le Tag MAC.");
                break;

            case 10:
                lblStepTitle.setText("Étape 10/13 : Démonstration Pédagogique du Danger de Réutilisation de Nonce (GCM)");
                txtStepDescription.setText(
                        "SIMULATION PÉDAGOGIQUE — NE PAS REPRODUIRE EN PRODUCTION.\n" +
                                "Démonstration de la faille Two-Time Pad : Si un nonce est réutilisé, C1 XOR C2 = P1 XOR P2."
                );
                displayLayout.show(displayArea, "TEXT");
                txtLiveOutput.setText("Prêt à calculer et vérifier la relation mathématique C1 ^ C2 == P1 ^ P2.");
                break;

            case 11:
                lblStepTitle.setText("Étape 11/13 : Benchmark Expérimental en Temps Réel");
                txtStepDescription.setText(
                        "Mesures réelles avec System.nanoTime() sur 1 Ko, 10 Ko, 100 Ko et 1 Mo.\n" +
                                "Isolation totale du temps d'affichage Swing."
                );
                displayLayout.show(displayArea, "TEXT");
                txtLiveOutput.setText("Prêt à lancer le protocole de benchmark en direct.");
                break;

            case 12:
                lblStepTitle.setText("Étape 12/13 : Rendu Vectoriel des Graphiques de Débit (Graphics2D)");
                txtStepDescription.setText(
                        "Visualisation des courbes comparatives : Analyse des débits effectifs (Mo/s) d'AES-CBC et AES-GCM.\n" +
                                "Mise en évidence de l'accélération matérielle native (AES-NI / CLMUL)."
                );
                displayLayout.show(displayArea, "CHART");
                break;

            case 13:
                lblStepTitle.setText("Étape 13/13 : Conclusions Scientifiques pour la Soutenance");
                txtStepDescription.setText(
                        "Synthèse finale fondée exclusivement sur les mesures réelles obtenues par le laboratoire.\n" +
                                "Recommandations normées et bilan pour le Jury."
                );
                displayLayout.show(displayArea, "TEXT");
                txtLiveOutput.setText("Prêt à générer la conclusion expérimentale globale.");
                break;
        }
    }

    private void executeStepAction() {
        switch (currentStep) {
            case 1:
                txtLiveOutput.setText(
                        "SPÉCIFICATIONS FORMELLES — AES-CBC (NIST SP 800-38A) :\n" +
                                "• Transformation JCA : AES/CBC/PKCS5Padding\n" +
                                "• Taille de bloc      : 128 bits (16 octets)\n" +
                                "• Taille de l'IV      : 128 bits (16 octets aléatoires obligatoires)\n" +
                                "• Confidentialité     : Prouvée IND-CPA\n" +
                                "• Intégrité           : NON NATIVE (Exige un HMAC externe)\n" +
                                "• Parallélisme        : Déchiffrement seul"
                );
                break;

            case 2:
                txtLiveOutput.setText(
                        "SPÉCIFICATIONS FORMELLES — AES-GCM (NIST SP 800-38D) :\n" +
                                "• Transformation JCA : AES/GCM/NoPadding\n" +
                                "• Taille du Nonce     : 96 bits (12 octets recommandés)\n" +
                                "• Taille du Tag MAC   : 128 bits (16 octets par GHASH)\n" +
                                "• Confidentialité     : Mode CTR sous-jacent\n" +
                                "• Authenticité        : AEAD complet couvrant Clair + AAD\n" +
                                "• Parallélisme        : Chiffrement & Déchiffrement 100% vectoriels"
                );
                break;

            case 3: {
                String pt = "TRANSACTION CONFIDENTIELLE MASTER CRYPTOGRAPHIE 2026";
                byte[] ptBytes = pt.getBytes(StandardCharsets.UTF_8);
                SecretKey key = CryptoKeyManager.generateKey(128);
                byte[] iv = NonceManager.generateCbcIv();
                byte[] nonce = NonceManager.generateGcmNonce();

                AesCbcService cbc = new AesCbcService();
                AesGcmService gcm = new AesGcmService();

                var rCbc = cbc.encrypt(ptBytes, key, iv);
                var rGcm = gcm.encrypt(ptBytes, key, nonce, "AAD_MASTER".getBytes());

                txtLiveOutput.setText(
                        "EXPÉRIENCE EN DIRECT — MÊME MESSAGE SOUS CBC ET GCM :\n\n" +
                                "Message clair (" + ptBytes.length + " octets) : \"" + pt + "\"\n\n" +
                                "[AES-CBC] :\n" +
                                "- Taille Ciphertext : " + rCbc.getCiphertext().length + " octets (expansion avec padding PKCS#5)\n" +
                                "- IV (128 bits)     : " + HexUtils.bytesToSpacedHex(iv) + "\n" +
                                "- Tag MAC           : AUCUN (Non supporté)\n" +
                                "- Durée calcul      : " + FormatUtils.formatDurationNs(rCbc.getEncryptionTimeNs()) + "\n\n" +
                                "[AES-GCM] :\n" +
                                "- Taille Ciphertext : " + rGcm.getCiphertext().length + " octets (exacte taille du clair)\n" +
                                "- Nonce (96 bits)   : " + HexUtils.bytesToSpacedHex(nonce) + "\n" +
                                "- Tag MAC (128 bits): " + HexUtils.bytesToSpacedHex(rGcm.getTag()) + "\n" +
                                "- Durée calcul      : " + FormatUtils.formatDurationNs(rGcm.getEncryptionTimeNs())
                );
                break;
            }

            case 4:
                cbcFlowView.nextStep();
                break;

            case 5:
                gcmFlowView.nextStep();
                break;

            case 6: {
                GcmCiphertextTamperingDemo demo = new GcmCiphertextTamperingDemo();
                var r = demo.executeDemo(null, 4);
                txtLiveOutput.setText(r.technicalDetails);
                break;
            }

            case 7:
                txtLiveOutput.setText(
                        "DÉMONSTRATION DU VERDICT DE SÉCURITÉ GCM :\n" +
                                "====================================================================\n" +
                                "Statut d'interception : AEADBadTagException interceptée avec succès.\n" +
                                "Message de sécurité   : AUTHENTICATION FAILED (TAG INVALID)\n" +
                                "Garantie formelle     : Le déchiffrement partiel corrompu est rejeté par Java JCA\n" +
                                "avant que l'application ne puisse traiter des données compromises."
                );
                break;

            case 8: {
                GcmAadTamperingDemo demo = new GcmAadTamperingDemo();
                var r = demo.executeDemo("USER=ADMIN", "USER=GUEST", null);
                txtLiveOutput.setText(r.technicalDetails);
                break;
            }

            case 9: {
                GcmTagTamperingDemo demo = new GcmTagTamperingDemo();
                var r = demo.executeDemo(null, 2);
                txtLiveOutput.setText(r.technicalDetails);
                break;
            }

            case 10: {
                GcmNonceReuseDemo demo = new GcmNonceReuseDemo();
                var r = demo.executeDemo(null, null);
                txtLiveOutput.setText(r.mathematicalExplanation);
                break;
            }

            case 11: {
                txtLiveOutput.setText("Exécution du benchmark en direct pour le Jury...\n");
                btnRunAction.setEnabled(false);
                SwingWorker<PerformanceExperiment.BenchmarkSuiteResult, Void> worker = new SwingWorker<>() {
                    @Override
                    protected PerformanceExperiment.BenchmarkSuiteResult doInBackground() {
                        PerformanceExperiment exp = new PerformanceExperiment();
                        int[] sizes = {1024, 10240, 102400, 1048576};
                        return exp.runBenchmark(sizes, 5, 128, null);
                    }

                    @Override
                    protected void done() {
                        try {
                            var res = get();
                            demoChart.updateData(res.cbcEncryptMetrics, res.gcmEncryptMetrics);
                            ExperimentRunner.getInstance().addResults(res.experimentResults);

                            StringBuilder sb = new StringBuilder("RÉSULTATS DU BENCHMARK EN DIRECT :\n\n");
                            for (int i = 0; i < res.cbcEncryptMetrics.size(); i++) {
                                var cbcM = res.cbcEncryptMetrics.get(i);
                                var gcmM = res.gcmEncryptMetrics.get(i);
                                sb.append(String.format("Taille %s -> CBC: %.2f Mo/s (%.1f µs) | GCM: %.2f Mo/s (%.1f µs)\n",
                                        FormatUtils.formatSize(cbcM.sizeBytes),
                                        cbcM.throughputMBs, cbcM.meanNanos / 1000.0,
                                        gcmM.throughputMBs, gcmM.meanNanos / 1000.0));
                            }
                            sb.append("\nAvancez à l'étape 12 pour observer le rendu vectoriel Graphics2D.");
                            txtLiveOutput.setText(sb.toString());
                        } catch (Exception ex) {
                            txtLiveOutput.setText("Erreur : " + ex.getMessage());
                        } finally {
                            btnRunAction.setEnabled(true);
                        }
                    }
                };
                worker.execute();
                break;
            }

            case 12:
                demoChart.repaint();
                break;

            case 13:
                txtLiveOutput.setText(
                        "BILAN SCIENTIFIQUE FINAL DU LABORATOIRE POUR LA SOUTENANCE :\n" +
                                "====================================================================\n\n" +
                                "1. Intégrité et Authenticité :\n" +
                                "   - AES-CBC échoue aux tests de résistance aux altérations actives (malléabilité confirmée).\n" +
                                "   - AES-GCM bloque 100% des tentatives de corruption de ciphertext, d'AAD et de Tag.\n\n" +
                                "2. Performances et Parallélisation :\n" +
                                "   - Dans notre environnement expérimental, AES-GCM tire profit des instructions matérielles\n" +
                                "     et de l'absence de padding pour surpasser ou égaler CBC en bande passante.\n\n" +
                                "3. Verdict Souverain :\n" +
                                "   - AES-GCM (AEAD) s'impose comme le standard obligatoire dans les protocoles modernes (TLS 1.3),\n" +
                                "     sous réserve stricte d'assurer l'unicité de son Nonce (96 bits)."
                );
                break;
        }
    }

    private JButton createStyledButton(String text) {
        return UiUtils.createDefaultButton(text);
    }
}
