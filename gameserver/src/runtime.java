

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.TreeMap;

import network.ConnectionInfoUDP;
import network.ConnectionManager;
import network.SessionManager;
import network.gameInputObject;
import network.objects.receiveData;
import corelogic.BroadcastTimer;
import corelogic.GameState;
import corelogic.PacketHandler;
import gameEntities.Player;
import gameObjects.GameStateObject;

/* This network code framework uses one-thread-per-client method, all recieved input request from client is put into a Queue for processing for another 
 * external core to handle. This allows modularity and let it become multipurposed.
 * Developed by George Chan
 * 
 * JAVA 1.5 VERSION TEST RELEASE
 * 2012-2013
 */

// port: UDP listen, port++: TCP listen, port+2: TCP send

//transmit needs to be implemented
public class runtime {
	/* linked-list = better for append new data, arraylist is O(1) to traverse and random access */

	@SuppressWarnings("resource")
	public static void main(String[] args) {	
		if(args.length < 1)
		{
			System.out.println("PORT REQUIRED");
			System.exit(0);
		} 
		/*else if(!args[0].matches("[0-6][0-5][0-5][0-3][0-5]"))
		{
			System.out.println("INVALID PORT SPECIFIED");
			System.exit(0);			
		}*/
		Queue<gameInputObject> UDPqueue = new LinkedList<gameInputObject>();
		TreeMap<String, ConnectionInfoUDP> connections = new TreeMap<String, ConnectionInfoUDP>();
		TreeMap<String, Player> playerList = new TreeMap<String, Player>();
		
		Scanner userInput = new Scanner(System.in);
		int port = Integer.parseInt(args[0]);
		GameStateObject sharedGameState = new GameStateObject(playerList);
		GameState gameProcess = new GameState(sharedGameState, playerList);
		PacketHandler InputTest = new PacketHandler(UDPqueue,playerList);
		
		ConnectionManager UDPtest = new ConnectionManager(port, UDPqueue, connections);
		SessionManager TCPtest = new SessionManager(port+1, connections, InputTest);
		BroadcastTimer broadcasttest = new BroadcastTimer(sharedGameState, UDPtest, TCPtest);
		new Thread(TCPtest).start();
		new Thread(UDPtest).start();
		new Thread(gameProcess).start();
		new Thread(InputTest).start();
		new Thread(broadcasttest).start();
		
	/*	byte[] testbuffer = new byte[1024];
		DatagramSocket socket = null; 
		try {
			socket = new DatagramSocket(4445);port
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} */
		while(true)
		{
			String input = userInput.nextLine();
			//System.out.println("UDP stack: " + UDPqueue);
			//System.out.println("IP table: " + connections);
			if(input.equals("exit"))
				System.exit(0);
			/*try {
				receiveData testPacket = new receiveData();
				testPacket.keyDown = true;
				UDPtest.transmitObject(testPacket, "127.0.0.1", port);
								
				//how to write into an object and send it
	/*			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(byteOut);
				out.writeObject(testPacket);
				testbuffer = byteOut.toByteArray();
				out.close();
				byteOut.close();
				DatagramPacket packet = new DatagramPacket(testbuffer, testbuffer.length, InetAddress.getByName("127.0.0.1"), 7236);
				socket.send(packet); */
				
		/*	} catch (IOException e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		
		
	}

}
