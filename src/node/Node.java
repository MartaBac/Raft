package node;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import messages.*;

public class Node implements Runnable {

	// Connection variables
	private int id;
	private String address = "127.0.0.1";
	private int port = 0;
	private String myFullAddress = address + ":" + port;
	private ArrayList<String> addresses = new ArrayList<String>(); // indirizzi altri nodi, o stream??

	// Receiver class
	private NodeReceiver receiver;

	// Raft variables
	private Role role = Role.FOLLOWER;
	private Log log = new Log();
	
	private Timer timer = new Timer("timer"+this.id);
	private ElectionTask electionTask = new ElectionTask(this);
	private HeartbeatTask heartbeatTask = new HeartbeatTask(this);
	private long electionTimeout = 0;
	private long heartbeatTimeout = Variables.heartbeat;

	private HashSet<String> voters = new HashSet<String>(); // totale voti ricevuti

	private String votedFor = null; // id nodo per cui ha votato
	private int currentTerm = 0;
	private int commitIndex = 0; // indice > fra i log, potrebbe essere ancora da committare
	private int lastApplied = 0; // indice dell'ultimo log applicato alla SM
	private HashMap<String, Integer> nextIndex = new HashMap<String, Integer>();
	private StateMachine sm = new StateMachine();

	// Costruttori
	public Node(int id, String address) {
		// Connessioni
		this.id = id;
		this.address = address;
		this.receiver = new NodeReceiver(this);
		this.setPort(this.receiver.getPort());
		this.myFullAddress = this.address + ":" + this.port;
	}

	private void setElectionTimeout() {
		this.stopElection();
		this.electionTask = new ElectionTask(this);
		double randomDouble = Math.random();
		this.electionTimeout = (long) (randomDouble * (Variables.maxRet - Variables.minRet)) + Variables.minRet;
		this.timer.schedule(this.electionTask, this.electionTimeout);
	}
	
	public void setElectionTimeout(int electionTimeout) {
		this.stopElection();
		this.electionTask = new ElectionTask(this);
		this.timer.schedule(this.electionTask, electionTimeout);
	}
	
	private void stopElection(){
		this.electionTask.cancel();
	}

	private void startHeartbeats() {
		AppendRequest heartbeat = new AppendRequest(this.currentTerm, this.myFullAddress, this.log.getDimension(), this.currentTerm - 1,
				this.commitIndex);
		this.sendBroadcast(heartbeat);
		this.stopHeartbeats();
		this.heartbeatTask = new HeartbeatTask(this);
		this.timer.schedule(this.heartbeatTask, 0, this.heartbeatTimeout);
	}
	
	public void stopHeartbeats(){
		this.heartbeatTask.cancel();
	}

	// Thread principale
	@Override
	public void run() {
		Thread receiverThread = new Thread(this.receiver);
		receiverThread.start();
		// Raft
		this.setRole(Role.FOLLOWER);
	}

