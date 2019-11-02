package messages;

public class VoteRequest extends Msg {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int lastLogIndex;
	private int lastLogTerm;

	public VoteRequest(int term, String sender, int lastLogIndex, int lastLogTerm){
		this.term = term;
		this.sender = sender;
		this.lastLogIndex = lastLogIndex;
		this.lastLogTerm = lastLogTerm;
	}

	public int getTerm() {
		return term;
	}

	public String getSender() {
		return sender;
	}

	public int getLastLogIndex() {
		return lastLogIndex;
	}

	public int getLastLogTerm() {
		return lastLogTerm;
	}
}
