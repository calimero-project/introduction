/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2016, 2018 B. Malinowsky

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
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;

/**
 * This example shows how to establish a client tunneling link to a KNXnet/IP server. Minimum requirements are Calimero
 * version &ge; 2.4 and Java SE 8 compact1 profile.
 * <p>
 * You can safely run this example, the (established) connection is closed directly afterwards. No KNX messages are sent
 * to the KNX network.
 *
 * @author B. Malinowsky
 */
public class CreateTunnelingLink
{
	/**
	 * Local endpoint, replace the IP address with your actual address. The local socket address is important for
	 * multi-homed clients (several network interfaces), or if the address via InetAddress.getLocalHost is not useful.
	 */
	private static final InetSocketAddress local = new InetSocketAddress("192.168.1.10", 0);

	/**
	 * Specifies the KNXnet/IP server to access the KNX network, insert your server's actual host name or IP address,
	 * e.g., "192.168.1.20". The default port is where most servers listen on for new connection requests.
	 */
	private static final InetSocketAddress server = new InetSocketAddress("myKnxServer.myHome",
			KNXnetIPConnection.DEFAULT_PORT);

	public static void main(final String[] args)
	{
		System.out.println("This example establishes a tunneling connection to the KNXnet/IP server " + server);

		// A KNX tunneling link supports NAT (Network Address Translation) if required.
		// We also indicate that the KNX installation uses twisted-pair (TP) medium, with TP1 being the most common.
		// KNXNetworkLink is the base interface implemented by all supported Calimero links to a KNX network.
		try (KNXNetworkLink knxLink = KNXNetworkLinkIP.newTunnelingLink(local, server, false, TPSettings.TP1)) {
			System.out.println("Connection established to server " + knxLink.getName());
			System.out.println("Close connection again");
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
