package br.com.solides.placar.event.internal;

import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para eventos internos.
 * Valida a criação e propriedades dos eventos de domínio.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@DisplayName("Eventos Internos - Testes Unitários")
class JogoEventTest {

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
    @DisplayName("JogoEvent (Classe Base)")
    class JogoEventBaseTests {

        @Test
        @DisplayName("Deve herdar de ApplicationEvent")
        void deveHerdarDeApplicationEvent() {
            // Arrange
            JogoCriadoEvent evento = new JogoCriadoEvent(source, jogoDTO);

            // Assert
            assertThat(evento).isInstanceOf(org.springframework.context.ApplicationEvent.class);
        }

        @Test
        @DisplayName("Deve conter jogo e operação")
        void deveConterJogoEOperacao() {
            // Arrange
            JogoCriadoEvent evento = new JogoCriadoEvent(source, jogoDTO);

            // Assert
            assertThat(evento.getJogo()).isEqualTo(jogoDTO);
            assertThat(evento.getOperacao()).isEqualTo("CRIADO");
            assertThat(evento.getSource()).isEqualTo(source);
        }

        @Test
        @DisplayName("Deve manter referência do source")
        void deveManterReferenciaDoSource() {
            // Arrange
            Object customSource = "custom-source";
            JogoCriadoEvent evento = new JogoCriadoEvent(customSource, jogoDTO);

            // Assert
            assertThat(evento.getSource()).isEqualTo(customSource);
        }
    }

    @Nested
    @DisplayName("JogoCriadoEvent")
    class JogoCriadoEventTests {

        @Test
        @DisplayName("Deve criar evento com operação CRIADO")
        void deveCriarEventoComOperacaoCriado() {
            // Act
            JogoCriadoEvent evento = new JogoCriadoEvent(source, jogoDTO);

            // Assert
            assertThat(evento.getOperacao()).isEqualTo("CRIADO");
            assertThat(evento.getJogo()).isEqualTo(jogoDTO);
        }

        @Test
        @DisplayName("Deve ser do tipo JogoEvent")
        void deveSerDoTipoJogoEvent() {
            // Act
            JogoCriadoEvent evento = new JogoCriadoEvent(source, jogoDTO);

            // Assert
            assertThat(evento).isInstanceOf(JogoEvent.class);
        }
    }

    @Nested
    @DisplayName("JogoAtualizadoEvent")
    class JogoAtualizadoEventTests {

        @Test
        @DisplayName("Deve criar evento com operação ATUALIZADO")
        void deveCriarEventoComOperacaoAtualizado() {
            // Act
            JogoAtualizadoEvent evento = new JogoAtualizadoEvent(source, jogoDTO);

            // Assert
            assertThat(evento.getOperacao()).isEqualTo("ATUALIZADO");
            assertThat(evento.getJogo()).isEqualTo(jogoDTO);
        }

        @Test
        @DisplayName("Deve preservar dados do jogo atualizado")
        void devePreservarDadosDoJogoAtualizado() {
            // Arrange
            jogoDTO.setTimeA("Santos");
            jogoDTO.setTimeB("Palmeiras");
            jogoDTO.setStatus(StatusJogo.EM_ANDAMENTO);

            // Act
            JogoAtualizadoEvent evento = new JogoAtualizadoEvent(source, jogoDTO);

            // Assert
            assertThat(evento.getJogo().getTimeA()).isEqualTo("Santos");
            assertThat(evento.getJogo().getTimeB()).isEqualTo("Palmeiras");
            assertThat(evento.getJogo().getStatus()).isEqualTo(StatusJogo.EM_ANDAMENTO);
        }
    }

    @Nested
    @DisplayName("JogoIniciadoEvent")
    class JogoIniciadoEventTests {

        @Test
        @DisplayName("Deve criar evento com operação INICIADO")
        void deveCriarEventoComOperacaoIniciado() {
            // Arrange
            jogoDTO.setStatus(StatusJogo.EM_ANDAMENTO);

            // Act
            JogoIniciadoEvent evento = new JogoIniciadoEvent(source, jogoDTO);

            // Assert
            assertThat(evento.getOperacao()).isEqualTo("INICIADO");
            assertThat(evento.getJogo().getStatus()).isEqualTo(StatusJogo.EM_ANDAMENTO);
        }
    }

    @Nested
    @DisplayName("PlacarAtualizadoInternalEvent")
    class PlacarAtualizadoInternalEventTests {

        @Test
        @DisplayName("Deve criar evento com operação PLACAR_ATUALIZADO")
        void deveCriarEventoComOperacaoPlacarAtualizado() {
            // Arrange
            jogoDTO.setPlacarA(2);
            jogoDTO.setPlacarB(1);
            jogoDTO.setStatus(StatusJogo.EM_ANDAMENTO);

            // Act
            PlacarAtualizadoInternalEvent evento = new PlacarAtualizadoInternalEvent(source, jogoDTO);

            // Assert
            assertThat(evento.getOperacao()).isEqualTo("PLACAR_ATUALIZADO");
            assertThat(evento.getJogo().getPlacarA()).isEqualTo(2);
            assertThat(evento.getJogo().getPlacarB()).isEqualTo(1);
        }

