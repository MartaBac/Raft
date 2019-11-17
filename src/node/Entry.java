package node;

import java.io.Serializable;

/**
 * Entry, classe che rappresenta un elemento del log.
 * Consiste in un comando da applicare alla state machine e il term associato.
 * 
 * @author marta
 *
 */
public class Entry implements Serializable {
	private static final long serialVersionUID = 1L;
	private Object command;
	private int term;
	
	public Entry(Object command) {
		this.command = command;
	}
	
	public Entry(Object command, int term){
		this.command=command;
		this.term=term;
	}

	public Object getCommand() {
		return command;
	}

	public int getTerm() {
		return term;
	}
	
	public String toString() {
		if (command == null) 
			return "null command";
		return command.toString();
	}
	
}
