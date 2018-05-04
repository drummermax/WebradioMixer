package midi;

import java.util.List;
import java.util.Scanner;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import javax.swing.JOptionPane;

import audio.MixingDesk;
import main.Mairlist;

public class MidiController {
	private static MidiController instance;

	private static MidiDevice midiDevice;
	private static MidiDevice.Info[] midiDeviceInfos;

	private static int[] timestamp;
	private static double faderSampleFrequency = 10;

	public int getCurrentTimestamp() {
		return (int) System.currentTimeMillis();
	}

	private MidiController() {
		timestamp = new int[8];

		midiDeviceInfos = MidiSystem.getMidiDeviceInfo();

		String[] midiDevicesString = new String[midiDeviceInfos.length];

		for (int i = 0; i < midiDeviceInfos.length; i++) {
			midiDevicesString[i] = i + ": " + midiDeviceInfos[i].toString();
		}

		String midiDeviceChoosenString = (String) JOptionPane.showInputDialog(null, "Select the Midi Device",
				"Midi Device", JOptionPane.QUESTION_MESSAGE, null, midiDevicesString, 0);

		int midiDeviceIndex = Integer.parseInt(midiDeviceChoosenString.split(":")[0]);

		try {
			midiDevice = MidiSystem.getMidiDevice(midiDeviceInfos[midiDeviceIndex]);

			List<Transmitter> transmitters = midiDevice.getTransmitters();

			for (int j = 0; j < transmitters.size(); j++) {
				transmitters.get(j).setReceiver(new MidiInputReceiver(midiDevice.getDeviceInfo().toString()));
			}

			Transmitter trans = midiDevice.getTransmitter();
			trans.setReceiver(new MidiInputReceiver(midiDevice.getDeviceInfo().toString()));

			midiDevice.open();

		} catch (MidiUnavailableException e) {
			System.out.println("FAILED");
		}
	}

	public int[] getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int fader, int timestamp) {
		MidiController.timestamp[fader] = timestamp;
	}

	public double getFaderSampleFrequency() {
		return faderSampleFrequency;
	}

	public void setFaderSampleFrequency(double faderSampleFrequency) {
		MidiController.faderSampleFrequency = faderSampleFrequency;
	}

	public static MidiController getInstance() {
		if (instance == null) {
			instance = new MidiController();
		}
		return instance;
	}

	private static class MidiInputReceiver implements Receiver {
		public String name;

		public MidiInputReceiver(String name) {
			this.name = name;
		}

		public void send(MidiMessage msg, long timeStamp) {
			int channel = msg.getMessage()[0];
			int note_cc = msg.getMessage()[1];
			double velocity = 0;

			if (msg.getMessage().length > 2)
				velocity = msg.getMessage()[2];
			if (channel == -104) {
				// note
				if (velocity == 127) {
					switch (note_cc) {
					case 41: // Channel 1 upper key
						return;
					case 73: // Channel 1 lower key
						MixingDesk.getInstance().toggleSpeakingActive(1);
						return;
					case 42: // Channel 2 upper key
						// System.out.println("Channel 2 oben");
						return;
					case 74: // Channel 2 lower key
						MixingDesk.getInstance().toggleSpeakingActive(2);
						return;
					case 43: // Channel 3 upper key
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYER1_PFL_ONOFF);
						MixingDesk.getInstance().togglePflActive();
						return;
					case 75: // Channel 3 lower key
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYER1_STARTSTOP);
						return;
					case 44: // Channel 4 upper key
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYER2_PFL_ONOFF);
						MixingDesk.getInstance().togglePflActive();
						return;
					case 76: // Channel 4 lower key
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYER2_STARTSTOP);
						return;
					case 57: // Channel 5 upper key
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.CARTWALL_MODEPFL);
						MixingDesk.getInstance().toggleCartwallActive();
						return;
					case 89: // Channel 5 lower key
						return;
					case 58: // Channel 6 upper key
						return;
					case 90: // Channel 6 lower key
						return;
					case 59: // Channel 7 upper key
						MixingDesk.getInstance().activatePhonesNormal();
						return;
					case 91: // Channel 7 lower key
						MixingDesk.getInstance().activatePhonesStdWiedergabe();
						return;
					case 60: // Channel 8 upper key
						return;
					case 92: // Channel 8 lower key
						return;
					case 106: // Mute key
						MixingDesk.getInstance().toggleMonitorMuted();
						return;
					case 107: // Solo key
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYLIST_EXTRAPFL);
						MixingDesk.getInstance().togglePflActive();
						return;
					}
				}
			} else if (channel == -72) {
				if (note_cc < 104) {
					if (MidiController.getInstance().getCurrentTimestamp()
							- MidiController.getInstance().getTimestamp()[note_cc - 77] < (1000
									/ faderSampleFrequency)) {
						return;
					} else {
						MidiController.getInstance().setTimestamp(note_cc - 77,
								MidiController.getInstance().getCurrentTimestamp());
					}
				}

				if (note_cc != 82 && note_cc != 83 && note_cc != 84) {
					if (velocity < 10) {
						velocity = 0;
					} else if (velocity >= 109) {
						velocity = 127;
					}
				}

				switch (note_cc) {
				case 77: // Fader 1
					MixingDesk.getInstance().getMicrophone1().setVolume(velocity / 127);
					return;
				case 78: // Fader 2
					MixingDesk.getInstance().getMicrophone2().setVolume(velocity / 127);
					return;
				case 79: // Fader 3
					MixingDesk.getInstance().getMairlistChannel1().setVolume(velocity / 127);
					return;
				case 80: // Fader 4
					MixingDesk.getInstance().getMairlistChannel2().setVolume(velocity / 127);
					return;
				case 81: // Fader 5
					MixingDesk.getInstance().getMairlistCartwall().setVolume(velocity / 127);
					return;
				case 82: // Fader 6
					MixingDesk.getInstance().getStdOut().setVolume(velocity / 127);
					return;
				case 83: // Fader 7
					MixingDesk.getInstance().getPhone().setVolume(velocity / 127);
					return;
				case 84: // Fader 8
					MixingDesk.getInstance().getMonitor().setVolume(velocity / 127);
					return;
				case 104: // Select up
					if (velocity == 127)
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYLIST_MOVEUP);
					return;
				case 105: // Select down
					if (velocity == 127)
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYLIST_MOVEDOWN);
					return;
				case 106: // Select left
					if (velocity == 127)
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.CARTWALL_PREVPAGE);
					return;
				case 107: // Select right
					if (velocity == 127)
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.CARTWALL_NEXTPAGE);
					return;
				}
			}
		}

		public void close() {
		}
	}
}
