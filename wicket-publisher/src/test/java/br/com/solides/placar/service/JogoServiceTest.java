package br.com.solides.placar.service;

import br.com.solides.placar.entity.Jogo;
import br.com.solides.placar.event.internal.*;
import br.com.solides.placar.mapper.JogoMapper;
import br.com.solides.placar.repository.JogoRepository;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.dto.JogoFilterDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.shared.exception.BusinessException;
import br.com.solides.placar.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para JogoService.
 * Valida regras de negócio, validações e integração com eventos.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JogoService - Testes Unitários")
class JogoServiceTest {

    @Mock
    private JogoRepository jogoRepository;

    @Mock
    private JogoMapper jogoMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private JogoService jogoService;

    private Jogo jogoEntity;
    private JogoDTO jogoDTO;
    private CriarJogoDTO criarJogoDTO;

    @BeforeEach
    void setUp() {
        // Setup comum para todos os testes
        jogoEntity = Jogo.builder()
                .id(1L)
                .timeA("Flamengo")
                .timeB("Vasco")
                .placarA(0)
                .placarB(0)
                .status(StatusJogo.NAO_INICIADO)
                .dataHoraPartida(LocalDateTime.now().plusDays(1))
                .build();

        jogoDTO = JogoDTO.builder()
                .id(1L)
                .timeA("Flamengo")
                .timeB("Vasco")
                .placarA(0)
                .placarB(0)
                .status(StatusJogo.NAO_INICIADO)
                .tempoDeJogo(0)
                .dataPartida(LocalDate.now().plusDays(1))
                .horaPartida("20:00")
                .build();

        criarJogoDTO = CriarJogoDTO.builder()
                .timeA("Flamengo")
                .timeB("Vasco")
                .dataPartida(LocalDate.now().plusDays(1))
                .horaPartida("20:00")
                .build();
    }

    @Nested
    @DisplayName("Criar Jogo")
    class CriarJogoTests {

        @Test
        @DisplayName("Deve criar jogo com sucesso")
        void deveCriarJogoComSucesso() {
            // Arrange
            when(jogoMapper.toEntity(criarJogoDTO)).thenReturn(jogoEntity);
            when(jogoRepository.save(any(Jogo.class))).thenReturn(jogoEntity);
            when(jogoMapper.toDTO(jogoEntity)).thenReturn(jogoDTO);

            // Act
            JogoDTO resultado = jogoService.criarJogo(criarJogoDTO);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTimeA()).isEqualTo("Flamengo");
            assertThat(resultado.getTimeB()).isEqualTo("Vasco");
            assertThat(resultado.getStatus()).isEqualTo(StatusJogo.NAO_INICIADO);

            verify(jogoRepository).save(any(Jogo.class));
            verify(eventPublisher).publishEvent(argThat(event -> 
                event instanceof JogoCriadoEvent &&
                ((JogoCriadoEvent) event).getJogo().equals(jogoDTO)
            ));
        }

