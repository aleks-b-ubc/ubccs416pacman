import java.io.*;
import java.net.*;

class ServerWorker implements Runnable {
  private Socket client;
  ServerNode serverNode;
  private int ghostID;
  
  
  
  BufferedReader in = null;
  BufferedWriter out = null;
  
  int test = 11;
  int responce;
  static int FAILLIMIT = 5;
  private int numFail = 0;
  private boolean clientFailed = false;
  
  private int timer = 0;
  private int timerLimit = 35;
  private boolean threadRun = true;

  
  ServerWorker(Socket client, ServerNode serverNode, int ghostID) {
	  //System.out.println(client.getInetAddress().getHostAddress());
   this.client = client;
   this.serverNode = serverNode;
   this.ghostID = ghostID;
   
   
   try{
     in = new BufferedReader(new InputStreamReader(client.getInputStream()));
     out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
     
   } catch (IOException e) {
     System.out.println("in or out failed");
     System.exit(-1);
   }
  }

  public void run(){
    byte b;

    while(threadRun){  
      try{
    	  //will keep track of inactivities. After a second if not reading anything
    	  //in we will test connection. If during that time ANYTHING comes in
    	  //the connection will be deemed alive.
    	  //else if after a conneciton test nothing. connection is dead.
    	  //client.setKeepAlive(true);
		  //client.setSoTimeout(1000);
    	  
    	  
    	  //inactivity increments timer
    	  timer++;
    	  if (in.ready()){
    		  b = (byte)in.read();
    		  System.out.println("RECEIVING: " + b);
    		  serverNode.ghostNewDirection[ghostID]=b;  
    		  //if we get something in, timer is reset.
    		  timer = 0;
    	  }
    	  
    	  if(timer >= timerLimit){
    		  testConnection();
    	  }
    	  Thread.sleep(1000/35);
    	  
      	} catch (SocketException e1) {
			System.out.println("TCP: client is not alive");
      	} catch (IOException e) {
      		System.out.println("Read failed");
      		System.exit(-1);
      	} catch (InterruptedException e) {
      		e.printStackTrace();
      	} 
    
    }
  }

private void testConnection() {
	clientFailed = false;
	numFail = 0;
	boolean gotAns = false;
			
	while(!gotAns && (numFail < FAILLIMIT)){		
		try {
			responce = -1;
			//System.out.println("sending");
			out.write(test);
			out.flush();
			
			responce = in.read();
		} catch (IOException e) {
			numFail++;		
		}
		//if we get a responce during that time, and it is either test, OR
		//one of the directions. Then connection is alive. If what we get is not
		//the test number, then it is a direction number and we set the direction.
		if(responce == test || responce == 0 || responce == 1
				|| responce == 2 || responce == 3 || responce == 4){
			if(responce != test){
				serverNode.ghostNewDirection[ghostID]= (byte) responce;
			}
			clientFailed = false;
			numFail = 0;
			gotAns = true;
		}
		else{			
			numFail++;
			if(numFail >= FAILLIMIT){
				System.out.println("CLIENTFAIL");
				clientFailed = true;
				serverNode.clientFail(ghostID);
				threadRun = false;
			}
		}		
	}
	
}
	
	
}