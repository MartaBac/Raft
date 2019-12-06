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
	private static int debugMultiplicator = 20;
	public static long minRet = 10 * debugMultiplicator;  
	public static long maxRet = 500 * debugMultiplicator; 
	public static long heartbeat = 3 * debugMultiplicator; 
	public static long nNodes = 5;
}
