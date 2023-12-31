package nz.co.pukekocorp.msginf.infrastructure.properties;

/**
 * This class encapsulates a queue in the properties file.
 * @param jndiName the jndiName attribute.
 * @param physicalName the physicalName attribute.
 */
public record PropertiesQueue(String jndiName, String physicalName) {
}
