package com.simada_backend.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class GroqConfig {

    @Value("${groq.apiBaseUrl}")
    private String apiBaseUrl;

    @Value("${groq.timeoutMillis:8000}")
    private int timeoutMillis;

    @Bean
    public WebClient groqWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMillis)
                .responseTimeout(Duration.ofMillis(timeoutMillis))
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(timeoutMillis, TimeUnit.MILLISECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(timeoutMillis, TimeUnit.MILLISECONDS));
                });

        return WebClient.builder()
                .baseUrl(apiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(512 * 1024))
                        .build())
                .build();
    }
}
