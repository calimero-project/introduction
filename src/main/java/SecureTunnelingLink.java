/*
    Calimero 3 - A library for KNX network access
    Copyright (c) 2023, 2023 B. Malinowsky

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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Duration;

import io.calimero.FrameEvent;
import io.calimero.KNXException;
import io.calimero.knxnetip.KNXnetIPConnection;
import io.calimero.knxnetip.SecureConnection;
import io.calimero.knxnetip.TcpConnection;
import io.calimero.link.KNXNetworkLinkIP;
import io.calimero.link.NetworkLinkListener;
import io.calimero.link.medium.TPSettings;

/**
 * This example shows how to establish a secure client tunneling link using KNX IP Secure. Minimum requirements are
 * Calimero version &ge; 3.0-M1 and Java SE 17 (module java.base).
 * <p>
 * You can safely run this example, the (established) connection listens to incoming frames and is closed without
 * sending KNX messages to the KNX network.
 */
public class SecureTunnelingLink {
	/**
	 * Specifies the KNXnet/IP server to access the KNX network, insert your server's actual host name or IP address,
	 * e.g., "192.168.1.20". The default port is where most servers listen for new connection requests.
	 */
	private static final InetSocketAddress server = new InetSocketAddress("myKnxServer.myHome",
			KNXnetIPConnection.DEFAULT_PORT);

	/** User to authenticate for the secure session with the server, 0 < user < 128 */
	private static final int user = 2;
	/** User password for the specified user */
	private static final String userPwd = "my-user-pwd";
	/** Device authentication password of the server */
	private static final String deviceAuthPwd = "dev-pwd";


	public static void main(final String[] args) throws SocketException, UnknownHostException {
		final var duration = Duration.ofSeconds(60);
		System.out.println("This example establishes a secure tunneling link to "
				+ server + ", and waits for tunneled datagrams for "
				+ duration.toSeconds() + " seconds");

		final byte[] userKey = SecureConnection.hashUserPassword(userPwd.toCharArray());
		final byte[] deviceAuthCode = SecureConnection.hashDeviceAuthenticationPassword(deviceAuthPwd.toCharArray());

		try (	var tcp = TcpConnection.newTcpConnection(server);
				var session = tcp.newSecureSession(user, userKey, deviceAuthCode);
				var link = KNXNetworkLinkIP.newSecureTunnelingLink(session, new TPSettings())) {

			link.addLinkListener(new NetworkLinkListener() {
				@Override
				public void indication(final FrameEvent e) { System.out.println(e.getFrame()); }
			});

			System.out.println("Secure link established to " + link.getName());
			Thread.sleep(duration.toMillis());
		}
		catch (KNXException | InterruptedException e) {
			System.out.println("Error creating KNX IP secure tunneling link: " + e);
		}
		finally {
			System.out.println("Link closed");
		}
	}
}
