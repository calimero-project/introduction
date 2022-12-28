/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2015, 2021 B. Malinowsky

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    Linking this library statically or dynamically with other modules is
    making a combined work based on this library. Thus, the terms and
    conditions of the GNU General Public License cover the whole
    combination.

    As a special exception, the copyright holders of this library give you
    permission to link this library with independent modules to produce an
    executable, regardless of the license terms of these independent
    modules, and to copy and distribute the resulting executable under terms
    of your choice, provided that you also meet, for each linked independent
    module, the terms and conditions of the license of that module. An
    independent module is a module which is not derived from or based on
    this library. If you modify this library, you may extend this exception
    to your version of the library, but you are not obligated to do so. If
    you do not wish to do so, delete this exception statement from your
    version.
*/

import java.io.IOException;
import java.net.NetworkInterface;
import java.nio.file.Path;

import io.calimero.DataUnitBuilder;
import io.calimero.DeviceDescriptor.DD0;
import io.calimero.IndividualAddress;
import io.calimero.KNXException;
import io.calimero.SerialNumber;
import io.calimero.datapoint.Datapoint;
import io.calimero.device.BaseKnxDevice;
import io.calimero.device.KnxDeviceServiceLogic;
import io.calimero.device.ios.SecurityObject;
import io.calimero.device.ios.SecurityObject.Pid;
import io.calimero.dptxlator.DPTXlator;
import io.calimero.link.KNXNetworkLinkIP;
import io.calimero.link.medium.KnxIPSettings;

/**
 * A programmable device, using KNX IP to communicate with other KNX endpoints. This example extends
 * {@link KnxDeviceServiceLogic} and supports KNX IP discovery {@literal &} self description.
 * <p>
 * This example is about programming via ETS and does not implement KNX process communication.
 * <p>
 * This class can be run as Java program directly in a terminal, and terminated by thread interruption or entering an
 * arbitray character that is read by {@link System.in}.
 */
public class ProgrammableDevice extends KnxDeviceServiceLogic {
	// The name/ID of our KNX device.
	// Used for KNXnet/IP device discovery, and therefore, limited to a maximum of 29 ISO 8859-1 characters.
	private static final String deviceName = "Programmable Device (KNX IP)";

	// Name of the network interface to use for KNX IP Routing
	private static final String networkInterface = "eth0";

	// Set true to adjust device identication for programming and activate programming mode, false otherwise
	private static final boolean prepareForProgramming = true;

	// The initial KNX device address of our device. A device's individual address is mainly used for device management
	private static final IndividualAddress deviceAddress = prepareForProgramming
			? new IndividualAddress(15, 15, 255) : new IndividualAddress(0);


	// Runs the programmable device
	public static void main(final String[] args) throws KNXException, IOException {
		// We need to do three things:
		// 1) initialize the device logic (this class), which implements the mgmt functionality of our device
		// 2) instantiate our KNX device
		// 3) setup a network link using IP routing, so our device can talk to the KNX network

		final ProgrammableDevice logic = new ProgrammableDevice();

		// Specify storage of device's interface object server (IOS). A device will initialize it's IOS from it if the
		// resource exists; otherwise, the resource is created during closing the device.
		// If you program a secure device, you might want to use a non-empty password for file encryption in BaseKnxDevice constructor.
		final var iosResource = Path.of(".", "src", "main", "resources", "device.xml").toAbsolutePath().normalize();

		final var ipSettings = new KnxIPSettings(deviceAddress);
		try (var device = new BaseKnxDevice(deviceName, logic, iosResource.toUri(), new char[0]);
				var link = KNXNetworkLinkIP.newRoutingLink(NetworkInterface.getByName(networkInterface),
						KNXNetworkLinkIP.DefaultMulticast, ipSettings)) {

			if (prepareForProgramming) {
				// prepare identification usually required for ETS download
				// unused parts can have arbitrary values
				final var deviceDescriptor = DD0.TYPE_0701;
				final int manufacturerId = 0x04;
				final var serialNumber = SerialNumber.from(DataUnitBuilder.fromHex("000a1c112913")); // 6 bytes
				final byte[] hardwareType = DataUnitBuilder.fromHex("000000000223"); // 6 bytes
				final byte[] programVersion = new byte[] { 0, 4, 0, 0, 0 }; // 5 bytes
				// a valid FDSK is only required for secure device download
				final byte[] fdsk = DataUnitBuilder.fromHex("35cb5a25771daf18d52d9ef7e39a2799"); // 16 bytes

				device.identification(deviceDescriptor, manufacturerId, serialNumber, hardwareType, programVersion, fdsk);

				logic.setProgrammingMode(true);
				// device is now ready for programming
			}

			device.setDeviceLink(link);
			System.out.println(device + " is up, programming mode = " + logic.inProgrammingMode());
			// type any character to stop the device, and save the IOS
			while (true) {
				if (System.in.read() != 0)
					break;
			}
		}
		catch (final KNXException e) {
			System.err.println("Initializing link of " + deviceName + " failed: " + e.getMessage());
		}
		finally {
			System.out.println(deviceName + " has left the building.");
		}
	}

	@Override
	public void updateDatapointValue(final Datapoint ofDp, final DPTXlator update) {}

	@Override
	public DPTXlator requestDatapointValue(final Datapoint ofDp) throws KNXException { return null; }
}
