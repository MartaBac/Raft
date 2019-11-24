package node;

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
	private ArrayList<String> addresses = new ArrayList<String>();

	// Receiver class
	private NodeLinker linker;

	// Raft variables
	private Role role = Role.FOLLOWER;
	private Log log = new Log();
	private HashSet<String> voters = new HashSet<String>(); // Elenco di chi l'ha votato
	private String votedFor = null; 
	private int currentTerm = 0;
	private int commitIndex = -1; 
	private int lastApplied = -1; 
	private HashMap<String, Integer> nextIndex = new HashMap<String, Integer>();
	private StateMachine sm = new StateMachine();
	private String leaderId = null;

	// Timers
	private Timer timer = new Timer("timer" + this.id);
	private ElectionTask electionTask = new ElectionTask(this);
	private HeartbeatTask heartbeatTask = new HeartbeatTask(this);
	private long electionTimeout = 0;
	private long heartbeatTimeout = Variables.heartbeat;

	// Costruttori
	public Node(int id, String address) {
		this.id = id;
		this.address = address;
		this.linker = new NodeLinker(this);
		this.setPort(this.linker.getPort());
		this.myFullAddress = this.address + ":" + this.port;
	}

	// Thread principale
	@Override
	public void run() {
		Thread receiverThread = new Thread(this.linker);
		receiverThread.start();
		this.setRole(Role.FOLLOWER);
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
		this.stopHeartbeats();
		this.heartbeatTask = new HeartbeatTask(this);
		this.timer.schedule(this.heartbeatTask, 0, this.heartbeatTimeout);
	}

	public void stopHeartbeats() {
		this.heartbeatTask.cancel();
	}

	/**
	 * Gestione dei diversi tipi di messaggi in ricezione
	 * 
	 * @param receivedValue
	 */
	public void processMessage(Msg receivedValue) {
		if (receivedValue instanceof VoteRequest) {
			VoteRequest resp = (VoteRequest) receivedValue;
			this.handleVoteRequest(resp);
			return;
		}
		if (receivedValue instanceof VoteResponse) {
			VoteResponse resp = (VoteResponse) receivedValue;
			this.handleVoteResponse(resp);
			return;
		}
		if (receivedValue instanceof AppendRequest) {
			AppendRequest resp = (AppendRequest) receivedValue;
			this.handleAppendRequest(resp);
			return;
		}
		if (receivedValue instanceof AppendResponse) {
			AppendResponse response = (AppendResponse) receivedValue;
			this.handleAppendResponse(response);
			return;
		}

		if (receivedValue instanceof ClientRequest) {
			ClientRequest response = (ClientRequest) receivedValue;
			this.handleClientRequest(response);
			return;
		}
		return;
	}

	/**
	 * Gestisce le AppendRequest: controllo se il commitIndex mio è inferiore a
	 * quello del leader, e in caso lo sia vuol dire che ho degli elementi da
	 * committare e quindi devo aggiornare il mio index e applicare le entries alla
	 * state machine. In caso l'append contenga una Entry null -> è un heartbeat, in
	 * esso verranno resettati i timer, eventualmente aggiornati i term e, se un non
	 * follower riceve un heartbeat con term più alto del proprio, cade e diventa
	 * follower. Vengono gestite anche le append contenenti effettivamente entries
	 * 
	 * @param resp
	 */
	private void handleAppendRequest(AppendRequest resp) {
		this.leaderId = resp.getLeaderId();
		if (resp.getLeaderCommit() > this.commitIndex) {
			// Sono state committate delle entries
			if (this.log.getEntry(resp.getPrevLogIndex()).getTerm() == resp.getPrevLogTerm()) {
				System.out.println("[" + this.myFullAddress + "] LEADER COMMIT 2: "
						+ this.log.getEntry(resp.getPrevLogIndex()).getTerm());
				this.commitIndex = resp.getLeaderCommit();
				this.applyEntries(this.lastApplied, this.commitIndex);
			} else {
				this.linker.sendMessage(new AppendResponse(this.currentTerm, false, this.myFullAddress),
						resp.getLeaderId());
			}
		}
		// Heartbeat
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
			}
			AppendResponse hResponse = new AppendResponse(this.currentTerm, b, this.myFullAddress);
			this.linker.sendMessage(hResponse, resp.getLeaderId());
		} else {
			AppendResponse appResponse;
			if (resp.getTerm() < this.currentTerm) {
				appResponse = new AppendResponse(this.currentTerm, false, this.myFullAddress);
				this.linker.sendMessage(appResponse, resp.getLeaderId());
				return;
			}

			Entry e = this.log.getEntry(resp.getPrevLogIndex());
			if (e.getCommand() == null && resp.getPrevLogIndex() >= 0) {
				appResponse = new AppendResponse(this.currentTerm, false, this.myFullAddress);
				this.linker.sendMessage(appResponse, resp.getLeaderId());
				return;
			}
			if (e.getTerm() != resp.getPrevLogTerm()) {
				// Rispondo false e sostituisco
				this.log.deleteFrom(resp.getPrevLogIndex());
				appResponse = new AppendResponse(this.currentTerm, false, this.myFullAddress);
				this.linker.sendMessage(appResponse, resp.getLeaderId());
				return;
			}
			// Rispondo true dopo aver fatto l'append
			this.log.appendEntries(resp.getEntry(), resp.getPrevLogIndex() + 1);
			appResponse = new AppendResponse(this.currentTerm, true, this.myFullAddress);
			this.linker.sendMessage(appResponse, resp.getLeaderId());
		}
		return;
	}

	/**
	 * 
	 * @param response
	 */
	private void handleClientRequest(ClientRequest response) {
		ClientResponse resp;
		if (!this.role.equals(Role.LEADER)) {
			resp = new ClientResponse(this.leaderId, false);
			this.linker.sendMessage(resp, response.getAddress());
			return;
		}
		Entry e;
		switch (response.getRequest()) {
			case "get":
				e = new Entry(response.getRequest() + " " + response.getAddress(), 
						this.currentTerm);
				break;
			case "op":
				e = new Entry(response.getParams(), this.currentTerm);
				break;
			default:
				resp = new ClientResponse("Invalid command " + response.getParams(), 
						false);
				this.linker.sendMessage(resp, response.getAddress());
				return;
		}
		if (!this.log.appendEntry(e)) {
			resp = new ClientResponse("Error appending " + response.getParams(), false);
			this.linker.sendMessage(resp, response.getAddress());
			return;
		}
		AppendRequest req;
		// Invio delle appendRequest ai follower
		for (String toFollower : this.addresses) {
			int indexF = this.nextIndex.get(toFollower);
			req = new AppendRequest(this.currentTerm, this.myFullAddress, indexF - 1,
					this.log.getEntry(indexF - 1).getTerm(), this.log.getEntries(indexF),
					this.commitIndex);
			this.linker.sendMessage(req, toFollower);
		}
	}

	/**
	 * Gestione delle AppendResponse: . se la risposta è positiva -> aggiungo il
	 * nodo fra i miei elettori e controllo se ho la maggioranza dei voti e quindi
	 * posso diventare Leader. . se la risposta è negativa -> confronto mio term con
	 * quello di chi ha risposto, e se quest'ultimo è maggiore cado dal ruolo di
	 * Leader, altrimenti devo decrementare il nextIndex di quel nodo, in quanto gli
	 * mancano delle informazioni, e gli mando una nuova appendRequest.
	 * 
	 * @param response
	 */
	private void handleAppendResponse(AppendResponse response) {
		if (response.isSuccess()) {
			this.nextIndex.put(response.getSender(), this.log.getDimension());
			// Controllo cosa posso committare
			ArrayList<Integer> comCount = new ArrayList<Integer>();
			for (Integer i : this.nextIndex.values()) {
				comCount.add(i - 1);
			}
			Collections.sort(comCount, Collections.reverseOrder());
			// La maggioranza sarebbe + 1 ma sto cercando l'indice -> farei +1 -1
			int indexMajority = Math.floorDiv(comCount.size() + 1, 2);
			// Cerco il maggiore committabile
			int committable = comCount.get(indexMajority);
			if (committable > this.commitIndex) {
				System.out.println("[" + this.myFullAddress + "] commit committable " + 
						committable);
				this.commitIndex = committable;
				this.applyEntries(this.lastApplied, committable);
			}

		} else {
			if (response.getTerm() > this.currentTerm) {
				this.currentTerm = response.getTerm();
				this.setRole(Role.FOLLOWER);
			} else {
				// Decremento nextIndex per quel follower perché mi ha risposto false
				// nonostante fosse nel termine giusto
				int decrementedIndex = this.nextIndex.get(response.getSender()) - 1;
				this.nextIndex.put(response.getSender(), decrementedIndex);
				// Mando appendRequest usando il nuovo index
				AppendRequest req = new AppendRequest(this.currentTerm, 
						this.myFullAddress, decrementedIndex,
						this.log.getEntry(decrementedIndex).getTerm(), 
						this.log.getEntries(decrementedIndex), this.commitIndex);
				this.linker.sendMessage(req, response.getSender());
			}
		}
		return;
	}

	/**
	 * Gestione della ricezione di un voto: controlla se il voto è positivo o meno
	 * e, in caso positivo, controlla se adesso ho la maggioranza dei voti (compreso
	 * me stesso) e quindi se posso diventare leader.
	 * 
	 * @param resp VoteResponse
	 */
	private void handleVoteResponse(VoteResponse resp) {
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

	/**
	 * Gestione della ricezione del messaggio di una voteRequest: voterà o no per il
	 * richiedente
	 * 
	 * @param resp VoteRequest
	 */
	private void handleVoteRequest(VoteRequest resp) {
		// Se sono candidato/leader e mi arriva un term più alto torno follower
		if (!this.role.equals(Role.FOLLOWER)) {
			if (this.currentTerm < resp.getTerm()) {
				this.setRole(Role.FOLLOWER);
			}
		} else {
			// Rimango follower e reset timer
			this.setElectionTimeout();
		}
		// Decisione voto
		if (resp.getTerm() >= this.currentTerm) {
			if ((this.votedFor == null || this.votedFor.equals(resp.getSender()))
					&& resp.getLastLogIndex() >= this.lastApplied) {
				this.linker.sendMessage(new VoteResponse(this.currentTerm, true, 
						this.myFullAddress), resp.getSender());
				this.votedFor = resp.getSender();
				return;
			}
		}
		this.linker.sendMessage(new VoteResponse(this.currentTerm, false, 
				this.myFullAddress), resp.getSender());
		return;
	}

	/**
	 * Chiama la funzione per applicare le entry alla state machine per ogni entry
	 * nel log che non è stata ancora applicata ma è stata committata.
	 * 
	 * @param lastCommitIndex rappresenta l'ultima entry applicata alla sm
	 * @param committable     è l'indice dell'ultima entry da applicare
	 */
	private void applyEntries(int lastCommitIndex, int committable) {
		System.out.println(
				"[" + this.myFullAddress + "] Last commit index: " + lastCommitIndex + 
				" Committable " + committable);
		for (int i = lastCommitIndex + 1; i <= committable; i++) {
			Entry e = this.log.getEntry(i);
			if (!this.sm.applyEntry(e)) {
				System.err.println("[" + this.myFullAddress + "] Invalid command in " + 
						"log");
			}
			// Se è una get mando un messaggio al client con il valore della sm
			String[] s = ((String)e.getCommand()).split(" ");
			if(s[0].equals("get") && this.role.equals(Role.LEADER)) {
				this.linker.sendMessage(new ClientResponse(
						this.getStateMachine().getState(),true), s[1]);
			}
			this.lastApplied = i;
		}
	}

	public void sendBroadcast(Msg message) {
		for (String nodeAddress : this.addresses) {
			this.linker.sendMessage(message, nodeAddress);
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
		System.out.println("[" + this.myFullAddress + "] changed role to " + role.toString());
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
			this.sendBroadcast(new VoteRequest(currentTerm, myFullAddress, commitIndex, 
					currentTerm));
			break;
		case LEADER:
			// Disattivo timeoutElection
			this.leaderId = this.myFullAddress;
			this.stopElection();
			this.nextIndex.clear();
			for (String nodeAd : this.addresses) {
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

	public NodeLinker getLinker() {
		return this.linker;
	}
}
