package br.com.solides.placar.wicket;

import org.apache.wicket.Page;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.csp.CSPHeaderConfiguration;
import org.apache.wicket.protocol.http.WebApplication;

import br.com.solides.placar.wicket.pages.HomePage;
import br.com.solides.placar.wicket.pages.jogo.GestaoJogoPage;
import br.com.solides.placar.wicket.pages.jogo.JogoListPage;
import lombok.extern.slf4j.Slf4j;

/**
 * Aplicação principal do módulo Wicket Publisher.
 * Esta classe configura a aplicação Wicket para publicação de dados do placar em tempo real.
 */
@Slf4j
public class WicketPublisherApplication extends WebApplication {

    // Constantes de URLs
    private static final String CDN_JSDELIVR_NET_URL = "https://cdn.jsdelivr.net";

    @Override
    public void init() {
        super.init();
        
        log.info("Iniciando Wicket Publisher Application");
        
        // Configurar CDI para injeção de dependências
        configureCDI();
        
        // Configurações básicas
        configureApplication();
        
        log.info("Wicket Publisher Application inicializada com sucesso");
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }
    
    /**
     * Configura CDI para injeção de dependências em componentes Wicket
     */
    private void configureCDI() {
        try {
            // Configurar CDI para Wicket
            new CdiConfiguration().configure(this);
            log.info("CDI configurado com sucesso para Wicket");
        } catch (Exception e) {
            log.error("Erro ao configurar CDI para Wicket", e);
            throw new RuntimeException("Falha na configuração CDI", e);
        }
    }
    
    /**
     * Configurações gerais da aplicação
     */
    private void configureApplication() {
        // Configurações de markup
        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
        
        // Modo de desenvolvimento
        getDebugSettings().setDevelopmentUtilitiesEnabled(true);
        getDebugSettings().setAjaxDebugModeEnabled(true);
        getMarkupSettings().setStripWicketTags(false);
        
        CSPHeaderConfiguration cfg = getCspSettings()
                .blocking();
        cfg.unsafeInline();
        
        cfg.add(CSPDirective.STYLE_SRC, CDN_JSDELIVR_NET_URL);
        cfg.add(CSPDirective.SCRIPT_SRC, CDN_JSDELIVR_NET_URL);
        cfg.add(CSPDirective.FONT_SRC, CDN_JSDELIVR_NET_URL);
        cfg.add(CSPDirective.IMG_SRC, "data:");
        cfg.add(CSPDirective.IMG_SRC, CDN_JSDELIVR_NET_URL);
        
        mountPage("/jogos/lista", JogoListPage.class);
        mountPage("/jogos/gerenciar", GestaoJogoPage.class);
        
        log.info("Aplicação configurada para modo DESENVOLVIMENTO");
    }
}