/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2016, 2018 B. Malinowsky

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

import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.knxnetip.Discoverer;

/**
 * This example shows how to discover active KNXnet/IP servers in an IP network. Minimum requirements are Calimero
 * version &ge; 2.4 and Java SE 8 compact1 profile.
 * <p>
 * You can safely run this example, no KNX messages are sent to the KNX network.
 *
 * @author B. Malinowsky
 */
public class DiscoverKnxServers
{
	public static void main(final String[] args)
	{
		System.out.println("This example discovers all active KNXnet/IP servers in your IP network");

		try {
			// set true to be aware of Network Address Translation (NAT) during discovery
			final boolean useNAT = false;
			// request multicast responses (as opposed to unicast responses) from discovered servers
			final boolean requestMulticastResponse = true;

			final Discoverer discoverer = new Discoverer(null, 0, useNAT, requestMulticastResponse);
			discoverer.startSearch(3, true);

			// print out responding servers
			discoverer.getSearchResponses().forEach(r -> System.out.format("%s %s <=> %s%n",
					r.getNetworkInterface().getName(),
					r.getAddress(),
					r.getResponse().toString().replace(", ", "\n\t")));
		}
		catch (KNXException | InterruptedException e) {
			// KNXException: all Calimero-specific checked exceptions are subtypes of KNXException
			// InterruptedException: longer tasks that might block are interruptible, e.g., server search.
			System.out.println("Error during KNXnet/IP discovery: " + e);
		}
	}
}
