/*
    Calimero 3 - A library for KNX network access
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

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import io.calimero.knxnetip.Discoverer;

/**
 * This example shows how to discover active KNXnet/IP servers in an IP network. Minimum requirements are Calimero
 * version 3.0-SNAPSHOT and Java 21 (java.base).
 * <p>
 * You can safely run this example, no KNX messages are sent to the KNX network.
 */
public class DiscoverKnxServers {
	public static void main(final String[] args) {
		System.out.println("This example discovers all active KNXnet/IP servers in your IP network");

		try {
			// set true to be aware of Network Address Translation (NAT) during discovery
			final boolean useNAT = false;
			Discoverer.udp(useNAT).timeout(Duration.ofSeconds(3)).search().get().forEach(r ->
					System.out.format("%s %s <=> %s%n",
							r.networkInterface().getName(),
							r.localEndpoint(),
							r.response().toString().replace(", ", "\n\t")));
		}
		catch (InterruptedException | ExecutionException e) {
			System.err.println("Error during KNXnet/IP discovery: " + e);
		}
	}
}
