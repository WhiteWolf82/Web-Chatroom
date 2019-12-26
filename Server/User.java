import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class User
{
	private int userID;
	private String userName;
	private Socket socket;
	private BufferedReader br;
	private PrintWriter pr;
	
	User(String name, int ID, Socket socket)
	{
		this.userID = ID;
		this.userName = name;
		this.socket = socket;
		try
		{
			this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.pr = new PrintWriter(socket.getOutputStream());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setUserID(int userID)
	{
		if (userID >= 0)
			this.userID = userID;
	}
	
	public void setUserName(String name)
	{
		if (name != null)
			this.userName = name;
	}
	
	public String getUserName()
	{
		return this.userName;
	}
	
	public int getUserID()
	{
		return userID;
	}
	
	public Socket getUserSocket()
	{
		return this.socket;
	}
	
	public BufferedReader getBufferedReader()
	{
		return this.br;
	}
	
	public PrintWriter getPrintWriter()
	{
		return this.pr;
	}
	
	public void closeSocket() throws IOException
	{
		socket.close();
	}
	
	public void closeBr() throws IOException
	{
		br.close();
	}
	
	public void closePr() throws IOException
	{
		pr.close();
	}
}