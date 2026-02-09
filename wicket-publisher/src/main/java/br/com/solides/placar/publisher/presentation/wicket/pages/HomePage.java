package br.com.solides.placar.publisher.presentation.wicket.pages;

import br.com.solides.placar.publisher.application.usecase.CriarJogoUseCase;
import br.com.solides.placar.publisher.application.usecase.ListarJogosUseCase;
import br.com.solides.placar.publisher.presentation.wicket.panels.CriarJogoPanel;
import br.com.solides.placar.publisher.presentation.wicket.panels.JogoListPanel;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import jakarta.inject.Inject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Página principal da aplicação Wicket.
 * Exibe lista de jogos e permite criar novos jogos.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;

    @Inject
    private ListarJogosUseCase listarJogosUseCase;

    @Inject
    private CriarJogoUseCase criarJogoUseCase;

    private JogoListPanel jogoListPanel;
    private CriarJogoPanel criarJogoPanel;
    private IModel<StatusJogo> filtroStatusModel;

    public HomePage() {
        // Título da página
        add(new Label("pageTitle", "Placar Realtime - Administração"));

        // Modelo para filtro de status
        filtroStatusModel = Model.of();

        // Modelo de lista de jogos
        IModel<List<JogoDTO>> jogosModel = new LoadableDetachableModel<List<JogoDTO>>() {
            @Override
            protected List<JogoDTO> load() {
                StatusJogo filtro = filtroStatusModel.getObject();
                if (filtro != null) {
                    return listarJogosUseCase.executar(filtro);
                } else {
                    return listarJogosUseCase.executar();
                }
            }
        };

        // Formulário de filtro
        Form<Void> filtroForm = new Form<>("filtroForm");
        add(filtroForm);

        // Dropdown de filtro por status
        DropDownChoice<StatusJogo> statusChoice = new DropDownChoice<>(
            "statusFilter",
            filtroStatusModel,
            Arrays.asList(StatusJogo.values())
        );
        statusChoice.setNullValid(true);
        filtroForm.add(statusChoice);

        // Botão de filtrar
        AjaxButton filtrarButton = new AjaxButton("filtrarButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                target.add(jogoListPanel);
            }
        };
        filtroForm.add(filtrarButton);

        // Botão de limpar filtro
        AjaxButton limparFiltroButton = new AjaxButton("limparFiltroButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                filtroStatusModel.setObject(null);
                target.add(jogoListPanel);
            }
        };
        filtroForm.add(limparFiltroButton);

        // Painel de criar jogo
        criarJogoPanel = new CriarJogoPanel("criarJogoPanel", criarJogoUseCase) {
            @Override
            protected void onJogoCriado(AjaxRequestTarget target) {
                target.add(jogoListPanel);
            }
        };
        criarJogoPanel.setOutputMarkupId(true);
        add(criarJogoPanel);

        // Painel de lista de jogos
        jogoListPanel = new JogoListPanel("jogoListPanel", jogosModel);
        jogoListPanel.setOutputMarkupId(true);
        add(jogoListPanel);

        // Auto-refresh a cada 5 segundos
        jogoListPanel.add(new AjaxSelfUpdatingTimerBehavior(Duration.ofSeconds(5)));
    }
}