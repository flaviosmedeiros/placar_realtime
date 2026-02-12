package br.com.solides.placar.util;

import java.time.format.DateTimeFormatter;

/**
 * Constantes para formatação de data e hora utilizadas em todo o sistema.
 * Centraliza os padrões de formatação para garantir consistência.
 * 
 * @author Copilot
 * @since 1.0.0
 */
public final class DateTimeConstants {
    
    private DateTimeConstants() {
        // Classe utilitária - construtor privado
    }
    
    /**
     * Formato ISO para data e hora (yyyy-MM-dd'T'HH:mm)
     * Usado em formulários e APIs
     */
    public static final DateTimeFormatter DATETIME_ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    
    /**
     * Formato brasileiro para data e hora (dd/MM/yyyy HH:mm)
     * Usado para exibição ao usuário
     */
    public static final DateTimeFormatter DATETIME_BR_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Formato brasileiro para data e hora com segundos (dd/MM/yyyy HH:mm:ss)
     * Usado para timestamps detalhados
     */
    public static final DateTimeFormatter DATETIME_BR_WITH_SECONDS = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    /**
     * Formato para apenas hora (HH:mm)
     * Usado para campos de hora
     */
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * Formato ISO para data (yyyy-MM-dd)
     * Usado em formulários e campos de data
     */
    public static final DateTimeFormatter DATE_ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
}