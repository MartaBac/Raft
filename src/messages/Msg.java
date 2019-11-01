package messages;

import java.io.Serializable;

public abstract class Msg implements Serializable{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int term;
	
	@Override
	public String toString(){
		return " from term " + Integer.toString(this.term) + " Message " + this.getClass();
	}
}
