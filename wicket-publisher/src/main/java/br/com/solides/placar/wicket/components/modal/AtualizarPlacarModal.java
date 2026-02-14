package br.com.solides.placar.wicket.components.modal;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.shared.dto.JogoDTO;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Modal para atualizar placar do jogo.
 * Permite alterar os placares dos times A e B.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Slf4j
public class AtualizarPlacarModal extends Panel {

    private static final long serialVersionUID = 1L;

    @Inject
    private JogoService jogoService;

    private ModalCallback callback;
    private JogoDTO jogoParaAtualizar;
    private AtualizarPlacarForm placarForm;
    private Form<AtualizarPlacarForm> form;

    public interface ModalCallback extends Serializable {
        void onSuccess(AjaxRequestTarget target, JogoDTO jogo);
        void onError(AjaxRequestTarget target, String mensagem);
        void onCancel(AjaxRequestTarget target);
    }

    public AtualizarPlacarModal(String id, ModalCallback callback) {
        super(id);
        this.callback = callback;
        
        placarForm = new AtualizarPlacarForm();
        
        initializeComponents();
        
        setOutputMarkupId(true);
    }

    private void initializeComponents() {
        // Form principal
        form = new Form<>("formAtualizarPlacar", new CompoundPropertyModel<>(placarForm));

        // Labels para os nomes dos times
        Label labelTimeA = new Label("timeA", new Model<String>() {
            @Override
            public String getObject() {
                return placarForm.getTimeA() != null ? placarForm.getTimeA() : "Time A";
            }
        });
        labelTimeA.setOutputMarkupId(true);
        form.add(labelTimeA);

        Label labelTimeB = new Label("timeB", new Model<String>() {
            @Override
            public String getObject() {
                return placarForm.getTimeB() != null ? placarForm.getTimeB() : "Time B";
            }
        });
        labelTimeB.setOutputMarkupId(true);
        form.add(labelTimeB);

        // Campo Placar A
        NumberTextField<Integer> campoPlacarA = new NumberTextField<>("placarA");
        campoPlacarA.setRequired(true);
        campoPlacarA.setMinimum(0);
        campoPlacarA.setLabel(Model.of("Placar Time A"));
        form.add(campoPlacarA);

        // Campo Placar B
        NumberTextField<Integer> campoPlacarB = new NumberTextField<>("placarB");
        campoPlacarB.setRequired(true);
        campoPlacarB.setMinimum(0);
        campoPlacarB.setLabel(Model.of("Placar Time B"));
        form.add(campoPlacarB);

        // Botões
        adicionarBotoes();
        
        add(form);
    }

    private void adicionarBotoes() {
        // Botão Salvar
        AjaxButton botaoSalvar = new AjaxButton("botaoSalvar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                try {
                    log.info("Atualizando placar do jogo ID: {}", jogoParaAtualizar.getId());
                    
                    // Usar método específico para atualizar placar
                    JogoDTO jogoAtualizado = jogoService.atualizarPlacar(
                        jogoParaAtualizar.getId(), 
                        placarForm.getPlacarA(), 
                        placarForm.getPlacarB()
                    );
                    
                    log.info("Placar atualizado com sucesso: {} x {}", 
                        placarForm.getPlacarA(), placarForm.getPlacarB());
                    
                    callback.onSuccess(target, jogoAtualizado);
                    
                } catch (Exception e) {
                    log.error("Erro ao atualizar placar", e);
                    callback.onError(target, "Erro ao atualizar placar: " + e.getMessage());
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                log.warn("Erro de validação no formulário de placar");
                callback.onError(target, "Dados inválidos. Verifique os campos e tente novamente.");
            }
        };
        form.add(botaoSalvar);

        // Botão Cancelar
        AjaxButton botaoCancelar = new AjaxButton("botaoCancelar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                callback.onCancel(target);
            }
        };
        botaoCancelar.setDefaultFormProcessing(false);
        form.add(botaoCancelar);
    }

    /**
     * Carrega o jogo para atualização de placar
     */
    public void carregarJogo(JogoDTO jogo) {
        this.jogoParaAtualizar = jogo;
        
        // Preencher form com dados atuais
        placarForm.setPlacarA(jogo.getPlacarA());
        placarForm.setPlacarB(jogo.getPlacarB());
        placarForm.setTimeA(jogo.getTimeA());
        placarForm.setTimeB(jogo.getTimeB());
        
        form.modelChanged();
    }

    /**
     * Classe interna para representar o formulário de placar
     */
    public static class AtualizarPlacarForm implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Integer placarA = 0;
        private Integer placarB = 0;
        private String timeA;
        private String timeB;

        // Getters e Setters
        public Integer getPlacarA() {
            return placarA;
        }

        public void setPlacarA(Integer placarA) {
            this.placarA = placarA;
        }

        public Integer getPlacarB() {
            return placarB;
        }

        public void setPlacarB(Integer placarB) {
            this.placarB = placarB;
        }

        public String getTimeA() {
            return timeA;
        }

        public void setTimeA(String timeA) {
            this.timeA = timeA;
        }

        public String getTimeB() {
            return timeB;
        }

        public void setTimeB(String timeB) {
            this.timeB = timeB;
        }
    }
}