package com.bank.wallet.config;

import com.bank.wallet.models.documents.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Autowired
    RedisConnectionFactory factory;

    @Bean
    public ReactiveRedisTemplate<String, Wallet> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {

        Jackson2JsonRedisSerializer<Wallet> serializer = new Jackson2JsonRedisSerializer<>(Wallet.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, Wallet> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, Wallet> context = builder.value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

}
