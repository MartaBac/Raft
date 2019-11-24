package messages;
/**
 * Rappresenta il messaggio di risposta al voto. 
 * Contiene il term number, se il voto è positivo o no, e l'id del votante.
 *
 */
public class VoteResponse extends Msg {
	private static final long serialVersionUID = 1L;
	private boolean voteGranted;
	
	/**
	 *
	 * @param term Numero del term del votante
	 * @param voteGranted True se vota per il candidato
	 * @param sender Indirizzo del votante
	 */
	public VoteResponse(int term, boolean voteGranted, String sender){
		this.term = term;
		this.voteGranted = voteGranted;
		this.sender = sender;
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

	public String getSender() {
		return sender;
	}
}
