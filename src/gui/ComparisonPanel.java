package gui;

import visualization.ComparisonView;
import visualization.SecurityMatrixView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Panneau comparatif général structuré selon les 4 axes académiques :
 * 7.1 Comparaison du niveau de sécurité
 * 7.2 Comparaison face aux attaques actives
 * 7.3 Comparaison des performances mesurées
 * 7.4 Avantages, limites et conditions d'utilisation
 */
public class ComparisonPanel extends JPanel {

    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color CARD_BORDER = new Color(71, 85, 105);
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color ACCENT_CYAN = new Color(14, 165, 233);
    private static final Color ACCENT_EMERALD = new Color(16, 185, 129);

    public ComparisonPanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
    }

    private void initUI() {
        JPanel northPanel = new JPanel(new BorderLayout(4, 4));
        northPanel.setOpaque(false);
        JLabel title = new JLabel("SYNTHÈSE COMPARATIVE APPROFONDIE : AES-CBC VS AES-GCM");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_LIGHT);
        JLabel desc = new JLabel("Évaluation intégrale des garanties formelles, de la résilience active et des profils d'application.");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(TEXT_MUTED);
        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(desc, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(CARD_BG);
        tabbedPane.setForeground(TEXT_LIGHT);
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 11));

        // Onglet 1 : Matrice de Sécurité (7.1)
        tabbedPane.addTab("7.1 Niveau de Sécurité (Matrice Vectorielle)", buildSection1());

        // Onglet 2 : Attaques Actives (7.2)
        tabbedPane.addTab("7.2 Résilience aux Attaques Actives", buildSection2());

        // Onglet 3 : Architecture Vectorielle Côte-à-Côte
        tabbedPane.addTab("7.3 Face-à-Face Architectural", new ComparisonView());

        // Onglet 4 : Avantages, Limites & Recommandations (7.4)
        tabbedPane.addTab("7.4 Avantages & Conditions d'Usage", buildSection4());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildSection1() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.add(new SecurityMatrixView(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSection2() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 14, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Panneau CBC Attaques
        JPanel cbcCard = createReportCard("COMPORTEMENT D'AES-CBC FACE AUX ATTAQUES", ACCENT_CYAN, new String[]{
                "1. Altération de Ciphertext :",
                "   -> Déchiffre aveuglément sans avertissement ni rejet.",
                "   -> Les octets altérés corrompent le bloc courant et modifient le suivant.",
                "",
                "2. Attaque Bit-Flipping :",
                "   -> VULNÉRABILITÉ MAJEURE : Inverser un bit dans C[i-1] inverse exactement",
                "      le même bit dans P[i] après déchiffrement.",
                "   -> Permet d'altérer des commandes métier (ex: ADMIN=FALSE -> TRUE).",
                "",
                "3. Attaques par Oracle de Padding (Vaudenay) :",
                "   -> Si le serveur distingue les erreurs de déchiffrement des erreurs",
                "      de padding PKCS#5, le texte clair est intégralement extrait en O(N)."
        });

        // Panneau GCM Attaques
        JPanel gcmCard = createReportCard("COMPORTEMENT D'AES-GCM FACE AUX ATTAQUES", ACCENT_EMERALD, new String[]{
                "1. Altération de Ciphertext :",
                "   -> Détection immédiate par le multiplicateur GHASH dans GF(2^128).",
                "   -> Rejet catégorique (AEADBadTagException) avant toute transmission au clair.",
                "",
                "2. Altération des Données Associées (AAD) :",
                "   -> Bien que transmise en clair, toute modification de l'AAD",
                "      invalide le Tag MAC et provoque l'échec d'authentification.",
                "",
                "3. Falsification de Tag MAC :",
                "   -> Probabilité de forgeage théorique : 2^(-128) (impossible sans clé).",
                "",
                "4. VULNÉRABILITÉ CRITIQUE : RÉUTILISATION DE NONCE",
                "   -> Deux clairs sous le même Nonce permettent de calculer P1 XOR P2.",
                "   -> Permet la dérivation directe de la clé d'authentification H (Joux, 2006)."
        });

        panel.add(cbcCard);
        panel.add(gcmCard);
        return panel;
    }

    private JPanel buildSection4() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 14, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Panneau Recommandations CBC
        JPanel cbcUsage = createReportCard("RECOMMANDATIONS D'USAGE : AES-CBC", ACCENT_CYAN, new String[]{
                "AVANTAGES :",
                "• Simplicité conceptuelle et antériorité dans les standards historiques.",
                "• Déchiffrement parallélisable.",
                "",
                "LIMITES :",
                "• Chiffrement strictement séquentiel (non parallélisable).",
                "• Nécessite un bourrage (surcoût de données et vecteurs d'oracles).",
                "• AUCUNE AUTHENTIFICATION : Doit impérativement être combiné à un HMAC.",
                "",
                "CONDITIONS D'UTILISATION NORMÉES :",
                "• Architecture 'Encrypt-then-MAC' obligatoire.",
                "• Ne plus utiliser dans les protocoles réseau modernes (banni de TLS 1.3)."
        });

        // Panneau Recommandations GCM
        JPanel gcmUsage = createReportCard("RECOMMANDATIONS D'USAGE : AES-GCM", ACCENT_EMERALD, new String[]{
                "AVANTAGES :",
                "• Standard de référence mondial AEAD (recommandé dans TLS 1.3, SSH, IPsec).",
                "• Chiffrement ET déchiffrement parallélisables (hautes performances).",
                "• Zéro padding (longueur ciphertext = longueur clair).",
                "• Intégrité et authenticité garanties simultanément.",
                "",
                "LIMITES :",
                "• Complexité algorithmique d'implémentation (arithmétique de Galois).",
                "• Tolérance zéro à la réutilisation de Nonce (perte critique de sécurité).",
                "",
                "CONDITIONS D'UTILISATION NORMÉES :",
                "• Utilisation d'un Nonce de 96 bits strictement unique par chiffrement.",
                "• Renouveler impérativement la clé avant d'atteindre 2^32 invocations."
        });

        panel.add(cbcUsage);
        panel.add(gcmUsage);
        return panel;
    }

    private JPanel createReportCard(String title, Color accent, String[] lines) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CARD_BORDER), title,
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), accent));

        JTextArea txt = new JTextArea();
        txt.setEditable(false);
        txt.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txt.setBackground(new Color(20, 28, 44));
        txt.setForeground(TEXT_LIGHT);
        txt.setBorder(new EmptyBorder(8, 8, 8, 8));

        StringBuilder sb = new StringBuilder();
        for (String l : lines) {
            sb.append(l).append("\n");
        }
        txt.setText(sb.toString());

        card.add(new JScrollPane(txt), BorderLayout.CENTER);
        return card;
    }
}
