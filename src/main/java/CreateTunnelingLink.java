/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2016, 2021 B. Malinowsky

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

import java.net.InetSocketAddress;

import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;

/**
 * This example shows how to establish a client tunneling link to a KNXnet/IP server. Minimum requirements are Calimero
 * version 2.6-SNAPSHOT and Java SE 11 (java.base).
 * <p>
 * You can safely run this example, the (established) connection is closed directly afterwards. No KNX messages are sent
 * to the KNX network.
 */
public class CreateTunnelingLink {
	/**
	 * Local endpoint, The local socket address is important for
	 * multi-homed clients (several network interfaces), or if the default route is not useful.
	 */
	private static final InetSocketAddress local = new InetSocketAddress(0);

	/**
	 * Specifies the KNXnet/IP server to access the KNX network, insert your server's actual host name or IP address,
	 * e.g., "192.168.1.20". The default port is where most servers listen for new connection requests.
	 */
	private static final InetSocketAddress server = new InetSocketAddress("myKnxServer.myHome",
			KNXnetIPConnection.DEFAULT_PORT);

	public static void main(final String[] args) {
		System.out.println("Establish a tunneling connection to the KNXnet/IP server " + server);

		// KNXNetworkLink is the base interface of a Calimero link to a KNX network. Here, we create an IP-based link,
		// which supports NAT (Network Address Translation) if required.
		// We also indicate that the KNX installation uses twisted-pair (TP1) medium.
		try (var knxLink = KNXNetworkLinkIP.newTunnelingLink(local, server, false, new TPSettings())) {

			System.out.println("Connection established to server " + knxLink.getName());

		}
		catch (KNXException | InterruptedException e) {
			// KNXException: all Calimero-specific checked exceptions are subtypes of KNXException

			// InterruptedException: longer tasks that might block are interruptible, e.g., connection procedures. In
			// such case, an instance of InterruptedException is thrown.
			// If a task got interrupted, Calimero will clean up its internal state and resources accordingly.
			// Any deviation of such behavior, e.g., where not feasible, is documented in the Calimero API.

			System.out.println("Error creating KNXnet/IP tunneling link: " + e);
		}
	}
}
