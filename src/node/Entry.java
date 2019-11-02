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
		return command.toString();
	}
	
}
