package AbstractObjects;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

public abstract class Entities {
	
	//Dimensions
	public int width;
	public int height;
	public int collisionWidth;
	public int collisionHeight;
	
	public int collisionPaddingLeft = (width - collisionWidth) /2;
	public int collisionPaddingRight = collisionWidth - collisionPaddingLeft;
	public int collisionPaddingBottom = 0;
	//moving statistics
	public float moveSpeed;
	public float maxSpeed;
	public float stopSpeed;
	public float fallSpeed;
	public float maxFallSpeed;
	public float stopJumpSpeed;
	
	// position and vector
	public float x;
	public float y;
	public float dx;
	public float dy;
	
	//for AABB
	public Rectangle2D boundingBox;
	
	public boolean isFalling;
	
	public void setPosition(float newX, float newY) 
	{
		x = newX;
		y = newY;
	}
	public void setFall(boolean value)
	{
		isFalling = value;
	}
	public void verticalHalt()
	{
		dy = 0;
	}
	
}
