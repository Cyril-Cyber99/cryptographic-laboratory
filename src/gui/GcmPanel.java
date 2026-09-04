package gui;

import crypto.AesGcmService;
import crypto.CryptoKeyManager;
import crypto.CryptoResult;
import crypto.NonceManager;
import experiment.ExperimentResult;
import experiment.ExperimentRunner;
import util.FormatUtils;
import util.HexUtils;
import visualization.GcmFlowView;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.nio.charset.StandardCharsets;

/**
 * Module expérimental interactif dédié à AES-GCM (Galois/Counter Mode).
 * Intègre la gestion des données associées (AAD), du Tag d'authentification MAC de 128 bits
 * et le composant vectoriel interactif GcmFlowView.
 */
public class GcmPanel extends JPanel {

    private JTextArea txtPlaintext;
    private JTextField txtAad;
    private JTextField txtKeyHex;
    private JTextField txtNonceHex;
    private JComboBox<Integer> cbKeySize;
    private JTextArea txtCiphertextHex;
    private JTextField txtTagHex;
    private JTextArea txtDecryptedText;
    private JLabel lblAuthStatus;
    private JLabel lblEncTime;
    private JLabel lblDecTime;

    private GcmFlowView gcmFlowView;

    private final AesGcmService gcmService = new AesGcmService();
    private SecretKey currentKey;
    private byte[] currentNonce;
    private byte[] currentCiphertext;
    private byte[] currentTag;

