import java.net.*; // Contains Socket classes
import java.io.*; // Contains Input/Output classes

class Main {
	
	public enum MairlistPFLSource {
		PLAYER1(0), PLAYER2(1), CARTWALL(2), EXTRA(3);

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
	
	
	public static void main(String argv[]) throws IOException {
		
		MairlistPFLSource test = MairlistPFLSource.PLAYER1;
		
		test.setPFLActive(true);
		
		test = MairlistPFLSource.EXTRA;
		test = MairlistPFLSource.PLAYER1;
		
		System.out.println(test.EXTRA.isPFLActive());
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ServerSocket s = new ServerSocket(9001);

		System.out.println("Server waiting for client on port " + s.getLocalPort());
		int count = 0;
		do {
			count = count + 1;
			Socket connected = s.accept();
			new clientThread(connected, count).start();
		} while (true);

	}
}

class clientThread extends Thread {

	Socket myclientSocket = null;
	int mycount;
	DataInputStream is = null;
	PrintStream os = null;

	public clientThread(Socket clientSocket, int count) {
		this.myclientSocket = clientSocket;
		this.mycount = count;
	}

	public void run() {
		try {

			
			BufferedReader reader = new BufferedReader(new InputStreamReader(myclientSocket.getInputStream()));

			String data = reader.readLine();

			System.out.println(data);
			
			myclientSocket.close();
			  
			
		}catch(IOException ex)

	{
	}
}}