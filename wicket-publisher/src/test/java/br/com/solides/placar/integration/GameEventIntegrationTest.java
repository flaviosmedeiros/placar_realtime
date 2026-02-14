package br.com.solides.placar.integration;

import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.service.publisher.GameEventPublisher;
import br.com.solides.placar.service.listener.GameEventListener;
import br.com.solides.placar.mapper.JogoMapper;
import br.com.solides.placar.repository.JogoRepository;
import br.com.solides.placar.entity.Jogo;
import br.com.solides.placar.shared.dto.CriarJogoDTO;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes de integração para o fluxo completo de eventos.
 * Valida a integração entre JogoService, eventos, listener e publisher.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Integração - Fluxo Completo de Eventos")
class GameEventIntegrationTest {

    @Mock
    private JogoRepository jogoRepository;

    @Mock
    private JogoMapper jogoMapper;

    @Mock
    private GameEventPublisher gameEventPublisher;

    private GameEventListener gameEventListener;
    private JogoService jogoService;
    private ApplicationEventPublisher eventPublisher;

    private Jogo jogoEntity;
    private JogoDTO jogoDTO;
    private CriarJogoDTO criarJogoDTO;

    @BeforeEach
    void setUp() {
        // Setup dos objetos de teste
        setupTestData();

        // Configurar dependências
        gameEventListener = new GameEventListener();
        setField(gameEventListener, "gameEventPublisher", gameEventPublisher);

        // Setup do event publisher que conecta JogoService com GameEventListener
        eventPublisher = event -> {
            // Simular comportamento do Spring ApplicationEventPublisher
            if (event instanceof br.com.solides.placar.event.internal.JogoEvent) {
                gameEventListener.handleJogoEvent((br.com.solides.placar.event.internal.JogoEvent) event);
            }
        };

        jogoService = new JogoService();
        setField(jogoService, "jogoRepository", jogoRepository);
        setField(jogoService, "jogoMapper", jogoMapper);
        setField(jogoService, "eventPublisher", eventPublisher);
    }

    private void setupTestData() {
        jogoEntity = Jogo.builder()
                .id(1L)
                .timeA("Flamengo")
                .timeB("Vasco")
                .placarA(0)
                .placarB(0)
                .status(StatusJogo.NAO_INICIADO)
                .dataHoraPartida(LocalDateTime.now().plusDays(1))
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
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

    @Test
    @DisplayName("Fluxo completo: Criar jogo → Evento interno → Publisher RabbitMQ")
    void fluxoCompletoCriarJogoEventoPublisher() {
        // Arrange
        when(jogoMapper.toEntity(criarJogoDTO)).thenReturn(jogoEntity);
        when(jogoRepository.save(any(Jogo.class))).thenReturn(jogoEntity);
        when(jogoMapper.toDTO(jogoEntity)).thenReturn(jogoDTO);

        // Act
        JogoDTO resultado = jogoService.criarJogo(criarJogoDTO);

        // Assert
        // 1. Verificar que o jogo foi criado corretamente
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTimeA()).isEqualTo("Flamengo");

        // 2. Verificar que o repository foi chamado
        verify(jogoRepository).save(any(Jogo.class));

        // 3. Verificar que o evento foi publicado no RabbitMQ
        verify(gameEventPublisher).publishGameEvent(jogoDTO, "CRIADO");
    }

