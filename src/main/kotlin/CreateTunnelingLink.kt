import io.calimero.KNXException
import io.calimero.knxnetip.KNXnetIPConnection
import io.calimero.link.KNXNetworkLinkIP
import io.calimero.link.medium.TPSettings
import java.net.InetSocketAddress

/**
 * This example shows how to establish a client tunneling link to a KNXnet/IP server. Minimum requirements are Calimero
 * version 2.6-SNAPSHOT.
 *
 * You can safely run this example, the (established) connection is closed directly afterwards. No KNX messages are sent
 * to the KNX network.
 */

/**
 * Local endpoint, The local socket address is important for
 * multi-homed clients (several network interfaces), or if the default route is not useful.
 */
private val local = InetSocketAddress(0)

/**
 * Specifies the KNXnet/IP server to access the KNX network, insert your server's actual host name or IP address,
 * e.g., "192.168.1.20". The default port is where most servers listen for new connection requests.
 */
private val server = InetSocketAddress("192.168.1.10", KNXnetIPConnection.DEFAULT_PORT)


fun main() {
    println("Establish a tunneling connection to the KNXnet/IP server $server")

    // KNXNetworkLink is the base interface of a Calimero link to a KNX network. Here, we create an IP-based link,
    // which supports NAT (Network Address Translation) if required.
    // We also indicate that the KNX installation uses twisted-pair (TP1) medium.
    try {
        KNXNetworkLinkIP.newTunnelingLink(local, server, false, TPSettings())
            .use { println("Connection established to server ${it.name}") }
    } catch (e: KNXException) {
        // KNXException: all Calimero-specific checked exceptions are subtypes of KNXException
        println("Error creating KNXnet/IP tunneling link: $e")
    } catch (e: InterruptedException) {
        // InterruptedException: longer tasks that might block are interruptible, e.g., connection procedures. In
        // such case, an instance of InterruptedException is thrown.
        // If a task got interrupted, Calimero will clean up its internal state and resources accordingly.
        // Any deviation of such behavior, e.g., where not feasible, is documented in the Calimero API.
        println("Error creating KNXnet/IP tunneling link: $e")
    }
}
