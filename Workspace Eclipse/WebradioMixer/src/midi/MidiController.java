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
import main.Mairlist.MairlistPFLSource;
import main.Mairlist.MairlistPlayerState;

public class MidiController {
	private static MidiController instance;

	private static MidiDevice midiDevice;
	private static MidiDevice.Info[] midiDeviceInfos;

	private static int[] timestamp;
	private static double faderSampleFrequency = 10;

	private static int timestampEOF1 = 0, timestampEOF2 = 0;

	public int getCurrentTimestamp() {
		return (int) System.currentTimeMillis();
	}

	public static class MIDIKey {
		public static MIDIKey FADER1 = new MIDIKey(-72, 77), BUTTONUP1 = new MIDIKey(-104, 41),
				BUTTONDOWN1 = new MIDIKey(-104, 73), FADER2 = new MIDIKey(-72, 78), BUTTONUP2 = new MIDIKey(-104, 42),
				BUTTONDOWN2 = new MIDIKey(-104, 74), FADER3 = new MIDIKey(-72, 79), BUTTONUP3 = new MIDIKey(-104, 43),
				BUTTONDOWN3 = new MIDIKey(-104, 75), FADER4 = new MIDIKey(-72, 80), BUTTONUP4 = new MIDIKey(-104, 44),
				BUTTONDOWN4 = new MIDIKey(-104, 76), FADER5 = new MIDIKey(-72, 81), BUTTONUP5 = new MIDIKey(-104, 57),
				BUTTONDOWN5 = new MIDIKey(-104, 89), FADER6 = new MIDIKey(-72, 82), BUTTONUP6 = new MIDIKey(-104, 58),
				BUTTONDOWN6 = new MIDIKey(-104, 90), FADER7 = new MIDIKey(-72, 83), BUTTONUP7 = new MIDIKey(-104, 59),
				BUTTONDOWN7 = new MIDIKey(-104, 91), FADER8 = new MIDIKey(-72, 84), BUTTONUP8 = new MIDIKey(-104, 60),
				BUTTONDOWN8 = new MIDIKey(-104, 92), BUTTONMUTE = new MIDIKey(-104, 106),
				BUTTONSOLO = new MIDIKey(-104, 107), BUTTONSELECTUP = new MIDIKey(-72, 104),
				BUTTONSELECTDOWN = new MIDIKey(-72, 105), BUTTONSELECTLEFT = new MIDIKey(-72, 106),
				BUTTONSELECTRIGHT = new MIDIKey(-72, 107);

		private int channel = 0, note_cc = 0;
		private boolean ledActivated = false;

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

		public boolean isFader() {
			if (this.equalsMIDIKey(FADER1) || this.equalsMIDIKey(FADER2) || this.equalsMIDIKey(FADER3) || this.equalsMIDIKey(FADER4)
					|| this.equalsMIDIKey(FADER5) || this.equalsMIDIKey(FADER6) || this.equalsMIDIKey(FADER7) || this.equalsMIDIKey(FADER8)) {
				return true;
			} else {
				return false;
			}
		}

		public void setLedActivated(boolean ledActivated, int color) {
			if (ledActivated && !this.ledActivated) {
				// color anstellen
			} else if (!ledActivated && this.ledActivated) {
				// led ausstellen
			}
		}

