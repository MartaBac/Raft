package node;
import java.util.ArrayList;

public class Log {
	private ArrayList<Entry> entries = new ArrayList<Entry>();
	
	
	public boolean appendEntry(Entry e){
		return entries.add(e);
	}
	
	public void appendEntries(ArrayList<Entry> e){
		for(Entry entry: e){
			entries.add(entry);
		}
	}
	
	public void appendEntries(ArrayList<Entry> e, int i){
		this.deleteFrom(i);
		for(Entry entry: e){
			entries.add(entry);
		}
	}
	
	/**
	 * 
	 * @return ultima entry inserita nel Log, null se out of bound
	 */
	public Entry getLastEntry(){
		return entries.get(entries.size()-1);
	}
	
	public Entry getEntry(int i){
		if(i >= 0 && entries.size() > i)
			return entries.get(i);
		else
			return new Entry(null, -1); 
	}
	
	public int getDimension(){
		return entries.size();
	}
	
	public ArrayList<Entry> getEntries(int i){
		return new ArrayList<Entry>(entries.subList(i, entries.size()));
	}
	
	public void deleteFrom(int i){
		while(this.entries.size() > i){
			this.entries.remove(this.entries.size()-1);
		}
	}
}
