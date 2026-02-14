package br.com.solides.placar.wicket.pages.jogo;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.dto.JogoFilterDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.util.DateTimeConstants;
import br.com.solides.placar.wicket.BaseWebPage;
import br.com.solides.placar.wicket.components.modal.CriarJogoModal;
import br.com.solides.placar.wicket.components.modal.EditarJogoModal;
import br.com.solides.placar.wicket.components.modal.ExcluirJogoModal;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Página principal para CRUD de Jogos.
 * Contém filtros de busca, tabela de listagem e ações para criar/editar/excluir jogos.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Slf4j
public class JogoListPage extends BaseWebPage {

    private static final long serialVersionUID = 1L;

    @Inject
    private JogoService jogoService;

    // Modelo para filtros
    private JogoFilterDTO filtro = new JogoFilterDTO();    
    
    
    // Componentes para AJAX
    private WebMarkupContainer jogosContainer;
    // Lista de jogos para exibição
    private IModel<List<JogoDTO>> jogosListModel;
    private ListView<JogoDTO> jogosListView;
    
    
    // Modais
    private CriarJogoModal criarJogoModal;
    private EditarJogoModal editarJogoModal;
    private ExcluirJogoModal excluirJogoModal;
    
    private FeedbackPanel feedbackPanel;

    
    
   
    public JogoListPage() {
        this(new PageParameters());
    }

    public JogoListPage(final PageParameters parameters) {
        super(parameters);  
        
        jogosListModel = Model.ofList(new ArrayList<>());
      
        // Carregar jogos inicialmente
        carregarJogos();
        
        // Inicializar componentes
        initializeComponents();
    }

       
    private void initializeComponents() {
        // Títulos da página
        add(new Label("pageTitle", "Gerenciamento de Jogos"));
        add(new Label("headerTitle", "Gerenciamento de Jogos"));
        
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
        
        // Modais
        criarModais();
        
        // Form de filtros
        criarFormFiltros();
        
        // Container para tabela de jogos (para AJAX)
        criarTabelaJogos();
               
        // Botão adicionar
        criarBotaoAdicionar();
    }

