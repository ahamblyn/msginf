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
                throw new RuntimeException(e);
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
     * @return a list of the names of the available messaging systems in the properties file.
     */
    public List<String> getAvailableMessagingSystems() {
        List<String> availableMessagingSystems = new ArrayList<>();
        Optional.ofNullable(configuration).ifPresent(config -> availableMessagingSystems.addAll(config.systems().system().stream()
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
    public List<PropertiesQueue> getQueues(String messagingSystemName) {
        List<PropertiesQueue> queuesList = new ArrayList<>();
        findSystem(messagingSystemName).ifPresent(system -> {
            List<PropertiesQueue> props = system.queues().stream()
                    .map(queue -> new PropertiesQueue(queue.jndiName(), queue.physicalName())).toList();
            queuesList.addAll(props);
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
    public String getSubmitConnectionRequestType(String messagingSystemName, String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(messagingSystemName, connectorName);
        Optional<RequestType> requestType = connection.flatMap(sub -> Optional.ofNullable(sub.requestType()));
        return requestType.orElse(RequestType.TEXT).name(); // default to text
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
    public String getRequestReplyConnectionRequestType(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<RequestType> requestType = connection.flatMap(rr -> Optional.ofNullable(rr.requestType()));
        return requestType.orElse(RequestType.TEXT).name(); // default to text
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
     * Returns message type based on message request type.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @param messageRequestType the message request type
     * @return the request type based on message request type.
     */
    public MessageType getMessageType(String messagingSystemName, String connectorName, MessageRequestType messageRequestType) {
        if (messageRequestType == MessageRequestType.SUBMIT) {
            String requestType = getSubmitConnectionRequestType(messagingSystemName, connectorName).toUpperCase();
            return MessageType.valueOf(requestType);
        } else if (messageRequestType == MessageRequestType.REQUEST_RESPONSE) {
            String requestType = getRequestReplyConnectionRequestType(messagingSystemName, connectorName).toUpperCase();
            return MessageType.valueOf(requestType);
        } else {
            return MessageType.TEXT;
        }
    }
}
