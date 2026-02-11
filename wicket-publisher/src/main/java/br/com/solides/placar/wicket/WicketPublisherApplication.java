package br.com.solides.placar.wicket;

import org.apache.wicket.Page;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.csp.CSPHeaderConfiguration;
import org.apache.wicket.protocol.http.WebApplication;

import br.com.solides.placar.wicket.pages.HomePage;
import lombok.extern.slf4j.Slf4j;

/**
 * Aplicação principal do módulo Wicket Publisher.
 * Esta classe configura a aplicação Wicket para publicação de dados do placar em tempo real.
 */
@Slf4j
public class WicketPublisherApplication extends WebApplication {

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
        
        cfg.add(CSPDirective.STYLE_SRC, "https://cdn.jsdelivr.net");
        cfg.add(CSPDirective.SCRIPT_SRC, "https://cdn.jsdelivr.net");
        cfg.add(CSPDirective.FONT_SRC, "https://cdn.jsdelivr.net");
        cfg.add(CSPDirective.IMG_SRC, "data:");
        cfg.add(CSPDirective.IMG_SRC, "https://cdn.jsdelivr.net");
        cfg.add(CSPDirective.CONNECT_SRC, "https://cdn.jsdelivr.net");

        
        log.info("Aplicação configurada para modo DESENVOLVIMENTO");
    }
}