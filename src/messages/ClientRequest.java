package messages;

/**
 * Struttura delle richieste client
 *
 */
public class ClientRequest extends Msg {
	private static final long serialVersionUID = 1L;
	private String request;
	private String address;
	private String params;
	
	/**
	 * Richiesta da parte del client senza params (i.e. get).
	 * 
	 * @param request	Tipo di comando
	 * @param address	Indirizzo client al quale rispondere
	 */
	public ClientRequest(String request, String address) {
		this.request = request;
		this.address = address;
	}
	
	/**
	 * Richiesta da parte del client con parametri
	 * 
	 * @param request	Tipo di comando (i.e. op)
	 * @param address	Indirizzo client al quale rispondere
	 * @param params	Parametri per eseguire l'operazione (i.e. add 1)
	 */
	public ClientRequest(String request, String address, String params) {
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
	
	public String getParams() {
		return this.params;
	}
}

