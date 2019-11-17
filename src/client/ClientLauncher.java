package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientLauncher {
	public static void main(String[] args) {
		Client client = new Client();
		Thread clientThread = new Thread(client);
		clientThread.start();
		String line;
		System.out.println("Client ready");
		boolean exit = false;
		do {
			line = "";
			try {
				InputStreamReader reader = new InputStreamReader(System.in);
				BufferedReader buffer = new BufferedReader(reader);
				line = buffer.readLine();
			} catch (Exception e) {
				System.err.println("Error while reading command");
				System.exit(-1);
			}
			String[] commands = line.split(" ");
			try {
				switch (commands[0]) {
					case "get":
						client.get(commands[1]);
						break;
					case "op":
						client.operation(commands[1], commands[2] + " " + commands[3]);
						break;
					default:
						System.err.println("Unrecognized command");
						break;
				}
			} catch(Exception e) {
				System.err.println("Invalid command format");
			}
		} while (!exit);
	}

}
