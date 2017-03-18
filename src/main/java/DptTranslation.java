/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2017 B. Malinowsky

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

import static tuwien.auto.calimero.dptxlator.DPTXlator2ByteFloat.DPT_TEMPERATURE;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXFormatException;
import tuwien.auto.calimero.datapoint.DatapointMap;
import tuwien.auto.calimero.datapoint.DatapointModel;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteFloat;
import tuwien.auto.calimero.dptxlator.TranslatorTypes;

/**
 * Shows several ways of translating KNX datapoint type using Calimero DPT translators.
 */
public class DptTranslation
{
	public static void main(final String[] args) throws KNXException
	{
		// our knx data (DPT 9.001) we want to translate to a java temperature value
		final byte[] data = new byte[] { 0xc, (byte) 0xe2 };

		// Approach 1: manually create a DPT translator
		manualTranslation(data);

		// Approach 2: request DPT translator using factory method and DPT
		createUsingDpt(data);

		// Approach 3: use a datapoint model with a datapoint configuration
		useDatapointModel(data);
	}

	private static void manualTranslation(final byte[] data) throws KNXFormatException
	{
		// DPT translator 9.001 for knx temperature datapoint
		final DPTXlator t = new DPTXlator2ByteFloat(DPT_TEMPERATURE);

		// translate knx data to java value
		t.setData(data);
		final double temperature = t.getNumericValue();
		final String formatted = t.getValue();
		System.out.println("temperature is " + formatted + " (" + temperature + ")");

		// set temperature value of -4 degree celsius (physical unit can be omitted)
		t.setValue("-4 \u00b0C");
		// get KNX translated data
		System.out.println(t.getValue() + " translated to knx data: 0x" + DataUnitBuilder.toHex(t.getData(), ""));
	}

	private static void createUsingDpt(final byte[] data) throws KNXException, KNXFormatException
	{
		final DPTXlator t = TranslatorTypes.createTranslator(DPT_TEMPERATURE);
		t.setData(data);
		System.out.println("temperature is " + t.getValue() + " (" + t.getNumericValue() + ")");
	}

	private static void useDatapointModel(final byte[] data) throws KNXFormatException, KNXException
	{
		// we use a map of state-based datapoints
		final DatapointModel<StateDP> datapoints = new DatapointMap<>();
		// add the required datapoints
		final GroupAddress temperature = new GroupAddress("0/0/1");
		datapoints.add(new StateDP(temperature, "my temperature", 0, DPT_TEMPERATURE.getID()));

		// now we can create a translator for datapoints (if the requested translator is supported by Calimero)
		if (datapoints.contains(temperature)) {
			final String dpt = datapoints.get(temperature).getDPT();
			final DPTXlator t = TranslatorTypes.createTranslator(0, dpt);
			t.setData(data);
			System.out.println("temperature is " + t.getValue() + " (" + t.getNumericValue() + ")");
		}
	}
}
