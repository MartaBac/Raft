package messages;

public class AppendResponse extends Msg {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean success;
	public AppendResponse(int term, boolean success) {
		this.term = term;
		this.success = success;
	}
	public boolean isSuccess() {
		return success;
	}
	
	public int getTerm() {
		return this.term;
	}
	
}
