package messages;

public class VoteResponse extends Msg {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean voteGranted;
	
	public VoteResponse(int term, boolean voteGranted){
		this.term = term;
		this.voteGranted = voteGranted;
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
}
