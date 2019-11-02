package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import node.Entry;
import node.StateMachine;

public class ClientLauncher {
	public static void main(String[] args) {
		Client client = new Client();
		Thread clientThread = new Thread(client);
		clientThread.start();
		String line;
		do {
			line = "";
			System.out.print("Command: ");
			try {
				InputStreamReader reader = new InputStreamReader(System.in);
				BufferedReader buffer = new BufferedReader(reader);
				line = buffer.readLine();
			} catch (Exception e) {
				System.out.println("Error while reading command");
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
			default:
				System.out.println("Unrecognized command");
				break;
			}
		} while (!line.equals("quit"));
	}

}