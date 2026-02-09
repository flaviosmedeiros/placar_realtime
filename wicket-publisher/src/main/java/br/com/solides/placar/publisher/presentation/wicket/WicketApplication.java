package br.com.solides.placar.publisher.presentation.wicket;

import br.com.solides.placar.publisher.presentation.wicket.pages.HomePage;
import org.apache.wicket.Page;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Aplicação Wicket para interface administrativa.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class WicketApplication extends WebApplication {

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    @Override
    public void init() {
        super.init();
        
        // Configurar integração com CDI
        new CdiConfiguration().configure(this);
        
        // Configurações de segurança
        getSecuritySettings().setEnforceMounts(true);
        
        // Configurações de markup
        getMarkupSettings().setStripWicketTags(true);
        getMarkupSettings().setCompressWhitespace(true);
        
        // Configurações de debug (ajustar em produção)
        getDebugSettings().setDevelopmentUtilitiesEnabled(true);
        
        // Montar páginas
        mountPage("/home", HomePage.class);
        
        getApplicationSettings().setPageExpiredErrorPage(HomePage.class);
    }
}
