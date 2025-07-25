/*
    Calimero 3 - A library for KNX network access
    Copyright (c) 2018, 2025 B. Malinowsky

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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.HexFormat;

import io.calimero.FrameEvent;
import io.calimero.KNXException;
import io.calimero.link.KNXNetworkLinkIP;
import io.calimero.link.NetworkLinkListener;
import io.calimero.link.medium.TPSettings;

/**
 * This example shows how to establish a secure client routing link using KNX IP Secure. Minimum requirements are
 * Calimero version &ge; 3.0-SNAPSHOT and Java 21 (module java.base).
 * <p>
 * You can safely run this example, the (established) connection listens to incoming frames and is closed without
 * sending KNX messages to the KNX network.
 */
public class KnxipSecure {
	/**
	 * Replace with local IP address or hostname. The local IP is used to select the network interface, important with
	 * multi-homed clients (several network interfaces).
	 */
	private static final String local = "192.168.10.10";

	/** Address of the routing multicast group to join, by default {@link KNXNetworkLinkIP#DefaultMulticast}. */
	private static final InetAddress multicastGroup = KNXNetworkLinkIP.DefaultMulticast;

	/** Insert here your KNX IP Secure group key (backbone key). */
	private static final byte[] groupKey = HexFormat.of().parseHex("85A0723F8C58A33333E4B6B7037C4F18");


	public static void main(final String[] args) throws SocketException, UnknownHostException {
		final var duration = Duration.ofSeconds(60);
		System.out.println("This example establishes a secure routing link for multicast group "
				+ multicastGroup.getHostAddress() + ", and waits for secure routing packets for "
				+ duration.toSeconds() + " seconds");

		// Find the local network interface by IP address
		final NetworkInterface netif = NetworkInterface.getByInetAddress(InetAddress.getByName(local));

		// Our KNX installation uses twisted-pair (TP) 1 medium
		try (var link = KNXNetworkLinkIP.newSecureRoutingLink(netif, multicastGroup, groupKey, Duration.ofMillis(1000),
				new TPSettings())) {

			link.addLinkListener(new NetworkLinkListener() {
				@Override
				public void indication(final FrameEvent e) { System.out.println(e.getFrame()); }
			});

			System.out.println("Secure link established for " + link.getName());
			Thread.sleep(duration.toMillis());
		}
		catch (KNXException | InterruptedException e) {
			System.out.println("Error creating KNX IP secure routing link: " + e);
		}
		finally {
			System.out.println("Link closed");
		}
	}
}
