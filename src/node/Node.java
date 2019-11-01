package node;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
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

	private Timer electionTimer = new Timer("electionTimer" + this.id);
	private long electionTimeout = 0;
	private Timer heartbeatTimer = new Timer("heartbeatTimer" + this.id);
	private long heartbeatTimeout = 0;

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
		this.electionTimer.cancel();
		this.electionTimer = new Timer("electionTimer" + this.id);
		double randomDouble = Math.random();
		this.electionTimeout = (long) (randomDouble * (Variables.maxRet - Variables.minRet)) + Variables.minRet;
		this.electionTimer.schedule(new TimerTask() {
			public void run() {
				setRole(Role.CANDIDATE);
			}
		}, this.electionTimeout);
	}

	private void startHeartbeats() {
		// TODO rimpiazzare this.currentTerm col Term dell'ultimo log nel log
		AppendRequest heartbeat = new AppendRequest(this.currentTerm, this.myFullAddress, this.log.getDimension(), this.currentTerm - 1,
				this.commitIndex);
		this.sendBroadcast(heartbeat);
		this.heartbeatTimer.cancel();
		this.heartbeatTimer = new Timer("heartbeatTimer" + this.id);
		this.heartbeatTimeout = Variables.heartbeat;
		this.heartbeatTimer.schedule(new TimerTask() {
			public void run() {
				// Spedisco heartbeat
				// TODO rimpiazzare this.currentTerm col Term dell'ultimo log nel log
				AppendRequest heartbeat = new AppendRequest(currentTerm, myFullAddress, log.getDimension(), currentTerm - 1,
						commitIndex);
				sendBroadcast(heartbeat);

			}
		}, this.heartbeatTimeout);
	}
	
	private void stopHeartbeats(){
		this.heartbeatTimer.cancel();
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
		System.out.println("to " + this.getId() +  " " + receivedValue.toString());
		if (receivedValue instanceof VoteRequest) {
			VoteRequest resp = (VoteRequest) receivedValue;

			//System.out.println(this.id + " reset timeout");
			this.setElectionTimeout();

			// Controllo se votare per lui o no
			if (resp.getTerm() >=  this.currentTerm) {
				// TODO: Check parametri
				// aggiorno term???
				if ((this.votedFor == null || this.votedFor.equals(resp.getIdAddress()))){
						//&& resp.getLastLogIndex() >= this.lastApplied) {
					System.out.println(this.id + " vote true to " + resp.getIdAddress() +
							" mio: " + this.currentTerm + " suo:" + resp.getTerm());
					this.sendMessage(new VoteResponse(this.currentTerm, true, this.myFullAddress), resp.getIdAddress());
					this.votedFor = resp.getIdAddress();
					return;
				}
			}
			System.out.println(this.id + " vote false to " + resp.getIdAddress());
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
			// System.out.println(this.id+ " sending message");
			s.connect(addr);
			os = s.getOutputStream();
			oos = new ObjectOutputStream(os);
			oos.writeObject(msg);
			oos.flush();
			s.close();
			// System.out.println(this.id + " sent message");
		} catch (Exception ex) {
			System.out.println("Connection error 002");
			return false;
		}
		return true;
	}

	private void sendBroadcast(Msg message) {
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
		System.out.println(this.id + " change role to " + role.toString());
		this.voters.clear();
		this.role = role;
		switch (role) {
		case FOLLOWER:
			this.setElectionTimeout();
			this.stopHeartbeats();
			System.out.println("++ follower" + this.getId());
			break;
		case CANDIDATE:
			System.out.println("-- candidate" + this.getId());
			this.currentTerm++;
			this.voters.clear();
			this.votedFor = this.myFullAddress;
			this.setElectionTimeout();
			System.out.println(id + " " + role.toString() + " mando request for votes");
			// TODO controllare correttezza parametri
			this.sendBroadcast(new VoteRequest(currentTerm, myFullAddress, commitIndex, currentTerm));
			break;
		case LEADER:
			// heartbeat
			// Disattivo timeoutElection
			System.out.println("++ leader" + this.getId());
			this.electionTimer.cancel();
			this.nextIndex.clear();
			for (String nodeAd : this.addresses) {
				// Controllare se è commitIndex or lastApplied
				this.nextIndex.put(nodeAd, this.commitIndex + 1);
			}
			this.startHeartbeats();
			break;
		default:
			System.out.println("Invalid role");
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

}
