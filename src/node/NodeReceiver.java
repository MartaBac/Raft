package node;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import static java.lang.System.exit;
import java.net.ServerSocket;
import java.net.Socket;

import messages.Msg;

/* This class will handle the incoming messages from other nodes */
public class NodeReceiver implements Runnable {
    private ServerSocket server;
    private int port;
    private boolean exit;
    private Node node;
    
    public NodeReceiver(Node node) {
        exit = false;
        this.node = node;
        try {
            server = new ServerSocket(0);
            this.port = server.getLocalPort();
        } catch (IOException ex) {
            System.out.println("NodeReceiver Error 002 - Node: " + this.node.getId());
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
                System.out.println(node.getId() + " received message");
                node.processMessage(receivedValue);
                s.close();
            } catch (Exception ex) {
                System.out.println(this.node.getId() + " NodeReceiver Error 003");
                ex.printStackTrace();
            }
        }
    }
    
    public int getPort() {
        return this.port;
    }
   
}