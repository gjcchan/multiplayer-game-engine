package corelogic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import tileMap.TileMap;
import gameEntities.Player;
import gameLevels.GameStage;
import gameObjects.GameStateObject;

/* ******************* GAME STATE THREAD ***********************************
 * 
 *	This thread and code is used to processing core game mechanics such as physics, collision, damage, adjusting player variables
 *	Most of cpu heavy-lifting should be implemented here
 *
 */
public class GameState implements Runnable {
	//game world simulation rate
	private static final double simulationRate = 66.667;
	private static final int simulationRateMsec = (int)Math.round(1000 / simulationRate);
	
	/* *******************  MULTI THREADING OPTION  ******************
	 * 
	 * multithread running on a rough functional implmentation, not optimized
	 * users should not enable multi-threading unless single thread is not enough and there are idle cpus
	 * mutli-threading uses considerable overhead in both memory and cpu, so only enable when single thread is overloaded
	 * 
	 * set _THREADS to amount of threads you want
	 * set _ENABLE_MULTITHREAD to set enable/disable multithreading
	 * 
	 */
	
	//set amount of threads to be used for multi-threading gameplay mechanics
	private static final int _THREADS = 6;
	private static final boolean _ENABLE_MULTITHREAD = true;
	//thread pool used to multi-threading game mechanics
	private ThreadPoolExecutor threads;
	//blocking queue used to task threads for use
	private BlockingQueue<Runnable> workQueue;

	
	//gamestate object for broadcast
	private GameStateObject currentState;
	
	//for reconciliations, each list size depends on the life of the 1 server sec of history of 66.67fps is 67 objects in List
	private List<GameStateObject> gameHistory;
		
	//place to hold tileMaps and levels
	private List<GameStage> stages;
	private int currentStage;
		
	//allows you to pause the server
	private boolean run;
	
	//record of spawned players
	private TreeMap<String, Player> players;
	
	public GameState(GameStateObject inputState, TreeMap<String, Player> newPlayers)
	{
		currentState = inputState;
		stages = new ArrayList<GameStage>();
		gameHistory = new LinkedList<GameStateObject>();
		
		//players = new TreeMap<String, Player>();
		players = newPlayers;
		initialize();
	}
	public void run() 
	{
		long start;
		run = true;
		while(run)
		{
			{
				start = System.nanoTime();
				//do something
				simulatePlayersPosition();
				//shove it into GameStateObject(currentState)
				currentState.setPlayers(players);
				//add it into game history
				gameHistory.add(currentState);
				if(gameHistory.size() > simulationRate)
					gameHistory.remove(0);
				//Calculate how long to sleep			
				try {
					Thread.sleep(simulationRateMsec - (System.nanoTime() - start) / 1000000);
				} catch (InterruptedException e) {
					System.out.println("WORLD SIMULATION: no time to sleep: wait < 0");
				} catch(IllegalArgumentException ex) {
					System.out.println("gamestate THREAD: CPU too slow");	
				}	
			}
		}		
	}
	//Initialize the whole game
	private void initialize()
	{	
		
		//manually import maps here (root directory is in bin folder)
		GameStage LEVEL1 = new GameStage("/Maps/testmap.map", 32);
		
		//checks to see if mutlithread needed
		if(_ENABLE_MULTITHREAD)
		{
			//initialize threadpool for multi-threading
			workQueue = new LinkedBlockingQueue<Runnable>();
			threads = new ThreadPoolExecutor(_THREADS,_THREADS, 1, TimeUnit.SECONDS, workQueue);
		}
		
		//put them into stages list
		stages.add(LEVEL1);
		currentStage = 0;
		currentState.setStage(stages.get(currentStage));
		currentState.clearState();	
	}

	private void removePlayer(String IP)
	{
		players.remove(IP);
	}
	
	private void simulatePlayersPosition()
	{
		//single thread option
		if(!_ENABLE_MULTITHREAD)
		{
			for(Map.Entry<String, Player> x : players.entrySet())
			{

				x.getValue().getNewPosition(stages.get(currentStage).levelMap);
			}
		//multithread option
		} else {
			for(Map.Entry<String, Player> x : players.entrySet())
			{
				threads.execute(new playerThreadWrapper(x.getValue(),stages.get(currentStage).levelMap));
			}		
		}		
	}
	
	
	
	//mutli-thread specific class
	private class playerThreadWrapper implements Runnable {
		
		private Player player;
		private TileMap tilemap;
		
		playerThreadWrapper(Player newPlayer, TileMap newTileMap) {
			player = newPlayer;
			tilemap = newTileMap;
		}
		public void run() {
			player.getNewPosition(tilemap);
		}
	}
	
}
