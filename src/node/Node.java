package node;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;

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

	private Timer timer = new Timer("timer" + this.id);
	private ElectionTask electionTask = new ElectionTask(this);
	private HeartbeatTask heartbeatTask = new HeartbeatTask(this);
	private long electionTimeout = 0;
	private long heartbeatTimeout = Variables.heartbeat;

	private HashSet<String> voters = new HashSet<String>(); // totale voti ricevuti

	private String votedFor = null; // id nodo per cui ha votato
	private int currentTerm = 0;
	private int commitIndex = -1; // indice > fra i log, potrebbe essere ancora da committare
	private int lastApplied = -1; // indice dell'ultimo log applicato alla SM
	private HashMap<String, Integer> nextIndex = new HashMap<String, Integer>();
	private StateMachine sm = new StateMachine();
	private String leaderId = null;

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

	private void stopElection() {
		this.electionTask.cancel();
	}

	private void startHeartbeats() {
		AppendRequest heartbeat = new AppendRequest(this.currentTerm, this.myFullAddress, this.log.getDimension(),
				this.currentTerm - 1, this.commitIndex);
		this.sendBroadcast(heartbeat);
		this.stopHeartbeats();
		this.heartbeatTask = new HeartbeatTask(this);
		this.timer.schedule(this.heartbeatTask, 0, this.heartbeatTimeout);
	}

	public void stopHeartbeats() {
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
			// Controllo che se sono candidato e mi arriva un term più alto torno follower
			if(!this.role.equals(Role.FOLLOWER)) {
				if (this.currentTerm < resp.getTerm()) {
					this.setRole(Role.FOLLOWER);
				}
			} else {
				// Sono follower
				this.setElectionTimeout();
			}

			// Controllo se votare per lui o no
			if (resp.getTerm() >= this.currentTerm) {
				// TODO: Check parametri
				// aggiorno term???
				if ((this.votedFor == null || this.votedFor.equals(resp.getSender()))) {
					// && resp.getLastLogIndex() >= this.lastApplied) {
					this.sendMessage(new VoteResponse(this.currentTerm, true, this.myFullAddress), 
							resp.getSender());
					this.votedFor = resp.getSender();
					return;
				}
			}
			this.sendMessage(new VoteResponse(this.currentTerm, false, this.myFullAddress), 
					resp.getSender());
			return;
		}
		if (receivedValue instanceof VoteResponse) {
			VoteResponse resp = (VoteResponse) receivedValue;

			if (!resp.isVoteGranted() && resp.getTerm() > this.currentTerm) {
				this.currentTerm = resp.getTerm();
				this.setRole(Role.FOLLOWER);
				return;
			}
			if (this.getRole().equals(Role.LEADER))
				return;
			if (resp.isVoteGranted())
				voters.add(resp.getSender());
			// Controllo voti
			if (Math.ceil(((double) this.addresses.size() + 1) / 2) <= (this.voters.size() + 1)) {
				this.setRole(Role.LEADER);
			}
			return;
		}
		// Le ricevono candidate e follower (?)
		if (receivedValue instanceof AppendRequest) {
			AppendRequest resp = (AppendRequest) receivedValue;
			this.leaderId  = resp.getLeaderId();
			if(resp.getLeaderCommit()>this.commitIndex){
				// sono state committate delle entries
				if(this.log.getEntry(resp.getPrevLogIndex()).getTerm() == 
						resp.getPrevLogTerm()){
					// To check
					System.out.println("LEADER COMMIT 2: "+this.log.getEntry(resp.getPrevLogIndex()).getTerm());
					this.commitIndex = resp.getLeaderCommit();
					//int ind = Math.min(resp.getLeaderCommit(), resp.getPrevLogIndex());
					this.applyEntries(this.lastApplied, this.commitIndex);
				}
			}
			// heartbeat
			boolean b = false;
			if (resp.getEntry() == null) {
				if (resp.getTerm() >= this.currentTerm) {
					this.votedFor = null;
					this.voters.clear();
					b = true;
					this.currentTerm = resp.getTerm();
					if (this.role != Role.FOLLOWER)
						this.setRole(Role.FOLLOWER);
					else {
						this.setElectionTimeout();
					}
					// Controllo se committare 
				}

				AppendResponse hResponse = new AppendResponse(this.currentTerm, b, 
						this.myFullAddress);
				this.sendMessage(hResponse, resp.getLeaderId());
			}
			// AppendRequest ricevute dal leader
			else{
				AppendResponse appResponse;
				if(resp.getTerm() < this.currentTerm){
					// rispondo falso
					appResponse = new AppendResponse(this.currentTerm,false, 
							this.myFullAddress);
					this.sendMessage(appResponse, resp.getLeaderId());
					return;
				}
				
				Entry e = this.log.getEntry(resp.getPrevLogIndex());
				if(e.getCommand() == null && resp.getPrevLogIndex() >= 0){
					// rispondo false
					appResponse = new AppendResponse(this.currentTerm,false, 
							this.myFullAddress);
					this.sendMessage(appResponse, resp.getLeaderId());
					return;
				}
				if(e.getTerm()!= resp.getPrevLogTerm()){
				// rispondo false e sostituisco
					this.log.deleteFrom(resp.getPrevLogIndex());
					// risposta false
					appResponse = new AppendResponse(this.currentTerm,false,
							this.myFullAddress);
					this.sendMessage(appResponse, resp.getLeaderId());
					return;
				}
				// rispondo true dopo aver fatto l'append
				this.log.appendEntries(resp.getEntry(), resp.getPrevLogIndex() + 1);
				appResponse = new AppendResponse(this.currentTerm,true,
						this.myFullAddress);
				this.sendMessage(appResponse, resp.getLeaderId());
			}
			return;
		}
		// Le riceve solo il leader
		if (receivedValue instanceof AppendResponse) {
			AppendResponse response = (AppendResponse) receivedValue;
			if(response.isSuccess()){		
				this.nextIndex.put(response.getSender(), this.log.getDimension());
				// Controllo cosa posso committare
				
				ArrayList<Integer> comCount = new ArrayList<Integer>();
				for(Integer i: this.nextIndex.values()){
					comCount.add(i-1);
				}
				Collections.sort(comCount, Collections.reverseOrder());
				// La maggioranza sarebbe  + 1 ma sto cercando l'indice -> farei +1 -1
				int indexMajority = Math.floorDiv(comCount.size() + 1 , 2);
				// Cerco il maggiore committabile
				int committable = comCount.get(indexMajority);
				if(committable > this.commitIndex){
					System.out.println("commit committable "+ committable);
					// committo
					this.commitIndex = committable;
					// applico state machine
					this.applyEntries(this.lastApplied, committable);
				}
				
			} else {
				if (response.getTerm() > this.currentTerm) {
					this.currentTerm = response.getTerm();
					this.setRole(Role.FOLLOWER);
				} else {
					// Decremento nextIndex per quel follower perchè mi ha risposto
					// false nonostante fosse nel termine giusto
					int decrementedIndex = this.nextIndex.get(response.getSender())-1;
					this.nextIndex.put(response.getSender(), 
							decrementedIndex);
					// Mando appendRequest usando il nuovo index
					AppendRequest req = new AppendRequest(this.currentTerm, this.myFullAddress,
							decrementedIndex , this.log.getEntry(decrementedIndex).getTerm(), 
							this.log.getEntries(decrementedIndex), this.commitIndex);
					this.sendMessage(req, response.getSender());
					
				}
			}
		}

		// TODO: client response
		if (receivedValue instanceof ClientRequest) {
			ClientRequest response = (ClientRequest) receivedValue;
			if (!this.role.equals(Role.LEADER)) {
				ClientResponse resp = new ClientResponse(new Entry(this.leaderId));
				this.sendMessage(resp, response.getAddress());
				return;
			}
			switch (response.getRequest()) {
			case "get":
				// TODO: per ora rispondo un valore a caso, il raft prevede altri passaggi
				ClientResponse resp = new ClientResponse(new Entry("TEST"));
				this.sendMessage(resp, response.getAddress());
				break;
			case "set":
				// TODO: per ora appendo e basta, poi bisogna rispondere quando è stata
				// committata nel log
				// o rifiutata
				Entry e = new Entry(response.getParams().getCommand(),this.currentTerm);
			
				if(!this.log.appendEntry(e)){
					// rispondi false
					break;
				}
				
				AppendRequest req;
				// Invio delle appendRequest ai follower
				for(String toFollower : this.addresses){
					int indexF = this.nextIndex.get(toFollower);
					System.out.println("indice append " + indexF);
					System.out.println("term append " +  this.log.getEntry(indexF).getTerm());
					System.out.println("entry " + this.log.getEntry(indexF).toString());
					req = new AppendRequest(this.currentTerm, this.myFullAddress,
							indexF - 1 , this.log.getEntry(indexF - 1).getTerm(), 
							this.log.getEntries(indexF), this.commitIndex);
					this.sendMessage(req, toFollower);
				}
				break;
			default:
				// TODO: rifiutare clientReq
				break;
			}
		}
	}

	/**
	 * Chiama la funzione per applicare le entry alla state machine per ogni entry nel log
	 * che non è stata ancora applicata ma è stata committata.
	 * 
	 * @param lastCommitIndex rappresenta l'ultima entry applicata alla sm
	 * @param committable è l'indice dell'ultima entry da applicare
	 */
	private void applyEntries(int lastCommitIndex, int committable) {
		System.out.println("Last commit index: "+lastCommitIndex+ " Committable "+ committable);
		// applico alla state machine
		for( int i = lastCommitIndex+1; i <= committable; i++){
			Entry e = this.log.getEntry(i);
			this.sm.applyEntry(e);
			this.lastApplied = i;
		}		
	}

	public boolean sendMessage(Msg msg, String address) {
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
			this.leaderId = this.myFullAddress;
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

	public void setHeartbeatTimeout(int time) {
		this.heartbeatTimeout = time;
	}

	public StateMachine getStateMachine() {
		return this.sm;
	}
	
	public HashMap<String, Integer> getNextIndex() {
		return this.nextIndex;
	}
}
