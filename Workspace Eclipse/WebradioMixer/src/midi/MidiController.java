package midi;

import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.swing.JOptionPane;
import audio.MixingDesk;
import main.Filemanager;
import main.Mairlist;
import main.Mairlist.MairlistPFLSource;
import main.Mairlist.MairlistPlayerState;

public class MidiController {
	private static MidiController instance;

	private static MidiDevice midiDeviceInput, midiDeviceOutput;
	private static MidiInputReceiverTransmitter midiInputReceiverTransmitter;
	private static MidiDevice.Info[] midiDeviceInfos;

	private static long[] timestamp;
	private static double faderSampleFrequency = 10;

	private static long timestampEOF1 = 0, timestampEOF2 = 0, timestampTelephone_onAir = 0,
			timestampTelephone_recording = 0;

	public long getCurrentTimestamp() {
		return System.currentTimeMillis();
	}

	public static class MIDIKey {
		public static MIDIKey FADER1 = new MIDIKey(-65, 77), BUTTONUP1 = new MIDIKey(-97, 41),
				BUTTONDOWN1 = new MIDIKey(-97, 73), FADER2 = new MIDIKey(-65, 78), BUTTONUP2 = new MIDIKey(-97, 42),
				BUTTONDOWN2 = new MIDIKey(-97, 74), FADER3 = new MIDIKey(-65, 79), BUTTONUP3 = new MIDIKey(-97, 43),
				BUTTONDOWN3 = new MIDIKey(-97, 75), FADER4 = new MIDIKey(-65, 80), BUTTONUP4 = new MIDIKey(-97, 44),
				BUTTONDOWN4 = new MIDIKey(-97, 76), FADER5 = new MIDIKey(-65, 81), BUTTONUP5 = new MIDIKey(-97, 57),
				BUTTONDOWN5 = new MIDIKey(-97, 89), FADER6 = new MIDIKey(-65, 82), BUTTONUP6 = new MIDIKey(-97, 58),
				BUTTONDOWN6 = new MIDIKey(-97, 90), FADER7 = new MIDIKey(-65, 83), BUTTONUP7 = new MIDIKey(-97, 59),
				BUTTONDOWN7 = new MIDIKey(-97, 91), FADER8 = new MIDIKey(-65, 84), BUTTONUP8 = new MIDIKey(-97, 60),
				BUTTONDOWN8 = new MIDIKey(-97, 92), BUTTONMUTE = new MIDIKey(-97, 106),
				BUTTONSOLO = new MIDIKey(-97, 107), BUTTONRECORD = new MIDIKey(-97, 108),
				BUTTONSELECTUP = new MIDIKey(-65, 104), BUTTONSELECTDOWN = new MIDIKey(-65, 105),
				BUTTONSELECTLEFT = new MIDIKey(-65, 106), BUTTONSELECTRIGHT = new MIDIKey(-65, 107);

		private int channel = 0, note_cc = 0;
		private LEDColor ledColor = LEDColor.OFF;

		public enum LEDColor {
			OFF(0), RED(1), YELLOW(2), GREEN(3);

			private final int color;

			LEDColor(int color) {
				this.color = color;
			}

			public int getState() {
				return color;
			}
		}

		public MIDIKey(int channel, int note_cc) {
			this.channel = channel;
			this.note_cc = note_cc;
		}

		public int getChannel() {
			return channel;
		}

		public void setChannel(int channel) {
			this.channel = channel;
		}

		public int getNote_cc() {
			return note_cc;
		}

		public void setNote_cc(int note_cc) {
			this.note_cc = note_cc;
		}

		public LEDColor getLedColor() {
			return ledColor;
		}

		public boolean isFader() {
			if (this.equalsMIDIKey(FADER1) || this.equalsMIDIKey(FADER2) || this.equalsMIDIKey(FADER3)
					|| this.equalsMIDIKey(FADER4) || this.equalsMIDIKey(FADER5) || this.equalsMIDIKey(FADER6)
					|| this.equalsMIDIKey(FADER7) || this.equalsMIDIKey(FADER8)) {
				return true;
			} else {
				return false;
			}
		}

