package network.objects;

import java.io.Serializable;

public class RepackagedPlayer implements Serializable 
{
	//public int health;
	public float posX;
	public float posY;
	public int playerIndex;

	public RepackagedPlayer(int newHealth, float x, float y, int newIndex)
	{
		//health = newHealth;
		posX = x;
		posY = y;
		playerIndex = newIndex;
	}
	public RepackagedPlayer(int newHealth, float x, float y, int newIndex, float dx, float dy)
	{
		//health = newHealth;
		posX = x;
		posY = y;
		playerIndex = newIndex;
	}
}
