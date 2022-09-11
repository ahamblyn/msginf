package nz.govt.nzqa.emi.client.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.govt.nzqa.emi.client.adapter.spring.SpringProducer;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Test servlet to put messages onto a queue using Spring.
 * 
 * @author Alisdair Hamblyn
 */
public class SpringServlet extends HttpServlet {
	
	/**
	 * The Spring Producer.
	 */
	private SpringProducer producer;

    /**
     * Initialises the servlet.
     * @param config the servlet configuration.
     * @throws ServletException
     */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
        XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("applicationContext.xml"));
        producer = (SpringProducer) factory.getBean("producer");
	}
	
	private String createHTMLResponse(String message) {
		String response = "<html>" +
		                  "<head><title>Spring</title></head>" +
						  "<body bgcolor=\"#FFFFCC\">" +
						  message +
						  "<P><a href=\"/msginf/html/Spring.html\">Return</a>" +
						  "</body>" +
						  "</html>";
		return response;
	}

	/**
     * Handles GET requests.
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		String message = request.getParameter("message");
		String htmlResponse = "";
		if (message == null || message.equals("")) {
			htmlResponse = createHTMLResponse("Please enter a message.");
		} else {
			htmlResponse = createHTMLResponse(message);
		}
		producer.sendMessage(message);
		out.print(htmlResponse);
		out.flush();
		out.close();
	}

	/**
     * Handles POST requests.
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
