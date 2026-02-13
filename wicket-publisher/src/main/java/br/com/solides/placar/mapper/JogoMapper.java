package br.com.solides.placar.mapper;

import java.time.LocalDateTime;
import java.util.List;

import br.com.solides.placar.entity.Jogo;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.util.DateTimeConstants;
import br.com.solides.placar.util.PublisherUtils;
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
                .tempoDeJogo(gerarTempoDejogo(entity))
                .dataHoraEncerramento(entity.getDataHoraEncerramento())
                .dataCriacao(entity.getDataCriacao())
                .dataAtualizacao(entity.getDataAtualizacao())
                .build();
       
        if (!PublisherUtils.nuloOuVazio(entity.getDataHoraPartida())) {
            LocalDateTime dataHora = entity.getDataHoraPartida();           
            dto.setDataPartida(entity.getDataHoraPartida().toLocalDate());          
            dto.setHoraPartida(dataHora.format(DateTimeConstants.TIME_FORMAT));
        }

        return dto;
    }

    
    private Integer gerarTempoDejogo(Jogo entity) {
		Integer tempoDeJogo = 0;
		if (entity.getStatus() == StatusJogo.EM_ANDAMENTO && !PublisherUtils.nuloOuVazio(entity.getDataHoraPartida())) {
			LocalDateTime agora = LocalDateTime.now();
			tempoDeJogo = (int) java.time.Duration.between(entity.getDataHoraPartida(), agora).toMinutes();
		} else if (entity.getStatus() == StatusJogo.FINALIZADO && !PublisherUtils.nuloOuVazio(entity.getDataHoraPartida()) && !PublisherUtils.nuloOuVazio(entity.getDataHoraEncerramento())) {
			tempoDeJogo = (int) java.time.Duration.between(entity.getDataHoraPartida(), entity.getDataHoraEncerramento()).toMinutes();
		}
		return tempoDeJogo;
	}

	/**
     * Converte DTO para Entity
     */
    public Jogo toEntity(JogoDTO dto) {
        if (dto == null) {
            return null;
        }

        LocalDateTime dataHoraPartida = PublisherUtils.construirDataHoraPartida(dto.getDataPartida(), dto.getHoraPartida());

        return Jogo.builder()
                .id(dto.getId())
                .timeA(dto.getTimeA())
                .timeB(dto.getTimeB())
                .placarA(dto.getPlacarA())
                .placarB(dto.getPlacarB())
                .status(dto.getStatus())
                .dataHoraPartida(dataHoraPartida)
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

        LocalDateTime dataHoraPartida = PublisherUtils.construirDataHoraPartida(criarDTO.getDataPartida(), criarDTO.getHoraPartida());

        return Jogo.builder()
                .timeA(criarDTO.getTimeA())
                .timeB(criarDTO.getTimeB())
                .dataHoraPartida(dataHoraPartida)
                .placarA(0) // Sempre inicializa com 0
                .placarB(0) // Sempre inicializa com 0
                .status(StatusJogo.NAO_INICIADO) // Sempre inicializa como NAO_INICIADO
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
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
        entity.setDataAtualizacao(LocalDateTime.now());
        
        // Atualizar dataHoraPartida
        LocalDateTime dataHoraPartida = PublisherUtils.construirDataHoraPartida(dto.getDataPartida(), dto.getHoraPartida());
        
        entity.setDataHoraPartida(dataHoraPartida);
        
        // dataHoraEncerramento só é atualizado quando o jogo é finalizado
        if (dto.getStatus() == StatusJogo.FINALIZADO && PublisherUtils.nuloOuVazio(entity.getDataHoraEncerramento())) {
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
                .map(this::toDTO).toList();
    }

    /**
     * Converte lista de DTOs para lista de Entities
     */
    public List<Jogo> toEntityList(List<JogoDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity).toList();
    }

    
}