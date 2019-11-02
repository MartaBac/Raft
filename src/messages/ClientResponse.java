package messages;

import node.Entry;

public class ClientResponse extends Msg {
	private static final long serialVersionUID = 1L;
	Entry params;
	
	public ClientResponse(Entry params) {
		this.params = params;
	}

	public Entry getParams() {
		return this.params;
	}
}
