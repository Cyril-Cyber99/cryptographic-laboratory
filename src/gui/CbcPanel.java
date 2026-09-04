package gui;

import crypto.AesCbcService;
import crypto.CryptoKeyManager;
import crypto.CryptoResult;
import crypto.NonceManager;
import experiment.ExperimentResult;
import experiment.ExperimentRunner;
import util.FormatUtils;
import util.HexUtils;
import visualization.BlockView;
import visualization.CbcFlowView;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.nio.charset.StandardCharsets;

/**
 * Module expérimental interactif dédié à AES-CBC (Cipher Block Chaining).
 * Intègre la console de manipulation cryptographique et le moteur vectoriel Graphics2D CbcFlowView.
 */
public class CbcPanel extends JPanel {

    private JTextArea txtPlaintext;
    private JTextField txtKeyHex;
    private JTextField txtIvHex;
    private JComboBox<Integer> cbKeySize;
    private JTextArea txtCiphertextHex;
    private JTextArea txtDecryptedText;
    private JLabel lblEncTime;
    private JLabel lblDecTime;
    private JLabel lblBlockCount;

    private CbcFlowView cbcFlowView;
    private BlockView blockView;

    private final AesCbcService cbcService = new AesCbcService();
    private SecretKey currentKey;
    private byte[] currentIv;
    private byte[] currentCiphertext;

