import tuwien.auto.calimero.serial.usb.UsbConnection
import javax.usb.UsbDevice

/**
 * This example lists all found KNX USB and USB virtual serial devices. Only devices for KNX communication are listed
 * (if Calimero is able to identify them as such). Minimum requirements are Calimero version 2.6-SNAPSHOT.
 *
 * You can safely run this example, no KNX messages are sent to the KNX network.
 */

fun main() {
    println("List of KNX USB & USB virtual serial devices")
    println("KNX USB devices: " + list(UsbConnection.getKnxDevices()))
    println("KNX serial devices: " + list(UsbConnection.getVirtualSerialKnxDevices()))
}

private fun list(d: List<UsbDevice>) = d.joinToString(", ") { deviceInfo(it) }

// returns a short string with usb device info
private fun deviceInfo(d: UsbDevice): String {
    var description = "description n/a"
    runCatching {
        description = "${truncateAtNull(d.manufacturerString)} - ${truncateAtNull(d.productString)}"
    }
    val dd = d.usbDeviceDescriptor
    return "%s [%04x:%04x]".format(description, dd.idVendor(), dd.idProduct())
}

// necessary because usb lib does not correctly cut off C strings at NULL character
private fun truncateAtNull(s: String) = s.substringBefore(0.toChar())
