package messages;

public class VoteResponse extends Msg {
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
}
