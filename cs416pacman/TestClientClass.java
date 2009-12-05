import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

//THIS HAS THE TCP CONNECTION CODE

public class TestClientClass {

	JFrame f = new JFrame("Receiver Client");
	JTextArea text = new JTextArea();
	JPanel mainPanel = new JPanel();
	
	
	public TestClientClass() {

	    //Frame
	    f.addWindowListener(new WindowAdapter() {
	       public void windowClosing(WindowEvent e) {
		 System.exit(0);
	       }
	    });
   
	    //frame layout
	    mainPanel.setLayout(null);
	    mainPanel.add(text);

	    f.getContentPane().add(mainPanel, BorderLayout.CENTER);
	    f.setSize(new Dimension(390,370));
	    f.setVisible(true);

	  }
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		//TestClientClass theClient = new TestClientClass();
		int listenPort = 4444; // the port on which local machine is listening
		int updateSendPort = 5555; // the port on which local machine is sending updates
		int updateListenPort = 6666;
		

		//This is here to show that communication is working!
		PacmanDataPacket received;	
		
		PacmanDataPacket packetToSend = new PacmanDataPacket(11);
		/**
		
		
		Scanner kbd = new Scanner(System.in);
		System.out.println("To send press 1: ");
		int choice = kbd.nextInt();	
		
		//THIS HAS WORKING CODE TO SEND THE PACKMANPACKET!
		
		if(choice == 1){
			//make a server socket, and then accept incoming connection
			ServerSocket listeningSocket = new ServerSocket(listenPort);
			System.out.println(InetAddress.getLocalHost().getHostAddress());
			
			//hand it over the the main socket
			Socket socket = listeningSocket.accept();
			System.out.println(socket.getInetAddress().getHostAddress());
			
			//next create a object output stream that will use the byteArray stream
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			//write out object to the line!
			out.writeObject(packetToSend);
			
			packetToSend = new PacmanDataPacket(12);
			out.writeObject(packetToSend);
			
			out.close();
			
		}
		else{
		*/
		
		Socket receivingSocket = new Socket(InetAddress.getLocalHost(), listenPort);
		ObjectInputStream in = new ObjectInputStream(receivingSocket.getInputStream());
		//WORKING CODE FOR RECEIVING PACKMANPACKET
		System.out.println("Ready to receive.");
		while(true){
			try{
				received = (PacmanDataPacket) in.readObject();
				System.out.println(received.stateToString());
			}	
			catch(IOException e){
				
			}	
		}
	}
		
}
