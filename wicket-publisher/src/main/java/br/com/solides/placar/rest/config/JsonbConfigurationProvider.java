package br.com.solides.placar.rest.config;

import br.com.solides.placar.shared.config.SharedLocalDateAdapter;
import br.com.solides.placar.shared.config.SharedLocalDateTimeAdapter;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Configurador customizado do Jakarta JSON-B para JAX-RS.
 * Resolve problemas de parsing de LocalDateTime configurando formatadores adequados.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Provider
public class JsonbConfigurationProvider implements ContextResolver<Jsonb> {

    private final Jsonb jsonb;

    public JsonbConfigurationProvider() {
        JsonbConfig config = new JsonbConfig()
            // Usar adaptadores específicos para cada tipo de data
            .withAdapters(
                new SharedLocalDateAdapter(),        // Para LocalDate (yyyy-MM-dd)
                new SharedLocalDateTimeAdapter()     // Para LocalDateTime (yyyy-MM-dd'T'HH:mm:ss)
            )
            // Configuração para tratar nulls
            .withNullValues(true);
            // Removendo configuração global de data que estava causando conflito

        this.jsonb = JsonbBuilder.create(config);
    }

    @Override
    public Jsonb getContext(Class<?> type) {
        return jsonb;
    }
}