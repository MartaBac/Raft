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
		int value;
		switch (commands[0]) {
			case "get":
				break;
			case "add":
				value = Integer.parseInt(commands[1]);
				this.state += value;
				break;
			case "sub":
				value = Integer.parseInt(commands[1]);
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
