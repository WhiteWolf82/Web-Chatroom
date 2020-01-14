import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ServerThread extends Thread
{
	private static final String USER = "xxx";
	private static final String PSWD = "xxxxxx";
	
	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://localhost:3306/WebChatroom?useSSL=false&serverTimezone=UTC";
	
    private ServerMain.Server server;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	private ArrayList<User> userList;
	private User user;
	private boolean exitFlag;
	
	public ServerThread(ServerMain.Server server, User user, ArrayList<User> userList)
	{
		this.server = server;
		this.user = user;
		this.userList = userList;
		this.bufferedReader = user.getBufferedReader();
		this.printWriter = user.getPrintWriter();
	}
	
	public void run()
	{
		try
		{
			while(!exitFlag)
			{
				//read messages from the client
				String msg = bufferedReader.readLine();
				if (msg != null)
					handleMsg(msg);
			}
		}
		catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public ArrayList<User> getUserList()
	{
		return userList;
	}
	
	/**get the current time*/
	private String getDatetime()
	{
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}
	
	//add a new user to the user list
	public void addUser(User user)
	{
		if (!userList.contains(user))
			userList.add(user);
	}
	
	//send message to the client
	public void sendToClient(String type, String content)
	{
		printWriter.println(type + "##" + content);
		printWriter.flush(); 
	}
	
	//send message to all the other clients
	private void sendMsgToOthers(String type, String msg)
	{
		for (User each : userList)
		{
			if (!user.getUserName().equals(each.getUserName()))
			{
				each.getPrintWriter().println(type + "##" + msg);
				each.getPrintWriter().flush();
			}
		}
	}
	
	//send history messages
	public void sendHistoryMsg(int type, String receiver)
	{
		Connection conn = null;
		Statement stmt = null;
		try
		{
			//register for JDBC Driver
			Class.forName(JDBC_DRIVER);
			//open connection
			conn = DriverManager.getConnection(DB_URL, USER, PSWD);
			//check record
			stmt = conn.createStatement();
			String sqlString;
			ResultSet rs;
			//broadcasting
			if (type == 1)
			{
				sqlString = "select * from MsgHistory;";
				rs = stmt.executeQuery(sqlString);
				String msgToClient = "";
				int msgCnt = 0;
				//only show the last 10 history messages
				while(rs.next() && msgCnt < 10)
				{
					String content = rs.getString("content");
					String sender = rs.getString("sender");
					String sendtime = rs.getString("sendtime");
					msgToClient = msgToClient + content + "**"
							+ sender + "**" + sendtime + "##";
					msgCnt++;
				}
				//only send if there are history messages
				if (msgCnt != 0)
					sendToClient("HISTORY", msgToClient);
			}
			//unicasting
			else if (type == 2)
			{
				//find messages between these two users
				sqlString = "select * from P2PMsgHistory " +
						"where sender = '" + user.getUserName() + "' and receiver = '" +
						receiver + "';";
				rs = stmt.executeQuery(sqlString);
				String msgHistory = "";
				int msgCnt = 0;
				//only show the last 10 history messages
				while(rs.next() && msgCnt < 10)
				{
					String content = rs.getString("content");
					String sender = rs.getString("sender");
					String sendtime = rs.getString("sendtime");
					msgHistory = msgHistory + content + "**"
							+ sender + "**" + sendtime + "##";
					msgCnt++;
				}
				//only send if there are history messages
				if (msgCnt != 0)
				{
					//send to this client
					sendToClient("P2PHISTORY", receiver + "##" + msgHistory);
					//also send to its counterpart
					for (User each : userList)
					{
						if (each.getUserName().equals(receiver))
						{
							each.getPrintWriter().println("P2PHISTORY##" + user.getUserName() + "##" + msgHistory);
							each.getPrintWriter().flush();
							break;
						}
					}
				}
			}
		} 
		catch(SQLException se)
		{
			System.out.println("Database select error!");
			se.printStackTrace();
		}
		catch(Exception ex)
		{
			System.out.println("Forname exception!");
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			}
			catch(SQLException se)
			{
				se.printStackTrace();
			}
		}
	}
	
	//update the user's name
	public void updateUsername(int userID, String userName)
	{
		for (User each : userList)
		{
			if (each.getUserID() == userID)
			{
				each.setUserName(userName);
				break;
			}
		}
	}
	
	//insert the message to the database
	public void insertMsgToDB(String sender, String receiver, String content, String sendtime, int type)
	{
		Connection conn = null;
		Statement stmt = null;
		try
		{
			//register for JDBC Driver
			Class.forName(JDBC_DRIVER);
			//open connection
			conn = DriverManager.getConnection(DB_URL, USER, PSWD);
			//insert record
			stmt = conn.createStatement();
			String sqlString = "";
			//broadcasting
			if (type == 1)
			{
				sqlString = "insert into MsgHistory(content, sender, sendtime) "
						+ "values('" + content + "', '" + sender + "', '" + sendtime + "');";
			}
			//unicasting
			else if (type == 2)
			{
				sqlString = "insert into P2PMsgHistory(content, sender, receiver, sendtime) "
						+ "values('" + content + "', '" + sender + "', '" + receiver + "', '" + sendtime + "');";
			}
			else
			{
				System.out.println("Error! Invalid type!");
				return;
			}
			if (stmt.executeUpdate(sqlString) != 0)
			{
				System.out.println("Message stored to database!");
			}
		}
		catch(SQLException se)
		{
			System.out.println("Database insert error!");
			se.printStackTrace();
		}
		catch(Exception ex)		//forName exception
		{
			System.out.println("Forname exception!");
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			}
			catch(SQLException se)
			{
				se.printStackTrace();
			}
		}
	}
	
	//register for the user, and store its info to the database
	public boolean registerUser(String username, String pswd)
	{
		Connection conn = null;
		Statement stmt = null;
		try
		{
			//register for JDBC Driver
			Class.forName(JDBC_DRIVER);
			//open connection
			conn = DriverManager.getConnection(DB_URL, USER, PSWD);
			//insert record
			stmt = conn.createStatement();
			String sqlString = "insert into users(username, pswd) "
					+ "values('" + username + "', '" + pswd + "');";
			if (stmt.executeUpdate(sqlString) != 0)
				return true;
			else
				return false;
		}
		catch(SQLIntegrityConstraintViolationException sie)
		{
			return false;
		}
		catch(SQLException se)
		{
			System.out.println("Database insert error!");
			se.printStackTrace();
			return false;
		}
		catch(Exception ex)		//forName exception
		{
			System.out.println("Forname exception!");
			ex.printStackTrace();
			return false;
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			}
			catch(SQLException se)
			{
				se.printStackTrace();
			}
		}
	}
	
	//login the user
	public boolean loginUser(String username, String pswd)
	{
		Connection conn = null;
		Statement stmt = null;
		try
		{
			//register for JDBC Driver
			Class.forName(JDBC_DRIVER);
			//open connection
			conn = DriverManager.getConnection(DB_URL, USER, PSWD);
			//check record
			stmt = conn.createStatement();
			String sqlString = "select * from users where "
					+ "username = '" + username + "' and pswd = '" + pswd + "';";
			ResultSet rs = stmt.executeQuery(sqlString);
			int cnt = 0;
			while (rs.next())
			{
				cnt++;
			}
			if (cnt != 0)
			{
				return true;
			}
			else
				return false;
		}
		catch(SQLException se)
		{
			System.out.println("Database select error!");
			se.printStackTrace();
			return false;
		}
		catch(Exception ex)
		{
			System.out.println("Forname exception!");
			ex.printStackTrace();
			return false;
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			}
			catch(SQLException se)
			{
				se.printStackTrace();
			}
		}
	}
	
	//handle the message from the client
	public void handleMsg(String msg) throws InterruptedException
	{
		String msgType = msg.split("##")[0];
		switch(msgType)
		{
		case "REGISTER":
			if (registerUser(msg.split("##")[1], msg.split("##")[2]) == true)
			{
				sendToClient("REGISTERSUCCESS", msg.split("##")[1]);
			}
			else
			{
				sendToClient("REGISTERFAIL", msg.split("##")[1]);
			}
			break;
		case "LOGIN":
			if (loginUser(msg.split("##")[1], msg.split("##")[2]) == true)
			{
				user.setUserName(msg.split("##")[1]);
				updateUsername(user.getUserID(), msg.split("##")[1]);
				server.updateUser(user);  //update the server's info for the user
				sendMsgToOthers("NEWUSER", user.getUserName());
				//send history message to client
				sendHistoryMsg(1, "");
				sendToClient("LOGINSUCCESS", msg.split("##")[1]);
			}
			else
			{
				sendToClient("LOGINFAIL", msg.split("##")[1]);
			}
			break;
		case "DISCONNECT":
			sendMsgToOthers("RMUSER", user.getUserName());
			//userList.remove(user);
			server.delUser(user.getUserName());
			try
			{
				sendToClient("DISCONNECT", user.getUserName());
				user.closeBr();
				user.closePr();
				user.closeSocket();
				server.removeThread(this.getId());
				exitFlag = true;
				System.out.println("User " + user.getUserName() + " has disconnected.");
			}
			catch(IOException e)
			{
				System.out.println("Error! Cannot close socket at " + user.getUserName());
				e.printStackTrace();
			}
			break;
		case "SENDMSG":
			sendToClient("SENDMSG", user.getUserName() + "##" + msg.split("##")[1] + "##" + getDatetime());
			sendMsgToOthers("SENDMSG", user.getUserName() + "##" + msg.split("##")[1] + "##" + getDatetime());
			insertMsgToDB(user.getUserName(), "", msg.split("##")[1], getDatetime(), 1);
			break;
		case "USERLIST":
			String sendContent = "";
			for (User each : userList)
			{
				if (!each.getUserName().equals(user.getUserName()))
					sendContent = sendContent + each.getUserName() + "##";
			}
			sendToClient("USERLIST", sendContent);
			break;
		case "EXIT":	//may login again, so don't remove
			sendMsgToOthers("RMUSER", user.getUserName());
			//userList.remove(user);
			//server.delUser(user.getUserName());
			sendToClient("EXIT", user.getUserName());
			break;
		case "CHAT":
			String chatUsername = msg.split("##")[1];
			for (User each : userList)
			{
				if (each.getUserName().equals(chatUsername))
				{
					each.getPrintWriter().println("CHAT" + "##" + user.getUserName());
					each.getPrintWriter().flush();
					break;
				}
			}
			//send history messages to the client and its chat partner
			sendHistoryMsg(2, chatUsername);
			break;
		case "P2PMSG":
			String recvUsername = msg.split("##")[1];
			String msgContent = msg.split("##")[2];
			for (User each : userList)
			{
				if (each.getUserName().equals(recvUsername))
				{
					each.getPrintWriter().println("P2PMSG" + "##" + user.getUserName() + "##" + msgContent + "##" + getDatetime());
					each.getPrintWriter().flush();
					break;
				}
			}
			insertMsgToDB(user.getUserName(), recvUsername, msgContent, getDatetime(), 2);
			break;
		case "EXITP2P":
			String exitUsername = msg.split("##")[1];
			for (User each : userList)
			{
				if (each.getUserName().equals(exitUsername))
				{
					each.getPrintWriter().println("EXITP2P" + "##" + user.getUserName());
					each.getPrintWriter().flush();
					break;
				}
			}
			break;
		default:
			break;
		}
	}
}
