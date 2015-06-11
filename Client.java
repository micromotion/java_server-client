import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import javax.swing.*;
import javax.swing.text.DefaultCaret;


public class Client extends JFrame  {

	private JTextField hostField, portField, chatRoomField, sendText, userField;
	private JLabel onlineUsers;
	private JButton join;
	private JTextArea chatArea, sideArea;
	private int port = 2000;
	private String host = "127.0.0.1";
	private String username;
	private String chatRoom = "MainRoom";
	private int usersCount = 0;

	private BufferedReader reader = null;
	private Socket socket = null;
	private PrintWriter writer = null;
	
	public static void main(String[] args) {
		
		new Client();
	}
	
	public Client() {
		setLayout(new BorderLayout());
		
		// North border for connection and username
		JPanel northPanel = new JPanel(new GridLayout(4,1));
		JPanel firstRow = new JPanel(new GridLayout(1,6,25,0));
		hostField = new JTextField(host);
		portField = new JTextField(""+port);
		chatRoomField = new JTextField(chatRoom);

		firstRow.add(new JLabel("Server Address:"));
		firstRow.add(hostField);
		firstRow.add(new JLabel("Port Number:"));
		firstRow.add(portField);
		firstRow.add(new JLabel("Chat Room:"));
		firstRow.add(chatRoomField);
		northPanel.add(firstRow);

		JLabel label = new JLabel("Enter your username below", SwingConstants.CENTER);
		northPanel.add(label);
		userField = new JTextField("Anonymous");
		userField.setHorizontalAlignment(JTextField.CENTER);
		northPanel.add(userField);
		
		JPanel lastRow = new JPanel(new GridLayout(1,2));
		onlineUsers = new JLabel("Users in Chat Room: " + usersCount);
		lastRow.add(onlineUsers);
		JLabel mainChat = new JLabel("Chat Room Display");
		lastRow.add(mainChat);
		northPanel.add(lastRow);
		
		add(northPanel, BorderLayout.NORTH);
	    
		// WEST panel, showing logged in users
	    JPanel sidePanel = new JPanel(new BorderLayout());
		sideArea = new JTextArea(10,15);
		sideArea.setForeground(Color.BLUE);
		sideArea.setEditable(false);
		sidePanel.add(new JScrollPane(sideArea));
		add(sidePanel, BorderLayout.WEST);
		
		// The CenterPanel which is the chat room	
		chatArea = new JTextArea();
		DefaultCaret caret = (DefaultCaret)chatArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(true);
		chatArea.setEditable(false);
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(new JScrollPane(chatArea));
		add(centerPanel, BorderLayout.CENTER);
		 
		// South Border for button
		join = new JButton("Join");
		
		JPanel southPanel = new JPanel(new GridLayout(2,1,0,20));
		sendText = new JTextField();
		southPanel.add(sendText);
		
		JPanel btnPanel = new JPanel();
		btnPanel.add(join);
		southPanel.add(btnPanel);
		add(southPanel, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(750, 650);
		setVisible(true);
		setTitle("Chat-Client");
		userField.requestFocus();
		
		join.addActionListener(new ActionListener() { // action to performed when the 'join' btn is pressed
			public void actionPerformed(ActionEvent event) {
				if (socket != null) {
					killConnection();
				}
				host = hostField.getText();
				port = Integer.parseInt(portField.getText());
				username = userField.getText();
				chatRoom = chatRoomField.getText();
				chatArea.setText("");
				
				try  {
                    // setup connection
					socket = new Socket(host, port);
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
					writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
					// send the username and chatroom to server
					writer.println(username);
					writer.println(chatRoom);

				} catch(IOException e) {
					chatArea.append("Failed connecting to host " + host + e.getMessage() + "\n");
					return;
				}
				host = socket.getInetAddress().getHostName();
				setTitle("CONNECTED TO: " + host + " - ON PORT: " + port);
				// create and start thread for listening on incomming streams
				ClientThread ct = new ClientThread();
				new Thread(ct).start();
			}
		}); 
		
		sendText.addActionListener(new ActionListener() { // action to perform when the client sends a message
			public void actionPerformed(ActionEvent event) {
				if (writer != null) {
                    writer.println(sendText.getText());
                    sendText.setText("");
				}
			}
		});
	}
	
	private void whoIsOnline(String users) { // function for updating online users
		sideArea.setText("");
		usersCount = 0;
		String[] array = users.split(" ");
		for(int i = 0; i < array.length; i++) {
			sideArea.append(array[i] + "\n");
			usersCount ++;
		}
		onlineUsers.setText("Users in Chat Room: " + usersCount);
	}
	
	private void killConnection() { // function for killing connection
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public class ClientThread implements Runnable {
		
		public ClientThread() {
			
		}
		
		public void run() {
			try {
				String line = "";
				while((line = reader.readLine()) != null) {
					
					if(line.equals("users")) { // if the line 'users' comes from server then theres an update on online users
						String nextLine = reader.readLine();
						whoIsOnline(nextLine); // send the online users string to function
					} else {
					chatArea.append(line + "\n");
					}
				}
				killConnection();
			} catch (SocketException e) {
				// Ignore
			} catch (IOException e) {
				System.out.println(e);
			}
		} 		
	}
}
