package messages;

import java.util.ArrayList;

import node.Entry;

public class AppendRequest extends Msg {
	private static final long serialVersionUID = 1L;
	private int term;
	private String leaderId;
	private int prevLogIndex;
	private int prevLogTerm;
	private ArrayList<Entry> entry;
	private int leaderCommit;
	
	/**
	 * Messaggi inviati dal leader verso i nodi
	 * 
	 * @param term			Term del leader
	 * @param leaderId		Indirizzo del leader
	 * @param prevLogIndex	Indice della log entry immediatamente precedente le nuove 
	 * @param prevLogTerm	Term della log entry prima delle nuove
	 * @param entry			ArrayList delle Entry da inserire
	 * @param leaderCommit	Indice di commit del leader
	 */
	public AppendRequest(int term, String leaderId, int prevLogIndex, int prevLogTerm, 
			ArrayList<Entry> entry, int leaderCommit){
		this.leaderId = leaderId;
		this.prevLogIndex = prevLogIndex;
		this.prevLogTerm = prevLogTerm;
		this.leaderCommit = leaderCommit;
		this.entry = entry;
		this.term = term;	
	}
	/**
	 * Heartbeat 
	 * @param leaderId
	 * @param prevLogIndex
	 * @param prevLogTerm
	 * @param leaderCommit
	 */
	public AppendRequest(int term, String leaderId, int prevLogIndex, int prevLogTerm,
			int leaderCommit){
		this.leaderId = leaderId;
		this.prevLogIndex = prevLogIndex;
		this.prevLogTerm = prevLogTerm;
		this.leaderCommit = leaderCommit;
		this.entry = null;
		this.term = term;
	}
	public String getLeaderId() {
		return leaderId;
	}
	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}
	public int getPrevLogIndex() {
		return prevLogIndex;
	}
	public void setPrevLogIndex(int prevLogIndex) {
		this.prevLogIndex = prevLogIndex;
	}
	public int getPrevLogTerm() {
		return prevLogTerm;
	}
	public void setPrevLogTerm(int prevLogTerm) {
		this.prevLogTerm = prevLogTerm;
	}
	public ArrayList<Entry> getEntry() {
		return entry;
	}
	public void setEntry(ArrayList<Entry> entry) {
		this.entry = entry;
	}
	public int getLeaderCommit() {
		return leaderCommit;
	}
	public void setLeaderCommit(int leaderCommit) {
		this.leaderCommit = leaderCommit;
	}
	public int getTerm() {
		return term;
	}
	public void setTerm(int term) {
		this.term = term;
	}
}
