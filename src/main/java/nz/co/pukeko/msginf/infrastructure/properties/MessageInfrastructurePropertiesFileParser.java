package nz.co.pukeko.msginf.infrastructure.properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
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
     * The current messaging system being parsed
     */
    private System currentSystem;

    /**
     * The name of the current messaging system
     */
    private String messagingSystem;

    /**
     * This constructor only allows the configuration and the number of messaging systems configured to be accessed. No other system information can be accessed.
     * @throws MessageException Message exception
     */
    public MessageInfrastructurePropertiesFileParser() throws MessageException {
    }

    /**
     * Main constructor. Allow access to the messaging system configuration.
     * @param messagingSystem the messaging system
     * @throws PropertiesFileException properties file exception
     */
    public MessageInfrastructurePropertiesFileParser(String messagingSystem) throws PropertiesFileException {
        initializeCurrentSystem(messagingSystem);
    }

    public void initializeCurrentSystem(String messagingSystem) throws PropertiesFileException {
        this.messagingSystem = messagingSystem;
        parseFile();
        Optional<System> sys = findSystem();
        currentSystem = sys.orElseThrow(()-> new PropertiesFileException("No system was found in the properties file for ${messagingSystem}"));
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
     * Return the current system.
     * @return the current system.
     */
    public System getCurrentSystem() {
        return currentSystem;
    }

    /**
     * Returns the name of the current messaging system.
     * @return the name of the current messaging system.
     */
    public String getCurrentMessagingSystem() {
        return messagingSystem;
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

    private Optional<System> findSystem() {
        return findSystem(messagingSystem);
    }

    private Optional<Connectors> findConnectors() {
        return Optional.ofNullable(currentSystem).flatMap(sys -> Optional.ofNullable(sys.getConnectors()));
    }

    private Optional<Connectors> findConnectors(String messagingSystemName) {
        Optional<System> system = findSystem(messagingSystemName);
        return system.flatMap(sys -> Optional.ofNullable(sys.getConnectors()));
    }

    private Optional<Submit> findSubmit(String connectorName) {
        Optional<Connectors> connectors = findConnectors();
        return connectors.flatMap(conns -> findSubmit(conns, connectorName));
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

    private Optional<SubmitConnection> findSubmitConnection(String connectorName) {
        Optional<Submit> submit = findSubmit(connectorName);
        return submit.flatMap(sub -> Optional.ofNullable(sub.getSubmitConnection()));
    }

    private Optional<SubmitConnection> findSubmitConnection(String messagingSystemName, String connectorName) {
        Optional<Submit> submit = findSubmit(messagingSystemName, connectorName);
        return submit.flatMap(sub -> Optional.ofNullable(sub.getSubmitConnection()));
    }

    private Optional<RequestReply> findRequestReply(String connectorName) {
        Optional<Connectors> connectors = findConnectors();
        return connectors.flatMap(conns -> findRequestReply(conns, connectorName));
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

    private Optional<RequestReplyConnection> findRequestReplyConnection(String connectorName) {
        Optional<RequestReply> requestReply = findRequestReply(connectorName);
        return requestReply.flatMap(rr -> Optional.ofNullable(rr.getRequestReplyConnection()));
    }

    private Optional<RequestReplyConnection> findRequestReplyConnection(String messagingSystemName, String connectorName) {
        Optional<RequestReply> requestReply = findRequestReply(messagingSystemName, connectorName);
        return requestReply.flatMap(rr -> Optional.ofNullable(rr.getRequestReplyConnection()));
    }

    /**
     * Returns the name of the current messaging system.
     * @return the name of the current messaging system.
     */
    public String getSystemName() {
        Optional<String> sysName = Optional.ofNullable(currentSystem).flatMap(sys -> Optional.ofNullable(sys.getName()));
        return sysName.orElse("");
    }

    /**
     * Returns the initial context factory name for the current messaging system.
     * @return the initial context factory name for the current messaging system.
     */
    public String getSystemInitialContextFactory() {
        Optional<String> contextFactory = Optional.ofNullable(currentSystem).flatMap(sys -> Optional.ofNullable(sys.getInitialContextFactory()));
        return contextFactory.orElse("");
    }

    /**
     * Returns the url for the current messaging system.
     * @return the url for the current messaging system.
     */
    public String getSystemUrl() {
        Optional<String> url = Optional.ofNullable(currentSystem).flatMap(sys -> Optional.ofNullable(sys.getUrl()));
        return url.orElse("");
    }

    /**
     * Returns a list of the jar file names for the current messaging system.
     * @return a list of the jar file names for the current messaging system.
     */
    public List<String> getJarFileNames() {
        List<String> jarFileNamesList = new ArrayList<>();
        Optional.ofNullable(currentSystem).ifPresent(system ->
                jarFileNamesList.addAll(system.getJarFiles().getJarFile().stream().map(JarFile::getJarFileName).toList()));
        return jarFileNamesList;
    }

    /**
     * Returns a list of the configured queues for the current messaging system.
     * @return a list of the configured queues for the current messaging system.
     */
    public List<PropertiesQueue> getQueues() {
        List<PropertiesQueue> queuesList = new ArrayList<>();
        Optional.ofNullable(currentSystem).ifPresent(system -> {
            List<PropertiesQueue> props = system.getQueues().getQueue().stream()
                    .map(queue -> new PropertiesQueue(queue.getJndiName(), queue.getPhysicalName())).toList();
            queuesList.addAll(props);
        });
        return queuesList;
    }

    /**
     * Returns whether connection pooling is configured for the current messaging system.
     * @return whether connection pooling is configured for the current messaging system.
     */
    public boolean getUseConnectionPooling() {
        Optional<Connectors> connectors = findConnectors();
        Optional<Boolean> b = connectors.flatMap(conns -> Optional.ofNullable(conns.getUseConnectionPooling()));
        return b.orElse(false);
    }

    /**
     * Returns the maximum number of connections for the current messaging system.
     * @return the maximum number of connections for the current messaging system.
     */
    public int getMaxConnections() {
        Optional<Connectors> connectors = findConnectors();
        Optional<Integer> res = connectors.flatMap(conns -> Optional.ofNullable(conns.getMaxConnections()));
        return res.orElse(0);
    }

    /**
     * Returns the minimum number of connections for the current messaging system.
     * @return the minimum number of connections for the current messaging system.
     */
    public int getMinConnections() {
        Optional<Connectors> connectors = findConnectors();
        Optional<Integer> res = connectors.flatMap(conns -> Optional.ofNullable(conns.getMinConnections()));
        return res.orElse(0);
    }

    /**
     * Returns a list of the names of the submit connectors for the current messaging system.
     * @return a list of the names of the submit connectors for the current messaging system.
     */
    public List<String> getSubmitConnectorNames() {
        List<String> names = new ArrayList<>();
        Optional<Connectors> connectors = findConnectors();
        connectors.ifPresent(conns -> names.addAll(conns.getSubmit().stream().map(Submit::getConnectorName).toList()));
        return names;
    }

    /**
     * Returns whether the submit connector exists for the current messaging system.
     * @return whether the submit connector exists for the current messaging system.
     */
    public boolean doesSubmitExist(String connectorName) {
        Optional<Connectors> connectors = findConnectors();
        return connectors.stream().anyMatch(conns -> conns.getSubmit().stream().anyMatch(sub ->
                        sub.getConnectorName().equals(connectorName)));
    }

    /**
     * Returns a list of the names of the request-reply connectors for the current messaging system.
     * @return a list of the names of the request-reply connectors for the current messaging system.
     */
    public List<String> getRequestReplyConnectorNames() {
        List<String> names = new ArrayList<>();
        Optional<Connectors> connectors = findConnectors();
        connectors.ifPresent(conns -> names.addAll(conns.getRequestReply().stream().map(RequestReply::getConnectorName).toList()));
        return names;
    }

    /**
     * Returns whether the request-reply connector exists for the current messaging system.
     * @return whether the request-reply connector exists for the current messaging system.
     */
    public boolean doesRequestReplyExist(String connectorName) {
        Optional<Connectors> connectors = findConnectors();
        return connectors.stream().anyMatch(conns -> conns.getRequestReply().stream().anyMatch(rr ->
                rr.getConnectorName().equals(connectorName)));
    }

    /**
     * Returns whether to compress binary messages for the submit connector.
     * @return whether to compress binary messages for the submit connector.
     */
    public boolean getSubmitCompressBinaryMessages(String connectorName) {
        Optional<Submit> submit = findSubmit(connectorName);
        Optional<Boolean> b = submit.flatMap(sub -> Optional.ofNullable(sub.getCompressBinaryMessages()));
        return b.orElse(false);
    }

    /**
     * Returns the submit connection submit queue name for the submit connector.
     * @return the submit connection submit queue name for the submit connector.
     */
    public String getSubmitConnectionSubmitQueueName(String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(connectorName);
        Optional<String> queueName = connection.flatMap(sub -> Optional.ofNullable(sub.getSubmitQueueName()));
        return queueName.orElse("");
    }

    /**
     * Returns the submit connection submit queue connection factory name for the submit connector.
     * @return the submit connection submit queue connection factory name for the submit connector.
     */
    public String getSubmitConnectionSubmitQueueConnFactoryName(String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(connectorName);
        Optional<String> queueNameConnFactory = connection.flatMap(sub -> Optional.ofNullable(sub.getSubmitQueueConnFactoryName()));
        return queueNameConnFactory.orElse("");
    }

    /**
     * Returns the submit connection message class name for the request-reply connector.
     * @return the submit connection message class name for the request-reply connector.
     */
    public String getSubmitConnectionMessageClassName(String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(connectorName);
        Optional<String> messageClassName = connection.flatMap(sub -> Optional.ofNullable(sub.getMessageClassName()));
        return messageClassName.orElse("");
    }

    /**
     * Returns the submit connection message time to live for the submit connector.
     * @return the submit connection message time to live for the submit connector.
     */
    public int getSubmitConnectionMessageTimeToLive(String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(connectorName);
        Optional<Integer> messageTimeToLive = connection.flatMap(sub -> Optional.ofNullable(sub.getMessageTimeToLive()));
        return messageTimeToLive.orElse(0);
    }

    /**
     * Returns the submit connection reply wait time for the submit connector.
     * @return the submit connection reply wait time for the submit connector.
     */
    public int getSubmitConnectionReplyWaitTime(String connectorName) {
        Optional<SubmitConnection> connection = findSubmitConnection(connectorName);
        Optional<Integer> replyWaitTime = connection.flatMap(sub -> Optional.ofNullable(sub.getReplyWaitTime()));
        return replyWaitTime.orElse(0);
    }

    /**
     * Returns whether to compress binary messages for the request-reply connector.
     * @return whether to compress binary messages for the request-reply connector.
     */
    public boolean getRequestReplyCompressBinaryMessages(String connectorName) {
        Optional<RequestReply> requestReply = findRequestReply(connectorName);
        Optional<Boolean> b = requestReply.flatMap(rr -> Optional.ofNullable(rr.getCompressBinaryMessages()));
        return b.orElse(false);
    }

    /**
     * Returns the request-reply connection request queue name for the request-reply connector.
     * @return the request-reply connection request queue name for the request-reply connector.
     */
    public String getRequestReplyConnectionRequestQueueName(String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(connectorName);
        Optional<String> queueName = connection.flatMap(rr -> Optional.ofNullable(rr.getRequestQueueName()));
        return queueName.orElse("");
    }

    /**
     * Returns the request-reply connection reply queue name for the request-reply connector.
     * @return the request-reply connection reply queue name for the request-reply connector.
     */
    public String getRequestReplyConnectionReplyQueueName(String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(connectorName);
        Optional<String> queueName = connection.flatMap(rr -> Optional.ofNullable(rr.getReplyQueueName()));
        return queueName.orElse("");
    }

    /**
     * Returns the request-reply connection request queue connection factory name for the request-reply connector.
     * @return the request-reply connection request queue connection factory name for the request-reply connector.
     */
    public String getRequestReplyConnectionRequestQueueConnFactoryName(String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(connectorName);
        Optional<String> queueNameConnFactory = connection.flatMap(rr -> Optional.ofNullable(rr.getRequestQueueConnFactoryName()));
        return queueNameConnFactory.orElse("");
    }

    /**
     * Returns the request-reply connection message class name for the request-reply connector.
     * @return the request-reply connection message class name for the request-reply connector.
     */
    public String getRequestReplyConnectionMessageClassName(String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(connectorName);
        Optional<String> messageClassName = connection.flatMap(rr -> Optional.ofNullable(rr.getMessageClassName()));
        return messageClassName.orElse("");
    }

    /**
     * Returns the request-reply connection requester class name for the request-reply connector.
     * @return the request-reply connection requester class name for the request-reply connector.
     */
    public String getRequestReplyConnectionRequesterClassName(String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(connectorName);
        Optional<String> requesterClassName = connection.flatMap(rr -> Optional.ofNullable(rr.getRequesterClassName()));
        return requesterClassName.orElse("");
    }

    /**
     * Returns the request-reply connection message time to live for the request-reply connector.
     * @return the request-reply connection message time to live for the request-reply connector.
     */
    public int getRequestReplyConnectionMessageTimeToLive(String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(connectorName);
        Optional<Integer> messageTimeToLive = connection.flatMap(rr -> Optional.ofNullable(rr.getMessageTimeToLive()));
        return messageTimeToLive.orElse(0);
    }

    /**
     * Returns the request-reply connection reply wait time for the request-reply connector.
     * @return the request-reply connection reply wait time for the request-reply connector.
     */
    public int getRequestReplyConnectionReplyWaitTime(String connectorName) {
        Optional<RequestReplyConnection> connection = findRequestReplyConnection(connectorName);
        Optional<Integer> replyWaitTime = connection.flatMap(rr -> Optional.ofNullable(rr.getReplyWaitTime()));
        return replyWaitTime.orElse(0);
    }

}
