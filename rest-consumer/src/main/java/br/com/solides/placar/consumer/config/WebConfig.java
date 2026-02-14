package br.com.solides.placar.consumer.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AppProperties properties;

    public WebConfig(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> allowedOrigins = properties.getSse().getAllowedOrigins();
        String[] origins = allowedOrigins.toArray(new String[0]);

        registry.addMapping("/consumer/api/sse/**")
            .allowedOrigins(origins)
            .allowedMethods("GET")
            .allowCredentials(false);

        registry.addMapping("/consumer/api/games/**")
            .allowedOrigins(origins)
            .allowedMethods("GET", "POST")
            .allowCredentials(false);
    }
}