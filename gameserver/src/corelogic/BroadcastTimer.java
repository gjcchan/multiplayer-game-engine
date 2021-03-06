package corelogic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import network.ConnectionManager;
import network.SessionManager;
import network.extensions.ObjectTransmitter;
import network.objects.RepackagedPlayer;
import network.objects.sendData;
import gameEntities.Player;
import gameObjects.GameStateObject;

//keeps track of broadcast rate
public class BroadcastTimer extends ObjectTransmitter implements Runnable{
	//how often to broadcast gamestate per sec
	private static final float broadcastRate = 22.222f;
	private static final int broadcastRateMsec =  Math.round(1000 / broadcastRate);
	//how often the server expects incoming per sec
	private static final int cmdRate = 10;
	
	/* ******************** TCP / UDP SELECTOR *************************
	 * 
	 * The code now broadcaster now supports transmitting gamestate object through TCP or UDP
	 * 
	 * UDP is preferred for lossy time-critical packets such as player movements
	 * TCP is preferred for accurate data and lossless transmissions
	 * 
	 */
	private static final String _TRANSMIT_TYPE = "TCP";
	//used to compare deltas and determine if a broadcast is needed
	//private GameStateObject previousGameState;
	private GameStateObject gameState;
	
	private sendData repackagedGameState;
	private ConnectionManager UDP;
	private SessionManager TCP;
	
	public BroadcastTimer(GameStateObject passInState, ConnectionManager UDPplug)
	{
		gameState = passInState;
		UDP = UDPplug;
		repackagedGameState = new sendData();
	}

	public BroadcastTimer(GameStateObject passInState, ConnectionManager UDPplug, SessionManager TCPplug)
	{
		gameState = passInState;
		UDP = UDPplug;
		TCP = TCPplug;
		repackagedGameState = new sendData();
	}
	public void run() 
	{		
		long start;
		while(true)
		{
			start = System.nanoTime();
			//if(previousGameState != null && !previousGameState.equals(gameState))
			//{
				//previousGameState = gameState;
				repackageGameState();
				broadcastToPlayers();
			//}
			//calcluate how long to sleep
			try {
				Thread.sleep(broadcastRateMsec - (System.nanoTime() - start) / 1000000);
			} catch (InterruptedException e) {
				System.out.println("Something interrupting thread sleep");
			} catch(IllegalArgumentException ex) {
				System.out.print("BROADCAST THREAD: no time to sleep: ");		
				System.out.println("RATE: " + (1000/(Math.abs(broadcastRateMsec - (System.nanoTime() - start) / 1000000) + broadcastRateMsec)) + "TARGET: " + broadcastRate);
			}	
		}
	}
	private void repackageGameState()
	{
		repackagedGameState = new sendData();
		for(Map.Entry<String, Player> x : gameState.players.entrySet())
		{	
			RepackagedPlayer temp = new RepackagedPlayer(
					x.getValue().health, 
					(float)x.getValue().boundingBox.getX(), 
					(float)x.getValue().boundingBox.getY(), 
					x.getValue().index, 
					(float)x.getValue().dx, //used for client-side prediction
					(float)x.getValue().dy);
			repackagedGameState.playerList.add(temp);
		}
	}
	private void broadcastToPlayers()
	{
		
		try {
			byte[] serializedGameState = serializeObject(repackagedGameState);
			//Checks for transmit method of UDP or TCP
			if( _TRANSMIT_TYPE == "UDP")
			{
				for(Map.Entry<String, Player> x : gameState.players.entrySet())
				{	
					try {
						UDP.transmitObject(serializedGameState, x.getKey(), x.getValue().port);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}		
			} else if( _TRANSMIT_TYPE == "TCP") {
				
				serializedGameState = appendTCPHeader(serializedGameState, "sendData");
				TCP.transmitObjectAll(serializedGameState);
				/*for(Map.Entry<String, Player> x : gameState.players.entrySet())
				{	
					try {
						TCP.transmitObject(repackagedGameState, x.getKey());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}		*/		
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
