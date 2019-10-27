package messages;

public class VoteResponse extends Msg {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean voteGranted;
	private String idAddress;
	
	public VoteResponse(int term, boolean voteGranted, String idAddress){
		this.term = term;
		this.voteGranted = voteGranted;
		this.idAddress = idAddress;
	}

	public int getTerm() {
		return term;
	}

	public boolean isVoteGranted() {
		return voteGranted;
	}
	
	public String toString() {
		return "=> " + this.term + " " + String.valueOf(this.voteGranted);
	}

	public String getIdAddress() {
		return idAddress;
	}
}
