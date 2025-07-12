/*
    Calimero 3 - A library for KNX network access
    Copyright (c) 2024, 2024 B. Malinowsky

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

import java.io.IOException;
import java.nio.file.Path;

import io.calimero.KNXException;
import io.calimero.knxnetip.UnixDomainSocketConnection;
import io.calimero.link.KNXNetworkLinkIP;
import io.calimero.link.medium.TPSettings;

/**
 * This example shows how to establish a client tunneling link to a KNXnet/IP server supporting Unix Domain Sockets
 * (UDS). This type of connection is only applicable for a KNXnet/IP _software_ server which runs on the same host and
 * supports connections over Unix domain sockets.
 * Minimum requirements are Calimero version 3.0-M1 and Java SE 17 (java.base).
 * <p>
 * You can safely run this example, the (established) connection is closed directly afterwards. No KNX messages are sent
 * to the KNX network.
 */
public class CreateUnixDomainSocketLink {
	/**
	 * Specifies the path to the Unix domain socket of the KNXnet/IP server.
	 */
	private static final Path socketPath = Path.of("/tmp/calimero/unix/.socket");

	public static void main(final String[] args) {
		System.out.println("Establish a UDS tunneling connection to the KNXnet/IP server socket " + socketPath);

		// KNXNetworkLink is the base interface of a Calimero link to a KNX network. Here, we create a UDS-based link.
		// We also indicate that the KNX installation uses twisted-pair (TP1) medium.
		try (var udsConnection = UnixDomainSocketConnection.newConnection(socketPath);
		     var knxLink = KNXNetworkLinkIP.newTunnelingLink(udsConnection, new TPSettings())) {

			System.out.println("Connection established to server " + knxLink.getName());
		}
		catch (KNXException | InterruptedException | IOException e) {
			// KNXException: all Calimero-specific checked exceptions are subtypes of KNXException

			// InterruptedException: longer tasks that might block are interruptible, e.g., connection procedures. In
			// such case, an instance of InterruptedException is thrown.
			// If a task got interrupted, Calimero will clean up its internal state and resources accordingly.
			// Any deviation of such behavior, e.g., where not feasible, is documented in the Calimero API.
			System.out.println("Error creating KNXnet/IP tunneling link: " + e);
		}
	}
}