		public void setLEDColor(LEDColor ledColor) {
			ShortMessage myMsg = new ShortMessage();

			if (ledColor != this.ledColor) {
				if (ledColor == LEDColor.RED) {
					try {
						myMsg.setMessage(ShortMessage.NOTE_ON, 15, this.note_cc, 15);
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}
				} else if (ledColor == LEDColor.YELLOW) {
					try {
						myMsg.setMessage(ShortMessage.NOTE_ON, 15, this.note_cc, 62);
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}
				} else if (ledColor == LEDColor.GREEN) {
					try {
						myMsg.setMessage(ShortMessage.NOTE_ON, 15, this.note_cc, 60);
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}
				} else if (ledColor == LEDColor.OFF) {
					try {
						myMsg.setMessage(ShortMessage.NOTE_ON, 15, this.note_cc, 12);
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}
				}

				midiInputReceiverTransmitter.getReceiver().send(myMsg, -1);
			}

			this.ledColor = ledColor;
		}

		public boolean equalsMIDIKey(MIDIKey midikey) {
			if (midikey.getChannel() == this.getChannel() && midikey.getNote_cc() == this.getNote_cc()) {
				return true;
			} else {
				return false;
			}
		}
	}

	private MidiController() {
		timestamp = new long[8];

		midiDeviceInfos = MidiSystem.getMidiDeviceInfo();

		String[] midiDevicesString = new String[midiDeviceInfos.length];

		for (int i = 0; i < midiDeviceInfos.length; i++) {
			midiDevicesString[i] = i + ": " + midiDeviceInfos[i].toString();
		}

		// INPUT

		// String midiDeviceChoosenString = (String)
		// JOptionPane.showInputDialog(null, "Select the Midi Input Device",
		// "Midi Device", JOptionPane.QUESTION_MESSAGE, null, midiDevicesString,
		// 0);

		// int midiDeviceIndex =
		// Integer.parseInt(midiDeviceChoosenString.split(":")[0]);
		int midiDeviceIndex = (int) Filemanager.getInstance().variables.get("midi in");

		try {
			midiDeviceInput = MidiSystem.getMidiDevice(midiDeviceInfos[midiDeviceIndex]);

			/*
			 * @
			 * https://stackoverflow.com/questions/37377869/how-can-i-intercept-
			 * midi-messages List<Transmitter> transmitters =
			 * midiDeviceInput.getTransmitters();
			 * 
			 * for (int j = 0; j < transmitters.size(); j++) {
			 * transmitters.get(j).setReceiver(new
			 * MidiInputReceiverTransmitter(midiDeviceInput.getDeviceInfo().
			 * toString())); }
			 * 
			 * if (transmitters.size() != 0) { Transmitter trans =
			 * midiDeviceInput.getTransmitter(); trans.setReceiver(new
			 * MidiInputReceiverTransmitter(midiDeviceInput.getDeviceInfo().
			 * toString())); }
			 */

			midiDeviceInput.open();

		} catch (MidiUnavailableException e) {
			System.out.println("FAILED");
		}

		// OUTPUT

		// midiDeviceChoosenString = (String) JOptionPane.showInputDialog(null,
		// "Select the Midi Output Device",
		// "Midi Device", JOptionPane.QUESTION_MESSAGE, null, midiDevicesString,
		// 0);

		// midiDeviceIndex =
		// Integer.parseInt(midiDeviceChoosenString.split(":")[0]);
		midiDeviceIndex = (int) Filemanager.getInstance().variables.get("midi out");

		try {
			midiDeviceOutput = MidiSystem.getMidiDevice(midiDeviceInfos[midiDeviceIndex]);

			/*
			 * List<Transmitter> transmitters =
			 * midiDeviceOutput.getTransmitters();
			 * 
			 * for (int j = 0; j < transmitters.size(); j++) {
			 * transmitters.get(j).setReceiver(new
			 * MidiInputReceiver(midiDeviceOutput.getDeviceInfo().toString()));
			 * }
			 * 
			 * if (transmitters.size() != 0) { Transmitter trans =
			 * midiDeviceOutput.getTransmitter(); trans.setReceiver(new
			 * MidiInputReceiver(midiDeviceOutput.getDeviceInfo().toString()));
			 * }
			 */
			midiDeviceOutput.open();

		} catch (MidiUnavailableException e) {
			System.out.println("FAILED");
		}

		midiInputReceiverTransmitter = new MidiInputReceiverTransmitter();

		try {
			midiDeviceInput.getTransmitter().setReceiver(midiInputReceiverTransmitter);
			midiInputReceiverTransmitter.setReceiver(midiDeviceOutput.getReceiver());
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}

		Thread ledUpdaterThread = new Thread(new ledUpdater());
		ledUpdaterThread.start();
	}

