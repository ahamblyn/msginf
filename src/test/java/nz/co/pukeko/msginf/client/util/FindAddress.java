/*
 * Created on 30/05/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nz.co.pukeko.msginf.client.util;

import java.net.InetAddress;

/**
 * @author AlisdairH
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FindAddress {

	public static void main(String[] args) {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			System.out.println("Local Host: " + localHost);
			System.out.println("Canonical Host Name: " + localHost.getCanonicalHostName());
			System.out.println("Host Address: " + localHost.getHostAddress());
			System.out.println("Host Name: " + localHost.getHostName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
