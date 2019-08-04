/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2015, 2019 B. Malinowsky

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
import java.time.LocalTime;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXFormatException;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.device.BaseKnxDevice;
import tuwien.auto.calimero.device.KnxDeviceServiceLogic;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
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
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;
import tuwien.auto.calimero.link.medium.KnxIPSettings;

/**
 * A KNX IP device that acts as 2-state push button (switch) in a KNX installation. This example extends upon the
 * {@link PushButtonDevice} example and supports KNX IP discovery {@literal &} self description.
 * <p>
 * This example is only concerned with KNX process communication, it will not show any KNX device management. Note that
 * {@link KnxDeviceServiceLogic} already implements most of the KNX device management logic, too.
 * <p>
 * This class can be run as Java program directly in a terminal, and terminated by thread interruption. On the terminal,
 * a running Java program is stopped by using, e.g., Control^C.
 */
public class PushButtonDeviceWithDiscovery extends KnxDeviceServiceLogic
{
	// The name/ID of our KNX device.
	// Used for KNXnet/IP device discovery, and therefore, limited to a maximum of 29 ISO 8859-1 characters.
	private static final String deviceName = "Push Button (KNX IP)";

	// The initial KNX device address of our device. A device's individual address is mainly used for device management
	// (it is not used to access the datapoint to read or write the pushbutton state!).
	private static final IndividualAddress deviceAddress = new IndividualAddress(1, 1, 10);

	// The KNX group address of our push button datapoint, used for "switching" the button.
	private static final GroupAddress dpAddress = new GroupAddress(1, 0, 1);

	// The state of our switch, i.e., the current datapoint value
	private boolean state;

	@Override
	public void updateDatapointValue(final Datapoint ofDp, final DPTXlator update)
	{
		// This method is called if we received a KNX process communication write indication: update our datapoint value
		// In our example of having only a single datapoint, we know it's the pushbutton state
		state = ((DPTXlatorBoolean) update).getValueBoolean();
		System.out.println(LocalTime.now() + " " + ofDp.getName() + " switched \"" + update.getValue() + "\"");
	}

	@Override
	public DPTXlator requestDatapointValue(final Datapoint ofDp) throws KNXException
	{
		// This method is called if we received a KNX process communication read request: respond with our
		// current datapoint value representing the push-button state
		final DPTXlatorBoolean t = new DPTXlatorBoolean(ofDp.getDPT());
		// set our current button state, the translator will translate it accordingly
		t.setValue(state);

		System.out.println(LocalTime.now() + " Respond with \"" + t.getValue() + "\" for " + ofDp.getName());
		return t;
	}

	// We override the KNXnet/IP routing implementation to filter and respond to search/description requests
	private static class DeviceRouting extends KNXnetIPRouting
	{
		DeviceRouting() throws KNXException
		{
			super(null, null);
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
			if (endpoint.getHostProtocol() != HPAI.IPV4_UDP) {
				logger.warn("KNX IP has protocol support for UDP/IP only");
				return true;
			}

			// prepare the info we want to return for search/description responses
			final NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
			final byte[] mac = ni != null ? ni.getHardwareAddress() : null;
			final InetAddress mcast = InetAddress.getByName(KNXnetIPRouting.DEFAULT_MULTICAST);
			final DeviceDIB device = new DeviceDIB(deviceName, 0, 0, KNXMediumSettings.MEDIUM_KNXIP, deviceAddress,
					new byte[6], mcast, mac != null ? mac : new byte[6]);
			final ServiceFamiliesDIB svcFamilies = new ServiceFamiliesDIB(new int[] { ServiceFamiliesDIB.CORE },
					new int[] { 1 });

			final byte[] buf;
			if (svc == SEARCH_REQ) {
				final HPAI ctrlEndpoint = new HPAI((InetAddress) null, ctrlEndpt.getPort());
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

	// Creates and runs our push-button example
	public static void main(final String[] args)
	{
		// We need to do three things:
		// 1) initialize the push-button logic (this class), implementing the functionality of our switch
		// 2) instantiate our KNX device
		// 3) setup a network link so our push-button can talk to the KNX network

		final PushButtonDeviceWithDiscovery logic = new PushButtonDeviceWithDiscovery();
		// Every KNX device has a datapoint model, where all the datapoints of that device are stored.
		// Add the datapoints we are concerned with to the service logic datapoint model. Subsequently, the
		// service logic will notify us about any read/write requests of those datapoints (and only those!).
		final StateDP pushButton = new StateDP(dpAddress, deviceName, 0, DPTXlatorBoolean.DPT_SWITCH.getID());
		logic.getDatapointModel().add(pushButton);

		final BaseKnxDevice device = new BaseKnxDevice(deviceName, logic);

		// Create the KNX IP network link on which KNX messages are received from, and sent on
		try (DeviceRouting routing = new DeviceRouting();
		       KNXNetworkLink link = new KNXNetworkLinkIP(2, routing, new KnxIPSettings(deviceAddress)) {}) {
			device.setDeviceLink(link);

			// That's it. From here on, our KNX device provides KNXnet/IP discovery & description, and KNX process
			// communication services for our datapoint.
			System.out.println(device + " is up and running, push-button address is " + dpAddress);
			// just let the service logic sit idle and wait for messages
			while (true) Thread.sleep(1000);
		}
		catch (final KNXException e) {
			System.out.println("Initializing link of " + deviceName + " failed: " + e.getMessage());
			e.printStackTrace();
		}
		catch (final InterruptedException e) {}
		finally {
			System.out.println(deviceName + " has left the building.");
		}
	}
}
