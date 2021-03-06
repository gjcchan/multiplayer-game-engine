package corelogic;

import java.util.Queue;
import java.util.TreeMap;

import network.gameInputObject;
import gameEntities.Player;

//UDP and (TCP if enabled and implemented) packethandler
public class PacketHandler implements Runnable {
	//all the incoming data to update the new inputs
	private Queue<gameInputObject> toDoList;
	//record of spawned players
	private TreeMap<String, Player> players;
	//TCP queue holder, used for handling communcations between TCP and game loop

	//a map that holds all the players info using string for now
	private TreeMap<String, Integer> playerList;
	
	public PacketHandler(Queue<gameInputObject> newStream, TreeMap<String, Player> newPlayers)
	{
		toDoList = newStream;
		players = newPlayers;
		playerList = new TreeMap<String, Integer>();
	}
	public synchronized void run() 
	{	
		//this is for looping with new user inputs
		while(true)
		{
			synchronized(toDoList)
			{
				try {
					toDoList.wait();
					processLogic(toDoList.poll());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}	
	//spawn player
	public void spawnPlayer(String IP, int newPort)
	{	
		Player newPlayer =  new Player(100,100, newPort);
		newPlayer.index = playerList.get(IP);
		players.put(IP, newPlayer);	
	}
	public void removePlayer(String IP)
	{
		players.remove(IP);
		playerList.remove(IP);
		//add something to clear indexes here
	}
	private void processLogic(gameInputObject input)
	{	
		//spawn player if not found
		if(!players.containsKey(input.IPAddress))
		{
			spawnPlayer(input.IPAddress, input.port);
		}
		//key conditionals
		else 
		{
			Player currentPlayer = players.get(input.IPAddress);
			if(input.port != currentPlayer.port)
				currentPlayer.port = input.port;
			
			currentPlayer.rightKeyPress = input.keyRight;
			currentPlayer.leftKeyPress = input.keyLeft;
			currentPlayer.isJumping = input.keyUp;
		}
	}
	public void addToList(String IP, int index) 
	{
		playerList.put(IP, index);
	}
	
	/*thread for TCP handling (completely useless for now since 'spawner' is in TCP connection
	* can be used in the future if i want to break into independence
	*
	*
	private class tcpProcessor implements Runnable
	{
		private Queue<String> TCPqueue;
		private TreeMap<String, String> playerList;
		
		public tcpProcessor(Queue<String> newQueue,TreeMap<String, String> newList )
		{
			TCPqueue = newQueue;
			playerList = newList;
		}
		@Override
		public void run() 
		{
			synchronized(TCPqueue)
			{
				try {
					TCPqueue.wait();
					processTcpRequests();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
		private void processTcpRequests()
		{
			String request[];
			while(TCPqueue.size() > 0)
			{
				String constRequest = TCPqueue.poll();
				request = constRequest.split(" ");
				if(request[0] == "ADD")
				{
					playerList.put(request[1], request[2]);
				}
				else if(request[0] == "REMOVE")
				{
					playerList.remove(request[1]);
				}
			}
		}
		
	} */
}
