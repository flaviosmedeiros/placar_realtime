package br.com.solides.placar.wicket.components.modal;

import java.io.Serializable;
import java.time.LocalDate;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.form.datetime.LocalDateTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

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
               
        initializeComponents();
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
        LocalDateTextField campoDataPartida = new LocalDateTextField("dataPartida", "yyyy-MM-dd");
        campoDataPartida.setLabel(Model.of("Data da Partida"));
        campoDataPartida.add(AttributeModifier.replace("type", "date"));
        campoDataPartida.setRequired(true);
        form.add(campoDataPartida);

        // Hora da Partida
        TextField<String> campoHoraPartida = new TextField<>("horaPartidaStr");
        campoHoraPartida.setLabel(Model.of("Hora da Partida"));
        campoHoraPartida.setRequired(true);
        campoHoraPartida.add(AttributeModifier.replace("type", "time"));
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

        private String timeA;
        private String timeB;
        private LocalDate dataPartida;
        private String horaPartidaStr;

        public CriarJogoForm() {
            // Inicializar com data/hora atual como padrão
            this.dataPartida = LocalDate.now().plusDays(1);
            this.horaPartidaStr = "00:00";
        }

        public CriarJogoDTO toCriarJogoDTO() {
            try {
               return CriarJogoDTO.builder()
                        .timeA(timeA)
                        .timeB(timeB)
                        .dataPartida(dataPartida)
                        .horaPartida(horaPartidaStr)
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
        
        public LocalDate getDataPartida() { return dataPartida; }
        public void setDataPartida(LocalDate dataPartida) { this.dataPartida = dataPartida; }
        
        public String getHoraPartidaStr() { return horaPartidaStr; }
        public void setHoraPartidaStr(String horaPartidaStr) { this.horaPartidaStr = horaPartidaStr; }
    }
}