package br.com.solides.placar.wicket.components.modal;

import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Modal para editar um jogo existente.
 * Permite alterar todos os campos conforme regras de negócio.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Slf4j
public class EditarJogoModal extends Panel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Inject
    private JogoService jogoService;

    private EditarJogoForm formData;
    private Form<EditarJogoForm> form;

    /**
     * Callback executado após edição bem-sucedida
     */
    private ModalCallback onSuccess;

    public interface ModalCallback extends Serializable {
        void onSuccess(AjaxRequestTarget target, JogoDTO jogo);
        void onError(AjaxRequestTarget target, String mensagem);
        void onCancel(AjaxRequestTarget target);
    }

    public EditarJogoModal(String id, ModalCallback callback) {
        super(id);
        this.onSuccess = callback;
        this.formData = new EditarJogoForm();
        
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
            log.warn("JogoService não foi injetado via CDI em EditarJogoModal, tentando recuperar via CDI programático");
            
            try {
                // Tentar via CDI programático
                jogoService = CDI.current().select(JogoService.class).get();
                log.info("JogoService recuperado via CDI programático em EditarJogoModal");
            } catch (Exception e) {
                log.warn("Falha ao recuperar JogoService via CDI programático em EditarJogoModal: {}", e.getMessage());
                
                try {
                    // Fallback para JNDI lookup
                    InitialContext ctx = new InitialContext();
                    jogoService = (JogoService) ctx.lookup("java:app/JogoService");
                    log.info("JogoService recuperado via JNDI lookup em EditarJogoModal");
                } catch (NamingException ne) {
                    log.error("Falha ao recuperar JogoService via JNDI em EditarJogoModal: {}", ne.getMessage());
                    throw new RuntimeException("Não foi possível injetar JogoService em EditarJogoModal", ne);
                }
            }
        } else {
            log.debug("JogoService injetado corretamente via CDI em EditarJogoModal");
        }
    }

    private void initializeComponents() {
        // Form principal
        form = new Form<>("formEditarJogo", new CompoundPropertyModel<>(formData));
        
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

        // Placar A
        NumberTextField<Integer> campoPlacarA = new NumberTextField<>("placarA");
        campoPlacarA.setMinimum(0);
        campoPlacarA.setLabel(Model.of("Placar A"));
        form.add(campoPlacarA);

        // Placar B
        NumberTextField<Integer> campoPlacarB = new NumberTextField<>("placarB");
        campoPlacarB.setMinimum(0);
        campoPlacarB.setLabel(Model.of("Placar B"));
        form.add(campoPlacarB);

        // Status
        List<StatusJogo> statusOptions = Arrays.asList(StatusJogo.values());
        DropDownChoice<StatusJogo> campoStatus = new DropDownChoice<>("status", statusOptions);
        campoStatus.setRequired(true);
        form.add(campoStatus);

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
                    log.info("Atualizando jogo ID: {}", formData.getId());
                    
                    // Converter form data para DTO
                    JogoDTO jogoDTO = formData.toJogoDTO();
                    
                    // Atualizar jogo via service
                    JogoDTO jogoAtualizado = jogoService.atualizarJogo(jogoDTO);
                    
                    // Callback de sucesso
                    if (onSuccess != null) {
                        onSuccess.onSuccess(target, jogoAtualizado);
                    }
                    
                    log.info("Jogo atualizado com sucesso: ID {}", jogoAtualizado.getId());
                    
                } catch (Exception e) {
                    log.error("Erro ao atualizar jogo", e);
                    
                    if (onSuccess != null) {
                        onSuccess.onError(target, "Erro ao atualizar jogo: " + e.getMessage());
                    }
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                log.warn("Erro de validação no form de editar jogo");
                
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
                log.debug("Cancelando edição de jogo");
                
                if (onSuccess != null) {
                    onSuccess.onCancel(target);
                }
            }
        };
        botaoCancelar.setDefaultFormProcessing(false);
        form.add(botaoCancelar);
    }

    /**
     * Carrega dados do jogo no formulário
     */
    public void carregarJogo(JogoDTO jogo) {
        formData = EditarJogoForm.fromJogoDTO(jogo);
        form.setModelObject(formData);
    }

    /**
     * Classe interna para dados do formulário
     */
    public static class EditarJogoForm implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

        private Long id;
        private String timeA;
        private String timeB;
        private Integer placarA;
        private Integer placarB;
        private StatusJogo status;
        private String dataPartidaStr;
        private String horaPartidaStr;
        private LocalDateTime dataCriacao;
        private LocalDateTime dataAtualizacao;

        public EditarJogoForm() {}

        public static EditarJogoForm fromJogoDTO(JogoDTO jogo) {
            EditarJogoForm form = new EditarJogoForm();
            form.id = jogo.getId();
            form.timeA = jogo.getTimeA();
            form.timeB = jogo.getTimeB();
            form.placarA = jogo.getPlacarA();
            form.placarB = jogo.getPlacarB();
            form.status = jogo.getStatus();
            form.dataCriacao = jogo.getDataCriacao();
            form.dataAtualizacao = jogo.getDataAtualizacao();
            
            // Separar dataHoraPartida em data e hora
            if (jogo.getDataHoraPartida() != null) {
                LocalDateTime dataHora = jogo.getDataHoraPartida();
                form.dataPartidaStr = dataHora.format(DATE_FORMATTER);
                form.horaPartidaStr = dataHora.format(TIME_FORMATTER);
            }
            
            return form;
        }

        public JogoDTO toJogoDTO() {
            try {
                LocalDate data = LocalDate.parse(dataPartidaStr, DATE_FORMATTER);
                LocalTime hora = LocalTime.parse(horaPartidaStr, TIME_FORMATTER);
                LocalDateTime dataHoraPartida = LocalDateTime.of(data, hora);
                
                return JogoDTO.builder()
                        .id(id)
                        .timeA(timeA)
                        .timeB(timeB)
                        .placarA(placarA)
                        .placarB(placarB)
                        .status(status)
                        .dataHoraPartida(dataHoraPartida)
                        .dataCriacao(dataCriacao)
                        .dataAtualizacao(dataAtualizacao)
                        .build();
            } catch (Exception e) {
                throw new IllegalArgumentException("Formato de data ou hora inválido. Use YYYY-MM-DD para data e HH:MM para hora.", e);
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
        
        public StatusJogo getStatus() { return status; }
        public void setStatus(StatusJogo status) { this.status = status; }
        
        public String getDataPartidaStr() { return dataPartidaStr; }
        public void setDataPartidaStr(String dataPartidaStr) { this.dataPartidaStr = dataPartidaStr; }
        
        public String getHoraPartidaStr() { return horaPartidaStr; }
        public void setHoraPartidaStr(String horaPartidaStr) { this.horaPartidaStr = horaPartidaStr; }
        
        public LocalDateTime getDataCriacao() { return dataCriacao; }
        public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
        
        public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
        public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
    }
}