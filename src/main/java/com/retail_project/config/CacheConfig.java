package com.retail_project.config;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.retail_project.product.ProductResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.List;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        ObjectMapper objectMapper = new ObjectMapper();

        // Jackson's default/polymorphic typing (any @class style) cannot reliably deserialize
        // scalar values (e.g. BigDecimal) into Java record canonical constructors — it always
        // falls back to array-wrapping non-natural scalars, which record creators can't consume.
        // Fix: skip polymorphic typing entirely and use a typed serializer per cache, since each
        // cache's exact value type is already known.
        Jackson2JsonRedisSerializer<ProductResponse> productSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, ProductResponse.class);

        Jackson2JsonRedisSerializer<List<ProductResponse>> productListSerializer =
                new Jackson2JsonRedisSerializer<>(
                        objectMapper,
                        TypeFactory.defaultInstance().constructCollectionType(List.class, ProductResponse.class)
                );

        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues();

        RedisCacheConfiguration productById = baseConfig
                .entryTtl(Duration.ofMinutes(15))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(productSerializer));

        RedisCacheConfiguration productsList = baseConfig
                .entryTtl(Duration.ofMinutes(2))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(productListSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig)
                .withCacheConfiguration("productById", productById)
                .withCacheConfiguration("productsList", productsList)
                .build();
    }
}
