package nz.co.pukeko.msginf.client.adapter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Test the SenderServlet.
 * 
 * @author Alisdair Hamblyn
 */
public class TestSenderServlet {
	
	/**
	 * The http client.
	 */
	private HttpClient httpClient;
	
	/**
	 * The sender servlet URL.
	 */
	private String url = "http://localhost:8080/msginf/sender";
	
	/**
	 * Constructs a TestSenderServlet.
	 */
	public TestSenderServlet() {
		httpClient = new HttpClient();
	}

	private String createMessage() {
		String message = "<?xml version=\"1.0\"?>" +
				         "<Data xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://localhost/test\">" +
					     "  <MessageID>12345</MessageID>" +
					     "  <Claim>" +
						 "    <Name>Fred Dagg</Name>" +
						 "    <Name>Bart Simpson</Name>" +
					     "  </Claim>" +
				         "</Data>";
		return message;
	}

	/**
	 * Send a test message to a submit connector.
	 * @return the message reply.
	 */
	public String postSubmitMessage() {
		String reply = "";
		PostMethod post = null;
		String message = createMessage();
		try {
			post = new PostMethod(url);
			post.setParameter("connector", "SubmitConnector");
			post.setParameter("data", message);
	        int res = httpClient.executeMethod(post);
	        if (res != HttpStatus.SC_OK) {
	        	System.err.println("Method failed: " + post.getStatusLine());
		    }
		    reply = post.getResponseBodyAsString();		
		} catch (Exception e) {
			e.printStackTrace();
 	    } finally {
	      // Release the connection.
	      post.releaseConnection();
	    }
 	    return reply;
	}

	/**
	 * Send a test message to a request/reply connector.
	 * @return the message reply.
	 */
	public String postRequestReplyMessage() {
		String reply = "";
		PostMethod post = null;
		String message = createMessage();
		try {
			post = new PostMethod(url);
			post.setParameter("connector", "RequestReplyConnector");
			post.setParameter("data", message);
	        int res = httpClient.executeMethod(post);
	        if (res != HttpStatus.SC_OK) {
	        	System.err.println("Method failed: " + post.getStatusLine());
		    }
		    reply = post.getResponseBodyAsString();		
		} catch (Exception e) {
			e.printStackTrace();
 	    } finally {
	      // Release the connection.
	      post.releaseConnection();
	    }
 	    return reply;
	}

	/**
	 * Reset the message count.
	 */
	public void postResetCountMessage() {
		PostMethod post = null;
		String message = createMessage();
		try {
			post = new PostMethod(url);
			post.setParameter("connector", "RequestReplyConnector");
			post.setParameter("data", "XXXXXXXXXXX");
			post.setParameter("resetCount", "true");
	        int res = httpClient.executeMethod(post);
	        if (res != HttpStatus.SC_OK) {
	        	System.err.println("Method failed: " + post.getStatusLine());
		    }
		} catch (Exception e) {
			e.printStackTrace();
 	    } finally {
	      // Release the connection.
	      post.releaseConnection();
	   }
	}

	/**
	 * Run the test.
	 * @param args the command line parameters.
	 */
	public static void main(String[] args) {
		TestSenderServlet test = new TestSenderServlet();
		for (int i = 0; i < 100; i++) {
			System.out.println(test.postSubmitMessage());
		}
		for (int i = 0; i < 100; i++) {
			System.out.println(test.postRequestReplyMessage());
		}
		test.postResetCountMessage();
	}
}
