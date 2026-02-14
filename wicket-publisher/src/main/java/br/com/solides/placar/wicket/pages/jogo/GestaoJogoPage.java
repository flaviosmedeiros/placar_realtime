package br.com.solides.placar.wicket.pages.jogo;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.util.PublisherUtils;
import br.com.solides.placar.wicket.BaseWebPage;
import br.com.solides.placar.wicket.components.modal.AtualizarPlacarModal;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Página para gestão em tempo real de um jogo específico.
 * Permite iniciar, finalizar e atualizar placar do jogo.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Slf4j
public class GestaoJogoPage extends BaseWebPage {
    private static final long serialVersionUID = 1L;

    @Inject
    private JogoService jogoService;

    private Long jogoId;
    private JogoDTO jogo;
    private FeedbackPanel feedbackPanel;
    private AtualizarPlacarModal atualizarPlacarModal;
    
    // Form para visualização das informações
    private Form<JogoVisualizacaoForm> formVisualizacao;
    private JogoVisualizacaoForm visualizacaoForm;

    

    public GestaoJogoPage(PageParameters parameters) {
        super(parameters);
        
        this.jogoId = parameters.get("jogoId").toOptionalLong();
        if (jogoId == null) {
            error("ID do jogo não informado");
            setResponsePage(JogoListPage.class);
            return;
        }
        
        carregarJogo();
        initializeComponents();
    }

    private void carregarJogo() {
        try {
            this.jogo = jogoService.buscarPorId(jogoId);
            if (jogo == null) {
                error("Jogo não encontrado");
                setResponsePage(JogoListPage.class);
                return;
            }
            
            // Atualizar form de visualização
            if (visualizacaoForm != null) {
                visualizacaoForm.fromJogoDTO(jogo);
                if (formVisualizacao != null) {
                    formVisualizacao.modelChanged();
                }
            } else {
                visualizacaoForm = new JogoVisualizacaoForm();
                visualizacaoForm.fromJogoDTO(jogo);
            }
            
        } catch (Exception e) {
            log.error("Erro ao carregar jogo ID: {}", jogoId, e);
            error("Erro ao carregar jogo: " + e.getMessage());
            setResponsePage(JogoListPage.class);
        }
    }