		public boolean isLedActivated() {
			return ledActivated;
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

		Thread ledUpdaterThread = new Thread(new ledUpdater());
		ledUpdaterThread.start();
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

	public int getTimestampEOF1() {
		return timestampEOF1;
	}

	public void setTimestampEOF1(int timestampEOF1) {
		MidiController.timestampEOF1 = timestampEOF1;
	}

	public int getTimestampEOF2() {
		return timestampEOF2;
	}

	public void setTimestampEOF2(int timestampEOF2) {
		MidiController.timestampEOF2 = timestampEOF2;
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

			MIDIKey midikey = new MIDIKey(channel, note_cc);

			if (midikey.isFader()) {
				if (MidiController.getInstance().getCurrentTimestamp()
						- MidiController.getInstance().getTimestamp()[note_cc - 77] < (1000 / faderSampleFrequency)) {
					return;
				} else {
					MidiController.getInstance().setTimestamp(note_cc - 77,
							MidiController.getInstance().getCurrentTimestamp());
				}
			}

			if (midikey.equalsMIDIKey(MIDIKey.BUTTONUP1)) {
				if (velocity == 127) {

				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONUP2)) {
				if (velocity == 127) {

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
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.CARTWALL_MODEPFL);
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

				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONDOWN1)) {
				if (velocity == 127) {
					MixingDesk.getInstance().toggleSpeakingActive(1);
					
					if (MixingDesk.getInstance().isSpeakingActive1() || MixingDesk.getInstance().isSpeakingActive2()) {
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.GUI_ONAIR);
					} else {
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.GUI_OFFAIR);
					}
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONDOWN2)) {
				if (velocity == 127) {
					MixingDesk.getInstance().toggleSpeakingActive(2);
					
					if (MixingDesk.getInstance().isSpeakingActive1() || MixingDesk.getInstance().isSpeakingActive2()) {
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.GUI_ONAIR);
					} else {
						Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.GUI_OFFAIR);
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

				}
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER1)) {
				if (velocity < 10) {
					velocity = 0;
				} else if (velocity >= 109) {
					velocity = 127;
				}

				MixingDesk.getInstance().getMicrophone1().setVolume(velocity / 127);
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER2)) {
				if (velocity < 10) {
					velocity = 0;
				} else if (velocity >= 109) {
					velocity = 127;
				}

				MixingDesk.getInstance().getMicrophone2().setVolume(velocity / 127);
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER3)) {
				if (velocity < 10) {
					velocity = 0;
				} else if (velocity >= 109) {
					velocity = 127;
				}

				MixingDesk.getInstance().getMairlistChannel1().setVolume(velocity / 127);
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER4)) {
				if (velocity < 10) {
					velocity = 0;
				} else if (velocity >= 109) {
					velocity = 127;
				}

				MixingDesk.getInstance().getMairlistChannel2().setVolume(velocity / 127);
			} else if (midikey.equalsMIDIKey(MIDIKey.FADER5)) {
				if (velocity < 10) {
					velocity = 0;
				} else if (velocity >= 109) {
					velocity = 127;
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
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONSELECTUP)) {
				if (velocity == 127) {
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYLIST_CURSORUP);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONSELECTDOWN)) {
				if (velocity == 127) {
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.PLAYLIST_CURSORDOWN);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONSELECTLEFT)) {
				if (velocity == 127) {
					Mairlist.getInstance().sendCommandTCP(Mairlist.CommandTCP.CARTWALL_PREVPAGE);
				}
			} else if (midikey.equalsMIDIKey(MIDIKey.BUTTONSELECTRIGHT)) {
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

		public void close() {
		}
	}

	public void sendLED(MIDIKey midikey, int color) {

	}

	private class ledUpdater implements Runnable {

		@Override
		public void run() {
			while (true) {
				if (Mairlist.getInstance().getMairlistPlayerStatePlayer1() == MairlistPlayerState.EMPTY) {
					MIDIKey.BUTTONDOWN3.setLedActivated(false, 0); // aus
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer1() == MairlistPlayerState.LOADED) {
					MIDIKey.BUTTONDOWN3.setLedActivated(true, 0); // gr�n
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer1() == MairlistPlayerState.PLAYING) {
					MIDIKey.BUTTONDOWN3.setLedActivated(true, 0); // gelb
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer1() == MairlistPlayerState.STOPPED) {
					MIDIKey.BUTTONDOWN3.setLedActivated(false, 0); // aus
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer1() == MairlistPlayerState.EOF) {
					if (MidiController.getInstance().getCurrentTimestamp()
							- MidiController.getInstance().getTimestampEOF1() > 500) {
						if (MIDIKey.BUTTONDOWN3.isLedActivated()) {
							MIDIKey.BUTTONDOWN3.setLedActivated(false, 0); // aus
						} else {
							MIDIKey.BUTTONDOWN3.setLedActivated(true, 0); // rot
						}

						MidiController.getInstance()
								.setTimestampEOF1(MidiController.getInstance().getCurrentTimestamp());
					}
				}

				if (Mairlist.getInstance().getMairlistPlayerStatePlayer2() == MairlistPlayerState.EMPTY) {
					MIDIKey.BUTTONDOWN4.setLedActivated(false, 0); // aus
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer2() == MairlistPlayerState.LOADED) {
					MIDIKey.BUTTONDOWN4.setLedActivated(true, 0);
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer2() == MairlistPlayerState.PLAYING) {
					MIDIKey.BUTTONDOWN4.setLedActivated(true, 0);
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer2() == MairlistPlayerState.STOPPED) {
					MIDIKey.BUTTONDOWN4.setLedActivated(false, 0);
				} else if (Mairlist.getInstance().getMairlistPlayerStatePlayer2() == MairlistPlayerState.EOF) {
					if (MidiController.getInstance().getCurrentTimestamp()
							- MidiController.getInstance().getTimestampEOF2() > 500) {
						if (MIDIKey.BUTTONDOWN4.isLedActivated()) {
							MIDIKey.BUTTONDOWN4.setLedActivated(false, 0); // aus
						} else {
							MIDIKey.BUTTONDOWN4.setLedActivated(true, 0); // rot
						}

						MidiController.getInstance()
								.setTimestampEOF2(MidiController.getInstance().getCurrentTimestamp());
					}
				}
				
				if (MixingDesk.getInstance().isCartwallActive()) {
					MIDIKey.BUTTONDOWN5.setLedActivated(true, 0); //gelb
				} else {
					MIDIKey.BUTTONDOWN5.setLedActivated(true, 0); //gr�n
				}

				if (MairlistPFLSource.PLAYER1.isPFLActive()) {
					MIDIKey.BUTTONUP3.setLedActivated(true, 0); // rot
					MixingDesk.getInstance().setPflActive(true);
				} else {
					MIDIKey.BUTTONUP3.setLedActivated(false, 0); // aus
					if (!MairlistPFLSource.PLAYER2.isPFLActive() && !MairlistPFLSource.CARTWALL.isPFLActive()
							&& !MairlistPFLSource.EXTRA.isPFLActive() && !MairlistPFLSource.UNKNOWN.isPFLActive())
						MixingDesk.getInstance().setPflActive(false);
				}

				if (MairlistPFLSource.PLAYER2.isPFLActive()) {
					MIDIKey.BUTTONUP4.setLedActivated(true, 0); // rot
					MixingDesk.getInstance().setPflActive(true);
				} else {
					MIDIKey.BUTTONUP4.setLedActivated(false, 0); // aus
					if (!MairlistPFLSource.PLAYER1.isPFLActive() && !MairlistPFLSource.CARTWALL.isPFLActive()
							&& !MairlistPFLSource.EXTRA.isPFLActive() && !MairlistPFLSource.UNKNOWN.isPFLActive())
						MixingDesk.getInstance().setPflActive(false);
				}

				if (MairlistPFLSource.CARTWALL.isPFLActive()) {
					MIDIKey.BUTTONUP5.setLedActivated(true, 0); // rot
					MixingDesk.getInstance().setPflActive(true);
				} else {
					MIDIKey.BUTTONUP5.setLedActivated(false, 0); // aus
					if (!MairlistPFLSource.PLAYER1.isPFLActive() && !MairlistPFLSource.PLAYER2.isPFLActive()
							&& !MairlistPFLSource.EXTRA.isPFLActive() && !MairlistPFLSource.UNKNOWN.isPFLActive())
						MixingDesk.getInstance().setPflActive(false);
				}

				if (MairlistPFLSource.EXTRA.isPFLActive()) {
					MIDIKey.BUTTONSOLO.setLedActivated(true, 0); // rot
					MixingDesk.getInstance().setPflActive(true);
				} else {
					MIDIKey.BUTTONSOLO.setLedActivated(false, 0); // aus
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
					MIDIKey.BUTTONUP1.setLedActivated(true, 0); // rot
				} else {
					MIDIKey.BUTTONUP1.setLedActivated(false, 0); // aus
				}

				if (MixingDesk.getInstance().isSpeakingActive2()) {
					MIDIKey.BUTTONUP2.setLedActivated(true, 0); // rot
				} else {
					MIDIKey.BUTTONUP2.setLedActivated(false, 0); // aus
				}

				if (MixingDesk.getInstance().isMonitorMuted()) {
					MIDIKey.BUTTONMUTE.setLedActivated(true, 0); // rot
				} else {
					MIDIKey.BUTTONMUTE.setLedActivated(false, 0); // aus
				}
				
				if (MixingDesk.getInstance().isPhonesStdWiedergabe()) {
					MIDIKey.BUTTONDOWN7.setLedActivated(true, 0); // rot
					MIDIKey.BUTTONUP7.setLedActivated(false, 0); // aus
				} else {
					MIDIKey.BUTTONDOWN7.setLedActivated(false, 0); // rot
					MIDIKey.BUTTONUP7.setLedActivated(true, 0); // aus
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
