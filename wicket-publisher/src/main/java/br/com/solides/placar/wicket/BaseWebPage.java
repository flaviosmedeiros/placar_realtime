package br.com.solides.placar.wicket;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class BaseWebPage extends WebPage {

	private static final long serialVersionUID = 1L;

	public BaseWebPage() {
		this(new PageParameters());
	}

	public BaseWebPage(final PageParameters parameters) {
		super(parameters);
	}
	
	
	@Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        
        // Bootstrap CSS
        response.render(
            CssHeaderItem.forUrl("https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"));      

        // Bootstrap Icons
        response.render(
            CssHeaderItem.forUrl("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css"));

        // Bootstrap JS
        response.render(
            JavaScriptHeaderItem.forUrl("https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"));
        
        
        //response.render(
	    //		CssHeaderItem.forReference(new PackageResourceReference(JogoListPage.class, "bootstrap-icons/bootstrap-icons.min.css")	     
	    //));
    }

}