	public void processMessage(Msg receivedValue) {
		if (receivedValue instanceof VoteRequest) {
			VoteRequest resp = (VoteRequest) receivedValue;

			if(!this.role.equals(Role.LEADER))
				this.setElectionTimeout();

			// Controllo se votare per lui o no
			if (resp.getTerm() >=  this.currentTerm) {
				// TODO: Check parametri
				// aggiorno term???
				if ((this.votedFor == null || this.votedFor.equals(resp.getIdAddress()))){
						//&& resp.getLastLogIndex() >= this.lastApplied) {
					this.sendMessage(new VoteResponse(this.currentTerm, true, this.myFullAddress), resp.getIdAddress());
					this.votedFor = resp.getIdAddress();
					return;
				}
			}
			this.sendMessage(new VoteResponse(this.currentTerm, false, this.myFullAddress), resp.getIdAddress());
			return;
		}
		if (receivedValue instanceof VoteResponse) {
			VoteResponse resp = (VoteResponse) receivedValue;
			
			if (!resp.isVoteGranted() && resp.getTerm() > this.currentTerm) {
				this.currentTerm = resp.getTerm();
				this.setRole(Role.FOLLOWER);
				return;
			}
			if(this.getRole().equals(Role.LEADER))
				return;
			if (resp.isVoteGranted()) 
				voters.add(resp.getIdAddress());
			// Controllo voti
			if (Math.ceil(((double) this.addresses.size() + 1) / 2) <= (this.voters.size() + 1)) {
				this.setRole(Role.LEADER);
			}
			return;
		}
		// Le ricevono candidate e follower (?)
		if (receivedValue instanceof AppendRequest) {
			AppendRequest resp = (AppendRequest) receivedValue;
			// heartbeat
			boolean b = false;
			if (resp.getEntry() == null) {		
				if (resp.getTerm() >= this.currentTerm) {
					this.votedFor = null;
					this.voters.clear();
					b = true;
					this.currentTerm = resp.getTerm();
					if(this.role != Role.FOLLOWER)
						this.setRole(Role.FOLLOWER);
					else{
						this.setElectionTimeout();
					}
				}
				
				AppendResponse hResponse = new AppendResponse(this.currentTerm, b);
				this.sendMessage(hResponse, resp.getLeaderId());
			}
			return;
		}
		// Le riceve solo il leader
		if (receivedValue instanceof AppendResponse){
			AppendResponse response = (AppendResponse) receivedValue;
			// Se ho ricevuto un false cado e torno follower
			if(!response.isSuccess()){
				this.setRole(Role.FOLLOWER);
			}
		}
		
		// TODO: client response
		if (receivedValue instanceof ClientRequest){
			ClientRequest response = (ClientRequest) receivedValue;
			// TODO: per ora rispondo un valore a caso, il raft prevede altri passaggi
			switch(response.getRequest()) {
			case "get":
				if (this.role.equals(role.LEADER)) {
					ClientResponse resp = new ClientResponse(new Entry("TEST"));
					this.sendMessage(resp, response.getAddress());
				} else {
					// Rispondo con l'indirizzo del leader
					ClientResponse resp = new ClientResponse(new Entry("Not leader"));
					this.sendMessage(resp, response.getAddress());
				}
				break;
			}
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
		} catch (Exception ex) {
			System.out.println("Connection error 002");
			return false;
		}
		return true;
	}

	public void sendBroadcast(Msg message) {
		for (String nodeAddress : this.addresses) {
			this.sendMessage(message, nodeAddress);
		}
	}
	
	public boolean addAddress(String address) {
		// Controllo che non mi sia passato il mio stesso indirizzo
		if (address.equals(this.address + ":" + this.port))
			return false;
		return this.addresses.add(address);
	}

	// Getter/Setter
	public void setRole(Role role) {
		this.voters.clear();
		System.out.println(this.myFullAddress + " changes role to " + role.toString());
		this.role = role;
		switch (role) {
		case FOLLOWER:
			this.setElectionTimeout();
			this.stopHeartbeats();
			break;
		case CANDIDATE:
			this.currentTerm++;
			this.voters.clear();
			this.votedFor = this.myFullAddress;
			this.setElectionTimeout();
			// TODO controllare correttezza parametri
			this.sendBroadcast(new VoteRequest(currentTerm, myFullAddress, commitIndex, currentTerm));
			break;
		case LEADER:
			// heartbeat
			// Disattivo timeoutElection
			this.stopElection();
			this.nextIndex.clear();
			for (String nodeAd : this.addresses) {
				// Controllare se è commitIndex or lastApplied
				this.nextIndex.put(nodeAd, this.commitIndex + 1);
			}
			this.startHeartbeats();
			break;
		default:
			break;
		}
	}

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

	public String getVotedFor() {
		return votedFor;
	}

	public String getAddress() {
		return address;
	}

	public ArrayList<String> getAddressesList() {
		return this.addresses;
	}

	public String getFullAddress() {
		return this.address + ":" + this.port;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public int getCurrentTerm() {
		return this.currentTerm;
	}
	
	public Log getLog() {
		return this.log;
	}
	
	public int getCommitIndex() {
		return this.commitIndex;
	}
	
	public void setHeartbeatTimeout(int time){
		this.heartbeatTimeout = time;
	}


}
