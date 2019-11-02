import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import node.Node;
import node.Role;

public class Launcher {
	private static final int nNodes = 5;
	private static String ip = null;
	private static HashMap<String, Node> listaNodi = new HashMap<String, Node>();

	public static void main(String[] args) throws Exception {
		System.out.println("INIZIO");
		Node t = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < nNodes; i++) {
			t = new Node(i, ip);
			listaNodi.put(ip + ":" + String.valueOf(t.getPort()), t);
		}

		Iterator<Node> it = listaNodi.values().iterator();
		while (it.hasNext()) {
			Node p = it.next();
			// Vengono mandati tutti gli indirizzi, anche di se stesso; verrà
			// poi scartato
			listaNodi.forEach((k, v) -> p.addAddress(v.getFullAddress()));
		}

		// Run nodes
		for (Map.Entry<String, Node> entry : listaNodi.entrySet()) {
			Thread thread = new Thread(entry.getValue());
			thread.start();
		}

		// Main cycle
		boolean exit = false;
		while (!exit) {
			System.out.println("Elenco nodi in memoria (numero porta):");
			listaNodi.forEach((k, v) -> System.out.println(k.split(":")[1]));

			// Gestione dati acquisiti dalla scrittura su console
			Scanner in = new Scanner(System.in);
			String s = in.nextLine();
			
			// Se viene inserito qualcosa da tastiera
			commandTranslation(s);
			
			int leader = 0;
			for (Map.Entry<String, Node> entry : listaNodi.entrySet()) {
				String key = entry.getKey();
				Node node = entry.getValue();
				// System.out.println(" "+key + " " + node.getRole());
				if (node.getRole().equals(Role.LEADER))
					leader++;
			}
			if (leader > 1)
				System.exit(-1);
			// System.out.println("");
		}
	}

	public static boolean isNumeric(String strNum) {
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException | NullPointerException nfe) {
			return false;
		}
		return true;
	}
	
	/**
	 * Funzione per smistare/tradurre in metodi ciò che l'utente scrive in console.
	 * Operazioni supportate:
	 * 'set electionTimeout portNumber int' <- con portNumber una delle port stampate 
	 * 		dal pogramma e int un intero a cui settare il timeout(in ms)
	 * 'stop heartbeats' <- per far smettere al leader di mandare heartbeats, ciò porterò
	 * 		al cambio di leader
	 * @param s Stringa inserita dall'utente
	 */
	public static void commandTranslation(String s){
		String[] split = s.split(" ");
		switch (split[0]) {
		case "set":
			switch (split[1]) {
			case "electionTimeout":
				if (split.length >= 4 && isNumeric(split[3]) && listaNodi.containsKey(ip + ":" + split[2])) {
					System.out.println("setting election timeout of " + split[2] + " to " + split[3]);
					listaNodi.get(ip + ":" + split[2]).setElectionTimeout(Integer.valueOf(split[3]));
				} else {
					System.err.println("Error: invalid input in election timeout");
				}
				break;
			
			default:
				System.err.println("Error: invalid input set");
				break;
			}
			break;
		case "stop" :
			switch (split[1]) {
			case "heartbeats":
				Iterator<Map.Entry<String, Node>> ite = listaNodi.entrySet().iterator();
				while (ite.hasNext()) {
				    Node n = ite.next().getValue();
				    if(n.getRole().equals(Role.LEADER))
				    	n.stopHeartbeats();
				}
				break;
			default:
				break;
		}
			break;
		default:
			System.err.println("Error: invalid input");
			break;
		}
	}
}