	public long[] getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int fader, long timestamp) {
		MidiController.timestamp[fader] = timestamp;
	}

	public double getFaderSampleFrequency() {
		return faderSampleFrequency;
	}

	public void setFaderSampleFrequency(double faderSampleFrequency) {
		MidiController.faderSampleFrequency = faderSampleFrequency;
	}

	public long getTimestampEOF1() {
		return timestampEOF1;
	}

	public void setTimestampEOF1(long timestampEOF1) {
		MidiController.timestampEOF1 = timestampEOF1;
	}

	public long getTimestampEOF2() {
		return timestampEOF2;
	}

	public void setTimestampEOF2(long timestampEOF2) {
		MidiController.timestampEOF2 = timestampEOF2;
	}

	public long getTimestampTelephone_onAir() {
		return timestampTelephone_onAir;
	}

	public void setTimestampTelephone_onAir(long timestampTelephone_onAir) {
		MidiController.timestampTelephone_onAir = timestampTelephone_onAir;
	}

	public long getTimestampTelephone_recording() {
		return timestampTelephone_recording;
	}

	public void setTimestampTelephone_recording(long timestampTelephone_recording) {
		MidiController.timestampTelephone_recording = timestampTelephone_recording;
	}

	public static MidiController getInstance() {
		if (instance == null) {
			instance = new MidiController();
		}
		return instance;
	}

	private static class MidiInputReceiverTransmitter implements Transmitter, Receiver {
		private Receiver receiver;

		@Override
		public Receiver getReceiver() {
			return this.receiver;
		}

		@Override
		public void setReceiver(Receiver receiver) {
			this.receiver = receiver;
		}

		@Override
		public void close() {
		}

		public void send(MidiMessage msg, long timeStamp) {
			int channel = msg.getMessage()[0];
			int note_cc = msg.getMessage()[1];
			double velocity = 0;

			if (msg.getMessage().length > 2)
				velocity = msg.getMessage()[2];

			MIDIKey midikey = new MIDIKey(channel, note_cc);

			//System.out.println("MIDI: Channel = " + channel + " note_cc = " + note_cc + " velocity = " + velocity);

			if (midikey.isFader()) {
				long currentTimestamp = MidiController.getInstance().getCurrentTimestamp();
				long faderTimestamp = MidiController.getInstance().getTimestamp()[note_cc - 77];

				if (currentTimestamp - faderTimestamp < (1000 / faderSampleFrequency)) {
					// System.out.println("time return with " +
					// (currentTimestamp - faderTimestamp));
					// System.out.println("current: " + currentTimestamp);
					// System.out.println("fader: " + faderTimestamp);
					return;
				} else {
					MidiController.getInstance().setTimestamp(note_cc - 77,
							MidiController.getInstance().getCurrentTimestamp());
					// System.out.println("no time return");
				}
			}

			if (midikey.equalsMIDIKey(MIDIKey.BUTTONUP1)) {
				if (velocity == 127) {
					MixingDesk.getInstance().toggleRecording_telephone();
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONUP2)) {
				if (velocity == 127) {
					MixingDesk.getInstance().toggleTelephone_phonesEnabled();
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONUP3)) {
				if (velocity == 127) {
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYER1_PFL_ONOFF);
					Mairlist.getInstance().setMairlistPFLSource(MairlistPFLSource.PLAYER1);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONUP4)) {
				if (velocity == 127) {
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYER2_PFL_ONOFF);
					Mairlist.getInstance().setMairlistPFLSource(MairlistPFLSource.PLAYER2);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONUP5)) {
				if (velocity == 127) {
					Mairlist.getInstance().setCartwallPFLActivated(!Mairlist.getInstance().isCartwallPFLActivated());
					Mairlist.getInstance().setMairlistPFLSource(MairlistPFLSource.CARTWALL);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONUP6)) {
				if (velocity == 127) {

				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONUP7)) {
				if (velocity == 127) {
					MixingDesk.getInstance().setPhonesStdWiedergabe(false);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONUP8)) {
				if (velocity == 127) {
					MixingDesk.getInstance().toggleTelephone_microphoneEnabled();
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONDOWN1)) {
				if (velocity == 127) {
					MixingDesk.getInstance().toggleSpeakingActive(1);

					if (MixingDesk.getInstance().isSpeakingActive1() && !MixingDesk.getInstance().isSpeakingActive2()) {
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.GUI_ONAIR);
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.TALKTIMER_RESET);
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.TALKTIMER_START);
					}

					if (!MixingDesk.getInstance().isSpeakingActive1()
							&& !MixingDesk.getInstance().isSpeakingActive2()) {
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.GUI_OFFAIR);
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.TALKTIMER_STOP);
					}
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONDOWN2)) {
				if (velocity == 127) {
					MixingDesk.getInstance().toggleSpeakingActive(2);

					if (!MixingDesk.getInstance().isSpeakingActive1() && MixingDesk.getInstance().isSpeakingActive2()) {
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.GUI_ONAIR);
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.TALKTIMER_RESET);
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.TALKTIMER_START);
					}

					if (!MixingDesk.getInstance().isSpeakingActive1()
							&& !MixingDesk.getInstance().isSpeakingActive2()) {
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.GUI_OFFAIR);
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.TALKTIMER_STOP);
					}
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONDOWN3)) {
				if (velocity == 127) {
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYER1_STARTSTOP);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONDOWN4)) {
				if (velocity == 127) {
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYER2_STARTSTOP);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONDOWN5)) {
				if (velocity == 127) {
					MixingDesk.getInstance().toggleCartwallActive();
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONDOWN6)) {
				if (velocity == 127) {

				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONDOWN7)) {
				if (velocity == 127) {
					MixingDesk.getInstance().setPhonesStdWiedergabe(true);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONDOWN8)) {
				if (velocity == 127) {
					MixingDesk.getInstance().toggleTelephone_musicEnabled();
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER1)) {
				if (velocity < 5) {
					velocity = 0;
				}

				MixingDesk.getInstance().getMicrophone1().setVolume(velocity / 127);
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER2)) {
				if (velocity < 5) {
					velocity = 0;
				}

				MixingDesk.getInstance().getTelephone().setVolume(velocity / 127);
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER3)) {
				if (velocity < 5) {
					velocity = 0;
				}

				MixingDesk.getInstance().getMairlistChannel1().setVolume(velocity / 127);
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER4)) {
				if (velocity < 5) {
					velocity = 0;
				}

				MixingDesk.getInstance().getMairlistChannel2().setVolume(velocity / 127);
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER5)) {
				if (velocity < 5) {
					velocity = 0;
				}

				MixingDesk.getInstance().getMairlistCartwall().setVolume(velocity / 127);
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER6)) {
				MixingDesk.getInstance().getStdOut().setVolume(velocity / 127);
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER7)) {
				MixingDesk.getInstance().getPhone().setVolume(velocity / 127);
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER8)) {
				MixingDesk.getInstance().getMonitor().setVolume(velocity / 127);
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONMUTE)) {
				if (velocity == 127) {
					MixingDesk.getInstance().toggleMonitorMuted();
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONSOLO)) {
				if (velocity == 127) {
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYLIST_EXTRAPFL);
					Mairlist.getInstance().setMairlistPFLSource(MairlistPFLSource.EXTRA);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONRECORD)) {
				if (velocity == 127) {
					if (!MixingDesk.getInstance().isSetRecording_manually()) {
						MixingDesk.getInstance().setSetRecording_manually(true);
					} else {
						MixingDesk.getInstance().setSetRecording_manually(false);
					}
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONSELECTUP)) {
				midiInputReceiverTransmitter.getReceiver().send(msg, timeStamp);
				if (velocity == 127) {
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYLIST_CURSORUP);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONSELECTDOWN)) {
				midiInputReceiverTransmitter.getReceiver().send(msg, timeStamp);
				if (velocity == 127) {
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYLIST_CURSORDOWN);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONSELECTLEFT)) {
				midiInputReceiverTransmitter.getReceiver().send(msg, timeStamp);
				if (velocity == 127) {
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.CARTWALL_PREVPAGE);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONSELECTRIGHT)) {
				midiInputReceiverTransmitter.getReceiver().send(msg, timeStamp);
				if (velocity == 127) {
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.CARTWALL_NEXTPAGE);
				}
			}

			/*
			 * if (channel == -104) { // note if (velocity == 127) { switch
			 * (note_cc) { case 41: // Channel 1 upper key return; case 73: //
			 * Channel 1 lower key
			 * MixingDesk.getInstance().toggleSpeakingActive(1); return; case
			 * 42: // Channel 2 upper key // System.out.println("Channel 2 oben"
			 * ); return; case 74: // Channel 2 lower key
			 * MixingDesk.getInstance().toggleSpeakingActive(2); return; case
			 * 43: // Channel 3 upper key
			 * Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.
			 * PLAYER1_PFL_ONOFF);
			 * Mairlist.getInstance().setMairlistPFLSource(MairlistPFLSource.
			 * PLAYER1); MixingDesk.getInstance().togglePflActive(); return;
			 * case 75: // Channel 3 lower key
			 * Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.
			 * PLAYER1_STARTSTOP); return; case 44: // Channel 4 upper key
			 * Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.
			 * PLAYER2_PFL_ONOFF);
			 * Mairlist.getInstance().setMairlistPFLSource(MairlistPFLSource.
			 * PLAYER2); MixingDesk.getInstance().togglePflActive(); return;
			 * case 76: // Channel 4 lower key
			 * Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.
			 * PLAYER2_STARTSTOP); return; case 57: // Channel 5 upper key
			 * Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.
			 * CARTWALL_MODEPFL);
			 * Mairlist.getInstance().setMairlistPFLSource(MairlistPFLSource.
			 * CARTWALL); MixingDesk.getInstance().toggleCartwallActive();
			 * return; case 89: // Channel 5 lower key return; case 58: //
			 * Channel 6 upper key return; case 90: // Channel 6 lower key
			 * return; case 59: // Channel 7 upper key
			 * MixingDesk.getInstance().activatePhonesNormal(); return; case 91:
			 * // Channel 7 lower key
			 * MixingDesk.getInstance().activatePhonesStdWiedergabe(); return;
			 * case 60: // Channel 8 upper key return; case 92: // Channel 8
			 * lower key return; case 106: // Mute key
			 * MixingDesk.getInstance().toggleMonitorMuted(); return; case 107:
			 * // Solo key
			 * Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.
			 * PLAYLIST_EXTRAPFL);
			 * Mairlist.getInstance().setMairlistPFLSource(MairlistPFLSource.
			 * EXTRA); MixingDesk.getInstance().togglePflActive(); return; } } }
			 * else if (channel == -72) { if (note_cc < 104) { if
			 * (MidiController.getInstance().getCurrentTimestamp() -
			 * MidiController.getInstance().getTimestamp()[note_cc - 77] < (1000
			 * / faderSampleFrequency)) { return; } else {
			 * MidiController.getInstance().setTimestamp(note_cc - 77,
			 * MidiController.getInstance().getCurrentTimestamp()); } }
			 * 
			 * if (note_cc != 82 && note_cc != 83 && note_cc != 84) { if
			 * (velocity < 10) { velocity = 0; } else if (velocity >= 109) {
			 * velocity = 127; } }
			 * 
			 * switch (note_cc) { case 77: // Fader 1
			 * MixingDesk.getInstance().getMicrophone1().setVolume(velocity /
			 * 127); return; case 78: // Fader 2
			 * MixingDesk.getInstance().getMicrophone2().setVolume(velocity /
			 * 127); return; case 79: // Fader 3
			 * MixingDesk.getInstance().getMairlistChannel1().setVolume(velocity
			 * / 127); return; case 80: // Fader 4
			 * MixingDesk.getInstance().getMairlistChannel2().setVolume(velocity
			 * / 127); return; case 81: // Fader 5
			 * MixingDesk.getInstance().getMairlistCartwall().setVolume(velocity
			 * / 127); return; case 82: // Fader 6
			 * MixingDesk.getInstance().getStdOut().setVolume(velocity / 127);
			 * return; case 83: // Fader 7
			 * MixingDesk.getInstance().getPhone().setVolume(velocity / 127);
			 * return; case 84: // Fader 8
			 * MixingDesk.getInstance().getMonitor().setVolume(velocity / 127);
			 * return; case 104: // Select up if (velocity == 127)
			 * Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.
			 * PLAYLIST_CURSORUP); return; case 105: // Select down if (velocity
			 * == 127)
			 * Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.
			 * PLAYLIST_CURSORDOWN); return; case 106: // Select left if
			 * (velocity == 127)
			 * Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.
			 * CARTWALL_PREVPAGE); return; case 107: // Select right if
			 * (velocity == 127)
			 * Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.
			 * CARTWALL_NEXTPAGE); return; } }
			 */
		}
	}

	public void sendLED(MIDIKey midikey, int color) {

	}

	private class ledUpdater implements Runnable {

		@Override
		public void run() {
			MIDIKey.BUTTONUP1.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONUP2.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONUP3.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONUP4.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONUP5.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONUP6.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONUP7.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONUP8.setLEDColor(MIDIKey.LEDColor.OFF);

			MIDIKey.BUTTONDOWN1.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONDOWN2.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONDOWN3.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONDOWN4.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONDOWN5.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONDOWN6.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONDOWN7.setLEDColor(MIDIKey.LEDColor.OFF);
			MIDIKey.BUTTONDOWN8.setLEDColor(MIDIKey.LEDColor.OFF);

			while (true) {
				if (Mairlist.getInstance().getMairlistPlayerStatePlayer1() == MairlistPlayerState.EMPTY) {
					MIDIKey.BUTTONDOWN3.setLEDColor(MIDIKey.LEDColor.RED); // aus
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer1() == MairlistPlayerState.LOADED) {
					MIDIKey.BUTTONDOWN3.setLEDColor(MIDIKey.LEDColor.YELLOW); // gr�n
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer1() == MairlistPlayerState.PLAYING) {
					MIDIKey.BUTTONDOWN3.setLEDColor(MIDIKey.LEDColor.GREEN); // gelb
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer1() == MairlistPlayerState.STOPPED) {
					MIDIKey.BUTTONDOWN3.setLEDColor(MIDIKey.LEDColor.OFF); // aus
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer1() == MairlistPlayerState.EOF) {
					if (MidiController.getInstance().getCurrentTimestamp()
							- MidiController.getInstance().getTimestampEOF1() > 420) {
						if (MIDIKey.BUTTONDOWN3.getLedColor() != MIDIKey.LEDColor.OFF) {
							MIDIKey.BUTTONDOWN3.setLEDColor(MIDIKey.LEDColor.OFF); // aus
						} else {
							MIDIKey.BUTTONDOWN3.setLEDColor(MIDIKey.LEDColor.RED); // rot
						}

						MidiController.getInstance()
								.setTimestampEOF1(MidiController.getInstance().getCurrentTimestamp());
					}
				}

				if (Mairlist.getInstance().getMairlistPlayerStatePlayer2() == MairlistPlayerState.EMPTY) {
					MIDIKey.BUTTONDOWN4.setLEDColor(MIDIKey.LEDColor.RED); // aus
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer2() == MairlistPlayerState.LOADED) {
					MIDIKey.BUTTONDOWN4.setLEDColor(MIDIKey.LEDColor.YELLOW);
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer2() == MairlistPlayerState.PLAYING) {
					MIDIKey.BUTTONDOWN4.setLEDColor(MIDIKey.LEDColor.GREEN);
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer2() == MairlistPlayerState.STOPPED) {
					MIDIKey.BUTTONDOWN4.setLEDColor(MIDIKey.LEDColor.OFF);
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer2() == MairlistPlayerState.EOF) {
					if (MidiController.getInstance().getCurrentTimestamp()
							- MidiController.getInstance().getTimestampEOF2() > 420) {
						if (MIDIKey.BUTTONDOWN4.getLedColor() != MIDIKey.LEDColor.OFF) {
							MIDIKey.BUTTONDOWN4.setLEDColor(MIDIKey.LEDColor.OFF); // aus
						} else {
							MIDIKey.BUTTONDOWN4.setLEDColor(MIDIKey.LEDColor.RED); // rot
						}

						MidiController.getInstance()
								.setTimestampEOF2(MidiController.getInstance().getCurrentTimestamp());
					}
				}

				if (MixingDesk.getInstance().isCartwallActive()) {
					MIDIKey.BUTTONDOWN5.setLEDColor(MIDIKey.LEDColor.GREEN); // gr�n
				} else {
					MIDIKey.BUTTONDOWN5.setLEDColor(MIDIKey.LEDColor.YELLOW); // gelb
				}

				if (MairlistPFLSource.PLAYER1.isPFLActive()) {
					MIDIKey.BUTTONUP3.setLEDColor(MIDIKey.LEDColor.RED); // rot
					MixingDesk.getInstance().setPflActive(true);
				} else {
					MIDIKey.BUTTONUP3.setLEDColor(MIDIKey.LEDColor.OFF); // aus
					if (!MairlistPFLSource.PLAYER2.isPFLActive() && !MairlistPFLSource.CARTWALL.isPFLActive()
							&& !MairlistPFLSource.EXTRA.isPFLActive() && !MairlistPFLSource.UNKNOWN.isPFLActive())
						MixingDesk.getInstance().setPflActive(false);
				}

				if (MairlistPFLSource.PLAYER2.isPFLActive()) {
					MIDIKey.BUTTONUP4.setLEDColor(MIDIKey.LEDColor.RED); // rot
					MixingDesk.getInstance().setPflActive(true);
				} else {
					MIDIKey.BUTTONUP4.setLEDColor(MIDIKey.LEDColor.OFF); // aus
					if (!MairlistPFLSource.PLAYER1.isPFLActive() && !MairlistPFLSource.CARTWALL.isPFLActive()
							&& !MairlistPFLSource.EXTRA.isPFLActive() && !MairlistPFLSource.UNKNOWN.isPFLActive())
						MixingDesk.getInstance().setPflActive(false);
				}

				if (MairlistPFLSource.CARTWALL.isPFLActive()) {
					MIDIKey.BUTTONUP5.setLEDColor(MIDIKey.LEDColor.RED); // rot
					MixingDesk.getInstance().setPflActive(true);
				} else {
					MIDIKey.BUTTONUP5.setLEDColor(MIDIKey.LEDColor.OFF); // aus
					if (!MairlistPFLSource.PLAYER1.isPFLActive() && !MairlistPFLSource.PLAYER2.isPFLActive()
							&& !MairlistPFLSource.EXTRA.isPFLActive() && !MairlistPFLSource.UNKNOWN.isPFLActive())
						MixingDesk.getInstance().setPflActive(false);
				}

				if (MairlistPFLSource.EXTRA.isPFLActive()) {
					MIDIKey.BUTTONSOLO.setLEDColor(MIDIKey.LEDColor.RED); // rot
					MixingDesk.getInstance().setPflActive(true);
				} else {
					MIDIKey.BUTTONSOLO.setLEDColor(MIDIKey.LEDColor.OFF); // aus
					if (!MairlistPFLSource.PLAYER1.isPFLActive() && !MairlistPFLSource.PLAYER2.isPFLActive()
							&& !MairlistPFLSource.CARTWALL.isPFLActive() && !MairlistPFLSource.UNKNOWN.isPFLActive())
						MixingDesk.getInstance().setPflActive(false);
				}

				if (MairlistPFLSource.UNKNOWN.isPFLActive()) {
					MixingDesk.getInstance().setPflActive(true);
				} else {
					if (!MairlistPFLSource.PLAYER1.isPFLActive() && !MairlistPFLSource.PLAYER2.isPFLActive()
							&& !MairlistPFLSource.CARTWALL.isPFLActive() && !MairlistPFLSource.EXTRA.isPFLActive())
						MixingDesk.getInstance().setPflActive(false);
				}

				if (MixingDesk.getInstance().isSpeakingActive1()) {
					MIDIKey.BUTTONDOWN1.setLEDColor(MIDIKey.LEDColor.RED); // rot
				} else {
					MIDIKey.BUTTONDOWN1.setLEDColor(MIDIKey.LEDColor.OFF); // aus
				}

				if (MixingDesk.getInstance().isSpeakingActive2()) {
					MIDIKey.BUTTONDOWN2.setLEDColor(MIDIKey.LEDColor.RED); // rot
				} else {
					MIDIKey.BUTTONDOWN2.setLEDColor(MIDIKey.LEDColor.OFF); // aus
				}

				if (MixingDesk.getInstance().isTelephone_phonesEnabled()) {
					MIDIKey.BUTTONUP2.setLEDColor(MIDIKey.LEDColor.GREEN); // gr�n
				} else {
					MIDIKey.BUTTONUP2.setLEDColor(MIDIKey.LEDColor.OFF); // aus
				}

				if (MixingDesk.getInstance().isMonitorMuted()) {
					MIDIKey.BUTTONMUTE.setLEDColor(MIDIKey.LEDColor.RED); // rot
				} else {
					MIDIKey.BUTTONMUTE.setLEDColor(MIDIKey.LEDColor.OFF); // aus
				}

				if (MixingDesk.getInstance().isPhonesStdWiedergabe()) {
					MIDIKey.BUTTONUP7.setLEDColor(MIDIKey.LEDColor.OFF); // aus
					MIDIKey.BUTTONDOWN7.setLEDColor(MIDIKey.LEDColor.YELLOW); // rot
				} else {
					MIDIKey.BUTTONUP7.setLEDColor(MIDIKey.LEDColor.YELLOW); // rot
					MIDIKey.BUTTONDOWN7.setLEDColor(MIDIKey.LEDColor.OFF); // aus
				}

				if (MixingDesk.getInstance().isSetRecording_manually()) {
					MIDIKey.BUTTONRECORD.setLEDColor(MIDIKey.LEDColor.RED);
				} else {
					MIDIKey.BUTTONRECORD.setLEDColor(MIDIKey.LEDColor.OFF);
				}

				if (MixingDesk.getInstance().isSetRecording_telephone()) {
					if (MidiController.getInstance().getCurrentTimestamp()
							- MidiController.getInstance().getTimestampTelephone_recording() > 420) {
						if (MIDIKey.BUTTONUP1.getLedColor() != MIDIKey.LEDColor.OFF) {
							MIDIKey.BUTTONUP1.setLEDColor(MIDIKey.LEDColor.OFF); // aus
						} else {
							MIDIKey.BUTTONUP1.setLEDColor(MIDIKey.LEDColor.RED); // rot
						}

						MidiController.getInstance()
								.setTimestampTelephone_recording(MidiController.getInstance().getCurrentTimestamp());
					}
				} else {
					MIDIKey.BUTTONUP1.setLEDColor(MIDIKey.LEDColor.OFF); // aus
				}

				if (MixingDesk.getInstance().isSpeakingActive2()) {
					if (MidiController.getInstance().getCurrentTimestamp()
							- MidiController.getInstance().getTimestampTelephone_onAir() > 420) {
						if (MIDIKey.BUTTONDOWN2.getLedColor() != MIDIKey.LEDColor.OFF) {
							MIDIKey.BUTTONDOWN2.setLEDColor(MIDIKey.LEDColor.OFF); // aus
						} else {
							MIDIKey.BUTTONDOWN2.setLEDColor(MIDIKey.LEDColor.RED); // rot
						}

						MidiController.getInstance()
								.setTimestampTelephone_onAir(MidiController.getInstance().getCurrentTimestamp());
					}
				} else {
					MIDIKey.BUTTONDOWN2.setLEDColor(MIDIKey.LEDColor.OFF); // aus
				}

				if (MixingDesk.getInstance().isTelephone_microphoneEnabled()) {
					MIDIKey.BUTTONUP8.setLEDColor(MIDIKey.LEDColor.YELLOW);
				} else {
					MIDIKey.BUTTONUP8.setLEDColor(MIDIKey.LEDColor.OFF);
				}

				if (MixingDesk.getInstance().isTelephone_musicEnabled()) {
					MIDIKey.BUTTONDOWN8.setLEDColor(MIDIKey.LEDColor.YELLOW);
				} else {
					MIDIKey.BUTTONDOWN8.setLEDColor(MIDIKey.LEDColor.OFF);
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static class MidiInputReceiver implements Receiver {
		public String name;

		public MidiInputReceiver(String name) {
			this.name = name;
		}

		public void send(MidiMessage msg, long timeStamp) {
		}

		public void close() {
		}
	}
}
