package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Utilitaires d'interface graphique Swing garantissant un rendu vectoriel sans dépendre
 * du thème natif de l'OS (évite les fonds blancs imposés par Windows Look & Feel).
 */
public final class UiUtils {

    public static final Color BG_DARK = new Color(15, 23, 42);         // Slate 900
    public static final Color CARD_BG = new Color(30, 41, 59);          // Slate 800
    public static final Color CARD_BORDER = new Color(71, 85, 105);     // Slate 600
    public static final Color BTN_DEFAULT = new Color(51, 65, 85);      // Slate 700
    public static final Color BTN_PRIMARY = new Color(14, 116, 144);    // Cyan dark
    public static final Color BTN_SUCCESS = new Color(5, 150, 105);     // Emerald dark
    public static final Color BTN_DANGER = new Color(185, 28, 28);      // Red dark
    public static final Color ACCENT_CYAN = new Color(14, 165, 233);    // Cyan 500
    public static final Color ACCENT_EMERALD = new Color(16, 185, 129); // Emerald 500
    public static final Color TEXT_LIGHT = new Color(248, 250, 252);    // Slate 50
    public static final Color TEXT_MUTED = new Color(148, 163, 184);    // Slate 400

    private UiUtils() {
    }

    /**
     * Crée un bouton personnalisé vectoriel haute lisibilité (fond sombre / accent, texte clair net, bordure soignée).
     * Empêche formellement le Look & Feel Windows de dessiner un fond blanc indésirable.
     */
    public static JButton createCustomButton(String text, Color bgColor, Color fgColor) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentBg;
                if (!isEnabled()) {
                    currentBg = new Color(35, 45, 60);
                } else if (getModel().isPressed()) {
                    currentBg = bgColor.darker();
                } else if (getModel().isRollover()) {
                    currentBg = bgColor.brighter();
                } else {
                    currentBg = bgColor;
                }

                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();

                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setForeground(fgColor != null ? fgColor : TEXT_LIGHT);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton createPrimaryButton(String text) {
        return createCustomButton(text, BTN_PRIMARY, TEXT_LIGHT);
    }

    public static JButton createDefaultButton(String text) {
        return createCustomButton(text, BTN_DEFAULT, TEXT_LIGHT);
    }

    public static JButton createSuccessButton(String text) {
        return createCustomButton(text, BTN_SUCCESS, TEXT_LIGHT);
    }

    public static JButton createDangerButton(String text) {
        return createCustomButton(text, BTN_DANGER, TEXT_LIGHT);
    }
}
