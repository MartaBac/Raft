package node;
import java.util.ArrayList;
import java.util.HashMap;

import javax.management.timer.Timer;

public class Node implements Runnable {
	
	// Connection variables
	private int id;
	private String address = "127.0.0.1";
	private int port = 0;
	private ArrayList<String> addresses; // indirizzi altri nodi, o stream??
	
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
	
	// TODO: remove quando verranno implementati i messaggi correttamente
	private Msg lastMessage;
	
	// Costruttori	
	public Node(int id, String address){
		this.id = id;
		this.address = address;
		this.receiver = new NodeReceiver(this);
		this.role = Role.FOLLOWER;
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
	public boolean addAddress(String address) {
		// Controllo che non mi sia passato il mio stesso indirizzo
		if (address == this.address)
			return false;
		 return this.addresses.add(address);
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	//TODO: remove quando sarà fatta la distinzione fra i messaggi
	public void setValue(Msg message) {
		this.lastMessage = message;
	}
	
}
