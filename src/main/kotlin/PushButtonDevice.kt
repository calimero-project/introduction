import tuwien.auto.calimero.GroupAddress
import tuwien.auto.calimero.IndividualAddress
import tuwien.auto.calimero.KNXException
import tuwien.auto.calimero.datapoint.Datapoint
import tuwien.auto.calimero.datapoint.StateDP
import tuwien.auto.calimero.device.BaseKnxDevice
import tuwien.auto.calimero.device.KnxDeviceServiceLogic
import tuwien.auto.calimero.dptxlator.DPTXlator
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean
import tuwien.auto.calimero.link.KNXNetworkLinkIP
import tuwien.auto.calimero.link.medium.KnxIPSettings
import java.net.NetworkInterface
import java.time.LocalTime

/**
 * A KNX device that acts as 2-state push button (switch) in a KNX installation. This example extends the Calimero
 * KNX device [BaseKnxDevice] and the basic implementation of a device's logic [KnxDeviceServiceLogic].
 * In our case of a push button, the state simply boils down to a boolean state value. We need to implement the
 * update/request methods for our state-based datapoint representing a read or write service from the KNX network. The
 * KNX device service logic notifies us of the relevant read/write requests addressed to our datapoint.
 *
 * This example is only concerned with KNX process communication, we will not interact with any KNX device management.
 * Note that [KnxDeviceServiceLogic] already implements plenty of KNX device management logic, too.
 *
 * This class can be run as Kotlin program directly in a terminal, and terminated by thread interruption. On the
 * terminal, a running Kotlin program is stopped by using, e.g., Control^C.
 *
 * The example uses a network link for KNX IP transmission; you can read/write the datapoint using KNXnet/IP Routing or
 * KNX IP.
 */

// Initialize some constants for this example

// The name/ID of our KNX device. It can be arbitrarily assigned by the user application, e.g.,
// a human-readable name, or some unique ID.
// It is used for KNXnet/IP device discovery, and therefore, limited to a maximum of 29 ISO 8859-1 characters.
private const val deviceName = "Push Button (KNX IP)"

// The KNX group address of our push button datapoint.
// This is the datapoint address for "switching" the button. Several KNX devices can belong to the same datapoint.
private val dpAddress = GroupAddress(1, 0, 3)

// The KNX device address of our device.
// A device's individual address is mainly used for device management (it is not used to access the datapoint to
// read or write the pushbutton state!)
private val deviceAddress = IndividualAddress(1, 1, 10)


// Creates and runs our push-button example
fun main() {
    // We need to do three things:
    // 1) initialize the push-button logic, implementing the functionality of our switch
    // 2) instantiate our KNX device
    // 3) set up a network link so our push-button can talk to the KNX network
    val logic = PushButtonDeviceLogic()

    // Every KNX device provides a datapoint model, where all the datapoints of that device are stored.
    // Add the datapoints we are concerned with to the service logic datapoint model. Subsequently, the
    // service logic will notify us about any read/write requests of those datapoints (and only those!).
    val pushButton = StateDP(dpAddress, deviceName, 0, DPTXlatorBoolean.DPT_SWITCH.id)
    logic.datapointModel.add(pushButton)

    // Create the device and the network link on which KNX messages are received from, and sent on
    // We create a KNX IP link here (hence, it will only work on a KNX IP network!)
    try {
        BaseKnxDevice(deviceName, logic).use { device ->
            KNXNetworkLinkIP.newRoutingLink(
                null as NetworkInterface?, KNXNetworkLinkIP.DefaultMulticast, KnxIPSettings(deviceAddress)
            ).use { link ->
                device.deviceLink = link

                // That's it. From here on, our KNX device provides KNX process communication services for our datapoint.
                println("$device is up and running, push-button datapoint address is $dpAddress")
                // just let the service logic sit idle and wait for messages
                while (true) Thread.sleep(1000)
            }
        }
    } catch (e: KNXException) {
        println("Running $deviceName failed: ${e.message}")
    } catch (_: InterruptedException) {
    } finally {
        println("$deviceName has left the building.")
    }
}

private class PushButtonDeviceLogic : KnxDeviceServiceLogic() {
    // Here it is: the state of our switch, i.e., the current datapoint value.
    // Because this example only uses a single datapoint of type Boolean (DPT 1.001), simply use a boolean.
    private var state = false

    override fun updateDatapointValue(ofDp: Datapoint, update: DPTXlator) {
        // This method is called wrt to a KNX process communication write indication service: update our datapoint value
        // In our example of having only a single datapoint, we know it's the push-button state
        state = (update as DPTXlatorBoolean).valueBoolean
        println("${LocalTime.now()} ${ofDp.name} switched \"${update.value}\"")
    }

    @Throws(KNXException::class)
    override fun requestDatapointValue(ofDp: Datapoint): DPTXlator {
        // This method is called if we received a KNX process communication read request: respond with our
        // current datapoint value representing the push-button state
        val t = DPTXlatorBoolean(ofDp.dpt)
        // set our current button state, the translator will translate it accordingly
        t.setValue(state)
        println("${LocalTime.now()} Respond with \"${t.value}\" to read-request for ${ofDp.name}")
        return t
    }
}
