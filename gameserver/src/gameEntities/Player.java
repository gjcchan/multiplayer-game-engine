package gameEntities;

import java.awt.geom.Rectangle2D;

import AbstractObjects.Entities;
import dependencies.AABBCollision;
import dependencies.Coordinates;
import tileMap.TileMap;

public class Player extends Entities {
	
	//custom fields
	public int maxHealth = 100;
	public int health;
	public String userName;
	public String IPAddress;
	public int port;

	public int index;
	
	//movement state
	public boolean leftKeyPress;
	public boolean rightKeyPress;
	
	public boolean isJumping;
	
	//custom characteristics
	public float jumpStart = -8.8f;
	public float stopJumpSpeed = 0.3f;
	
	//constructor
	public Player(int spawnX, int spawnY, int newPort)
	{
		health = maxHealth;
		port = newPort;
		
		//reset next position variable
		dx = 0;
		dy = 0;
		
		//let render decide on the how to draw player not server
		width = 20;
		height = 32;
		
		//moving statistics
		moveSpeed = 1.0f;
		maxSpeed = 2.6f;
		stopSpeed = 0.4f;
		fallSpeed = 0.15f;
		maxFallSpeed = 5.5f;
		
		boundingBox = new Rectangle2D.Float(spawnX, spawnY, width, height);
	}
	public void hit(int damage)
	{
		health -= damage;
	}
	public void getNewPosition(TileMap map)
	{
		getNextPosition();
		Coordinates newPos = AABBCollision.getNewPosition(this, map);
		boundingBox.setRect(newPos.x, newPos.y, width, height);
	}
		
	//no collision detection here
	private void getNextPosition() 
	{
		// movement (horizontal)
		if(leftKeyPress) {
			dx -= moveSpeed;
			if(dx < -maxSpeed) 
			{
				dx = -maxSpeed;
			}
		}
		else if(rightKeyPress) 
		{
			dx += moveSpeed;
			if(dx > maxSpeed) 
			{
				dx = maxSpeed;
			}
		}
		else {
			if(dx > 0) 
			{
				dx -= stopSpeed;
				if(dx < 0) 
				{
					dx = 0;
				}
			}
			else if(dx < 0) 
			{
				dx += stopSpeed;
				if(dx > 0) 
				{
					dx = 0;
				}
			}
		}
		// jumping (vertical)
		if(isJumping && !isFalling) 
		{
			dy = jumpStart;
			isFalling = true;
		}	
		// falling
		if(isFalling) 
		{
			dy += fallSpeed;
			if(dy > 0) 
			{
				isJumping = false;
			}
			if(dy < 0 && !isJumping)
			{
				dy += stopJumpSpeed;
			}
			if(dy > maxFallSpeed) 
			{
				dy = maxFallSpeed;
			}			
		}
	}
}
