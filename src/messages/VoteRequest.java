package messages;
/**
 * Rappresenta il messaggio di richiesta voto.
 */
public class VoteRequest extends Msg {
	private static final long serialVersionUID = 1L;
	private int lastLogIndex;
	private int lastLogTerm;

	/**
	 * Invocata dai candidati per richiedere voti
	 * 
	 * @param term			Term del candidato
	 * @param sender		Id del sender
	 * @param lastLogIndex	Index dell'ultima entry che il candidato ha nel log 
	 * @param lastLogTerm	Term dell'ultima entry che il candidato ha nel log
	 */
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
