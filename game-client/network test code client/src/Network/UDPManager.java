package Network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import Network.Interface.ObjectTransmitter;
import sharedResources.globalShare;
import frameworkObjects.RepackagedPlayer;
import frameworkObjects.receiveData;
import frameworkObjects.sendData;

public class UDPManager extends ObjectTransmitter implements Runnable{
	private int port;
	//transmit port
	private int outPort;
	private DatagramSocket listener;
	private globalShare shared;
	
	//used for interpolation timing
	private long initialPacket;
	
    //shared global gamestate for update
    private sendData gameState;
    
	//input streams, used to rebuild objects from byte arrays
	private ByteArrayInputStream dataInputStream;
	private ObjectInputStream objectStream;

	//output streams used to turn objects into byte arrays
	ByteArrayOutputStream dataOutputStream;
	ObjectOutputStream objectOut;
	

	
	public UDPManager(int newPort)
	{
		try {
			listener = new DatagramSocket(newPort);
			sendDataBuffer = new byte[sendBufferSize];
			receiveDataBuffer = new byte[receiveBufferSize];		
			outPort = newPort;
			gameState = new sendData();
			//initialize serializer streams
			dataOutputStream = new ByteArrayOutputStream();
			objectOut = new ObjectOutputStream(dataOutputStream);		
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		
	}
	public UDPManager(int newPort, globalShare resources)
	{
		try {
			listener = new DatagramSocket(newPort);
			sendDataBuffer = new byte[sendBufferSize];
			receiveDataBuffer = new byte[receiveBufferSize];		
			outPort = newPort;
			shared = resources;
			//initialize serializer streams
			dataOutputStream = new ByteArrayOutputStream();
			objectOut = new ObjectOutputStream(dataOutputStream);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		
	}
	@Override
	public void run() 
	{
		while(true)
		{
			DatagramPacket receivePacket = new DatagramPacket(receiveDataBuffer, receiveDataBuffer.length);
			try {			
				//listen block waiting for object to come in
				listener.receive(receivePacket);
				
				//mark time between packets for more accurate interpolation
				shared.betweenState = (System.nanoTime() - initialPacket)/ 1000000;
				initialPacket = System.nanoTime();
				
				//get where source port is (useless for now)
				port = receivePacket.getPort();
				//initialize the streams to read the data
				
				//initialize and run inflater
				//ByteArrayOutputStream output = new ByteArrayOutputStream(receiveBufferSize);
				//InflaterOutputStream ios = new InflaterOutputStream(output);				
				//ios.write(receiveDataBuffer);
				//ios.finish();
				//ios.close();
				//redeploy inflated packet to buffer
				//receiveDataBuffer = output.toByteArray();
				//output.close();
				dataInputStream = new ByteArrayInputStream(receiveDataBuffer);
				objectStream = new ObjectInputStream(dataInputStream);
				
				//set new receive as new state		
				
				//turn bytes into object
				sendData loopPrivateGameState = (sendData)objectStream.readObject();
				//move current state to old state to allow new state to come in (interpolation)
				if(shared.UDPstate.playerList != null)
				{
					shared.previousState.playerList = new ArrayList<RepackagedPlayer>(shared.UDPstate.playerList);			
				}
				shared.UDPstate.playerList = loopPrivateGameState.playerList;
				//close up existing stream
				objectStream.close();
				dataInputStream.close();		
				//System.out.println(loopPrivateGameState.playerList.get(0).posX+" , " +loopPrivateGameState.playerList.get(0).posY);
			} catch (StreamCorruptedException e) {
				//stream header read error
				System.out.println("corrupted packet or unrecognized object");
			} catch (ClassNotFoundException e) {
				// When rebuilding object fails
				System.out.println("unrecognized object");
			}catch (IOException e) {
					// general network failure
					e.printStackTrace();
				}			
		}
		
	}
	
	public void dispose() throws IOException
	{
		//close listener and its sessions
		listener.close();
		
		//close serializers
		objectOut.close();
		dataOutputStream.close();
	}
	public void transmitObject(Object c, String IP, int newPort) throws IOException
	{
		dataOutputStream = new ByteArrayOutputStream();
		objectOut = new ObjectOutputStream(dataOutputStream);
		objectOut.writeObject(c);
		//System.out.println(dataOutputStream.toByteArray().length);
		//Deflater dfl = new Deflater(Deflater.BEST_COMPRESSION, true);
		//ByteArrayOutputStream tempByteArray = new ByteArrayOutputStream();
		//DeflaterOutputStream dos = new DeflaterOutputStream(tempByteArray, dfl);
		//dos.write(dataOutputStream.toByteArray());
		//dos.finish();
		//dos.flush();
		
		sendDataBuffer = dataOutputStream.toByteArray();//tempByteArray.toByteArray();
		//System.out.println(tempByteArray.toByteArray().length);
		DatagramPacket packet = new DatagramPacket(sendDataBuffer, sendDataBuffer.length, InetAddress.getByName(IP), newPort);
		
		//implement delta encoding here?
		
		listener.send(packet);
		objectOut.close();
		dataOutputStream.close();
		
		//tempByteArray.close();
		//dos.close();
		//dfl.end();
	}
	public void transmitObject(Object c, String IP) throws IOException
	{
		transmitObject(c, IP, outPort);
	}		
}
