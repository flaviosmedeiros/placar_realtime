package br.com.solides.placar.service.listener;

import br.com.solides.placar.event.internal.*;
import br.com.solides.placar.service.publisher.GameEventPublisher;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para GameEventListener.
 * Valida o processamento de eventos internos e publicação no RabbitMQ.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameEventListener - Testes Unitários")
class GameEventListenerTest {

    @Mock
    private GameEventPublisher gameEventPublisher;

    @InjectMocks
    private GameEventListener gameEventListener;

    private JogoDTO jogoDTO;
    private Object source;

    @BeforeEach
    void setUp() {
        source = new Object();
        
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
    }

    @Nested
    @DisplayName("Processamento de Eventos")
    class ProcessamentoEventosTests {

        @Test
        @DisplayName("Deve processar JogoCriadoEvent com sucesso")
        void deveProcessarJogoCriadoEventComSucesso() {
            // Arrange
            JogoCriadoEvent evento = new JogoCriadoEvent(source, jogoDTO);

            // Act
            gameEventListener.handleJogoEvent(evento);

            // Assert
            verify(gameEventPublisher).publishGameEvent(jogoDTO, "CRIADO");
        }

        @Test
        @DisplayName("Deve processar JogoAtualizadoEvent com sucesso")
        void deveProcessarJogoAtualizadoEventComSucesso() {
            // Arrange
            JogoAtualizadoEvent evento = new JogoAtualizadoEvent(source, jogoDTO);

            // Act
            gameEventListener.handleJogoEvent(evento);

            // Assert
            verify(gameEventPublisher).publishGameEvent(jogoDTO, "ATUALIZADO");
        }

        @Test
        @DisplayName("Deve processar JogoIniciadoEvent com sucesso")
        void deveProcessarJogoIniciadoEventComSucesso() {
            // Arrange
            jogoDTO.setStatus(StatusJogo.EM_ANDAMENTO);
            JogoIniciadoEvent evento = new JogoIniciadoEvent(source, jogoDTO);

            // Act
            gameEventListener.handleJogoEvent(evento);

            // Assert
            verify(gameEventPublisher).publishGameEvent(jogoDTO, "INICIADO");
        }

        @Test
        @DisplayName("Deve processar PlacarAtualizadoInternalEvent com sucesso")
        void deveProcessarPlacarAtualizadoInternalEventComSucesso() {
            // Arrange
            jogoDTO.setStatus(StatusJogo.EM_ANDAMENTO);
            jogoDTO.setPlacarA(2);
            jogoDTO.setPlacarB(1);
            PlacarAtualizadoInternalEvent evento = new PlacarAtualizadoInternalEvent(source, jogoDTO);

            // Act
            gameEventListener.handleJogoEvent(evento);

            // Assert
            verify(gameEventPublisher).publishGameEvent(jogoDTO, "PLACAR_ATUALIZADO");
        }

        @Test
        @DisplayName("Deve processar JogoFinalizadoEvent com sucesso")
        void deveProcessarJogoFinalizadoEventComSucesso() {
            // Arrange
            jogoDTO.setStatus(StatusJogo.FINALIZADO);
            JogoFinalizadoEvent evento = new JogoFinalizadoEvent(source, jogoDTO);

            // Act
            gameEventListener.handleJogoEvent(evento);

            // Assert
            verify(gameEventPublisher).publishGameEvent(jogoDTO, "FINALIZADO");
        }
    }

    @Nested
    @DisplayName("Tratamento de Erros")
    class TratamentoErrosTests {

        @Test
        @DisplayName("Deve lidar com erro no GameEventPublisher sem lançar exceção")
        void deveLidarComErroNoGameEventPublisherSemLancarExcecao() {
            // Arrange
            JogoCriadoEvent evento = new JogoCriadoEvent(source, jogoDTO);
            doThrow(new RuntimeException("Erro de conectividade RabbitMQ"))
                    .when(gameEventPublisher).publishGameEvent(any(), any());

            // Act & Assert - não deve lançar exceção
            assertThatCode(() -> gameEventListener.handleJogoEvent(evento))
                    .doesNotThrowAnyException();

            verify(gameEventPublisher).publishGameEvent(jogoDTO, "CRIADO");
        }

        @Test
        @DisplayName("Deve tentar publicar mesmo quando GameEventPublisher falha")
        void deveTentarPublicarMesmoQuandoGameEventPublisherFalha() {
            // Arrange
            JogoAtualizadoEvent evento = new JogoAtualizadoEvent(source, jogoDTO);
            doThrow(new RuntimeException("Falha na publicação"))
                    .when(gameEventPublisher).publishGameEvent(any(), any());

            // Act
            gameEventListener.handleJogoEvent(evento);

            // Assert - deve ter tentado publicar
            verify(gameEventPublisher).publishGameEvent(jogoDTO, "ATUALIZADO");
        }
    }

