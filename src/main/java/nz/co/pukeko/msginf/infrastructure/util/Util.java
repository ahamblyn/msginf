package nz.co.pukeko.msginf.infrastructure.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.exception.InfrastructureUtilityClassException;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.properties.PropertiesQueue;

/**
 * Utility class.
 * 
 * @author Alisdair Hamblyn
 */
@Slf4j
public class Util {
	/**
	 * Reads a file into a String.
	 * @param fileName the file name.
	 * @return the file contents as a String.
	 * @throws MessageException Message exception
	 */
	public static String readFile(String fileName) throws MessageException {
		String res = "";
		try {
			String thisLine;
			FileInputStream fin = new FileInputStream(fileName);
			BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				res = res + thisLine;
			}
		} catch (IOException ioe) {
			throw new InfrastructureUtilityClassException(ioe);
		}
		return res;
	}

	/**
	 * Decompresses a bytes message.
	 * @param message the bytes message.
	 * @return the decompressed byte array.
	 * @throws JMSException JMS exception
	 */
	public static byte[] decompressBytesMessage(BytesMessage message) throws JMSException {
    	message.getBodyLength();
    	long length = message.getBodyLength();
    	byte[] data = new byte[(int)length];
    	message.readBytes(data);
		Inflater decompresser = new Inflater();
		// Give the decompresser the data to inflate
		decompresser.setInput(data, 0, data.length);
		// Create a byte array to hold the decompressed data.
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
		// decompress the data
		try {
			byte[] buf = new byte[1024];
			while (!decompresser.finished()) {
				int count = decompresser.inflate(buf);
				bos.write(buf, 0, count);
			}	// end while
		    bos.close();
		} catch (IOException | DataFormatException e) {
    	}
		// Get the decompressed data
		return bos.toByteArray();
	}

	/**
	 * Loads the jar files in the properties file.
	 * @throws MessageException Message exception
	 */
	public static void loadRuntimeJarFiles() throws MessageException {
		// load the runtime jar files
		MessageInfrastructurePropertiesFileParser systemParser = new MessageInfrastructurePropertiesFileParser();
		List<String> availableMessagingSystems = systemParser.getAvailableMessagingSystems();
		for (String messagingSystem : availableMessagingSystems) {
			MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser(messagingSystem);
			// load system specific jar files into classpath
			List<String> jarFileNames = parser.getJarFileNames();
			try {
				for (String jarFileName : jarFileNames) {
					ClassPathHacker.addFile(jarFileName);
				}
			} catch (IOException ioe) {
				throw new MessageException(ioe);
			}
		}
	}

	/**
	 * Create the context.
	 * @param messagingSystem the messaging system.
	 * @return the context.
	 * @throws MessageException Message exception
	 */
	public static Context createContext(String messagingSystem) throws MessageException {
		InitialContext jmsCtx = null;
		MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser(messagingSystem);
		String initialContextFactory = parser.getSystemInitialContextFactory();
		String url = parser.getSystemUrl();
		String host = parser.getSystemHost();
		int port = parser.getSystemPort();
		String namingFactoryUrlPkgs = parser.getSystemNamingFactoryUrlPkgs();
		List<PropertiesQueue> queues = parser.getQueues();
		try {
			if (initialContextFactory == null || initialContextFactory.equals("")) {
				// no properties required to initialise context
				jmsCtx = new InitialContext();
			} else {
				Properties props = new Properties();
				props.setProperty(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
				if (url != null && !url.equals("")) {
					props.setProperty(Context.PROVIDER_URL, url);
					props.setProperty("brokerURL", url);
				}
				if (host != null && !host.equals("")) {
					props.setProperty("java.naming.factory.host", host);
					props.setProperty("java.naming.factory.port", Integer.toString(port));
				}
				if (namingFactoryUrlPkgs != null && !namingFactoryUrlPkgs.equals("")) {
					props.setProperty(Context.URL_PKG_PREFIXES, namingFactoryUrlPkgs);
				}
				// add queue info
				for (PropertiesQueue queue : queues) {
					props.setProperty("queue." + queue.getJndiName(), queue.getPhysicalName());
				}
				jmsCtx = new InitialContext(props);
			}
		} catch (NamingException ne) {
			// cannot connect
			log.info("Cannot initialise " + messagingSystem);
		}
		return jmsCtx;
	}

	/**
     * Get the extension of a file.
     * @param file File
     * @return the extension
     */
	public static String getExtension(File file) {
        String ext = null;
        String s = file.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }	
	
    public static void connectToPort(String host, int port) {
        // connect to the server socket
        try {
			Socket clientSocket = new Socket(host, port);
            log.info("Connected to server " + host + ":" + port);
        } catch (UnknownHostException e) {
            log.error("Couldn't find " + host, e);
        } catch (IOException e) {
            log.error("IO exception", e);
        }
    }
}