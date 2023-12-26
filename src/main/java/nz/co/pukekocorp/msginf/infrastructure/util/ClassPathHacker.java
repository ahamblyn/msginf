package nz.co.pukekocorp.msginf.infrastructure.util;

import ca.cgjennings.jvm.JarLoader;

import java.io.File;
import java.io.IOException;

/**
 * Utility class to add external jar files to the classpath at runtime.
 */
public class ClassPathHacker {

	/**
	 * Add a jar file to the classpath
	 * @param path the absolute file path
	 * @throws IOException IO Exception
	 */
	public static void addFile(String path) throws IOException {
		File f = new File(path);
		addFile(f);
	}

	/**
	 * Add a jar file to the classpath
	 * @param file the file
	 * @throws IOException IO Exception
	 */
	public static void addFile(File file) throws IOException {
		JarLoader.addToClassPath(file);
	}
}
