package br.com.solides.placar.shared.config;

import jakarta.json.bind.adapter.JsonbAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Adaptador específico para LocalDate no shared-domain.
 * Garante parsing correto apenas com data (sem horário).
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class SharedLocalDateAdapter implements JsonbAdapter<LocalDate, String> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public String adaptToJson(LocalDate date) throws Exception {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    @Override
    public LocalDate adaptFromJson(String jsonString) throws Exception {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }

        String trimmed = jsonString.trim();
        
        try {
            return LocalDate.parse(trimmed, DATE_FORMATTER);
        } catch (DateTimeParseException e1) {
            // Tentar parsing padrão ISO se falhar
            try {
                return LocalDate.parse(trimmed);
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException(
                    String.format("Formato de data inválido: '%s'. Formato aceito: yyyy-MM-dd", trimmed), e2);
            }
        }
    }
}