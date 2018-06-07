/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2015, 2018 B. Malinowsky

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

import java.net.NetworkInterface;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.device.BaseKnxDevice;
import tuwien.auto.calimero.device.KnxDeviceServiceLogic;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.KnxIPSettings;

/**
 * A KNX device that acts as 2-state push button (switch) in a KNX installation. This example extends the Calimero
 * KNX device {@link BaseKnxDevice} and the basic implementation of a device's logic {@link KnxDeviceServiceLogic}.
 * In our case of a push button, the state simply boils down to a boolean state value. We need to implement the
 * update/request methods for our state-based datapoint representing a read or write service from the KNX network. The
 * KNX device service logic notifies us of the relevant read/write requests addressed to our datapoint.
 * <p>
 * This example is only concerned with KNX process communication, we will not interact with any KNX device management.
 * Note that {@link KnxDeviceServiceLogic} already implements plenty of KNX device management logic, too.
 * <p>
 * This class can be run as Java program directly in a terminal, and terminated by thread interruption. On the terminal,
 * a running Java program is stopped by using, e.g., Control^C.
 * <p>
 * The example uses a network link for KNX IP transmission; you can read/write the datapoint using KNXnet/IP Routing or
 * KNX IP.
 */
public class PushButtonDevice extends KnxDeviceServiceLogic
{
	// Initialize some constants for this example

	// The name/ID of our KNX device. It can be arbitrarily assigned by the user application, e.g.,
	// a human readable name, or some unique ID like "building:1/floor:1/room:3/switch:2".
	private static final String deviceName = "push-button";

	// The KNX device address of our device.
	// A device's individual address is mainly used for device management (it is not used to access the datapoint to
	// read or write the pushbutton state!)
	private static final IndividualAddress deviceAddress = new IndividualAddress(1, 1, 10);

	// The KNX group address of our push button datapoint.
	// This is the datapoint address for "switching" the button. Several KNX devices can belong to the same datapoint.
	private static final GroupAddress dpAddress = new GroupAddress(1, 0, 3);


	// Here it is: the state of our switch, i.e., the current datapoint value.
	// Because this example only uses a single datapoint of type Boolean (DPT 1.001), simply use a boolean.
	private boolean state;

	// Creates and runs our push-button example
	public static void main(final String[] args)
	{
		// We need to do three things:
		// 1) initialize the push-button logic (this class), implementing the functionality of our switch
		// 2) instantiate our KNX device
		// 3) setup a network link so our push-button can talk to the KNX network

		final PushButtonDevice logic = new PushButtonDevice();
		// Every KNX device provides a datapoint model, where all the datapoints of that device are stored.
		// Add the datapoints we are concerned with to the service logic datapoint model. Subsequently, the
		// service logic will notify us about any read/write requests of those datapoints (and only those!).
		final StateDP pushButton = new StateDP(dpAddress, deviceName, 0, DPTXlatorBoolean.DPT_SWITCH.getID());
		logic.getDatapointModel().add(pushButton);

		final BaseKnxDevice device = new BaseKnxDevice(deviceName, logic);

		// Create the network link on which KNX messages are received from, and sent on
		// We create a KNX IP link here (hence, it will only work on a KNX IP network!)
		try (KNXNetworkLink link = KNXNetworkLinkIP.newRoutingLink((NetworkInterface) null, null,
				new KnxIPSettings(deviceAddress))) {
			device.setDeviceLink(link);

			// That's it. From here on, our KNX device provides KNX process communication services for our datapoint.
			System.out.println(device + " is up and running, push-button datapoint address is " + dpAddress);
			// just let the service logic sit idle and wait for messages
			while (true) Thread.sleep(1000);
		}
		catch (final KNXException e) {
			System.out.println("Running " + deviceName + " failed: " + e.getMessage());
			e.printStackTrace();
		}
		catch (final InterruptedException e) {}
		finally {
			System.out.println(deviceName + " has left the building.");
		}
	}

	@Override
	public void updateDatapointValue(final Datapoint ofDp, final DPTXlator update)
	{
		// This method is called wrt to a KNX process communication write indication service: update our datapoint value
		// In our example of having only a single datapoint, we know it's the pushbutton state
		state = ((DPTXlatorBoolean) update).getValueBoolean();
		System.out.println(ofDp.getName() + " datapoint value got updated to \"" + update.getValue() + "\"");
	}

	@Override
	public DPTXlator requestDatapointValue(final Datapoint ofDp) throws KNXException
	{
		// This method is called wrt a KNX process communication read request service: respond with our
		// current datapoint value representing the push-button state
		final DPTXlatorBoolean t = new DPTXlatorBoolean(ofDp.getDPT());
		// set our current button state, the translator will translate it accordingly
		t.setValue(state);

		System.out.println("Respond with \"" + t.getValue() + "\" to read-request for " + ofDp.getName());
		return t;
	}
}
