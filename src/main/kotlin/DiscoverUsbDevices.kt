import tuwien.auto.calimero.serial.usb.UsbConnectionFactory
import tuwien.auto.calimero.serial.usb.Device

/**
 * This example lists all found KNX USB and USB virtual serial devices. Only devices for KNX communication are listed
 * (if Calimero is able to identify them as such). Minimum requirements are Calimero version 2.6-rc1.
 *
 * You can safely run this example, no KNX messages are sent to the KNX network.
 */

fun main() {
    println("List of KNX USB devices:\n" + list(UsbConnectionFactory.attachedKnxUsbDevices()))
}

private fun list(devices: Set<Device>) = if (devices.isEmpty()) "none found" else devices.joinToString("\n") { it.toString() }
