package node;

import java.util.TimerTask;

/**
 * Quando il timer del nodo scade gli fa il set del ruolo a Candidate
 * 
 */

public class ElectionTask extends TimerTask {
	
	private Node node;
	
	ElectionTask(Node node) {
		super();
		this.node = node;
	}

	@Override
	public void run() {
		this.node.setRole(Role.CANDIDATE);
	}

}
