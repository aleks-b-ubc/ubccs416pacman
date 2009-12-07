import java.io.*;
import java.net.*;

public class ClientWorker implements Runnable {
	Socket tcpSocket;
	GameModel m_gameModel;
	PrintWriter out;
	BufferedReader in;
	Node clientNode;
	boolean threadRun = true;;

	public ClientWorker(GameModel m_gameModel, String hostIP, Node client) {
		try {
			this.m_gameModel = m_gameModel;
			tcpSocket = new Socket(hostIP, PacMan.serverlistenPort);
			in = new BufferedReader(new InputStreamReader(tcpSocket
					.getInputStream()));
			clientNode = client;

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		clientNode.serverFailed = false;
	}

	public void run() {
		while (threadRun) {
			try {
				
				if(clientNode.m_pacMan.controller){
					threadRun = false;
				}
				byte lastDirection = -1;
				byte newDirection;

				// tcpSocket.setKeepAlive(true);
				// tcpSocket.setSoTimeout(3000);

				out = new PrintWriter(tcpSocket.getOutputStream(), true);
				while (true) {
					Thread.sleep(1000 / 35);
					synchronized (m_gameModel) {
						newDirection = m_gameModel.m_player.m_requestedDirection;
					}
					if (lastDirection != newDirection) {
						lastDirection = newDirection;
						out.write(newDirection);
						out.flush();
					}
					// this checks if we get something in the TCP socket.
					// if it is a test message, we echo it immideatly.
					int test;
					if (!clientNode.serverFailed) {
						try {
							test = in.read();
							if (test == ServerWorker.TEST) {
								out.write(test);
								out.flush();
							}
						} catch (IOException e) {
							clientNode.serverFailed = true;
							clientNode.sendElect();

						}
					}
				}

			} catch (Exception e) {
				System.out.print("TCPClient:Whoops! It didn't work!\n");
			}

		}
	}

	protected void finalize() {
		// Objects created in run method are finalized when
		// program terminates and thread exits
		try {
			out.close();
			tcpSocket.close();
		} catch (IOException e) {
			System.out.println("Could not close socket");
			System.exit(-1);
		}
	}

}