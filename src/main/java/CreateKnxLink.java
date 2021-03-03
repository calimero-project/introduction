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

import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;

/**
 * This example shows how to establish a client link to a KNXnet/IP server with KNX default UDP port
 * settings.
 * <p>
 * It uses the basic interface for this in Calimero, a {@link KNXNetworkLink}.<br>
 * In any case, the (established) connection is closed directly afterwards; no KNX messages are sent
 * to the KNX network. This allows you to safely run this example.
 * <p>
 * The example is sprinkled with <code>System.out</code> lines to print status using the standard
 * output stream.
 * 
 * @author B. Malinowsky
 */
public class CreateKnxLink
{
	/**
	 * Specifies the KNX server, either as host name or IP address, used for access to the KNX
	 * network. Replace the string with an actual host/address of yours. For example, if you know
	 * the IP address of your KNXnet/IP server, use something like "192.168.1.20".
	 */
	private static final String remoteHost = "myKnxServer.myHome.com";

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// System.out is the standard output stream of our environment (here, most likely terminal)
		System.out.println("This example shows how to establish a KNX connection "
				+ "to a KNXnet/IP server.");

		try {
			System.out.println("Try connecting to " + remoteHost + " ...");

			// KNXNetworkLink is the base interface of a Calimero link to a KNX network.
			// For all the Calimero interfaces you don't know yet, check the Calimero API
			// documentation.
			final KNXNetworkLink knxLink;

			// Here, we create the IP-based link, which tries to establish a connection to the
			// remote host in its constructor. It uses KNXnet/IP tunneling.
			// The second parameter tells our link that the KNX
			// network is based on twisted-pair medium, with TP1 being the most commonly used one.
			knxLink = new KNXNetworkLinkIP(remoteHost, TPSettings.TP1);

			System.out.println("Connection to " + remoteHost + " successfully established");

			// We always make sure to close the connection after we used it
			knxLink.close();

			System.out.println("Connection got closed");
		}
		catch (final KNXException e) {

			// All checked Calimero-specific exceptions are subtypes of KNXException, just print
			// what the exception message is
			System.out.println("Error connecting to " + remoteHost + ": " + e.getMessage());
		}
		catch (final InterruptedException e) {

			// Longer tasks that might block, like connection procedures, are interruptible; in
			// such case, an instance of InterruptedException is thrown.
			// If a task got interrupted, Calimero will clean up its internal state and resources.
			// Any deviation of this behavior (e.g., if not feasible) will be noted in the
			// Calimero API documentation.
			System.out.println("Connecting to " + remoteHost + " was interrupted, quit");
		}
	}
}
