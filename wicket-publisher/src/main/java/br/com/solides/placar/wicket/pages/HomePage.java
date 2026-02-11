package br.com.solides.placar.wicket.pages;

import br.com.solides.placar.wicket.pages.jogo.JogoListPage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Página inicial do Wicket Publisher.
 * Esta página serve como ponto de entrada da aplicação e demonstra 
 * a integração com Bootstrap para responsividade.
 */
@Slf4j
public class HomePage extends WebPage {
    
    private static final long serialVersionUID = 1L;
    
    public HomePage() {
        this(new PageParameters());
    }
    
    public HomePage(final PageParameters parameters) {
        super(parameters);
        
        log.info("Carregando HomePage do Wicket Publisher");
        
        initializePage();
        
        log.info("HomePage carregada com sucesso");
    }
    
    private void initializePage() {
        // Título da página
        add(new Label("pageTitle", Model.of("Wicket Publisher - Placar Realtime")));
        
        // Título principal
        add(new Label("mainTitle", Model.of("Bem-vindo ao Wicket Publisher")));
        
        // Subtítulo
        add(new Label("subtitle", Model.of("Sistema de Publicação de Placar em Tempo Real")));
        
        // Informações do sistema
        add(new Label("systemInfo", Model.of("Sistema iniciado com sucesso!")));
        
        // Timestamp atual
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        add(new Label("currentTime", Model.of(currentTime)));
        
        // Versão da aplicação
        add(new Label("appVersion", Model.of("1.0.0-SNAPSHOT")));
        
        // Tecnologias utilizadas
        add(new Label("techStack", Model.of(buildTechStackInfo())));
        
        // Link para gerenciamento de jogos
        add(new BookmarkablePageLink<>("linkJogos", JogoListPage.class));
    }
    
    private String buildTechStackInfo() {
        return "Jakarta EE 10.0.0 • Apache Wicket 10.0.0 • Payara Server 6.2023.5 • Bootstrap 5.3.2";
    }
}