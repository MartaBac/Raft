package node;
import java.util.HashMap;

import javax.management.timer.Timer;

public class Node implements Runnable {
	
	// Connection variables
	private int id;
	private String address = "127.0.0.1";
	private int port = 0;
	private String[] addresses; // indirizzi altri nodi, o stream??
	
	// Receiver class
	private NodeReceiver receiver;
	
	// Raft variables
	private Role role;
	private Log log = new Log();
	
	private Timer electionTimer;
	
	private int votedFor; // id nodo per cui ha votato
	private int currentTerm;
	private int commitIndex; //indice > fra i log, potrebbe essere ancora da committare
	private int lastApplied; //indice dell'ultimo log applicato alla SM
	private HashMap<Integer,Integer> nextIndex = new HashMap<Integer,Integer>();
	private StateMachine sm = new StateMachine();
	
	// Costruttori
	public Node() throws Exception{
		throw new Exception("Node error 001: missing params");
	}
	
	public Node(int id, String address) throws Exception{
		this.id = id;
		this.address = address;
		this.receiver = new NodeReceiver(this);
        this.setPort(this.receiver.getPort());
	}
	
	// Thread principale
	@Override
	public void run() {
		Thread receiverThread = new Thread(this.receiver);
		receiverThread.start();
		
		// TODO: far partire i timer
	}
	
	// Getter/Setter
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
