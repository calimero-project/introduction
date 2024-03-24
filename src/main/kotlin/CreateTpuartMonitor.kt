import tuwien.auto.calimero.FrameEvent
import tuwien.auto.calimero.KNXException
import tuwien.auto.calimero.link.KNXNetworkMonitor
import tuwien.auto.calimero.link.KNXNetworkMonitorTpuart
import tuwien.auto.calimero.link.LinkListener
import tuwien.auto.calimero.link.MonitorFrameEvent

/**
 * This example shows how to establish a client network monitor ([KNXNetworkMonitor]) to a KNX TP1 network using
 * TP-UART serial communication. Minimum requirements are Calimero version 2.6-rc1.
 *
 * You can safely run this example; the (established) monitor connection is completely passive. No KNX messages are sent
 * to the KNX network. The network monitor will run for 10 seconds to let you monitor some KNX frames.
 */

/** Specify the serial port of your KNX TP-UART device. */
private const val portId = "/dev/ttyACM0"

@Throws(KNXException::class, InterruptedException::class)
fun main() {
    println("This example establishes a KNX monitor connection using TP-UART on port '$portId'")
    KNXNetworkMonitorTpuart(portId, true).use { knxMonitor ->
        println("Connection established")

        // add listener to notify us of any indication, and provide us with the decoded raw frame
        knxMonitor.addMonitorListener(object : LinkListener {
            override fun indication(e: FrameEvent) = println("${e.frame}: ${(e as MonitorFrameEvent).rawFrame}")
        })

        // let's wait some seconds to monitor KNX frames
        Thread.sleep(10_000)
    }
}
