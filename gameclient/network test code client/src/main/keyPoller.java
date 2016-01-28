package main;
import java.io.IOException;

import network.TCPManager;
import network.UDPManager;
import network.objects.receiveData;


public class keyPoller implements Runnable
{
	//how often to broadcast gamestate per sec
	private static final int broadcastRate = 20;
	private static final int broadcastRateMsec = 1000 / broadcastRate;
	//how often the server expects incoming per sec
	private static final int cmdRate = 10;
	private receiveData keys;
	private receiveData oldKeys;
	private UDPManager serverConnectionUDP;
	private TCPManager serverConnectionTCP;
	
	private int udpPort;
	
	public keyPoller(receiveData inKeys, UDPManager connection, int port)
	{
		oldKeys = new receiveData();
		keys = inKeys;
		serverConnectionUDP = connection;
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
					if(oldKeys.keyUp != keys.keyUp || oldKeys.keyRight != keys.keyRight || oldKeys.keyLeft != keys.keyLeft)
					//if(!oldKeys.equals(keys))
					{
						serverConnectionUDP.ping(runtime.serverIP, udpPort);
						serverConnectionUDP.transmitObject(keys, runtime.serverIP, udpPort);
						
						// ONLY USED FOR TCP TRANSMIT
						//serverConnectionTCP.transmitToServer(keys);

						oldKeys.keyUp = keys.keyUp;
						oldKeys.keyDown = keys.keyDown;
						oldKeys.keyLeft = keys.keyLeft;
						oldKeys.keyRight = keys.keyRight;
						oldKeys.jumpKey = keys.jumpKey;
						oldKeys.fire = keys.fire;
						
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
