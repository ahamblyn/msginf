package nz.co.pukekocorp.msginf.infrastructure.properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.PropertiesFileException;
import nz.co.pukekocorp.msginf.models.configuration.*;
import nz.co.pukekocorp.msginf.models.configuration.System;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import nz.co.pukekocorp.msginf.models.message.MessageType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class parses the JSON properties file.
 * @author alisdairh
 */
@Slf4j
public class MessageInfrastructurePropertiesFileParser {

    /**
     * The base configuration element
     */
    private Configuration configuration;

    /**
     * Main constructor. Allow access to the messaging system configuration.
     * @throws PropertiesFileException Properties file exception
     */
    public MessageInfrastructurePropertiesFileParser() throws PropertiesFileException {
        parseFile();
    }

    private void parseFile() throws PropertiesFileException {
        // get the properties file name from the system properties: -Dmsginf.propertiesfile
        String fileName = java.lang.System.getProperty("msginf.propertiesfile");
        try {
            if (fileName == null || fileName.isEmpty()) {
                // load the default
                InputStream is = MessageInfrastructurePropertiesFileParser.class.getResourceAsStream("/msginf-config.json");
                ObjectMapper objectMapper = new ObjectMapper();
                configuration = objectMapper.readValue(is, Configuration.class);
                log.info("Default msginf config file loaded");
            } else {
                // load the file directly
                File file = new File(fileName);
                log.info("msginf config file: " + file.getAbsolutePath());
                ObjectMapper objectMapper = new ObjectMapper();
                configuration = objectMapper.readValue(file, Configuration.class);
            }
        } catch (IOException e) {
            throw new PropertiesFileException(e);
        }
    }