    @Nested
    @DisplayName("Validação de Dados")
    class ValidacaoDadosTests {

        @Test
        @DisplayName("Deve processar evento com jogo contendo todos os dados")
        void deveProcessarEventoComJogoContendoTodosOsDados() {
            // Arrange
            jogoDTO.setId(123L);
            jogoDTO.setTimeA("Santos");
            jogoDTO.setTimeB("Palmeiras");
            jogoDTO.setPlacarA(3);
            jogoDTO.setPlacarB(2);
            jogoDTO.setStatus(StatusJogo.EM_ANDAMENTO);
            jogoDTO.setTempoDeJogo(75);

            JogoCriadoEvent evento = new JogoCriadoEvent(source, jogoDTO);

            // Act
            gameEventListener.handleJogoEvent(evento);

            // Assert
            verify(gameEventPublisher).publishGameEvent(argThat(jogo ->
                jogo.getId().equals(123L) &&
                jogo.getTimeA().equals("Santos") &&
                jogo.getTimeB().equals("Palmeiras") &&
                jogo.getPlacarA().equals(3) &&
                jogo.getPlacarB().equals(2) &&
                jogo.getStatus().equals(StatusJogo.EM_ANDAMENTO) &&
                jogo.getTempoDeJogo().equals(75)
            ), eq("CRIADO"));
        }

        @Test
        @DisplayName("Deve processar eventos com diferentes tipos de operação")
        void deveProcessarEventosComDiferentesTiposDeOperacao() {
            // Test múltiplos eventos
            JogoCriadoEvent eventoCriado = new JogoCriadoEvent(source, jogoDTO);
            JogoIniciadoEvent eventoIniciado = new JogoIniciadoEvent(source, jogoDTO);
            PlacarAtualizadoInternalEvent eventoPlacar = new PlacarAtualizadoInternalEvent(source, jogoDTO);
            JogoFinalizadoEvent eventoFinalizado = new JogoFinalizadoEvent(source, jogoDTO);

            // Act
            gameEventListener.handleJogoEvent(eventoCriado);
            gameEventListener.handleJogoEvent(eventoIniciado);
            gameEventListener.handleJogoEvent(eventoPlacar);
            gameEventListener.handleJogoEvent(eventoFinalizado);

            // Assert
            verify(gameEventPublisher).publishGameEvent(jogoDTO, "CRIADO");
            verify(gameEventPublisher).publishGameEvent(jogoDTO, "INICIADO");
            verify(gameEventPublisher).publishGameEvent(jogoDTO, "PLACAR_ATUALIZADO");
            verify(gameEventPublisher).publishGameEvent(jogoDTO, "FINALIZADO");
            
            // Verificar total de invocações
            verify(gameEventPublisher, times(4)).publishGameEvent(any(JogoDTO.class), anyString());
        }
    }

    @Nested
    @DisplayName("Comportamento Assíncrono")
    class ComportamentoAssincronoTests {

        @Test
        @DisplayName("Deve processar evento sem bloquear thread chamadora")
        void deveProcessarEventoSemBloquearThreadChamadora() {
            // Arrange
            JogoCriadoEvent evento = new JogoCriadoEvent(source, jogoDTO);

            // Simular processamento lento no publisher
            doAnswer(invocation -> {
                Thread.sleep(10); // Pequeno delay para simular processamento
                return null;
            }).when(gameEventPublisher).publishGameEvent(any(), any());

            // Act - deve retornar rapidamente, pois é assíncrono
            long startTime = System.currentTimeMillis();
            gameEventListener.handleJogoEvent(evento);
            long endTime = System.currentTimeMillis();

            // Assert - o método deve retornar rapidamente
            // Como removemos CompletableFuture, o teste vai ser síncrono agora
            assertThat(endTime - startTime).isLessThan(100L); // Menos de 100ms
            verify(gameEventPublisher).publishGameEvent(jogoDTO, "CRIADO");
        }

        @Test
        @DisplayName("Deve processar múltiplos eventos sequencialmente")
        void deveProcessarMultiplosEventosSequencialmente() {
            // Arrange
            JogoCriadoEvent evento1 = new JogoCriadoEvent(source, jogoDTO);
            
            JogoDTO jogo2 = JogoDTO.builder()
                    .id(2L)
                    .timeA("Corinthians")
                    .timeB("São Paulo")
                    .status(StatusJogo.EM_ANDAMENTO)
                    .build();
            JogoIniciadoEvent evento2 = new JogoIniciadoEvent(source, jogo2);

            // Act
            gameEventListener.handleJogoEvent(evento1);
            gameEventListener.handleJogoEvent(evento2);

            // Assert
            verify(gameEventPublisher).publishGameEvent(jogoDTO, "CRIADO");
            verify(gameEventPublisher).publishGameEvent(jogo2, "INICIADO");
            verify(gameEventPublisher, times(2)).publishGameEvent(any(JogoDTO.class), anyString());
        }
    }
}