package node;

import java.util.TimerTask;

/**
 * Contiene le funzioni da eseguire quando scade l'electionTimeout
 * 
 */

public class ElectionTask extends TimerTask {
	
	private Node node;
	
	ElectionTask(Node node) {
		super();
		this.node = node;
	}

	/**
	 * Quando il timer del nodo scade il ruolo del nodo diventa Candidate
	 */
	@Override
	public void run() {
		this.node.setRole(Role.CANDIDATE);
	}

}
