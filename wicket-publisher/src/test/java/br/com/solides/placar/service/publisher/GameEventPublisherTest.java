package br.com.solides.placar.service.publisher;

import br.com.solides.placar.config.RabbitMQConfig;
import br.com.solides.placar.shared.constants.RabbitMQConstants;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para GameEventPublisher.
 * Valida a publicação de eventos no RabbitMQ e conversão de DTOs.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GameEventPublisher - Testes Unitários")
class GameEventPublisherTest {

    @Mock
    private RabbitMQConfig rabbitMQConfig;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private GameEventPublisher gameEventPublisher;

    @Captor
    private ArgumentCaptor<PlacarAtualizadoEvent> eventCaptor;

    @Captor
    private ArgumentCaptor<String> exchangeCaptor;

    @Captor
    private ArgumentCaptor<String> routingKeyCaptor;

    private JogoDTO jogoDTO;

    @BeforeEach
    void setUp() {
        jogoDTO = JogoDTO.builder()
                .id(1L)
                .timeA("Flamengo")
                .timeB("Vasco")
                .placarA(2)
                .placarB(1)
                .status(StatusJogo.EM_ANDAMENTO)
                .tempoDeJogo(45)
                .dataPartida(LocalDate.now())
                .horaPartida("20:00")
                .dataHoraEncerramento(null)
                .build();

        // Setup do mock RabbitMQConfig
        when(rabbitMQConfig.getRabbitTemplate()).thenReturn(rabbitTemplate);
    }

    @Nested
    @DisplayName("Publicação de Eventos")
    class PublicacaoEventosTests {

        @Test
        @DisplayName("Deve publicar evento no RabbitMQ com sucesso")
        void devePublicarEventoNoRabbitMQComSucesso() {
            // Arrange
            String operacao = "CRIADO";

            // Act
            gameEventPublisher.publishGameEvent(jogoDTO, operacao);

            // Assert
            verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                eventCaptor.capture(),
                any(MessagePostProcessor.class)
            );

            // Validar parâmetros de envio
            assertThat(exchangeCaptor.getValue()).isEqualTo(RabbitMQConstants.EXCHANGE_NAME);
            assertThat(routingKeyCaptor.getValue()).isEqualTo(RabbitMQConstants.ROUTING_KEY_PLACAR_ATUALIZADO);

            // Validar evento convertido
            PlacarAtualizadoEvent evento = eventCaptor.getValue();
            assertThat(evento.getId()).isEqualTo(jogoDTO.getId());
            assertThat(evento.getTimeA()).isEqualTo(jogoDTO.getTimeA());
            assertThat(evento.getTimeB()).isEqualTo(jogoDTO.getTimeB());
            assertThat(evento.getPlacarA()).isEqualTo(jogoDTO.getPlacarA());
            assertThat(evento.getPlacarB()).isEqualTo(jogoDTO.getPlacarB());
            assertThat(evento.getStatus()).isEqualTo(jogoDTO.getStatus());
            assertThat(evento.getTempoDeJogo()).isEqualTo(jogoDTO.getTempoDeJogo());
        }

