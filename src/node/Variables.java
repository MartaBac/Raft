package node;

public class Variables {
	// Election timeout
	private static int debugMultiplicator = 5;
	public static long minRet = 400 * debugMultiplicator; // in teoria questo 10ms (pag 10 paper)
	public static long maxRet = 500 * debugMultiplicator; // in teoria questo 500ms
	public static long heartbeat = 10 * debugMultiplicator; // in teoria fra (0.5-20)
}
