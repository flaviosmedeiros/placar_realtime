package br.com.solides.placar.publisher.presentation.wicket.panels;

import br.com.solides.placar.publisher.application.usecase.CriarJogoUseCase;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.StringValidator;

import java.time.LocalDateTime;

/**
 * Painel Wicket para criação de novos jogos.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public abstract class CriarJogoPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private final CriarJogoUseCase criarJogoUseCase;
    private FeedbackPanel feedbackPanel;

    public CriarJogoPanel(String id, CriarJogoUseCase criarJogoUseCase) {
        super(id);
        this.criarJogoUseCase = criarJogoUseCase;
        
        // Feedback panel
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        // Modelo do formulário
        CriarJogoDTO dto = new CriarJogoDTO();
        CompoundPropertyModel<CriarJogoDTO> model = new CompoundPropertyModel<>(dto);

        // Formulário
        Form<CriarJogoDTO> form = new Form<>("criarJogoForm", model);
        add(form);

        // Campo Time A
        TextField<String> timeAField = new TextField<>("timeA");
        timeAField.setRequired(true);
        timeAField.add(StringValidator.minimumLength(2));
        form.add(timeAField);

        // Campo Time B
        TextField<String> timeBField = new TextField<>("timeB");
        timeBField.setRequired(true);
        timeBField.add(StringValidator.minimumLength(2));
        form.add(timeBField);

        // Campo Data/Hora (usando TextField simples por simplicidade)
        // Em produção, usar DateTimePicker
        TextField<String> dataHoraField = new TextField<>("dataHoraStr", Model.of(""));
        dataHoraField.setRequired(false);
        form.add(dataHoraField);

        // Botão de submit
        AjaxButton submitButton = new AjaxButton("submitButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                try {
                    CriarJogoDTO formDto = form.getModelObject();
                    
                    // Se não informou data/hora, usar agora + 1 hora
                    if (formDto.getDataHoraInicioPartida() == null) {
                        formDto.setDataHoraInicioPartida(LocalDateTime.now().plusHours(1));
                    }
                    
                    // Criar jogo
                    criarJogoUseCase.execute(formDto);
                    
                    success("Jogo criado com sucesso!");
                    
                    // Limpar formulário
                    form.setModelObject(new CriarJogoDTO());
                    
                    // Callback customizado
                    onJogoCriado(target);
                    
                } catch (Exception e) {
                    error("Erro ao criar jogo: " + e.getMessage());
                }
                
                target.add(feedbackPanel);
                target.add(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(feedbackPanel);
            }
        };
        form.add(submitButton);
    }

    /**
     * Callback chamado quando um jogo é criado com sucesso.
     * Implementar na classe concreta para ações adicionais.
     */
    protected abstract void onJogoCriado(AjaxRequestTarget target);
}
