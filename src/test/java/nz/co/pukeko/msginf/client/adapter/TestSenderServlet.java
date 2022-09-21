package nz.co.pukeko.msginf.client.adapter;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test the SenderServlet.
 * 
 * @author Alisdair Hamblyn
 */
public class TestSenderServlet {
	
	/**
	 * The sender servlet URL.
	 */
	private final String url = "http://localhost:8080/msginf/sender";
	
	/**
	 * Constructs a TestSenderServlet.
	 */
	public TestSenderServlet() {
	}

	private String createMessage() {
		return "<?xml version=\"1.0\"?>" +
				         "<Data xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://localhost/test\">" +
					     "  <MessageID>12345</MessageID>" +
					     "  <Claim>" +
						 "    <Name>Fred Dagg</Name>" +
						 "    <Name>Bart Simpson</Name>" +
					     "  </Claim>" +
				         "</Data>";
	}

	/**
	 * Send a test message to a submit connector.
	 * @return the message reply.
	 */
	public String postSubmitMessage() {
		String reply = "";
		String message = createMessage();
		try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost post = new HttpPost(url);
			List<NameValuePair> nvps = new ArrayList<>();
			nvps.add(new BasicNameValuePair("connector", "SubmitConnector"));
			nvps.add(new BasicNameValuePair("data", message));
			post.setEntity(new UrlEncodedFormEntity(nvps));
			reply = httpClient.execute(post, new TestHttpClientResponseHandler<>());
		} catch (Exception e) {
			e.printStackTrace();
	    }
 	    return reply;
	}

	/**
	 * Send a test message to a request/reply connector.
	 * @return the message reply.
	 */
	public String postRequestReplyMessage() {
		String reply = "";
		String message = createMessage();
		try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost post = new HttpPost(url);
			List<NameValuePair> nvps = new ArrayList<>();
			nvps.add(new BasicNameValuePair("connector", "RequestReplyConnector"));
			nvps.add(new BasicNameValuePair("data", message));
			post.setEntity(new UrlEncodedFormEntity(nvps));
			reply = httpClient.execute(post, new TestHttpClientResponseHandler<>());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reply;
	}

	/**
	 * Reset the message count.
	 */
	public void postResetCountMessage() {
		try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost post = new HttpPost(url);
			List<NameValuePair> nvps = new ArrayList<>();
			nvps.add(new BasicNameValuePair("connector", "RequestReplyConnector"));
			nvps.add(new BasicNameValuePair("data", "XXXXXXXXXXX"));
			nvps.add(new BasicNameValuePair("resetCount", "true"));
			post.setEntity(new UrlEncodedFormEntity(nvps));
			httpClient.execute(post, new TestHttpClientResponseHandler<>());
		} catch (Exception e) {
			e.printStackTrace();
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

	private static class TestHttpClientResponseHandler<String> implements HttpClientResponseHandler<String> {

		@Override
		public String handleResponse(ClassicHttpResponse response) throws IOException {
			final int status = response.getCode();
			if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
				final HttpEntity entity = response.getEntity();
				try {
					return entity != null ? (String) EntityUtils.toString(entity) : null;
				} catch (final ParseException ex) {
					throw new ClientProtocolException(ex);
				}
			} else {
				throw new ClientProtocolException("Unexpected response status: " + status);
			}

		}
	}
}
