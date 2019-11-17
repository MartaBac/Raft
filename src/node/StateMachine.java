package node;

public class StateMachine {
	private int state;
	
	public StateMachine(){
		this.setState(0);
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public boolean applyEntry(Entry entry){
		String command = (String) entry.getCommand();
		String[] commands = command.split(" ");
		int value = Integer.parseInt(commands[1]);
		switch (commands[0]) { 
		case "add":
			this.state += value;
			break;
		case "sub":
			this.state -= value;
			break;
		default:
			return false;
		}
		return true;
	}
	
	public String toString() {
		return Integer.toString(this.state);
	}
	
}
