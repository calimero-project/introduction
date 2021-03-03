/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2013 B. Malinowsky

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;

/**
 * This example shows how to establish a client link to a KNXnet/IP server.
 * <p>
 * In addition to the basic {@link CreateKnxLink} example, it uses a KNX network link constructor
 * with more configuration possibilities for connection service mode and local endpoint.<br>
 * It uses the basic interface for this in Calimero, a {@link KNXNetworkLink}.<br>
 * In any case, the (established) connection is closed directly afterwards; no KNX messages are sent
 * to the KNX network. This allows you to safely run this example.
 * <p>
 * The example is sprinkled with code lines of <code>System.out</code> to print status using the
 * standard output stream.
 * 
 * @author B. Malinowsky
 */
public class CreateKnxLink2
{
	/**
	 * Specifies the KNX server, either as host name or IP address, used for access to the KNX
	 * network. Replace the string with an actual host/address of yours. For example, if you know
	 * the IP address of your KNXnet/IP server, use something like "192.168.1.20".
	 */
	private static final String remoteHost = "myKnxServer.myHome.com";
	
	/**
	 * The local host used for the connection. Replace the IP address with a local of yours.
	 */
	private static final String localHost = "192.168.1.10";
	
	/**
	 * The UDP control port a KNXnet/IP server listens for new KNX connection.<p>
	 */
	private static final int knxServerPort = KNXnetIPConnection.DEFAULT_PORT;
	
	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// System.out is the standard output stream of our environment (here, most likely terminal)
		System.out.println("This example shows how to establish a KNX connection "
				+ "to a KNXnet/IP server.");

		try {
			// KNXNetworkLink is the base interface of a Calimero link to a KNX network.
			// For all the Calimero interfaces you don't know yet, check the Calimero API
			// documentation.
			final KNXNetworkLink knxLink;

			// We have to provide our own local and remote endpoints to the network link
			// using an InetSocketAddress, which we create first. This approach is of advantage if
			// our computer is multi-homed, i.e., has several network interface cards, the default
			// configuration of local host (via InetAddress.getLocalHost) is not correct or not
			// desired, or if the KNXnet/IP server (i.e., remote endpoint) uses a non-default port.
			final InetSocketAddress localEP = new InetSocketAddress(
					InetAddress.getByName(localHost), 0);
			// now the remote port
			final InetSocketAddress remoteEP = new InetSocketAddress(remoteHost, knxServerPort);
			
			System.out.println("Try connecting to " + remoteHost + " on port " + knxServerPort + "...");
			
			// Here, we create the IP-based link, which tries to establish a connection to the
			// remote endpoint in its constructor.
			// This constructor also allows to specify the service mode of the link, i.e.,
			// KNX tunneling or routing according to the standard, and whether to use NAT-aware
			// (Network Address Translation) addressing. The last parameter tells our
			// link that the KNX network is based on twisted-pair medium, with TP1 currently being
			// the most commonly used one for KNX networks.
			knxLink = new KNXNetworkLinkIP(KNXNetworkLinkIP.TUNNELING, localEP, remoteEP, false,
					TPSettings.TP1);

			System.out.println("Connection to " + remoteHost + " successfully established");

			// We always make sure to close the connection after we used it
			knxLink.close();

			System.out.println("Connection got closed");
		}
		catch (final KNXException e) {

			// All checked Calimero-specific exceptions are subtypes of KNXException
			System.out.println("Error connecting to " + remoteHost + ": " + e.getMessage());
		}
		catch (final InterruptedException e) {

			// Longer tasks that might block, like connection procedures, are interruptible, e.g.,
			// by using Ctrl+C from the terminal; in such case, an instance of InterruptedException
			// is thrown.
			// If a task got interrupted, Calimero will clean up its internal state and resources.
			// Any deviation of this behavior (e.g., if not feasible) will be noted in the
			// Calimero API documentation.
			System.out.println("Connecting to " + remoteHost + " was interrupted, quit");
		}
		catch (final UnknownHostException e) {
			System.out.println("Host resolution for local endpoint " + localHost + " failed");
		}
	}
}
