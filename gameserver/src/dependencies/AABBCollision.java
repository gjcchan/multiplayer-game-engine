package dependencies;

import gameEntities.Player;

import java.awt.geom.Rectangle2D;

import tileMap.Tile;
import tileMap.TileMap;
import AbstractObjects.Entities;

public class AABBCollision {

	public static final int nothing = 0;
	public static final int blocked = 1;
	public static final int blocked2 = 2;	
	public static final int damage = 3;
	//purely test function hence O(n^2) --use QuadTree instead O(nlogn)
	public static Coordinates getNewPosition(Entities object, TileMap level)
	{
		Coordinates newPoint = new Coordinates((float)object.boundingBox.getX() + object.dx, (float)object.boundingBox.getY() + object.dy);	
		//different child classes can be identified like this to react differently
		if(object instanceof Player)
		{
			// doesnt work, need to factor in dy and dx
			if(object.dx != 0 || object.dy != 0)
			{
				//create new rectangle
				Rectangle2D focusedObject = new Rectangle2D.Float((float)(object.boundingBox.getX()+object.dx), 
													  (float)(object.boundingBox.getY()+object.dy),
													  (float)object.boundingBox.getWidth(), 
													  (float)object.boundingBox.getHeight());
				for(Tile mapTile : level.map)
				{
					//create rectangle for the other object
					Rectangle2D otherObject = new Rectangle2D.Float((float)(mapTile.boundingBox.getX()+mapTile.dx), 
														  (float)(mapTile.boundingBox.getY()+mapTile.dy),
														  (float)mapTile.boundingBox.getWidth(), 
														  (float)mapTile.boundingBox.getHeight());
					//check to see if they intersect
					if(intersect(focusedObject, otherObject))
					{
						//checks for all collisions, and selects the most intense intersection as the new position limit
						switch(mapTile.getType())
						{
							default:
								break;
							case blocked:
							case blocked2:
								Rectangle2D collidingBox = focusedObject.createIntersection(otherObject);
								float intersectWidth = (float) collidingBox.getWidth();
								float intersectHeight = (float) collidingBox.getHeight();

								if(intersectWidth > 0)
								{
									if(object.dx > 0)
										newPoint.x = (float) (object.boundingBox.getX() + object.dx - intersectWidth);
									else if(object.dx < 0)
										newPoint.x = (float) (object.boundingBox.getX() + object.dx + intersectWidth);
									object.dx = 0;
									focusedObject = new Rectangle2D.Float((float)newPoint.x, (float)focusedObject.getY(), (float)focusedObject.getWidth(), (float)focusedObject.getHeight());
								}			
								if(intersectHeight > 0)
								{
									if(object.dy > 0)
									{
										if(object.boundingBox.getY()+object.boundingBox.getHeight() <= otherObject.getY()) {
											newPoint.y = (float) (object.boundingBox.getY() + object.dy - intersectHeight);
											object.isFalling = false;											
										}
										else {
											newPoint.y = (float)object.boundingBox.getY();
										}
									}
									else if(object.dy < 0)
									{
										newPoint.y = (float) (object.boundingBox.getY() + object.dy + intersectHeight);	
									}
									object.dy = 0;
								}
		
								break;				
							case damage:
								break;
						}
					}
				}
			}			
		}
		return newPoint;
	}
	
	private static boolean intersect(Rectangle2D A, Rectangle2D B)
	{
		return A.intersects(B);
	}
	private static float getIntersect(String axis, Rectangle2D A, Rectangle2D B)
	{
		Rectangle2D intersectRectangle= A.createIntersection(B);
		if(axis == "x" || axis == "X")
			if(intersectRectangle.getHeight() > 0)
				return (float)intersectRectangle.getWidth();
			else
				return 0;
		else if(axis == "y" || axis == "Y")
			if(intersectRectangle.getWidth() > 0)
				return (float) A.createIntersection(B).getHeight();
			else
				return 0;	
		else
			return 0;
		}
	}

