package node;

import java.util.TimerTask;

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
