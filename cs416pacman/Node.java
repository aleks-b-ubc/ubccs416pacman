import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Enumeration;

import javax.swing.JOptionPane;

public class Node {
	int ghostID;
	int id;
	String ip;
	PacMan m_pacMan;
	GameModel m_gameModel;
	GameUI m_gameUI;
	Node[] nodes;
	Thread[] threads;
	boolean serverFailed;

	byte[] ghostNewDirection = new byte[4];
	
	int ansCounter;


	public Node(String ipAddress) {
		// java.net.NetworkInterface.
		ip = ipAddress;
		// System.out.println(ip);
		id = getLastIpNumber();
		// System.out.println(id);
	}

	// Creates a new node that knows about the game
	public Node(PacMan pacMan, boolean controller) {

		m_pacMan = pacMan;
		m_gameUI = pacMan.m_gameUI;
		m_gameModel = pacMan.m_gameModel;
		m_pacMan.controller = controller;
		ip = getIP();
		id = getLastIpNumber(); // generate the id for this node based on the
								// last number
		// of the IP address

	}

	// update ghost players is run on the server to control
	// the ghost players on the screen.
	public void updateGhostPlayers() {
		for (int i = 0; i < m_pacMan.numOfClients; i++) {
			m_gameModel.m_ghosts[i].m_requestedDirection = ghostNewDirection[i];
		}
	}

	// this sets up hosting. First thing that is done after person presses
	// the H button.
	public void setUpHosting() {
		m_pacMan.netMultiplayer = true;
		m_pacMan.controller = true;
		m_pacMan.playerIsGhost = false;

		try {
			// create a new server socket for accepting connections from slaves
			m_pacMan.serverSocket = new ServerSocket(m_pacMan.serverlistenPort);

			//If the update socket is not null, we are converting from client to 
			//server. So, first we dissconnect, then we reconnect.
			if(m_pacMan.updateSocket != null){	
				m_pacMan.updateSocket.disconnect();
			}	
			m_pacMan.updateSocket = new MulticastSocket(m_pacMan.updatePort);
			
			
			// Oh. Here we do necessary UI changes
			// but only if we are setting up connection. Else we might be playing
			if(m_gameModel.m_state == GameModel.STATE_SET_UP_CONNECTION){
				m_gameUI.m_bShowHostingGame = true;
			}

			m_gameUI.hostingIP = getIP();
			m_gameUI.portNumber = Integer.toString(m_pacMan.serverSocket
					.getLocalPort());
			m_gameUI.m_bRedrawAll = true;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// This is the next step after setting up connection. We wait for client to
	// connect
	// and accept their connections
	public void connectToClients() {
		nodes = new Node[m_pacMan.numOfClients];
		threads = new Thread[m_pacMan.numOfClients];
		ServerWorker[] clients = new ServerWorker[m_pacMan.numOfClients];
		for (int i = 0; i < m_pacMan.numOfClients; i++) {
			acceptConnection(clients[i], i);
		}
	}

	// we actually accept the connections
	private void acceptConnection(ServerWorker sw, int ghostID) {
		// tcpSocket = serverSocket.accept();
		try {
			// serverSocket.accept();
			Socket clientSocket = m_pacMan.serverSocket.accept();
			String clientIP = clientSocket.getInetAddress().getHostAddress();
			nodes[ghostID] = new Node(clientIP);
			System.out.println(clientIP);
			sw = new ServerWorker(clientSocket, this, ghostID);
			Thread t = new Thread(sw);
			threads[ghostID] = t;
			// t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		} catch (IOException e) {
			System.out.println("Accept failed: 4444");
			// System.exit(-1);
		}
		m_pacMan.multiplayerActive = true;

	}

	public synchronized void clientFail(int ghostID) {
		m_gameModel.m_ghosts[ghostID] = m_gameModel
				.ghostPlayerToAI2(m_gameModel.m_ghosts[ghostID]);
		m_gameModel.fillThingArray();
		m_pacMan.numOfClients--;

	}

	//this sends out the elect message
	

	public void pauseGame() {
		if (m_gameModel.m_state == GameModel.STATE_GAMEOVER)
			return;
		else {
			if(m_gameModel.m_state != GameModel.STATE_PAUSED){
				m_gameModel.m_pausedState = m_pacMan.m_gameModel.m_state;
				m_gameModel.m_state = GameModel.STATE_PAUSED;
			}
		}
		
	}
	public void unpauseGame(){
		if (m_gameModel.m_state == GameModel.STATE_PAUSED) {
			m_gameModel.m_state = m_pacMan.m_gameModel.m_pausedState;
			m_gameUI.m_bDrawPaused = false;
			m_gameUI.m_bRedrawAll = true;
		} 
	}

	// this is the code run by the client.
	public void connectMultiplayerGame(String hostIP) {
		m_pacMan.netMultiplayer = true;
		m_pacMan.controller = false;
		m_pacMan.playerIsGhost = true;
		m_pacMan.multiplayerActive = true;

		try {
			// initialize the socket over which slave receive updates
			m_pacMan.updateSocket = new MulticastSocket(m_pacMan.updatePort);
			m_pacMan.updateSocket.joinGroup(InetAddress
					.getByName(m_pacMan.group));

		} catch (IOException e) {
			e.printStackTrace();
		}

		// initialize the socket over which slave send updates
		ClientWorker cw;
		cw = new ClientWorker(m_gameModel, hostIP, this);

		Thread t2 = new Thread(cw);
		t2.start();
	}

	private String getIP() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return "127.0.0.1";
		}
	}

