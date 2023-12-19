package nz.co.pukekocorp.msginf.infrastructure.util;

import ca.cgjennings.jvm.JarLoader;

import java.io.File;
import java.io.IOException;

public class ClassPathHacker {

	public static void addFile(String s) throws IOException {
		File f = new File(s);
		addFile(f);
	}
	 
	public static void addFile(File f) throws IOException {
		JarLoader.addToClassPath(f);
	}
}
