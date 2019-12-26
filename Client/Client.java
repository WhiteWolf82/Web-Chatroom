import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client implements ActionListener
{
	private JFrame frame;
	private JFrame childFrame;
	private JTextField hostTextField;
	private JTextField portTextField;
	private JTextField nameTextField;
	private JPasswordField pswdField;
	private JTextField sendMsgTextField;
	private JLabel userLabel;
	private JLabel hostLabel;
	private JLabel portLabel;
	private JLabel nameLabel;
	private JLabel pswdLabel;
	private JLabel welcomeLabel;
	private JLabel connectLabel;
	private JButton connectButton;
	private JButton disconnectButton;
	private JButton loginButton;
	private JButton registerButton;
	private JButton exitButton;
	private JButton sendButton;
	private JButton chatButton;
	private JList<String> userList;
	private JTextPane showMsgTextPane;
	private JScrollPane msgScrollPane;
	private JScrollBar msgScrollBar;
	private ArrayList<String> userArrayList;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	DefaultListModel<String> userListModel;
	
	private boolean isLogin = false;
	private boolean isConnect = false;
	
	/**constructor function*/
	public Client()
	{
		userArrayList = new ArrayList<>();
		initialize();
	}
	
	public JFrame getFrame()
	{
		return frame;
	}
	
	public JFrame getChildFrame()
	{
		return childFrame;
	}
	
	/**connect to server*/
	public boolean connect(String host, int port)
	{
		try
		{
			Socket socket = new Socket(host, port);
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			printWriter = new PrintWriter(socket.getOutputStream());
			ClientThread clientThread = new ClientThread(socket, this);
			clientThread.start();
			return true;
		}
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(frame, "Cannot connect to server!");
			return false;
		}
	}
	
	public void disconnect()
	{
		/*isConnect = false;
		hostLabel.setVisible(true);
		hostTextField.setVisible(true);
		portLabel.setVisible(true);
		portTextField.setVisible(true);
		connectButton.setVisible(true);
		connectLabel.setVisible(false);
		disconnectButton.setVisible(false);*/
		frame.dispose();
		System.exit(0);
	}
	
	/**initialize the GUI*/
	public void initialize()
	{
		frame = new JFrame("Online Chatroom");
		JPanel panel = new JPanel();
		JPanel upPanel = new JPanel();
		JPanel bottomPanel = new JPanel();
		JPanel leftPanel = new JPanel();
		
		frame.setBounds(200, 40, 1024, 768);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(panel);
		frame.setLayout(new BorderLayout());
		
		upPanel.setLayout(new FlowLayout());
		bottomPanel.setLayout(new GridBagLayout());
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setPreferredSize(new Dimension(150, 0));
		
		hostTextField = new JTextField("127.0.0.1");
		portTextField = new JTextField("3226");
		nameTextField = new JTextField();
		pswdField = new JPasswordField();
		hostTextField.setPreferredSize(new Dimension(100, 25));
		portTextField.setPreferredSize(new Dimension(70, 25));
		nameTextField.setPreferredSize(new Dimension(150, 25));
		pswdField.setPreferredSize(new Dimension(150, 25));
		
		hostLabel = new JLabel("Host:");
		portLabel = new JLabel("Port:");
		nameLabel = new JLabel("Username:");
		pswdLabel = new JLabel("Password:");
		welcomeLabel = new JLabel();
		welcomeLabel.setVisible(false);    //only be visible after login
		connectLabel = new JLabel();
		connectLabel.setVisible(false); 	//only be visible after connecting
		
		connectButton = new JButton("Connect");
		disconnectButton = new JButton("Disconnect");
		disconnectButton.setVisible(false); 	//only be visible after connecting
		loginButton = new JButton("Login");
		registerButton = new JButton("Register");
		exitButton = new JButton("Exit");
		exitButton.setVisible(false); 	//only be visible after login
		
		upPanel.add(connectLabel);
		upPanel.add(disconnectButton);
		upPanel.add(hostLabel);
		upPanel.add(hostTextField);
		upPanel.add(portLabel);
		upPanel.add(portTextField);
		upPanel.add(connectButton);
		upPanel.add(welcomeLabel);
		upPanel.add(nameLabel);
		upPanel.add(nameTextField);
		upPanel.add(pswdLabel);
		upPanel.add(pswdField);
		upPanel.add(loginButton);
		upPanel.add(registerButton);
		upPanel.add(exitButton);
		
		sendButton = new JButton("Send");
		sendMsgTextField = new JTextField();
		bottomPanel.add(sendMsgTextField, new GridBagConstraints(0, 0, 1, 1, 100, 100, 
			    GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		bottomPanel.add(sendButton, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, 
			    GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		userLabel = new JLabel("Online (0):");
		chatButton = new JButton("Chat");
		
		userListModel = new DefaultListModel<>();
		userList = new JList<>(userListModel);
		
		JScrollPane userListPane = new JScrollPane(userList);
		
		leftPanel.add(userLabel, new GridBagConstraints(0, 0, 1, 1, 1, 1, 
			    GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		leftPanel.add(chatButton, new GridBagConstraints(0, 1, 1, 1, 1, 1, 
			    GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		leftPanel.add(userListPane, new GridBagConstraints(0, 3, 1, 1, 100, 100, 
			    GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		showMsgTextPane = new JTextPane();
		showMsgTextPane.setEditable(false);
		msgScrollPane = new JScrollPane();
		msgScrollPane.setViewportView(showMsgTextPane);
		msgScrollBar = new JScrollBar(JScrollBar.VERTICAL);
		msgScrollBar.setAutoscrolls(true);
		msgScrollPane.setVerticalScrollBar(msgScrollBar);
		
		panel.add(upPanel, "North");
		panel.add(bottomPanel, "South");
		panel.add(leftPanel, "West");
		panel.add(msgScrollPane, "Center");
		
		//register buttons
		connectButton.addActionListener(this);
		disconnectButton.addActionListener(this);
		loginButton.addActionListener(this);
		registerButton.addActionListener(this);
		sendButton.addActionListener(this);
		exitButton.addActionListener(this);
		chatButton.addActionListener(this);
		
		frame.setVisible(true);
	}
	
	public void sendToServer(String type, String content)
	{
		printWriter.println(type + "##" + content);
		printWriter.flush();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();
		String host = hostTextField.getText();
		String port = portTextField.getText();
		String username = nameTextField.getText();
		String pswd = pswdField.getText();
		String sendMsg = sendMsgTextField.getText();
		switch(cmd)
		{
		case "Connect":
			if (connect(host, Integer.parseInt(port)))
			{
				isConnect = true;
				hostLabel.setVisible(false);
				hostTextField.setVisible(false);
				portLabel.setVisible(false);
				portTextField.setVisible(false);
				connectButton.setVisible(false);
				connectLabel.setText("You've connected to " + host + "         ");
				connectLabel.setVisible(true);
				disconnectButton.setVisible(true);
				sendToServer("CONNECT", "");
			}
			else 
			{
				JOptionPane.showMessageDialog(frame, 
						"Connect failed, try again later!",
						"Fail",
						JOptionPane.WARNING_MESSAGE);
			}
			break;
		case "Disconnect":
			sendToServer("DISCONNECT", username);
			break;
		case "Login":
			if (!isConnect)
			{
				JOptionPane.showMessageDialog(frame, 
						"Please connect first!",
						"Warning",
						JOptionPane.WARNING_MESSAGE);
				break;
			}
			if (username.length() == 0 || pswd.length() == 0)
			{
				JOptionPane.showMessageDialog(frame, 
						"Username or Password cannot be empty!",
						"Warning",
						JOptionPane.WARNING_MESSAGE);
				break;
			}
			sendToServer("LOGIN", username + "##" + pswd);
			break;
		case "Register":
			if (!isConnect)
			{
				JOptionPane.showMessageDialog(frame, 
						"Please connect first!",
						"Warning",
						JOptionPane.WARNING_MESSAGE);
				break;
			}
			ClientRegister clientRegister = new ClientRegister(frame);
			childFrame = clientRegister.getFrame();
			break;
		case "Send":
			sendToServer("SENDMSG", sendMsg);
			sendMsgTextField.setText(null);
			break;
		case "Exit":
			sendToServer("EXIT", "");
			break;
		case "Chat":
			//todo
			break;
		default:
			break;
		}
	}
	
	public void loginSuccess(String username)
	{
		sendToServer("USERLIST", "");
		isLogin = true;
		JOptionPane.showMessageDialog(frame, 
				"Login succeed!",
				"Succeed",
				JOptionPane.INFORMATION_MESSAGE);
		nameLabel.setVisible(false);
		nameTextField.setVisible(false);
		pswdLabel.setVisible(false);
		pswdField.setVisible(false);
		loginButton.setVisible(false);
		registerButton.setVisible(false);
		welcomeLabel.setText("                Welcome, " + username + "                ");
		welcomeLabel.setVisible(true);
		exitButton.setVisible(true);
	}
	
	public void loginFail()
	{
		isLogin = false;
		JOptionPane.showMessageDialog(frame, 
				"Wrong username or password!",
				"Fail",
				JOptionPane.WARNING_MESSAGE);
	}
	
	/**get the current time*/
	private String getDatetime()
	{
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}
	
	/**insert a new message to the text area*/
	private void insertMsg(JScrollPane scrollPane, JTextPane textPane, String title, String content)
	{
		StyledDocument doc = textPane.getStyledDocument();
		//title attributes
		SimpleAttributeSet titleAttribute = new SimpleAttributeSet();
		StyleConstants.setBold(titleAttribute, true);
		StyleConstants.setForeground(titleAttribute, Color.blue);
		StyleConstants.setFontSize(titleAttribute, 12);
		//content attributes
		SimpleAttributeSet contentAttribute = new SimpleAttributeSet();
		StyleConstants.setForeground(contentAttribute, Color.black);
		StyleConstants.setFontSize(contentAttribute, 15);
		//insert into document
		try
		{
			doc.insertString(doc.getLength(), title + '\n', titleAttribute);
			doc.insertString(doc.getLength(), content + '\n', contentAttribute);
		} 
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		msgScrollBar.setValue(msgScrollBar.getMaximum());
	}
	
	/**add a new user*/
	public void addUser(String username)
	{
		System.out.println("add" + username);
		if (!userArrayList.contains(username))
		{
			System.out.println(username + " came.");
			userArrayList.add(username);
			userListModel.addElement(username);
			insertMsg(msgScrollPane, showMsgTextPane, "Server (" + getDatetime() + "):", username +
					" has entered the chatroom.");
			userLabel.setText("Online (" + userArrayList.size() + "):");
			System.out.println("Current users: " + Integer.toString(userArrayList.size()));
		}
	}
	
	/**remove a user*/
	public void removeUser(String username)
	{
		if (userArrayList.contains(username))
		{
			userArrayList.remove(username);
			userListModel.removeElement(username);
			insertMsg(msgScrollPane, showMsgTextPane, "Server (" + getDatetime() + "):", username +
					" has leaved the chatroom.");
			userLabel.setText("Online (" + userArrayList.size() + "):");
		}
	}
	
	/**show user list*/
	public void showUserList(ArrayList<String> userList)
	{
		for (String user:userList)
		{
			addUser(user);
		}
		userLabel.setText("Online (" + userArrayList.size() + "):");
	}
	
	/** show history messages*/
	public void showHistoryMsg(ArrayList<String> msgList)
	{
		for (int i = 0; i < msgList.size(); i++)
		{
			String content = msgList.get(i).split("[**]")[0];
			String sender = msgList.get(i).split("[**]")[2];
			String sendTime = msgList.get(i).split("[**]")[4];
			insertMsg(msgScrollPane, showMsgTextPane, sender + " (" + sendTime + "):", content);
		}
		insertMsg(msgScrollPane, showMsgTextPane, 
				"[Above are histroy messages (only show the last 10 messages)]", "");
	}
	
	/** interface of insertMsg for ClientThread*/
	public void addMsg(String sender, String content, String sendtime)
	{
		insertMsg(msgScrollPane, showMsgTextPane, sender + "( " + sendtime + "):", content);
	}
	
	public void registerSuccess(JFrame registerFrame)
	{
		JOptionPane.showMessageDialog(registerFrame, 
				"Register succeed!",
				"Succeed",
				JOptionPane.INFORMATION_MESSAGE);
		registerFrame.dispose();
	}
	
	public void registerFail(JFrame registerFrame)
	{
		JOptionPane.showMessageDialog(registerFrame, 
				"Username exists!",
				"Fail",
				JOptionPane.WARNING_MESSAGE);
	}
	
	public void exitLogin(String username)
	{
		userArrayList.clear();
		userLabel.setText("Online (0):");
		nameLabel.setVisible(true);
		nameTextField.setVisible(true);
		pswdLabel.setVisible(true);
		pswdField.setVisible(true);
		loginButton.setVisible(true);
		registerButton.setVisible(true);
		welcomeLabel.setVisible(false);
		exitButton.setVisible(false);
		showMsgTextPane.setText("");
		userListModel.clear();
	}
	
	public class ClientRegister implements ActionListener
	{	
		private JFrame registerFrame;
		private JPanel panel;
		private JLabel noteLabel;
		private JLabel noteLabel1;
		private JLabel noteLabel2;
		private JLabel userLabel;
		private JLabel pswdLabel;
		private JTextField userTextField;
		private JTextField pswdField;
		
		public JFrame getFrame()
		{
			return registerFrame;
		}
		
		public ClientRegister(JFrame mainFrame)
		{
			registerFrame = new JFrame("Register");
			panel = new JPanel();
			panel.setLayout(null);
			//registerFrame.setLayout(new BorderLayout());
			registerFrame.setSize(400, 300);
			registerFrame.setResizable(false);
			registerFrame.setLocationRelativeTo(mainFrame);
			registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			registerFrame.setContentPane(panel);
			
			noteLabel = new JLabel("NOTE:");
			noteLabel1 = new JLabel("Username can only start with a character.");
			noteLabel2 = new JLabel("Password must be at least six characters long.");
			userLabel = new JLabel("Username:");
			pswdLabel = new JLabel("Password:");
			
			userTextField = new JTextField();
			pswdField = new JPasswordField();
			
			JButton registerBtn = new JButton("Register");
			
			noteLabel.setForeground(Color.red);
			noteLabel.setBounds(80, 10, 65, 10);
			panel.add(noteLabel);
			noteLabel1.setForeground(Color.red);
			noteLabel1.setBounds(85, 30, 300, 15);
			panel.add(noteLabel1);
			noteLabel2.setForeground(Color.red);
			noteLabel2.setBounds(85, 50, 300, 15);
			panel.add(noteLabel2);
			userLabel.setBounds(50, 95, 65, 10);
			panel.add(userLabel);
			userTextField.setBounds(130, 90, 160, 25);
			panel.add(userTextField);
			pswdLabel.setBounds(50, 145, 65, 10);
			panel.add(pswdLabel);
			pswdField.setBounds(130, 140, 160, 25);
			panel.add(pswdField);
			registerBtn.setBounds(160, 200, 90, 20);
			panel.add(registerBtn);
			
			registerFrame.setVisible(true);
			
			registerBtn.addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (e.getActionCommand() == "Register")
			{
				String username = userTextField.getText();
				String pswd = pswdField.getText();
				
				boolean flag = true;
				
				//test whether username and password are valid
				if (username.length() == 0 || pswd.length() == 0)
				{
					JOptionPane.showMessageDialog(registerFrame, 
							"Username or Password cannot be empty!",
							"Warning",
							JOptionPane.WARNING_MESSAGE);
					flag = false;
				}
				else if (username.charAt(0) >= '0' && username.charAt(0) <= '9')
				{
					JOptionPane.showMessageDialog(registerFrame, 
							"Username cannot start with a number!",
							"Warning",
							JOptionPane.WARNING_MESSAGE);
					flag = false;
				}
				else if (pswd.length() < 6)
				{
					JOptionPane.showMessageDialog(registerFrame, 
							"Password must be at least six characters long!",
							"Warning",
							JOptionPane.WARNING_MESSAGE);
					flag = false;
				}
				
				if (flag == true)
				{
					sendToServer("REGISTER", username + "##" + pswd);
				}
			}
		}
	}

	public static void main(String[] args)
	{
		Client client = new Client();
	}
}
