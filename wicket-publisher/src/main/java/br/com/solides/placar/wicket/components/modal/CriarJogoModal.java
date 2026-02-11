package br.com.solides.placar.wicket.components.modal;

import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;

import jakarta.inject.Inject;
import jakarta.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Date;

/**
 * Modal para criar um novo jogo.
 * Contém form com validação e integração AJAX.
 * 
 * @author Copilot  
 * @since 1.0.0
 */
@Slf4j
public class CriarJogoModal extends Panel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Inject
    private JogoService jogoService;

    private CriarJogoForm formData;
    private Form<CriarJogoForm> form;

    /**
     * Callback executado após criação bem-sucedida
     */
    private ModalCallback onSuccess;

    public interface ModalCallback extends Serializable {
        void onSuccess(AjaxRequestTarget target, JogoDTO jogo);
        void onError(AjaxRequestTarget target, String mensagem);
        void onCancel(AjaxRequestTarget target);
    }

    public CriarJogoModal(String id, ModalCallback callback) {
        super(id);
        this.onSuccess = callback;
        this.formData = new CriarJogoForm();
        
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
            log.warn("JogoService não foi injetado via CDI em CriarJogoModal, tentando recuperar via CDI programático");
            
            try {
                // Tentar via CDI programático
                jogoService = CDI.current().select(JogoService.class).get();
                log.info("JogoService recuperado via CDI programático em CriarJogoModal");
            } catch (Exception e) {
                log.warn("Falha ao recuperar JogoService via CDI programático em CriarJogoModal: {}", e.getMessage());
                
                try {
                    // Fallback para JNDI lookup
                    InitialContext ctx = new InitialContext();
                    jogoService = (JogoService) ctx.lookup("java:app/JogoService");
                    log.info("JogoService recuperado via JNDI lookup em CriarJogoModal");
                } catch (NamingException ne) {
                    log.error("Falha ao recuperar JogoService via JNDI em CriarJogoModal: {}", ne.getMessage());
                    throw new RuntimeException("Não foi possível injetar JogoService em CriarJogoModal", ne);
                }
            }
        } else {
            log.debug("JogoService injetado corretamente via CDI em CriarJogoModal");
        }
    }

    private void initializeComponents() {
        // Form principal
        form = new Form<>("formCriarJogo", new CompoundPropertyModel<>(formData));
        
        // Campos do formulário
        adicionarCampos();
        
        // Botões
        adicionarBotoes();
        
        add(form);
    }

    private void adicionarCampos() {
        // Time A
        RequiredTextField<String> campoTimeA = new RequiredTextField<>("timeA");
        campoTimeA.setLabel(Model.of("Time A"));
        form.add(campoTimeA);

        // Time B
        RequiredTextField<String> campoTimeB = new RequiredTextField<>("timeB");
        campoTimeB.setLabel(Model.of("Time B"));
        form.add(campoTimeB);

        // Data da Partida
        TextField<String> campoDataPartida = new TextField<>("dataPartidaStr");
        campoDataPartida.setLabel(Model.of("Data da Partida"));
        campoDataPartida.setRequired(true);
        campoDataPartida.add(new PatternValidator("\\d{4}-\\d{2}-\\d{2}"));
        form.add(campoDataPartida);

        // Hora da Partida
        TextField<String> campoHoraPartida = new TextField<>("horaPartidaStr");
        campoHoraPartida.setLabel(Model.of("Hora da Partida"));
        campoHoraPartida.setRequired(true);
        campoHoraPartida.add(new PatternValidator("\\d{2}:\\d{2}"));
        form.add(campoHoraPartida);
    }

    private void adicionarBotoes() {
        // Botão Salvar
        AjaxButton botaoSalvar = new AjaxButton("botaoSalvar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                try {
                    log.info("Salvando novo jogo: {} vs {}", formData.getTimeA(), formData.getTimeB());
                    
                    // Converter form data para DTO
                    CriarJogoDTO criarDTO = formData.toCriarJogoDTO();
                    
                    // Criar jogo via service
                    JogoDTO novoJogo = jogoService.criarJogo(criarDTO);
                    
                    // Callback de sucesso
                    if (onSuccess != null) {
                        onSuccess.onSuccess(target, novoJogo);
                    }
                    
                    // Limpar form
                    formData = new CriarJogoForm();
                    form.setModelObject(formData);
                    
                    log.info("Jogo criado com sucesso: ID {}", novoJogo.getId());
                    
                } catch (Exception e) {
                    log.error("Erro ao criar jogo", e);
                    
                    if (onSuccess != null) {
                        onSuccess.onError(target, "Erro ao criar jogo: " + e.getMessage());
                    }
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                log.warn("Erro de validação no form de criar jogo");
                
                if (onSuccess != null) {
                    onSuccess.onError(target, "Verifique os dados informados");
                }
            }
        };
        form.add(botaoSalvar);

        // Botão Cancelar
        AjaxButton botaoCancelar = new AjaxButton("botaoCancelar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                log.debug("Cancelando criação de jogo");
                
                // Limpar form
                formData = new CriarJogoForm();
                form.setModelObject(formData);
                
                if (onSuccess != null) {
                    onSuccess.onCancel(target);
                }
            }
        };
        botaoCancelar.setDefaultFormProcessing(false);
        form.add(botaoCancelar);
    }

    /**
     * Classe interna para dados do formulário
     */
    public static class CriarJogoForm implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

        private String timeA;
        private String timeB;
        private String dataPartidaStr;
        private String horaPartidaStr;

        public CriarJogoForm() {
            // Inicializar com data atual e hora atual + 1 hora
            LocalDateTime dataDefault = LocalDateTime.now().plusHours(1);
            this.dataPartidaStr = dataDefault.format(DATE_FORMATTER);
            this.horaPartidaStr = dataDefault.format(TIME_FORMATTER);
        }

        public CriarJogoDTO toCriarJogoDTO() {
            try {
                LocalDate data = LocalDate.parse(dataPartidaStr, DATE_FORMATTER);
                LocalTime hora = LocalTime.parse(horaPartidaStr, TIME_FORMATTER);
                LocalDateTime dataHoraPartida = LocalDateTime.of(data, hora);
                
                return CriarJogoDTO.builder()
                        .timeA(timeA)
                        .timeB(timeB)
                        .dataHoraPartida(dataHoraPartida)
                        .build();
            } catch (Exception e) {
                throw new IllegalArgumentException("Formato de data ou hora inválido. Use YYYY-MM-DD para data e HH:MM para hora.", e);
            }
        }

        // Getters e Setters
        public String getTimeA() { return timeA; }
        public void setTimeA(String timeA) { this.timeA = timeA; }
        
        public String getTimeB() { return timeB; }
        public void setTimeB(String timeB) { this.timeB = timeB; }
        
        public String getDataPartidaStr() { return dataPartidaStr; }
        public void setDataPartidaStr(String dataPartidaStr) { this.dataPartidaStr = dataPartidaStr; }
        
        public String getHoraPartidaStr() { return horaPartidaStr; }
        public void setHoraPartidaStr(String horaPartidaStr) { this.horaPartidaStr = horaPartidaStr; }
    }
}