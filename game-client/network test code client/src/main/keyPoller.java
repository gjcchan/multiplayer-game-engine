package main;
import java.io.IOException;

import Network.UDPManager;
import frameworkObjects.receiveData;


public class keyPoller implements Runnable
{
	//how often to broadcast gamestate per sec
	private static final int broadcastRate = 20;
	private static final int broadcastRateMsec = 1000 / broadcastRate;
	//how often the server expects incoming per sec
	private static final int cmdRate = 10;
	private receiveData keys;
	private receiveData oldKeys;
	private UDPManager serverConnection;
	private int udpPort;
	
	public keyPoller(receiveData inKeys, UDPManager connection, int port)
	{
		oldKeys = new receiveData();
		keys = inKeys;
		serverConnection = connection;
		udpPort = port;
	}
	public void run() 
	{
		long start;
		while(true)
		{
			try {
				start = System.nanoTime();
				synchronized(keys) 
				{
					keys.wait();
					//if(oldKeys.keyUp != keys.keyUp || oldKeys.keyDown != keys.keyDown || oldKeys.keyLeft == keys.keyLeft)
					if(!oldKeys.equals(keys))
					{
						serverConnection.transmitObject(keys, runtime.serverIP, udpPort);
						oldKeys.keyUp = keys.keyUp;
						oldKeys.keyDown = keys.keyDown;
						oldKeys.keyLeft = keys.keyLeft;
					}
				}
				//calcluate how long to sleep
				Thread.sleep(broadcastRateMsec - (System.nanoTime() - start) / 1000000);
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			} catch(IllegalArgumentException ex) {
				System.out.println("KEY POLLER THREAD: no time to sleep: wait < 0");				
			}
		}
	}
	
}
