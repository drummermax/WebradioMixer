package main;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Mairlist {
	private static Mairlist instance;

	private Robot robot;

	private Socket pingSocket = null;
	private PrintWriter out = null;

	private ServerSocket mairlistListenerServer = null;

	private static MairlistPlayerState mairlistPlayerStatePlayer1 = MairlistPlayerState.EMPTY,
			mairlistPlayerStatePlayer2 = MairlistPlayerState.EMPTY;
	private static MairlistPFLSource mairlistPFLSource = MairlistPFLSource.PLAYER1;

	public enum CommandShortcut {
		PLAYER1_STARTSTOP(1), PLAYER1_PFL_ONOFF(3), PLAYER2_STARTSTOP(2), PLAYER2_PFL_ONOFF(4), CARTWALL_MODEPFL(
				5), CARTWALL_PREVPAGE(6), CARTWALL_NEXTPAGE(7);

		private int shortkeyKeyEvent;

		CommandShortcut(int shortkeyNumber) {
			this.shortkeyKeyEvent = 0;

			switch (shortkeyNumber) {
			case 1:
				this.shortkeyKeyEvent = KeyEvent.VK_1;
				return;
			case 2:
				this.shortkeyKeyEvent = KeyEvent.VK_2;
				return;
			case 3:
				this.shortkeyKeyEvent = KeyEvent.VK_3;
				return;
			case 4:
				this.shortkeyKeyEvent = KeyEvent.VK_4;
				return;
			case 5:
				this.shortkeyKeyEvent = KeyEvent.VK_5;
				return;
			case 6:
				this.shortkeyKeyEvent = KeyEvent.VK_6;
				return;
			case 7:
				this.shortkeyKeyEvent = KeyEvent.VK_7;
				return;
			}
		}

		public int getShortkeyKeyEvent() {
			return shortkeyKeyEvent;
		}
	}

	public enum CommandTCP {
		PLAYER1_START("PLAYER 1-1 START"), PLAYER1_STOP("PLAYER 1-1 STOP"), PLAYER1_STARTSTOP(
				"PLAYER 1-1 START/STOP"), PLAYER1_PFL_ON("PLAYER 1-1 PFL ON"), PLAYER1_PFL_OFF(
						"PLAYER 1-1 PFL OFF"), PLAYER1_PFL_ONOFF("PLAYER 1-1 PFL ON/OFF"), PLAYER2_START(
								"PLAYER 1-2 START"), PLAYER2_STOP("PLAYER 1-2 STOP"), PLAYER2_STARTSTOP(
										"PLAYER 1-2 START/STOP"), PLAYER2_PFL_ON("PLAYER 1-2 PFL ON"), PLAYER2_PFL_OFF(
												"PLAYER 1-2 PFL OFF"), PLAYER2_PFL_ONOFF(
														"PLAYER 1-2 PFL ON/OFF"), CARTWALL_START(
																"CARTWALL 1 START"), CARTWALL_STOP(
																		"CARTWALL 1 STOP"), CARTWALL_STARTSTOP(
																				"CARTWALL 1 START/STOP"), CARTWALL_MODEPFL(
																						"CARTWALL MODE PFL"), CARTWALL_PREVPAGE(
																								"CARTWALL PREVIOUS PAGE"), CARTWALL_NEXTPAGE(
																										"CARTWALL NEXT PAGE"), PLAYLIST_CURSORUP(
																												"PLAYLIST 1 CURSOR UP"), PLAYLIST_CURSORDOWN(
																														"PLAYLIST 1 CURSOR DOWN"), PLAYLIST_EXTRAPFL(
																																"PLAYLIST 1 EXTRAPFL ON/OFF"), GUI_ONAIR(
																																		"ON AIR"), GUI_OFFAIR(
																																				"OFF AIR"), GUI_ONOFFAIR(
																																						"ON/OFF AIR");

		private final String text;

		CommandTCP(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	public enum MairlistPlayerState {
		EMPTY(0), LOADED(1), PLAYING(2), STOPPED(3), EOF(4), ACTIVATED(5), DEACTIVATED(6), PHONES_STD(7), PHONES_MASTER(
				8);

		private final int state;

		MairlistPlayerState(int state) {
			this.state = state;
		}

		public int getState() {
			return state;
		}
	}

	public enum MairlistPFLSource {
		PLAYER1(0), PLAYER2(1), CARTWALL(2), EXTRA(3), UNKNOWN(4);

		private final int state;
		private boolean PFLActive = false;

		MairlistPFLSource(int state) {
			this.state = state;
		}

		public int getState() {
			return state;
		}

		public boolean isPFLActive() {
			return PFLActive;
		}

		public void setPFLActive(boolean PFLActive) {
			this.PFLActive = PFLActive;
		}
	}

	public static Mairlist getInstance() {
		if (instance == null) {
			instance = new Mairlist();
		}
		return instance;
	}

	private Mairlist() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		try {
			pingSocket = new Socket("127.0.0.1", 9000);
			out = new PrintWriter(pingSocket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("mAirList Server not reachable!");
		}

		try {
			mairlistListenerServer = new ServerSocket(9001);
		} catch (IOException e) {
			System.out.println("mAirList Listener Server not openable!");
		}

		Thread mairlistListenerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				do {
					Socket connected = null;
					try {
						connected = mairlistListenerServer.accept();
					} catch (IOException e) {
						System.out.println("mAirList Listener Server doesn't work!");
					}
					new mairlistListener(connected).start();
				} while (true);
			}
		});
		mairlistListenerThread.start();
	}

	public void sendCommandShortcut(CommandShortcut commandShortcut) {
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(KeyEvent.VK_SHIFT);
		robot.keyPress(commandShortcut.getShortkeyKeyEvent());

		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyRelease(KeyEvent.VK_ALT);
		robot.keyRelease(KeyEvent.VK_SHIFT);
		robot.keyRelease(commandShortcut.getShortkeyKeyEvent());
	}

	public void sendCommandTCP(CommandTCP commandTCP) {
		sendText(commandTCP.getText());
	}

	private void sendText(String text) {
		out.println(text);
	}

	public void close() {
		out.close();
		try {
			pingSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MairlistPlayerState getMairlistPlayerStatePlayer1() {
		return mairlistPlayerStatePlayer1;
	}

	public MairlistPlayerState getMairlistPlayerStatePlayer2() {
		return mairlistPlayerStatePlayer2;
	}

	public MairlistPFLSource getMairlistPFLSource() {
		return mairlistPFLSource;
	}

	public void setMairlistPFLSource(MairlistPFLSource mairlistPFLSource) {
		Mairlist.mairlistPFLSource = mairlistPFLSource;
	}

	class mairlistListener extends Thread {

		Socket myclientSocket = null;
		DataInputStream is = null;
		PrintStream os = null;

		public mairlistListener(Socket clientSocket) {
			this.myclientSocket = clientSocket;
		}

		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(myclientSocket.getInputStream()));

				String data = reader.readLine();
				
				System.out.println("MAIRLIST RETURN: " + data);

				if (data.contains("LOADED")) {
					if (data.split(" ")[1].equals("0")) {
						mairlistPlayerStatePlayer1 = MairlistPlayerState.LOADED;
					} else if (data.split(" ")[1].equals("1")) {
						mairlistPlayerStatePlayer2 = MairlistPlayerState.LOADED;
					}
				} else if (data.contains("EMPTY")) {
					if (data.split(" ")[1].equals("0")) {
						mairlistPlayerStatePlayer1 = MairlistPlayerState.EMPTY;
					} else if (data.split(" ")[1].equals("1")) {
						mairlistPlayerStatePlayer2 = MairlistPlayerState.EMPTY;
					}
				} else if (data.contains("PLAYING")) {
					if (data.split(" ")[1].equals("0")) {
						mairlistPlayerStatePlayer1 = MairlistPlayerState.PLAYING;
					} else if (data.split(" ")[1].equals("1")) {
						mairlistPlayerStatePlayer2 = MairlistPlayerState.PLAYING;
					}
				} else if (data.contains("STOP")) {
					if (data.split(" ")[1].equals("0")) {
						mairlistPlayerStatePlayer1 = MairlistPlayerState.STOPPED;
					} else if (data.split(" ")[1].equals("1")) {
						mairlistPlayerStatePlayer2 = MairlistPlayerState.STOPPED;
					}
				} else if (data.contains("EOF")) {
					if (data.split(" ")[1].equals("0")) {
						mairlistPlayerStatePlayer1 = MairlistPlayerState.EOF;
					} else if (data.split(" ")[1].equals("1")) {
						mairlistPlayerStatePlayer2 = MairlistPlayerState.EOF;
					}
				} else if (data.contains("PFL")) {
					if (data.split(" ")[1].equals("OFF")) {
						mairlistPFLSource.setPFLActive(false);
						mairlistPFLSource = MairlistPFLSource.UNKNOWN;
					} else if (data.split(" ")[1].equals("ON")) {
						mairlistPFLSource.setPFLActive(true);
						mairlistPFLSource = MairlistPFLSource.UNKNOWN;
					}
				}

				myclientSocket.close();
			} catch (IOException ex)

			{
			}
		}
	}
}
