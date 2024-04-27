package nz.co.pukekocorp.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.ConfigurationException;
import nz.co.pukekocorp.msginf.infrastructure.exception.PropertiesFileException;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.configuration.MessagingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class AdapterConfiguration {

    @Bean
    public Map<String, QueueManager> queueManagers(@Value("#{${jndi.url.map}}") Map<String, String> jndiUrlMap) {
        Map<String, QueueManager> queueManagerMap = new ConcurrentHashMap<>();
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            parser.getAvailableMessagingSystems(MessagingModel.POINT_TO_POINT).forEach(messagingSystem -> {
                try {
                    String jndiUrl = Optional.ofNullable(jndiUrlMap.get(messagingSystem))
                            .orElseThrow(() -> new ConfigurationException("The messaging system " + messagingSystem + " has no JNDI url configured. Check the jndi.url.map property in the application.properties file."));
                    queueManagerMap.put(messagingSystem, new QueueManager(parser, messagingSystem, jndiUrl));
                } catch (ConfigurationException e) {
                    log.error("QueueManager unable to be created for " + messagingSystem, e);
                    throw new RuntimeException(e);
                }
            });
        } catch (PropertiesFileException e) {
            log.error("QueueManagers bean unable to be created", e);
            throw new RuntimeException(e);
        }
        return queueManagerMap;
    }

    @Bean
    public Map<String, TopicManager> topicManagers(@Value("#{${jndi.url.map}}") Map<String, String> jndiUrlMap) {
        Map<String, TopicManager> topicManagerMap = new ConcurrentHashMap<>();
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            parser.getAvailableMessagingSystems(MessagingModel.PUBLISH_SUBSCRIBE).forEach(messagingSystem -> {
                try {
                    String jndiUrl = Optional.ofNullable(jndiUrlMap.get(messagingSystem))
                            .orElseThrow(() -> new ConfigurationException("The messaging system " + messagingSystem + " has no JNDI url configured. Check the jndi.url.map property in the application.properties file."));
                    topicManagerMap.put(messagingSystem, new TopicManager(parser, messagingSystem, jndiUrl));
                } catch (ConfigurationException e) {
                    log.error("TopicManager unable to be created for " + messagingSystem, e);
                    throw new RuntimeException(e);
                }
            });
        } catch (PropertiesFileException e) {
            log.error("TopicManagers bean unable to be created", e);
            throw new RuntimeException(e);
        }
        return topicManagerMap;
    }
}
