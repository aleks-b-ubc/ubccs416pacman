import java.io.IOException;
import java.net.*;


public class BullyNode {
	boolean election=false;
	boolean wonElection = false;
	static final byte AYA = 0x15;
	static final byte IAA = 0x16;
	static final byte ELECT = 0x12;
	static final byte ANSWER = 0x13;
	static final byte COORD = 0x14;
	boolean isClient;
	PacMan pacMan;
	ClientNode clientNode;
	ServerNode serverNode;
	Node[] nodes;
	byte[] buf = new byte[256];
	private DatagramPacket packet;
	private DatagramSocket socket;
	private byte local_id;
	private byte id;
	private byte request;
	private boolean[] node_down = new boolean[4];
	private Object[] last_active;
	byte coord;
	private boolean answerd = true;
	byte myId;
	
	public BullyNode(PacMan pacMan, boolean isClient){
		this.isClient = isClient;
		this.pacMan = pacMan;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isClient){
			clientNode = new ClientNode(pacMan);
		}
	}
	public void runElection() throws IOException{
		socket.setSoTimeout(5000);// set for answer timeout -> im the coord
		packet = new DatagramPacket(buf, buf.length);
		 socket.receive(packet);
		 id = buf[0];
		 request = buf[1];		 
		switch (request){
		case AYA:
			buf[1] = local_id;
			buf[0] = IAA;
			packet = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
	        socket.send(packet);
			break;
		case IAA:
			//last_active[id] = ;
			//nodes[id].down = false;
			break;
		      case ELECT:
			if (id < local_id) {
			  buf[1] = local_id;
			  buf[0] = ANSWER;
			  packet = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
		        socket.send(packet);
			}
			break;
		 case ANSWER:
			 answerd =true;
			wonElection=false;
			break;

		      case COORD:
			coord = id;
			//send ok to coordinator
			election = false;
			break;
			
		    default: if (!answerd){
		    	// ive won
		    	coord = myId;
		    	sendCoord();
		    }
		    	
		      }
		
	}

	public void electMe() throws IOException{
		answerd=false;
		for(int i=0;i<4;i++){
			buf[0]=ELECT;
			buf[1]=myId;
			String ip = nodes[i].ip;
			int port = 8000+i;
			packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), port);
	        socket.send(packet);
		}
		//pacMan.m_gameModel.
		serverNode = new ServerNode(pacMan);
		clientNode = null;
	}
	
	public void sendCoord() throws IOException{
		answerd=false;
		for(int i=0;i<4;i++){
			buf[0]=COORD;
			buf[1]=myId;
			String ip = nodes[i].ip;
			int port = 8000+i;
			packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), port);
	        socket.send(packet);
		}
		//pacMan.m_gameModel.
		serverNode = new ServerNode(pacMan);
		clientNode = null;
	}
	
	//public boolean coordACK(){
	//}
		
	
	public void sendAya(String ip, int port) throws IOException{
		socket.setSoTimeout(200);
		buf[0]=AYA;
		packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), port);
        socket.send(packet);
	}
	
	public void sendIaa(String ip, int port) throws IOException{
		buf[0]=IAA;
		packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), port);
        socket.send(packet);
	}

}
