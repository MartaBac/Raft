import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import node.Node;
import node.Role;
import node.Variables;
/**
 * Implementazione meccanismo di leader election del Raft protocol
 * 
 */
public class Launcher {
	private static String ip = null;
	private static HashMap<String, Node> listaNodi = new HashMap<String, Node>();

	public static void main(String[] args) throws Exception {
		System.out.println("[Main] Start");
		Node t = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.err.println("[Main] Could not get ip address");
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Creo nuovi nodi
		for (int i = 0; i < Variables.nNodes; i++) {
			t = new Node(i, ip);
			listaNodi.put(ip + ":" + String.valueOf(t.getPort()), t);
		}

		// Inoltro a tutti i nodi l'indirizzo degli altri
		Iterator<Node> it = listaNodi.values().iterator();
		while (it.hasNext()) {
			Node p = it.next();
			listaNodi.forEach((k, v) -> p.addAddress(v.getFullAddress()));
		}

		// Stampo elenco nodi in memoria
		printNodeList();
		
		// Faccio eseguire i nodi
		for (Map.Entry<String, Node> entry : listaNodi.entrySet()) {
			Thread thread = new Thread(entry.getValue());
			thread.start();
		}

		// Main
		boolean exit = false;
		Scanner in = new Scanner(System.in);
		while (!exit) {
			// Gestione dati acquisiti dalla scrittura su console
			String s = in.nextLine();
			exit = commandTranslation(s);	
		}
		in.close();
	}

	private static void printNodeList() {
		System.out.println("[Main] List of nodes:");
		listaNodi.forEach((k, v) -> System.out.println(" - " + k));
	}

	/**
	 * 
	 * @param strNum
	 * @return true se la String rappresenta un numero in cifre
	 */
	public static boolean isNumeric(String strNum) {
		try {
			Double.parseDouble(strNum);
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
	 * @return 
	 */
	public static boolean commandTranslation(String s){
		String[] split = s.split(" ");
		switch (split[0]) {
		case "get":
			switch (split[1]){
			case "nodes":
				printNodeList();
				break;
			default:
				System.err.println("[Main] Error: invalid input set");
				break;
			}
			break;
		case "set":
			switch (split[1]) {
			case "electionTimeout":
				if (split.length >= 4 && isNumeric(split[3]) && listaNodi.containsKey(split[2])) {
					System.out.println("[Main] Setting election timeout of " + split[2] + " to "
						+ split[3]);
					listaNodi.get(split[2]).setElectionTimeout(Integer.valueOf
							(split[3]));
				} else {
					System.err.println("[Main] Error: invalid input in election timeout");
				}
				break;
			
			default:
				System.err.println("[Main] Error: invalid input set");
				break;
			}
			break;
		case "stop" :
			switch (split[1]) {
			case "heartbeats":
				Iterator<Map.Entry<String, Node>> ite = listaNodi.entrySet().iterator();
				while (ite.hasNext()) {
				    Node n = ite.next().getValue();
				    if(n.getRole().equals(Role.LEADER)) {
				    	n.stopHeartbeats();
				    	System.out.println("[Main] Stopped heartbeat on "+n.getFullAddress());
				    }
				}
				break;
			default:
				break;
			}
			break;
		case "check":
			switch (split[1]) {
			case "state":
				System.out.println("[Main] State of " + split[2] + " is "+
						listaNodi.get(split[2]).getStateMachine().toString());
				break;
			case "log":
				System.out.println("[Main] Log of " + split[2] + " is "+
						listaNodi.get(split[2]).getLog().toString());
				break;
			case "term":
				System.out.println("[Main] Term of " + split[2] + " is "+
						listaNodi.get(split[2]).getCurrentTerm());
				break;
			default:
				System.err.println("[Main] Error: invalid input set");
				break;
			}
			break;
		default:
			System.err.println("[Main] Error: invalid input");
			break;
		}
		return false;
	}
}
