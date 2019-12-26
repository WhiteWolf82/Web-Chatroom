import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread extends Thread
{
	private Socket socket;
	private Client client;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	private boolean exitFlag;
	
	public ClientThread(Socket socket, Client client)
	{
		this.socket = socket;
		this.client = client;
		try
		{
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			printWriter = new PrintWriter(socket.getOutputStream());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		try
		{
			while(!exitFlag)
			{
				String msgFromServer = bufferedReader.readLine();
				if (msgFromServer != null)
					handleMsg(msgFromServer);
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void handleMsg(String msg)
	{
		String msgType = msg.split("##")[0];
		System.out.println(msg);
		switch(msgType)
		{
		case "REGISTERSUCCESS":
			client.registerSuccess(client.getChildFrame());;
			break;
		case "REGISTERFAIL":
			client.registerFail(client.getChildFrame());;
			break;
		case "LOGINSUCCESS":
			String username = msg.split("##")[1];
			client.loginSuccess(username);
			client.addUser(username);
			break;
		case "LOGINFAIL":
			client.loginFail();
			break;
		case "CONNECT":
			break;
		case "DISCONNECT":
			try
			{
				bufferedReader.close();
				printWriter.close();
				socket.close();
				client.exitLogin(msg.split("##")[1]);
				client.disconnect();
				exitFlag = true;
			}
			catch(IOException e)
			{
				System.out.println("Error! Cannot close socket.");
				e.printStackTrace();
			}
			break;
		case "SENDMSG":
			String sendUser = msg.split("##")[1];
			String sendContent = msg.split("##")[2];
			String sendTime = msg.split("##")[3];
			client.addMsg(sendUser, sendContent, sendTime);
			break;
		case "USERLIST":
			ArrayList<String> userList = new ArrayList<>();
			//the first one is msgType, so eliminate
			for (int i = 1; i < msg.split("##").length; i++)
			{
				userList.add(msg.split("##")[i]);
			}
			client.showUserList(userList);
			break;
		case "NEWUSER":
			client.addUser(msg.split("##")[1]);
			break;
		case "RMUSER":
			client.removeUser(msg.split("##")[1]);
			break;
		case "HISTORY":
			ArrayList<String> sendMsgs = new ArrayList<>();
			//the first one is msgType, so eliminate
			for (int i = 1; i < msg.split("##").length; i++)
			{
				sendMsgs.add(msg.split("##")[i]);
			}
			client.showHistoryMsg(sendMsgs);
			break;
		case "EXIT":
			client.exitLogin(msg.split("##")[1]);
			break;
		default:
			break;
		}
	}
}