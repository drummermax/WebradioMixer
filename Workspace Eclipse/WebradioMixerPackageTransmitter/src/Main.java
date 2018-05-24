import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {

	public static void main(String[] args) throws IOException {
		Socket pingSocket = null;
        PrintWriter out = null;

        try {
            pingSocket = new Socket("127.0.0.1", 9001);
            out = new PrintWriter(pingSocket.getOutputStream(), true);
        } catch (IOException e) {
            return;
        }

        out.println(args[0] + "#" + System.currentTimeMillis());
        out.close();
        pingSocket.close();
	}
}