    /**
     * toString method.
     * @return the instance as a String.
     */
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Optional<String> s = Optional.ofNullable(configuration).flatMap(config -> {
            try {
                return Optional.ofNullable(objectMapper.writeValueAsString(config));
            } catch (JsonProcessingException e) {
                return Optional.empty();
            }
        });
        return s.orElse("");
    }

    /**
     * Return the configuration.
     * @return the configuration.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Returns a list of the names of the available messaging systems in the properties file.
     * @param messagingModel the messaging model
     * @return a list of the names of the available messaging systems in the properties file.
     */
    public List<String> getAvailableMessagingSystems(MessagingModel messagingModel) {
        List<String> availableMessagingSystems = new ArrayList<>();
        Optional.ofNullable(configuration).ifPresent(config -> availableMessagingSystems.addAll(config.systems().system().stream()
                .filter(system -> system.messagingModel() == messagingModel)
                .map(System::name).toList()));
        return availableMessagingSystems;
    }

    /**
     * Get system configuration for messaging system.
     * @param messagingSystemName messaging system
     * @return system configuration
     */
    public Optional<System> getSystem(String messagingSystemName) {
        return findSystem(messagingSystemName);
    }

    private Optional<System> findSystem(String messagingSystemName) {
        return Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.systems())
                        .flatMap(systems -> systems.system().stream()
                                .filter(sys -> sys.name().equals(messagingSystemName)).findFirst()));
    }

    private Optional<Connectors> findConnectors(String messagingSystemName) {
        Optional<System> system = findSystem(messagingSystemName);
        return system.flatMap(sys -> Optional.ofNullable(sys.connectors()));
    }

    private Optional<Submit> findSubmit(String messagingSystemName, String connectorName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        return connectors.flatMap(conns -> findSubmit(conns, connectorName));
    }

    private Optional<Submit> findSubmit(Connectors connectors, String connectorName) {
        return Optional.ofNullable(connectors)
                .flatMap(conns -> conns.submit().stream().filter(sub ->
                        sub.connectorName().equals(connectorName)).findFirst());
    }

    private Optional<SubmitConnection> findSubmitConnection(String messagingSystemName, String connectorName) {
        Optional<Submit> submit = findSubmit(messagingSystemName, connectorName);
        return submit.flatMap(sub -> Optional.ofNullable(sub.submitConnection()));
    }

    private Optional<RequestReply> findRequestReply(String messagingSystemName, String connectorName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        return connectors.flatMap(conns -> findRequestReply(conns, connectorName));
    }

    private Optional<RequestReply> findRequestReply(Connectors connectors, String connectorName) {
        return Optional.ofNullable(connectors)
                .flatMap(conns -> conns.requestReply().stream().filter(rr ->
                        rr.connectorName().equals(connectorName)).findFirst());
    }

    private Optional<RequestReplyConnection> findRequestReplyConnection(String messagingSystemName, String connectorName) {
        Optional<RequestReply> requestReply = findRequestReply(messagingSystemName, connectorName);
        return requestReply.flatMap(rr -> Optional.ofNullable(rr.requestReplyConnection()));
    }

    private Optional<PublishSubscribe> findPublishSubscribe(String messagingSystemName, String connectorName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        return connectors.flatMap(conns -> findPublishSubscribe(conns, connectorName));
    }

    private Optional<PublishSubscribe> findPublishSubscribe(Connectors connectors, String connectorName) {
        return Optional.ofNullable(connectors)
                .flatMap(conns -> conns.publishSubscribe().stream().filter(pubsub ->
                        pubsub.connectorName().equals(connectorName)).findFirst());
    }

    private Optional<PublishSubscribeConnection> findPublishSubscribeConnection(String messagingSystemName, String connectorName) {
        Optional<PublishSubscribe> publishSubscribe = findPublishSubscribe(messagingSystemName, connectorName);
        return publishSubscribe.flatMap(pubsub -> Optional.ofNullable(pubsub.publishSubscribeConnection()));
    }

    /**
     * Returns the initial context factory name for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the initial context factory name for the messaging system.
     */
    public String getSystemInitialContextFactory(String messagingSystemName) {
        Optional<String> contextFactory = findSystem(messagingSystemName).flatMap(sys -> Optional.ofNullable(sys.jndiProperties().initialContextFactory()));
        return contextFactory.orElse("");
    }

    /**
     * Returns the messaging model for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the messaging model for the messaging system.
     */
    public MessagingModel getMessagingModel(String messagingSystemName) {
        Optional<MessagingModel> messagingModel = findSystem(messagingSystemName).flatMap(sys -> Optional.ofNullable(sys.messagingModel()));
        return messagingModel.orElse(MessagingModel.POINT_TO_POINT);
    }

    /**
     * Returns the JMS implementation for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the JMS implementation for the messaging system.
     */
    public JmsImplementation getJmsImplementation(String messagingSystemName) {
        Optional<JmsImplementation> jmsImplementation = findSystem(messagingSystemName).flatMap(sys -> Optional.ofNullable(sys.jmsImplementation()));
        return jmsImplementation.orElse(JmsImplementation.JAKARTA_JMS);
    }

    /**
     * Returns the naming factory url packages for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the naming factory url packages for the messaging system.
     */
    public String getSystemNamingFactoryUrlPkgs(String messagingSystemName) {
        Optional<String> namingFactoryUrlPkgs = findSystem(messagingSystemName).flatMap(sys -> Optional.ofNullable(sys.jndiProperties().namingFactoryUrlPkgs()));
        return namingFactoryUrlPkgs.orElse("");
    }

    /**
     * Returns a list of the configured queues for the messaging system.
     * @param messagingSystemName the messaging system
     * @return a list of the configured queues for the messaging system.
     */
    public List<PropertiesDestination> getQueues(String messagingSystemName) {
        List<PropertiesDestination> queuesList = new ArrayList<>();
        findSystem(messagingSystemName).ifPresent(system -> {
            if (system.queues() != null) {
                List<PropertiesDestination> props = system.queues().stream()
                        .map(queue -> new PropertiesDestination(queue.jndiName(), queue.physicalName())).toList();
                queuesList.addAll(props);
            }
        });
        return queuesList;
    }

    /**
     * Returns a list of the configured topics for the messaging system.
     * @param messagingSystemName the messaging system
     * @return a list of the configured topics for the messaging system.
     */
    public List<PropertiesDestination> getTopics(String messagingSystemName) {
        List<PropertiesDestination> queuesList = new ArrayList<>();
        findSystem(messagingSystemName).ifPresent(system -> {
            if (system.topics() != null) {
                List<PropertiesDestination> props = system.topics().stream()
                        .map(queue -> new PropertiesDestination(queue.jndiName(), queue.physicalName())).toList();
                queuesList.addAll(props);
            }
        });
        return queuesList;
    }

    /**
     * Returns a list of the vendor specific JNDI properties for the messaging system.
     * @param messagingSystemName the messaging system
     * @return a list of the vendor specific JNDI properties for the messaging system.
     */
    public List<VendorJNDIProperty> getVendorJNDIProperties(String messagingSystemName) {
        List<VendorJNDIProperty> propertiesList = new ArrayList<>();
        findSystem(messagingSystemName).ifPresent(system -> {
            Optional.ofNullable(system.jndiProperties().vendorJNDIProperties()).ifPresent(vendorJNDIProperties -> {
                List<VendorJNDIProperty> props = system.jndiProperties().vendorJNDIProperties().stream()
                        .map(property -> new VendorJNDIProperty(property.name(), property.value())).toList();
                propertiesList.addAll(props);
            });
        });
        return propertiesList;
    }

    /**
     * Returns whether connection pooling is configured for the messaging system.
     * @param messagingSystemName the messaging system
     * @return whether connection pooling is configured for the messaging system.
     */
    public boolean getUseConnectionPooling(String messagingSystemName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        Optional<Boolean> b = connectors.flatMap(conns -> Optional.ofNullable(conns.useConnectionPooling()));
        return b.orElse(false);
    }

    /**
     * Returns the maximum number of connections for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the maximum number of connections for the messaging system.
     */
    public int getMaxConnections(String messagingSystemName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        Optional<Integer> res = connectors.flatMap(conns -> Optional.ofNullable(conns.maxConnections()));
        return res.orElse(0);
    }

    /**
     * Returns the minimum number of connections for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the minimum number of connections for the messaging system.
     */
    public int getMinConnections(String messagingSystemName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        Optional<Integer> res = connectors.flatMap(conns -> Optional.ofNullable(conns.minConnections()));
        return res.orElse(0);
    }

    /**
     * Returns whether the use of a durable subscriber is configured for the messaging system.
     * @param messagingSystemName the messaging system
     * @return whether the use of a durable subscriber is configured for the messaging system.
     */
    public boolean getUseDurableSubscriber(String messagingSystemName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        Optional<Boolean> b = connectors.flatMap(conns -> Optional.ofNullable(conns.useDurableSubscriber()));
        return b.orElse(false);
    }

    /**
     * Returns a list of the names of the submit connectors for the messaging system.
     * @param messagingSystemName the messaging system
     * @return a list of the names of the submit connectors for the messaging system.
     */
    public List<String> getSubmitConnectorNames(String messagingSystemName) {
        List<String> names = new ArrayList<>();
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        connectors.ifPresent(conns -> names.addAll(conns.submit().stream().map(Submit::connectorName).toList()));
        return names;
    }

    /**
     * Returns whether the submit connector exists for the messaging system.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return whether the submit connector exists for the messaging system.
     */
    public boolean doesSubmitExist(String messagingSystemName, String connectorName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        return connectors.stream().anyMatch(conns -> conns.submit().stream().anyMatch(sub ->
                        sub.connectorName().equals(connectorName)));
    }

    /**
     * Returns a list of the names of the request-reply connectors for the messaging system.
     * @param messagingSystemName the messaging system
     * @return a list of the names of the request-reply connectors for the messaging system.
     */
    public List<String> getRequestReplyConnectorNames(String messagingSystemName) {
        List<String> names = new ArrayList<>();
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        connectors.ifPresent(conns -> names.addAll(conns.requestReply().stream().map(RequestReply::connectorName).toList()));
        return names;
    }

    /**
     * Returns whether the request-reply connector exists for the messaging system.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return whether the request-reply connector exists for the messaging system.
     */
    public boolean doesRequestReplyExist(String messagingSystemName, String connectorName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        return connectors.stream().anyMatch(conns -> conns.requestReply().stream().anyMatch(rr ->
                rr.connectorName().equals(connectorName)));
    }

    /**
     * Returns a list of the names of the PublishSubscribe connectors for the messaging system.
     * @param messagingSystemName the messaging system
     * @return a list of the names of the PublishSubscribe connectors for the messaging system.
     */
    public List<String> getPublishSubscribeConnectorNames(String messagingSystemName) {
        List<String> names = new ArrayList<>();
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        connectors.ifPresent(conns -> names.addAll(conns.publishSubscribe().stream().map(PublishSubscribe::connectorName).toList()));
        return names;
    }

    /**
     * Returns whether the PublishSubscribe connector exists for the messaging system.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return whether the PublishSubscribe connector exists for the messaging system.
     */
    public boolean doesPublishSubscribeExist(String messagingSystemName, String connectorName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        return connectors.stream().anyMatch(conns -> conns.publishSubscribe().stream().anyMatch(pubsub ->
                pubsub.connectorName().equals(connectorName)));
    }

    /**
     * Returns whether to compress binary messages for the submit connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return whether to compress binary messages for the submit connector.
     */
    public boolean getSubmitCompressBinaryMessages(String messagingSystemName, String connectorName) {
        Optional<Submit> submit = findSubmit(messagingSystemName, connectorName);
        Optional<Boolean> b = submit.flatMap(sub -> Optional.ofNullable(sub.compressBinaryMessages()));
        return b.orElse(false);
    }

    /**
     * Returns the submit connection submit queue name for the submit connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the submit connection submit queue name for the submit connector.
     */
    public String getSubmitConnectionSubmitQueueName(String messagingSystemName, String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(messagingSystemName, connectorName);
        Optional<String> queueName = connection.flatMap(sub -> Optional.ofNullable(sub.submitQueueName()));
        return queueName.orElse("");
    }

    /**
     * Returns the submit connection submit queue connection factory name for the submit connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the submit connection submit queue connection factory name for the submit connector.
     */
    public String getSubmitConnectionSubmitQueueConnFactoryName(String messagingSystemName, String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(messagingSystemName, connectorName);
        Optional<String> queueNameConnFactory = connection.flatMap(sub -> Optional.ofNullable(sub.submitQueueConnFactoryName()));
        return queueNameConnFactory.orElse("");
    }

    /**
     * Returns the submit connection request type for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the submit connection request type for the request-reply connector.
     */
    public RequestType getSubmitConnectionRequestType(String messagingSystemName, String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(messagingSystemName, connectorName);
        Optional<RequestType> requestType = connection.flatMap(sub -> Optional.ofNullable(sub.requestType()));
        return requestType.orElse(RequestType.TEXT); // default to text
    }

    /**
     * Returns the submit connection message time to live for the submit connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the submit connection message time to live for the submit connector.
     */
    public int getSubmitConnectionMessageTimeToLive(String messagingSystemName, String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(messagingSystemName, connectorName);
        Optional<Integer> messageTimeToLive = connection.flatMap(sub -> Optional.ofNullable(sub.messageTimeToLive()));
        return messageTimeToLive.orElse(0);
    }

    /**
     * Returns the submit connection message properties for the submit connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the submit connection message properties for the submit connector.
     */
    public List<MessageProperty> getSubmitConnectionMessageProperties(String messagingSystemName, String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(messagingSystemName, connectorName);
        Optional<List<MessageProperty>> parserMessageProperties = connection.flatMap(sub -> Optional.ofNullable(sub.messageProperties()));
        return parserMessageProperties.orElse(new ArrayList<>());
    }

    /**
     * Returns whether to compress binary messages for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return whether to compress binary messages for the request-reply connector.
     */
    public boolean getRequestReplyCompressBinaryMessages(String messagingSystemName, String connectorName) {
        Optional<RequestReply> requestReply = findRequestReply(messagingSystemName, connectorName);
        Optional<Boolean> b = requestReply.flatMap(rr -> Optional.ofNullable(rr.compressBinaryMessages()));
        return b.orElse(false);
    }

    /**
     * Returns the request-reply connection request queue name for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the request-reply connection request queue name for the request-reply connector.
     */
    public String getRequestReplyConnectionRequestQueueName(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<String> queueName = connection.flatMap(rr -> Optional.ofNullable(rr.requestQueueName()));
        return queueName.orElse("");
    }

    /**
     * Returns the request-reply connection reply queue name for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the request-reply connection reply queue name for the request-reply connector.
     */
    public String getRequestReplyConnectionReplyQueueName(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<String> queueName = connection.flatMap(rr -> Optional.ofNullable(rr.replyQueueName()));
        return queueName.orElse("");
    }

    /**
     * Returns the request-reply connection request queue connection factory name for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the request-reply connection request queue connection factory name for the request-reply connector.
     */
    public String getRequestReplyConnectionRequestQueueConnFactoryName(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<String> queueNameConnFactory = connection.flatMap(rr -> Optional.ofNullable(rr.requestQueueConnFactoryName()));
        return queueNameConnFactory.orElse("");
    }

    /**
     * Returns the request-reply connection request type for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the request-reply connection request type for the request-reply connector.
     */
    public RequestType getRequestReplyConnectionRequestType(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<RequestType> requestType = connection.flatMap(rr -> Optional.ofNullable(rr.requestType()));
        return requestType.orElse(RequestType.TEXT); // default to text
    }

    /**
     * Returns the request-reply connection message time to live for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the request-reply connection message time to live for the request-reply connector.
     */
    public int getRequestReplyConnectionMessageTimeToLive(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<Integer> messageTimeToLive = connection.flatMap(rr -> Optional.ofNullable(rr.messageTimeToLive()));
        return messageTimeToLive.orElse(0);
    }

    /**
     * Returns the request-reply connection message properties for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the request-reply connection message properties for the request-reply connector.
     */
    public List<MessageProperty> getRequestReplyConnectionMessageProperties(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<List<MessageProperty>> parserMessageProperties = connection.flatMap(rr -> Optional.ofNullable(rr.messageProperties()));
        return parserMessageProperties.orElse(new ArrayList<>());
    }

    /**
     * Returns the request-reply connection reply wait time for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the request-reply connection reply wait time for the request-reply connector.
     */
    public int getRequestReplyConnectionReplyWaitTime(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<Integer> replyWaitTime = connection.flatMap(rr -> Optional.ofNullable(rr.replyWaitTime()));
        return replyWaitTime.orElse(0);
    }

    /**
     * Returns the request-reply connection use message selector value for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the request-reply connection use message selector value for the request-reply connector.
     */
    public boolean getRequestReplyConnectionUseMessageSelector(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<Boolean> replyWaitTime = connection.flatMap(rr -> Optional.ofNullable(rr.useMessageSelector()));
        return replyWaitTime.orElse(true);
    }

    /**
     * Returns whether to compress binary messages for the PublishSubscribe connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return whether to compress binary messages for the PublishSubscribe connector.
     */
    public boolean getPublishSubscribeCompressBinaryMessages(String messagingSystemName, String connectorName) {
        Optional<PublishSubscribe> publishSubscribe = findPublishSubscribe(messagingSystemName, connectorName);
        Optional<Boolean> b = publishSubscribe.flatMap(pubsub -> Optional.ofNullable(pubsub.compressBinaryMessages()));
        return b.orElse(false);
    }

    /**
     * Returns the PublishSubscribe connection PublishSubscribe topic name for the PublishSubscribe connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the submit connection PublishSubscribe topic name for the PublishSubscribe connector.
     */
    public String getPublishSubscribeConnectionPublishSubscribeTopicName(String messagingSystemName, String connectorName) {
        Optional<PublishSubscribeConnection> connection = findPublishSubscribeConnection(messagingSystemName, connectorName);
        Optional<String> topicName = connection.flatMap(pubsub -> Optional.ofNullable(pubsub.publishSubscribeTopicName()));
        return topicName.orElse("");
    }

    /**
     * Returns the PublishSubscribe connection PublishSubscribe topic connection factory name for the PublishSubscribe connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the PublishSubscribe connection PublishSubscribe topic connection factory name for the PublishSubscribe connector.
     */
    public String getPublishSubscribeConnectionPublishSubscribeTopicConnFactoryName(String messagingSystemName, String connectorName) {
        Optional<PublishSubscribeConnection> connection = findPublishSubscribeConnection(messagingSystemName, connectorName);
        Optional<String> topicNameConnFactory = connection.flatMap(pubsub -> Optional.ofNullable(pubsub.publishSubscribeTopicConnFactoryName()));
        return topicNameConnFactory.orElse("");
    }

    /**
     * Returns the PublishSubscribe connection request type for the PublishSubscribe connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the PublishSubscribe connection request type for the PublishSubscribe connector.
     */
    public RequestType getPublishSubscribeConnectionRequestType(String messagingSystemName, String connectorName) {
        Optional<PublishSubscribeConnection> connection = findPublishSubscribeConnection(messagingSystemName, connectorName);
        Optional<RequestType> requestType = connection.flatMap(pubsub -> Optional.ofNullable(pubsub.requestType()));
        return requestType.orElse(RequestType.TEXT); // default to text
    }

    /**
     * Returns the PublishSubscribe connection message time to live for the PublishSubscribe connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the PublishSubscribe connection message time to live for the PublishSubscribe connector.
     */
    public int getPublishSubscribeConnectionMessageTimeToLive(String messagingSystemName, String connectorName) {
        Optional<PublishSubscribeConnection> connection = findPublishSubscribeConnection(messagingSystemName, connectorName);
        Optional<Integer> messageTimeToLive = connection.flatMap(pubsub -> Optional.ofNullable(pubsub.messageTimeToLive()));
        return messageTimeToLive.orElse(0);
    }

    /**
     * Returns the PublishSubscribe connection message properties for the PublishSubscribe connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the PublishSubscribe connection message properties for the PublishSubscribe connector.
     */
    public List<MessageProperty> getPublishSubscribeConnectionMessageProperties(String messagingSystemName, String connectorName) {
        Optional<PublishSubscribeConnection> connection = findPublishSubscribeConnection(messagingSystemName, connectorName);
        Optional<List<MessageProperty>> parserMessageProperties = connection.flatMap(pubsub -> Optional.ofNullable(pubsub.messageProperties()));
        return parserMessageProperties.orElse(new ArrayList<>());
    }

    /**
     * Returns message type based on message request type.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @param messageRequestType the message request type
     * @return the request type based on message request type.
     */
    public MessageType getMessageType(String messagingSystemName, String connectorName, MessageRequestType messageRequestType) {
        if (messageRequestType == MessageRequestType.SUBMIT) {
            String requestType = getSubmitConnectionRequestType(messagingSystemName, connectorName).name();
            return MessageType.valueOf(requestType);
        } else if (messageRequestType == MessageRequestType.REQUEST_RESPONSE) {
            String requestType = getRequestReplyConnectionRequestType(messagingSystemName, connectorName).name();
            return MessageType.valueOf(requestType);
        } else if (messageRequestType == MessageRequestType.PUBLISH_SUBSCRIBE) {
            String requestType = getPublishSubscribeConnectionRequestType(messagingSystemName, connectorName).name();
            return MessageType.valueOf(requestType);
        } else {
            return MessageType.TEXT;
        }
    }
}
