package br.com.solides.placar.wicket.components.modal;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.util.DateTimeConstants;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Modal para confirmar exclusão de jogo.
 * Exibe dados do jogo e solicita confirmação.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Slf4j
public class ExcluirJogoModal extends Panel {

    private static final long serialVersionUID = 1L;

    @Inject
    private JogoService jogoService;

    private JogoDTO jogoParaExcluir;
    private Form<Void> form;

    /**
     * Callback executado após exclusão bem-sucedida
     */
    private ModalCallback onSuccess;

    public interface ModalCallback extends Serializable {
        void onSuccess(AjaxRequestTarget target, JogoDTO jogo);
        void onError(AjaxRequestTarget target, String mensagem);
        void onCancel(AjaxRequestTarget target);
    }

    public ExcluirJogoModal(String id, ModalCallback callback) {
        super(id);
        this.onSuccess = callback;
        
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
               
        initializeComponents();
    }

  

    private void initializeComponents() {
        // Form principal
        form = new Form<>("formExcluirJogo");
        
        // Labels para dados do jogo (dentro do form)
        form.add(new Label("jogoId", new Model<String>() {
            @Override
            public String getObject() {
                return jogoParaExcluir != null ? jogoParaExcluir.getId().toString() : "-";
            }
        }));
        
        form.add(new Label("jogoTimes", new Model<String>() {
            @Override
            public String getObject() {
                return jogoParaExcluir != null ? 
                    jogoParaExcluir.getTimeA() + " vs " + jogoParaExcluir.getTimeB() : "-";
            }
        }));
        
        form.add(new Label("jogoPlacar", new Model<String>() {
            @Override
            public String getObject() {
                return jogoParaExcluir != null ? 
                    jogoParaExcluir.getPlacarA() + " x " + jogoParaExcluir.getPlacarB() : "-";
            }
        }));
        
        form.add(new Label("jogoStatus", new Model<String>() {
            @Override
            public String getObject() {
                return jogoParaExcluir != null ? getStatusLabel(jogoParaExcluir.getStatus()) : "-";
            }
        }));
        
        form.add(new Label("jogoDataHora", new Model<String>() {
            @Override
            public String getObject() {
                return (jogoParaExcluir != null && jogoParaExcluir.getDataPartida() != null && jogoParaExcluir.getHoraPartida() != null) ? 
                    java.time.LocalDateTime.of(jogoParaExcluir.getDataPartida(), java.time.LocalTime.parse(jogoParaExcluir.getHoraPartida(), DateTimeConstants.TIME_FORMAT)).format(DateTimeConstants.DATETIME_BR_FORMAT) : "-";
            }
        }));
        
        // Botões
        adicionarBotoes();
        
        add(form);
    }

    private void adicionarBotoes() {
        // Botão Confirmar
        AjaxButton botaoConfirmar = new AjaxButton("botaoConfirmar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (jogoParaExcluir == null) {
                    if (onSuccess != null) {
                        onSuccess.onError(target, "Nenhum jogo selecionado para exclusão");
                    }
                    return;
                }
                
                try {
                    log.info("Excluindo jogo ID: {}", jogoParaExcluir.getId());
                    
                    // Excluir jogo via service
                    jogoService.deletarJogo(jogoParaExcluir.getId());
                    
                    // Callback de sucesso
                    if (onSuccess != null) {
                        onSuccess.onSuccess(target, jogoParaExcluir);
                    }
                    
                    log.info("Jogo excluído com sucesso: ID {}", jogoParaExcluir.getId());
                    
                    // Limpar jogo
                    jogoParaExcluir = null;
                    
                } catch (Exception e) {
                    log.error("Erro ao excluir jogo", e);
                    
                    if (onSuccess != null) {
                        onSuccess.onError(target, "Erro ao excluir jogo: " + e.getMessage());
                    }
                }
            }
        };
        form.add(botaoConfirmar);

        // Botão Cancelar
        AjaxButton botaoCancelar = new AjaxButton("botaoCancelar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                log.debug("Cancelando exclusão de jogo");
                
                // Limpar jogo
                jogoParaExcluir = null;
                
                if (onSuccess != null) {
                    onSuccess.onCancel(target);
                }
            }
        };
        botaoCancelar.setDefaultFormProcessing(false);
        form.add(botaoCancelar);
    }

    /**
     * Define o jogo a ser excluído
     */
    public void setJogoParaExcluir(JogoDTO jogo) {
        this.jogoParaExcluir = jogo;
    }

    private String getStatusLabel(br.com.solides.placar.shared.enums.StatusJogo status) {
        if (status == null) return "-";
        
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
}