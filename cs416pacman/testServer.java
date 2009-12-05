import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class testServer {

	int numFail = 0;
	boolean failed = false;
	boolean gotAns = false;

	static int test = 11;
	int responce;
	static int FAILLIMIT = 35;
	int choice = 1;
	
	ServerSocket server;
	Socket client;

	BufferedReader in;
	BufferedWriter out;
	
	public testServer() throws IOException {
		server = new ServerSocket(4444);

		client = server.accept();

		in = new BufferedReader(new InputStreamReader(client
				.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(client
				.getOutputStream()));
		Scanner kbd = new Scanner(System.in);

		
		
		while(choice == 1){
			testConnection();
			System.out.println("Connection failed "+failed);
			
			System.out.println("To test connection press 1: ");
			choice = kbd.nextInt();	
		}

	}

	public void testConnection() {
		failed = false;
		numFail = 0;
		gotAns = false;
				
		while(!gotAns && (numFail < FAILLIMIT)){		
			try {
				responce = 0;
				System.out.println("sending");
				out.write(test);
				out.flush();
				
				responce = in.read();
			} catch (IOException e) {
				System.out.println("Error on send/read");
				numFail++;		
			}
			if(responce == test){
				failed = false;
				numFail = 0;
				gotAns = true;
			}
			else{			
				numFail++;
				if(numFail >= FAILLIMIT){
					failed = true;
				}
			}		
		}
		
		
	}

	public static void main(String[] args) throws IOException {
		testServer test = new testServer();
	}

}
