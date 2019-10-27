package node;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import messages.*;


public class Node implements Runnable {
	
	// Connection variables
	private int id;
	private String address = "127.0.0.1";
	private int port = 0;
	private String myFullAddress = address+":"+port;
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
			role = Role.CANDIDATE;
            System.out.println(role.toString() + " " + id+" Mando request for votes");
            for(String nodeAddress : addresses) {
            	// TODO: verificare che siano i parametri corretti
            	sendMessage(new VoteRequest(currentTerm, myFullAddress, commitIndex, currentTerm), nodeAddress);
            	System.out.println(id + " Send message to "+ nodeAddress);
            }
        }
	};
	
	private String votedFor = null; // id nodo per cui ha votato
	private int currentTerm = 0;
	private int commitIndex = 0; //indice > fra i log, potrebbe essere ancora da committare
	private int lastApplied = 0; //indice dell'ultimo log applicato alla SM
	private HashMap<Integer,Integer> nextIndex = new HashMap<Integer,Integer>();
	private StateMachine sm = new StateMachine();
	
	// Costruttori	
	public Node(int id, String address){
		// Connessioni
		this.id = id;
		this.address = address;
		this.receiver = new NodeReceiver(this);
        this.setPort(this.receiver.getPort());
        this.myFullAddress = this.address + ":" + this.port;
        
        // Raft
        this.setRole(Role.FOLLOWER);
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
		this.setElectionTimeout();
	}
	
	public boolean addAddress(String address) {
		// Controllo che non mi sia passato il mio stesso indirizzo
		if (address.equals(this.address+":"+this.port))
			return false;
		 return this.addresses.add(address);
	}
	
	public void processMessage(Msg receivedValue){
		if(receivedValue instanceof VoteRequest){
			//TODO
			//System.out.println(this.id+" reset timeout");
			//this.setElectionTimeout();
			this.sendMessage(new VoteResponse(this.currentTerm, true), ((VoteRequest) receivedValue).getIdAddress());
			this.votedFor = ((VoteRequest) receivedValue).getIdAddress();
			return;
		}
		if(receivedValue instanceof VoteResponse){
			//TODO
			return;
		}
		if(receivedValue instanceof AppendRequest){
			//TODO
			return;
		}
	}

	
	private boolean sendMessage(Msg msg, String address) {
		String[] split = address.split(":");
		String receiverAddress = split[0];
		int receiverPort = Integer.parseInt(split[1]);
		OutputStream os = null;			
        ObjectOutputStream oos = null;
        InetSocketAddress addr = new InetSocketAddress(receiverAddress, receiverPort);
        Socket s = new Socket();
        try {
            s.connect(addr);
            os = s.getOutputStream();			
            oos = new ObjectOutputStream(os);
            oos.writeObject(msg);
            oos.flush();
            s.close();
            System.out.println("Sent value [" + msg.toString() + "] to "
                    + address);
        } catch (Exception ex) {
            System.out.println("Connection error 002");
            return false;
        }
        return true;		
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

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getVotedFor() {
		return votedFor;
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
	
}
