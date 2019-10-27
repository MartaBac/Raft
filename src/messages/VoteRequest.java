package messages;

public class VoteRequest extends Msg {
	private String idAddress;
	private int lastLogIndex;
	private int lastLogTerm;

	public VoteRequest(int term, String idAddress, int lastLogIndex, int lastLogTerm){
		this.term = term;
		this.idAddress = idAddress;
		this.lastLogIndex = lastLogIndex;
		this.lastLogTerm = lastLogTerm;
	}

	public int getTerm() {
		return term;
	}

	public String getIdAddress() {
		return idAddress;
	}

	public int getLastLogIndex() {
		return lastLogIndex;
	}

	public int getLastLogTerm() {
		return lastLogTerm;
	}
	
	@Override
	public String toString() {
		return idAddress;
	}
	
}
