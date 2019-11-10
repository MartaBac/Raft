package client;

import static java.lang.System.exit;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import messages.*;
import node.Entry;
import node.StateMachine;

/* This class will handle the incoming messages from other nodes */
public class Client implements Runnable {
    private ServerSocket server;
    private int port;
    private String address;
    private String fullAddress;
    private boolean exit;
    
    public Client() {
        exit = false;
        try {
            server = new ServerSocket(0);
            this.port = server.getLocalPort();
            this.address = InetAddress.getLocalHost().getHostAddress();
            this.fullAddress = this.address+":"+this.port;
        } catch (IOException ex) {
            System.err.println("Client error");
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
                this.processMessage(receivedValue);
                s.close();
            } catch (Exception ex) {
                System.err.println("Client error while receiving message");
                ex.printStackTrace();
            }
        }
    }
    
    private void processMessage(Msg receivedValue) {
		if (receivedValue instanceof ClientResponse) {
			Entry value = ((ClientResponse)receivedValue).getParams();
			System.out.println(value.getCommand().toString());
		}
		
	}

	public boolean get(String address) {
    	ClientRequest msg = new ClientRequest("get", this.fullAddress);
    	if(this.sendMessage(address, msg))
    		return true;
    	return false;
    }
    
    public boolean set(String address, Entry value) {
    	ClientRequest msg = new ClientRequest("set", this.fullAddress, value);
    	if(this.sendMessage(address, msg))
    		return true;
    	return false;
    }
    
    public boolean send(String address, Entry value) {
    	ClientRequest msg = new ClientRequest("set", this.fullAddress, value);
    	if(this.sendMessage(address, msg))
    		return true;
    	return false;
    }
    
    public boolean sendMessage(String address, ClientRequest msg) {
    	String[] split;
		String sendToAddr;
		int sendToPort;
    	try {
	    	split = address.split(":");
	    	sendToAddr = split[0];
	    	sendToPort = Integer.parseInt(split[1]);
    	} catch (Exception e) {
    		System.err.println("Error: invalid address (valid format ipAddr:port)");
			return false;
    	}
		OutputStream os = null;
		ObjectOutputStream oos = null;
		InetSocketAddress addr = new InetSocketAddress(sendToAddr, sendToPort);
		Socket s = new Socket();
		try {
			s.connect(addr);
			os = s.getOutputStream();
			oos = new ObjectOutputStream(os);
			oos.writeObject(msg);
			oos.flush();
			s.close();
		} catch (Exception ex) {
			System.err.println("Error: sending message - host unavailable");
			return false;
		}
		return true;   	
    }
    
    public int getPort() {
        return this.port;
    }
   
}