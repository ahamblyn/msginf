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

/**
 * Configures the beans required by the adapter.
 */
@Configuration
@Slf4j
public class AdapterConfiguration {

    /**
     * The properties file parser.
     */
    private MessageInfrastructurePropertiesFileParser propertiesFileParser;

    /**
     * The JNDI url map used to map messaging systems to their JNDI urls.
     * This is defined in the Spring Boot application.properties file.
     */
    @Value("#{${msginf.jndi.url.map}}")
    private Map<String, String> jndiUrlMap;

    /**
     * The map of messaging systems to their respective queue manager.
     * @return the map of messaging systems and their queue manager.
     */
    @Bean
    public Map<String, QueueManager> queueManagers() {
        return createQueueManagers();
    }

    /**
     * Create the messaging systems to queue manager map.
     * @return the messaging systems to queue manager map.
     */
    public Map<String, QueueManager> createQueueManagers() {
        Map<String, QueueManager> queueManagerMap = new ConcurrentHashMap<>();
        try {
            propertiesFileParser = propertiesFileParser();
            propertiesFileParser.getAvailableMessagingSystems(MessagingModel.POINT_TO_POINT).forEach(messagingSystem -> {
                try {
                    String jndiUrl = Optional.ofNullable(jndiUrlMap.get(messagingSystem))
                            .orElseThrow(() -> new ConfigurationException("The messaging system " + messagingSystem + " has no JNDI url configured. Check the msginf.jndi.url.map property in the application.properties file."));
                    queueManagerMap.put(messagingSystem, new QueueManager(propertiesFileParser, messagingSystem, jndiUrl));
                } catch (ConfigurationException e) {
                    log.error("QueueManager unable to be created for " + messagingSystem, e);
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            throw e;
        }
        return queueManagerMap;
    }

    /**
     * The map of messaging systems to their respective topic manager.
     * @return the map of messaging systems and their topic manager.
     */
    @Bean
    public Map<String, TopicManager> topicManagers() {
        return createTopicManagers();
    }

    /**
     * Create the messaging systems to topic manager map.
     * @return the messaging systems to topic manager map.
     */
    public Map<String, TopicManager> createTopicManagers() {
        Map<String, TopicManager> topicManagerMap = new ConcurrentHashMap<>();
        try {
            propertiesFileParser = propertiesFileParser();
            propertiesFileParser.getAvailableMessagingSystems(MessagingModel.PUBLISH_SUBSCRIBE).forEach(messagingSystem -> {
                try {
                    String jndiUrl = Optional.ofNullable(jndiUrlMap.get(messagingSystem))
                            .orElseThrow(() -> new ConfigurationException("The messaging system " + messagingSystem + " has no JNDI url configured. Check the msginf.jndi.url.map property in the application.properties file."));
                    topicManagerMap.put(messagingSystem, new TopicManager(propertiesFileParser, messagingSystem, jndiUrl));
                } catch (ConfigurationException e) {
                    log.error("TopicManager unable to be created for " + messagingSystem, e);
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            throw e;
        }
        return topicManagerMap;
    }

    /**
     * The properties file parser bean.
     * @return the properties file parser bean.
     */
    @Bean
    public MessageInfrastructurePropertiesFileParser propertiesFileParser() {
        try {
            if (propertiesFileParser == null) {
                propertiesFileParser = new MessageInfrastructurePropertiesFileParser();
            }
        } catch (PropertiesFileException e) {
            log.error("Unable to create the MessageInfrastructurePropertiesFileParser", e);
            throw new RuntimeException(e);
        }
        return propertiesFileParser;
    }
}