	private int getLastIpNumber() {
		String[] temp = ip.split("\\.");
		return Integer.parseInt(temp[3]);

	}
public synchronized void sendElect() {
		
		//PAUSE THE GAME!!!
		pauseGame();
		
		System.out.println("Sent elect, ID: "+id);
		PacmanDataPacket elect = new PacmanDataPacket(PacmanDataPacket.TYPE_ELECT, id);
		try{
			ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outBuffer);
			out.writeObject(elect);
			out.close();
        
        //sending a datagram packet to the multicast group
			DatagramPacket packet = new DatagramPacket(outBuffer.toByteArray(), outBuffer.toByteArray().length,
                        InetAddress.getByName(m_pacMan.group), m_pacMan.updatePort);
			m_pacMan.updateSocket.send(packet);
		}catch (IOException e) {
			System.out.println("Well, we failed sending elect");
		}
		
		ansCounter = 0;
	}

	public void sendAns(int replyID) {
		System.out.println("Sent ans, reply ID: "+replyID);
		
		PacmanDataPacket ans = new PacmanDataPacket(PacmanDataPacket.TYPE_ANS, replyID);
		try{
			ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outBuffer);
			out.writeObject(ans);
			out.close();
        
        //sending a datagram packet to the multicast group
			DatagramPacket packet = new DatagramPacket(outBuffer.toByteArray(), outBuffer.toByteArray().length,
                        InetAddress.getByName(m_pacMan.group), m_pacMan.updatePort);
			m_pacMan.updateSocket.send(packet);
		}catch (IOException e) {
			System.out.println("Well, we failed sending ans");
		}
		
	}

	public void sendCoord() {
		System.out.println("Sent coord with ip " +ip);
		PacmanDataPacket coord = new PacmanDataPacket(PacmanDataPacket.TYPE_ANS, ip);
		try{
			ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outBuffer);
			out.writeObject(coord);
			out.close();
        
			//sending a datagram packet to the multicast group
			DatagramPacket packet = new DatagramPacket(outBuffer.toByteArray(), outBuffer.toByteArray().length,
                        InetAddress.getByName(m_pacMan.group), m_pacMan.updatePort);
			m_pacMan.updateSocket.send(packet);
		}catch (IOException e) {
			System.out.println("Well, we failed sending ans");
		}
		
	}

}
