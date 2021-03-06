package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import network.extensions.ObjectTransmitter;
import network.objects.receiveData;

/*
 * This is a UDP connection manager designed for lossy high throughput data
 */
public class ConnectionManager extends ObjectTransmitter implements Runnable {
	private int port;
	private DatagramSocket listener;
	
	//transmit port
	private int outPort;
	
	//passed in for concurrent use with a TCP connection
	private TreeMap<String, ConnectionInfoUDP> IPtable;
	
	//input streams, used to rebuild objects from byte arrays
	private ByteArrayInputStream dataInputStream;
	private ObjectInputStream objectStream;
	
	//output streams used to turn objects into byte arrays
	ByteArrayOutputStream dataOutputStream;
	ObjectOutputStream objectOut;
	
	//buffer size (a place to store the bytes coming and going out), set if you receive too much data it will overflow, underflow is not an issue
    byte[] sendDataBuffer;
    byte[] receiveDataBuffer;
    		
	//passed in queue to core processing
	private Queue<gameInputObject> inputData;
	
	//static parameters
	private static final int sendBufferSize = 1024;
	private static final int receiveBufferSize = 1024;

	//TCP UDP conjunction server thread connection
	public ConnectionManager(int newPort, Queue<gameInputObject> sharedQueue, TreeMap<String, ConnectionInfoUDP>  IP)
	{
		try {
			listener = new DatagramSocket(newPort);
			sendDataBuffer = new byte[sendBufferSize];
			receiveDataBuffer = new byte[receiveBufferSize];
			port = newPort;
			outPort = newPort+3;
			IPtable = IP;
			//initialize serializer streams
			dataOutputStream = new ByteArrayOutputStream();
			objectOut = new ObjectOutputStream(dataOutputStream);
			
			inputData = sharedQueue;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//main listener session
	public void run() 
	{
		
		try {			
			String IPAddress;
			int port;
			boolean validIP;
			Object a;
			while(true)
			{			
				DatagramPacket receivePacket = new DatagramPacket(receiveDataBuffer, receiveDataBuffer.length);
				//listen block waiting for object to come in
				listener.receive(receivePacket);
				validIP = false;
				//check if incoming packet is from valid IP and update new port for NAT ( need to implement a discard command)
				IPAddress = receivePacket.getAddress().toString().substring(1);
				port = receivePacket.getPort();
				if(IPtable.containsKey(IPAddress))
					validIP = true;
				if(!validIP)
					System.out.println("unrecognized IP...");
				else
				{
					//initialize and run inflater
					//ByteArrayOutputStream output = new ByteArrayOutputStream(receiveBufferSize);
					//InflaterOutputStream ios = new InflaterOutputStream(output);				
					//ios.write(receiveDataBuffer);
					//ios.close();
					//redeploy inflated packet to buffer
					//receiveDataBuffer = output.toByteArray();
					
					//initialize the streams to read the data
					dataInputStream = new ByteArrayInputStream(receiveDataBuffer);
					objectStream = new ObjectInputStream(dataInputStream);
					//turn bytes into object
					a = objectStream.readObject();
					//receive ping
					if(a instanceof Byte)
					{
						if((Byte)a == (byte)0xFF)
							transmitObject((Byte)a, IPAddress, port);
					}
					else if(a instanceof receiveData)
					{
						receiveData tempHolder = (receiveData)a;	
						synchronized(inputData)
						{
							//add the new object to queue
							inputData.add( new gameInputObject(tempHolder, IPAddress, port));
							inputData.notify();
						}
					}
					//close up existing stream
					objectStream.close();
					dataInputStream.close();
				}
			}
		} catch (StreamCorruptedException e) {
			//stream header read error
			System.out.println("corrupted packet or unrecognized object");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// When rebuilding object fails
			System.out.println("unrecognized object");
			System.out.println(e);
			e.printStackTrace();
		}catch (IOException e) {
			// general network failure
			e.printStackTrace();
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
	
	@Override
	public void broadcast()
	{
		//implement later
	}
	public void transmitObject(Object c, String IP, int newPort) throws IOException
	{
		transmitObject(serializeObject(c), IP, outPort);
	}
	public void transmitObject(byte[] object, String IP, int newPort) throws IOException
	{
		DatagramPacket packet = new DatagramPacket(object, object.length, InetAddress.getByName(IP), newPort);	
		listener.send(packet);
		objectOut.close();
		dataOutputStream.close();
	}
	public void transmitObject(Object c, String IP) throws IOException
	{
		transmitObject(serializeObject(c), IP, outPort);
	}	
	
	
	
	/*public void transmitObject(Object c, String IP, int newPort) throws IOException
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
	}	*/
}
