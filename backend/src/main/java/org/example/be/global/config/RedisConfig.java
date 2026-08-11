package org.example.be.global.config;

import org.example.be.domain.notification.broadcast.NotificationRedisSubscriber;
import org.example.be.domain.notification.broadcast.RedisPubSubNotificationSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisConfig {

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper;
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
		NotificationRedisSubscriber notificationRedisSubscriber) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		container.addMessageListener(notificationRedisSubscriber,
			new ChannelTopic(RedisPubSubNotificationSender.CHANNEL));
		return container;

	}
}
