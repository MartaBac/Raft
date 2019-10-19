package node;
import java.util.ArrayList;

public class Log {
	private ArrayList<Entry> entries = new ArrayList<Entry>();
	
	
	public void appendEntry(Entry e){
		entries.add(e);
	}
	
	/**
	 * 
	 * @return ultima entry inserita nel Log
	 */
	public Entry getLastEntry(){
		return entries.get(entries.size()-1);
	}
	
	public Entry getEntry(int i){
		return entries.get(i);
	}
	
	public int getDimension(){
		return entries.size();
	}
}
