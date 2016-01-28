package network;

import java.io.*;
import java.net.*;
import java.util.*;

import network.extensions.ObjectTransmitter;
import network.objects.receiveData;


//one thread per client uses a lot of memory due to individual stack for client
public class ConnectedSession extends ObjectTransmitter implements Runnable {
	private Socket connection;
	private InputStream persistentInputStream;
	private BufferedReader streamReader;
	private DataOutputStream persistentOutputStream;
	private boolean terminated;

	//passed in queue to core processing
	private Queue<gameInputObject> inputData;	
	
	private SessionManager sessionStarter;
	
	//basic connection setup
	public ConnectedSession(Socket newConnection)
	{
		connection = newConnection;
		//setup parameters
		
		//connection.setSoTimeout(timeout);
		try {
			connection.setTcpNoDelay(true);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		
		terminated = true;
		try {
			persistentInputStream = connection.getInputStream();
			streamReader = new BufferedReader(new InputStreamReader(this.persistentInputStream));
			persistentOutputStream = new DataOutputStream(connection.getOutputStream());
			//be sure to use flush()!
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//more advanced setup have communication with parent
	public ConnectedSession(Socket newConnection, SessionManager initializer)
	{
		sessionStarter = initializer;
		connection = newConnection;
		terminated = true;
		try {
			persistentInputStream = connection.getInputStream();
			streamReader = new BufferedReader(new InputStreamReader(this.persistentInputStream));
			persistentOutputStream = new DataOutputStream(connection.getOutputStream());
			//be sure to use flush()!
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//set this if you want to set game data packets to be received through TCP
	public void setGameData(Queue<gameInputObject> stream)
	{
		inputData = stream;
	}
	//main listener loop
	public void run()
	{
		terminated = false;
		Object newData;
		while(!terminated)
		{
			try {
				
				newData = buildObject(persistentInputStream);
				//newData = streamReader.readLine(); // ONLY TERMINATES BY \n!!! no endline signal it continues reading forever!
				//if client disconnects or a malicious request
				if(newData instanceof String)
				{
					String message = (String)newData;
					if(message == null || message.length() > 255)
					{
						terminated = true;
						this.dispose();
					}
					else
					{
						transmit(this.addToQueue(message));
					}
				} 
				else if(newData instanceof receiveData)
				{
					//inputData not set, will crash if do so
					receiveData tempHolder = (receiveData)newData;	
					synchronized(inputData)
					{
						//add the new object to queue
						inputData.add( new gameInputObject(tempHolder, getIP(), getPort() ));
						inputData.notify();
					}
				}
			} catch (IOException e) {
				System.out.println(this.getIP() + " has disconnected");
				terminated = true;
				this.dispose();
			} catch (ClassNotFoundException e) {
				System.out.println(this.getIP() + " parse object error");
				terminated = true;
				this.dispose();
			}
		}
	}
	//transmit string
	public void transmit(String data)
	{	
		try {
			transmit(
					appendTCPHeader(serializeObject(data), "")
					);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void transmit(byte[] data) throws IOException
	{
		persistentOutputStream.write(data, 0, data.length);
		persistentOutputStream.flush();
	}
	public void halt()
	{
		terminated = true;
	}	
	public void dispose()
	{
		halt();
		sessionStarter.remove(this);
	}
	//supporting functions
	public String getIP() 
	{
		return connection.getInetAddress().toString().substring(1);		
	}
	public int getPort()
	{
		return connection.getPort();
	}
	/*DEPRECATED
	 * private String addToQueue(String data)
	{
		return data + ":" + sessionStarter.parseRequest(this.getIP(), data);
	}*/
	private byte[] addToQueue(String data)
	{
		return sessionStarter.responseBuilder(this.getIP(), data);
	}
	
	public void close() throws IOException
	{
		connection.close();
	}
}
