/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2024, 2025 B. Malinowsky

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

import io.calimero.FrameEvent
import io.calimero.KNXException
import io.calimero.knxnetip.KNXnetIPConnection
import io.calimero.knxnetip.SecureConnection
import io.calimero.knxnetip.TcpConnection
import io.calimero.link.KNXNetworkLinkIP
import io.calimero.link.NetworkLinkListener
import io.calimero.link.medium.TPSettings
import java.net.InetSocketAddress
import java.time.Duration

/**
 * This example shows how to establish a secure client tunneling link using KNX IP Secure. Minimum requirements are
 * Calimero version 3.0-SNAPSHOT and Java SE 17 (module java.base).
 *
 * You can safely run this example, the (established) connection listens to incoming frames and is closed without
 * sending KNX messages to the KNX network.
 */

/**
 * Specifies the KNXnet/IP server to access the KNX network, insert your server's actual host name or IP address,
 * e.g., "192.168.1.20". The default port is where most servers listen for new connection requests.
 */
private val server = InetSocketAddress("myKnxServer.myHome", KNXnetIPConnection.DEFAULT_PORT)

/** User to authenticate for the secure session with the server, 0 < user < 128  */
private const val user = 2

/** User password for the specified user  */
private const val userPwd = "my-user-pwd"

/** Device authentication password of the server  */
private const val deviceAuthPwd = "dev-pwd"

fun main() {
	val duration = Duration.ofSeconds(60)
	println("This example establishes a secure tunneling link to $server, " +
			"and waits for tunneled datagrams for ${duration.toSeconds()} seconds")

	val userKey = SecureConnection.hashUserPassword(userPwd.toCharArray())
	val deviceAuthCode = SecureConnection.hashDeviceAuthenticationPassword(deviceAuthPwd.toCharArray())

	try {
		TcpConnection.newTcpConnection(server).use { tcp ->
			tcp.newSecureSession(user, userKey, deviceAuthCode).use { session ->
				KNXNetworkLinkIP.newSecureTunnelingLink(session, TPSettings()).use { link ->
					link.addLinkListener(object : NetworkLinkListener {
						override fun indication(e: FrameEvent) = println(e.frame)
					})
					println("Secure link established to ${link.name}")
					Thread.sleep(duration.toMillis())
				}
			}
		}
	} catch (e: KNXException) { println("Error creating KNX IP secure tunneling link: $e")
	} catch (_: InterruptedException) { println("Interrupted")
	} finally { println("Link closed") }
}
