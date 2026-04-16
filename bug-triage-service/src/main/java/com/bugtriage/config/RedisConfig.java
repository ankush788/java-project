package com.bugtriage.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * Redis template for general purpose caching with String key and Object value
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

       
         // Configure ObjectMapper
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule()); // ✅ Fix for LocalDateTime
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Dates will be stored as readable string
        // in java time can be store as readable formatt or number

        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // Allows Jackson to access all fields (even private)

        om.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        //Adds class/type info in JSON
        //Helps in converting JSON back to correct object     

        // ✅ value serializer 
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
            new Jackson2JsonRedisSerializer<>(om, Object.class);

        // Key serializer
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // Set serializers for string keys and JSON values
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}



  /*
        ObjectMapper
        → converts data structure → another structure 
        (while converting you can apply rules (this field) in result structure)
        (object ⇄ JSON ⇄ object, object ⇄ object)
        
        Serializer
        → converts data → storable/transferable format (byte[])

        ->some time serializer internally use Mapper. 

*/