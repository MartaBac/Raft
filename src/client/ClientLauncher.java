package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientLauncher {
	public static void main(String[] args) {
		Client client = new Client();
		Thread clientThread = new Thread(client);
		clientThread.start();
		String line;
		do {
			line = "";
			System.out.println("Command: ");
			try {
				InputStreamReader reader = new InputStreamReader(System.in);
				BufferedReader buffer = new BufferedReader(reader);
				line = buffer.readLine();
			} catch (Exception e) {
				System.err.println("Error while reading command");
				System.exit(-1);
			}
			String[] commands = line.split(" ");
			switch (commands[0]) {
				case "get":
					client.get(commands[1]);
					break;
				case "op":
					client.operation(commands[1], commands[2]+" "+commands[3]);
				default:
					System.err.println("Unrecognized command");
					break;
			}
		} while (!line.equals("quit"));
	}

}
