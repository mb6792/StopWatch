package marathon.stopwatch.serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;

import marathon.stopwatch.logging.Logger;

public class Reader {
	
	/** A mapping from names to CommPortIdentifiers. */
	protected HashMap<String, CommPortIdentifier> map = new HashMap<String, CommPortIdentifier>();
	/** The name of the choice the user made. */
	protected String selectedPortName;
	/** The CommPortIdentifier the user chose. */
	protected CommPortIdentifier selectedPortIdentifier;
	/** The SerialPort object */
	protected SerialPort ttya;
	
	/** How long to wait for the open to finish up. */
	public final int TIMEOUTSECONDS = 30;
	/** The input stream */
	protected InputStream is;
	/** The output stream */
	protected PrintStream os;
	/** The chosen Port Identifier */
	CommPortIdentifier thePortID;
	/** The chosen Port itself */
	CommPort thePort;
	
	PacketHandler packetHandler;
	
	public void open(String portIdentifier, String baudRate) throws PortInUseException,UnsupportedCommOperationException, IOException {
		
		this.thePortID = this.map.get(portIdentifier);
		thePort = thePortID.open("DarwinSys DataComm", TIMEOUTSECONDS * 1000);
		SerialPort myPort = (SerialPort) thePort;
		// set up the serial port
		myPort.setSerialPortParams(Integer.parseInt(baudRate.trim()), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

		try {
			is = thePort.getInputStream();//is = new BufferedReader(new InputStreamReader(thePort.getInputStream()));
		} catch (IOException e) {
			System.err.println("Can't open input stream: write-only");
			is = null;
		}
		
		packetHandler = new PacketHandler();		
	}

	public void read() throws IOException {
		
		int i = 0;

		System.out.println("Port Open. Now Reading...");
		
		while(true){
			if((i = is.read())!= -1){
				Logger.logByte((byte)(i & 0x00FF));
				//System.out.print(String.format("%02X ", i));
				this.packetHandler.handlePacketByte((byte)i);
			}
		}
	}
	
	public void getAvailablePorts(){
		
		@SuppressWarnings("rawtypes")
		Enumeration pList = CommPortIdentifier.getPortIdentifiers();

		// Process the list, putting serial and parallel into ComboBoxes
		while (pList.hasMoreElements()) {
			CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
			if(cpi.getPortType() == CommPortIdentifier.PORT_SERIAL){
				map.put(cpi.getName(), cpi);
			}
		}
	}
}
