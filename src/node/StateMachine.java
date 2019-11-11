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
	
	public void applyEntry(Entry entry){
		System.out.print("APPLYING "+ entry.toString());
		String command = (String) entry.getCommand();
		String[] commands = command.split(" ");
		int value = Integer.parseInt(commands[1]);
		switch (commands[0]) { 
		case "ADD":
			System.out.println("ADDING");
			this.state += value;
			break;
		case "SUB":
			this.state -= value;
			break;
		default:
			System.err.println("Invalid command");
			break;
		}
	}
	
	public String toString() {
		return Integer.toString(this.state);
	}
	
}
