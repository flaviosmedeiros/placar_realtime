package br.com.solides.placar.consumer.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @Valid
    private final Rabbit rabbit = new Rabbit();

    @Valid
    private final Sse sse = new Sse();

    @Getter
    @Setter
    public static class Rabbit {
        @NotBlank
        private String exchange;

        @NotBlank
        private String queue;

        @NotBlank
        private String routing;

        @Valid
        private final Dlq dlq = new Dlq();

        @Getter
        @Setter
        public static class Dlq {
            @NotBlank
            private String exchange;

            @NotBlank
            private String routing;

            @NotBlank
            private String queue;
        }
    }

    @Getter
    @Setter
    public static class Sse {
        @Valid
        private final Endpoints endpoints = new Endpoints();

        @NotNull
        private List<@NotBlank String> allowedOrigins = List.of();

        @Valid
        private final Integer heartbeat = 10000;

        @Getter
        @Setter
        public static class Endpoints {
            @NotBlank
            private String novos;

            @NotBlank
            private String inicio;

            @NotBlank
            private String placar;

            @NotBlank
            private String encerrado;
        }
    }
}
