import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class testClient {
	
	Socket recSocket;
	BufferedReader in;
	BufferedWriter out;

	public testClient() {
		try {
			recSocket = new Socket("localhost", 4444);
			in = new BufferedReader(new InputStreamReader(recSocket
					.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(recSocket
					.getOutputStream()));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int test;
		while(true){
			try {
				test = in.read();
				out.write(test);
				out.flush();
				System.out.println("Echoed");
			} catch (IOException e) {
				System.out.println("FUcked.");
			}
		}		
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) {
		
		testClient test = new testClient();
		
	}

}
