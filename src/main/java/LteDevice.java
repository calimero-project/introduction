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
import java.util.Arrays;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.LteHeeTag;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.device.BaseKnxDevice;
import tuwien.auto.calimero.device.KnxDeviceServiceLogic;
import tuwien.auto.calimero.device.LteProcessEvent;
import tuwien.auto.calimero.device.ServiceResult;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteFloat;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.KnxIPSettings;
import tuwien.auto.calimero.process.ProcessEvent;

/**
 * An LTE device, for KNX LTE-HEE runtime communication over KNX IP.
 * This example extends {@link KnxDeviceServiceLogic} with the capability to send and respond to LTE-HEE messages,
 * and supports KNX IP discovery {@literal &} self description.
 * <p>
 * This class can be run as Java program directly in a terminal, and terminated by thread interruption or entering an
 * arbitray character that is read by {@link System.in}.
 */
public class LteDevice extends KnxDeviceServiceLogic {
	// The name/ID of our KNX device.
	// Used for KNXnet/IP device discovery, and therefore, limited to a maximum of 29 ISO 8859-1 characters.
	private static final String deviceName = "LTE Device (KNX IP)";

	// Name of the network interface to use for KNX IP Routing
	private static final String networkInterface = "en0";

	// The initial KNX device address of our device. A device's individual address is mainly used for device management
	private static final IndividualAddress deviceAddress = new IndividualAddress(1, 2, 3);


	private static final int GroupPropResponse = 0b1111101001;
	private static final int GroupPropWrite = 0b1111101010;
	private static final int GroupPropInfo = 0b1111101011;


	// Runs the LTE device
	public static void main(final String[] args) throws KNXException, IOException {
		// We need to do three things:
		// 1) initialize the device logic (this class)
		// 2) instantiate our KNX device
		// 3) setup a network link using IP routing, so our device can talk to the KNX network

		final var logic = new LteDevice();

		final var ipSettings = new KnxIPSettings(deviceAddress);
		try (var device = new BaseKnxDevice(deviceName, logic);
				var link = KNXNetworkLinkIP.newRoutingLink(NetworkInterface.getByName(networkInterface),
						KNXNetworkLinkIP.DefaultMulticast, ipSettings)) {

			// initialize interface object with a property for LTE communication
			final var ios = device.getInterfaceObjectServer();
			final int iot = 127; // interface object type
			final int oi = 1; // object instance
			final int pid = 150; // property ID
			ios.addInterfaceObject(iot);

			final var xlator = new DPTXlator2ByteFloat(DPTXlator2ByteFloat.DPT_TEMPERATURE);
			xlator.setValue(20.3);
			ios.setProperty(iot, oi, pid, 1, 1, xlator.getData());


			device.setDeviceLink(link);
			System.out.println(device + " is up");
			// every 10 seconds, send group property info with the specified property
			final var tag = LteHeeTag.geoTag(1, 1, 0);
			try {
				while (true) {
					Thread.sleep(10_000);
					logic.sendLteHee(GroupPropInfo, tag, iot, oi, pid);
				}
			}
			catch (final InterruptedException e) {}
		}
		catch (final KNXException e) {
			System.err.println("Initializing link of " + deviceName + " failed: " + e.getMessage());
		}
		finally {
			System.out.println(deviceName + " has left the building.");
		}
	}

	@Override
	public ServiceResult<byte[]> groupReadRequest(final ProcessEvent e) {
		if (e instanceof LteProcessEvent)
			return lteGroupReadRequest((LteProcessEvent) e);
		return super.groupReadRequest(e);
	}

	private ServiceResult<byte[]> lteGroupReadRequest(final LteProcessEvent lteEvent) {
		final var tag = LteHeeTag.from(lteEvent.extFrameFormat() , lteEvent.getDestination());

		final byte[] asdu = lteEvent.getASDU();
		final int iot = ((asdu[0] & 0xff) << 8) | (asdu[1] & 0xff);
		// object instance is not known by sender and has no meaning, should be always 0 (wildcard)
		final int oi = asdu[2] & 0xff;
		final int pid = asdu[3] & 0xff;
		if (pid == 0xff) {
			final int companyCode = ((asdu[4] & 0xff) << 8) | (asdu[5] & 0xff);
			final int privatePid = asdu[6] & 0xff;
			System.out.println(tag + " IOT " + iot + " OI " + oi + " company " + companyCode + " PID " + privatePid + ": "
					+ DataUnitBuilder.toHex(Arrays.copyOfRange(asdu, 7, asdu.length), ""));
		}
		else
			System.out.println(tag + " IOT " + iot + " OI " + oi + " PID " + pid + ": "
					+ DataUnitBuilder.toHex(Arrays.copyOfRange(asdu, 4, asdu.length), ""));

		final var sr = new ServiceResult<byte[]>() {
			@Override
			public void run() {
				// TODO response addr and object instance might differ depending on property we look up
				sendLteHee(GroupPropResponse, tag, iot, oi, pid);
			}
		};

		return sr;
	}

	@Override
	public void updateDatapointValue(final Datapoint ofDp, final DPTXlator update) {}

	@Override
	public DPTXlator requestDatapointValue(final Datapoint ofDp) throws KNXException { return null; }
}
