package com.retail_project.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );


        RedisCacheConfiguration productById = baseConfig.entryTtl(Duration.ofMinutes(15));
        RedisCacheConfiguration productsList = baseConfig.entryTtl(Duration.ofMinutes(2));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig)
                .withCacheConfiguration("productById", productById)
                .withCacheConfiguration("productsList", productsList)
                .build();
    }
}