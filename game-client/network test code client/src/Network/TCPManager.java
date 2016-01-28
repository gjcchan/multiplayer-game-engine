package Network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import Network.Interface.ObjectTransmitter;
import frameworkObjects.RepackagedPlayer;
import frameworkObjects.sendData;
import sharedResources.globalShare;
import sharedResources.playerName;

public class TCPManager implements Runnable {
	private Socket connection;
	private DataOutputStream outToServer;
	private InputStream persistentInputStream;
	private BufferedReader streamReader;
	private DataInputStream dataReader;
	//players index
	public  globalShare shared;
	
	public TCPManager(String IP, int port, globalShare newShared)
	{
		try {
			connection = new Socket(IP, port);
			connection.setTcpNoDelay(true);
			outToServer = new DataOutputStream(connection.getOutputStream());
			shared = newShared;
			setUsername(shared.username);
			refreshNames();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}		
	}
	
	private void refreshNames() 
	{
		shared.playerNames.clear();
		transmitToServer(clientCommandDefinitions.GET_PLAYER_USERNAMES);
	}
	private void setUsername(String name) 
	{
		transmitToServer(clientCommandDefinitions.UPDATE_PLAYER_USERNAME + " " + name);
	}
	public void transmitToServer(String data)
	{
		try {
		outToServer.writeBytes(data +" \n");
		outToServer.flush();
		} catch (IOException e) {
			System.out.println("ERROR: transmitting to server");
			e.printStackTrace();
}
	}
	
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException, StreamCorruptedException {
		System.out.println(new String(data, "UTF-8"));
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);	    
	    Object a =  is.readObject();
	    is.close();
	    in.close();
	    return a;
	}
	
	public void parseString(String input)
	{
		//parses for string commands
		input = input.substring(input.indexOf(':') == -1 ? 0 : input.indexOf(':')+1);
		//assign player an index no. 
		//if(stringBuffer.substring(0).equals(serverCommandDefinitions.ASSIGN_INDEX))
		if(input.charAt(0) == serverCommandDefinitions.ASSIGN_INDEX)
		{
			String[] tempArray = input.split(" ");
			synchronized(shared)
			{
				//get players index 
				shared.index = Integer.parseInt(tempArray[1]);					
			}
		}
		//gives client player names
		//else if(stringBuffer.substring(0).equals(serverCommandDefinitions.RECEIVE_PLAYER_NAMES)) 
		else if(input.substring(0).charAt(0) == serverCommandDefinitions.RECEIVE_PLAYER_NAMES)
		{
			String[] listOfNames = input.substring(2).split(" ");
			for(String x : listOfNames) 
			{
				String[] tempString = x.split("-");
				synchronized(shared)
				{
					try {
						shared.playerNames.put(Integer.parseInt(tempString[0]),  tempString[1]);
					} catch (Exception ex) {
						System.out.println("ERROR WRITING playerName: " +  Arrays.toString(tempString));
					}
				}
			}
		}		
	}
	/************************ PARSING COMMANDS **************************
	 * 
	 *  Use the dictionary create a command list for TCP transmit usage.
	 *  Users can choose to use TCP to transmit game-data if desired, but for this framework
	 *  it is design to transmit strings only and parse them accordingly. If user feels need to send
	 *  serializable objects, then it is done through custom code
	 *  
	 */
	
	@Override
	public void run() 
	{
		String stringBuffer;
		byte[] headerBuffer = new byte[3];
		byte[] inputBuffer;
		int dataLength = 0;
		int objectID = 0;
		//used to calculating time between packet
		long initialPacket =  System.nanoTime();
		//end of stream flag
		ObjectInputStream is;
		try {
			persistentInputStream = connection.getInputStream();
			streamReader = new BufferedReader(new InputStreamReader(this.persistentInputStream, "UTF-8"));
			//dataReader = new DataInputStream(connection.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		} 
		//listen for TCP msg from server
		while(true)
		{
			try
			{					
				//stringBuffer = streamReader.readLine();				
				//rebuild the byteArray recognizes use -128 int
				
				//read header
				headerBuffer[0] = (byte) persistentInputStream.read();
				headerBuffer[1] = (byte) persistentInputStream.read();
				headerBuffer[2] = (byte) persistentInputStream.read();
				//System.out.println(Arrays.toString(headerBuffer));
				//add compile length of packet value 
				dataLength = (int)(headerBuffer[0]& 0xFF) + (int)(headerBuffer[1]& 0xFF * 256);
				
				//retrieve objectID from the 4 MSB from the header
				objectID = (int)(headerBuffer[2] >>> 4) & 0xFF;
				
				//strip out objectID of third byte (& 0xFF operator converts signed to unsigned int) and retrieve data length
				headerBuffer[2] = (byte) (headerBuffer[2] << 4);
				headerBuffer[2] = (byte) (headerBuffer[2] >>> 4);
				//System.out.println(Arrays.toString(headerBuffer));
				dataLength += (int)(headerBuffer[2]& 0xFF * 65536);
				//read data
				inputBuffer = new byte[dataLength];
				persistentInputStream.read(inputBuffer);
				//System.out.println(Arrays.toString(inputBuffer));
				//stringBuffer = new String(inputBuffer, "UTF-8");
				//System.out.println(stringBuffer);
				/*byte[] byteArray = new byte[byteList.size()-1];
				for(int i = 0; i < byteList.size()-1; i++)
				{
					byteArray[i] = byteList.get(i);
				}
				System.out.println(Arrays.toString(byteArray));
				System.out.println(byteArray.length);
				stringBuffer = new String(byteArray, "UTF-8");
				System.out.println(stringBuffer);
				*/
			    try {
				    is = new ObjectInputStream(new ByteArrayInputStream(inputBuffer));	
					stringBuffer = new String(inputBuffer, "UTF-8");
					Object a = is.readObject();
					if(a instanceof String) {
						System.out.println((String)a); 
						parseString((String)a);
					}
					else if(a instanceof sendData)
					{
						shared.betweenState = (System.nanoTime() - initialPacket)/ 1000000;
						initialPacket = System.nanoTime();
						//move current state to old state to allow new state to come in (interpolation)
						if(shared.UDPstate.playerList != null)
						{
							shared.previousState.playerList = new ArrayList<RepackagedPlayer>(shared.UDPstate.playerList);			
						}
						shared.UDPstate.playerList = ((sendData)a).playerList;
					}
					is.close();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (StreamCorruptedException sce) {
					sce.printStackTrace();
				} catch (EOFException eof) {
					eof.printStackTrace();
				}
			    
			    				
				//stringBuffer = stringBuffer.substring(0, stringBuffer.length()-2);
				
				//ByteArrayInputStream test = new ByteArrayInputStream(stringBuffer.getBytes());
				//ObjectInputStream objectStream = new ObjectInputStream(test);
				//sendData loopPrivateGameState = (sendData)objectStream.readObject();
				
				//insert code for parsing objects here
				
				/*
				//parses for string commands
				stringBuffer = stringBuffer.substring(stringBuffer.indexOf(':') == -1 ? 0 : stringBuffer.indexOf(':')+1);
				//assign player an index no. 
				//if(stringBuffer.substring(0).equals(serverCommandDefinitions.ASSIGN_INDEX))
				if(stringBuffer.charAt(0) == serverCommandDefinitions.ASSIGN_INDEX)
				{
					String[] tempArray = stringBuffer.split(" ");
					synchronized(shared)
					{
						//get players index 
						shared.index = Integer.parseInt(tempArray[1]);					
					}
				}
				//gives client player names
				//else if(stringBuffer.substring(0).equals(serverCommandDefinitions.RECEIVE_PLAYER_NAMES)) 
				else if(stringBuffer.substring(0).charAt(0) == serverCommandDefinitions.RECEIVE_PLAYER_NAMES)
				{
					String[] listOfNames = stringBuffer.substring(2).split(" ");
					for(String x : listOfNames) 
					{
						String[] tempString = x.split("-");
						synchronized(shared)
						{
							try {
								shared.playerNames.put(Integer.parseInt(tempString[0]),  tempString[1]);
							} catch (Exception ex) {
								System.out.println("ERROR WRITING playerName: " +  Arrays.toString(tempString));
							}
						}
					}
				}
				*/
				//does something else
			}
			catch (IOException e)
			{
				e.printStackTrace();
				System.out.println(e);
				break;
			} 	
		}
	}
	
}
