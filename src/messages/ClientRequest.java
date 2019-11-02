package messages;

import node.Entry;

public class ClientRequest extends Msg {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String request;
	String address;
	Entry params;
	
	public ClientRequest(String request, String address) {
		this.request = request;
		this.address = address;
	}
	
	public ClientRequest(String request, String address, Entry params) {
		this.request = request;
		this.address = address;
		this.params = params;
	}
	
	public String getRequest() {
		return this.request;
	}

	public String getAddress() {
		return this.address;
	}
}
