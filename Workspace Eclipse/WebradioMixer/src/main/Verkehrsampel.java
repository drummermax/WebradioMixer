package main;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Enumeration;

import audio.MixingDesk;
import gnu.io.*;
import main.Mairlist.MairlistPFLSource;
import main.Mairlist.MairlistPlayerState;
import midi.MidiController;
import midi.MidiController.MIDIKey;

public class Verkehrsampel {
	private static Verkehrsampel instance;
	
	private CommPortIdentifier serialPortId;
	private Enumeration<CommPortIdentifier> enumComm;
	private SerialPort serialPort;
	private OutputStream outputStream;
	private boolean serialPortGeoeffnet = false;

	private int baudrate = 9600;
	private int dataBits = SerialPort.DATABITS_8;
	private int stopBits = SerialPort.STOPBITS_1;
	private int parity = SerialPort.PARITY_NONE;
	private String portName = "COM5";
	
	private int brightness_red = 0, brightness_yellow = 0, brightness_green = 0;
	private static long timestampEOF = 0;

	public long getCurrentTimestamp() {
		return System.currentTimeMillis();
	}

	public static Verkehrsampel getInstance() {
		if (instance == null) {
			instance = new Verkehrsampel();
		}
		return instance;
	}
	
	private Verkehrsampel() {
		System.setProperty("java.library.path",
				System.getProperty("java.library.path") + ";" + System.getProperty("user.dir") + "\\extlib");
		Field fieldSysPath = null;

		try {
			fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		
		fieldSysPath.setAccessible(true);
		
		try {
			fieldSysPath.set(null, null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public String[] getPortNames() {
		CommPortIdentifier serialPortIdCount;
	    Enumeration<CommPortIdentifier> enumCommCount;
	    
	    int counter = 0;

	    enumCommCount = CommPortIdentifier.getPortIdentifiers();
	    while (enumCommCount.hasMoreElements()) {
	    	serialPortIdCount = enumCommCount.nextElement();
	     	if(serialPortIdCount.getPortType() == CommPortIdentifier.PORT_SERIAL) {
	    		counter++;
	    	}
	    }
	    
	    if (counter == 0) {
	    	String[] nullString = new String[1];
	    	nullString[0] = "";
	    	return nullString;
	    }
	    
	    String[] portNames = new String[counter];
	    counter = 0;

	    enumCommCount = CommPortIdentifier.getPortIdentifiers();
	    while (enumCommCount.hasMoreElements()) {
	    	serialPortIdCount = enumCommCount.nextElement();
	     	if(serialPortIdCount.getPortType() == CommPortIdentifier.PORT_SERIAL) {
	    		portNames[counter++] = serialPortIdCount.getName();
	    	}
	    }
	    
	    return portNames;
	}
	
	@SuppressWarnings("unchecked")
	public boolean oeffneSerialPort(String portName)
	{
		boolean foundPort = false;
		if (serialPortGeoeffnet != false) {
			System.out.println("Serialport bereits geöffnet");
			return false;
		}
		System.out.println("Öffne Serialport");
		enumComm = CommPortIdentifier.getPortIdentifiers();
		while(enumComm.hasMoreElements()) {
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if (portName.contentEquals(serialPortId.getName())) {
				foundPort = true;
				break;
			}
		}
		if (foundPort != true) {
			System.out.println("Serialport nicht gefunden: " + portName);
			return false;
		}
		try {
			serialPort = (SerialPort) serialPortId.open("Öffnen und Senden", 500);
		} catch (PortInUseException e) {
			System.out.println("Port belegt");
		}
		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			System.out.println("Keinen Zugriff auf OutputStream");
		}
		
		try {
			serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
		} catch(UnsupportedCommOperationException e) {
			System.out.println("Konnte Schnittstellen-Paramter nicht setzen");
		}
		
		serialPortGeoeffnet = true;
		
		Thread verkehrsampelUpdaterThread = new Thread(new verkehrsampelUpdater());
		verkehrsampelUpdaterThread.start();
		
		return true;
	}

	public void schliesseSerialPort()
	{
		if ( serialPortGeoeffnet == true) {
			System.out.println("Schließe Serialport");
			serialPort.close();
			serialPortGeoeffnet = false;
		} else {
			System.out.println("Serialport bereits geschlossen");
		}
	}
	
	public void sendeSerialPort(byte[] data)
	{
		if (serialPortGeoeffnet != true) {
			//System.out.println("Nicht gesendet, da nicht geöffnet");
			return;
		}
		try {
			outputStream.write(data);
		} catch (IOException e) {
			System.out.println("Fehler beim Senden");
		}
	}
	

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}
	
	public long getTimestampEOF() {
		return timestampEOF;
	}

	public void setTimestampEOF(long timestampEOF) {
		Verkehrsampel.timestampEOF = timestampEOF;
	}
	
	public int getBrightness_red() {
		return brightness_red;
	}

	public void setBrightness_red(int brightness_red) {
		this.brightness_red = brightness_red;
	}

	public int getBrightness_yellow() {
		return brightness_yellow;
	}

	public void setBrightness_yellow(int brightness_yellow) {
		this.brightness_yellow = brightness_yellow;
	}

	public int getBrightness_green() {
		return brightness_green;
	}

	public void setBrightness_green(int brightness_green) {
		this.brightness_green = brightness_green;
	}

	private class verkehrsampelUpdater implements Runnable {

		@Override
		public void run() {
			byte[] data = new byte[2];
			
			brightness_green = 15;
			
			data[0] = 0x00;
			data[1] = (byte) 0xF0;
			
			Verkehrsampel.getInstance().sendeSerialPort(data);
			
			while (true) {
				
				if (Mairlist.getInstance().getMairlistPlayerStatePlayer1() == MairlistPlayerState.EOF || Mairlist.getInstance().getMairlistPlayerStatePlayer2() == MairlistPlayerState.EOF) {
					if (Verkehrsampel.getInstance().getCurrentTimestamp()
							- Verkehrsampel.getInstance().getTimestampEOF() > 420) {
						if (Verkehrsampel.getInstance().getBrightness_yellow() != 0) {
							Verkehrsampel.getInstance().setBrightness_yellow(0);
						} else {
							Verkehrsampel.getInstance().setBrightness_yellow(15);
						}

						Verkehrsampel.getInstance()
								.setTimestampEOF(Verkehrsampel.getInstance().getCurrentTimestamp());

						Verkehrsampel.getInstance().setBrightness_green(0);
					}
				} else {
					Verkehrsampel.getInstance().setBrightness_green(15);
					Verkehrsampel.getInstance().setBrightness_yellow(0);
				}
				

				if (MixingDesk.getInstance().isSpeakingActive1() || MixingDesk.getInstance().isSpeakingActive2()) {
					Verkehrsampel.getInstance().setBrightness_green(0);
					Verkehrsampel.getInstance().setBrightness_red(15);
				} else {
					Verkehrsampel.getInstance().setBrightness_green(15);
					Verkehrsampel.getInstance().setBrightness_red(0);
				}
				

				data[0] = (byte) (brightness_red << 4 | brightness_yellow);
				data[1] = (byte) (brightness_green << 4);
				Verkehrsampel.getInstance().sendeSerialPort(data);

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	

}
