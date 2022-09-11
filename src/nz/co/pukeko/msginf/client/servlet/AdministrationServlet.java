package nz.co.pukeko.msginf.client.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.pukeko.msginf.infrastructure.data.QueueStatistics;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.queue.QueueChannelPoolFactory;

/**
 * This servlet administers the messaging infrastructure.
 *   
 * @author Alisdair Hamblyn
 */
public class AdministrationServlet extends HttpServlet {

	/**
	 * The queue statistics collector.
	 */
	private QueueStatisticsCollector collector = QueueStatisticsCollector.getInstance();
	
    /**
     * Initialises the servlet.
     * @param config the servlet configuration.
     * @throws ServletException
     */
	public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * Handles GET requests.
     * @param request the HTTP request.
     * @param response the HTTP response
     * @throws ServletException
     * @throws IOException
     */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String action = request.getParameter("action");
        String key = request.getParameter("key");
        if (action == null || action.equals("")) {
        	messageHTML("No action", out);
        	return;
        }
        if (action.equals("RestartQueues")) {
        	// restart the queues
        	try {
        		restartQueues();
            	messageHTML("Queue restart successful", out);
        	} catch (MessageException me) {
        		exceptionMessageHTML(out, me);
        	}
        } else if (action.equals("StartQueues")) {
        	// start the queues
        	try {
        		startQueues();
            	messageHTML("Queue start successful", out);
        	} catch (MessageException me) {
        		exceptionMessageHTML(out, me);
        	}
        } else if (action.equals("StopQueues")) {
        	// stop the queues
        	try {
        		stopQueues();
            	messageHTML("Queue stop successful", out);
        	} catch (MessageException me) {
        		exceptionMessageHTML(out, me);
        	}
        } else if (action.equals("QueueStatisticsSummaryList")) {
        	// get queue stats summary list
    		queueStatisticsSummaryListHTML(queueChannelPoolStats(), out);
        } else if (action.equals("QueueStatisticsSummary")) {
        	// get queue stats summary
    		queueStatisticsSummaryHTML(queueChannelPoolStats(), out, key);
        } else if (action.equals("QueueStatisticsSummaryDetails")) {
        	// get queue stats details
    		queueStatisticsDetailsHTML(queueChannelPoolStats(), out, key);
        } else if (action.equals("ResetQueueStatistics")) {
        	// reset queue stats
    		resetQueueChannelPoolStats();
        	messageHTML("Queue Statistics reset", out);
        } else {
        	messageHTML("The " + action + " action doesn't exist", out);
        }
    }

    /**
     * Handles POST requests.
     * @param request the HTTP request.
     * @param response the HTTP response
     * @throws ServletException
     * @throws IOException
     */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }
    
    private void restartQueues() throws MessageException {
    	QueueChannelPoolFactory.getInstance().restartQueueChannelPools();
    }
    
    private void startQueues() throws MessageException {
    	QueueChannelPoolFactory.getInstance().startQueueChannelPools();
    }

    private void stopQueues() throws MessageException {
    	QueueChannelPoolFactory.getInstance().stopQueueChannelPools();
    }

    private Hashtable<String,QueueStatistics> queueChannelPoolStats() {
    	return collector.getQueueStatsTable();
    }
    
    private void resetQueueChannelPoolStats() {
    	collector.resetQueueStatistics();
    }

    private void messageHTML(String message, PrintWriter out) {
       out.println("<HTML>");
       out.println("<HEAD><TITLE>EMI Administration</TITLE></HEAD>");
       out.println("<BODY BGCOLOR=\"#FFFFCC\">");
       out.println("<H3>Action Result</H3>");
       out.println(message);
       out.println("<P><A HREF=\"/msginf/html/Admin.html\">Return</A>");
       out.println("</BODY></HTML>");
       out.flush();
       out.close();
    }
    
    private void queueStatisticsDetailsHTML(Hashtable<String,QueueStatistics> stats, PrintWriter out, String key) {
    	if (key != null && !key.equals("")) {
            out.println("<HTML>");
            out.println("<HEAD><TITLE>EMI Administration</TITLE></HEAD>");
            out.println("<BODY BGCOLOR=\"#FFFFCC\">");
            out.println("<H3>Queue Statistics Details for " + key + "</H3>");
        	QueueStatistics qs = stats.get(key);
            out.println("<TABLE BORDER=\"1\">");
            out.println("<TR><TH>Message Count</TH><TH>Time (ms)</TH></TR>");
            Vector messageTimes = qs.getMessageTimes();
            for (int i = 0; i < messageTimes.size(); i++) {
            	Long time = (Long)messageTimes.elementAt(i);
                out.println("<TR><TD>" + (i + 1) + "</TD><TD>" + time + "</TD></TR>");
            }
            out.println("</TABLE>");
            out.println("<P><A HREF=\"/msginf/admin?action=QueueStatisticsSummaryList\">Queue Statistics Summary List</A>");
            out.println("<P><A HREF=\"/msginf/html/Admin.html\">Return</A>");
            out.println("</BODY></HTML>");
            out.flush();
            out.close();
    	}
    }

    private void queueStatisticsSummaryHTML(Hashtable<String,QueueStatistics> stats, PrintWriter out, String key) {
        out.println("<HTML>");
        out.println("<HEAD><TITLE>EMI Administration</TITLE></HEAD>");
        out.println("<BODY BGCOLOR=\"#FFFFCC\">");
        out.println("<H3>Queue Statistics Summary for " + key + "</H3>");
    	QueueStatistics qs = stats.get(key);
        out.println("<TABLE BORDER=\"1\">");
        out.println("<TR><TD>Messages Sent</TD><TD>" + qs.getMessageCount() + "</TD></TR>");
        if (qs.getFailedMessageCount() > 0) {
            out.println("<TR><TD>Failed Messages</TD><TD><FONT COLOR=\"#FF0000\">" + qs.getFailedMessageCount() + "</FONT></TD></TR>");
        } else {
            out.println("<TR><TD>Failed Messages</TD><TD>" + qs.getFailedMessageCount() + "</TD></TR>");
        }
        out.println("<TR><TD>Average Time Per Message (ms)</TD><TD>" + qs.getAverageTimePerMessage() + "</TD></TR>");
        out.println("<TR><TD>Median Time Per Message (ms)</TD><TD>" + qs.getMedianMessageTime() + "</TD></TR>");
        out.println("<TR><TD>Max Message Time (ms)</TD><TD>" + qs.getMaxMessageTime() + "</TD></TR>");
        out.println("<TR><TD>Min Message Time (ms)</TD><TD>" + qs.getMinMessageTime() + "</TD></TR>");
        out.println("</TABLE>");
        out.println("<P><H3>Queue Statistics Histogram Data for " + key + "</H3>");
        Hashtable<Long,Integer> histData = qs.getHistogramData();
        createHistogram(histData, out);
        out.println("<P><TABLE BORDER=\"1\">");
        out.println("<TR><TH>Time (ms)</TH><TH>Count</TH></TR>");
		SortedSet<Long> ss = new TreeSet<Long>(histData.keySet());
        for (Long histkey : ss) {
            out.println("<TR><TD>" + histkey + "</TD><TD>" + histData.get(histkey) + "</TD></TR>");
        }
        out.println("</TABLE>");
        out.println("<P><A HREF=\"/msginf/admin?action=QueueStatisticsSummaryList\">Queue Statistics Summary List</A>");
        out.println("<P><A HREF=\"/msginf/html/Admin.html\">Return</A>");
        out.println("</BODY></HTML>");
        out.flush();
        out.close();
     }

    private void queueStatisticsSummaryListHTML(Hashtable<String,QueueStatistics> stats, PrintWriter out) {
        out.println("<HTML>");
        out.println("<HEAD><TITLE>EMI Administration</TITLE></HEAD>");
        out.println("<BODY BGCOLOR=\"#FFFFCC\">");
        out.println("<H3>Queue Statistics Summary List</H3>");
        out.println("<TABLE BORDER=\"1\">");
        out.println("<TR><TH>Collection Name</TH><TH>Summary</TH><TH>Details</TH></TR>");
        Vector<String> v = new Vector<String>(stats.keySet());
        Collections.sort(v);
        for (String key : v) {
            out.println("<TR><TD>" + key + "</TD><TD><A HREF=\"/msginf/admin?action=QueueStatisticsSummary&key=" + key + "\">Summary</A></TD><TD><A HREF=\"/msginf/admin?action=QueueStatisticsSummaryDetails&key=" + key + "\">Details</A></TD></TR>");
        }
        out.println("</TABLE>");
        out.println("<P><A HREF=\"/msginf/html/Admin.html\">Return</A>");
        out.println("</BODY></HTML>");
        out.flush();
        out.close();
     }

	private void createHistogram(Hashtable<Long,Integer> histData, PrintWriter out) {
		SortedSet<Long> ss = new TreeSet<Long>(histData.keySet());
		Iterator itMaxMin = ss.iterator();
		long minXValue = Long.MAX_VALUE;
		long maxXValue = 0;
		long minYValue = Long.MAX_VALUE;
		long maxYValue = 0;
		while (itMaxMin.hasNext()) {
        	Long histkey = (Long)itMaxMin.next();
        	if (histkey > maxXValue) {
        		maxXValue = histkey;
        	}
        	if (histkey < minXValue) {
        		minXValue = histkey;
        	}
        	Integer value = histData.get(histkey);
        	if (value.longValue() > maxYValue) {
        		maxYValue = value.longValue();
        	}
        	if (value.longValue() < minYValue) {
        		minYValue = value.longValue();
        	}
		}
		// 30 pixels width per column
		int width = ss.size() * 30;
		// aspect ratio 0.75
		int height = (int)(width * 0.75);
        out.println("<P><APPLET code=\"barchart.class\" height=" + height + " width=" + width + ">");
        out.println("<PARAM name=title value=\"Histogram\">");
        out.println("<PARAM name=NumberOfVals value=1>");
        out.println("<PARAM name=NumberOfCols value=" + ss.size() + ">");
        out.println("<PARAM name=border value=50>");
        out.println("<PARAM name=yborder value=18>");
        out.println("<PARAM name=colmargin value=6>");
        out.println("<PARAM name=keywidth value=90>");
        out.println("<PARAM name=ymax value=" + maxYValue + ">");
        out.println("<PARAM name=ymin value=0>");
        out.println("<PARAM name=KEY_1 value=\"Count\">");
        out.println("<PARAM name=LineColor_R_1 value=200>");
        out.println("<PARAM name=LineColor_G_1 value=0>");
        out.println("<PARAM name=LineColor_B_1 value=0>");
        out.println("<PARAM name=Grid value=\"true\">");
		Iterator it = ss.iterator();
		int count = 0;
		while (it.hasNext()) {
        	Long histkey = (Long)it.next();
            out.println("<PARAM name=VAL_" + ++count + "_1 value=" + histData.get(histkey) + ">");
		}
		Iterator it1 = ss.iterator();
		count = 0;
		while (it1.hasNext()) {
        	Long histkey = (Long)it1.next();
            out.println("<PARAM name=LAB" + ++count + " value=\"" + histkey + "\">");
		}
        out.println("</APPLET>");
	}

	private void exceptionMessageHTML(PrintWriter out, Exception e) {
        out.println("<HTML>");
        out.println("<HEAD><TITLE>EMI Administration</TITLE></HEAD>");
        out.println("<BODY BGCOLOR=\"#FFFFCC\">");
        out.println("<H3>Result</H3>");
        out.println("Action failed<P>");
        out.println("<PRE>");
        out.println(e.getMessage() + "<P>");
        e.printStackTrace(out);
        out.println("</PRE>");
        out.println("<P><A HREF=\"/msginf/html/Admin.html\">Return</A>");
        out.println("</BODY></HTML>");
        out.flush();
        out.close();
     }
}
