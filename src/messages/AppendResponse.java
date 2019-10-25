package messages;

public class AppendResponse extends Msg {
	private boolean success;
	public AppendResponse(int term, boolean success) {
		this.term = term;
		this.success = success;
	}
	public boolean isSuccess() {
		return success;
	}
	
}
