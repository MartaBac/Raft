package messages;

public class ClientResponse extends Msg {
	private static final long serialVersionUID = 1L;
	private Object params;
	private boolean isState;
	
	/**
	 * Messaggio di risposta alle request dei client.
	 * 
	 * @param params	valore della SM, indirizzo Leader o messaggio di errore 
	 * @param isState	true se params contiene il valore della SM, false altrimenti
	 */
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
