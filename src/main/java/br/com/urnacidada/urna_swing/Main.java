package br.com.urnacidada.urna_swing;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Usa o visual padrão do Swing caso não esteja disponível.
            }
            new UrnaFrame().setVisible(true);
        });
    }
}
