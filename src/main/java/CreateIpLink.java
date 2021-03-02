/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2013, 2021 B. Malinowsky

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
 * This example shows how to establish a client link to a KNXnet/IP server with KNX default UDP port settings.
 * The (established) link is closed directly afterwards; no KNX messages are sent to the KNX network.
 * This allows you to safely run this example.
 */
public class CreateIpLink {
	// Specifies the KNX server, either as host name or IP address, used for access to the KNX network. Replace the
	// string with an existing host/address.
	private static final String remoteHost = "192.168.1.20";

	/** @param args */
	public static void main(final String[] args) {
		System.out.println("Establish a KNX IP tunneling connection to a KNXnet/IP server");

		// KNXNetworkLink is the base interface of a Calimero link to a KNX network. Here, we create an IP-based link,
		// which establishes a connection to an remote IP host. It uses KNXnet/IP tunneling.
		// TPSettings tells our link that the KNX network is based on twisted-pair medium (TP1).
		try (var knxLink = KNXNetworkLinkIP.newTunnelingLink(new InetSocketAddress(0),
				new InetSocketAddress(remoteHost, KNXnetIPConnection.DEFAULT_PORT), false, new TPSettings())) {

			System.out.println("Connection to " + remoteHost + " successfully established");

		}
		catch (final KNXException e) {
			// All checked Calimero-specific exceptions are subtypes of KNXException
			System.out.println("Error connecting to " + remoteHost + ": " + e.getMessage());
		}
		catch (final InterruptedException e) {
			// Longer tasks (like connection procedures) that might block are interruptible; in
			// such case, an instance of InterruptedException is thrown.
			// If a task got interrupted, Calimero will clean up its internal state and resources.
			// Any deviation of this behavior will be noted in the Calimero API documentation.
			System.out.println("Connecting to " + remoteHost + " was interrupted, quit");
		}
	}
}
