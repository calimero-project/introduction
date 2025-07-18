import io.calimero.GroupAddress
import io.calimero.KNXException
import io.calimero.link.KNXNetworkLinkIP
import io.calimero.link.medium.TPSettings
import io.calimero.process.ProcessCommunicatorImpl
import java.net.InetSocketAddress

/**
 * Example of Calimero process communication, we read (and write) a boolean datapoint in the KNX network. By default,
 * this example will not change any datapoint value in the network.
 */

// Address of your KNXnet/IP server. Replace the IP host or address as necessary.
private const val remoteHost = "192.168.1.10"

// We will read a boolean from the KNX datapoint with this group address, replace the address as necessary.
// Make sure this datapoint exists, otherwise you will get a read timeout!
private const val group = "1/0/2"

fun main() {
    val anyLocal = InetSocketAddress(0)
    val remote = InetSocketAddress(remoteHost, 3671)
    // Create our network link, and pass it to a process communicator
    try {
        KNXNetworkLinkIP.newTunnelingLink(anyLocal, remote, false, TPSettings()).use {
            ProcessCommunicatorImpl(it).use { pc ->
                println("read boolean value from datapoint $group")
                val value = pc.readBool(GroupAddress(group))
                println("datapoint $group value = $value")
            }
        }
    } catch (e: KNXException) {
        println("Error accessing KNX datapoint: ${e.message}")
    } catch (_: InterruptedException) {
        println("Interrupted")
    }
}
