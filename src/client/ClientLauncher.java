package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import node.Entry;

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
			case "set":
				client.set(commands[1], new Entry(commands[2]));
				break;
			case "ADD":
				client.send(commands[2], new Entry(commands[0] + " " + commands[1]));
				break;
			default:
				System.err.println("Unrecognized command");
				break;
			}
		} while (!line.equals("quit"));
	}

}
