package gameObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import AbstractObjects.Entities;
import gameEntities.Player;
import gameLevels.GameStage;

public class GameStateObject {
	public GameStage currentStage;
	public TreeMap<String, Player> players;
	public List<Entities> entities;
	
	
	public GameStateObject(TreeMap<String, Player> newPlayers)
	{
		players = newPlayers;
	}
	public void setStage(GameStage newStage)
	{
		currentStage = newStage;
		players = new TreeMap<String, Player>();
		entities = new ArrayList<Entities>();
	}
	public void clearState()
	{
		players.clear();
		entities.clear();
	}
	public void setPlayers(TreeMap<String, Player> newPlayers)
	{
		players = newPlayers;
	}
}
