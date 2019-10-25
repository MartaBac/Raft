import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import node.Node;


public class Launcher {
	private static final int nNodes = 5;
	private static String ip = null;
	private static HashMap<String, Node> listaNodi = new HashMap<String, Node>();
	
	public static void main() throws Exception{
		Node t;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
			
		for(int i=0; i<nNodes;i++){
			t = new Node(i,ip);
			listaNodi.put(ip+":"+String.valueOf(t.getPort()),t);
		}
		
		Iterator<Node> it = listaNodi.values().iterator();

		while (it.hasNext()) {
		    listaNodi.forEach((k,v) -> it.next().addAddress(v.getAddress()));
		}
		
	}
}
