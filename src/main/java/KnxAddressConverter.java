/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2014 B. Malinowsky

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

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * Introduction to KNX addresses, and their different representations.
 * <p>
 * When working with KNX addresses, this example might help getting used to the different KNX
 * address formats. It prints the different representations of a KNX address to
 * <code>System.out</code>.
 * <p>
 * KNX addresses are either group addresses, using 2-level or 3-level formatting, or individual
 * addresses. The KNX address data structure is the same, the difference is in its representation,
 * depending on the address type.
 * <p>
 * From a terminal (or within your IDE), execute it via
 * <code>KnxAddressConverter &lt;KNX address&gt;</code>, for example,
 * <code>KnxAddressConverter 1/2/3</code>.<br>
 * The following options are supported (or just use no option to see all representations):
 * <ul>
 * <li>-g print group address representation</li>
 * <li>-i print individual address representation</li>
 * <li>-r print raw (unformatted) address</li>
 * </ul>
 * 
 * @author B. Malinowsky
 */
public class KnxAddressConverter
{
	private static String program = KnxAddressConverter.class.getName();

	public static void main(final String[] args)
	{
		if (args.length < 1) {
			printUsage();
			return;
		}

		try {
			final String opt = args[0];
			final String in = args[args.length - 1];
			if (opt.startsWith("-")) {
				if (opt.contains("g"))
					out(new GroupAddress(in));
				if (opt.contains("i"))
					out(new IndividualAddress(in));
				if (opt.contains("r"))
					out("" + KNXAddress.create(in).getRawAddress());
			}
			else {
				final GroupAddress ga = new GroupAddress(in);
				GroupAddress.setLevelPresentation(false);
				final String g2 = ga.toString();
				GroupAddress.setLevelPresentation(true);
				String type = "unknown";
				try {
					type = KNXAddress.create(in).getType();
				}
				catch (final KNXFormatException e) {}
				out("address type=" + type + ": group=" + new GroupAddress(in) + " (2-level=" + g2
						+ "), ind=" + new IndividualAddress(ga.getRawAddress()) + ", raw="
						+ ga.getRawAddress());
			}
		}
		catch (final KNXFormatException e) {
			System.err.println(program + ": " + e.getMessage());
		}
	}

	private static void printUsage()
	{
		out(program + " [-gir] <KNX group/individual/raw address>");
		out("Options:");
		out("    -gir    Convert to group/individual/raw address");
		out("            On no option, all conversions are shown");
	}

	private static void out(final Object s)
	{
		System.out.println(s);
	}
}
