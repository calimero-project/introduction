/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2015, 2016 B. Malinowsky

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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.midlet.MIDlet;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.Settings;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlatorDate;
import tuwien.auto.calimero.dptxlator.DPTXlatorTime;
import tuwien.auto.calimero.dptxlator.TranslatorTypes;
import tuwien.auto.calimero.internal.EndpointAddress;
import tuwien.auto.calimero.internal.JavaME;
import tuwien.auto.calimero.knxnetip.Discoverer;
import tuwien.auto.calimero.knxnetip.Discoverer.Result;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.mgmt.PropertyClient;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.xml.XmlInputFactory;
import tuwien.auto.calimero.xml.XmlReader;

/**
 * A simple example of a midlet that executes some KNX tasks on Java ME Embedded 8 platforms. The midlet accesses system
 * resources required to run Calimero. It does not modify any system settings, but attempts to read from KNX group
 * address 1/0/3, and write to it. Note that not all tasks might complete successfully.
 *
 * @author B. Malinowsky
 */
public class CalimeroMidlet extends MIDlet
{
	private static final org.slf4j.Logger l = org.slf4j.LoggerFactory.getLogger("calimero.midlet");

	private static final GroupAddress boolDp = new GroupAddress(1, 0, 3);
	// address will be updated by KNXnet/IP discovery
	private static EndpointAddress server = EndpointAddress.of("192.168.10.12:3676");

	@Override
	public void startApp()
	{
		clockreading();

		showInfo();

		if (!isGcfImplemented()) {
			l.error("Calimero requires the Generic Connection Framework (GCF)");
			destroyApp(true);
			return;
		}

		discoverKnxnetIpDevices();
		readViaRouting(boolDp);
		readWriteViaTunneling(server, boolDp);

		testTranslators();
		accessJarResource();
		readXml();
		accessFileSystem();

		destroyApp(true);
	}

	@Override
	public void destroyApp(final boolean unconditional)
	{
		l.info("Bye!");
		notifyDestroyed();
	}

	// Provides a rough way to compare the performance of different devices
	private void clockreading()
	{
		double d = 0;
		final int samples = 10000;
		for (int i = 0; i < samples; i++) {
			final long now = System.nanoTime();
			final long diff = System.nanoTime() - now;
			d += diff;
		}
		d /= samples;
		l.debug("reading nano-time takes " + d + " ns, " + d / 1000 + " us (" + samples + " samples)");
	}

	private static boolean isGcfImplemented()
	{
		return System.getProperty("microedition.io.gcf") != null;
	}

	private void showInfo()
	{
		l.info("\n{}\n{}", Settings.getLibraryHeader(true), Settings.getBundleListing());
	}

	private void discoverKnxnetIpDevices()
	{
		try {
			// multicasting is probably not supported, so request unicast response
			final Discoverer d = new Discoverer(null, 0, true, false);
			d.startSearch(3, true);
			final List<Result<SearchResponse>> found = d.getSearchResponses();
			for (final Result<SearchResponse> sr : found) {
				server = sr.getAddress();
				l.info("{}", sr.getAddress());
				l.info("{}", sr.getResponse());
			}
		}
		catch (KNXException | RuntimeException | InterruptedException e) {
			l.error(e.getMessage(), e);
		}
	}

	private void readViaRouting(final GroupAddress datapoint)
	{
		l.info("");
		l.info("Try KNXnet/IP routing");
		try (KNXNetworkLink link = new KNXNetworkLinkIP(null, null, TPSettings.TP1)) {
			final ProcessCommunicator pc = new ProcessCommunicatorImpl(link);
			final boolean b = pc.readBool(datapoint);
			l.info("read from {}: {}", datapoint, b);
			pc.detach();
		}
		catch (final KNXException | RuntimeException | InterruptedException e) {
			l.error(e.getMessage(), e);
		}
	}

	private void readWriteViaTunneling(final EndpointAddress connect, final GroupAddress datapoint)
	{
		l.info("");
		l.info("Try KNXnet/IP tunneling");
		try (KNXNetworkLink link = new KNXNetworkLinkIP(KNXNetworkLinkIP.TUNNELING, null, connect, true,
				TPSettings.TP1)) {
			final ProcessCommunicator pc = new ProcessCommunicatorImpl(link);

			l.info("read from {}: {}", datapoint, pc.readBool(datapoint));
			Thread.sleep(1000);
			final boolean value = true;
			l.info("write to {}: {}", datapoint, value);
			pc.write(datapoint, value);
			Thread.sleep(1000);
			l.info("read from {}: {}", datapoint, pc.readBool(datapoint));

			pc.detach();
		}
		catch (final KNXException | RuntimeException | InterruptedException e) {
			l.error(e.getMessage(), e);
		}
	}