    @Test
    @DisplayName("Fluxo completo: Iniciar jogo → Evento interno → Publisher RabbitMQ")
    void fluxoCompletoIniciarJogoEventoPublisher() {
        // Arrange
        Long jogoId = 1L;
        jogoEntity.setStatus(StatusJogo.NAO_INICIADO);
        
        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogoEntity));
        when(jogoRepository.save(any(Jogo.class))).thenReturn(jogoEntity);
        when(jogoMapper.toDTO(any(Jogo.class))).thenReturn(jogoDTO);

        // Act
        JogoDTO resultado = jogoService.iniciarJogo(jogoId);

        // Assert
        // 1. Verificar que o jogo foi encontrado e atualizado
        assertThat(resultado).isNotNull();
        verify(jogoRepository).findById(jogoId);
        verify(jogoRepository).save(argThat(jogo -> jogo.getStatus() == StatusJogo.EM_ANDAMENTO));

        // 2. Verificar que o evento foi publicado no RabbitMQ
        verify(gameEventPublisher).publishGameEvent(jogoDTO, "INICIADO");
    }

    @Test
    @DisplayName("Fluxo completo: Atualizar placar → Evento interno → Publisher RabbitMQ")
    void fluxoCompletoAtualizarPlacarEventoPublisher() {
        // Arrange
        Long jogoId = 1L;
        Integer placarA = 2;
        Integer placarB = 1;
        
        jogoEntity.setStatus(StatusJogo.EM_ANDAMENTO);
        jogoDTO.setStatus(StatusJogo.EM_ANDAMENTO);
        jogoDTO.setPlacarA(placarA);
        jogoDTO.setPlacarB(placarB);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogoEntity));
        when(jogoRepository.save(any(Jogo.class))).thenReturn(jogoEntity);
        when(jogoMapper.toDTO(any(Jogo.class))).thenReturn(jogoDTO);

        // Act
        JogoDTO resultado = jogoService.atualizarPlacar(jogoId, placarA, placarB);

        // Assert
        // 1. Verificar que o placar foi atualizado
        assertThat(resultado).isNotNull();
        verify(jogoRepository).save(argThat(jogo -> 
            jogo.getPlacarA().equals(placarA) && jogo.getPlacarB().equals(placarB)
        ));

        // 2. Verificar que o evento foi publicado no RabbitMQ
        verify(gameEventPublisher).publishGameEvent(jogoDTO, "PLACAR_ATUALIZADO");
    }

    @Test
    @DisplayName("Fluxo completo: Finalizar jogo → Evento interno → Publisher RabbitMQ")
    void fluxoCompletoFinalizarJogoEventoPublisher() {
        // Arrange
        Long jogoId = 1L;
        jogoEntity.setStatus(StatusJogo.EM_ANDAMENTO);
        jogoDTO.setStatus(StatusJogo.FINALIZADO);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogoEntity));
        when(jogoRepository.save(any(Jogo.class))).thenReturn(jogoEntity);
        when(jogoMapper.toDTO(any(Jogo.class))).thenReturn(jogoDTO);

        // Act
        JogoDTO resultado = jogoService.finalizarJogo(jogoId);

        // Assert
        // 1. Verificar que o jogo foi finalizado
        assertThat(resultado).isNotNull();
        verify(jogoRepository).save(argThat(jogo -> 
            jogo.getStatus() == StatusJogo.FINALIZADO && jogo.getDataHoraEncerramento() != null
        ));

        // 2. Verificar que o evento foi publicado no RabbitMQ
        verify(gameEventPublisher).publishGameEvent(jogoDTO, "FINALIZADO");
    }

    @Test
    @DisplayName("Fluxo de erro: Falha no publisher não afeta transação principal")
    void fluxoErroFalhaPublisherNaoAfetaTransacao() {
        // Arrange
        when(jogoMapper.toEntity(criarJogoDTO)).thenReturn(jogoEntity);
        when(jogoRepository.save(any(Jogo.class))).thenReturn(jogoEntity);
        when(jogoMapper.toDTO(jogoEntity)).thenReturn(jogoDTO);
        
        // Simular erro no publisher
        doThrow(new RuntimeException("Erro RabbitMQ")).when(gameEventPublisher)
                .publishGameEvent(any(), any());

        // Act & Assert - não deve lançar exceção
        assertThatCode(() -> jogoService.criarJogo(criarJogoDTO))
                .doesNotThrowAnyException();

        // Verificar que a transação principal foi concluída
        verify(jogoRepository).save(any(Jogo.class));
        verify(gameEventPublisher).publishGameEvent(any(), any());
    }

    @Test
    @DisplayName("Fluxo sequencial: Criar → Iniciar → Atualizar Placar → Finalizar")
    void fluxoSequencialCompletoDoJogo() {
        // Setup para múltiplas operações
        when(jogoMapper.toEntity(any(CriarJogoDTO.class))).thenReturn(jogoEntity);
        when(jogoRepository.save(any())).thenReturn(jogoEntity);
        when(jogoRepository.findById(any())).thenReturn(Optional.of(jogoEntity));
        when(jogoMapper.toDTO(any())).thenReturn(jogoDTO);

        // 1. Criar jogo
        JogoDTO jogoCriado = jogoService.criarJogo(criarJogoDTO);
        assertThat(jogoCriado).isNotNull();

        // 2. Iniciar jogo
        jogoEntity.setStatus(StatusJogo.NAO_INICIADO);
        JogoDTO jogoIniciado = jogoService.iniciarJogo(1L);
        assertThat(jogoIniciado).isNotNull();

        // 3. Atualizar placar
        jogoEntity.setStatus(StatusJogo.EM_ANDAMENTO);
        JogoDTO jogoComPlacar = jogoService.atualizarPlacar(1L, 3, 1);
        assertThat(jogoComPlacar).isNotNull();

        // 4. Finalizar jogo
        JogoDTO jogoFinalizado = jogoService.finalizarJogo(1L);
        assertThat(jogoFinalizado).isNotNull();

        // Verificar que todos os eventos foram publicados
        verify(gameEventPublisher).publishGameEvent(any(), eq("CRIADO"));
        verify(gameEventPublisher).publishGameEvent(any(), eq("INICIADO"));
        verify(gameEventPublisher).publishGameEvent(any(), eq("PLACAR_ATUALIZADO"));
        verify(gameEventPublisher).publishGameEvent(any(), eq("FINALIZADO"));

        // Verificar total de 4 publicações
        verify(gameEventPublisher, times(4)).publishGameEvent(any(), any());
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao configurar campo de teste: " + fieldName, e);
        }
    }
}
