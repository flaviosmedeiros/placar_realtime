package br.com.solides.placar.shared.config;

import jakarta.json.bind.adapter.JsonbAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Adaptador global para LocalDateTime no shared-domain.
 * Garante parsing consistente em todos os módulos que usam este shared-domain.
 * Resolve especificamente o erro: "Error parsing class java.time.LocalDateTime from value: 2026-02-14 13:21:05"
 * 
 * @author Copilot
 * @since 1.0.0
 */
public class SharedLocalDateTimeAdapter implements JsonbAdapter<LocalDateTime, String> {

    // Formato padrão ISO 8601 com T
    private static final DateTimeFormatter PRIMARY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    // Formato problemático que está causando o erro (sem T)
    private static final DateTimeFormatter PROBLEMATIC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Formato com milissegundos
    private static final DateTimeFormatter WITH_MILLIS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    
    // Formato com microssegundos
    private static final DateTimeFormatter WITH_MICROS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    @Override
    public String adaptToJson(LocalDateTime dateTime) throws Exception {
        if (dateTime == null) {
            return null;
        }
        // Sempre serializar no formato padrão ISO 8601
        return dateTime.format(PRIMARY_FORMATTER);
    }

    @Override
    public LocalDateTime adaptFromJson(String jsonString) throws Exception {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }

        String trimmed = jsonString.trim();
        
        // Log para debug (apenas durante desenvolvimento)
        System.out.println("SharedLocalDateTimeAdapter: Tentando fazer parsing de: '" + trimmed + "'");
        
        // 1. Tentar formato ISO padrão: yyyy-MM-dd'T'HH:mm:ss
        try {
            LocalDateTime result = LocalDateTime.parse(trimmed, PRIMARY_FORMATTER);
            System.out.println("SharedLocalDateTimeAdapter: Sucesso com PRIMARY_FORMATTER");
            return result;
        } catch (DateTimeParseException e1) {
            // 2. Tentar formato problemático: yyyy-MM-dd HH:mm:ss (SEM o T) - que está causando o erro
            try {
                LocalDateTime result = LocalDateTime.parse(trimmed, PROBLEMATIC_FORMATTER);
                System.out.println("SharedLocalDateTimeAdapter: Sucesso com PROBLEMATIC_FORMATTER (sem T)");
                return result;
            } catch (DateTimeParseException e2) {
                // 3. Tentar formato com milissegundos: yyyy-MM-dd'T'HH:mm:ss.SSS
                try {
                    LocalDateTime result = LocalDateTime.parse(trimmed, WITH_MILLIS_FORMATTER);
                    System.out.println("SharedLocalDateTimeAdapter: Sucesso com WITH_MILLIS_FORMATTER");
                    return result;
                } catch (DateTimeParseException e3) {
                    // 4. Tentar formato com microssegundos
                    try {
                        LocalDateTime result = LocalDateTime.parse(trimmed, WITH_MICROS_FORMATTER);
                        System.out.println("SharedLocalDateTimeAdapter: Sucesso com WITH_MICROS_FORMATTER");
                        return result;
                    } catch (DateTimeParseException e4) {
                        // 5. Último recurso: parsing padrão ISO
                        try {
                            LocalDateTime result = LocalDateTime.parse(trimmed);
                            System.out.println("SharedLocalDateTimeAdapter: Sucesso com parsing padrão ISO");
                            return result;
                        } catch (DateTimeParseException e5) {
                            String errorMessage = String.format(
                                "Erro ao fazer parsing de LocalDateTime: '%s'. " +
                                "Formatos aceitos: yyyy-MM-dd'T'HH:mm:ss, yyyy-MM-dd HH:mm:ss, yyyy-MM-dd'T'HH:mm:ss.SSS. " +
                                "Erro original: %s", 
                                trimmed, e1.getMessage());
                            
                            System.err.println("SharedLocalDateTimeAdapter: " + errorMessage);
                            throw new IllegalArgumentException(errorMessage, e5);
                        }
                    }
                }
            }
        }
    }
}