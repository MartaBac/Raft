package node;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import static java.lang.System.exit;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import messages.Msg;

/* This class will handle the incoming messages from other nodes */
public class NodeLinker implements Runnable {
    private ServerSocket server;
    private int port;
    private boolean exit;
    private Node node;
    
    public NodeLinker(Node node) {
        exit = false;
        this.node = node;
        try {
            server = new ServerSocket(0);
            this.port = server.getLocalPort();
        } catch (IOException ex) {
            System.out.println("NodeLinker Error 001 - Node: " + this.node.getId());
            ex.printStackTrace();
            exit(-1);
        }
    }

    @Override
    public void run() {
        InputStream is;
        ObjectInputStream ois;
        while (!exit) {
            try {
                Socket s = server.accept();
                is = s.getInputStream();
                ois = new ObjectInputStream(is);
                Msg receivedValue = (Msg) ois.readObject();
                node.processMessage(receivedValue);
                s.close();
            } catch (Exception ex) {
                System.out.println("NodeLinker Error 002 - Node: " + this.node.getId());
                ex.printStackTrace();
            }
        }
    }
    
	public boolean sendMessage(Msg msg, String address) {
		String[] split = address.split(":");
		String receiverAddress = split[0];
		int receiverPort = Integer.parseInt(split[1]);
		OutputStream os = null;
		ObjectOutputStream oos = null;
		InetSocketAddress addr = new InetSocketAddress(receiverAddress, receiverPort);
		Socket s = new Socket();
		try {
			s.connect(addr);
			os = s.getOutputStream();
			oos = new ObjectOutputStream(os);
			oos.writeObject(msg);
			oos.flush();
			s.close();
		} catch (Exception ex) {
			System.out.println("Connection error 002");
			return false;
		}
		return true;
	}
    
    public int getPort() {
        return this.port;
    }
   
}