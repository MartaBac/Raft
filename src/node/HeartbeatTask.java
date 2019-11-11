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
		AppendRequest hb;
		// Invio hearbeat ai follower
		for(String toFollower : this.node.getAddressesList()){
			int indexF = this.node.getNextIndex().get(toFollower);
			hb = new AppendRequest(
					this.node.getCurrentTerm(), 
					this.node.getFullAddress(),
					indexF - 1 , 
					this.node.getLog().getEntry(indexF - 1).getTerm(),
					this.node.getCommitIndex());
			this.node.sendMessage(hb, toFollower);
		}
	}

}
