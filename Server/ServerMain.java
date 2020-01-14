import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMain
{
	public class Server
	{
		private ServerSocket serverSocket;
		private int port;
		private ArrayList<User> allUserList;
		private ArrayList<ServerThread> allThreads;
		private int clientCnt;
		
		public Server(int port)
		{
			this.port = port;
			this.clientCnt = 0;
			try
			{
				this.serverSocket = new ServerSocket(port);
				this.allUserList = new ArrayList<>();
				this.allThreads = new ArrayList<>();
				System.out.println("Server is built successfully!");
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		//listen to the connection requests from clients
		public void startListen() throws IOException
		{
			while(true)
			{
				Socket socket = serverSocket.accept();
				String defaultUsername = "User" + Integer.toString(clientCnt);
				User newUser = new User(defaultUsername, clientCnt, socket);
				//allUserList.add(newUser);
				//create a thread to handle messages from this client
				ServerThread serverThread = new ServerThread(this, newUser, allUserList);
				serverThread.start();
				allThreads.add(serverThread);
				System.out.println(defaultUsername + " has connected.");
				clientCnt += 1;
			}
		}
		
		public void closeServersocket() throws IOException
		{
			serverSocket.close();
		}
		
		public synchronized void updateUser(User user)
		{
			if (!allUserList.contains(user))
				allUserList.add(user);
			//let all the other threads know
			for (ServerThread thread : allThreads)
			{
				thread.addUser(user);
			}
			/*for (ServerThread thread : allThreads)
			{
				thread.updateUsername(userID, newUsername);
			}*/
		}
		
		/**delete a user since it has disconnected*/
		public synchronized void delUser(String userName)
		{
			int delUserID = -1;
			for (int i = 0; i < allUserList.size(); i++)
			{
				if (allUserList.get(i).getUserName().equals(userName))
				{
					delUserID = i;
					break;
				}
			}
			if (delUserID != -1)	//if found the user
			{
				//update the other users' ID
				for (int i = delUserID + 1; i < allUserList.size(); i++)
				{
					allUserList.get(i).setUserID(allUserList.get(i).getUserID() - 1);
				}
				allUserList.remove(delUserID);
				clientCnt--;
			}
			else
			{
				System.out.println("Error, cannot find user to be deleted!");
			}
		}
		
		/**remove the deleted user's thread*/
		public synchronized void removeThread(long threadID)
		{
			for (ServerThread thread : allThreads)
			{
				if (thread.getId() == threadID)
				{
					allThreads.remove(thread);
					break;
				}
			}
		}
	}

	public static void main(String[] args)
	{
		Server server = new ServerMain().new Server(3226);
		try
		{
			server.startListen();
		} 
		catch (IOException e)
		{
			System.out.println("Server error!");
			e.printStackTrace();
		}
		try
		{
			server.closeServersocket();
		}
		catch(IOException e)
		{
			System.out.println("Error! Cannot close server socket.");
			e.printStackTrace();
		}
	}
}
