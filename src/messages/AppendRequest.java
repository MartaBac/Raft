package messages;

import node.Entry;

public class AppendRequest extends Msg {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int toBeCommitted;
	private String leaderId;
	private int prevLogIndex;
	private int prevLogTerm;
	private Entry entry;
	private int leaderCommit;
	
	public AppendRequest(String leaderId, int prevLogIndex, int prevLogTerm, Entry entry,
			int leaderCommit){
		this.leaderId = leaderId;
		this.prevLogIndex = prevLogIndex;
		this.prevLogTerm = prevLogTerm;
		this.leaderCommit = leaderCommit;
		this.entry = entry;
		
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
	public Entry getEntry() {
		return entry;
	}
	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	public int getLeaderCommit() {
		return leaderCommit;
	}
	public void setLeaderCommit(int leaderCommit) {
		this.leaderCommit = leaderCommit;
	}
}
