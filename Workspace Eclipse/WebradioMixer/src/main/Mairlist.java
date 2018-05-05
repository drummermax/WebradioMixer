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
	
	//private boolean player1Loaded = false, player1Started = false, player1EOF = false, player2Loaded = false, player2Started = false, player2EOF = false, cartwall

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
		PLAYER1_START("PLAYER 1-1 START"), 
		PLAYER1_STOP("PLAYER 1-1 STOP"), 
		PLAYER1_STARTSTOP("PLAYER 1-1 START/STOP"), 
		PLAYER1_PFL_ON("PLAYER 1-1 PFL ON"), 
		PLAYER1_PFL_OFF("PLAYER 1-1 PFL OFF"), 
		PLAYER1_PFL_ONOFF("PLAYER 1-1 PFL ON/OFF"), 
		PLAYER2_START("PLAYER 1-2 START"), 
		PLAYER2_STOP("PLAYER 1-2 STOP"), 
		PLAYER2_STARTSTOP("PLAYER 1-2 START/STOP"), 
		PLAYER2_PFL_ON("PLAYER 1-2 PFL ON"), 
		PLAYER2_PFL_OFF("PLAYER 1-2 PFL OFF"), 
		PLAYER2_PFL_ONOFF("PLAYER 1-2 PFL ON/OFF"), 
		CARTWALL_START("CARTWALL 1 START"), 
		CARTWALL_STOP("CARTWALL 1 STOP"), 
		CARTWALL_STARTSTOP("CARTWALL 1 START/STOP"), 
		CARTWALL_MODEPFL("CARTWALL MODE PFL"), 
		CARTWALL_PREVPAGE("CARTWALL PREVIOUS PAGE"), 
		CARTWALL_NEXTPAGE("CARTWALL NEXT PAGE"), 
		PLAYLIST_CURSORUP("PLAYLIST 1 CURSOR UP"), 
		PLAYLIST_CURSORDOWN("PLAYLIST 1 CURSOR DOWN"), 
		PLAYLIST_EXTRAPFL("PLAYLIST 1 EXTRAPFL ON/OFF"),
		GUI_ONAIR("ON AIR"),
		GUI_OFFAIR("OFF AIR"),
		GUI_ONOFFAIR("ON/OFF AIR");

		private final String text;

		CommandTCP(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
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

				// data verarbeiten

				myclientSocket.close();

			} catch (IOException ex)

			{
			}
		}
	}
}
