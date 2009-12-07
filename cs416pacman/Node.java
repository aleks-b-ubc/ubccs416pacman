import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

import javax.swing.JOptionPane;

public class Node {
	int thingID;
	int id;
	String ip;
	boolean coord = false;
	PacMan m_pacMan;
	GameModel m_gameModel;
	GameUI m_gameUI;
	Node[] nodes;
	Thread[] threads;

	byte[] ghostNewDirection = new byte[4];
	private int numOfClients = 0;

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
		coord = true; // set this node as a coordinator
		ip = getIP();
		id = getLastIpNumber(); // generate the id for this node based on the
								// last number
		// of the IP address

	}

	// update ghost players is run on the server to control
	// the ghost players on the screen.
	public void updateGhostPlayers() {
		for (int i = 0; i < numOfClients; i++) {
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
			m_pacMan.updateSocket = new MulticastSocket(m_pacMan.updateSendPort);
			
			
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
	public void connectToClients(int numOfClients) {
		this.numOfClients = numOfClients;
		nodes = new Node[numOfClients];
		threads = new Thread[numOfClients];
		ServerWorker[] clients = new ServerWorker[numOfClients];
		for (int i = 0; i < numOfClients; i++) {
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
		numOfClients--;

	}

	public synchronized void serverFailed() {
		System.out.println("Server Failed");
	}

	// this is the code run by the client.
	public void connectMultiplayerGame() {
		m_pacMan.netMultiplayer = true;
		m_pacMan.controller = false;
		m_pacMan.playerIsGhost = true;
		m_pacMan.multiplayerActive = true;

		try {
			// initialize the socket over which slave receive updates
			m_pacMan.updateSocket = new MulticastSocket(m_pacMan.updateListenPort);
			m_pacMan.updateSocket.joinGroup(InetAddress
					.getByName(m_pacMan.group));

		} catch (IOException e) {
			e.printStackTrace();
		}

		// initialize the socket over which slave send updates
		ClientWorker cw;
		String hostIP = JOptionPane.showInputDialog(null, "Enter host's ip : ",
				"", 1);
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

}