	private void testTranslators()
	{
		l.info("");
		l.info("Test DPT translators");
		try {
			final DPTXlator t = TranslatorTypes.createTranslator(0, "1.001");
			t.setData(new byte[] { 1 });
			l.trace("DPT bool translation = {}", t.getValue());
			l.trace("DPT bool translation = {}", t.getNumericValue());
		}
		catch (final KNXException e) {
			l.error("translators", e);
		}

		// test formatters of date and time xlator
		try {
			final DPTXlatorDate t = new DPTXlatorDate(DPTXlatorDate.DPT_DATE);
			DPTXlatorDate.useValueFormat("%tD");
			t.setValue(2015, 4, 14);
			l.trace("DPT date translation = {}", t.getValue());
		}
		catch (final KNXException e) {
			l.error("translators", e);
		}
		try {
			final DPTXlatorTime t = new DPTXlatorTime(DPTXlatorTime.DPT_TIMEOFDAY);
			DPTXlatorDate.useValueFormat("%d,%d,%d");
			t.setValue(3, 15, 16, 17);
			l.trace("DPT time translation = {}", t.getValue());
		}
		catch (final KNXException e) {
			l.error("translators", e);
		}
	}

	private void accessJarResource()
	{
		l.info("");
		l.info("Read file as stream from jar resource");
		final String file = "/config/properties.xml";
		try (InputStream is = getClass().getResourceAsStream(file)) {
			l.info("created input stream from resource");
		}
		catch (IOException | RuntimeException e) {
			l.error("error", e);
		}
	}

	private void readXml()
	{
		final String file = "/config/properties.xml";

		// TODO loading from jar resource is not supported by Calimero

		l.info("");
		l.info("Read XML - load KNX property definitions");
		try {
			PropertyClient.loadDefinitions(file, null);
			l.info("definitions loaded");
		}
		catch (KNXException | RuntimeException e) {
			l.error("error ", e);
		}

		l.info("");
		l.info("Read XML - create XML reader with file from jar resource");
		try {
			final XmlReader r = XmlInputFactory.getInstance().createXMLReader(file);
			l.debug("reader created");
			r.close();
		}
		catch (final RuntimeException e) {
			l.error("error", e);
		}

		try (XmlReader r = XmlInputFactory.getInstance()
				.createXMLStreamReader(new ByteArrayInputStream(new byte[100]))) {
			l.debug("got an XML stream reader instance");
		}
		catch (final RuntimeException e) {
			l.error("error", e);
		}

		try (InputStream is = getClass().getResourceAsStream(file)) {
			l.debug("got resource as input stream");
			try (XmlReader r = XmlInputFactory.getInstance().createXMLStreamReader(is)) {

				// with something like this I have to replace the current way of loading resources
//				Collection<Property> c = PropertyClient.getResouceHandler().load(r);
				l.debug("KNX properties loaded");
			}
		}
		catch (IOException | RuntimeException e) {
			l.error("error", e);
		}
	}

	private void accessFileSystem()
	{
		l.info("");
		l.info("Access file system");
		String root = null;
		try {
			final Enumeration<String> roots = FileSystemRegistry.listRoots();
			l.debug("Mounted file systems:");
			while (roots.hasMoreElements()) {
				final String r = roots.nextElement();
				l.debug(r);
				// any guess as good as the other, just take the first we find
				if (root == null)
					root = r;
			}
		}
		catch (final Exception e) {
			l.error("error", e);
		}

		l.info("");
		l.info("Test file connection");
		final String dir = "file://localhost/" + root;
		try (FileConnection fc = (FileConnection) Connector.open(dir, Connector.READ)) {
			l.trace("files under " + dir);
			final Enumeration<String> list = fc.list();
			while (list.hasMoreElements()) {
				final String s = list.nextElement();
				l.trace(s);
			}
		}
		catch (final Exception e) {
			l.error("error", e);
		}

		l.info("");
		l.info("Test file connection on raspberry pi");
		final String raspDir = root + "pi/test.txt";
		try (InputStream is = JavaME.newFileInputStream(raspDir)) {
			l.debug(new BufferedReader(new InputStreamReader(is)).readLine());
		}
		catch (final Exception e) {
			l.error("error", e);
		}
	}
}
