import java.io.*;
import java.net.*;


public class ClientWorker implements Runnable{
	Socket tcpSocket;
	GameModel m_gameModel;
	PrintWriter out;
	BufferedReader in;
	
	boolean serverFailed;

	
	public ClientWorker(GameModel m_gameModel, String hostIP){
			try {
				this.m_gameModel = m_gameModel;
				tcpSocket = new Socket(hostIP, PacMan.serverlistenPort);
				in = new BufferedReader(new InputStreamReader(tcpSocket
						.getInputStream()));
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
	}

	public void run() {
		try{
			byte lastDirection = -1;
			byte newDirection;
		
			//tcpSocket.setKeepAlive(true);
			//tcpSocket.setSoTimeout(3000);
			
		   out = new PrintWriter(tcpSocket.getOutputStream(), true);
		   while (true)
		   {
			   Thread.sleep(1000/35);
			   synchronized(m_gameModel){newDirection = m_gameModel.m_player.m_requestedDirection;}
			   if (lastDirection != newDirection){
				   lastDirection = newDirection;
			   		System.out.println("sending: "+newDirection);
				    out.write(newDirection);
				    out.flush();  
			   }
			   //this checks if we get something in the TCP socket.
			   //if we do, we echo it immideatly. If we get an exception, that
			   //means the server has dropped out.
			   int test;
			   try {
					test = in.read();
					out.write(test);
					out.flush();
				} catch (IOException e) {
					System.out.println("SERVERFAILLLLLL");
					serverFailed = true;
				}
		   }
	 
		}catch(Exception e) {
	         System.out.print("TCPClient:Whoops! It didn't work!\n");
	      }
	
	}
	protected void finalize(){
		//Objects created in run method are finalized when 
		//program terminates and thread exits
		     try{
		    	 out.close();
		    	 tcpSocket.close();
		    } catch (IOException e) {
		        System.out.println("Could not close socket");
		        System.exit(-1);
		    }
		  }

}