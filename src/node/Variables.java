package node;

/**
 * In questa classe è possibile modificare le variabili del sistema: tempo di invio degli
 * heartbeats, numero dei nodi, minimo e massimo valore fra cui viene generato 
 * randomicamente l'electionTimeout dei nodi.
 * 
 * NB: per evitare l'invio di messaggi troppo frequenti, è consigliato non 
 * aumentare troppo i valori, altrimenti occorrerebbe calibrarli adeguatamente.
 *
 */
public class Variables {
	// Election timeout
	private static int debugMultiplicator = 10;
	public static long minRet = 10 * debugMultiplicator; // In teoria questo 10ms (pag.10 paper)
	public static long maxRet = 500 * debugMultiplicator; // In teoria questo 500ms
	public static long heartbeat = 3 * debugMultiplicator; // In teoria fra (0.5-20)
	public static long nNodes = 5;
}