        @Test
        @DisplayName("Deve preservar placar atualizado")
        void devePreservarPlacarAtualizado() {
            // Arrange
            jogoDTO.setPlacarA(5);
            jogoDTO.setPlacarB(3);
            jogoDTO.setTempoDeJogo(90);

            // Act
            PlacarAtualizadoInternalEvent evento = new PlacarAtualizadoInternalEvent(source, jogoDTO);

            // Assert
            JogoDTO jogoEvento = evento.getJogo();
            assertThat(jogoEvento.getPlacarA()).isEqualTo(5);
            assertThat(jogoEvento.getPlacarB()).isEqualTo(3);
            assertThat(jogoEvento.getTempoDeJogo()).isEqualTo(90);
        }
    }

    @Nested
    @DisplayName("JogoFinalizadoEvent")
    class JogoFinalizadoEventTests {

        @Test
        @DisplayName("Deve criar evento com operação FINALIZADO")
        void deveCriarEventoComOperacaoFinalizado() {
            // Arrange
            jogoDTO.setStatus(StatusJogo.FINALIZADO);
            jogoDTO.setPlacarA(3);
            jogoDTO.setPlacarB(1);

            // Act
            JogoFinalizadoEvent evento = new JogoFinalizadoEvent(source, jogoDTO);

            // Assert
            assertThat(evento.getOperacao()).isEqualTo("FINALIZADO");
            assertThat(evento.getJogo().getStatus()).isEqualTo(StatusJogo.FINALIZADO);
            assertThat(evento.getJogo().getPlacarA()).isEqualTo(3);
            assertThat(evento.getJogo().getPlacarB()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Comportamentos Gerais")
    class ComportamentosGeraisTests {

        @Test
        @DisplayName("Eventos devem preservar referência do JogoDTO")
        void EventosDevePreservarReferenciaDoJogoDTO() {
            // Arrange
            JogoDTO jogoOriginal = jogoDTO;

            // Act
            JogoCriadoEvent eventoCriado = new JogoCriadoEvent(source, jogoOriginal);
            JogoAtualizadoEvent eventoAtualizado = new JogoAtualizadoEvent(source, jogoOriginal);
            JogoIniciadoEvent eventoIniciado = new JogoIniciadoEvent(source, jogoOriginal);
            PlacarAtualizadoInternalEvent eventoPlacar = new PlacarAtualizadoInternalEvent(source, jogoOriginal);
            JogoFinalizadoEvent eventoFinalizado = new JogoFinalizadoEvent(source, jogoOriginal);

            // Assert - verificar que todos referenciam o mesmo objeto
            assertThat(eventoCriado.getJogo()).isSameAs(jogoOriginal);
            assertThat(eventoAtualizado.getJogo()).isSameAs(jogoOriginal);
            assertThat(eventoIniciado.getJogo()).isSameAs(jogoOriginal);
            assertThat(eventoPlacar.getJogo()).isSameAs(jogoOriginal);
            assertThat(eventoFinalizado.getJogo()).isSameAs(jogoOriginal);
        }

        @Test
        @DisplayName("Eventos devem ter operações específicas únicas")
        void EventosDevemTerOperacoesEspecificasUnicas() {
            // Act
            JogoCriadoEvent eventoCriado = new JogoCriadoEvent(source, jogoDTO);
            JogoAtualizadoEvent eventoAtualizado = new JogoAtualizadoEvent(source, jogoDTO);
            JogoIniciadoEvent eventoIniciado = new JogoIniciadoEvent(source, jogoDTO);
            PlacarAtualizadoInternalEvent eventoPlacar = new PlacarAtualizadoInternalEvent(source, jogoDTO);
            JogoFinalizadoEvent eventoFinalizado = new JogoFinalizadoEvent(source, jogoDTO);

            // Assert - verificar que cada operação é única
            assertThat(eventoCriado.getOperacao()).isEqualTo("CRIADO");
            assertThat(eventoAtualizado.getOperacao()).isEqualTo("ATUALIZADO");
            assertThat(eventoIniciado.getOperacao()).isEqualTo("INICIADO");
            assertThat(eventoPlacar.getOperacao()).isEqualTo("PLACAR_ATUALIZADO");
            assertThat(eventoFinalizado.getOperacao()).isEqualTo("FINALIZADO");

            // Verificar que são todas diferentes
            assertThat(eventoCriado.getOperacao()).isNotEqualTo(eventoAtualizado.getOperacao());
            assertThat(eventoIniciado.getOperacao()).isNotEqualTo(eventoPlacar.getOperacao());
            assertThat(eventoFinalizado.getOperacao()).isNotEqualTo(eventoCriado.getOperacao());
        }
    }
}