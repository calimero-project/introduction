import tuwien.auto.calimero.knxnetip.Discoverer
import java.time.Duration
import java.util.concurrent.ExecutionException

/**
 * This example shows how to discover active KNXnet/IP servers in an IP network. Minimum requirements are Calimero
 * version 2.6-rc2.
 *
 * You can safely run this example, no KNX messages are sent to the KNX network.
 */
fun main() {
    println("This example discovers all active KNXnet/IP servers in your IP network")
    try {
        // set true to be aware of Network Address Translation (NAT) during discovery
        val useNAT = false
        Discoverer.udp(useNAT).timeout(Duration.ofSeconds(3)).search().get()
            .forEach {
                val res = it.response().toString().replace(", ", "\n\t")
                println("${it.networkInterface().name} ${it.localEndpoint()} <=> $res")
            }
    } catch (e: InterruptedException) {
        System.err.println("Error during KNXnet/IP discovery: $e")
    } catch (e: ExecutionException) {
        System.err.println("Error during KNXnet/IP discovery: $e")
    }
}
