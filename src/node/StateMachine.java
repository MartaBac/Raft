package node;

public class StateMachine {
	private String state;
	
	public StateMachine(){
		this.setState("void");
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	public void applyEntry(Entry entry){
		//TODO
	}
	
}
