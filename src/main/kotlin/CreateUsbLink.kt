import tuwien.auto.calimero.link.KNXNetworkLink
import tuwien.auto.calimero.link.KNXNetworkLinkUsb
import tuwien.auto.calimero.link.LinkEvent
import tuwien.auto.calimero.link.NetworkLinkListener
import tuwien.auto.calimero.link.medium.TPSettings
import tuwien.auto.calimero.serial.ConnectionStatus

/**
 * This example shows how to establish a client network link ([KNXNetworkLink]) to a KNX TP1 network using a KNX
 * USB device. Minimum requirements are Calimero version 3.0-SNAPSHOT.
 *
 * You can safely run this example; the established connection is closed 10 seconds after creation.
 * No KNX messages are sent to the KNX network.
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
        // Add a listener with a link event which notifies us in case the USB interface to KNX connection got disrupted.
        // (Note, this is not the connection-state of the USB network link attached to this host itself.)
        it.addLinkListener(object : NetworkLinkListener {
            @LinkEvent
            fun status(status: ConnectionStatus) { println("KNX connection status: $status") }
        })

        Thread.sleep(10_000)
    }
}
