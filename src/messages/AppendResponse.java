package messages;

public class AppendResponse extends Msg {
	private static final long serialVersionUID = 1L;
	private boolean success;
	
	/**
	 * Risposta ad appendRequest
	 * 
	 * @param term		Term nodo
	 * @param success	True se inserita correttamente nel log
	 * @param sender	Indirizzo nodo
	 */
	public AppendResponse(int term, boolean success, String sender) {
		this.term = term;
		this.success = success;
		this.sender = sender;
	}
	public boolean isSuccess() {
		return success;
	}
	
	public int getTerm() {
		return this.term;
	}
	
	public String getSender(){
		return this.sender;
	}
	
	@Override
	public String toString(){
		return " Heartbeat response " + this.success;
	}
	
}
