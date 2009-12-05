import java.net.*;
import java.util.Enumeration;


public class Node {
	int thingID;
long id;
String ip;
boolean coord=false;
public Node(String ip){
	//java.net.NetworkInterface.
	this.ip = ip;
	//System.out.println(ip);
	this.id = Long.parseLong(removeChar(ip, '.'));
	//System.out.println(id);
}
public Node(){
		//java.net.NetworkInterface.
		this.ip = getIP();
		System.out.println(ip);
		this.id = Long.parseLong(removeChar(ip, '.'));
		//System.out.println(id);
}
	public String getIP(){
	     Enumeration<NetworkInterface> netInterfaces = null;
	        try {
	            netInterfaces = NetworkInterface.getNetworkInterfaces();
	        } catch (SocketException e) {
	            //log.error("Somehow we have a socket error...");
	        }

	        while (netInterfaces.hasMoreElements()) {
	            NetworkInterface ni = netInterfaces.nextElement();
	            Enumeration<InetAddress> address = ni.getInetAddresses();
	            while (address.hasMoreElements()) {
	                InetAddress addr = address.nextElement();
	                if (!addr.isLoopbackAddress() && !addr.isSiteLocalAddress()
	                        && !(addr.getHostAddress().indexOf(":") > -1)) {
	                    return addr.getHostAddress();
	                }
	            }
	        }
	        try {
	            return InetAddress.getLocalHost().getHostAddress();
	        } catch (UnknownHostException e) {
	            return "127.0.0.1";
	        }
	}


public static String removeChar(String s, char c) {
	   String r = "";
	   for (int i = 0; i < s.length(); i ++) {
	      if (s.charAt(i) != c) r += s.charAt(i);
	      }
	   return r;
	}

}