    // Palette institutionnelle
    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color CARD_BORDER = new Color(71, 85, 105);
    private static final Color ACCENT_EMERALD = new Color(16, 185, 129);
    private static final Color ACCENT_CYAN = new Color(14, 165, 233);
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    public GcmPanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
        generateNewKeyAndNonce();
    }

    private void initUI() {
        // En-tête
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        JLabel title = new JLabel("MODULE EXPÉRIMENTAL : AES-GCM (GALOIS/COUNTER MODE - AEAD)");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_LIGHT);
        JLabel desc = new JLabel("Chiffrement authentifié avec données associées (AAD) et vérification par Tag MAC de 128 bits.");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(TEXT_MUTED);
        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(desc, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // Visualisation Vectorielle avec Contrôleur temporel
        JPanel visualContainer = new JPanel(new BorderLayout(6, 6));
        visualContainer.setOpaque(false);
        visualContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CARD_BORDER), "Visualisation Vectorielle Dynamique GCM (Graphics2D)",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), ACCENT_EMERALD));

        gcmFlowView = new GcmFlowView();
        visualContainer.add(gcmFlowView, BorderLayout.CENTER);

        // Barre de contrôle animation
        JPanel animControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        animControls.setOpaque(false);

        JButton btnPrev = createStyledButton("⏮ Étape précédente");
        JButton btnNext = createStyledButton("Étape suivante ⏭");
        JButton btnPlay = createStyledButton("▶ Lecture");
        JButton btnPause = createStyledButton("⏸ Pause");
        JButton btnReset = createStyledButton("↺ Réinitialiser");

        btnPrev.addActionListener(e -> gcmFlowView.previousStep());
        btnNext.addActionListener(e -> gcmFlowView.nextStep());
        btnPlay.addActionListener(e -> gcmFlowView.play());
        btnPause.addActionListener(e -> gcmFlowView.pause());
        btnReset.addActionListener(e -> gcmFlowView.reset());

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

        consolePanel.add(buildInputsCard());
        consolePanel.add(buildOutputsCard());

        centerPanel.add(consolePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildInputsCard() {
        JPanel card = createStyledCard("Paramètres, Nonce et Données d'Entrée");
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Clé AES
        gbc.gridx = 0;
        gbc.gridy = 0;
        card.add(createFieldLabel("Taille Clé :"), gbc);

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

        // Nonce GCM (96 bits)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        card.add(createFieldLabel("Nonce (96b) :"), gbc);

        gbc.gridx = 1;
        txtNonceHex = new JTextField();
        txtNonceHex.setEditable(false);
        txtNonceHex.setBackground(new Color(20, 28, 44));
        txtNonceHex.setForeground(TEXT_LIGHT);
        card.add(txtNonceHex, gbc);

        gbc.gridx = 2;
        JButton btnGenNonce = createStyledButton("Générer Nonce");
        btnGenNonce.addActionListener(e -> generateNewNonce());
        card.add(btnGenNonce, gbc);

        // AAD
        gbc.gridx = 0;
        gbc.gridy = 3;
        card.add(createFieldLabel("AAD (Auth) :"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtAad = new JTextField("USER_ID=4291;CLEARANCE=SECRET;ROLE=ANALYSTE");
        txtAad.setBackground(new Color(20, 28, 44));
        txtAad.setForeground(new Color(168, 85, 247)); // Purple
        card.add(txtAad, gbc);

        // Plaintext
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        card.add(createFieldLabel("Plaintext :"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtPlaintext = new JTextArea("TRANSACTION SÉCURISÉE DE PAIEMENT : MONTANT=25000 EUR; DEVISE=EUR; RECEVEUR=LABORATOIRE_CRYPTO.");
        txtPlaintext.setLineWrap(true);
        txtPlaintext.setWrapStyleWord(true);
        txtPlaintext.setBackground(new Color(20, 28, 44));
        txtPlaintext.setForeground(TEXT_LIGHT);
        card.add(new JScrollPane(txtPlaintext), gbc);

        // Bouton Chiffrer
        gbc.gridy = 6;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton btnEncrypt = UiUtils.createSuccessButton("⚡ Chiffrer en AES-GCM (AEAD)");
        btnEncrypt.addActionListener(e -> executeEncryption());
        card.add(btnEncrypt, gbc);

        return card;
    }

    private JPanel buildOutputsCard() {
        JPanel card = createStyledCard("Sorties Cryptographiques, Tag MAC et Vérification");
        card.setLayout(new BorderLayout(6, 6));

        // Panneau métriques et statut d'authentification
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 6, 6));
        metricsPanel.setOpaque(false);

        lblAuthStatus = new JLabel("Statut : Prêt", SwingConstants.CENTER);
        lblEncTime = new JLabel("Chiffrement : --", SwingConstants.CENTER);
        lblDecTime = new JLabel("Déchiffrement : --", SwingConstants.CENTER);

        styleMetricLabel(lblAuthStatus, ACCENT_EMERALD);
        styleMetricLabel(lblEncTime, ACCENT_CYAN);
        styleMetricLabel(lblDecTime, ACCENT_CYAN);

        metricsPanel.add(lblAuthStatus);
        metricsPanel.add(lblEncTime);
        metricsPanel.add(lblDecTime);
        card.add(metricsPanel, BorderLayout.NORTH);

        // Centre : Ciphertext, Tag MAC et Plaintext Déchiffré
        JPanel centerGrid = new JPanel(new GridLayout(3, 1, 4, 4));
        centerGrid.setOpaque(false);

        // Ciphertext Hex
        JPanel ctPanel = new JPanel(new BorderLayout(2, 2));
        ctPanel.setOpaque(false);
        JLabel ctLabel = new JLabel("Ciphertext (Hexadécimal sans le tag) :");
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

        // Tag MAC Hex (128 bits / 16 octets)
        JPanel tagPanel = new JPanel(new BorderLayout(2, 2));
        tagPanel.setOpaque(false);
        JLabel tagLabel = new JLabel("Tag d'Authentification MAC (128 bits / 16 octets) :");
        tagLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        tagLabel.setForeground(new Color(16, 185, 129));
        tagPanel.add(tagLabel, BorderLayout.NORTH);

        txtTagHex = new JTextField();
        txtTagHex.setEditable(false);
        txtTagHex.setFont(new Font("Monospaced", Font.BOLD, 12));
        txtTagHex.setBackground(new Color(20, 28, 44));
        txtTagHex.setForeground(new Color(16, 185, 129));
        tagPanel.add(txtTagHex, BorderLayout.CENTER);
        centerGrid.add(tagPanel);

        // Déchiffré
        JPanel decPanel = new JPanel(new BorderLayout(2, 2));
        decPanel.setOpaque(false);
        JLabel decLabel = new JLabel("Plaintext Déchiffré et Authentifié :");
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

        // Bouton Déchiffrer & Vérifier
        JButton btnDecrypt = UiUtils.createSuccessButton("🛡 Déchiffrer et Vérifier le Tag MAC");
        btnDecrypt.addActionListener(e -> executeDecryption());
        card.add(btnDecrypt, BorderLayout.SOUTH);

        return card;
    }

    private void generateNewKey() {
        int bits = (Integer) cbKeySize.getSelectedItem();
        currentKey = CryptoKeyManager.generateKey(bits);
        txtKeyHex.setText(HexUtils.bytesToSpacedHex(currentKey.getEncoded()));
    }

    private void generateNewNonce() {
        currentNonce = NonceManager.generateGcmNonce();
        txtNonceHex.setText(HexUtils.bytesToSpacedHex(currentNonce));
    }

    private void generateNewKeyAndNonce() {
        generateNewKey();
        generateNewNonce();
    }

    private void executeEncryption() {
        String pt = txtPlaintext.getText();
        if (pt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un texte clair.", "Saisie vide", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentKey == null) generateNewKey();
        if (currentNonce == null) generateNewNonce();

        byte[] ptBytes = pt.getBytes(StandardCharsets.UTF_8);
        byte[] aadBytes = txtAad.getText().trim().getBytes(StandardCharsets.UTF_8);

        CryptoResult res = gcmService.encrypt(ptBytes, currentKey, currentNonce, aadBytes);

        if (res.isSuccess()) {
            currentCiphertext = res.getCiphertext();
            currentTag = res.getTag();

            txtCiphertextHex.setText(HexUtils.bytesToSpacedHex(currentCiphertext));
            txtTagHex.setText(HexUtils.bytesToSpacedHex(currentTag));
            lblEncTime.setText("Chiffrement : " + FormatUtils.formatDurationNs(res.getEncryptionTimeNs()));
            lblAuthStatus.setText("Tag Émis (128b)");
            txtDecryptedText.setText("");

            // Archivage expérience
            ExperimentRunner.getInstance().addResult(new ExperimentResult(
                    "EXP-GCM-" + System.currentTimeMillis() % 10000,
                    "AES", "GCM", ptBytes.length, currentKey.getEncoded().length * 8, 12, aadBytes.length,
                    res.getEncryptionTimeNs(), 0, 0, "VALID", "NONE", "SUCCESS",
                    "Chiffrement AES-GCM (AEAD)", "Émission conjointe du ciphertext et du tag GHASH",
                    "Génération du tag MAC 128 bits protégeant le clair et l'AAD",
                    "Calculs réalisés", "Chiffrement et authentification réussis."
            ));
        } else {
            JOptionPane.showMessageDialog(this, "Erreur GCM : " + res.getErrorMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeDecryption() {
        if (currentCiphertext == null || currentTag == null || currentKey == null || currentNonce == null) {
            JOptionPane.showMessageDialog(this, "Veuillez d'abord chiffrer un message.", "Données manquantes", JOptionPane.WARNING_MESSAGE);
            return;
        }

        byte[] aadBytes = txtAad.getText().trim().getBytes(StandardCharsets.UTF_8);
        CryptoResult res = gcmService.decrypt(currentCiphertext, currentTag, currentKey, currentNonce, aadBytes);

        if (res.isSuccess() && res.getPlaintext() != null) {
            txtDecryptedText.setText(new String(res.getPlaintext(), StandardCharsets.UTF_8));
            lblDecTime.setText("Déchiffrement : " + FormatUtils.formatDurationNs(res.getDecryptionTimeNs()));
            lblAuthStatus.setText("AUTHENTIFIÉ (VALIDE)");
            lblAuthStatus.setForeground(ACCENT_EMERALD);
        } else {
            txtDecryptedText.setText("[REJET SYSTÈME : " + res.getErrorMessage() + "]");
            lblAuthStatus.setText("AUTHENTICATION FAILED");
            lblAuthStatus.setForeground(new Color(239, 68, 68)); // Red
        }
    }

    private JPanel createStyledCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CARD_BORDER), title,
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), ACCENT_EMERALD));
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

    private void styleMetricLabel(JLabel l, Color fg) {
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setOpaque(true);
        l.setBackground(new Color(15, 23, 42));
        l.setForeground(fg);
        l.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
    }
}
