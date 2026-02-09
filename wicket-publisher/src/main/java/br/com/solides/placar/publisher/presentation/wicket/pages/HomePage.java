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
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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
        log.info("=== Iniciando construção da HomePage ===");
        
        try {
            log.debug("Verificando injeção de dependências...");
            if (listarJogosUseCase == null) {
                log.error("ERRO CRÍTICO: listarJogosUseCase NÃO foi injetado!");
                throw new IllegalStateException("ListarJogosUseCase não foi injetado");
            }
            if (criarJogoUseCase == null) {
                log.error("ERRO CRÍTICO: criarJogoUseCase NÃO foi injetado!");
                throw new IllegalStateException("CriarJogoUseCase não foi injetado");
            }
            log.debug("Injeção de dependências OK");
            
            // Título da página
            log.debug("Adicionando título da página...");
            add(new Label("pageTitle", "Placar Realtime - Administração"));
            log.debug("Título adicionado com sucesso");

            // Modelo para filtro de status
            log.debug("Criando modelo de filtro de status...");
            filtroStatusModel = Model.of();
            log.debug("Modelo de filtro criado");

            // Modelo de lista de jogos
            log.debug("Criando modelo LoadableDetachableModel para lista de jogos...");
            IModel<List<JogoDTO>> jogosModel = new LoadableDetachableModel<List<JogoDTO>>() {
                @Override
                protected List<JogoDTO> load() {
                    log.trace("LoadableDetachableModel.load() chamado");
                    try {
                        StatusJogo filtro = filtroStatusModel.getObject();
                        log.debug("Carregando jogos com filtro: {}", filtro);
                        
                        List<JogoDTO> jogos;
                        if (filtro != null) {
                            jogos = listarJogosUseCase.executar(filtro);
                        } else {
                            jogos = listarJogosUseCase.executar();
                        }
                        
                        log.info("Jogos carregados com sucesso. Total: {}", jogos != null ? jogos.size() : 0);
                        return jogos;
                    } catch (Exception e) {
                        log.error("ERRO ao carregar lista de jogos: {}", e.getMessage(), e);
                        throw new RuntimeException("Erro ao carregar jogos", e);
                    }
                }
            };
            log.debug("Modelo de jogos criado");

            // Formulário de filtro
            log.debug("Criando formulário de filtro...");
            Form<Void> filtroForm = new Form<>("filtroForm");
            add(filtroForm);
            log.debug("Formulário de filtro adicionado");

            // Dropdown de filtro por status
            log.debug("Criando dropdown de status...");
            DropDownChoice<StatusJogo> statusChoice = new DropDownChoice<>(
                "statusFilter",
                filtroStatusModel,
                Arrays.asList(StatusJogo.values())
            );
            statusChoice.setNullValid(true);
            filtroForm.add(statusChoice);
            log.debug("Dropdown de status adicionado");

            // Botão de filtrar
            log.debug("Criando botão de filtrar...");
            AjaxButton filtrarButton = new AjaxButton("filtrarButton") {
                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    log.info("Botão filtrar clicado. Filtro: {}", filtroStatusModel.getObject());
                    target.add(jogoListPanel);
                }
            };
            filtroForm.add(filtrarButton);
            log.debug("Botão filtrar adicionado");

            // Botão de limpar filtro
            log.debug("Criando botão de limpar filtro...");
            AjaxButton limparFiltroButton = new AjaxButton("limparFiltroButton") {
                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    log.info("Botão limpar filtro clicado");
                    filtroStatusModel.setObject(null);
                    target.add(jogoListPanel);
                }
            };
            filtroForm.add(limparFiltroButton);
            log.debug("Botão limpar filtro adicionado");

            // Painel de criar jogo
            log.debug("Criando CriarJogoPanel...");
            criarJogoPanel = new CriarJogoPanel("criarJogoPanel", criarJogoUseCase) {
                @Override
            protected void onJogoCriado(AjaxRequestTarget target) {
                log.info("Callback onJogoCriado chamado");
                target.add(jogoListPanel);
            }
        };
        criarJogoPanel.setOutputMarkupId(true);
        add(criarJogoPanel);
        log.debug("CriarJogoPanel adicionado");

        // Painel de lista de jogos
        log.debug("Criando JogoListPanel...");
        jogoListPanel = new JogoListPanel("jogoListPanel", jogosModel);
        jogoListPanel.setOutputMarkupId(true);
        add(jogoListPanel);
        log.debug("JogoListPanel adicionado");

        // Auto-refresh a cada 5 segundos
        log.debug("Configurando auto-refresh de 5 segundos...");
        jogoListPanel.add(new AjaxSelfUpdatingTimerBehavior(Duration.ofSeconds(5)));
        log.debug("Auto-refresh configurado");
        
        log.info("=== HomePage construída com SUCESSO ===");
        
        } catch (Exception e) {
            log.error("=== ERRO FATAL na construção da HomePage ===", e);
            log.error("Tipo do erro: {}", e.getClass().getName());
            log.error("Mensagem: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Causa raiz: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Falha ao construir HomePage", e);
        }
    }
}