Introduction to Calimero
========================

This repository contains additional documentation for Calimero and code examples.

### Examples:

* [Create a KNX link (using default settings)](examples/CreateKnxLink.java)
* [Create a KNX link (with more options)](examples/CreateKnxLink2.java)
* [Process communication (read/write datapoints)](examples/ProcessCommunication.java)
* [Use logging output](examples/Logging.java)
* [KNX address converter](examples/KnxAddressConverter.java)

### Examples requiring Java 8 and Calimero version 2.4 (or later):

* [Discover KNXnet/IP servers](src/main/java/DiscoverKnxServers.java)
* [Discover KNX USB devices](src/main/java/DiscoverUsbDevices.java)
* [Create client-side KNXnet/IP tunneling network link](src/main/java/CreateTunnelingLink.java)
* [Create client-side KNX USB network link](src/main/java/CreateUsbLink.java)
* [Create TPUART network monitor link](src/main/java/CreateTpuartMonitor.java)

#### KNX device

* [2-state push button device](examples/java8/PushButtonActuator.java)

  - Run the example in your IDE. Or, from the terminal (put the required `jar` dependencies in the introduction directory: calimero-core, calimero-device, slf4j-api, slf4j-simple):

 ~~~ sh
	# Compile the actuator Java file
	$ javac -cp "./*" examples/java8/PushButtonActuator.java 
	# Start the actuator
	$ java -cp "./*:examples/java8/" PushButtonActuator
 ~~~
 
  - Use process communication to read/write the push button state. For example, start the Calimero Group Monitor tool in a second terminal

 ~~~ sh
	$ mvn exec:java -Dexec.args="groupmon 224.0.23.12 --routing"
 ~~~
 and enter the following commands:
 
 ~~~ sh	
	read 1/0/3 switch
	[response should be printed with switch state off]
	write 1/0/3 on
	r 1/0/3
	[response should printed with switch state on]
	Ctrl^C
 ~~~

  - Read device information. For example, using the Calimero Device Info tool
 
 ~~~ sh 
	$ mvn exec:java -Dexec.args="devinfo --routing 224.0.23.12 1.1.10"
 ~~~

### Java ME Embedded 8 Example:

* [Java ME Embedded 8 Midlet](examples/midlet/)
