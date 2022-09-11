package nz.govt.nzqa.emi.infrastructure.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import nz.govt.nzqa.emi.infrastructure.exception.MessageException;
import nz.govt.nzqa.emi.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

import org.apache.log4j.PropertyConfigurator;

/**
 * This class sets up the logging for the messaging infrastructure.
 * 
 * @author Alisdair Hamblyn
 */

public class MessagingLoggerConfiguration extends PropertyConfigurator {

    /**
     * The static instance of this class.
     */
	private static MessagingLoggerConfiguration ml;

    /**
     * The location of the log4j file in the XML properties file
     */
   private static String logFileLocation;
   
   /**
    * The URL of the log4j file.
    */
   private static URL logFileURL;

    /**
     * The MessagingLoggerConfiguration constructor. It sets the log4j
     * PropertyConfigurator to use the log4j.properties file in the
     * XML properties file.
     */
	public MessagingLoggerConfiguration() {
       if (logFileLocation == null) {
    	  try {
              XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser();
              logFileLocation = parser.getLog4jPropertiesFile();
              System.out.println("log4j file location: " + logFileLocation);
              // try to load as file
              if (!loadFile()) {
        		  // load the default file
        		  loadDefault();
              }
    	  } catch (MessageException me) {
    		  me.printStackTrace();
    	  }
       }
       super.configure(logFileURL);
	}
	
	private void loadDefault() {
		// use the default /log4j.properties in the msginf.jar file
        System.out.println("Using default log4j file /log4j.properties");
        logFileURL = MessagingLoggerConfiguration.class.getResource("/log4j.properties");
        System.out.println("log4j file URL: " + logFileURL);
	}
	
	private boolean loadFile() {
		boolean fileOK = false;
        try {
            File logFile = new File(logFileLocation);
            // test the file. if not found return false
            if (logFile.exists()) {
    			logFileURL  = logFile.toURL();
                System.out.println("log4j file URL: " + logFileURL);
            	fileOK = true;
            }
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (SecurityException se) {
			se.printStackTrace();
		}
		return fileOK;
	}
	
    /**
     * Instantiate the static instance.
     */
	public static void configure() {
		MessagingLoggerConfiguration ml = new MessagingLoggerConfiguration();
	}
}
