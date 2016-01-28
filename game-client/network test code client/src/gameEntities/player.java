package gameEntities;

import java.util.ArrayList;

import org.lwjgl.util.Rectangle;

import TileMap.Tile;

public class player extends Entities {
	
	//definition states
	public final static int IDLE = 0;
	public final static int JUMP = 1;
	public final static int WALK = 2;
	public final static int RUN = 3;
	
	//animation frames
	public Tile idle;
	public Tile jump;
	public Tile walk;
	public Tile run;
	
	/* ***************** LOADING TEXTURES AND ORIGANIZING FRAMES *****************************
	 * The graphics are one gigantic sprite sheets with different rows, each row represent a different animation state
	 * 
	 * first = idle
	 * second = jump
	 * third = walk
	 * fourth = run
	 * 
	 * These are default presets, you can customize states by simply add or removing new ArrayLists holding the animation offsets
	 * When rendering, OpenGL with render the texture by using offsets of the image according to the array list
	 * 
	 */
	
	//holds subimage coordinates
	public ArrayList<Rectangle> idleCoords;
	public ArrayList<Rectangle> jumpCoords;
	public ArrayList<Rectangle> walkCoords;
	public ArrayList<Rectangle> runCoords;
	
	public int currentState;
	
	public player() 
	{
		idleCoords = new ArrayList<Rectangle>();
		
		//ddddddddddloadTexture("test");
	}
	
}
