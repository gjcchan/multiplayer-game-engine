package network;

import java.io.*;
import java.net.*;
import java.util.*;

import network.ConnectedSession;
import network.extensions.ObjectTransmitter;
import Definitions.clientCommandDefinitions;
import Definitions.serverCommandDefinitions;
import corelogic.GameState;
import corelogic.PacketHandler;
/* 
 * this is a TCP session server, it can be used as a session establisher and reliable communication
 */

/* could use serversocketchannel from Java.nio.*
 * so it can be nonblocking
 */

//use vector instead of list since it's synchronized?
public class SessionManager extends ObjectTransmitter implements Runnable {
	private ServerSocket listener;
	private List<ConnectedSession> connections;
	private TreeMap<String, ConnectionInfoUDP>  IPtable;
	private boolean terminated;
	private PacketHandler spawner;
	private int sessionIndex;
	
	
	//basic connection setup
	public SessionManager(int port)
	{
		connections = new ArrayList<ConnectedSession>();
		terminated = true;
		try {
			listener = new ServerSocket(port);
			//allows reuse of same address so OS can reuse socket
			listener.setReuseAddress(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//connection setup with communication with parent with UDP conjunction
	public SessionManager(int port, TreeMap<String, ConnectionInfoUDP> IP, PacketHandler gProcess)
	{
		sessionIndex = 0;
		connections = new ArrayList<ConnectedSession>();
		IPtable = IP;
		terminated = true;
		spawner = gProcess;
		try {
			listener = new ServerSocket(port);
			listener.setReuseAddress(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public void run()
	{
		terminated = false;
		while(!terminated)
		{
			try {
				Socket newConnection = listener.accept();
				newConnection.setTcpNoDelay(true); //disable nagle's algorithim for maxiumum streaming performance
				//ConnectedSession placeHolder = new ConnectedSession(newConnection);
				ConnectedSession placeHolder = new ConnectedSession(newConnection, this);				
				//adds to the list of accepted incoming UDP packets (remove when implementing authenticator
				IPtable.put(placeHolder.getIP(),new ConnectionInfoUDP(placeHolder.getIP(), sessionIndex));
				connections.add(placeHolder);
				System.out.println("connection established to: " + placeHolder.getIP() + ":" + placeHolder.getPort());
				assignIndexToPlayer(placeHolder.getIP(), sessionIndex);		
				//placeHolder.transmit("I " + sessionIndex); (DEPRECATED)
				placeHolder.transmit(appendTCPHeader(serializeObject("I " + sessionIndex), "string"));
				sessionIndex++;
				new Thread(placeHolder).start();
			}
			 catch (IOException e) {
					terminated = true;
					e.printStackTrace();
				}
		}
	}	
	
	public void assignIndexToPlayer(String IP, int index)
	{
		spawner.addToList(IP, index);
	}
	// transmit data to client
	public void transmit(String data, String IP)
	{
		//horribly, horribly unoptimized way of finding connection, but it will be place holder for now
		try {
			getConnectionByIP(IP).transmit(appendTCPHeader(serializeObject(data), "string"));
		} catch (IOException e) {
			//disconnect from client
			remove(getConnectionByIP(IP));			
			e.printStackTrace();
		}
	}
	//gets IP very inefficient, could use hashmap, will figure out later
	private ConnectedSession getConnectionByIP(String IP) 
	{
		int x = 0;
		while(!connections.get(x).getIP().contentEquals(IP))
			x++;	
		return connections.get(x);
	}
	//for deletion of SessionManager
	public void dispose()
	{
		try {
			listener.close();
			for(ConnectedSession x :connections)
			{
				x.dispose();
			}
			//connections.removeAll(connections); // redundant
			for(Map.Entry<String, ConnectionInfoUDP> x : IPtable.entrySet())
			{
				x = null;
			}
			IPtable.clear();
			IPtable = null;
			connections = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//broadcast message to all clients (DEPRECATED)
	/*public void broadcast(String data)
	{
		for(ConnectedSession x :connections)
		{
			try {
				x.transmit(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}*/
	public void broadcast(String data)
	{
		for(ConnectedSession x :connections)
		{
			try {
				x.transmit(appendTCPHeader(serializeObject(data), "string"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}	
	//transmit serialized objects
	public void transmitObject(Object c, String IP) throws IOException
	{
		transmitObject(serializeObject(c), IP);
	}	
	public void transmitObject(byte[] object, String IP) throws IOException
	{
		try {
			getConnectionByIP(IP).transmit(object);
		} catch (IOException e) {
			//disconnect from client
			remove(getConnectionByIP(IP));			
			e.printStackTrace();
		}
	}
	public void transmitObjectAll(byte[] object) throws IOException 
	{
		for(ConnectedSession x: connections)
		{
			x.transmit(object);
		}
	}

	//halting the connection manager for adding new connections without break current connections
	public void haltConnectionManager()
	{
		terminated = true;
	}
	//remove a specific connection ie disconnect and end session
	public void remove(ConnectedSession toBeDeleted)
	{
		String tempIP = toBeDeleted.getIP();
		IPtable.remove(tempIP);
		spawner.removePlayer(tempIP);
		connections.remove(toBeDeleted);
		try {
			toBeDeleted.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//parses and handles admin stuff
	public String parseRequest(String IP, String newInput)
	{
		String[] parsedOutput = newInput.split(" ");
		//switch case faster -- optimize later
		
		//set or update client username
		if(parsedOutput[0].equals(clientCommandDefinitions.UPDATE_PLAYER_USERNAME))
		{
			if(!IPtable.containsKey(IP))
				return serverCommandDefinitions.ACTION_FAILED;
			if(newInput.split(" ")[1].trim().length() > 1)
			{
				ConnectionInfoUDP modifyPlayer = IPtable.get(IP);
				if(modifyPlayer == null)
					return serverCommandDefinitions.ACTION_FAILED;
				modifyPlayer.username = newInput.split(" ")[1];
				//notify all live users of name change
				broadcast(serverCommandDefinitions.SEND_PLAYER_NAMES + " " + modifyPlayer.index + "-" + modifyPlayer.username);
				return serverCommandDefinitions.ACTION_SUCCESS;
			}
			return serverCommandDefinitions.ACTION_FAILED;
		}
		//fetch list of usernames of all live players
		else if(parsedOutput[0].equals(clientCommandDefinitions.GET_PLAYER_USERNAMES))
		{
			String userNames = new String();
			for(Map.Entry<String, ConnectionInfoUDP> x : IPtable.entrySet())
			{
				if(x.getValue().username.length() != 0) //.isEmpty() not in 1.5
				{
					userNames += x.getValue().index + "-" + x.getValue().username + " ";
				}
			}
			return serverCommandDefinitions.SEND_PLAYER_NAMES + " " + userNames;
		}
		return "UNKNOWN CMD " +serverCommandDefinitions.ACTION_FAILED;
	}	
	public byte[] responseBuilder(String IP, String newInput) 
	{
		try {
			return appendTCPHeader(serializeObject(newInput + ":" + parseRequest(IP, newInput)), "string");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