        @Test
        @DisplayName("Deve publicar evento para operação PLACAR_ATUALIZADO")
        void devePublicarEventoParaOperacaoPlacarAtualizado() {
            // Arrange
            String operacao = "PLACAR_ATUALIZADO";

            // Act
            gameEventPublisher.publishGameEvent(jogoDTO, operacao);

            // Assert
            verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConstants.EXCHANGE_NAME),
                eq(RabbitMQConstants.ROUTING_KEY_PLACAR_ATUALIZADO),
                eventCaptor.capture(),
                any(MessagePostProcessor.class)
            );

            PlacarAtualizadoEvent evento = eventCaptor.getValue();
            assertThat(evento.getPlacarA()).isEqualTo(2);
            assertThat(evento.getPlacarB()).isEqualTo(1);
        }

        @Test
        @DisplayName("Deve publicar evento para jogo finalizado")
        void devePublicarEventoParaJogoFinalizado() {
            // Arrange
            jogoDTO.setStatus(StatusJogo.FINALIZADO);
            jogoDTO.setDataHoraEncerramento(LocalDateTime.now());
            String operacao = "FINALIZADO";

            // Act
            gameEventPublisher.publishGameEvent(jogoDTO, operacao);

            // Assert
            verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                eventCaptor.capture(),
                any(MessagePostProcessor.class)
            );

            PlacarAtualizadoEvent evento = eventCaptor.getValue();
            assertThat(evento.getStatus()).isEqualTo(StatusJogo.FINALIZADO);
            assertThat(evento.getDataHoraEncerramento()).isNotNull();
        }

        @Test
        @DisplayName("Deve propagar erro quando RabbitTemplate lança exceção")
        void devePropagarErroQuandoRabbitTemplateLancaExcecao() {
            // Arrange
            doThrow(new RuntimeException("Erro de conectividade"))
                    .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(),
                            any(MessagePostProcessor.class));

            // Act & Assert
            assertThatThrownBy(() -> gameEventPublisher.publishGameEvent(jogoDTO, "TESTE"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro de conectividade");

            verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(),
                    any(MessagePostProcessor.class));
        }
    }

    @Nested
    @DisplayName("Conversão de DTOs")
    class ConversaoDTOsTests {

        @Test
        @DisplayName("Deve converter JogoDTO para PlacarAtualizadoEvent corretamente")
        void deveConverterJogoDTOParaPlacarAtualizadoEventCorretamente() {
            // Act
            gameEventPublisher.publishGameEvent(jogoDTO, "TESTE");

            // Assert
            verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                eventCaptor.capture(),
                any(MessagePostProcessor.class)
            );

            PlacarAtualizadoEvent evento = eventCaptor.getValue();
            
            // Verificar todos os campos
            assertThat(evento.getId()).isEqualTo(jogoDTO.getId());
            assertThat(evento.getTimeA()).isEqualTo(jogoDTO.getTimeA());
            assertThat(evento.getTimeB()).isEqualTo(jogoDTO.getTimeB());
            assertThat(evento.getPlacarA()).isEqualTo(jogoDTO.getPlacarA());
            assertThat(evento.getPlacarB()).isEqualTo(jogoDTO.getPlacarB());
            assertThat(evento.getStatus()).isEqualTo(jogoDTO.getStatus());
            assertThat(evento.getTempoDeJogo()).isEqualTo(jogoDTO.getTempoDeJogo());
            assertThat(evento.getDataHoraEncerramento()).isEqualTo(jogoDTO.getDataHoraEncerramento());
        }

        @Test
        @DisplayName("Deve lidar com data/hora de partida nula")
        void deveLidarComDataHoraDePartidaNula() {
            // Arrange
            jogoDTO.setDataPartida(null);
            jogoDTO.setHoraPartida(null);

            // Act
            gameEventPublisher.publishGameEvent(jogoDTO, "TESTE");

            // Assert
            verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                eventCaptor.capture(),
                any(MessagePostProcessor.class)
            );

            PlacarAtualizadoEvent evento = eventCaptor.getValue();
            // dataHoraInicioPartida deve ser null quando não há data/hora
            assertThat(evento.getDataHoraInicioPartida()).isNull();
        }

        @Test
        @DisplayName("Deve criar dataHoraInicioPartida a partir de data e hora separadas")
        void deveCriarDataHoraInicioPartidaAPartirDeDataEHoraSeparadas() {
            // Arrange
            LocalDate dataEsperada = LocalDate.of(2026, 2, 13);
            String horaEsperada = "20:30";
            
            jogoDTO.setDataPartida(dataEsperada);
            jogoDTO.setHoraPartida(horaEsperada);

            // Act
            gameEventPublisher.publishGameEvent(jogoDTO, "TESTE");

            // Assert
            verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                eventCaptor.capture(),
                any(MessagePostProcessor.class)
            );

            PlacarAtualizadoEvent evento = eventCaptor.getValue();
            assertThat(evento.getDataHoraInicioPartida()).isNotNull();
            assertThat(evento.getDataHoraInicioPartida().toLocalDate()).isEqualTo(dataEsperada);
            assertThat(evento.getDataHoraInicioPartida().getHour()).isEqualTo(20);
            assertThat(evento.getDataHoraInicioPartida().getMinute()).isEqualTo(30);
        }

        @Test
        @DisplayName("Deve lançar erro quando conversão de hora falha")
        void deveLancarErroQuandoConversaoDeHoraFalha() {
            // Arrange
            jogoDTO.setDataPartida(LocalDate.now());
            jogoDTO.setHoraPartida("formato-inválido");

            // Act & Assert
            assertThatThrownBy(() -> gameEventPublisher.publishGameEvent(jogoDTO, "TESTE"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Erro ao converter data e hora da partida");

            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(),
                    any(MessagePostProcessor.class));
        }
    }
}
