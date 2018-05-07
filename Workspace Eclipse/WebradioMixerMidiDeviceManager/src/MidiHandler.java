import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;

public class MidiHandler {

	public static void main(String[] args) throws MidiUnavailableException {
		MyMidiDevice myDevice = new MyMidiDevice();

		MidiDevice inputDevice, outputDevice;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

		for (int i = 0; i < infos.length; i++) {
			System.out.println(i + ": " + infos[i]);
		}

		System.out.println("Bitte den Index des Midi-Ger�tes eingeben input:");
		Scanner in = new Scanner(System.in);
		int index = in.nextInt();

		inputDevice = MidiSystem.getMidiDevice(infos[index]);
		inputDevice.open();

		System.out.println("Bitte den Index des Midi-Ger�tes eingeben output:");
		int index2 = in.nextInt();
		in.close();

		outputDevice = MidiSystem.getMidiDevice(infos[index2]);
		outputDevice.open();

		System.out.println("input max rec " + inputDevice.getMaxReceivers());
		System.out.println("input max tran " + inputDevice.getMaxTransmitters());
		System.out.println("input get rec " + inputDevice.getReceivers());
		System.out.println("input get trans " + inputDevice.getTransmitters());
		
		System.out.println("output max rec " + outputDevice.getMaxReceivers());
		System.out.println("output max tran " + outputDevice.getMaxTransmitters());
		System.out.println("output get rec " + outputDevice.getReceivers());
		System.out.println("output get trans " + outputDevice.getTransmitters());
		
		
		
		
//		inputDevice.getTransmitter().setReceiver(myDevice);
//		myDevice.setReceiver(outputDevice.getReceiver());

		ShortMessage myMsg = new ShortMessage();

		
		
		
		try {
			myMsg.setMessage(ShortMessage.CONTROL_CHANGE, 0x00, 0, 127);
		} catch (InvalidMidiDataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

//		myDevice.getReceiver().send(myMsg, -1);
		outputDevice.getReceiver().send(myMsg, -1);
		
		
//		try {
//			Thread.sleep(100000);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		
		while (true) {
			try {
				myMsg.setMessage(ShortMessage.NOTE_ON, 0x01, 41, 15); // 144/145
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}

			myDevice.getReceiver().send(myMsg, -1);
			outputDevice.getReceiver().send(myMsg, -1);

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				myMsg.setMessage(ShortMessage.NOTE_ON, 0x01, 41, 60); // 0x98
																		// //128/129
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}

			myDevice.getReceiver().send(myMsg, -1);
			outputDevice.getReceiver().send(myMsg, -1);

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/*
		 * MidiDevice device; MidiDevice.Info[] infos =
		 * MidiSystem.getMidiDeviceInfo();
		 * 
		 * for (int i = 0; i < infos.length; i++) { System.out.println(i + ": "
		 * + infos[i]); }
		 * 
		 * System.out.println("Bitte den Index des Midi-Ger�tes eingeben:");
		 * Scanner in = new Scanner(System.in); int index = in.nextInt();
		 * in.close();
		 * 
		 * Transmitter trans = null;
		 * 
		 * try { device = MidiSystem.getMidiDevice(infos[index]); // does the
		 * device have any transmitters? // if it does, add it to the device
		 * list System.out.println(infos[index]);
		 * 
		 * // get all transmitters List<Transmitter> transmitters =
		 * device.getTransmitters(); // and for each transmitter
		 * 
		 * for (int j = 0; j < transmitters.size(); j++) { // create a new
		 * receiver transmitters.get(j).setReceiver( // using my own
		 * MidiInputReceiver new
		 * MidiInputReceiver(device.getDeviceInfo().toString())); }
		 * 
		 * trans = device.getTransmitter(); trans.setReceiver(new
		 * MidiInputReceiver(device.getDeviceInfo().toString()));
		 * 
		 * // open each device device.open(); // if code gets this far without
		 * throwing an exception // print a success message
		 * System.out.println(device.getDeviceInfo() + " Was Opened");
		 * 
		 * } catch (MidiUnavailableException e) { System.out.println("FAILED");
		 * }
		 * 
		 * ShortMessage myMsg = new ShortMessage();
		 * 
		 * while (true) { try { myMsg.setMessage(ShortMessage.NOTE_ON, 0x01, 41,
		 * 15); //144/145 } catch (InvalidMidiDataException e) {
		 * e.printStackTrace(); }
		 * 
		 * trans.getReceiver().send(myMsg, -1);
		 * 
		 * try { Thread.sleep(500); } catch (InterruptedException e) {
		 * e.printStackTrace(); }
		 * 
		 * try { myMsg.setMessage(ShortMessage.NOTE_ON, 0x01, 41, 60); //0x98
		 * //128/129 } catch (InvalidMidiDataException e) { e.printStackTrace();
		 * }
		 * 
		 * trans.getReceiver().send(myMsg, -1);
		 * 
		 * try { Thread.sleep(500); } catch (InterruptedException e) {
		 * e.printStackTrace(); } }
		 */
	}

	public static class MyMidiDevice implements Transmitter, Receiver {

		private Receiver receiver;

		@Override
		public Receiver getReceiver() {
			System.out.println("get");
			return this.receiver;
		}

		@Override
		public void setReceiver(Receiver receiver) {
			this.receiver = receiver;
		}

		@Override
		public void close() {
		}

		@Override
		public void send(MidiMessage message, long timeStamp) {
			System.out.println(message); // computations
			this.getReceiver().send(message, timeStamp);
		}
	}

	// tried to write my own class. I thought the send method handles an
	// MidiEvents sent to it
	public static class MidiInputReceiver implements Receiver {
		public String name;

		public MidiInputReceiver(String name) {
			this.name = name;
		}

		public void send(MidiMessage msg, long timeStamp) {
			// for (int i = 0; i < msg.getMessage().length; i++) {
			// System.out.println("midi received " + i + ": " +
			// msg.getMessage()[i]);
			// System.out.println("midi received status " + i + ": " +
			// msg.getStatus());

			int channel = msg.getMessage()[0];
			int note_cc = msg.getMessage()[1];
			int velocity = 0;

			if (msg.getMessage().length > 2)
				velocity = msg.getMessage()[2];

			System.out.println("Channel: " + channel + " cc: " + note_cc + " vel: " + velocity);

			if (channel == -104) {
				// note
				if (velocity == 127) {
					switch (note_cc) {
					case 41:
						System.out.println("Channel 1 oben");
						return; // 1.1
					case 73:
						System.out.println("Channel 1 unten");
						return; // 1.2
					case 42:
						System.out.println("Channel 2 oben");
						return;
					case 74:
						System.out.println("Channel 2 unten");
						return;
					case 43:
						System.out.println("Channel 3 oben");
						return;
					case 75:
						System.out.println("Channel 3 unten");
						return;
					case 44:
						System.out.println("Channel 4 oben");
						return;
					case 76:
						System.out.println("Channel 4 unten");
						return;
					case 57:
						System.out.println("Channel 5 oben");
						return;
					case 89:
						System.out.println("Channel 5 unten");
						return;
					case 58:
						System.out.println("Channel 6 oben");
						return;
					case 90:
						System.out.println("Channel 6 unten");
						return;
					case 59:
						System.out.println("Channel 7 oben");
						return;
					case 91:
						System.out.println("Channel 7 unten");
						return;
					case 60:
						System.out.println("Channel 8 oben");
						return;
					case 92:
						System.out.println("Channel 8 unten");
						return;
					}
				}
			} else if (channel == -72) {
				// cc
				switch (note_cc) {
				case 77:
					System.out.println("Channel 1 " + velocity);
					return;
				case 78:
					System.out.println("Channel 2 " + velocity);
					return;
				case 79:
					System.out.println("Channel 3 " + velocity);
					return;
				case 80:
					System.out.println("Channel 4 " + velocity);
					return;
				case 81:
					System.out.println("Channel 5 " + velocity);
					return;
				case 82:
					System.out.println("Channel 6 " + velocity);
					return;
				case 83:
					System.out.println("Channel 7 " + velocity);
					return;
				case 84:
					System.out.println("Channel 8 " + velocity);
					return;
				}
			}
			// }
		}

		public void close() {
		}
	}
}
