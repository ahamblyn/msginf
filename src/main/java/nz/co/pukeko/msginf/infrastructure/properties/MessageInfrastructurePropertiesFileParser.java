package nz.co.pukeko.msginf.infrastructure.properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.exception.PropertiesFileException;
import nz.co.pukeko.msginf.models.configuration.*;
import nz.co.pukeko.msginf.models.configuration.System;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
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
        File file;
        // get the properties file name from the system properties: -Dmsginf.propertiesfile
        String fileName = java.lang.System.getProperty("msginf.propertiesfile");
        try {
            if (fileName == null || fileName.equals("")) {
                // set the default
                Resource resource = new ClassPathResource("msginf-config.json");
                file = resource.getFile();
                log.info("msginf config file: " + file.getAbsolutePath());
            } else {
                // load the file directly
                file = new File(fileName);
                log.info("msginf config file: " + file.getAbsolutePath());
            }
            ObjectMapper objectMapper = new ObjectMapper();
            configuration = objectMapper.readValue(file, Configuration.class);
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
        Optional.ofNullable(configuration).ifPresent(config -> availableMessagingSystems.addAll(config.getSystems().getSystem().stream()
                .map(System::getName).toList()));
        return availableMessagingSystems;
    }

    public Optional<System> getSystem(String messagingSystemName) {
        return findSystem(messagingSystemName);
    }

    private Optional<System> findSystem(String messagingSystemName) {
        // TODO fix return
        final var ref = new Object() {
            Optional<System> returnSystem;
        };
        Optional.ofNullable(configuration).flatMap(config -> Optional.ofNullable(config.getSystems()))
                .flatMap(systems -> Optional.ofNullable(systems.getSystem()))
                .ifPresent(systemsList -> ref.returnSystem = systemsList.stream().filter(system ->
                        system.getName().equals(messagingSystemName)).findFirst());
        return ref.returnSystem;
    }

    private Optional<Connectors> findConnectors(String messagingSystemName) {
        Optional<System> system = findSystem(messagingSystemName);
        return system.flatMap(sys -> Optional.ofNullable(sys.getConnectors()));
    }

    private Optional<Submit> findSubmit(String messagingSystemName, String connectorName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        return connectors.flatMap(conns -> findSubmit(conns, connectorName));
    }

    private Optional<Submit> findSubmit(Connectors connectors, String connectorName) {
        return Optional.ofNullable(connectors)
                .flatMap(conns -> conns.getSubmit().stream().filter(sub ->
                        sub.getConnectorName().equals(connectorName)).findFirst());
    }

    private Optional<SubmitConnection> findSubmitConnection(String messagingSystemName, String connectorName) {
        Optional<Submit> submit = findSubmit(messagingSystemName, connectorName);
        return submit.flatMap(sub -> Optional.ofNullable(sub.getSubmitConnection()));
    }

    private Optional<RequestReply> findRequestReply(String messagingSystemName, String connectorName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        return connectors.flatMap(conns -> findRequestReply(conns, connectorName));
    }

    private Optional<RequestReply> findRequestReply(Connectors connectors, String connectorName) {
        return Optional.ofNullable(connectors)
                .flatMap(conns -> conns.getRequestReply().stream().filter(rr ->
                        rr.getConnectorName().equals(connectorName)).findFirst());
    }

    private Optional<RequestReplyConnection> findRequestReplyConnection(String messagingSystemName, String connectorName) {
        Optional<RequestReply> requestReply = findRequestReply(messagingSystemName, connectorName);
        return requestReply.flatMap(rr -> Optional.ofNullable(rr.getRequestReplyConnection()));
    }

    /**
     * Returns the initial context factory name for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the initial context factory name for the messaging system.
     */
    public String getSystemInitialContextFactory(String messagingSystemName) {
        Optional<String> contextFactory = findSystem(messagingSystemName).flatMap(sys -> Optional.ofNullable(sys.getInitialContextFactory()));
        return contextFactory.orElse("");
    }

    /**
     * Returns the url for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the url for the messaging system.
     */
    public String getSystemUrl(String messagingSystemName) {
        Optional<String> url = findSystem(messagingSystemName).flatMap(sys -> Optional.ofNullable(sys.getUrl()));
        return url.orElse("");
    }

    /**
     * Returns the host for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the host for the messaging system.
     */
    public String getSystemHost(String messagingSystemName) {
        Optional<String> host = findSystem(messagingSystemName).flatMap(sys -> Optional.ofNullable(sys.getHost()));
        return host.orElse("");
    }

    /**
     * Returns the port for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the port for the messaging system.
     */
    public int getSystemPort(String messagingSystemName) {
        Optional<Integer> port = findSystem(messagingSystemName).flatMap(sys -> Optional.ofNullable(sys.getPort()));
        return port.orElse(0);
    }

    /**
     * Returns the naming factory url packages for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the naming factory url packages for the messaging system.
     */
    public String getSystemNamingFactoryUrlPkgs(String messagingSystemName) {
        Optional<String> namingFactoryUrlPkgs = findSystem(messagingSystemName).flatMap(sys -> Optional.ofNullable(sys.getNamingFactoryUrlPkgs()));
        return namingFactoryUrlPkgs.orElse("");
    }

    /**
     * Returns a list of the jar file names for the messaging system.
     * @param messagingSystemName the messaging system
     * @return a list of the jar file names for the messaging system.
     */
    public List<String> getJarFileNames(String messagingSystemName) {
        List<String> jarFileNamesList = new ArrayList<>();
        findSystem(messagingSystemName).ifPresent(system ->
                jarFileNamesList.addAll(system.getJarFiles().getJarFile().stream().map(JarFile::getJarFileName).toList()));
        return jarFileNamesList;
    }

    /**
     * Returns a list of the configured queues for the messaging system.
     * @param messagingSystemName the messaging system
     * @return a list of the configured queues for the messaging system.
     */
    public List<PropertiesQueue> getQueues(String messagingSystemName) {
        List<PropertiesQueue> queuesList = new ArrayList<>();
        findSystem(messagingSystemName).ifPresent(system -> {
            List<PropertiesQueue> props = system.getQueues().getQueue().stream()
                    .map(queue -> new PropertiesQueue(queue.getJndiName(), queue.getPhysicalName())).toList();
            queuesList.addAll(props);
        });
        return queuesList;
    }

    /**
     * Returns whether connection pooling is configured for the messaging system.
     * @param messagingSystemName the messaging system
     * @return whether connection pooling is configured for the messaging system.
     */
    public boolean getUseConnectionPooling(String messagingSystemName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        Optional<Boolean> b = connectors.flatMap(conns -> Optional.ofNullable(conns.getUseConnectionPooling()));
        return b.orElse(false);
    }

    /**
     * Returns the maximum number of connections for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the maximum number of connections for the messaging system.
     */
    public int getMaxConnections(String messagingSystemName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        Optional<Integer> res = connectors.flatMap(conns -> Optional.ofNullable(conns.getMaxConnections()));
        return res.orElse(0);
    }

    /**
     * Returns the minimum number of connections for the messaging system.
     * @param messagingSystemName the messaging system
     * @return the minimum number of connections for the messaging system.
     */
    public int getMinConnections(String messagingSystemName) {
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        Optional<Integer> res = connectors.flatMap(conns -> Optional.ofNullable(conns.getMinConnections()));
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
        connectors.ifPresent(conns -> names.addAll(conns.getSubmit().stream().map(Submit::getConnectorName).toList()));
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
        return connectors.stream().anyMatch(conns -> conns.getSubmit().stream().anyMatch(sub ->
                        sub.getConnectorName().equals(connectorName)));
    }

    /**
     * Returns a list of the names of the request-reply connectors for the messaging system.
     * @param messagingSystemName the messaging system
     * @return a list of the names of the request-reply connectors for the messaging system.
     */
    public List<String> getRequestReplyConnectorNames(String messagingSystemName) {
        List<String> names = new ArrayList<>();
        Optional<Connectors> connectors = findConnectors(messagingSystemName);
        connectors.ifPresent(conns -> names.addAll(conns.getRequestReply().stream().map(RequestReply::getConnectorName).toList()));
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
        return connectors.stream().anyMatch(conns -> conns.getRequestReply().stream().anyMatch(rr ->
                rr.getConnectorName().equals(connectorName)));
    }

    /**
     * Returns whether to compress binary messages for the submit connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return whether to compress binary messages for the submit connector.
     */
    public boolean getSubmitCompressBinaryMessages(String messagingSystemName, String connectorName) {
        Optional<Submit> submit = findSubmit(messagingSystemName, connectorName);
        Optional<Boolean> b = submit.flatMap(sub -> Optional.ofNullable(sub.getCompressBinaryMessages()));
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
        Optional<String> queueName = connection.flatMap(sub -> Optional.ofNullable(sub.getSubmitQueueName()));
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
        Optional<String> queueNameConnFactory = connection.flatMap(sub -> Optional.ofNullable(sub.getSubmitQueueConnFactoryName()));
        return queueNameConnFactory.orElse("");
    }

    /**
     * Returns the submit connection message class name for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the submit connection message class name for the request-reply connector.
     */
    public String getSubmitConnectionMessageClassName(String messagingSystemName, String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(messagingSystemName, connectorName);
        Optional<String> messageClassName = connection.flatMap(sub -> Optional.ofNullable(sub.getMessageClassName()));
        return messageClassName.orElse("");
    }

    /**
     * Returns the submit connection message time to live for the submit connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the submit connection message time to live for the submit connector.
     */
    public int getSubmitConnectionMessageTimeToLive(String messagingSystemName, String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(messagingSystemName, connectorName);
        Optional<Integer> messageTimeToLive = connection.flatMap(sub -> Optional.ofNullable(sub.getMessageTimeToLive()));
        return messageTimeToLive.orElse(0);
    }

    /**
     * Returns the submit connection reply wait time for the submit connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the submit connection reply wait time for the submit connector.
     */
    public int getSubmitConnectionReplyWaitTime(String messagingSystemName, String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(messagingSystemName, connectorName);
        Optional<Integer> replyWaitTime = connection.flatMap(sub -> Optional.ofNullable(sub.getReplyWaitTime()));
        return replyWaitTime.orElse(0);
    }

    /**
     * Returns whether to compress binary messages for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return whether to compress binary messages for the request-reply connector.
     */
    public boolean getRequestReplyCompressBinaryMessages(String messagingSystemName, String connectorName) {
        Optional<RequestReply> requestReply = findRequestReply(messagingSystemName, connectorName);
        Optional<Boolean> b = requestReply.flatMap(rr -> Optional.ofNullable(rr.getCompressBinaryMessages()));
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
        Optional<String> queueName = connection.flatMap(rr -> Optional.ofNullable(rr.getRequestQueueName()));
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
        Optional<String> queueName = connection.flatMap(rr -> Optional.ofNullable(rr.getReplyQueueName()));
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
        Optional<String> queueNameConnFactory = connection.flatMap(rr -> Optional.ofNullable(rr.getRequestQueueConnFactoryName()));
        return queueNameConnFactory.orElse("");
    }

    /**
     * Returns the request-reply connection message class name for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the request-reply connection message class name for the request-reply connector.
     */
    public String getRequestReplyConnectionMessageClassName(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<String> messageClassName = connection.flatMap(rr -> Optional.ofNullable(rr.getMessageClassName()));
        return messageClassName.orElse("");
    }

    /**
     * Returns the request-reply connection requester class name for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the request-reply connection requester class name for the request-reply connector.
     */
    public String getRequestReplyConnectionRequesterClassName(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<String> requesterClassName = connection.flatMap(rr -> Optional.ofNullable(rr.getRequesterClassName()));
        return requesterClassName.orElse("");
    }

    /**
     * Returns the request-reply connection message time to live for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the request-reply connection message time to live for the request-reply connector.
     */
    public int getRequestReplyConnectionMessageTimeToLive(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<Integer> messageTimeToLive = connection.flatMap(rr -> Optional.ofNullable(rr.getMessageTimeToLive()));
        return messageTimeToLive.orElse(0);
    }

    /**
     * Returns the request-reply connection reply wait time for the request-reply connector.
     * @param messagingSystemName the messaging system
     * @param connectorName the connector name
     * @return the request-reply connection reply wait time for the request-reply connector.
     */
    public int getRequestReplyConnectionReplyWaitTime(String messagingSystemName, String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(messagingSystemName, connectorName);
        Optional<Integer> replyWaitTime = connection.flatMap(rr -> Optional.ofNullable(rr.getReplyWaitTime()));
        return replyWaitTime.orElse(0);
    }

}
