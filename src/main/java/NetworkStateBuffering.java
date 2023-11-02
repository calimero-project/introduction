/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2016, 2023 B. Malinowsky

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
*/

import java.net.NetworkInterface;
import java.net.UnknownHostException;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.buffer.Configuration;
import tuwien.auto.calimero.buffer.NetworkBuffer;
import tuwien.auto.calimero.buffer.StateFilter;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitUnsigned;
import tuwien.auto.calimero.knxnetip.KNXnetIPRouting;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.KnxIPSettings;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;

/**
 * Example showing basic Calimero network buffering for state-based KNX datapoints and a polling task.
 */
public class NetworkStateBuffering
{
	private static final GroupAddress group = new GroupAddress(1, 0, 3);
	private static final StateDP dp = new StateDP(group, "my datapoint", 0, DPTXlator8BitUnsigned.DPT_PERCENT_U8.getID());

	public static void main(final String[] args) throws UnknownHostException, KNXException, InterruptedException
	{
		// Like always, create a network link of your choice
		try (KNXNetworkLink link = KNXNetworkLinkIP.newRoutingLink((NetworkInterface) null,
				KNXnetIPRouting.DefaultMulticast, new KnxIPSettings(new IndividualAddress(1, 2, 3)));
		     // setup Calimero network buffer
		     NetworkBuffer nb = NetworkBuffer.createBuffer("my-networkbuffer")) {

			final Configuration config = nb.addConfiguration(link);
			// create a filter, here we use a default filter for state-based requests
			final StateFilter f = new StateFilter();
			config.setFilter(null, f);
			config.activate(true);
			// The buffered link will interact with the network buffer on each .req/.con/.ind
			final KNXNetworkLink bufferedLink = config.getBufferedLink();

			// Poll the buffered link
			try (ProcessCommunicatorImpl pc = new ProcessCommunicatorImpl(bufferedLink)) {
				// in the best case, the following loop will generate no traffic at all on the KNX network
				// in the worst case, the following loop will generate only 1 actual read on the KNX network
				for (int i = 0; i < 100; i++)
					System.out.println("datapoint state value = " + pc.readNumeric(dp));
			}
		}
	}
}
