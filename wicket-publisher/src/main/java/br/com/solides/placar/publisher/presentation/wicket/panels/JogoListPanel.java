package br.com.solides.placar.publisher.presentation.wicket.panels;

import br.com.solides.placar.shared.dto.JogoDTO;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Painel Wicket para exibir lista de jogos.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class JogoListPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public JogoListPanel(String id, IModel<List<JogoDTO>> jogosModel) {
        super(id);
        setOutputMarkupId(true);

        // Container para a lista
        WebMarkupContainer listContainer = new WebMarkupContainer("listContainer");
        listContainer.setOutputMarkupId(true);
        add(listContainer);

        // ListView de jogos
        ListView<JogoDTO> jogosList = new ListView<JogoDTO>("jogosList", jogosModel) {
            @Override
            protected void populateItem(ListItem<JogoDTO> item) {
                JogoDTO jogo = item.getModelObject();

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
            }
        };
        listContainer.add(jogosList);

        // Mensagem quando não há jogos
        Label emptyMessage = new Label("emptyMessage", "Nenhum jogo encontrado") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(jogosModel.getObject() == null || jogosModel.getObject().isEmpty());
            }
        };
        listContainer.add(emptyMessage);
    }
}
