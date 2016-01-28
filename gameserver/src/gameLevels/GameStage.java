package gameLevels;

import tileMap.TileMap;

public class GameStage {
	public TileMap levelMap;
	
	public GameStage(String MapDirectory, int tileSize)
	{
		levelMap = new TileMap(tileSize);
		levelMap.loadMap(MapDirectory);
	}


}