    private void initializeComponents() {
        // Título da página
        add(new Label("pageTitle", "Gestão do Jogo - " + jogo.getTimeA() + " vs " + jogo.getTimeB()));
        add(new Label("headerTitle", "Gestão do Jogo"));
        
        // Feedback Panel
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
        
        // Botão Voltar
        AjaxLink<Void> botaoVoltar = new AjaxLink<Void>("botaoVoltar") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(JogoListPage.class);
            }
        };
        add(botaoVoltar);
        
        // Form de visualização
        criarFormVisualizacao();       
        
        // Modal para atualizar placar
        criarModal();
    }

    private void criarFormVisualizacao() {
    	
        formVisualizacao = new Form<>("formVisualizacao", new CompoundPropertyModel<>(visualizacaoForm));
        
        // Campos do jogo (somente leitura)
        TextField<Long> campoId = new TextField<>("id");
        campoId.setEnabled(false);
        formVisualizacao.add(campoId);
        
        TextField<String> campoTimeA = new TextField<>("timeA");
        campoTimeA.setEnabled(false);
        formVisualizacao.add(campoTimeA);
        
        TextField<String> campoTimeB = new TextField<>("timeB");
        campoTimeB.setEnabled(false);
        formVisualizacao.add(campoTimeB);
        
        NumberTextField<Integer> campoPlacarA = new NumberTextField<>("placarA");
        campoPlacarA.setEnabled(false);
        formVisualizacao.add(campoPlacarA);
        
        NumberTextField<Integer> campoPlacarB = new NumberTextField<>("placarB");
        campoPlacarB.setEnabled(false);
        formVisualizacao.add(campoPlacarB);
        
        TextField<String> campoStatus = new TextField<>("statusLabel");
        campoStatus.setEnabled(false);
        formVisualizacao.add(campoStatus);
        
        TextField<String> campoDataPartida = new TextField<>("dataPartidaStr");
        campoDataPartida.setEnabled(false);
        formVisualizacao.add(campoDataPartida);
        
        TextField<String> campoHoraPartida = new TextField<>("horaPartida");
        campoHoraPartida.setEnabled(false);
        formVisualizacao.add(campoHoraPartida);
        
        TextField<Integer> campoTempoJogo = new TextField<>("tempoDeJogo");
        campoTempoJogo.setEnabled(false);
        formVisualizacao.add(campoTempoJogo);
        
        formVisualizacao.setOutputMarkupId(true);        
        
        // Toolbar com botões de ação
        criarToolbar();        
        
        add(formVisualizacao);
    }

    private void criarToolbar() {
    	
    	
        // Botão Iniciar Jogo
        AjaxLink<Void> botaoIniciar = new AjaxLink<Void>("botaoIniciar") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    log.info("Iniciando jogo ID: {}", jogoId);
                    
                    jogo = jogoService.iniciarJogo(jogoId);
                    
                    success("Jogo iniciado com sucesso!");
                    carregarJogo(); // Recarregar dados
                    
                    target.add(formVisualizacao);
                    target.add(feedbackPanel);
                    
                } catch (Exception e) {
                    log.error("Erro ao iniciar jogo", e);
                    error("Erro ao iniciar jogo: " + e.getMessage());
                    target.add(feedbackPanel);
                }
            }
            
            @Override
            public boolean isVisible() {
                return jogo != null && jogo.getStatus() == StatusJogo.NAO_INICIADO;
            }
        };
        botaoIniciar.setOutputMarkupId(true);
        botaoIniciar.setOutputMarkupPlaceholderTag(true);
        formVisualizacao.add(botaoIniciar);

        
        
        
        // Botão Finalizar Jogo
        AjaxLink<Void> botaoFinalizar = new AjaxLink<Void>("botaoFinalizar") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    log.info("Finalizando jogo ID: {}", jogoId);
                    
                    jogo = jogoService.finalizarJogo(jogoId);
                    
                    success("Jogo finalizado com sucesso!");
                    carregarJogo(); // Recarregar dados
                    
                    target.add(formVisualizacao);
                    target.add(feedbackPanel);
                    
                } catch (Exception e) {
                    log.error("Erro ao finalizar jogo", e);
                    error("Erro ao finalizar jogo: " + e.getMessage());
                    target.add(feedbackPanel);
                }
            }
            
            @Override
            public boolean isVisible() {
                return jogo != null && jogo.getStatus() == StatusJogo.EM_ANDAMENTO;
            }
        };
        botaoFinalizar.setOutputMarkupId(true);
        botaoFinalizar.setOutputMarkupPlaceholderTag(true);
        
        formVisualizacao.add(botaoFinalizar);

        
        
        
        // Botão Atualizar Placar
        AjaxLink<Void> botaoAtualizarPlacar = new AjaxLink<Void>("botaoAtualizarPlacar") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                // Abrir modal para atualizar placar
                atualizarPlacarModal.carregarJogo(jogo);
                target.add(atualizarPlacarModal);
                target.appendJavaScript("$('#modalAtualizarPlacar').modal('show');");
            }
            
            @Override
            public boolean isVisible() {
                return jogo != null && jogo.getStatus() == StatusJogo.EM_ANDAMENTO;
            }
        };
        botaoAtualizarPlacar.setOutputMarkupId(true);
        botaoAtualizarPlacar.setOutputMarkupPlaceholderTag(true);
        
        formVisualizacao.add(botaoAtualizarPlacar);
    }

    
    
    
    
    
    
    
    
    private void criarModal() {
        // Modal Atualizar Placar
        atualizarPlacarModal = new AtualizarPlacarModal("atualizarPlacarModal", new AtualizarPlacarModal.ModalCallback() {
            @Override
            public void onSuccess(AjaxRequestTarget target, JogoDTO jogoAtualizado) {
                success("Placar atualizado com sucesso: " + jogoAtualizado.getPlacarA() + " x " + jogoAtualizado.getPlacarB());
                carregarJogo(); // Recarregar dados
                target.add(formVisualizacao);
                target.add(feedbackPanel);
                target.appendJavaScript("$('#modalAtualizarPlacar').modal('hide');");
            }

            @Override
            public void onError(AjaxRequestTarget target, String mensagem) {
                error(mensagem);
                target.add(feedbackPanel);
            }

            @Override
            public void onCancel(AjaxRequestTarget target) {
                target.appendJavaScript("$('#modalAtualizarPlacar').modal('hide');");
            }
        });
        add(atualizarPlacarModal);
    }

    /**
     * Classe interna para representar o form de visualização do jogo
     */
    public static class JogoVisualizacaoForm implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long id;
        private String timeA;
        private String timeB;
        private Integer placarA;
        private Integer placarB;
        private String statusLabel;
        private String dataPartidaStr;
        private String horaPartida;
        private Integer tempoDeJogo;
        
        public void fromJogoDTO(JogoDTO jogo) {
            this.id = jogo.getId();
            this.timeA = jogo.getTimeA();
            this.timeB = jogo.getTimeB();
            this.placarA = jogo.getPlacarA();
            this.placarB = jogo.getPlacarB();
            this.statusLabel = getStatusLabel(jogo.getStatus());
            this.dataPartidaStr = jogo.getDataPartida() != null ? jogo.getDataPartida().toString() : "-";
            this.horaPartida = jogo.getHoraPartida() != null ? jogo.getHoraPartida() : "-";
            this.tempoDeJogo = calculateTempoDeJogo(jogo);
        }       
       

		private String getStatusLabel(StatusJogo status) {
            switch (status) {
                case NAO_INICIADO:
                    return "Não Iniciado";
                case EM_ANDAMENTO:
                    return "Em Andamento";
                case FINALIZADO:
                    return "Finalizado";
                default:
                    return status.toString();
            }
        }

        // Getters e Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTimeA() { return timeA; }
        public void setTimeA(String timeA) { this.timeA = timeA; }
        
        public String getTimeB() { return timeB; }
        public void setTimeB(String timeB) { this.timeB = timeB; }
        
        public Integer getPlacarA() { return placarA; }
        public void setPlacarA(Integer placarA) { this.placarA = placarA; }
        
        public Integer getPlacarB() { return placarB; }
        public void setPlacarB(Integer placarB) { this.placarB = placarB; }
        
        public String getStatusLabel() { return statusLabel; }
        public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
        
        public String getDataPartidaStr() { return dataPartidaStr; }
        public void setDataPartidaStr(String dataPartidaStr) { this.dataPartidaStr = dataPartidaStr; }
        
        public String getHoraPartida() { return horaPartida; }
        public void setHoraPartida(String horaPartida) { this.horaPartida = horaPartida; }
        
        public Integer getTempoDeJogo() { return tempoDeJogo; }
        public void setTempoDeJogo(Integer tempoDeJogo) { this.tempoDeJogo = tempoDeJogo; }
        
        
        private Integer calculateTempoDeJogo(JogoDTO jogo) {
        	if (jogo.getStatus() == StatusJogo.NAO_INICIADO) {
                return 0;
            }
        	
        	try {
        	
        		// Verifica se temos os dados necessários
                if (PublisherUtils.nuloOuVazio(jogo.getDataPartida()) || PublisherUtils.nuloOuVazio(jogo.getHoraPartida())) {
                    return jogo.getTempoDeJogo() != null ? jogo.getTempoDeJogo() : 0;
                }
                
	        	LocalDateTime dataHoraInicio = PublisherUtils.construirDataHoraPartida(jogo.getDataPartida(), jogo.getHoraPartida());
	        	LocalDateTime agora = LocalDateTime.now();
	            
	            // Se o jogo ainda não começou (data/hora futura), retorna 0
	            if (dataHoraInicio.isAfter(agora)) {
	                return 0;
	            }          
                
                // Calcula a duração em minutos
                Duration duracao = Duration.between(dataHoraInicio, agora);
                long minutosDecorridos = duracao.toMinutes();
                
                // Para jogos finalizados, usa a data de encerramento se disponível
                if (jogo.getStatus() == StatusJogo.FINALIZADO && !PublisherUtils.nuloOuVazio(jogo.getDataHoraEncerramento())) {
                    Duration duracaoFinal = Duration.between(dataHoraInicio, jogo.getDataHoraEncerramento());
                    minutosDecorridos = duracaoFinal.toMinutes();
                }
                
                return (int) Math.max(0, minutosDecorridos);
                
            } catch (Exception e) {
            	return jogo.getTempoDeJogo() != null ? jogo.getTempoDeJogo() : 0;
            }
		}
        
    }
   
}