package com.draft.restapi.common.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.max-timeout-millis:500}")
    private int maxTimeoutMillis;

    @Bean
    public RedisClient redisClient() {
        RedisURI redisURI = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .withTimeout(Duration.ofMillis(maxTimeoutMillis)) // fast connection timeout
                .build();
                
        RedisClient client = RedisClient.create(redisURI);
        
        ClientOptions clientOptions = ClientOptions.builder()
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS) // fail fast instead of queueing
                .autoReconnect(true)
                .socketOptions(SocketOptions.builder().connectTimeout(Duration.ofMillis(maxTimeoutMillis)).build())
                .timeoutOptions(TimeoutOptions.builder().fixedTimeout(Duration.ofMillis(maxTimeoutMillis)).build())
                .build();
                
        client.setOptions(clientOptions);
        return client;
    }

    @Bean
    @Lazy
    public ProxyManager<byte[]> proxyManager(RedisClient redisClient) {
        return LettuceBasedProxyManager.builderFor(redisClient)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofHours(1)))
                .build();
    }

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceCustomizer() {
        return clientConfigurationBuilder -> {
            ClientOptions clientOptions = ClientOptions.builder()
                    .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                    .socketOptions(SocketOptions.builder().connectTimeout(Duration.ofMillis(maxTimeoutMillis)).build())
                    .timeoutOptions(TimeoutOptions.builder().fixedTimeout(Duration.ofMillis(maxTimeoutMillis)).build())
                    .build();
            clientConfigurationBuilder.clientOptions(clientOptions);
            clientConfigurationBuilder.commandTimeout(Duration.ofMillis(maxTimeoutMillis));
        };
    }
}
