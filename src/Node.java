import java.util.HashMap;

import javax.management.timer.Timer;

public class Node implements Runnable {
	private int id;
	private String indirizzo;
	private Role role;
	private Timer timer;
	private Log log = new Log();
	private int votedFor; // id nodo per cui ha votato
	private int currentTerm;
	private int commitIndex; //indice > fra i log, potrebbe essere ancora da committare
	private int lastApplied; //indice dell'ultimo log applicato alla SM
	private HashMap<Integer,Integer> nextIndex = new HashMap<Integer,Integer>();
	private StateMachine sm = new StateMachine();
	private String[] addresses; // indirizzi altri nodi, o stream??
	
	public Node(){
		
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void run() {
		Thread receiverThread = new Thread();
	}
	
}
