package br.com.solides.placar.wicket.components.modal;

import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.shared.dto.JogoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import jakarta.inject.Inject;
import jakarta.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;

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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
        
        // Garantir que o serviço esteja injetado
        initializeService();
        
        initializeComponents();
    }

    /**
     * Inicializa o serviço, utilizando CDI ou fallback para JNDI
     */
    private void initializeService() {
        if (jogoService == null) {
            log.warn("JogoService não foi injetado via CDI em ExcluirJogoModal, tentando recuperar via CDI programático");
            
            try {
                // Tentar via CDI programático
                jogoService = CDI.current().select(JogoService.class).get();
                log.info("JogoService recuperado via CDI programático em ExcluirJogoModal");
            } catch (Exception e) {
                log.warn("Falha ao recuperar JogoService via CDI programático em ExcluirJogoModal: {}", e.getMessage());
                
                try {
                    // Fallback para JNDI lookup
                    InitialContext ctx = new InitialContext();
                    jogoService = (JogoService) ctx.lookup("java:app/JogoService");
                    log.info("JogoService recuperado via JNDI lookup em ExcluirJogoModal");
                } catch (NamingException ne) {
                    log.error("Falha ao recuperar JogoService via JNDI em ExcluirJogoModal: {}", ne.getMessage());
                    throw new RuntimeException("Não foi possível injetar JogoService em ExcluirJogoModal", ne);
                }
            }
        } else {
            log.debug("JogoService injetado corretamente via CDI em ExcluirJogoModal");
        }
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
                return jogoParaExcluir != null ? 
                    jogoParaExcluir.getDataHoraPartida().format(DATE_FORMATTER) : "-";
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