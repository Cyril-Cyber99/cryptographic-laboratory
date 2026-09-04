package app;

import gui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Point d'entrée principal du Laboratoire Expérimental AES-CBC vs AES-GCM.
 * Initialise l'environnement Swing, le rendu vectoriel haute résolution et déploie le MainFrame.
 */
public class Main {

    public static void main(String[] args) {
        // Optimisations du rendu graphique pour affichage haute résolution et vectoriel
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Tentative d'application du Look & Feel Système standard
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fallback gracieux sur le rendu Java SE par défaut
            }

            // Personnalisation des thèmes par défaut pour cohérence avec la charte sombre
            UIManager.put("ToolTip.background", new Color(30, 41, 59));
            UIManager.put("ToolTip.foreground", new Color(248, 250, 252));
            UIManager.put("ToolTip.border", BorderFactory.createLineBorder(new Color(71, 85, 105)));
            UIManager.put("ComboBox.background", new Color(30, 41, 59));
            UIManager.put("ComboBox.foreground", new Color(248, 250, 252));
            UIManager.put("ComboBox.selectionBackground", new Color(14, 165, 233));
            UIManager.put("ComboBox.selectionForeground", Color.WHITE);
            UIManager.put("Button.background", new Color(51, 65, 85));
            UIManager.put("Button.foreground", new Color(248, 250, 252));

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
