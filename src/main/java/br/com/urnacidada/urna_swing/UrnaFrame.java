package br.com.urnacidada.urna_swing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.util.Arrays;
import javax.swing.Timer;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/** Urna fictícia com visual inspirado no formato de urnas eleitorais, sem marcas oficiais. */
public class UrnaFrame extends JFrame {

    private static final Color CASCO = new Color(221, 220, 215);
    private static final Color CASCO_ESCURO = new Color(187, 187, 183);
    private static final Color TELA = new Color(250, 250, 248);
    private static final Color LETRA = new Color(28, 32, 36);
    private static final Color PAINEL_TECLADO = new Color(39, 40, 42);
    private static final Color TECLA = new Color(71, 72, 74);
    private static final Color BRANCO = new Color(249, 249, 247);
    private static final Color LARANJA = new Color(243, 161, 52);
    private static final Color VERDE = new Color(103, 194, 142);

    private final UrnaApiClient api = new UrnaApiClient();
    private final PreviaCandidatoCache catalogo = new PreviaCandidatoCache();
    private final List<JButton> teclas = new ArrayList<>();
    private final JLabel cargoLabel = texto("PRONTO PARA INICIAR", 29, Font.BOLD);
    private final JLabel nomeLabel = texto("VotoVerso", 26, Font.BOLD);
    private final JLabel partidoLabel = texto("Eleição 100% fictícia", 19, Font.PLAIN);
    private final JLabel statusVotoLabel = texto("", 24, Font.BOLD);
    private final JLabel viceLabel = texto("", 19, Font.BOLD);
    private final JLabel instrucoesLabel = texto("Pressione INICIAR SIMULAÇÃO para começar.", 15, Font.PLAIN);
    private final JLabel statusBarra = texto("SIMULADOR EDUCATIVO • AGUARDANDO INÍCIO", 14, Font.PLAIN);
    private final JLabel[] digitos = new JLabel[4];
    private final PainelRetrato retrato = new PainelRetrato();
    private final PainelRetrato retratoVice = new PainelRetrato();
    private final JPanel painelCampos = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 7, 4));
    private final JPanel dadosCandidato = new JPanel();
    private final JPanel centroTela = new JPanel(new BorderLayout());
    private JPanel molduraTela;
    private JPanel topoTela;
    private JPanel rodapeTela;
    private final JPanel painelFim = new JPanel(new BorderLayout());
    private final JPanel painelGravando = new JPanel(new BorderLayout());
    private final JProgressBar progressoGravacao = new JProgressBar(0, 100);
    private final JButton iniciar = botao("INICIAR SIMULAÇÃO", new Color(62, 79, 88), Color.WHITE, 15);
    private final JButton controleEleicao = botao("ZERÉSIMA / APURAÇÃO",
        new Color(83, 91, 95), Color.WHITE, 13);
    private final JButton zerarTestes = botao("ZERAR TESTES",
        new Color(161, 70, 55), Color.WHITE, 13);
    private final JPanel painelZeresima = new JPanel(new BorderLayout());
    private final JPanel painelApuracao = new JPanel(new BorderLayout());
    private final JTextArea textoZeresima = new JTextArea();
    private final JTextArea textoApuracao = new JTextArea();
    private final JPanel painelFotosVencedores = new JPanel(new BorderLayout());
    private String estadoEleicao = "CARREGANDO";
    private long totalVotosEleicao = 0L;
    private long rodadaAtual = 0L;
    private boolean mostrandoZeresima = false;
    private boolean mostrandoApuracao = false;
    private final JButton brancoButton = botao("BRANCO", BRANCO, LETRA, 15);
    private final JButton corrigeButton = botao("CORRIGE", LARANJA, LETRA, 15);
    private final JButton confirmaButton = botao("CONFIRMA", VERDE, LETRA, 15);

    private String sessaoId;
    private String etapa = "INICIO";
    private String numeroDigitado = "";
    private boolean branco = false;
    private boolean ocupado = false;
    private boolean mostrandoGravacao = false;
    private Map<String, Object> previa;

    public UrnaFrame() {
        super("VotoVerso — O Multiverso Vai às Urnas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1160, 730);
        setMinimumSize(new Dimension(990, 640));
        setLocationRelativeTo(null);
        setContentPane(montarInterface());
        configurarEventos();
        atualizarTela();
        carregarEstadoEleicao();
        carregarCatalogoCandidatos();
    }

    private JPanel montarInterface() {
        JPanel corpo = new PainelCasco(new BorderLayout(13, 14));
        corpo.setBorder(new EmptyBorder(20, 24, 16, 24));

        JPanel cabecalho = new JPanel(new BorderLayout(15, 0));
        cabecalho.setOpaque(false);
        cabecalho.setPreferredSize(new Dimension(0, 70));

        JPanel identidade = new JPanel();
        identidade.setOpaque(false);
        identidade.setLayout(new BoxLayout(identidade, BoxLayout.Y_AXIS));
        JLabel marca = texto("VOTOVERSO", 25, Font.BOLD);
        JLabel subtitulo = texto("O MULTIVERSO VAI ÀS URNAS • MARVEL × DC", 12, Font.BOLD);
        subtitulo.setForeground(new Color(83, 86, 88));
        identidade.add(marca);
        identidade.add(Box.createVerticalStrut(3));
        identidade.add(subtitulo);
        cabecalho.add(identidade, BorderLayout.WEST);

        JPanel ladoIniciar = new JPanel(new BorderLayout());
        ladoIniciar.setOpaque(false);
        ladoIniciar.setBorder(new EmptyBorder(6, 0, 6, 0));
        iniciar.setPreferredSize(new Dimension(192, 54));
        controleEleicao.setPreferredSize(new Dimension(198, 54));
        zerarTestes.setPreferredSize(new Dimension(148, 54));
        JPanel controles = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        controles.setOpaque(false);
        controles.add(controleEleicao);
        controles.add(zerarTestes);
        controles.add(iniciar);
        ladoIniciar.add(controles, BorderLayout.CENTER);
        cabecalho.add(ladoIniciar, BorderLayout.EAST);
        corpo.add(cabecalho, BorderLayout.NORTH);

        JPanel maquinas = new JPanel(new BorderLayout(18, 0));
        maquinas.setOpaque(false);
        maquinas.add(montarTela(), BorderLayout.CENTER);
        maquinas.add(montarTeclado(), BorderLayout.EAST);
        corpo.add(maquinas, BorderLayout.CENTER);

        statusBarra.setForeground(new Color(45, 50, 54));
        statusBarra.setHorizontalAlignment(SwingConstants.CENTER);
        corpo.add(statusBarra, BorderLayout.SOUTH);
        return corpo;
    }

    private JPanel montarTela() {
        JPanel moldura = new JPanel(new BorderLayout());
        molduraTela = moldura;
        moldura.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(148, 150, 151), 4),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(238, 239, 238), 3),
                new EmptyBorder(16, 22, 16, 22)
            )
        ));
        moldura.setBackground(TELA);

        JPanel topo = new JPanel();
        topoTela = topo;
        topo.setOpaque(false);
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
        JLabel treinamento = texto("TREINAMENTO • VOTO SIMULADO", 13, Font.BOLD);
        treinamento.setAlignmentX(CENTER_ALIGNMENT);
        treinamento.setForeground(new Color(103, 104, 105));
        topo.add(treinamento);
        topo.add(Box.createVerticalStrut(13));

        JLabel titulo = texto("SEU VOTO SIMULADO PARA", 16, Font.BOLD);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        topo.add(titulo);
        topo.add(Box.createVerticalStrut(6));
        cargoLabel.setAlignmentX(LEFT_ALIGNMENT);
        topo.add(cargoLabel);
        topo.add(Box.createVerticalStrut(16));

        painelCampos.setOpaque(false);
        painelCampos.setAlignmentX(LEFT_ALIGNMENT);
        for (int i = 0; i < digitos.length; i++) {
            JLabel campo = texto("", 36, Font.BOLD);
            campo.setHorizontalAlignment(SwingConstants.CENTER);
            campo.setVerticalAlignment(SwingConstants.CENTER);
            campo.setOpaque(true);
            campo.setBackground(Color.WHITE);
            campo.setBorder(BorderFactory.createLineBorder(new Color(58, 60, 62), 2));
            campo.setPreferredSize(new Dimension(54, 64));
            digitos[i] = campo;
            painelCampos.add(campo);
        }
        topo.add(painelCampos);
        topo.add(Box.createVerticalStrut(13));
        moldura.add(topo, BorderLayout.NORTH);

        centroTela.setOpaque(false);
        dadosCandidato.setOpaque(false);
        dadosCandidato.setBorder(new EmptyBorder(26, 0, 0, 6));
        dadosCandidato.setLayout(new BoxLayout(dadosCandidato, BoxLayout.Y_AXIS));
        nomeLabel.setAlignmentX(LEFT_ALIGNMENT);
        partidoLabel.setAlignmentX(LEFT_ALIGNMENT);
        viceLabel.setAlignmentX(LEFT_ALIGNMENT);
        statusVotoLabel.setAlignmentX(LEFT_ALIGNMENT);
        dadosCandidato.add(nomeLabel);
        dadosCandidato.add(Box.createVerticalStrut(15));
        dadosCandidato.add(partidoLabel);
        dadosCandidato.add(Box.createVerticalStrut(15));
        dadosCandidato.add(viceLabel);
        dadosCandidato.add(Box.createVerticalStrut(22));
        dadosCandidato.add(statusVotoLabel);
        dadosCandidato.add(Box.createVerticalGlue());
        centroTela.add(dadosCandidato, BorderLayout.CENTER);

        // Foto do titular em cima e foto do vice logo abaixo, na etapa presidencial.
        retrato.setPreferredSize(new Dimension(168, 204));
        retrato.setMaximumSize(new Dimension(168, 204));
        retratoVice.setPreferredSize(new Dimension(168, 165));
        retratoVice.setMaximumSize(new Dimension(168, 165));
        retrato.setBorder(BorderFactory.createLineBorder(new Color(119, 121, 123), 2));
        retratoVice.setBorder(BorderFactory.createLineBorder(new Color(119, 121, 123), 2));

        JPanel molduraRetrato = new JPanel();
        molduraRetrato.setOpaque(false);
        molduraRetrato.setLayout(new BoxLayout(molduraRetrato, BoxLayout.Y_AXIS));
        molduraRetrato.setBorder(new EmptyBorder(8, 6, 10, 0));
        molduraRetrato.add(retrato);
        molduraRetrato.add(Box.createVerticalStrut(12));
        molduraRetrato.add(retratoVice);
        centroTela.add(molduraRetrato, BorderLayout.EAST);
        moldura.add(centroTela, BorderLayout.CENTER);

        JLabel fim = texto("FIM", 118, Font.BOLD);
        fim.setHorizontalAlignment(SwingConstants.CENTER);
        fim.setForeground(new Color(52, 61, 69));
        painelFim.setOpaque(false);
        painelFim.add(fim, BorderLayout.CENTER);

        // Tela intermediária: exibida após CONFIRMA no cargo de Presidente.
        painelGravando.setOpaque(false);
        JPanel centroGravando = new JPanel();
        centroGravando.setOpaque(false);
        centroGravando.setLayout(new BoxLayout(centroGravando, BoxLayout.Y_AXIS));
        JLabel gravandoLabel = texto("GRAVANDO", 72, Font.BOLD);
        gravandoLabel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel gravandoSubtitulo = texto("Registrando o voto simulado...", 17, Font.PLAIN);
        gravandoSubtitulo.setForeground(new Color(80, 85, 89));
        gravandoSubtitulo.setAlignmentX(CENTER_ALIGNMENT);
        progressoGravacao.setPreferredSize(new Dimension(490, 30));
        progressoGravacao.setMaximumSize(new Dimension(490, 30));
        progressoGravacao.setAlignmentX(CENTER_ALIGNMENT);
        progressoGravacao.setForeground(new Color(81, 145, 125));
        progressoGravacao.setBackground(Color.WHITE);
        progressoGravacao.setBorder(BorderFactory.createLineBorder(new Color(115, 122, 122), 2));
        progressoGravacao.setStringPainted(false);
        centroGravando.add(Box.createVerticalGlue());
        centroGravando.add(gravandoLabel);
        centroGravando.add(Box.createVerticalStrut(30));
        centroGravando.add(progressoGravacao);
        centroGravando.add(Box.createVerticalStrut(14));
        centroGravando.add(gravandoSubtitulo);
        centroGravando.add(Box.createVerticalGlue());
        painelGravando.add(centroGravando, BorderLayout.CENTER);

        prepararPainelRelatorio(painelZeresima, textoZeresima);
        prepararPainelRelatorio(painelApuracao, textoApuracao);
        painelFotosVencedores.setOpaque(false);
        painelFotosVencedores.setBorder(new EmptyBorder(0, 8, 0, 0));
        painelFotosVencedores.setPreferredSize(new Dimension(332, 0));
        painelFotosVencedores.setVisible(false);
        painelApuracao.add(painelFotosVencedores, BorderLayout.EAST);

        JPanel rodape = new JPanel(new BorderLayout());
        rodapeTela = rodape;
        rodape.setOpaque(false);
        rodape.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(117, 117, 117)),
            new EmptyBorder(13, 0, 3, 0)
        ));
        instrucoesLabel.setForeground(new Color(50, 54, 59));
        rodape.add(instrucoesLabel, BorderLayout.CENTER);
        moldura.add(rodape, BorderLayout.SOUTH);
        return moldura;
    }

    private void prepararPainelRelatorio(JPanel painel, JTextArea texto) {
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(30, 35, 30, 35));
        texto.setEditable(false);
        texto.setFocusable(false);
        texto.setFont(new Font("Consolas", Font.BOLD, 20));
        texto.setForeground(LETRA);
        texto.setBackground(TELA);
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(texto);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TELA);
        painel.add(scroll, BorderLayout.CENTER);
    }

    private JPanel montarTeclado() {
        JPanel lado = new JPanel(new BorderLayout(0, 12));
        lado.setOpaque(false);
        lado.setPreferredSize(new Dimension(337, 0));

        JPanel cabeca = new JPanel(new BorderLayout());
        cabeca.setOpaque(false);
        JLabel titulo = texto("MARVEL  ×  DC", 23, Font.BOLD);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel texto = texto("TECLADO DA URNA", 13, Font.BOLD);
        texto.setForeground(new Color(73, 76, 78));
        texto.setHorizontalAlignment(SwingConstants.CENTER);
        cabeca.add(titulo, BorderLayout.CENTER);
        cabeca.add(texto, BorderLayout.SOUTH);
        cabeca.setPreferredSize(new Dimension(0, 62));
        lado.add(cabeca, BorderLayout.NORTH);

        JPanel painel = new JPanel(new BorderLayout(8, 13));
        painel.setBackground(PAINEL_TECLADO);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(109, 109, 109), 3),
            new EmptyBorder(15, 12, 16, 12)
        ));

        JPanel grade = new JPanel(new GridLayout(4, 3, 11, 11));
        grade.setOpaque(false);
        for (String numero : new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", ""}) {
            if (numero.isEmpty()) {
                JLabel vazio = new JLabel("");
                grade.add(vazio);
                continue;
            }
            JButton botao = botao(numero, TECLA, Color.WHITE, 32);
            botao.addActionListener(e -> digitar(numero));
            teclas.add(botao);
            grade.add(botao);
        }
        painel.add(grade, BorderLayout.CENTER);

        JPanel acoes = new JPanel(new GridLayout(1, 3, 7, 0));
        acoes.setOpaque(false);
        acoes.setPreferredSize(new Dimension(0, 66));
        acoes.add(brancoButton);
        acoes.add(corrigeButton);
        acoes.add(confirmaButton);
        painel.add(acoes, BorderLayout.SOUTH);
        lado.add(painel, BorderLayout.CENTER);
        return lado;
    }

    private void configurarEventos() {
        iniciar.addActionListener(e -> iniciarSessao());
        controleEleicao.addActionListener(e -> controlarEleicao());
        zerarTestes.addActionListener(e -> solicitarZerarTestes());
        brancoButton.addActionListener(e -> votarBranco());
        corrigeButton.addActionListener(e -> corrigir());
        confirmaButton.addActionListener(e -> confirmar());

        for (int i = 0; i <= 9; i++) {
            final String n = Integer.toString(i);
            registrarAtalho(n, "digitar-" + n, () -> digitar(n));
        }
        registrarAtalho("B", "votar-branco", this::votarBranco);
        registrarAtalho("BACK_SPACE", "corrigir", this::corrigir);
        registrarAtalho("ENTER", "confirmar", this::confirmar);
    }

    private void registrarAtalho(String tecla, String chave, Runnable acao) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(tecla), chave
        );
        getRootPane().getActionMap().put(chave, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acao.run();
            }
        });
    }

    private void iniciarSessao() {
        if (!"ABERTA".equals(estadoEleicao)) {
            JOptionPane.showMessageDialog(this, "Emita a zerésima antes de iniciar a votação.");
            return;
        }
        if (ocupado || (sessaoId != null && !"FINALIZADA".equals(etapa))) return;
        requisicao(api::criarSessao, resposta -> {
            mostrandoGravacao = false;
            mostrandoZeresima = false;
            mostrandoApuracao = false;
            sessaoId = String.valueOf(resposta.get("id"));
            etapa = String.valueOf(resposta.get("etapa"));
            limparDigitacao();
            statusBarra.setText("SESSÃO INICIADA • DIGITE O NÚMERO DO DEPUTADO FEDERAL");
            atualizarTela();
        });
    }

    private void digitar(String digito) {
        if (!podeDigitar() || branco || numeroDigitado.length() >= tamanhoNumero()) return;
        numeroDigitado += digito;
        previa = null;
        atualizarTela();
        if (numeroDigitado.length() == tamanhoNumero()) preparar();
    }

    private void votarBranco() {
        if (!podeDigitar()) return;
        if (!numeroDigitado.isEmpty()) {
            statusBarra.setText("PARA VOTAR EM BRANCO, APERTE CORRIGE PRIMEIRO");
            return;
        }
        branco = true;
        previa = null;
        atualizarTela();
        preparar();
    }

    private void corrigir() {
        if (!podeDigitar()) return;
        limparDigitacao();
        statusBarra.setText("ESCOLHA APAGADA • DIGITE NOVAMENTE");
        atualizarTela();
    }

    /**
     * Pré-carrega o catálogo e as fotos fora da thread gráfica. A consulta ao
     * Firebase não é necessária para simplesmente exibir um candidato.
     */
    private void carregarCatalogoCandidatos() {
        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                PainelRetrato.precarregar();
                return api.listarCandidatos();
            }
            @Override
            protected void done() {
                try {
                    catalogo.atualizar(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException | IllegalArgumentException e) {
                    // Fallback: /preparar continua disponível se a primeira busca falhar.
                    System.err.println("Catálogo rápido indisponível: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void preparar() {
        final String id = sessaoId;
        final String cargo = cargoAtual();
        final String numero = numeroDigitado;
        final boolean votoBranco = branco;
        // Mostra candidato/fotos imediatamente quando o catálogo já está carregado.
        // A confirmação REAL continua sendo validada na transação do Firestore.
        Map<String, Object> instantanea = catalogo.preparar(cargo, numero, votoBranco);
        if (instantanea != null) {
            previa = instantanea;
            statusBarra.setText("ESCOLHA IDENTIFICADA • CONFIRMA PARA REGISTRAR OU CORRIGE PARA APAGAR");
            atualizarTela();
            return;
        }
        // Se a API ainda não carregou o catálogo, mantém o caminho anterior seguro.
        statusBarra.setText("IDENTIFICANDO CANDIDATO NO SERVIDOR...");
        requisicao(() -> api.preparar(id, cargo, numero, votoBranco), resposta -> {
            previa = resposta;
            statusBarra.setText("ESCOLHA IDENTIFICADA • CONFIRMA PARA REGISTRAR OU CORRIGE PARA APAGAR");
            atualizarTela();
        });
    }

    private void confirmar() {
        if (!podeDigitar() || previa == null) return;
        final String id = sessaoId;
        final String cargo = cargoAtual();
        final String numero = numeroDigitado;
        final boolean votoBranco = branco;
        final boolean ultimoCargo = "PRESIDENTE".equals(etapa);

        if (ultimoCargo) {
            mostrandoGravacao = true;
            progressoGravacao.setValue(0);
            progressoGravacao.setIndeterminate(true);
        }

        statusBarra.setText("GRAVANDO VOTO NO FIREBASE • AGUARDE A CONFIRMAÇÃO");
        requisicao(() -> api.confirmar(id, cargo, numero, votoBranco), resposta -> {
            Object confirmado = resposta.get("totalConfirmados");
            if (confirmado instanceof Number numeroVotos) {
                totalVotosEleicao = numeroVotos.longValue();
            }
            String proxima = String.valueOf(resposta.get("proximaEtapa"));
            if (ultimoCargo && "FINALIZADA".equals(proxima)) {
                // Só avança para FIM após o backend confirmar o voto.
                // A barra oferece tempo para a pessoa perceber a transição.
                etapa = proxima;
                ocupado = true;
                progressoGravacao.setIndeterminate(false);
                progressoGravacao.setValue(10);
                Timer animacao = new Timer(40, null);
                animacao.addActionListener(e -> {
                    int valor = Math.min(100, progressoGravacao.getValue() + 6);
                    progressoGravacao.setValue(valor);
                    if (valor == 100) {
                        animacao.stop();
                        mostrandoGravacao = false;
                        ocupado = false;
                        limparDigitacao();
                        statusBarra.setText("VOTAÇÃO SIMULADA FINALIZADA • OBRIGADO POR PARTICIPAR");
                        atualizarTela();
                    }
                });
                animacao.start();
            } else {
                etapa = proxima;
                mostrandoGravacao = false;
                limparDigitacao();
                statusBarra.setText("VOTO CONFIRMADO • AGORA ESCOLHA O PRESIDENTE");
                atualizarTela();
            }
        });
    }

    private void limparDigitacao() {
        numeroDigitado = "";
        branco = false;
        previa = null;
    }

    private boolean podeDigitar() {
        return sessaoId != null && !ocupado && !mostrandoGravacao
            && !"FINALIZADA".equals(etapa);
    }

    private int tamanhoNumero() {
        return "DEPUTADO".equals(etapa) ? 4 : 2;
    }

    private String cargoAtual() {
        return "DEPUTADO".equals(etapa) ? "Deputado Federal" : "Presidente";
    }

    @SuppressWarnings("unchecked")
    private void atualizarTela() {
        boolean finalizada = "FINALIZADA".equals(etapa);
        boolean gravando = mostrandoGravacao;
        boolean inicial = sessaoId == null;
        cargoLabel.setText(inicial ? "PRONTO PARA INICIAR" : finalizada ? "" : cargoAtual().toUpperCase());

        if (molduraTela != null) {
            molduraTela.remove(centroTela);
            molduraTela.remove(painelFim);
            molduraTela.remove(painelGravando);
            molduraTela.remove(painelZeresima);
            molduraTela.remove(painelApuracao);
            molduraTela.add(mostrandoApuracao ? painelApuracao :
                (gravando ? painelGravando :
                (mostrandoZeresima ? painelZeresima : (finalizada ? painelFim : centroTela))),
                BorderLayout.CENTER);
            boolean ocultarVoto = gravando || finalizada || mostrandoZeresima || mostrandoApuracao;
            topoTela.setVisible(!ocultarVoto);
            rodapeTela.setVisible(!ocultarVoto);
            molduraTela.revalidate();
            molduraTela.repaint();
        }
        viceLabel.setVisible(false);
        retratoVice.setVisible(false);

        painelCampos.setVisible(!inicial && !finalizada && !branco);
        for (int i = 0; i < digitos.length; i++) {
            JLabel campo = digitos[i];
            campo.setVisible(!inicial && !finalizada && i < tamanhoNumero());
            campo.setText(i < numeroDigitado.length() ? numeroDigitado.substring(i, i + 1) : "");
            campo.setBackground(i == numeroDigitado.length() && !branco ? new Color(255, 255, 239) : Color.WHITE);
        }

        if (inicial) {
            nomeLabel.setText("VOTOVERSO");
            partidoLabel.setText("O Multiverso Vai às Urnas • Marvel × DC");
            statusVotoLabel.setText("PRONTO PARA VOTAR");
            statusVotoLabel.setForeground(new Color(60, 65, 70));
            instrucoesLabel.setText("Esta é uma simulação educativa. Nenhum voto real é registrado.");
            retrato.exibir("", false, "CANDIDATO");
        } else if (finalizada) {
            instrucoesLabel.setText("Simulação concluída. Você pode iniciar uma nova sessão.");
            retrato.exibir("", false, "CANDIDATO");
        } else if (previa != null) {
            String tipo = String.valueOf(previa.get("tipoVoto"));
            if ("VALIDO".equals(tipo)) {
                Map<String, Object> candidato = (Map<String, Object>) previa.get("candidato");
                Map<String, Object> partido = (Map<String, Object>) candidato.get("partido");
                nomeLabel.setText("Nome: " + candidato.get("nome"));
                partidoLabel.setText("Partido: " + partido.get("sigla") + " (" + partido.get("nome") + ")");
                statusVotoLabel.setText("CANDIDATO ENCONTRADO");
                statusVotoLabel.setForeground(new Color(36, 103, 68));
                boolean presidente = "PRESIDENTE".equals(etapa);
                String numeroCandidato = String.valueOf(candidato.get("numero"));
                retrato.exibir(numeroCandidato, true,
                    presidente ? "PRESIDENTE" : "DEPUTADO");

                if (presidente && candidato.get("vice") instanceof Map<?, ?> dadosVice) {
                    Object nomeVice = dadosVice.get("nome");
                    if (nomeVice != null) {
                        viceLabel.setText("Vice-presidente: " + nomeVice);
                        viceLabel.setVisible(true);
                        retratoVice.exibir(numeroCandidato + "-VICE", true, "VICE-PRESIDENTE");
                        retratoVice.setVisible(true);
                    }
                }
            } else {
                nomeLabel.setText("BRANCO".equals(tipo) ? "VOTO EM BRANCO" : "VOTO NULO");
                partidoLabel.setText("Nenhum candidato selecionado");
                statusVotoLabel.setText("PRESSIONE CONFIRMA OU CORRIGE");
                statusVotoLabel.setForeground(LETRA);
                retrato.exibir("", false, "CANDIDATO");
            }
            instrucoesLabel.setText("CONFIRMA para registrar este voto     |     CORRIGE para alterar");
        } else {
            nomeLabel.setText(branco ? "VOTO EM BRANCO" : "Digite o número do candidato");
            partidoLabel.setText("Número com " + tamanhoNumero() + " dígitos");
            statusVotoLabel.setText("");
            instrucoesLabel.setText("BRANCO para voto em branco     |     CORRIGE para apagar     |     CONFIRMA para registrar");
            retrato.exibir("", false, "CANDIDATO");
        }

        boolean habilitarTeclas = podeDigitar();
        for (JButton tecla : teclas) {
            tecla.setEnabled(habilitarTeclas && !branco && numeroDigitado.length() < tamanhoNumero());
        }
        brancoButton.setEnabled(habilitarTeclas && numeroDigitado.isEmpty() && !branco);
        corrigeButton.setEnabled(habilitarTeclas && (branco || !numeroDigitado.isEmpty()));
        confirmaButton.setEnabled(habilitarTeclas && previa != null);
        iniciar.setText(inicial ? "INICIAR SIMULAÇÃO" : "NOVA SIMULAÇÃO");
        iniciar.setEnabled(!ocupado && "ABERTA".equals(estadoEleicao)
            && (inicial || finalizada));
        controleEleicao.setEnabled(!ocupado && !mostrandoGravacao
            && (sessaoId == null || finalizada)
            && !"CARREGANDO".equals(estadoEleicao));
        // O controle destrutivo só aparece quando existem votos nesta rodada.
        zerarTestes.setVisible(totalVotosEleicao > 0 && (sessaoId == null || finalizada));
        zerarTestes.setEnabled(!ocupado && !mostrandoGravacao
            && (sessaoId == null || finalizada)
            && totalVotosEleicao > 0);
        controleEleicao.setText(switch (estadoEleicao) {
            case "PREPARACAO" -> "EMITIR ZERÉSIMA";
            case "ABERTA" -> "ZERÉSIMA / ENCERRAR";
            case "ENCERRADA" -> "VER APURAÇÃO";
            default -> "CARREGANDO...";
        });
        if ("ENCERRADA".equals(estadoEleicao)) iniciar.setText("ELEIÇÃO ENCERRADA");
    }

    /** Exibe os zeros históricos da abertura. Nunca recalcula a zerésima com votos existentes. */
    private void exibirZeresima(Map<String, Object> dados) {
        mostrandoZeresima = true;
        mostrandoApuracao = false;
        textoZeresima.setText("ZERÉSIMA — VOTOVERSO\n"
            + "O MULTIVERSO VAI ÀS URNAS\n\n"
            + (totalVotosEleicao > 0
                ? "ATENÇÃO: HÁ " + totalVotosEleicao + " VOTOS NESTA RODADA!\n"
                    + "Para começar do zero, clique ZERAR TESTES.\n\n"
                : "SITUAÇÃO ATUAL: ZERO VOTOS.\n\n")
            + "Registro anterior ao primeiro voto.\n"
            + "Eleição: " + dados.get("eleicaoId") + " • Rodada: " + dados.get("rodada") + "\n"
            + "Emissão (Bahia): " + HorarioLocal.formatar(dados.get("emitidaEm")) + "\n\n"
            + "DEPUTADO FEDERAL\n"
            + "  Homem-Aranha (PM) ............ 0\n"
            + "  Mulher-Maravilha (PDC) ....... 0\n"
            + "  Brancos / Nulos .............. 0 / 0\n\n"
            + "PRESIDENTE\n"
            + "  Homem de Ferro (PM) .......... 0\n"
            + "  Superman (PDC) ............... 0\n"
            + "  Brancos / Nulos .............. 0 / 0\n\n"
            + "TOTAL DE VOTOS CONFIRMADOS: 0\n\n"
            + "Este é o registro histórico da abertura, não um placar ao vivo."
            + (totalVotosEleicao > 0
                ? "\n\nATENÇÃO: esta rodada já recebeu " + totalVotosEleicao
                    + " votos confirmados.\nPara reiniciar os testes, use ZERAR TESTES com senha da equipe."
                : "\n\nSISTEMA ZERADO E PRONTO PARA VOTAÇÃO."));
        textoZeresima.setCaretPosition(0);
        statusBarra.setText(totalVotosEleicao > 0
            ? "JÁ EXISTEM VOTOS • SE FOR TESTE, USE ZERAR TESTES"
            : "ZERÉSIMA EMITIDA • CONFIRA OS ZEROS E INICIE A SIMULAÇÃO");
        atualizarTela();
    }

    private void carregarEstadoEleicao() {
        requisicao(api::estadoEleicao, dados -> {
            estadoEleicao = String.valueOf(dados.get("estado"));
            if (dados.get("totalConfirmados") instanceof Number n) totalVotosEleicao = n.longValue();
            if (dados.get("rodada") instanceof Number n) rodadaAtual = n.longValue();
            if ("PREPARACAO".equals(estadoEleicao)) {
                statusBarra.setText("ELEIÇÃO AINDA NÃO ABERTA • EMITA A ZERÉSIMA");
            } else if ("ABERTA".equals(estadoEleicao)) {
                statusBarra.setText(totalVotosEleicao == 0
                    ? "ZERÉSIMA • ZERO VOTOS • INICIE A SIMULAÇÃO"
                    : "ELEIÇÃO COM " + totalVotosEleicao + " VOTOS • RESET RESTRITO À EQUIPE");
                requisicao(api::verZeresima, this::exibirZeresima);
            } else {
                statusBarra.setText("ELEIÇÃO ENCERRADA • RESULTADOS DISPONÍVEIS");
                carregarResultado();
            }
            atualizarTela();
        });
    }

    private String solicitarChave() {
        JPasswordField campo = new JPasswordField(20);
        int opcao = JOptionPane.showConfirmDialog(this, campo,
            "Senha da equipe organizadora (URNA_ADMIN_KEY)",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) return null;
        char[] senha = campo.getPassword();
        String chave = new String(senha);
        Arrays.fill(senha, '\0');
        return chave;
    }

    private void controlarEleicao() {
        if (ocupado) return;
        switch (estadoEleicao) {
            case "PREPARACAO" -> {
                String chave = solicitarChave();
                if (chave == null) return;
                requisicao(() -> api.emitirZeresima(chave), dados -> {
                    estadoEleicao = "ABERTA";
                    totalVotosEleicao = 0L;
                    rodadaAtual = ((Number) dados.get("rodada")).longValue();
                    exibirZeresima(dados);
                });
            }
            case "ABERTA" -> {
                String[] opcoes = {"VER ZERÉSIMA", "ATUALIZAR ESTADO", "ENCERRAR E APURAR", "CANCELAR"};
                int opcao = JOptionPane.showOptionDialog(this,
                    "Ver o registro inicial ou encerrar a eleição de toda a turma?",
                    "Controle do VotoVerso", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
                if (opcao == 0) {
                    requisicao(api::verZeresima, this::exibirZeresima);
                } else if (opcao == 1) {
                    carregarEstadoEleicao();
                } else if (opcao == 2) {
                    if (sessaoId != null && !"FINALIZADA".equals(etapa)) {
                        JOptionPane.showMessageDialog(this,
                            "Finalize a votação em andamento neste computador primeiro.");
                        return;
                    }
                    int ok = JOptionPane.showConfirmDialog(this,
                        "ATENÇÃO: encerrar é definitivo para esta eleição!\n"
                        + "Faça isso somente após todos os participantes votarem.\n"
                        + "Depois, não será possível registrar novos votos.\n\n"
                        + "Encerrar e mostrar vencedores?",
                        "ENCERRAR ELEIÇÃO", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    if (ok != JOptionPane.YES_OPTION) return;
                    String chave = solicitarChave();
                    if (chave == null) return;
                    requisicao(() -> api.encerrarEleicao(chave), dados -> {
                        estadoEleicao = "ENCERRADA";
                        exibirResultado(dados);
                    });
                }
            }
            case "ENCERRADA" -> carregarResultado();
            default -> { }
        }
    }

    /** Reinicia uma rodada e arquiva a antiga; exige senha do professor/equipe. */
    private void solicitarZerarTestes() {
        if (ocupado || totalVotosEleicao <= 0) return;
        if (sessaoId != null && !"FINALIZADA".equals(etapa)) {
            JOptionPane.showMessageDialog(this,
                "Finalize a sessão atual antes de reiniciar os testes.");
            return;
        }
        int escolha = JOptionPane.showConfirmDialog(this,
            "Esta rodada possui " + totalVotosEleicao + " votos confirmados.\n"
            + "ZERAR TESTES inicia uma nova rodada com todos os contadores zerados.\n"
            + "Os dados antigos ficam arquivados no Firestore.\n"
            + "Sessões antigas, inclusive em outros computadores, deixarão de funcionar.\n\n"
            + "Deseja continuar?",
            "REINICIAR RODADA DE TESTES", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (escolha != JOptionPane.YES_OPTION) return;
        String chave = solicitarChave();
        if (chave == null) return;
        requisicao(() -> api.zerarTestes(chave), dados -> {
            rodadaAtual = ((Number) dados.get("rodada")).longValue();
            totalVotosEleicao = 0;
            estadoEleicao = "ABERTA";
            sessaoId = null;
            etapa = "INICIO";
            limparDigitacao();
            mostrandoGravacao = false;
            mostrandoApuracao = false;
            exibirZeresima(dados);
            statusBarra.setText("RODADA " + rodadaAtual + " ZERADA • PRONTA PARA NOVOS TESTES");
        });
    }

    private void carregarResultado() {
        requisicao(api::resultadoEleicao, this::exibirResultado);
    }

    @SuppressWarnings("unchecked")
    private void exibirResultado(Map<String, Object> dados) {
        Map<String, Object> deputado = (Map<String, Object>) dados.get("deputado");
        Map<String, Object> presidente = (Map<String, Object>) dados.get("presidente");
        mostrandoApuracao = true;
        mostrandoZeresima = false;
        textoApuracao.setText("VOTOVERSO — APURAÇÃO FINAL\n"
            + "Eleição encerrada: " + dados.get("eleicaoId") + "\n\n"
            + "DEPUTADO FEDERAL\n"
            + "  Homem-Aranha (PM):        " + deputado.get("pm") + " votos\n"
            + "  Mulher-Maravilha (PDC):   " + deputado.get("pdc") + " votos\n"
            + "  Brancos: " + deputado.get("brancos")
            + "    Nulos: " + deputado.get("nulos") + "\n"
            + "  RESULTADO: " + deputado.get("vencedor") + "\n\n"
            + "PRESIDENTE\n"
            + "  Homem de Ferro + Thor:    " + presidente.get("pm") + " votos\n"
            + "  Superman + Batman:        " + presidente.get("pdc") + " votos\n"
            + "  Brancos: " + presidente.get("brancos")
            + "    Nulos: " + presidente.get("nulos") + "\n"
            + "  RESULTADO: " + presidente.get("vencedor") + "\n\n"
            + "Votos confirmados (ambos os cargos): " + dados.get("totalConfirmados")
            + "\n\nEleição fictícia educativa • Sem vínculo com eleições reais.");
        textoApuracao.setCaretPosition(0);
        atualizarFotosApuracao(deputado, presidente);
        statusBarra.setText("RESULTADO DO SIMULADOR • ELEIÇÃO ENCERRADA");
        atualizarTela();
    }

    /**
     * Relatório final: mostra foto somente quando existe um vencedor único.
     * O deputado tem um retrato; a chapa vencedora tem presidente e vice.
     * Empate ou zero votos válidos continuam só com a informação escrita.
     */
    private void atualizarFotosApuracao(Map<String, Object> deputado,
                                       Map<String, Object> presidente) {
        painelFotosVencedores.removeAll();
        List<FotosApuracao.Retrato> eleitosDeputado = FotosApuracao.deputados(deputado);
        List<FotosApuracao.Retrato> chapaEleita = FotosApuracao.presidencia(presidente);
        boolean temFoto = !eleitosDeputado.isEmpty() || !chapaEleita.isEmpty();
        painelFotosVencedores.setVisible(temFoto);
        if (!temFoto) {
            painelFotosVencedores.revalidate();
            painelFotosVencedores.repaint();
            return;
        }

        JPanel coluna = new JPanel();
        coluna.setOpaque(false);
        coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));
        if (!eleitosDeputado.isEmpty()) {
            JLabel titulo = tituloFotos("DEPUTADO ELEITO");
            coluna.add(titulo);
            coluna.add(Box.createVerticalStrut(5));
            JPanel linha = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
            linha.setOpaque(false);
            linha.add(cartaoEleito(eleitosDeputado.get(0), 158, 168));
            coluna.add(linha);
            coluna.add(Box.createVerticalStrut(15));
        }
        if (!chapaEleita.isEmpty()) {
            coluna.add(tituloFotos("CHAPA PRESIDENCIAL ELEITA"));
            coluna.add(Box.createVerticalStrut(5));
            JPanel linha = new JPanel(new GridLayout(1, 2, 8, 0));
            linha.setOpaque(false);
            for (FotosApuracao.Retrato eleito : chapaEleita) {
                linha.add(cartaoEleito(eleito, 150, 168));
            }
            coluna.add(linha);
        }
        painelFotosVencedores.add(coluna, BorderLayout.NORTH);
        painelFotosVencedores.revalidate();
        painelFotosVencedores.repaint();
    }

    private static JLabel tituloFotos(String valor) {
        JLabel titulo = texto(valor, 14, Font.BOLD);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setAlignmentX(CENTER_ALIGNMENT);
        return titulo;
    }

    private static JPanel cartaoEleito(FotosApuracao.Retrato eleito, int largura, int altura) {
        JPanel cartao = new JPanel(new BorderLayout(0, 3));
        cartao.setOpaque(false);
        cartao.setPreferredSize(new Dimension(largura, altura + 30));

        PainelRetrato foto = new PainelRetrato();
        foto.setPreferredSize(new Dimension(largura, altura));
        foto.setBorder(BorderFactory.createLineBorder(new Color(120, 124, 128), 2));
        foto.exibir(eleito.identificador(), true, eleito.cargo());
        cartao.add(foto, BorderLayout.CENTER);

        JLabel nome = texto(eleito.nome(), 12, Font.BOLD);
        nome.setHorizontalAlignment(SwingConstants.CENTER);
        cartao.add(nome, BorderLayout.SOUTH);
        return cartao;
    }

    private void requisicao(Callable<Map<String, Object>> operacao,
                           Consumer<Map<String, Object>> sucesso) {
        ocupado = true;
        atualizarTela();
        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return operacao.call();
            }
            @Override
            protected void done() {
                ocupado = false;
                try {
                    sucesso.accept(get());
                } catch (InterruptedException ex) {
                    mostrandoGravacao = false;
                    progressoGravacao.setIndeterminate(false);
                    Thread.currentThread().interrupt();
                    mostrarErro("Operação interrompida.");
                } catch (ExecutionException ex) {
                    mostrandoGravacao = false;
                    progressoGravacao.setIndeterminate(false);
                    Throwable causa = ex.getCause();
                    mostrarErro(causa == null ? "Falha desconhecida" : causa.getMessage());
                } finally {
                    atualizarTela();
                }
            }
        }.execute();
    }

    private void mostrarErro(String mensagem) {
        statusBarra.setText("FALHA NA COMUNICAÇÃO COM O SERVIDOR");
        JOptionPane.showMessageDialog(this,
            mensagem + "\n\nVerifique se o Spring Boot está ativo em localhost:8080.",
            "Simulador VotoVerso", JOptionPane.WARNING_MESSAGE);
    }

    private static JLabel texto(String valor, int tamanho, int estilo) {
        JLabel texto = new JLabel(valor);
        texto.setFont(new Font("Arial", estilo, tamanho));
        texto.setForeground(LETRA);
        return texto;
    }

    private static JButton botao(String nome, Color fundo, Color frente, int tamanho) {
        return new Tecla3D(nome, fundo, frente, tamanho);
    }

    /** Desenha botões sem usar as cores automáticas de componentes desabilitados. */
    private static final class Tecla3D extends JButton {
        private final Color base;
        private final Color escrita;

        Tecla3D(String titulo, Color base, Color escrita, int tamanho) {
            super(titulo);
            this.base = base;
            this.escrita = escrita;
            setFont(new Font("Arial", Font.BOLD, tamanho));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setMargin(new java.awt.Insets(3, 3, 3, 3));
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 2;
            int h = getHeight() - 3;
            boolean pressionado = getModel().isPressed() && getModel().isArmed();
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRoundRect(2, 5, w - 2, h - 2, 7, 7);
            Color claro = base.brighter();
            Color escuro = base.darker();
            if (!isEnabled()) {
                claro = base;
                escuro = base.darker();
            }
            g2.setPaint(new GradientPaint(0, 0, pressionado ? escuro : claro,
                0, h, pressionado ? claro : base));
            g2.fillRoundRect(1, pressionado ? 4 : 1, w - 2, h - 3, 6, 6);
            g2.setColor(base.darker().darker());
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(1, pressionado ? 4 : 1, w - 3, h - 4, 6, 6);
            g2.setColor(isEnabled() ? escrita : mistura(escrita, base, 0.14));
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String txt = getText();
            int px = (getWidth() - fm.stringWidth(txt)) / 2;
            int py = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() + (pressionado ? 2 : 0);
            g2.drawString(txt, Math.max(3, px), py);
            g2.dispose();
        }

        private static Color mistura(Color a, Color b, double proporcao) {
            double p = Math.max(0, Math.min(1, proporcao));
            return new Color((int)(a.getRed() * (1-p) + b.getRed() * p),
                (int)(a.getGreen() * (1-p) + b.getGreen() * p),
                (int)(a.getBlue() * (1-p) + b.getBlue() * p));
        }
    }

    /**
     * Carrega fotos JPEG do classpath: src/main/resources/imagens/.
     * Os nomes dos arquivos são vinculados ao número registrado no backend.
     */
    private static final class PainelRetrato extends JPanel {
        private static final int ALTURA_LEGENDA = 24;
        private static final Map<String, String> ARQUIVOS = Map.of(
            "7101", "homem-aranha.jpg",
            "71", "homem-ferro.jpg",
            "71-VICE", "thor.jpg",
            "7201", "mulher-maravilha.jpg",
            "72", "superman.jpg",
            "72-VICE", "batman.jpg"
        );
        private static final Map<String, BufferedImage> CACHE = new java.util.concurrent.ConcurrentHashMap<>();

        private BufferedImage foto;
        private String funcao = "CANDIDATO";
        private boolean mostrar;

        PainelRetrato() {
            setOpaque(true);
            setBackground(new Color(230, 234, 237));
        }

        void exibir(String identificador, boolean mostrar, String funcao) {
            this.mostrar = mostrar;
            this.funcao = funcao;
            this.foto = mostrar ? carregarFoto(identificador) : null;
            repaint();
        }

        static void precarregar() {
            for (String identificador : ARQUIVOS.keySet()) carregarFoto(identificador);
        }

        private static BufferedImage carregarFoto(String identificador) {
            String arquivo = ARQUIVOS.get(identificador);
            if (arquivo == null) return null;
            if (CACHE.containsKey(arquivo)) return CACHE.get(arquivo);

            BufferedImage carregada = null;
            try (InputStream entrada = UrnaFrame.class.getResourceAsStream("/imagens/" + arquivo)) {
                if (entrada != null) {
                    carregada = ImageIO.read(entrada);
                } else {
                    System.err.println("Imagem não encontrada em resources/imagens/: " + arquivo);
                }
            } catch (IOException erro) {
                System.err.println("Não foi possível ler " + arquivo + ": " + erro.getMessage());
            }
            if (carregada != null) CACHE.putIfAbsent(arquivo, carregada);
            return carregada;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int largura = getWidth();
            int altura = getHeight();
            int alturaFoto = Math.max(1, altura - ALTURA_LEGENDA);
            g2.setColor(new Color(230, 234, 237));
            g2.fillRect(0, 0, largura, altura);

            if (mostrar && foto != null && largura > 0) {
                // Preenche a moldura sem distorcer a imagem. Para retratos
                // verticais, prioriza a região superior (rosto do personagem).
                int fotoW = foto.getWidth();
                int fotoH = foto.getHeight();
                double fator = Math.max((double) largura / fotoW,
                    (double) alturaFoto / fotoH);
                int corteW = Math.min(fotoW, (int) Math.ceil(largura / fator));
                int corteH = Math.min(fotoH, (int) Math.ceil(alturaFoto / fator));
                int origemX = (fotoW - corteW) / 2;
                int origemY = Math.max(0, (fotoH - corteH) / 5);
                g2.drawImage(foto, 0, 0, largura, alturaFoto,
                    origemX, origemY, origemX + corteW, origemY + corteH, null);
            } else {
                g2.setColor(new Color(88, 93, 97));
                g2.setFont(new Font("Arial", Font.BOLD, 13));
                centralizar(g2, mostrar ? "SEM IMAGEM" : "FOTO", largura, alturaFoto / 2);
            }

            if (mostrar) {
                g2.setColor(new Color(242, 243, 243));
                g2.fillRect(0, alturaFoto, largura, ALTURA_LEGENDA);
                g2.setColor(new Color(40, 46, 50));
                g2.setFont(new Font("Arial", Font.BOLD,
                    "VICE-PRESIDENTE".equals(funcao) ? 10 : 11));
                centralizar(g2, funcao, largura, alturaFoto + 16);
            }
            g2.dispose();
        }

        private static void centralizar(Graphics2D g, String texto, int largura, int y) {
            FontMetrics metricas = g.getFontMetrics();
            g.drawString(texto, (largura - metricas.stringWidth(texto)) / 2, y);
        }
    }

    private static final class PainelCasco extends JPanel {
        PainelCasco(BorderLayout layout) { super(layout); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, CASCO,
                getWidth(), getHeight(), CASCO_ESCURO));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
