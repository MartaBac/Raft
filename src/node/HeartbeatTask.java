package node;

import java.util.TimerTask;

import messages.AppendRequest;

public class HeartbeatTask extends TimerTask {
	
	private Node node;
	
	HeartbeatTask(Node node) {
		super();
		this.node = node;
	}

	@Override
	public void run() {
		AppendRequest heartbeat = new AppendRequest(
				this.node.getCurrentTerm(), 
				this.node.getFullAddress(), 
				this.node.getLog().getDimension(), 
				this.node.getCurrentTerm() - 1,
				this.node.getCommitIndex());
		this.node.sendBroadcast(heartbeat);
	}

}