        @Test
        @DisplayName("Deve falhar ao criar jogo com times iguais")
        void deveFalharAoCriarJogoComTimesIguais() {
            // Arrange
            criarJogoDTO.setTimeB("Flamengo"); // Mesmo time

            // Act & Assert
            assertThatThrownBy(() -> jogoService.criarJogo(criarJogoDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Os times A e B devem ser diferentes");

            verify(jogoRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Deve falhar ao criar jogo com data no passado")
        void deveFalharAoCriarJogoComDataNoPassado() {
            // Arrange
            criarJogoDTO.setDataPartida(LocalDate.now().minusDays(1));

            // Act & Assert
            assertThatThrownBy(() -> jogoService.criarJogo(criarJogoDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Data/hora da partida não pode ser no passado");

            verify(jogoRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Buscar Jogo")
    class BuscarJogoTests {

        @Test
        @DisplayName("Deve buscar jogo por ID com sucesso")
        void deveBuscarJogoPorIdComSucesso() {
            // Arrange
            Long jogoId = 1L;
            when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogoEntity));
            when(jogoMapper.toDTO(jogoEntity)).thenReturn(jogoDTO);

            // Act
            JogoDTO resultado = jogoService.buscarPorId(jogoId);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(jogoId);
            verify(jogoRepository).findById(jogoId);
        }

        @Test
        @DisplayName("Deve falhar ao buscar jogo inexistente")
        void deveFalharAoBuscarJogoInexistente() {
            // Arrange
            Long jogoId = 999L;
            when(jogoRepository.findById(jogoId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> jogoService.buscarPorId(jogoId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(jogoRepository).findById(jogoId);
        }
    }

    @Nested
    @DisplayName("Iniciar Jogo")
    class IniciarJogoTests {

        @Test
        @DisplayName("Deve iniciar jogo com sucesso")
        void deveIniciarJogoComSucesso() {
            // Arrange
            Long jogoId = 1L;
            jogoEntity.setStatus(StatusJogo.NAO_INICIADO);
            when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogoEntity));
            when(jogoRepository.save(any(Jogo.class))).thenReturn(jogoEntity);
            when(jogoMapper.toDTO(any(Jogo.class))).thenReturn(jogoDTO);

            // Act
            JogoDTO resultado = jogoService.iniciarJogo(jogoId);

            // Assert
            assertThat(resultado).isNotNull();
            verify(jogoRepository).save(argThat(jogo -> 
                jogo.getStatus() == StatusJogo.EM_ANDAMENTO &&
                jogo.getPlacarA() == 0 &&
                jogo.getPlacarB() == 0
            ));
            verify(eventPublisher).publishEvent(any(JogoIniciadoEvent.class));
        }

        @Test
        @DisplayName("Deve falhar ao iniciar jogo já em andamento")
        void deveFalharAoIniciarJogoJaEmAndamento() {
            // Arrange
            Long jogoId = 1L;
            jogoEntity.setStatus(StatusJogo.EM_ANDAMENTO);
            when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogoEntity));

            // Act & Assert
            assertThatThrownBy(() -> jogoService.iniciarJogo(jogoId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Apenas jogos com status 'Não Iniciado' podem ser iniciados");

            verify(jogoRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Atualizar Placar")
    class AtualizarPlacarTests {

        @Test
        @DisplayName("Deve atualizar placar com sucesso")
        void deveAtualizarPlacarComSucesso() {
            // Arrange
            Long jogoId = 1L;
            Integer placarA = 2;
            Integer placarB = 1;
            jogoEntity.setStatus(StatusJogo.EM_ANDAMENTO);
            
            when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogoEntity));
            when(jogoRepository.save(any(Jogo.class))).thenReturn(jogoEntity);
            when(jogoMapper.toDTO(any(Jogo.class))).thenReturn(jogoDTO);

            // Act
            JogoDTO resultado = jogoService.atualizarPlacar(jogoId, placarA, placarB);

            // Assert
            assertThat(resultado).isNotNull();
            verify(jogoRepository).save(argThat(jogo ->
                jogo.getPlacarA().equals(placarA) &&
                jogo.getPlacarB().equals(placarB)
            ));
            verify(eventPublisher).publishEvent(any(PlacarAtualizadoInternalEvent.class));
        }

        @Test
        @DisplayName("Deve falhar ao atualizar placar de jogo não iniciado")
        void deveFalharAoAtualizarPlacarJogoNaoIniciado() {
            // Arrange
            Long jogoId = 1L;
            jogoEntity.setStatus(StatusJogo.NAO_INICIADO);
            when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogoEntity));

            // Act & Assert
            assertThatThrownBy(() -> jogoService.atualizarPlacar(jogoId, 1, 0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Apenas jogos em andamento podem ter placar atualizado");

            verify(jogoRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Deve falhar ao atualizar placar com valores negativos")
        void deveFalharAoAtualizarPlacarComValoresNegativos() {
            // Arrange
            Long jogoId = 1L;
            jogoEntity.setStatus(StatusJogo.EM_ANDAMENTO);
            when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogoEntity));

            // Act & Assert
            assertThatThrownBy(() -> jogoService.atualizarPlacar(jogoId, -1, 0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Placar não pode ser negativo");

            verify(jogoRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Finalizar Jogo")
    class FinalizarJogoTests {

        @Test
        @DisplayName("Deve finalizar jogo com sucesso")
        void deveFinalizarJogoComSucesso() {
            // Arrange
            Long jogoId = 1L;
            jogoEntity.setStatus(StatusJogo.EM_ANDAMENTO);
            
            when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogoEntity));
            when(jogoRepository.save(any(Jogo.class))).thenReturn(jogoEntity);
            when(jogoMapper.toDTO(any(Jogo.class))).thenReturn(jogoDTO);

            // Act
            JogoDTO resultado = jogoService.finalizarJogo(jogoId);

            // Assert
            assertThat(resultado).isNotNull();
            verify(jogoRepository).save(argThat(jogo ->
                jogo.getStatus() == StatusJogo.FINALIZADO &&
                jogo.getDataHoraEncerramento() != null
            ));
            verify(eventPublisher).publishEvent(any(JogoFinalizadoEvent.class));
        }

        @Test
        @DisplayName("Deve falhar ao finalizar jogo não iniciado")
        void deveFalharAoFinalizarJogoNaoIniciado() {
            // Arrange
            Long jogoId = 1L;
            jogoEntity.setStatus(StatusJogo.NAO_INICIADO);
            when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogoEntity));

            // Act & Assert
            assertThatThrownBy(() -> jogoService.finalizarJogo(jogoId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Apenas jogos com status 'Em Andamento' podem ser finalizados");

            verify(jogoRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Listar Jogos")
    class ListarJogosTests {

        @Test
        @DisplayName("Deve listar todos os jogos")
        void deveListarTodosOsJogos() {
            // Arrange
            List<Jogo> jogos = Arrays.asList(jogoEntity);
            List<JogoDTO> jogosDTO = Arrays.asList(jogoDTO);
            
            when(jogoRepository.findAll()).thenReturn(jogos);
            when(jogoMapper.toDTOList(jogos)).thenReturn(jogosDTO);

            // Act
            List<JogoDTO> resultado = jogoService.listarTodos();

            // Assert
            assertThat(resultado).isNotEmpty();
            assertThat(resultado).hasSize(1);
            verify(jogoRepository).findAll();
        }

        @Test
        @DisplayName("Deve listar jogos por filtro")
        void deveListarJogosPorFiltro() {
            // Arrange
            JogoFilterDTO filtro = new JogoFilterDTO();
            List<Jogo> jogos = Arrays.asList(jogoEntity);
            List<JogoDTO> jogosDTO = Arrays.asList(jogoDTO);
            
            when(jogoRepository.findByFilter(filtro)).thenReturn(jogos);
            when(jogoMapper.toDTOList(jogos)).thenReturn(jogosDTO);

            // Act
            List<JogoDTO> resultado = jogoService.listarPorFiltro(filtro);

            // Assert
            assertThat(resultado).isNotEmpty();
            assertThat(resultado).hasSize(1);
            verify(jogoRepository).findByFilter(filtro);
        }
    }
}
