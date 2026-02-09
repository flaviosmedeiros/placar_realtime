package br.com.solides.placar.publisher.presentation.wicket.panels;

import br.com.solides.placar.shared.dto.JogoDTO;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Painel Wicket para exibir lista de jogos.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Slf4j
public class JogoListPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public JogoListPanel(String id, IModel<List<JogoDTO>> jogosModel) {
        super(id);
        log.info("=== Construindo JogoListPanel ===");
        
        try {
            setOutputMarkupId(true);

            log.debug("Verificando modelo de jogos...");
            if (jogosModel == null) {
                log.error("ERRO: jogosModel é NULL!");
                throw new IllegalArgumentException("jogosModel não pode ser null");
            }

            // Container para a lista
            log.debug("Criando listContainer...");
            WebMarkupContainer listContainer = new WebMarkupContainer("listContainer");
            listContainer.setOutputMarkupId(true);
            add(listContainer);

            // ListView de jogos
            log.debug("Criando ListView de jogos...");
            ListView<JogoDTO> jogosList = new ListView<JogoDTO>("jogosList", jogosModel) {
                @Override
                protected void populateItem(ListItem<JogoDTO> item) {
                    try {
                        JogoDTO jogo = item.getModelObject();
                        log.trace("Populando item da lista - Jogo ID: {}", jogo != null ? jogo.getId() : "NULL");

                        if (jogo == null) {
                            log.warn("JogoDTO é NULL no item da lista!");
                            return;
                        }

                        // Informações do jogo
                        item.add(new Label("jogoId", jogo.getId()));
                        item.add(new Label("timeA", jogo.getTimeA()));
                        item.add(new Label("timeB", jogo.getTimeB()));
                        item.add(new Label("placarA", jogo.getPlacarA()));
                        item.add(new Label("placarB", jogo.getPlacarB()));
                        item.add(new Label("status", jogo.getStatus().toString()));
                        
                        String dataFormatada = jogo.getDataHoraInicioPartida() != null 
                            ? jogo.getDataHoraInicioPartida().format(DATE_FORMATTER)
                            : "N/A";
                        item.add(new Label("dataHoraPartida", dataFormatada));

                        // CSS class baseado no status
                        String statusClass = jogo.getStatus().toString().toLowerCase().replace("_", "-");
                        item.add(new Label("statusClass", statusClass));
                        
                    } catch (Exception e) {
                        log.error("ERRO ao popular item da lista de jogos", e);
                        throw new RuntimeException("Erro ao renderizar jogo", e);
                    }
                }
            };
            listContainer.add(jogosList);
            log.debug("ListView adicionado ao container");

            // Mensagem quando não há jogos
            log.debug("Criando mensagem de lista vazia...");
            Label emptyMessage = new Label("emptyMessage", "Nenhum jogo encontrado") {
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(jogosModel.getObject() == null || jogosModel.getObject().isEmpty());
                }
            };
            listContainer.add(emptyMessage);
            log.debug("Mensagem de lista vazia adicionada");

            log.info("=== JogoListPanel construído com sucesso ===");
            
        } catch (Exception e) {
            log.error("=== ERRO ao construir JogoListPanel ===", e);
            throw new RuntimeException("Falha ao construir JogoListPanel", e);
        }
    }
}
