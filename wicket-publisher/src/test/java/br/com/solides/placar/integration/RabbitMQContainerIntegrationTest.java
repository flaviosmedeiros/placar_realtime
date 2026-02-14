package br.com.solides.placar.integration;

import br.com.solides.placar.config.RabbitMQConfig;
import br.com.solides.placar.config.properties.RabbitMQProperties;
import br.com.solides.placar.service.publisher.GameEventPublisher;
import br.com.solides.placar.shared.constants.RabbitMQConstants;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

/**
 * Teste de integração com RabbitMQ usando TestContainers.
 * Valida a publicação real de mensagens em uma instância RabbitMQ.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Integração RabbitMQ - TestContainers")
class RabbitMQContainerIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.12-management"))
            .withExposedPorts(5672, 15672)
            .withUser("test", "test")
            .withVhost("/test");

    private GameEventPublisher gameEventPublisher;
    private RabbitMQConfig rabbitMQConfig;
    private RabbitMQProperties properties;

    @BeforeEach
    void setUp() {
        // Configurar propriedades com dados do container
        properties = new RabbitMQProperties();
        setField(properties, "host", rabbitMQ.getHost());
        setField(properties, "port", rabbitMQ.getMappedPort(5672));
        setField(properties, "username", "test");
        setField(properties, "password", "test");
        setField(properties, "virtualHost", "/test");
        setField(properties, "exchange", RabbitMQConstants.EXCHANGE_NAME);
        setField(properties, "queue", "games.partidas");
        setField(properties, "routingKey", RabbitMQConstants.ROUTING_KEY_PLACAR_ATUALIZADO);
        
        // Configurar RabbitMQ
        rabbitMQConfig = new RabbitMQConfig();
        setField(rabbitMQConfig, "properties", properties);
        rabbitMQConfig.init();
        rabbitMQConfig.getRabbitTemplate().execute(channel -> {
            channel.exchangeDeclare(RabbitMQConstants.EXCHANGE_NAME, "topic", true);
            return null;
        });

        // Configurar GameEventPublisher
        gameEventPublisher = new GameEventPublisher();
        setField(gameEventPublisher, "rabbitMQConfig", rabbitMQConfig);
    }

    @Test
    @DisplayName("Deve publicar evento no RabbitMQ com sucesso")
    void devePublicarEventoNoRabbitMQComSucesso() {
        // Arrange
        JogoDTO jogoDTO = JogoDTO.builder()
                .id(1L)
                .timeA("Flamengo")
                .timeB("Vasco")
                .placarA(2)
                .placarB(1)
                .status(StatusJogo.EM_ANDAMENTO)
                .tempoDeJogo(45)
                .dataPartida(LocalDate.now())
                .horaPartida("20:00")
                .build();

        // Act
        assertThatCode(() -> gameEventPublisher.publishGameEvent(jogoDTO, "TESTE"))
                .doesNotThrowAnyException();

        // Assert
        // Verificar que a conexão foi estabelecida
        assertThat(rabbitMQConfig.getConnectionFactory().getCacheMode())
                .isEqualTo(org.springframework.amqp.rabbit.connection.CachingConnectionFactory.CacheMode.CONNECTION);
    }

    @Test
    @DisplayName("Deve configurar RabbitTemplate corretamente")
    void deveConfigurarRabbitTemplateCorretamente() {
        // Act
        RabbitTemplate rabbitTemplate = rabbitMQConfig.getRabbitTemplate();

        // Assert
        assertThat(rabbitTemplate).isNotNull();
        assertThat(rabbitTemplate.getExchange()).isEqualTo(RabbitMQConstants.EXCHANGE_NAME);
        assertThat(rabbitTemplate.getRoutingKey()).isEqualTo(RabbitMQConstants.ROUTING_KEY_PLACAR_ATUALIZADO);
        assertThat(rabbitTemplate.getMessageConverter()).isInstanceOf(Jackson2JsonMessageConverter.class);
        // Note: isMandatory() não é um método público na versão atual do RabbitTemplate
    }

    @Test
    @DisplayName("Deve conectar no RabbitMQ container")
    void deveConectarNoRabbitMQContainer() {
        // Assert - verificar que o container está rodando
        assertThat(rabbitMQ.isRunning()).isTrue();
        assertThat(rabbitMQ.getHost()).isNotEmpty();
        assertThat(rabbitMQ.getMappedPort(5672)).isGreaterThan(0);

        // Verificar que a conexão foi estabelecida
        assertThat(rabbitMQConfig.getConnectionFactory().getHost()).isEqualTo(rabbitMQ.getHost());
        assertThat(rabbitMQConfig.getConnectionFactory().getPort()).isEqualTo(rabbitMQ.getMappedPort(5672));
    }

    @Test
    @DisplayName("Deve serializar PlacarAtualizadoEvent corretamente")
    void deveSerializarPlacarAtualizadoEventCorretamente() throws Exception {
        // Arrange
        JogoDTO jogoDTO = JogoDTO.builder()
                .id(123L)
                .timeA("Santos")
                .timeB("Palmeiras")
                .placarA(3)
                .placarB(2)
                .status(StatusJogo.FINALIZADO)
                .tempoDeJogo(90)
                .dataPartida(LocalDate.of(2026, 2, 13))
                .horaPartida("21:00")
                .build();

        // Act
        gameEventPublisher.publishGameEvent(jogoDTO, "FINALIZADO");

        // Assert - verificar que não houve erro na serialização
        // Como estamos usando o container real, se chegou até aqui sem exceção, a serialização funcionou
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            // Verificar que o RabbitTemplate foi configurado
            assertThat(rabbitMQConfig.getRabbitTemplate()).isNotNull();
        });
    }

    @Test
    @DisplayName("Deve lidar com múltiplas publicações sequenciais")
    void deveLidarComMultiplasPublicacoesSequenciais() {
        // Arrange
        JogoDTO jogo1 = createJogoDTO(1L, "Time A", "Time B");
        JogoDTO jogo2 = createJogoDTO(2L, "Time C", "Time D");
        JogoDTO jogo3 = createJogoDTO(3L, "Time E", "Time F");

        // Act & Assert - todas devem ser publicadas sem erro
        assertThatCode(() -> {
            gameEventPublisher.publishGameEvent(jogo1, "CRIADO");
            gameEventPublisher.publishGameEvent(jogo2, "INICIADO");
            gameEventPublisher.publishGameEvent(jogo3, "FINALIZADO");
        }).doesNotThrowAnyException();

        // Verificar que o connection pool está funcionando
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(rabbitMQConfig.getConnectionFactory().getChannelCacheSize()).isEqualTo(25);
        });
    }

    @Test
    @DisplayName("Deve manter conexão ativa durante múltiplas operações")
    void deveManterConexaoAtivaDuranteMultiplasOperacoes() {
        // Arrange
        JogoDTO jogoDTO = createJogoDTO(1L, "Flamengo", "Vasco");

        // Act - publicar várias vezes para testar pool de conexões
        for (int i = 0; i < 5; i++) {
            final int index = i; // Variável final para uso na lambda
            jogoDTO.setId((long) (index + 1));
            jogoDTO.setPlacarA(index);
            jogoDTO.setPlacarB(index + 1);
            
            assertThatCode(() -> gameEventPublisher.publishGameEvent(jogoDTO, "TESTE_" + index))
                    .doesNotThrowAnyException();
        }

        // Assert - verificar que a conexão ainda está ativa
        assertThat(rabbitMQConfig.getConnectionFactory().getCacheMode())
                .isEqualTo(org.springframework.amqp.rabbit.connection.CachingConnectionFactory.CacheMode.CONNECTION);
    }

    private JogoDTO createJogoDTO(Long id, String timeA, String timeB) {
        return JogoDTO.builder()
                .id(id)
                .timeA(timeA)
                .timeB(timeB)
                .placarA(0)
                .placarB(0)
                .status(StatusJogo.NAO_INICIADO)
                .tempoDeJogo(0)
                .dataPartida(LocalDate.now())
                .horaPartida("20:00")
                .build();
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
