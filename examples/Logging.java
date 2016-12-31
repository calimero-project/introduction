/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2013, 2016 B. Malinowsky

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

import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.knxnetip.Discoverer;
import tuwien.auto.calimero.log.LogLevel;
import tuwien.auto.calimero.log.LogManager;
import tuwien.auto.calimero.log.LogService;
import tuwien.auto.calimero.log.LogStreamWriter;
import tuwien.auto.calimero.log.LogWriter;

/**
 * This tutorial shows how to use basic logging functionality with Calimero.
 * <p>
 *
 * @author B. Malinowsky
 */
public class Logging
{
	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// Let's ask for the log service used by the KNXnet/IP discovery and description
		// Discoverer.LOG_SERVICE is of type String and holds the name used by the discovery log service
		final LogService discovererLog = LogManager.getManager().getLogService(
				Discoverer.LOG_SERVICE);

		// Internally, the Calimero library will also use something like the
		// following to request a log service, using a specific identifier. Since the "myLogger"
		// log service does not exist by now, it will be created
		final LogService myLogger = LogManager.getManager().getLogService("myLogger");

		// Request a different log service for some other functionality
		final LogService anotherLogger = LogManager.getManager().getLogService("anotherLogger");

		// Print all the log service names currently known to the log manager
		System.out.println("Log services currently known to the log manager:");
		final String[] loggerNames = LogManager.getManager().getAllLogServices();
		for (int i = 0; i < loggerNames.length; i++) {
			final String s = loggerNames[i];
			System.out.println(s);
		}

		// Anyone interested in some log output can use the same mechanism as the library to request
		// a specific log service
		// Here, we obtain access to the same logger as above
		final LogService logger = LogManager.getManager().getLogService("myLogger");

		System.out.println("\nNow, create 1 log writer for warnings and 1 unformatted "
				+ "log writer for all output");

		// Now, we create a writer that actually outputs the log information we obtain from that
		// specific log service. In this example, we write to the standard output.
		// You can also create a LogFileWriter for file output, a LogNetWriter to write to a
		// network destination, or extend your own Writer for some other purpose
		final LogWriter stdOut = new LogStreamWriter(LogLevel.WARN, System.out, true, false);
		logger.addWriter(stdOut);

		// A simple way is to register a log writer to all log services currently known and created
		// in the future, i.e., that logs everything. Let's do this, creating a writer using no
		// output formatting
		final LogWriter all = LogStreamWriter.newUnformatted(LogLevel.ALL, System.out, true, false);
		LogManager.getManager().addWriter("", all);

		// Now lets log some messages. You should see some of the messages twice, and also notice
		// their different format (formatted vs. unformatted)

		myLogger.warn("This message got logged as warning");
		myLogger.info("This message got logged as INFO, you should only see it once");
		myLogger.log(LogLevel.ALWAYS, "This message is unconditionally logged", null);

		// Now let's run discoverer and see what its log service produces
		anotherLogger.log(LogLevel.ALWAYS,
				"\nStart KNXnet/IP discovery and see what its log service produces:", null);

		try {
			new Discoverer(0, false).startSearch(3, true);
		}
		catch (final KNXException e) {
			e.printStackTrace();
		}
		catch (final InterruptedException e) {
			e.printStackTrace();
		}

		// Since we are not interested in discoverer output, we can also remove the writer
		discovererLog.removeWriter(stdOut);

		// Logging happens asynchronously; therefore, if our application shuts down, tell the log
		// manager to write out all remaining pending messages. Optionally, you also can request
		// that all writers registered with the manager get closed.
		LogManager.getManager().shutdown(true);
	}
}