    // Palette institutionnelle
    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color CARD_BORDER = new Color(71, 85, 105);
    private static final Color ACCENT_CYAN = new Color(14, 165, 233);
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    public CbcPanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
        generateNewKeyAndIv();
    }

    private void initUI() {
        // En-tête
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        JLabel title = new JLabel("MODULE EXPÉRIMENTAL : AES-CBC (CIPHER BLOCK CHAINING)");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_LIGHT);
        JLabel desc = new JLabel("Chiffrement par blocs séquentiels avec vecteur d'initialisation (IV) et bourrage PKCS#5.");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(TEXT_MUTED);
        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(desc, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // Zone centrale divisée en 2 : Haut (Visualisation vectorielle Graphics2D), Bas (Console opérationnelle)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // Visualisation Vectorielle avec Contrôleur temporel
        JPanel visualContainer = new JPanel(new BorderLayout(6, 6));
        visualContainer.setOpaque(false);
        visualContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CARD_BORDER), "Visualisation Vectorielle Dynamique (Graphics2D)",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), ACCENT_CYAN));

        cbcFlowView = new CbcFlowView();
        visualContainer.add(cbcFlowView, BorderLayout.CENTER);

        // Barre de contrôle animation
        JPanel animControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        animControls.setOpaque(false);

        JButton btnPrev = createStyledButton("⏮ Étape précédente");
        JButton btnNext = createStyledButton("Étape suivante ⏭");
        JButton btnPlay = createStyledButton("▶ Lecture");
        JButton btnPause = createStyledButton("⏸ Pause");
        JButton btnReset = createStyledButton("↺ Réinitialiser");

        btnPrev.addActionListener(e -> cbcFlowView.previousStep());
        btnNext.addActionListener(e -> cbcFlowView.nextStep());
        btnPlay.addActionListener(e -> cbcFlowView.play());
        btnPause.addActionListener(e -> cbcFlowView.pause());
        btnReset.addActionListener(e -> cbcFlowView.reset());

        animControls.add(btnPrev);
        animControls.add(btnPlay);
        animControls.add(btnPause);
        animControls.add(btnNext);
        animControls.add(btnReset);
        visualContainer.add(animControls, BorderLayout.SOUTH);

        centerPanel.add(visualContainer, BorderLayout.NORTH);

        // Console opérationnelle inférieure
        JPanel consolePanel = new JPanel(new GridLayout(1, 2, 12, 0));
        consolePanel.setOpaque(false);

        // Colonne Gauche : Paramètres et Entrées
        consolePanel.add(buildInputsCard());

        // Colonne Droite : Sorties, Blocs Hex et Métriques
        consolePanel.add(buildOutputsCard());

        centerPanel.add(consolePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildInputsCard() {
        JPanel card = createStyledCard("Paramètres et Données d'Entrée");
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Clé AES et Taille
        gbc.gridx = 0;
        gbc.gridy = 0;
        card.add(createFieldLabel("Taille de Clé :"), gbc);

        gbc.gridx = 1;
        cbKeySize = new JComboBox<>(new Integer[]{128, 192, 256});
        cbKeySize.setBackground(CARD_BG);
        cbKeySize.setForeground(TEXT_LIGHT);
        card.add(cbKeySize, gbc);

        gbc.gridx = 2;
        JButton btnGenKey = createStyledButton("Générer Clé");
        btnGenKey.addActionListener(e -> generateNewKey());
        card.add(btnGenKey, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        card.add(createFieldLabel("Clé (Hex) :"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtKeyHex = new JTextField();
        txtKeyHex.setEditable(false);
        txtKeyHex.setBackground(new Color(20, 28, 44));
        txtKeyHex.setForeground(TEXT_LIGHT);
        card.add(txtKeyHex, gbc);

        // Vecteur d'Initialisation (IV)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        card.add(createFieldLabel("IV (128 bits) :"), gbc);

        gbc.gridx = 1;
        txtIvHex = new JTextField();
        txtIvHex.setEditable(false);
        txtIvHex.setBackground(new Color(20, 28, 44));
        txtIvHex.setForeground(TEXT_LIGHT);
        card.add(txtIvHex, gbc);

        gbc.gridx = 2;
        JButton btnGenIv = createStyledButton("Générer IV");
        btnGenIv.addActionListener(e -> generateNewIv());
        card.add(btnGenIv, gbc);

        // Plaintext
        gbc.gridx = 0;
        gbc.gridy = 3;
        card.add(createFieldLabel("Texte Clair :"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtPlaintext = new JTextArea("PROTOCOLE ACADÉMIQUE DE TEST POUR AES-CBC. MESSAGE CONFIDENTIEL DE NIVEAU MASTER.");
        txtPlaintext.setLineWrap(true);
        txtPlaintext.setWrapStyleWord(true);
        txtPlaintext.setBackground(new Color(20, 28, 44));
        txtPlaintext.setForeground(TEXT_LIGHT);
        txtPlaintext.setCaretColor(TEXT_LIGHT);
        card.add(new JScrollPane(txtPlaintext), gbc);

        // Bouton Chiffrer
        gbc.gridy = 5;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton btnEncrypt = UiUtils.createPrimaryButton("⚡ Chiffrer en AES-CBC");
        btnEncrypt.addActionListener(e -> executeEncryption());
        card.add(btnEncrypt, gbc);

        return card;
    }

    private JPanel buildOutputsCard() {
        JPanel card = createStyledCard("Résultats Cryptographiques et Décomposition");
        card.setLayout(new BorderLayout(6, 6));

        // Panneau supérieur : Métriques de temps
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 6, 6));
        metricsPanel.setOpaque(false);
        lblEncTime = new JLabel("Chiffrement : --", SwingConstants.CENTER);
        lblDecTime = new JLabel("Déchiffrement : --", SwingConstants.CENTER);
        lblBlockCount = new JLabel("Blocs : 0", SwingConstants.CENTER);
        styleMetricLabel(lblEncTime);
        styleMetricLabel(lblDecTime);
        styleMetricLabel(lblBlockCount);
        metricsPanel.add(lblEncTime);
        metricsPanel.add(lblDecTime);
        metricsPanel.add(lblBlockCount);
        card.add(metricsPanel, BorderLayout.NORTH);

        // Centre : Affichage Hexadécimal & Déchiffrement
        JPanel centerGrid = new JPanel(new GridLayout(2, 1, 6, 6));
        centerGrid.setOpaque(false);

        // Ciphertext Hex
        JPanel ctPanel = new JPanel(new BorderLayout(4, 4));
        ctPanel.setOpaque(false);
        JLabel ctLabel = new JLabel("Ciphertext (Hexadécimal formaté par blocs de 16 octets) :");
        ctLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        ctLabel.setForeground(TEXT_LIGHT);
        ctPanel.add(ctLabel, BorderLayout.NORTH);

        txtCiphertextHex = new JTextArea();
        txtCiphertextHex.setEditable(false);
        txtCiphertextHex.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtCiphertextHex.setBackground(new Color(20, 28, 44));
        txtCiphertextHex.setForeground(new Color(16, 185, 129));
        ctPanel.add(new JScrollPane(txtCiphertextHex), BorderLayout.CENTER);
        centerGrid.add(ctPanel);

        // Plaintext déchiffré
        JPanel decPanel = new JPanel(new BorderLayout(4, 4));
        decPanel.setOpaque(false);
        JLabel decLabel = new JLabel("Texte Déchiffré :");
        decLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        decLabel.setForeground(TEXT_LIGHT);
        decPanel.add(decLabel, BorderLayout.NORTH);

        txtDecryptedText = new JTextArea();
        txtDecryptedText.setEditable(false);
        txtDecryptedText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtDecryptedText.setBackground(new Color(20, 28, 44));
        txtDecryptedText.setForeground(TEXT_LIGHT);
        decPanel.add(new JScrollPane(txtDecryptedText), BorderLayout.CENTER);
        centerGrid.add(decPanel);

        card.add(centerGrid, BorderLayout.CENTER);

        // Bouton Déchiffrer
        JButton btnDecrypt = UiUtils.createPrimaryButton("🔓 Déchiffrer le Ciphertext");
        btnDecrypt.addActionListener(e -> executeDecryption());
        card.add(btnDecrypt, BorderLayout.SOUTH);

        return card;
    }

    private void generateNewKey() {
        int bits = (Integer) cbKeySize.getSelectedItem();
        currentKey = CryptoKeyManager.generateKey(bits);
        txtKeyHex.setText(HexUtils.bytesToSpacedHex(currentKey.getEncoded()));
    }

    private void generateNewIv() {
        currentIv = NonceManager.generateCbcIv();
        txtIvHex.setText(HexUtils.bytesToSpacedHex(currentIv));
    }

    private void generateNewKeyAndIv() {
        generateNewKey();
        generateNewIv();
    }

    private void executeEncryption() {
        String pt = txtPlaintext.getText();
        if (pt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un texte clair.", "Saisie vide", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentKey == null) generateNewKey();
        if (currentIv == null) generateNewIv();

        byte[] ptBytes = pt.getBytes(StandardCharsets.UTF_8);
        CryptoResult res = cbcService.encrypt(ptBytes, currentKey, currentIv);

        if (res.isSuccess()) {
            currentCiphertext = res.getCiphertext();
            txtCiphertextHex.setText(HexUtils.formatBlocks(currentCiphertext, 16));
            lblEncTime.setText("Chiffrement : " + FormatUtils.formatDurationNs(res.getEncryptionTimeNs()));
            int blockCount = (int) Math.ceil((double) currentCiphertext.length / 16.0);
            lblBlockCount.setText("Blocs : " + blockCount + " (" + currentCiphertext.length + " o)");
            txtDecryptedText.setText("");

            // Enregistrement de l'expérience
            ExperimentRunner.getInstance().addResult(new ExperimentResult(
                    "EXP-CBC-" + System.currentTimeMillis() % 10000,
                    "AES", "CBC", ptBytes.length, currentKey.getEncoded().length * 8, 16, 0,
                    res.getEncryptionTimeNs(), 0, 0, "NOT_APPLICABLE", "NONE", "SUCCESS",
                    "Chiffrement AES-CBC interactif", "Observation du chaînage des blocs",
                    "Chiffrement séquentiel sous PKCS#5", "Opération conforme", "Chiffrement exécuté sans contrôle d'intégrité."
            ));
        } else {
            JOptionPane.showMessageDialog(this, "Erreur de chiffrement : " + res.getErrorMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeDecryption() {
        if (currentCiphertext == null || currentKey == null || currentIv == null) {
            JOptionPane.showMessageDialog(this, "Veuillez d'abord chiffrer un message.", "Données manquantes", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CryptoResult res = cbcService.decrypt(currentCiphertext, currentKey, currentIv);
        if (res.isSuccess() && res.getPlaintext() != null) {
            txtDecryptedText.setText(new String(res.getPlaintext(), StandardCharsets.UTF_8));
            lblDecTime.setText("Déchiffrement : " + FormatUtils.formatDurationNs(res.getDecryptionTimeNs()));
        } else {
            txtDecryptedText.setText("[ÉCHEC DE DÉCHIFFREMENT : " + res.getErrorMessage() + "]");
        }
    }

    private JPanel createStyledCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CARD_BORDER), title,
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), ACCENT_CYAN));
        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(TEXT_LIGHT);
        return l;
    }

    private JButton createStyledButton(String text) {
        return UiUtils.createDefaultButton(text);
    }

    private void styleMetricLabel(JLabel l) {
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setOpaque(true);
        l.setBackground(new Color(15, 23, 42));
        l.setForeground(ACCENT_CYAN);
        l.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
    }
}
