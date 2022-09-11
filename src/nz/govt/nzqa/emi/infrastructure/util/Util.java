package nz.govt.nzqa.emi.infrastructure.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import nz.govt.nzqa.emi.infrastructure.exception.InfrastructureUtilityClassException;
import nz.govt.nzqa.emi.infrastructure.exception.MessageException;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.XMLPropertiesQueue;

/**
 * Utility class.
 * 
 * @author Alisdair Hamblyn
 */
public class Util {
	private static Logger logger = Logger.getLogger(Util.class);

	/**
	 * Reads a file into a String.
	 * @param fileName the file name.
	 * @return the file contents as a String.
	 * @throws MessageException
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
	 * @throws JMSException
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
		} catch (IOException e) {
    	} catch (DataFormatException dfe) {
    	}
		// Get the decompressed data
		return bos.toByteArray();
	}

	/**
	 * Loads the jar files in the xml properties file.
	 * @throws MessageException
	 */
	public static void loadRuntimeJarFiles() throws MessageException {
		// load the runtime jar files
		XMLMessageInfrastructurePropertiesFileParser systemParser = new XMLMessageInfrastructurePropertiesFileParser();
		List availableMessagingSystems = systemParser.getAvailableMessagingSystems();
		for (int i = 0; i < availableMessagingSystems.size(); i++) {
			String messagingSystem = (String)availableMessagingSystems.get(i);
			XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser(messagingSystem);
			// load system specific jar files into classpath
			List jarFileNames = parser.getJarFileNames();
			try {
				for (int listIndex = 0; listIndex < jarFileNames.size(); listIndex++) {
					String jarFileName = (String)jarFileNames.get(listIndex);
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
	 * @throws MessageException
	 */
	public static Context createContext(String messagingSystem) throws MessageException {
		InitialContext jmsCtx = null;
		XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser(messagingSystem);
		String initialContextFactory = parser.getSystemInitialContextFactory();
		String url = parser.getSystemUrl();
		String host = parser.getSystemHost();
		int port = parser.getSystemPort();
		String namingFactoryUrlPkgs = parser.getSystemNamingFactoryUrlPkgs();
		List queues = parser.getQueues();
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
				for (int x = 0; x < queues.size(); x++) {
					XMLPropertiesQueue queue = (XMLPropertiesQueue)queues.get(x);
					props.setProperty("queue." + queue.getJndiName(), queue.getPhysicalName());
				}
				jmsCtx = new InitialContext(props);
			}
		} catch (NamingException ne) {
			// cannot connect
			logger.info("Cannot initialise " + messagingSystem);
		}
		return jmsCtx;
	}

	/**
	 * Returns an object array from a collection's array.
	 * @param genericArray
	 * @param rowClass
	 * @return the object array.
	 */
	public static Object[] narrow(Object[] genericArray, Class rowClass) {
	    Object[] typedArray = (Object[]) Array.newInstance(rowClass, genericArray.length);
	    System.arraycopy(genericArray, 0, typedArray, 0, genericArray.length);
	    return typedArray;
    }
	
	/**
     * Get the extension of a file.
     * @param file
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
	
	/**
	 * Converts an object array into a Vector.
	 * @param array
	 * @return the Vector.
	 */
	public static Vector<Object> convertArrayToVector(Object[] array) {
		Vector<Object> res = new Vector<Object>();
        for (Object anArray : array) {
            res.add(anArray);
        }
        return res;
	}

    public static void connectToPort(String host, int port) {
        // connect to the server socket
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(host, port);
            logger.info("Connected to server " + host + ":" + port);
        } catch (UnknownHostException e) {
            logger.error("Couldn't find " + host, e);
        } catch (IOException e) {
            logger.error("IO exception", e);
        }
    }
}