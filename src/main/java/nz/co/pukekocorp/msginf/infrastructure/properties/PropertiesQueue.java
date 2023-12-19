package nz.co.pukekocorp.msginf.infrastructure.properties;

/**
 * This class encapsulates a queue in the properties file.
 * @author alisdairh
 */
public class PropertiesQueue {
	/**
	 * The jndiName attribute.
	 */
	private String jndiName;

	/**
	 * The physicalName attribute.
	 */
	private String physicalName;

	/**
	 * Constructor.
	 * @param jndiName the jndiName attribute.
	 * @param physicalName the physicalName attribute.
	 */
	public PropertiesQueue(String jndiName, String physicalName) {
		this.jndiName = jndiName;
		this.physicalName = physicalName;
	}

	/**
	 * Returns the jndi name.
	 * @return the jndi name.
	 */
	public String getJndiName() {
		return jndiName;
	}

	/**
	 * Returns the physical name.
	 * @return the physical name.
	 */
	public String getPhysicalName() {
		return physicalName;
	}
	
	/**
	 * Sets the JNDI name.
	 * @param jndiName the JNDI name.
	 */
	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	/**
	 * Sets the physical name.
	 * @param physicalName the physical name.
	 */
	public void setPhysicalName(String physicalName) {
		this.physicalName = physicalName;
	}

	/**
	 * toString method.
	 * @return the instance as a String.
	 */
	public String toString() {
		return jndiName + ":" + physicalName;
	}
}
