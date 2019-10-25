package node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class Node implements Runnable {
	
	// Connection variables
	private int id;
	private String address = "127.0.0.1";
	private int port = 0;
	private ArrayList<String> addresses = new ArrayList<String>(); // indirizzi altri nodi, o stream??

	// Receiver class
	private NodeReceiver receiver;
	
	// Raft variables
	private Role role;
	private Log log = new Log();
	
	private Timer electionTimer = new Timer("electionTimer");
	private long electionTimeout = 0;
	private TimerTask electionTask = new TimerTask() {
		public void run() {
			
            System.out.println("Ok");
        }
	};
	
	private String votedFor = null; // id nodo per cui ha votato
	private int currentTerm = 0;
	private int commitIndex = 0; //indice > fra i log, potrebbe essere ancora da committare
	private int lastApplied = 0; //indice dell'ultimo log applicato alla SM
	private HashMap<Integer,Integer> nextIndex = new HashMap<Integer,Integer>();
	private StateMachine sm = new StateMachine();
	
	// TODO: remove quando verranno implementati i messaggi correttamente
	private Msg lastMessage;
	
	// Costruttori	
	public Node(int id, String address){
		// Connessioni
		this.id = id;
		this.address = address;
		this.receiver = new NodeReceiver(this);
        this.setPort(this.receiver.getPort());
        // Raft
        this.setRole(Role.FOLLOWER);
        this.setElectionTimeout();
	}
	
	private void setElectionTimeout() {
		this.electionTimer.cancel();
		this.electionTimer = new Timer("electionTimer");
		double randomDouble = Math.random();
        this.electionTimeout =(long)(randomDouble * (Variables.maxRet-Variables.minRet)) + Variables.minRet;
        this.electionTimer.scheduleAtFixedRate(this.electionTask, 0, this.electionTimeout);
	}

	// Thread principale
	@Override
	public void run() {
		Thread receiverThread = new Thread(this.receiver);
		receiverThread.start();
		// Election timer
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
		if (address.equals(this.address+":"+this.port))
			return false;
		 return this.addresses.add(address);
	}
	public String getAddress() {
		return address;
	}
	
	public ArrayList<String> getAddressesList() {
		return this.addresses;
	}
	
	public String getFullAddress(){
		return this.address+":"+this.port;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	//TODO: remove quando sarà fatta la distinzione fra i messaggi
	public void setValue(Msg message) {
		this.lastMessage = message;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
	
}
