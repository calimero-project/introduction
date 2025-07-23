import io.calimero.DetachEvent
import io.calimero.link.KNXNetworkLinkUsb
import io.calimero.link.medium.TPSettings
import io.calimero.process.ProcessCommunicatorImpl
import io.calimero.process.ProcessEvent
import io.calimero.process.ProcessListener
import io.calimero.secure.Keyring
import io.calimero.secure.Security
import java.time.LocalTime
import java.util.HexFormat

/**
 * This example shows how to use KNX Data Secure process communication (using a KNX USB device link). For communicating
 * with a single KNX installation, providing the necessary keys to `Security.defaultInstallation()` is sufficient
 * for any subsequent secure communication.
 *
 * Because a client application is usually not configured as part of a KNX Data Secure installation, it is
 * not recognized as KNX Data Secure device. Therefore, secure process communication relies on Group Object Diagnostics.
 *
 * Minimum requirements are Calimero version 3.0-SNAPSHOT and Java 21 (java.base).
 *
 * You can safely run this example; the established connection is closed 10 seconds after creation.
 * No KNX messages are sent to the KNX network.
 */

// Specify your KNX USB device; either use the product or manufacturer name, or the USB vendor:product ID
private const val device = "weinzierl"

// URI pointing to a keyring with key information for KNX Secure
private const val keyringUri = "my-keys.knxkeys"
private val keyringPwd = "keyring-pwd".toCharArray()


fun main() {
    println("Establish KNX Data Secure process communication using the KNX USB device '$device'")

    // Provide the keyring to use by default for KNX Data Secure
    Security.defaultInstallation().useKeyring(Keyring.load(keyringUri), keyringPwd)

    KNXNetworkLinkUsb(device, TPSettings()).use {
        ProcessCommunicatorImpl(it).use { pc ->
            // Add a process listener which prints (decrypted) process events
            pc.addProcessListener(object : ProcessListener {
                override fun groupWrite(e: ProcessEvent) = print("write.ind", e)
                override fun groupReadRequest(e: ProcessEvent) = print("read.req", e)
                override fun groupReadResponse(e: ProcessEvent) = print("read.res", e)
                override fun detached(e: DetachEvent) {}
            })

            println("KNX Data Secure is ready")

            // Writing datapoint values will use KNX Secure if required by the used keyring
            // pc.write(...);
            Thread.sleep(10_000)
        }
    }
}

private fun print(svc: String, pe: ProcessEvent) = try {
    println("${LocalTime.now()} ${pe.sourceAddr}->${pe.destination} $svc: ${HexFormat.of().formatHex(pe.asdu)}")
} catch (rte: RuntimeException) {
    System.err.println(rte)
}
