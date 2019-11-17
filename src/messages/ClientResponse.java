package messages;

public class ClientResponse extends Msg {
	private static final long serialVersionUID = 1L;
	private Object params;
	private boolean isState;
	
	public ClientResponse(Object params, boolean isState) {
		this.params = params;
		this.isState = isState;
	}
	
	public Object getParams() {
		return this.params;
	}
	
	public boolean isState(){
		return this.isState;
	}
}
