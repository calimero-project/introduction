/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2015, 2020 B. Malinowsky

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

import static tuwien.auto.calimero.knxnetip.servicetype.KNXnetIPHeader.DESCRIPTION_REQ;
import static tuwien.auto.calimero.knxnetip.servicetype.KNXnetIPHeader.SEARCH_REQ;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.DeviceDescriptor.DD0;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXFormatException;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.device.BaseKnxDevice;
import tuwien.auto.calimero.device.KnxDeviceServiceLogic;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.knxnetip.KNXnetIPRouting;
import tuwien.auto.calimero.knxnetip.servicetype.DescriptionRequest;
import tuwien.auto.calimero.knxnetip.servicetype.DescriptionResponse;
import tuwien.auto.calimero.knxnetip.servicetype.KNXnetIPHeader;
import tuwien.auto.calimero.knxnetip.servicetype.PacketHelper;
import tuwien.auto.calimero.knxnetip.servicetype.SearchRequest;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;
import tuwien.auto.calimero.knxnetip.util.DeviceDIB;
import tuwien.auto.calimero.knxnetip.util.HPAI;
import tuwien.auto.calimero.knxnetip.util.ServiceFamiliesDIB;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;
import tuwien.auto.calimero.link.medium.KnxIPSettings;

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

	private static boolean prepareForProgramming = true;

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
		final var iosResource = Path.of(".", "src", "main", "resources", "interfaceObjects.xml").toAbsolutePath().normalize();

		try (var device = new BaseKnxDevice(deviceName, logic, iosResource.toUri(), new char[0]);
			var routing = new DeviceRouting();
			var link = new KNXNetworkLinkIP(2, routing, new KnxIPSettings(deviceAddress)) {}) {

			if (prepareForProgramming) {
				// prepare identification usually required for ETS download
				// unused parts can have arbitrary values
				final var deviceDescriptor = DD0.TYPE_0701;
				final int manufacturerId = 0x04;
				final byte[] serialNumber = DataUnitBuilder.fromHex("000a1c112913"); // 6 bytes
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
		catch (final InterruptedException e) {}
		finally {
			System.out.println(deviceName + " has left the building.");
		}
	}

	@Override
	public void updateDatapointValue(final Datapoint ofDp, final DPTXlator update) {}

	@Override
	public DPTXlator requestDatapointValue(final Datapoint ofDp) throws KNXException { return null; }


	// We override the KNXnet/IP routing implementation to filter and respond to KNXnet/IP search/description requests
	private static class DeviceRouting extends KNXnetIPRouting {
		// TODO how to init secure routing?
		DeviceRouting() throws KNXException, SocketException {
			super(KNXnetIPRouting.DefaultMulticast);
			init(NetworkInterface.getByName("en0"), false, true);
		}

		@Override
		protected boolean handleServiceType(final KNXnetIPHeader h, final byte[] data, final int offset,
			final InetAddress src, final int port) throws KNXFormatException, IOException
		{
			final int svc = h.getServiceType();
			if (svc != SEARCH_REQ && svc != DESCRIPTION_REQ)
				return super.handleServiceType(h, data, offset, src, port);
			if (!supportedVersion(h))
				return true;
			final HPAI endpoint = svc == SEARCH_REQ ? new SearchRequest(data, offset).getEndpoint()
					: new DescriptionRequest(data, offset).getEndpoint();
			if (endpoint.getHostProtocol() != HPAI.IPV4_UDP)
				return true;

			// prepare the info we want to return for search/description responses
			final InetAddress localHost = InetAddress.getLocalHost();
			final NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
			final byte[] mac = ni != null ? ni.getHardwareAddress() : null;
			final InetAddress mcast = InetAddress.getByName(KNXnetIPRouting.DEFAULT_MULTICAST);
			final DeviceDIB device = new DeviceDIB(deviceName, 0, 0, KNXMediumSettings.MEDIUM_KNXIP, deviceAddress,
					new byte[6], mcast, mac != null ? mac : new byte[6]);
			final ServiceFamiliesDIB svcFamilies = new ServiceFamiliesDIB(new int[] { ServiceFamiliesDIB.CORE },
					new int[] { 1 });

			final byte[] buf;
			if (svc == SEARCH_REQ) {
				final HPAI ctrlEndpoint = new HPAI(localHost, ctrlEndpt.getPort());
				buf = PacketHelper.toPacket(new SearchResponse(ctrlEndpoint, device, svcFamilies));
			}
			else
				buf = PacketHelper.toPacket(new DescriptionResponse(device, svcFamilies));

			socket.send(new DatagramPacket(buf, buf.length, createResponseAddress(endpoint, src, port)));
			return true;
		}

		private InetSocketAddress createResponseAddress(final HPAI endpoint, final InetAddress senderHost,
			final int senderPort)
		{
			// NAT: if the data EP is incomplete or left empty, we fall back to the IP address and port of the sender.
			if (endpoint.getAddress().isAnyLocalAddress() || endpoint.getPort() == 0)
				return new InetSocketAddress(senderHost, senderPort);
			return new InetSocketAddress(endpoint.getAddress(), endpoint.getPort());
		}
	}
}
