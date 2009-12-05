import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class ServerNode extends Node {

	
	private PacMan m_pacMan;
	private GameUI m_gameUI;
	private GameModel m_gameModel;
	Node[] nodes;
	Thread[] threads;

	byte[] ghostNewDirection = new byte[4];
	private int numOfClients = 0;
	
	public ServerNode (PacMan pacMan){
		super(); // init node
		//reference to all game models
		//m_pacManthis.ip = m_pacMan.appletIP;
		m_pacMan = pacMan;
		m_gameUI = pacMan.m_gameUI;
		m_gameModel = pacMan.m_gameModel;
		m_pacMan.controller = true;
		coord = true; //set this node as a coordinator
	}
	
	public void updateGhostPlayers (){
		for (int i=0; i<numOfClients; i++){
			m_gameModel.m_ghosts[i].m_requestedDirection = ghostNewDirection[i];
		}
	}
	
	public void connectToClients(int numOfClients){
			this.numOfClients = numOfClients;
			nodes = new Node[numOfClients];
			threads = new Thread[numOfClients];
			ServerWorker[] clients = new ServerWorker[numOfClients];
			for (int i=0; i<numOfClients; i++){
			acceptConnection(clients[i], i);
		}
	}

	private  void acceptConnection(ServerWorker sw, int ghostID) {
		//tcpSocket = serverSocket.accept();
		  try{
			  //serverSocket.accept();
			  Socket clientSocket = m_pacMan.serverSocket.accept();
			  String clientIP = clientSocket.getInetAddress().getHostAddress();
			  nodes[ghostID] = new Node(clientIP);
			  System.out.println(clientIP);
		    sw = new ServerWorker(clientSocket, this, ghostID);
		    Thread t = new Thread(sw);
		    threads[ghostID] = t;
		    //t.setPriority(Thread.MIN_PRIORITY);
		    t.start();
		  } catch (IOException e) {
		    System.out.println("Accept failed: 4444");
		    //System.exit(-1);
		  }
		m_pacMan.multiplayerActive = true;

		// start new game?
		m_gameModel.m_state = GameModel.STATE_NEWGAME;

	}
	
	public void setUpHosting() {
		m_pacMan.netMultiplayer = true;
		m_pacMan.controller = true;
		m_pacMan.playerIsGhost = false;

		
		try {
			//create a new server socket for accepting connections from slaves
			m_pacMan.serverSocket = new ServerSocket(m_pacMan.serverlistenPort);
			
			//Also initialize the update socket for SENDING
			m_pacMan.updateSocket = new MulticastSocket(m_pacMan.updateSendPort);

			// Oh. Here we do necessary UI changes
			// Then we accept the connection?
			m_gameUI.m_bShowHostingGame = true;

			m_gameUI.hostingIP = ip;
			m_gameUI.portNumber = Integer.toString(m_pacMan.serverSocket.getLocalPort());
			m_gameUI.m_bRedrawAll = true;

			m_gameModel.m_state = GameModel.STATE_HOSTING;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void clientFail(int ghostID){
		m_gameModel.m_ghosts[ghostID]=m_gameModel.ghostPlayerToAI2(m_gameModel.m_ghosts[ghostID]);
		System.out.println("ghostchange" + ghostID + m_gameModel.m_ghosts[ghostID].m_type);
		m_gameModel.fillThingArray();
		
	}
}
