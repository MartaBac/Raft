package node;
import java.util.ArrayList;

/**
 * La classe contiene un elenco di Entry inserite in conseguenza alla ricezione di una 
 * appendRequest contenente Entry.
 *
 */
public class Log {
	private ArrayList<Entry> entries = new ArrayList<Entry>();
	
	/**
	 * Inserisce una Entry in coda al log
	 * 
	 * @param e Entry
	 * @return	false se la Entry non rappresenta un'operazione valida
	 */
	public boolean appendEntry(Entry e){
		String[] s = ((String) e.getCommand()).split(" ");
		if(!Operation.contains(s[0]))
			return false;
		return entries.add(e);
	}
	
	/**
	 * Inserisce le Entry in coda al log.
	 * 
	 * @param e Elenco Entry
	 * @return	false se almeno un inserimento fallisce
	 */
	public boolean appendEntries(ArrayList<Entry> e){
		for(Entry entry: e){
			if(!this.appendEntry(entry))
				return false;
		}
		return true;
	}
	
	/**
	 * Inserisce le Entry passate come parametro nel log a partire dall'indice 'i'. 
	 * Se sono già presenti elementi in 'i' o dopo, vengono prima cancellati e poi 
	 * aggiunte le nuove Entry.
	 * 
	 * @param e		Entries da aggiungere
	 * @param i		Indice dove aggiungerle
	 * @return		false se almeno un append fallisce
	 */
	public boolean appendEntries(ArrayList<Entry> e, int i){
		this.deleteFrom(i);
		for(Entry entry: e){
			if(!this.appendEntry(entry))
				return false;
		}
		return true;
	}
	
	/**
	 * Fa il get dell'ultima entry inserita nel Log
	 * 
	 * @return ultima entry, null se out of bound
	 */
	public Entry getLastEntry(){
		return entries.get(entries.size()-1);
	}
	
	/**
	 * Restituisce la entry presente ad un dato indice nel log. Se assente, restituisce
	 * una Entry vuota.
	 * 
	 * @param i	indice
	 * @return	
	 */
	public Entry getEntry(int i){
		if(i >= 0 && entries.size() > i)
			return entries.get(i);
		else
			return new Entry(null, -1); 
	}
	
	/**
	 * Restituisce la dimensione del log
	 * @return	int 
	 */
	public int getDimension(){
		return entries.size();
	}
	
	/**
	 * Prende tutte le entries dall'indice specificato in poi
	 * @param i	Indice
	 * @return	Entries
	 */
	public ArrayList<Entry> getEntries(int i){
		if(i >= 0 && entries.size() > i)
			return new ArrayList<Entry>(entries.subList(i, entries.size()));
		else{
			ArrayList<Entry> a = new ArrayList<Entry>();
			a.add(new Entry(null, -1));
			return a;
		}
	}
	
	/**
	 * Cancella tutte le entries dall'indice specificato in poi
	 * @param i	Indice
	 */
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
