package gui;

import attack.*;
import experiment.ExperimentResult;
import experiment.ExperimentRunner;
import util.HexUtils;
import visualization.BlockView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Laboratoire interactif d'expériences d'attaques actives contrôlées.
 * Confiné strictement à l'apprentissage académique local.
 *
 * Implémente les 5 scénarios expérimentaux du cahier des charges :
 * 1. Modification du Ciphertext (CBC vs GCM)
 * 2. Modification de l'AAD (GCM)
 * 3. Modification du Tag MAC (GCM)
 * 4. Réutilisation de Nonce (GCM Nonce Reuse / Two-Time Pad)
 * 5. Altération de bit contrôlée (CBC Bit-Flipping)
 */
public class AttackPanel extends JPanel {

    private JComboBox<String> cbAttackType;
    private JTextArea txtAttackDescription;
    private JTextArea txtAttackResults;
    private JButton btnExecuteAttack;
    private JLabel lblResultStatus;
    private BlockView blockView;

    // Démos
    private final CbcBitFlippingDemo cbcBitFlippingDemo = new CbcBitFlippingDemo();
    private final GcmCiphertextTamperingDemo gcmCiphertextTamperingDemo = new GcmCiphertextTamperingDemo();
    private final GcmAadTamperingDemo gcmAadTamperingDemo = new GcmAadTamperingDemo();
    private final GcmTagTamperingDemo gcmTagTamperingDemo = new GcmTagTamperingDemo();
    private final GcmNonceReuseDemo gcmNonceReuseDemo = new GcmNonceReuseDemo();