    private void criarFormFiltros() {
        Form<JogoFilterDTO> formFiltros = new Form<>("formFiltros", 
            new CompoundPropertyModel<>(filtro));

        // Campo ID
        NumberTextField<Long> campoId = new NumberTextField<>("id");
        formFiltros.add(campoId);

        // Campo Time A
        TextField<String> campoTimeA = new TextField<>("timeA");
        formFiltros.add(campoTimeA);

        // Campo Time B
        TextField<String> campoTimeB = new TextField<>("timeB");
        formFiltros.add(campoTimeB);

        // Campo Placar A
        NumberTextField<Integer> campoPlacarA = new NumberTextField<>("placarA");
        formFiltros.add(campoPlacarA);

        // Campo Placar B
        NumberTextField<Integer> campoPlacarB = new NumberTextField<>("placarB");
        formFiltros.add(campoPlacarB);

        // Campo Status
        List<StatusJogo> statusOptions = Arrays.asList(StatusJogo.values());
        DropDownChoice<StatusJogo> campoStatus = new DropDownChoice<>("status", statusOptions);
        campoStatus.setNullValid(true);
        formFiltros.add(campoStatus);

        
        // Botão Buscar
        AjaxButton botaoBuscar = new AjaxButton("botaoBuscar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                log.debug("Aplicando filtros: {}", filtro);
                carregarJogos();
                target.add(jogosContainer);
                target.add(getFeedbackPanel());
            }
            
            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(getFeedbackPanel());
            }
        };
        formFiltros.add(botaoBuscar);

        
        // Botão Limpar
        AjaxButton botaoLimpar = new AjaxButton("botaoLimpar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                log.debug("Limpando filtros");

                filtro.setId(null);
                filtro.setTimeA(null);
                filtro.setTimeB(null);
                filtro.setPlacarA(null);
                filtro.setPlacarB(null);
                filtro.setStatus(null);
                formFiltros.modelChanged();

                carregarJogos();
                target.add(formFiltros);
                target.add(jogosContainer);
                target.add(getFeedbackPanel());
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(getFeedbackPanel());
            }
        };
        botaoLimpar.setDefaultFormProcessing(false);
        formFiltros.add(botaoLimpar);

        add(formFiltros);
    }

    private void criarTabelaJogos() {
        // Container para AJAX
        jogosContainer = new WebMarkupContainer("jogosContainer");
        jogosContainer.setOutputMarkupId(true);        
       

        // ListView para jogos
        jogosListView = new ListView<JogoDTO>("jogos", jogosListModel) {
            
            @Override
            protected void populateItem(ListItem<JogoDTO> item) {
                JogoDTO jogo = item.getModelObject();
                
             
                item.add(new Label("timeA", Model.of(jogo.getTimeA())));
                item.add(new Label("timeB", Model.of(jogo.getTimeB())));
                item.add(new Label("id", Model.of(jogo.getId())));
                item.add(new Label("placarA", Model.of(jogo.getPlacarA())));
                item.add(new Label("placarB", Model.of(jogo.getPlacarB())));
                item.add(new Label("status", Model.of(getStatusLabel(jogo.getStatus()))));
                
                String dataHoraStr = (jogo.getDataPartida() != null && jogo.getHoraPartida() != null) 
                        ? LocalDateTime.of(jogo.getDataPartida(), LocalTime.parse(jogo.getHoraPartida(), DateTimeConstants.TIME_FORMAT)).format(DateTimeConstants.DATETIME_BR_FORMAT)
                        : "-";
                
                item.add(new Label("dataHoraPartida", Model.of(dataHoraStr)));            
                
                // Botões de ação
                criarBotoesAcao(item, jogo);
            }            
        };

        
        jogosContainer.add(jogosListView);
        
        // Componente "sem jogos" - aparece quando lista está vazia
        WebMarkupContainer semJogos = new WebMarkupContainer("semJogos") {
            @Override
            public boolean isVisible() {
                return jogosListModel == null || jogosListModel.getObject().isEmpty();
            }
        };
        
        semJogos.setOutputMarkupPlaceholderTag(true);
        jogosContainer.add(semJogos);
        
        add(jogosContainer);        
    }

    
    private void criarBotoesAcao(ListItem<JogoDTO> item, JogoDTO jogo) {   	
    	
    		
    		
    		// Botão Editar
            AjaxLink<Void> botaoEditar = new AjaxLink<Void>("botaoEditar") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    log.info("Editando jogo ID: {}", jogo.getId());
                    
                    // Carregar dados do jogo no modal
                    editarJogoModal.carregarJogo(jogo);
                    target.add(editarJogoModal);
                    
                    // Abrir modal
                    target.appendJavaScript("$('#modalEditarJogo').modal('show');");
                }
                
                @Override
                public boolean isVisible() {
                    return jogo != null && jogo.getStatus() == StatusJogo.NAO_INICIADO;
                }
            };
            item.add(botaoEditar);            
            
            
            // Botão Excluir
            AjaxLink<Void> botaoExcluir = new AjaxLink<Void>("botaoExcluir") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    log.info("Preparando exclusão do jogo ID: {}", jogo.getId());
                    
                    // Definir jogo para exclusão no modal
                    excluirJogoModal.setJogoParaExcluir(jogo);
                    target.add(excluirJogoModal);
                    
                    // Abrir modal
                    target.appendJavaScript("$('#modalExcluirJogo').modal('show');");
                }
                
                @Override
                public boolean isVisible() {
                    return jogo != null && jogo.getStatus() == StatusJogo.NAO_INICIADO;
                }
            };
            item.add(botaoExcluir);            
    	
    	       
        
        
		item.add(new Link<Void>("botaoGestao") {
			@Override
			public void onClick() {
				Long jogoId = item.getModelObject().getId();

				PageParameters params = new PageParameters()
						.add("jogoId", jogoId);

				setResponsePage(GestaoJogoPage.class, params);
			}
			
			@Override
            public boolean isVisible() {
                return jogo != null && jogo.getStatus() != StatusJogo.FINALIZADO;
            }
		});

    }

    
    
    private void criarBotaoAdicionar() {
        AjaxLink<Void> botaoAdicionar = new AjaxLink<Void>("botaoAdicionar") {
            @Override
            public void onClick(AjaxRequestTarget target) {                
                target.appendJavaScript("$('#modalCriarJogo').modal('show');");
            }
        };
        add(botaoAdicionar);
    }
    
    private void criarModais() {
        // Modal Criar Jogo
        criarJogoModal = new CriarJogoModal("criarJogoModal", new CriarJogoModal.ModalCallback() {
            @Override
            public void onSuccess(AjaxRequestTarget target, JogoDTO jogo) {
                success("Jogo criado com sucesso: " + jogo.getTimeA() + " vs " + jogo.getTimeB());
                carregarJogos();
                target.add(jogosContainer);
                target.add(getFeedbackPanel());
                target.appendJavaScript("$('#modalCriarJogo').modal('hide');");
            }

            @Override
            public void onError(AjaxRequestTarget target, String mensagem) {
                error(mensagem);
                target.add(getFeedbackPanel());
            }

            @Override
            public void onCancel(AjaxRequestTarget target) {
                target.appendJavaScript("$('#modalCriarJogo').modal('hide');");
            }
        });
        add(criarJogoModal);

        // Modal Editar Jogo
        editarJogoModal = new EditarJogoModal("editarJogoModal", new EditarJogoModal.ModalCallback() {
            @Override
            public void onSuccess(AjaxRequestTarget target, JogoDTO jogo) {
                success("Jogo atualizado com sucesso: " + jogo.getTimeA() + " vs " + jogo.getTimeB());
                carregarJogos();
                target.add(jogosContainer);
                target.add(getFeedbackPanel());
                target.appendJavaScript("$('#modalEditarJogo').modal('hide');");
            }

            @Override
            public void onError(AjaxRequestTarget target, String mensagem) {
                error(mensagem);
                target.add(getFeedbackPanel());
            }

            @Override
            public void onCancel(AjaxRequestTarget target) {
                target.appendJavaScript("$('#modalEditarJogo').modal('hide');");
            }
        });
        add(editarJogoModal);

        // Modal Excluir Jogo
        excluirJogoModal = new ExcluirJogoModal("excluirJogoModal", new ExcluirJogoModal.ModalCallback() {
            @Override
            public void onSuccess(AjaxRequestTarget target, JogoDTO jogo) {
                success("Jogo excluído com sucesso: " + jogo.getTimeA() + " vs " + jogo.getTimeB());
                carregarJogos();
                target.add(jogosContainer);
                target.add(getFeedbackPanel());
                target.appendJavaScript("$('#modalExcluirJogo').modal('hide');");
            }

            @Override
            public void onError(AjaxRequestTarget target, String mensagem) {
                error(mensagem);
                target.add(getFeedbackPanel());
            }

            @Override
            public void onCancel(AjaxRequestTarget target) {
                target.appendJavaScript("$('#modalExcluirJogo').modal('hide');");
            }
        });
        add(excluirJogoModal);
    }

    private void carregarJogos() {
        try {
            log.debug("Carregando jogos com filtros: {}", filtro);
            
            List<JogoDTO> listaJogos = jogoService.listarPorFiltro(filtro);         
            
            jogosListModel.setObject(listaJogos);

        } catch (Exception e) {
            log.error("Erro ao carregar jogos", e);
            error("Erro ao carregar jogos: " + e.getMessage());
            jogosListModel.setObject(List.of());
        }
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

    

    public ListView<JogoDTO> getJogosListView() {
		return jogosListView;
	}


	public void setJogosListView(ListView<JogoDTO> jogosListView) {
		this.jogosListView = jogosListView;
	}
	
	
    private FeedbackPanel getFeedbackPanel() {
    	return feedbackPanel;
    }
}