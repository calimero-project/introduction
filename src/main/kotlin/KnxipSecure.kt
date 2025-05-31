import tuwien.auto.calimero.FrameEvent
import tuwien.auto.calimero.KNXException
import tuwien.auto.calimero.link.KNXNetworkLinkIP
import tuwien.auto.calimero.link.NetworkLinkListener
import tuwien.auto.calimero.link.medium.TPSettings
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.net.UnknownHostException
import java.time.Duration
import java.util.HexFormat

/**
 * This example shows how to establish a secure client routing link using KNX IP Secure. Minimum requirements are
 * Calimero version 2.6.
 *
 * You can safely run this example, the (established) connection listens to incoming frames and is closed without
 * sending KNX messages to the KNX network.
 */


/**
 * Replace with local IP address or hostname. The local IP is used to select the network interface, important with
 * multi-homed clients (several network interfaces).
 */
private const val local = "192.168.1.10"

/** Address of the routing multicast group to join, by default [KNXNetworkLinkIP.DefaultMulticast].  */
private val multicastGroup = KNXNetworkLinkIP.DefaultMulticast

/** Insert here your KNX IP Secure group key (backbone key).  */
private val groupKey = HexFormat.of().parseHex("85A0723F8C58A33333E4B6B7037C4F18")


@Throws(SocketException::class, UnknownHostException::class)
fun main() {
    val duration = Duration.ofSeconds(60)
    println(
        "This example establishes a secure routing link for multicast group ${multicastGroup.hostAddress}, " +
                "and waits for secure routing packets for ${duration.toSeconds()} seconds"
    )

    // Find the local network interface by IP address
    val netif = NetworkInterface.getByInetAddress(InetAddress.getByName(local))

    try {
        // Our KNX installation uses twisted-pair (TP) 1 medium
        KNXNetworkLinkIP.newSecureRoutingLink(
            netif, multicastGroup, groupKey, Duration.ofMillis(1000),
            TPSettings()
        ).use { link ->
            link.addLinkListener(object : NetworkLinkListener {
                override fun indication(e: FrameEvent) = println(e.frame)
            })
            println("Secure link established for ${link.name}")
            Thread.sleep(duration.toMillis())
        }
    } catch (e: KNXException) {
        println("Error creating KNX IP secure routing link: $e")
    } catch (e: InterruptedException) {
        println("Error creating KNX IP secure routing link: $e")
    } finally {
        println("Link closed")
    }
}
