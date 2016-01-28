package tileMap;

import java.awt.Rectangle;

import AbstractObjects.Entities;

public class Tile extends Entities {
	private int type;
	
	// tile types
	public static final int NORMAL = 0;
	public static final int BLOCKED = 1;
	
	public Tile(int type, int newX, int newY, int width, int height) 
	{
		this.type = type;
		boundingBox = new Rectangle(newX, newY, width, height);
		dy = 0;
		dx = 0;
	}
	public int getType() { return type; }
}
