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
	
	public static void main(String[] args) throws Exception{
		Node t = null;
		String z;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
			
		for(int i=0; i<nNodes;i++){
			t = new Node(i,ip);
			listaNodi.put(ip+":"+String.valueOf(t.getPort()),t);
		}
		z= ip+":"+String.valueOf(t.getPort());
		
		Iterator<Node> it = listaNodi.values().iterator();
		while (it.hasNext()) {
			Node p = it.next();
			// Vengono mandati tutti gli indirizzi, anche di se stesso; verrà poi scartato
		    listaNodi.forEach((k,v) -> p.addAddress(v.getFullAddress()));
		}
		
		// Run nodes
		for (Map.Entry<String, Node> entry : listaNodi.entrySet()) {
			Thread thread = new Thread(entry.getValue());
			thread.start();
		}
		
		// Main cycle
		boolean exit = false;
		while (!exit) {
			for(Map.Entry<String, Node> entry : listaNodi.entrySet() ) {
				String key = entry.getKey();
				Node node = entry.getValue();
				//System.out.println(key+" "+node.getRole());
			}
			//System.out.println("");
			
		}
		
	}
}
