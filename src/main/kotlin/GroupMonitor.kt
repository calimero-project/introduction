import io.calimero.DetachEvent
import io.calimero.link.KNXNetworkLinkIP
import io.calimero.link.medium.TPSettings
import io.calimero.process.ProcessCommunicatorImpl
import io.calimero.process.ProcessEvent
import io.calimero.process.ProcessListener
import java.net.InetSocketAddress
import java.time.LocalTime
import java.util.HexFormat

/**
 * Example code showing how to use KNX process communication for group monitoring on a KNX Twisted Pair 1 (TP1) network.
 * On receiving group notifications, the KNX source and destination address are printed to System.out, as well as any
 * data part of the application service data unit (ASDU) in hexadecimal format.
 *
 * Note that this example does not exit, i.e., it monitors forever or until the KNX network link connection got
 * closed. Hence, with KNX servers that have a limit on active tunneling connections (usually 1 or 4), if the group
 * monitor in connected state is terminated by the client (you), the pending state of the open tunnel on the KNX server
 * might temporarily cause an error on subsequent connection attempts.
 */

// Address of your KNXnet/IP server. Replace the host or IP address as necessary.
private const val remoteHost = "192.168.1.10"

fun main() {
    val remote = InetSocketAddress(remoteHost, 3671)
    KNXNetworkLinkIP.newTunnelingLink(InetSocketAddress(0), remote, false, TPSettings())
        .use {
            ProcessCommunicatorImpl(it).use { pc ->
                // start listening to group notifications using a process listener
                pc.addProcessListener(MonitorListener())
                println("Monitoring KNX network using KNXnet/IP server $remoteHost ...")
                while (it.isOpen) Thread.sleep(1000)
            }
        }
}

private class MonitorListener : ProcessListener {
    override fun groupWrite(e: ProcessEvent) = print("write.ind", e)
    override fun groupReadRequest(e: ProcessEvent) = print("read.req", e)
    override fun groupReadResponse(e: ProcessEvent) = print("read.res", e)
    override fun detached(e: DetachEvent) = Unit
}

// Called on every group notification issued by a datapoint on the KNX network. It prints the service primitive,
// KNX source and destination address, and Application Service Data Unit (ASDU) to System.out.
private fun print(svc: String, e: ProcessEvent) {
    try {
        println("${LocalTime.now()} ${e.sourceAddr}->${e.destination} $svc: ${HexFormat.of().formatHex(e.asdu)}")
    } catch (ex: RuntimeException) {
        System.err.println(ex)
    }
}