    // Palette institutionnelle
    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color CARD_BORDER = new Color(71, 85, 105);
    private static final Color ACCENT_CYAN = new Color(14, 165, 233);
    private static final Color ACCENT_AMBER = new Color(245, 158, 11);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color ACCENT_EMERALD = new Color(16, 185, 129);
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    public AttackPanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
        updateAttackSelection();
    }

    private void initUI() {
        // En-tête
        JPanel northPanel = new JPanel(new BorderLayout(4, 4));
        northPanel.setOpaque(false);

        JLabel title = new JLabel("LABORATOIRE D'ATTAQUES ACTIVES & ANALYSE DE RÉSILIENCE");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_LIGHT);

        JLabel desc = new JLabel("Expériences locales contrôlées évaluant l'intégrité, la malléabilité et la détection d'altération active.");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(TEXT_MUTED);

        JLabel warningLabel = new JLabel("⚠ CADRE ACADÉMIQUE STRICT : Expériences confinées à l'évaluation en laboratoire local. Ne constitue pas un outil offensif.");
        warningLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        warningLabel.setForeground(ACCENT_AMBER);

        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(desc, BorderLayout.CENTER);
        northPanel.add(warningLabel, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // Corps divisé : Gauche (Configuration & Déclenchement), Droite (Visualisation vectorielle & Diagnostic)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        centerPanel.setOpaque(false);

        centerPanel.add(buildControlCard());
        centerPanel.add(buildResultCard());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildControlCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CARD_BORDER), "Protocole Expérimental d'Attaque",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), ACCENT_CYAN));

        // Sélection de l'attaque
        JPanel selPanel = new JPanel(new BorderLayout(6, 6));
        selPanel.setOpaque(false);
        JLabel selLbl = new JLabel("Sélectionner l'expérience d'attaque :");
        selLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        selLbl.setForeground(TEXT_LIGHT);
        selPanel.add(selLbl, BorderLayout.NORTH);

        cbAttackType = new JComboBox<>(new String[]{
                "1. Altération Ciphertext : CBC (Inaperçu) vs GCM (Rejet)",
                "2. Altération AAD : Invalidation du Tag GHASH (GCM)",
                "3. Altération Tag : Falsification du MAC 128 bits (GCM)",
                "4. CBC Bit-Flipping : Altération ciblée du clair P[i]",
                "5. Réutilisation de Nonce (GCM Two-Time Pad)"
        });
        cbAttackType.setBackground(CARD_BG);
        cbAttackType.setForeground(TEXT_LIGHT);
        cbAttackType.setFont(new Font("SansSerif", Font.BOLD, 11));
        cbAttackType.addActionListener(e -> updateAttackSelection());
        selPanel.add(cbAttackType, BorderLayout.CENTER);
        card.add(selPanel, BorderLayout.NORTH);

        // Zone descriptive
        JPanel descPanel = new JPanel(new BorderLayout(4, 4));
        descPanel.setOpaque(false);
        JLabel dLbl = new JLabel("Fondements théoriques et modèle de menace :");
        dLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        dLbl.setForeground(TEXT_MUTED);
        descPanel.add(dLbl, BorderLayout.NORTH);

        txtAttackDescription = new JTextArea();
        txtAttackDescription.setEditable(false);
        txtAttackDescription.setLineWrap(true);
        txtAttackDescription.setWrapStyleWord(true);
        txtAttackDescription.setFont(new Font("SansSerif", Font.PLAIN, 11));
        txtAttackDescription.setBackground(new Color(20, 28, 44));
        txtAttackDescription.setForeground(TEXT_LIGHT);
        txtAttackDescription.setBorder(new EmptyBorder(8, 8, 8, 8));
        descPanel.add(new JScrollPane(txtAttackDescription), BorderLayout.CENTER);
        card.add(descPanel, BorderLayout.CENTER);

        // Bouton d'exécution
        btnExecuteAttack = UiUtils.createDangerButton("▶ Lancer l'Expérience d'Attaque Contrôlée");
        btnExecuteAttack.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnExecuteAttack.addActionListener(e -> runSelectedAttack());
        card.add(btnExecuteAttack, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildResultCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CARD_BORDER), "Mesures & Verdict Scientifique",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), ACCENT_EMERALD));

        // Statut en haut
        JPanel topStatus = new JPanel(new BorderLayout(6, 6));
        topStatus.setOpaque(false);
        lblResultStatus = new JLabel("En attente d'exécution", SwingConstants.CENTER);
        lblResultStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblResultStatus.setOpaque(true);
        lblResultStatus.setBackground(new Color(15, 23, 42));
        lblResultStatus.setForeground(ACCENT_CYAN);
        lblResultStatus.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        topStatus.add(lblResultStatus, BorderLayout.NORTH);

        // Visualisation vectorielle du bloc
        blockView = new BlockView();
        topStatus.add(blockView, BorderLayout.CENTER);
        card.add(topStatus, BorderLayout.NORTH);

        // Console détaillée
        txtAttackResults = new JTextArea();
        txtAttackResults.setEditable(false);
        txtAttackResults.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtAttackResults.setBackground(new Color(20, 28, 44));
        txtAttackResults.setForeground(new Color(226, 232, 240));
        txtAttackResults.setBorder(new EmptyBorder(8, 8, 8, 8));
        card.add(new JScrollPane(txtAttackResults), BorderLayout.CENTER);

        return card;
    }

    private void updateAttackSelection() {
        int idx = cbAttackType.getSelectedIndex();
        switch (idx) {
            case 0:
                txtAttackDescription.setText(
                        "EXPÉRIENCE 1 : MODIFICATION DU CIPHERTEXT\n\n" +
                                "Hypothèse : Modifier un octet chiffré dans CBC altère aveuglément le déchiffrement sans alerte, " +
                                "alors que GCM intercepte immédiatement l'anomalie grâce à son tag GHASH.\n\n" +
                                "Protocole : Chiffrement d'un texte, inversion d'un octet de ciphertext, tentative de déchiffrement."
                );
                break;
            case 1:
                txtAttackDescription.setText(
                        "EXPÉRIENCE 2 : MODIFICATION DE L'AAD (DONNÉES ASSOCIÉES GCM)\n\n" +
                                "Hypothèse : L'AAD n'est pas chiffrée (lisible en clair) mais authentifiée. Altérer l'AAD (ex: passer de USER=ADMIN à USER=GUEST) " +
                                "modifie le polynôme GHASH et provoque un échec d'authentification.\n\n" +
                                "Protocole : Émission avec AAD d'origine, falsification de l'AAD au déchiffrement, vérification du rejet."
                );
                break;
            case 2:
                txtAttackDescription.setText(
                        "EXPÉRIENCE 3 : MODIFICATION DU TAG D'AUTHENTIFICATION MAC (GCM)\n\n" +
                                "Hypothèse : Le tag MAC de 128 bits garantit l'intégrité par comparaison en temps constant. " +
                                "Inverser ne serait-ce qu'un seul bit dans ce tag entraîne le rejet catégorique (probabilité de forger = 2^-128).\n\n" +
                                "Protocole : Altération d'un octet du tag MAC, tentative de déchiffrement, interception d'AEADBadTagException."
                );
                break;
            case 3:
                txtAttackDescription.setText(
                        "EXPÉRIENCE 4 : CBC BIT-FLIPPING (RETOURNEMENT DE BITS CONTRÔLÉ)\n\n" +
                                "Hypothèse : Dans CBC, P[i] = AES_Inv(C[i]) XOR C[i-1]. L'attaquant modifie C[i-1] pour forcer " +
                                "une valeur précise dans P[i], démontrant l'absence totale d'intégrité dans CBC seul.\n\n" +
                                "Protocole : Modification calculée dans C[0], déchiffrement, observation de la corruption de P[0] et du changement ciblé dans P[1]."
                );
                break;
            case 4:
                txtAttackDescription.setText(
                        "EXPÉRIENCE 5 : RÉUTILISATION DE NONCE EN AES-GCM (SIMULATION PÉDAGOGIQUE)\n\n" +
                                "AVERTISSEMENT : SIMULATION PÉDAGOGIQUE — NE PAS REPRODUIRE EN PRODUCTION.\n\n" +
                                "Hypothèse : Réutiliser le même Nonce avec la même clé en mode CTR produit le même Keystream S. " +
                                "L'attaquant calcule : C1 XOR C2 = (P1 XOR S) XOR (P2 XOR S) = P1 XOR P2.\n\n" +
                                "Protocole : Chiffrement de deux messages distincts avec (K, Nonce) identiques, vérification de l'égalité P1 XOR P2."
                );
                break;
        }
        lblResultStatus.setText("Prêt pour : " + cbAttackType.getSelectedItem());
        lblResultStatus.setForeground(ACCENT_CYAN);
    }

    private void runSelectedAttack() {
        int idx = cbAttackType.getSelectedIndex();
        switch (idx) {
            case 0:
                runCiphertextTampering();
                break;
            case 1:
                runAadTampering();
                break;
            case 2:
                runTagTampering();
                break;
            case 3:
                runBitFlipping();
                break;
            case 4:
                runNonceReuse();
                break;
        }
    }

    private void runCiphertextTampering() {
        GcmCiphertextTamperingDemo.TamperingResult res = gcmCiphertextTamperingDemo.executeDemo(null, 4);
        blockView.setData(res.tamperedCiphertext);
        blockView.setCorruptedByteIndex(res.tamperedByteIndex);

        lblResultStatus.setText("VERDICT GCM : " + res.status);
        lblResultStatus.setForeground(res.rejected ? ACCENT_EMERALD : ACCENT_RED);
        txtAttackResults.setText(res.technicalDetails);

        ExperimentRunner.getInstance().addResult(new ExperimentResult(
                "ATK-GCM-TAMPER-" + System.currentTimeMillis() % 10000,
                "AES", "GCM", res.originalCiphertext.length, 128, 12, 0,
                0, 0, 0, "REJECTED", "CIPHERTEXT_TAMPERING", res.rejected ? "DETECTED & REJECTED" : "ACCEPTED",
                "Altération Ciphertext GCM", "Vérifier le rejet immédiat", "GHASH détecte toute falsification",
                "Exception AEADBadTagException levée", "GCM garantit l'intégrité stricte du ciphertext."
        ));
    }

    private void runAadTampering() {
        GcmAadTamperingDemo.AadTamperingResult res = gcmAadTamperingDemo.executeDemo("USER=ADMIN", "USER=GUEST", null);
        blockView.setData(res.ciphertext);
        blockView.setCorruptedByteIndex(-1);

        lblResultStatus.setText("VERDICT GCM : " + res.status);
        lblResultStatus.setForeground(res.rejected ? ACCENT_EMERALD : ACCENT_RED);
        txtAttackResults.setText(res.technicalDetails);

        ExperimentRunner.getInstance().addResult(new ExperimentResult(
                "ATK-GCM-AAD-" + System.currentTimeMillis() % 10000,
                "AES", "GCM", res.ciphertext.length, 128, 12, res.originalAad.length(),
                0, 0, 0, "REJECTED", "AAD_TAMPERING", res.rejected ? "DETECTED & REJECTED" : "ACCEPTED",
                "Altération AAD GCM", "Vérifier l'authenticité des données associées",
                "L'AAD est protégée par GHASH", "Données rejetées", "L'AAD n'est pas chiffrée, mais elle est authentifiée."
        ));
    }

    private void runTagTampering() {
        GcmTagTamperingDemo.TagTamperingResult res = gcmTagTamperingDemo.executeDemo(null, 2);
        blockView.setData(res.tamperedTag);
        blockView.setCorruptedByteIndex(res.modifiedByteIndex);

        lblResultStatus.setText("VERDICT GCM : " + res.status);
        lblResultStatus.setForeground(res.rejected ? ACCENT_EMERALD : ACCENT_RED);
        txtAttackResults.setText(res.technicalDetails);

        ExperimentRunner.getInstance().addResult(new ExperimentResult(
                "ATK-GCM-TAG-" + System.currentTimeMillis() % 10000,
                "AES", "GCM", 32, 128, 12, 0,
                0, 0, 0, "REJECTED", "TAG_TAMPERING", res.rejected ? "DETECTED & REJECTED" : "ACCEPTED",
                "Altération Tag MAC GCM", "Vérifier la détection d'un tag falsifié",
                "Inversion d'un bit dans le MAC", "Rejet immédiat", "Falsification impossible sans la clé secrète."
        ));
    }

    private void runBitFlipping() {
        CbcBitFlippingDemo.BitFlippingResult res = cbcBitFlippingDemo.executeDemo(null);
        blockView.setData(res.tamperedCiphertext);
        blockView.setCorruptedByteIndex(res.byteIndexModified);

        lblResultStatus.setText("VERDICT CBC : FALSIFICATION NON DÉTECTÉE (SUCCÈS DE L'ATTAQUE)");
        lblResultStatus.setForeground(ACCENT_RED);
        txtAttackResults.setText(res.technicalExplanation + "\n\n" +
                "Clair d'origine : \"" + res.originalPlaintext + "\"\n" +
                "Clair déchiffré : \"" + res.tamperedPlaintext + "\"");

        ExperimentRunner.getInstance().addResult(new ExperimentResult(
                "ATK-CBC-FLIP-" + System.currentTimeMillis() % 10000,
                "AES", "CBC", 32, 128, 16, 0,
                0, 0, 0, "NOT_APPLICABLE", "BIT_FLIPPING", "UNDETECTED (FALSIFICATION RÉUSSIE)",
                "CBC Bit-Flipping", "Démontrer la malléabilité de CBC",
                "CBC sans HMAC est malléable", "P[i] altéré sans lever d'erreur", "AES-CBC seul ne garantit aucune intégrité."
        ));
    }

    private void runNonceReuse() {
        GcmNonceReuseDemo.NonceReuseResult res = gcmNonceReuseDemo.executeDemo(null, null);
        blockView.setData(res.xorCiphertexts);
        blockView.setCorruptedByteIndex(-1);

        lblResultStatus.setText("SIMULATION GCM : VULNÉRABILITÉ NONCE REUSE CONFIRMÉE");
        lblResultStatus.setForeground(ACCENT_AMBER);
        txtAttackResults.setText(res.mathematicalExplanation);

        ExperimentRunner.getInstance().addResult(new ExperimentResult(
                "ATK-GCM-REUSE-" + System.currentTimeMillis() % 10000,
                "AES", "GCM", res.ciphertext1.length, 128, 12, 0,
                0, 0, 0, "COMPROMISED", "NONCE_REUSE", res.xorMatches ? "VULNERABILITY_CONFIRMED" : "SECURE",
                "Simulation Réutilisation Nonce GCM", "Démontrer mathématiquement le risque Two-Time Pad",
                "C1 ^ C2 == P1 ^ P2", "Identité vérifiée", "La réutilisation d'un nonce détruit la sécurité de GCM."
        ));
    }
}
