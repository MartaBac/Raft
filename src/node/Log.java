package node;
import java.util.ArrayList;

public class Log {
	private ArrayList<Entry> entries = new ArrayList<Entry>();
	
	
	public boolean appendEntry(Entry e){
		String[] s = ((String) e.getCommand()).split(" ");
		if(!Operation.contains(s[0]))
			return false;
		return entries.add(e);
	}
	
	public boolean appendEntries(ArrayList<Entry> e){
		for(Entry entry: e){
			if(!this.appendEntry(entry))
				return false;
		}
		return true;
	}
	
	public boolean appendEntries(ArrayList<Entry> e, int i){
		this.deleteFrom(i);
		for(Entry entry: e){
			if(!this.appendEntry(entry))
				return false;
		}
		return true;
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
		if(i >= 0 && entries.size() > i)
			return new ArrayList<Entry>(entries.subList(i, entries.size()));
		else{
			ArrayList<Entry> a = new ArrayList<Entry>();
			a.add(new Entry(null, -1));
			return a;
		}
	}
	
	public void deleteFrom(int i){
		while(this.entries.size() > i){
			this.entries.remove(this.entries.size()-1);
		}
	}
	
	@Override
	public String toString() {
		String result = "";
		for(Entry e: this.entries) {
			result += e.toString() + " \n"; 
		}
		if(result.equals("")){
			return "[]";
		}
		return result;
	}
}
