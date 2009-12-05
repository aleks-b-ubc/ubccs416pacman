import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;


public class ClientNode extends Node{

	PacMan m_pacMan;
	GameModel m_gameModel;

	public ClientNode(PacMan pacMan){
		super();
		m_pacMan = pacMan;
		m_gameModel = pacMan.m_gameModel;
		m_pacMan.controller = false;
		coord = false;
	}
	
	public  void connectMultiplayerGame() {
		m_pacMan.netMultiplayer = true;
		m_pacMan.controller = false;
		m_pacMan.playerIsGhost = true;
		m_pacMan.multiplayerActive = true;
		
		try {
			//initialize the socket over which slave receive updates
		m_pacMan.updateSocket = new MulticastSocket(m_pacMan.updateListenPort);
		m_pacMan.updateSocket.joinGroup(InetAddress.getByName(m_pacMan.group));
	
		} catch (IOException e) {
			e.printStackTrace();
		}
		//once connections are opened, we start the game!
		m_gameModel.m_state = GameModel.STATE_NEWGAME;
		
		
		//initialize the socket over which slave send updates
		ClientWorker cw;
		  String hostIP = JOptionPane.showInputDialog(null, "Enter host's ip : ", 
				  "", 1);
		cw = new ClientWorker(m_gameModel ,hostIP);

	    Thread t2 = new Thread(cw);
	    t2.start();	
	}
	
	
}
