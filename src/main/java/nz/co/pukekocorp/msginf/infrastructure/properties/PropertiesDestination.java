package nz.co.pukekocorp.msginf.infrastructure.properties;

/**
 * This class encapsulates a destination in the properties file.
 * @param jndiName the jndiName attribute.
 * @param physicalName the physicalName attribute.
 */
public record PropertiesDestination(String jndiName, String physicalName) {
}
