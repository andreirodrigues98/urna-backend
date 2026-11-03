
package br.com.urnacidada.urna_swing;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            // JANELA PRINCIPAL
            JFrame janela = new JFrame("Urna Cidadã");

            janela.setSize(900, 600);
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setLocationRelativeTo(null);

            // PAINEL PRINCIPAL
            JPanel painelPrincipal = new JPanel();

            painelPrincipal.setLayout(
                new BorderLayout(10, 10)
            );

            painelPrincipal.setBackground(
                new Color(190, 190, 190)
            );

            painelPrincipal.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            );

            // TELA DE VOTAÇÃO
            JPanel tela = new JPanel();

            tela.setLayout(new BorderLayout());

            tela.setBackground(
                new Color(235, 235, 235)
            );

            JLabel textoTela = new JLabel(
                "TELA DE VOTAÇÃO",
                JLabel.CENTER
            );

            textoTela.setFont(
                new Font("Arial", Font.BOLD, 24)
            );

            tela.add(textoTela, BorderLayout.CENTER);

            // PAINEL DO TECLADO
            JPanel teclado = new JPanel();

            teclado.setLayout(new BorderLayout());

            teclado.setBackground(
                new Color(45, 45, 45)
            );

            teclado.setPreferredSize(
                new Dimension(300, 0)
            );

            JLabel textoTeclado = new JLabel(
                "TECLADO",
                JLabel.CENTER
            );

            textoTeclado.setForeground(Color.WHITE);

            textoTeclado.setFont(
                new Font("Arial", Font.BOLD, 22)
            );

            teclado.add(textoTeclado, BorderLayout.CENTER);

            // ADICIONAR PAINÉIS
            painelPrincipal.add(
                tela,
                BorderLayout.CENTER
            );

            painelPrincipal.add(
                teclado,
                BorderLayout.EAST
            );

            // ADICIONAR À JANELA
            janela.setContentPane(painelPrincipal);

            // EXIBIR JANELA
            janela.setVisible(true);

        });
    }
}
