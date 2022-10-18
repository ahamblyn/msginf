package nz.co.pukeko.msginf.infrastructure.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import lombok.extern.slf4j.Slf4j;
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
	 * Compress the input byte[]
	 * @param input the byte[] to compress
	 * @param compressionLevel tje compression level
	 * @return the compressed byte[]
	 */
	public static byte[] compress(byte[] input, int compressionLevel) {
		Deflater compressor = new Deflater(compressionLevel);
		compressor.setInput(input);
		compressor.finish();
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		byte[] readBuffer = new byte[1024];
		while (!compressor.finished()) {
			int readCount = compressor.deflate(readBuffer);
			if (readCount > 0) {
				bao.write(readBuffer, 0, readCount);
			}
		}
		compressor.end();
		return bao.toByteArray();
	}

	/**
	 * Decompress the input byte[]
	 * @param input the byte[] to decompress
	 * @return the decompressed byte[]
	 * @throws DataFormatException the data format exception
	 */
	public static byte[] decompress(byte[] input) throws DataFormatException {
		Inflater decompressor = new Inflater();
		decompressor.setInput(input);
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		byte[] readBuffer = new byte[1024];
		while (!decompressor.finished()) {
			int readCount = decompressor.inflate(readBuffer);
			if (readCount > 0) {
				bao.write(readBuffer, 0, readCount);
			}
		}
		decompressor.end();
		return bao.toByteArray();
	}

	/**
	 * Loads the jar files in the properties file.
	 * @param parser the properties file parser
	 * @throws MessageException Message exception
	 */
	public static void loadRuntimeJarFiles(MessageInfrastructurePropertiesFileParser parser) throws MessageException {
		// load the runtime jar files
		List<String> availableMessagingSystems = parser.getAvailableMessagingSystems();
		for (String messagingSystem : availableMessagingSystems) {
			// load system specific jar files into classpath
			List<String> jarFileNames = parser.getJarFileNames(messagingSystem);
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
	 * @param parser the properties file parser
	 * @param messagingSystem the messaging system.
	 * @return the context.
	 */
	public static Context createContext(MessageInfrastructurePropertiesFileParser parser, String messagingSystem) {
		InitialContext jmsCtx = null;
		String initialContextFactory = parser.getSystemInitialContextFactory(messagingSystem);
		String url = parser.getSystemUrl(messagingSystem);
		String host = parser.getSystemHost(messagingSystem);
		int port = parser.getSystemPort(messagingSystem);
		String namingFactoryUrlPkgs = parser.getSystemNamingFactoryUrlPkgs(messagingSystem);
		List<PropertiesQueue> queues = parser.getQueues(messagingSystem);
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
	 * Convert the binary message string into a ByteArrayOutputStream
	 * @param binaryMessage the binary message to decode
	 * @return the decoded binary message
	 */
	public static byte[] decodeBinaryMessage(String binaryMessage) throws MessageException {
		try {
			return Base64.getDecoder().decode(binaryMessage);
		} catch (RuntimeException e) {
			throw new MessageException("Unable to decode the binary message");
		}
	}

	public static String encodeBinaryMessage(byte[] binaryMessage) {
		if (binaryMessage != null) {
			return Base64.getEncoder().encodeToString(binaryMessage);
		} else {
			// Want null returned, so it is not displayed in JSON response.
			return null;
		}
	}
}