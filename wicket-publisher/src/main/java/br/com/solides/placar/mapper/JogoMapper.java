package br.com.solides.placar.mapper;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import br.com.solides.placar.entity.Jogo;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper para conversão entre Entity Jogo e DTOs.
 * Implementação manual para máximo controle sobre as conversões.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
public class JogoMapper {

    /**
     * Converte Entity para DTO completo
     */
    public JogoDTO toDTO(Jogo entity) {
        if (entity == null) {
            return null;
        }

        JogoDTO dto = JogoDTO.builder()
                .id(entity.getId())
                .timeA(entity.getTimeA())
                .timeB(entity.getTimeB())
                .placarA(entity.getPlacarA())
                .placarB(entity.getPlacarB())
                .status(entity.getStatus())
                .dataHoraPartida(entity.getDataHoraPartida())
                .tempoDeJogo(entity.getTempoDeJogo())
                .dataHoraEncerramento(entity.getDataHoraEncerramento())
                .dataCriacao(entity.getDataCriacao())
                .dataAtualizacao(entity.getDataAtualizacao())
                .build();

        // Converter dataHoraPartida para dataPartida e horaPartida
        if (entity.getDataHoraPartida() != null) {
            LocalDateTime dataHora = entity.getDataHoraPartida();
            
            // Converter para Date
            dto.setDataPartida(Date.from(dataHora.atZone(ZoneId.systemDefault()).toInstant()));
            
            // Formatar hora como string HH:mm
            dto.setHoraPartida(dataHora.format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        return dto;
    }

    /**
     * Converte DTO para Entity
     */
    public Jogo toEntity(JogoDTO dto) {
        if (dto == null) {
            return null;
        }

        LocalDateTime dataHoraPartida = dto.getDataHoraPartida();
        
        // Se dataHoraPartida for null, tentar construir a partir de dataPartida e horaPartida
        if (dataHoraPartida == null && dto.getDataPartida() != null && dto.getHoraPartida() != null) {
            try {
                LocalDate data = dto.getDataPartida().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalTime hora = LocalTime.parse(dto.getHoraPartida(), DateTimeFormatter.ofPattern("HH:mm"));
                dataHoraPartida = LocalDateTime.of(data, hora);
            } catch (Exception e) {
                throw new IllegalArgumentException("Erro ao converter data e hora da partida", e);
            }
        }

        return Jogo.builder()
                .id(dto.getId())
                .timeA(dto.getTimeA())
                .timeB(dto.getTimeB())
                .placarA(dto.getPlacarA())
                .placarB(dto.getPlacarB())
                .status(dto.getStatus())
                .dataHoraPartida(dataHoraPartida)
                .tempoDeJogo(dto.getTempoDeJogo())
                .dataHoraEncerramento(dto.getDataHoraEncerramento())
                .dataCriacao(dto.getDataCriacao())
                .dataAtualizacao(dto.getDataAtualizacao())
                .build();
    }

    /**
     * Converte CriarJogoDTO para Entity (para criação)
     */
    public Jogo toEntity(CriarJogoDTO criarDTO) {
        if (criarDTO == null) {
            return null;
        }

        LocalDateTime dataHoraPartida = criarDTO.getDataHoraPartida();
        
        // Se dataHoraPartida for null, tentar construir a partir de dataPartida e horaPartida
        if (dataHoraPartida == null && criarDTO.getDataPartida() != null && criarDTO.getHoraPartida() != null) {
            try {
                LocalDate data = criarDTO.getDataPartida().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalTime hora = LocalTime.parse(criarDTO.getHoraPartida(), DateTimeFormatter.ofPattern("HH:mm"));
                dataHoraPartida = LocalDateTime.of(data, hora);
            } catch (Exception e) {
                throw new IllegalArgumentException("Erro ao converter data e hora da partida no CriarJogoDTO", e);
            }
        }

        return Jogo.builder()
                .timeA(criarDTO.getTimeA())
                .timeB(criarDTO.getTimeB())
                .dataHoraPartida(dataHoraPartida)
                .placarA(0) // Sempre inicializa com 0
                .placarB(0) // Sempre inicializa com 0
                .status(StatusJogo.NAO_INICIADO) // Sempre inicializa como NAO_INICIADO
                .tempoDeJogo(0)
                .build();
    }

    /**
     * Atualiza Entity existente com dados do DTO
     */
    public void updateEntity(Jogo entity, JogoDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setTimeA(dto.getTimeA());
        entity.setTimeB(dto.getTimeB());
        entity.setPlacarA(dto.getPlacarA());
        entity.setPlacarB(dto.getPlacarB());
        entity.setStatus(dto.getStatus());
        
        // Atualizar dataHoraPartida
        LocalDateTime dataHoraPartida = dto.getDataHoraPartida();
        
        // Se dataHoraPartida for null, tentar construir a partir de dataPartida e horaPartida
        if (dataHoraPartida == null && dto.getDataPartida() != null && dto.getHoraPartida() != null) {
            try {
                LocalDate data = dto.getDataPartida().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalTime hora = LocalTime.parse(dto.getHoraPartida(), DateTimeFormatter.ofPattern("HH:mm"));
                dataHoraPartida = LocalDateTime.of(data, hora);
            } catch (Exception e) {
                throw new IllegalArgumentException("Erro ao converter data e hora da partida", e);
            }
        }
        
        entity.setDataHoraPartida(dataHoraPartida);
        entity.setTempoDeJogo(dto.getTempoDeJogo());
        
        // dataHoraEncerramento só é atualizado quando o jogo é finalizado
        if (dto.getStatus() == StatusJogo.FINALIZADO && entity.getDataHoraEncerramento() == null) {
            entity.setDataHoraEncerramento(LocalDateTime.now());
        }
    }

    /**
     * Converte lista de Entities para lista de DTOs
     */
    public List<JogoDTO> toDTOList(List<Jogo> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte lista de DTOs para lista de Entities
     */
    public List<Jogo> toEntityList(List<JogoDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}