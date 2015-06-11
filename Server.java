import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

public class Server extends JFrame {
	private static int port;
	private ServerSocket serverSocket;
	public JTextArea textArea = new JTextArea();
	
	public static void main(String[] args) {
		port = args.length > 0 ? Integer.parseInt(args[0]) : 2000;
		
		new Server(port);
	}
	
	public Server(int port) {
        // GUI for the server
	    getContentPane().add("Center", new JScrollPane(textArea));
	    textArea.setEditable(false);
	    setLocation(10, 10);
	    setSize(650, 250);
	    setTitle("CHAT-SERVER ON PORT: " + port);
	    setVisible(true);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    
	    try {
	      serverSocket = new ServerSocket(port);
	     
	    } catch (IOException e) {
	      System.out.println("COULD NOT LISTEN ON PORT: " + port + e);
	      System.exit(1);
	    }
	    
	    while(true) {
	    	Socket connection = null;
	    	String username = "";
	    	String chatRoom = "";
	    	
	    	try {
                // create connection and in and outputstream
				connection = serverSocket.accept(); // wait for new client
				InputStream input = connection.getInputStream();
				InputStreamReader readInput = new InputStreamReader(input,"UTF-8");
				BufferedReader reader = new BufferedReader(readInput);
				// read the username and chatroom from client
				username = reader.readLine();
				chatRoom = reader.readLine();
				
			} catch (IOException e) {
				System.out.println("server" + e);
			}
	    	// send client data to function, no spaces and special chars allowed in username
	    	addClient(connection, username.replaceAll("\\s+",""), chatRoom);
	    }
	}
	 
	private void addClient(Socket connection, String username, String chatRoom) { // function for adding client
        // add new client and its data, give it its own thread
		ClientHandler ch = new ClientHandler(connection, username, chatRoom, this);
		new Thread(ch).start();
	}
	
	public synchronized void setHeader() { // function for setting the title in the GUI
		String host="";
			try {
				host = serverSocket.getInetAddress().getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			int clientsOnline = ClientHandler.clients.size();
	    setTitle("SERVER ON: " + host + " - PORT: " + port + " - CLIENTS: " + clientsOnline);
	}
}
