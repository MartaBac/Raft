package node;

import java.util.TimerTask;


import messages.AppendRequest;

/**
 * Contiene le operazioni da eseguire quando scade il timeout di invio degli heartbeat
 *
 */

public class HeartbeatTask extends TimerTask {
	
	private Node node;
	
	HeartbeatTask(Node node) {
		super();
		this.node = node;
	}

	@Override
	public void run() {
		sendHeartbeats();
	}
	
	/**
	 * Task per l'invio degli Heartbeats ai nodi.
	 */
	public void sendHeartbeats(){
		AppendRequest hb;
		for(String toFollower : this.node.getAddressesList()){
			int indexF = this.node.getNextIndex().get(toFollower);
			hb = new AppendRequest(
					this.node.getCurrentTerm(), 
					this.node.getFullAddress(),
					indexF - 1 , 
					this.node.getLog().getEntry(indexF - 1).getTerm(),
					this.node.getCommitIndex());
			this.node.getLinker().sendMessage(hb, toFollower);
		}
	}

}
