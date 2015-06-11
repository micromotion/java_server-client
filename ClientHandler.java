
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    // array of client objects
	public static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
	private BufferedReader reader;
	private Socket connection;
	private PrintWriter writer;
	private String username;
	private String chatRoom;
	private Server server;
	
	public ClientHandler(Socket connection, String username, String chatRoom, Server server) {
		this.connection = connection;
		this.chatRoom = chatRoom;
		this.username = username;
		this.server = server;
		
		synchronized(this) {
            // add this client in array
			clients.add(this);
		}
        // update server GUI title
		server.setHeader();
		
		try {
			writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		usersOnline();
		String host = connection.getInetAddress().getHostName();
		String message = "CLIENT: " + username + " <" + host + "> CONNECTED TO ROOM: " + chatRoom;
		sendStream(message);
		server.textArea.append(message + "\n");
	}
	
	public void run() {
		
		try {
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));					
		} catch (IOException e) {
			System.out.println("clienthandler" + e);
		}
		
		String line = "";
		try {
			while((line = reader.readLine()) != null) {
				sendStream(username + ": " + line);
			}
		} catch (IOException e) {
			System.out.println(e);	
		}
		deleteClient();					
	}
	
	private synchronized void sendStream(String input){ // function for sending messages to clients in the same chatroom
		for (ClientHandler client : clients) {
			if(client.chatRoom.equals(chatRoom)) {
				try {
				    client.writer.println(input);
				} catch (NullPointerException e) {
				    // let's ignore this
				}
			}
		}
	}
	
	private synchronized void deleteClient() { // function for deleting clients and therfore apply updates
		clients.remove(this);
		usersOnline();
		server.setHeader();
		String host = connection.getInetAddress().getHostName();
		String message = "CLIENT: " + username + " <" + host + "> DISCONNECTED";
		sendStream(message);
		server.textArea.append(message + "\n");
		
		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void usersOnline() { // function for updating online users and keep the clients aware
		String allUsers="";
        // get all clients in chatroom and store the username
		for(ClientHandler client : clients) { 
			if(client.chatRoom.equals(chatRoom)) {
				allUsers += client.username + " ";
			}
		}
		
		for(ClientHandler client: clients) {
			if(client.chatRoom.equals(chatRoom)) {
				client.writer.println("users"); // Print specialstring to clients to indicate UsersOnlineUpdate
				client.writer.println(allUsers); // send all users
			}
		}
	}
}