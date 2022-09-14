package nz.co.pukeko.msginf.infrastructure.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

import nz.co.pukeko.msginf.infrastructure.exception.ByteArrayWrapperException;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A wrapper class to store a byte array.
 *  
 * @author Alisdair Hamblyn
 */
public class ByteArrayWrapper {
	
	/**
	 * The log4j2 logger.
	 */
	private static Logger logger = LogManager.getLogger(ByteArrayWrapper.class);

	/**
	 * The byte array.
	 */
	private byte[] data = null;
	
	/**
	 * Whether the internal byte[] is String data or not.
	 */
	private boolean stringData = false;
	
	/**
	 * Constructs the ByteArrayWrapper.
	 * @param data the byte array data.
	 * @param stringData whether the internal data is a String or not.
	 */
	public ByteArrayWrapper(byte[] data, boolean stringData) throws MessageException {
		this(data, stringData, false);
	}
	
	/**
	 * Constructs the ByteArrayWrapper.
	 * @param data the byte array data.
	 * @param stringData whether the internal data is a String or not.
	 * @param compress whether to compress the data or not.
	 * @throws MessageException
	 */
	public ByteArrayWrapper(byte[] data, boolean stringData, boolean compress) throws MessageException {
		MessagingLoggerConfiguration.configure();
		if (compress) {
			// compress the binary data
			byte[] compressedData = compress(data);
			double compressionRatio = (double)compressedData.length / (double)data.length;
			logger.info("Compression Ratio: " + (100 - Math.round(compressionRatio * 100)) + "%");
			this.data = compress(data);
		} else {
			this.data = data;
		}
		this.stringData = stringData;
	}
	
	/**
	 * Gets the byte array data.
	 * @return the byte array data.
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * Gets the byte array data size.
	 * @return the byte array data size.
	 */
	public int getSize() {
		if (data != null) {
			return data.length;
		} else {
			return 0;
		}
	}
	
    /**
     * Gets whether the internal byte[] is String data or not.
     * @return whether the internal byte[] is String data or not.
     */
	public boolean isStringData() {
		return stringData;
	}

	/**
     * Gets this object as a String.
     * @return this object as a String.
     */
	public String toString() {
		if (data != null) {
			if (stringData) {
				return new String(data);
			} else {
				return "Data size = " + data.length + " bytes.";
			}
		} else {
			return "Data size = undefined";
		}
	}

	private byte[] compress(byte[] input) throws MessageException {
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);
		// Give the compressor the data to compress
		compressor.setInput(input);
		compressor.finish();
		// Create a byte array to hold the compressed data.
		// There is no guarantee that the compressed data will be smaller than
		// the uncompressed data.
		ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
		// Compress the data
		byte[] buf = new byte[1024];
		while (!compressor.finished()) {
			int count = compressor.deflate(buf);
			bos.write(buf, 0, count);
		}	// end while
		try {
		     bos.close();
		} catch (IOException ioe) {
			throw new ByteArrayWrapperException(ioe);
		}
		// Get the compressed data
		return bos.toByteArray();
	}

}
