package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Fenêtre principale du Laboratoire Cryptographique Académique (Master).
 * Structure l'interface selon le découpage institutionnel :
 * HEADER - SIDEBAR DE NAVIGATION - ZONE CENTRALE (CardLayout) - BARRE D'ÉTAT.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);
    private final JLabel lblClock = new JLabel();
    private final JLabel lblStatusMsg = new JLabel("Laboratoire prêt. Toutes primitives Java 17 initialisées.");
    private final Map<String, JButton> navButtons = new HashMap<>();

    // Palette institutionnelle
    private static final Color BG_DARK = new Color(15, 23, 42);         // Slate 900
    private static final Color SIDEBAR_BG = new Color(24, 32, 47);       // Slate 850
    private static final Color HEADER_BG = new Color(18, 26, 40);        // Dark header
    private static final Color STATUS_BG = new Color(12, 18, 30);        // Status bar
    private static final Color BORDER_COLOR = new Color(51, 65, 85);     // Slate 700
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);    // Slate 50
    private static final Color TEXT_MUTED = new Color(148, 163, 184);    // Slate 400
    private static final Color ACCENT_CYAN = new Color(14, 165, 233);    // Cyan 500
    private static final Color BTN_ACTIVE = new Color(30, 41, 59);       // Slate 800

    public MainFrame() {
        super("Laboratoire Cryptographique Expérimental — AES-CBC vs AES-GCM (Master)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 750));
        setPreferredSize(new Dimension(1300, 850));
        setLocationRelativeTo(null);

        initLayout();
        startClockTimer();
    }

    private void initLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        // 1. HEADER INSTITUTIONNEL
        root.add(buildHeader(), BorderLayout.NORTH);

        // 2. SIDEBAR DE NAVIGATION
        root.add(buildSidebar(), BorderLayout.WEST);

        // 3. ZONE CENTRALE (Panneaux modulaires)
        initContentArea();
        root.add(contentArea, BorderLayout.CENTER);

        // 4. BARRE D'ÉTAT INFÉRIEURE
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 16));
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 20, 12, 20)
        ));

        // Titre et écusson universitaire
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel logoBadge = new JLabel(" 🛡 AES-LAB ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(14, 165, 233, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(ACCENT_CYAN);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setFont(new Font("SansSerif", Font.BOLD, 13));
        logoBadge.setForeground(ACCENT_CYAN);
        logoBadge.setBorder(new EmptyBorder(4, 8, 4, 8));
        brandPanel.add(logoBadge);

        JPanel titles = new JPanel(new GridLayout(2, 1, 2, 2));
        titles.setOpaque(false);
        JLabel titleMain = new JLabel("LABORATOIRE CRYPTOGRAPHIQUE ACADÉMIQUE DE NIVEAU MASTER");
        titleMain.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleMain.setForeground(TEXT_LIGHT);

        JLabel titleSub = new JLabel("Banc d'Essai Expérimental et Analyse Comparative des Modes AES-CBC et AES-GCM (AEAD)");
        titleSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        titleSub.setForeground(TEXT_MUTED);

        titles.add(titleMain);
        titles.add(titleSub);
        brandPanel.add(titles);

        header.add(brandPanel, BorderLayout.WEST);

        // Indicateurs côté droit
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightInfo.setOpaque(false);

        JLabel envLabel = new JLabel("JDK 17 LTS Pure Standard | Java JCA");
        envLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        envLabel.setForeground(new Color(16, 185, 129));
        rightInfo.add(envLabel);

        lblClock.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblClock.setForeground(TEXT_MUTED);
        rightInfo.add(lblClock);

        header.add(rightInfo, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // En-tête Sidebar
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        titlePanel.setOpaque(false);
        JLabel navTitle = new JLabel("NAVIGATION RECHERCHE");
        navTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        navTitle.setForeground(TEXT_MUTED);
        titlePanel.add(navTitle);
        sidebar.add(titlePanel, BorderLayout.NORTH);

        // Menu de boutons
        JPanel btnList = new JPanel(new GridLayout(9, 1, 4, 4));
        btnList.setOpaque(false);
        btnList.setBorder(new EmptyBorder(4, 10, 10, 10));

        addNavButton(btnList, "Dashboard", "📊 Tableau de Bord", "DASHBOARD");
        addNavButton(btnList, "AES-CBC", "🔒 Mode AES-CBC", "CBC");
        addNavButton(btnList, "AES-GCM", "🛡 Mode AES-GCM (AEAD)", "GCM");
        addNavButton(btnList, "Attaques", "⚡ Attaques & Résilience", "ATTACK");
        addNavButton(btnList, "Performances", "📈 Benchmark Débit", "PERFORMANCE");
        addNavButton(btnList, "Comparaison", "⚖ Synthèse Comparative", "COMPARISON");
        addNavButton(btnList, "Expériences", "🔬 Mode Recherche", "EXPERIMENTS");
        addNavButton(btnList, "Résultats", "📁 Archives & Export", "RESULTS");
        addNavButton(btnList, "Démonstration Jury", "🎓 Mode Soutenance Jury", "JURY_DEMO");

        sidebar.add(btnList, BorderLayout.CENTER);

        // Pied de sidebar
        JPanel bottomSidebar = new JPanel(new GridLayout(2, 1, 2, 2));
        bottomSidebar.setOpaque(false);
        bottomSidebar.setBorder(new EmptyBorder(10, 16, 14, 16));

        JLabel info1 = new JLabel("Architecture Modulaire");
        info1.setFont(new Font("SansSerif", Font.BOLD, 10));
        info1.setForeground(TEXT_MUTED);

        JLabel info2 = new JLabel("Zero Librairie Externe");
        info2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        info2.setForeground(new Color(16, 185, 129));

        bottomSidebar.add(info1);
        bottomSidebar.add(info2);
        sidebar.add(bottomSidebar, BorderLayout.SOUTH);

        return sidebar;
    }

    private String currentNavKey = "Dashboard";

    private void addNavButton(JPanel container, String key, String text, String cardName) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = key.equals(currentNavKey);
                Color bg = isActive ? BTN_ACTIVE : (getModel().isRollover() ? new Color(38, 50, 70) : SIDEBAR_BG);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                if (isActive) {
                    g2.setColor(ACCENT_CYAN);
                    g2.fillRect(0, 0, 4, getHeight());
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(TEXT_LIGHT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            cardLayout.show(contentArea, cardName);
            currentNavKey = key;
            updateNavButtonStyles(key);
            lblStatusMsg.setText("Module actif : " + text);
        });

        navButtons.put(key, btn);
        container.add(btn);
    }

    private void updateNavButtonStyles(String activeKey) {
        this.currentNavKey = activeKey;
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            if (entry.getKey().equals(activeKey)) {
                b.setForeground(ACCENT_CYAN);
            } else {
                b.setForeground(TEXT_LIGHT);
            }
            b.repaint();
        }
    }

    private void initContentArea() {
        contentArea.add(new DashboardPanel(), "DASHBOARD");
        contentArea.add(new CbcPanel(), "CBC");
        contentArea.add(new GcmPanel(), "GCM");
        contentArea.add(new AttackPanel(), "ATTACK");
        contentArea.add(new PerformancePanel(), "PERFORMANCE");
        contentArea.add(new ComparisonPanel(), "COMPARISON");
        contentArea.add(new ExperimentPanel(), "EXPERIMENTS");
        contentArea.add(new ResultsPanel(), "RESULTS");
        contentArea.add(new JuryDemoPanel(), "JURY_DEMO");

        updateNavButtonStyles("Dashboard");
    }

    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout(10, 0));
        statusBar.setBackground(STATUS_BG);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(6, 16, 6, 16)
        ));

        lblStatusMsg.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblStatusMsg.setForeground(TEXT_MUTED);
        statusBar.add(lblStatusMsg, BorderLayout.WEST);

        JLabel rightTag = new JLabel("Projet Master Cryptographie 2026 | NIST SP 800-38A / NIST SP 800-38D");
        rightTag.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rightTag.setForeground(TEXT_MUTED);
        statusBar.add(rightTag, BorderLayout.EAST);

        return statusBar;
    }

    private void startClockTimer() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lblClock.setText(LocalDateTime.now().format(dtf));
            }
        });
        timer.start();
        lblClock.setText(LocalDateTime.now().format(dtf));
    }
}
