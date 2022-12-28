import io.calimero.link.KNXNetworkLink
import io.calimero.link.KNXNetworkLinkUsb
import io.calimero.link.medium.TPSettings

/**
 * This example shows how to establish a client network link ([KNXNetworkLink]) to a KNX TP1 network using a KNX
 * USB device. Minimum requirements are Calimero version 2.6-SNAPSHOT.
 *
 * You can safely run this example; the (established) connection is closed directly afterwards. No KNX messages are sent
 * to the KNX network.
 */

// Specify your KNX USB device; you can either use the product or manufacturer name, or the USB vendor:product ID.
private const val device = "weinzierl"

@Throws(InterruptedException::class)
fun main() {
    println("This example establishes a KNX connection using the KNX USB device '$device'")

    // Create the USB-based link. The network link uses the KNX USB communication protocol. The second argument
    // indicates that the KNX installation uses twisted-pair (TP) medium, with TP1 being most common.
    KNXNetworkLinkUsb(device, TPSettings()).use {
        println("Connection established using KNX USB device ${it.name}")
        println("Link status: connected=${it.isOpen}")
    }
}
