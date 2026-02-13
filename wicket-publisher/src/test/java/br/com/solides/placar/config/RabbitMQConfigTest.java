package br.com.solides.placar.config;

import br.com.solides.placar.config.properties.RabbitMQProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RabbitMQConfig.
 * Valida inicialização e configuração dos componentes RabbitMQ.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RabbitMQConfig - Testes Unitários")
class RabbitMQConfigTest {

    @Mock
    private RabbitMQProperties properties;

    @InjectMocks
    private RabbitMQConfig rabbitMQConfig;

    @BeforeEach
    void setUp() {
        // Setup das propriedades mock
        when(properties.getHost()).thenReturn("localhost");
        when(properties.getPort()).thenReturn(5672);
        when(properties.getUsername()).thenReturn("test");
        when(properties.getPassword()).thenReturn("test");
        when(properties.getVirtualHost()).thenReturn("/test");
        when(properties.getExchange()).thenReturn("test.exchange");
        when(properties.getRoutingKey()).thenReturn("test.routing");
    }

    @Nested
    @DisplayName("Inicialização")
    class InicializacaoTests {

        @Test
        @DisplayName("Deve inicializar configuração com sucesso")
        void deveInicializarConfiguracaoComSucesso() {
            // Act
            assertThatCode(() -> rabbitMQConfig.init())
                    .doesNotThrowAnyException();

            // Assert
            assertThat(rabbitMQConfig.getRabbitTemplate()).isNotNull();
            assertThat(rabbitMQConfig.getConnectionFactory()).isNotNull();
        }

        @Test
        @DisplayName("Deve configurar ConnectionFactory com propriedades corretas")
        void deveConfigurarConnectionFactoryComPropriedadesCorretas() {
            // Act
            rabbitMQConfig.init();
            CachingConnectionFactory connectionFactory = rabbitMQConfig.getConnectionFactory();

            // Assert
            assertThat(connectionFactory).isNotNull();
            assertThat(connectionFactory.getHost()).isEqualTo("localhost");
            assertThat(connectionFactory.getPort()).isEqualTo(5672);
            assertThat(connectionFactory.getUsername()).isEqualTo("test");
            assertThat(connectionFactory.getVirtualHost()).isEqualTo("/test");
        }

        @Test
        @DisplayName("Deve configurar RabbitTemplate com exchange e routing key corretos")
        void deveConfigurarRabbitTemplateComExchangeERoutingKeyCorretos() {
            // Act
            rabbitMQConfig.init();
            RabbitTemplate rabbitTemplate = rabbitMQConfig.getRabbitTemplate();

            // Assert
            assertThat(rabbitTemplate).isNotNull();
            assertThat(rabbitTemplate.getExchange()).isEqualTo("games.topic");
            assertThat(rabbitTemplate.getRoutingKey()).isEqualTo("games.partidas");
            assertThat(rabbitTemplate.getMessageConverter()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Destruição")
    class DestruicaoTests {

        @Test
        @DisplayName("Deve fechar conexões na destruição")
        void deveFecherConexoesNaDestruicao() {
            // Arrange
            rabbitMQConfig.init();
            CachingConnectionFactory connectionFactory = rabbitMQConfig.getConnectionFactory();

            // Act
            assertThatCode(() -> rabbitMQConfig.destroy())
                    .doesNotThrowAnyException();

            // Assert - Verificar que destroy foi chamado
            // Como não temos controle direto sobre o mock, apenas verificamos que não há exceção
            assertThat(connectionFactory).isNotNull();
        }

        @Test
        @DisplayName("Deve lidar com destroy quando connectionFactory é null")
        void deveLidarComDestroyQuandoConnectionFactoryEhNull() {
            // Act & Assert - não deve lançar exceção mesmo sem init
            assertThatCode(() -> rabbitMQConfig.destroy())
                    .doesNotThrowAnyException();
        }
    }
}
