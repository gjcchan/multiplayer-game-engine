package gameEntities;


import org.newdawn.slick.opengl.Texture;


public class player extends Entities {
	
	//definition states
	public final static int IDLE = 0;
	public final static int JUMP = 1;
	public final static int WALK = 2;
	public final static int RUN  = 3;
	public final static int DEAD = 4;
	public final static int SHOOT = 5;
	public final static int FALL = 6;
	
	/* ***************** LOADING TEXTURES AND ORIGANIZING FRAMES (NOT IMPLEMENTED)*****************************
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
	
	private boolean faceLeft;
	
	private int currentState;
	private int currentFrame;
	
	private long beginningFrameTime;
	
	private Texture[] idleGraphics;
	private Texture[] jumpGraphics;
	private Texture[] fallGraphics;
	private Texture[] walkGraphics;
	private Texture[] runGraphics;
	private Texture[] shootGraphics;
	private Texture[] deathGraphics;
	
	//time between frames (in msec)
	private final int[] idleTimer = {1000, 1000, 1000, 1000};
	private final int[] jumpTimer = {1000};
	private final int[] fallTimer = {1000};
	private final int[] walkTimer = {250, 250, 250, 250};
	private final int[] shootTimer = {100, 100};
	private final int[] runTimer  = {1000, 1000, 1000, 1000};
	private final int[] deathTimer = {1000, 1000, 1000, 1000};
	
	public player() 
	{
		/************* LOAD IDLE STATE GRAPHICS ********************/
		faceLeft = true;
		beginningFrameTime = System.currentTimeMillis();
		currentState = 0;
		currentFrame = 0;
		
		idleGraphics = new Texture[4];
		idleGraphics[0] = (loadTexture("/Player/idle/idle1.PNG"));
		idleGraphics[1] = (loadTexture("/Player/idle/idle2.PNG"));
		idleGraphics[2] = (loadTexture("/Player/idle/idle3.PNG"));
		idleGraphics[3] = (loadTexture("/Player/idle/idle4.PNG"));
		
		shootGraphics = new Texture[2];
		shootGraphics[0] = (loadTexture("/Player/shoot/shoot1.PNG"));
		shootGraphics[1] = (loadTexture("/Player/shoot/shoot2.PNG"));
		
		jumpGraphics = new Texture[1];
		jumpGraphics[0] = (loadTexture("/Player/jump/jump1.PNG"));
		
		fallGraphics = new Texture[1];
		fallGraphics[0] = (loadTexture("/Player/fall/fall1.PNG"));
		
		walkGraphics = new Texture[4];
		walkGraphics[0] = (loadTexture("/Player/walk/walk1.PNG"));
		walkGraphics[1] = (loadTexture("/Player/walk/walk2.PNG"));
		walkGraphics[2] = (loadTexture("/Player/walk/walk3.PNG"));
		walkGraphics[3] = (loadTexture("/Player/walk/walk4.PNG"));		
		
	}
	private void setFrameTime() 
	{
		beginningFrameTime = System.currentTimeMillis();
	}
	private boolean checkFrameTimeElapsed() 
	{
		switch(currentState) {
		case player.IDLE:
			return (idleTimer[currentFrame] < System.currentTimeMillis() - beginningFrameTime);
		case player.JUMP:
			return (jumpTimer[currentFrame] < System.currentTimeMillis() - beginningFrameTime);
		case player.WALK:
			return (walkTimer[currentFrame] < System.currentTimeMillis() - beginningFrameTime);
		case player.RUN:
			return (runTimer[currentFrame] < System.currentTimeMillis() - beginningFrameTime);
		case player.DEAD:
			return (deathTimer[currentFrame] < System.currentTimeMillis() - beginningFrameTime);
		case player.SHOOT:
			return (shootTimer[currentFrame] < System.currentTimeMillis() - beginningFrameTime);
		case player.FALL:
			return (fallTimer[currentFrame] < System.currentTimeMillis() - beginningFrameTime);		
		default:
			return false;
		}
	}
	
	public void incrementFrame()
	{
		if(checkFrameTimeElapsed())
		{
			currentFrame++;
			setFrameTime();
			switch(currentState) {
			case player.IDLE:
			case player.JUMP:
			case player.WALK:
			case player.RUN:
			case player.DEAD:
			case player.FALL:
				if(runTimer.length-1 < currentFrame)
					currentFrame = 0;
				break;
			case player.SHOOT:
				if(runTimer.length-1 < currentFrame)
					setState(player.IDLE);
				break;
			default:
				break;
			}
		}
	}
	public void setState(int newState) 
	{
		if(currentState != newState && newState < 3 && newState > -1)
		{
			currentState = newState;
			currentFrame = 0;
			setFrameTime();
		}
	}
	public void setLeft(boolean dir)
	{
		faceLeft = dir;
	}
	public boolean getLeft()
	{
		return faceLeft;
	}
	public Texture getFrame()
	{
		switch(currentState) {
		case player.IDLE:
			return idleGraphics[currentFrame];
		case player.JUMP:
			return jumpGraphics[currentFrame];
		case player.WALK:
			return walkGraphics[currentFrame];
		case player.RUN:
			return runGraphics[currentFrame];
		case player.DEAD:
			return deathGraphics[currentFrame];
		case player.SHOOT:
			return shootGraphics[currentFrame];
		case player.FALL:
			return fallGraphics[currentFrame];
		default:
			return null;
		}
	}
	public int getCurrentFrameWidth() 
	{
		switch(currentState) {
		case player.IDLE:
			return faceLeft ? idleGraphics[currentFrame].getImageWidth() :  -1*idleGraphics[currentFrame].getImageWidth();
		case player.JUMP:
			return faceLeft ? jumpGraphics[currentFrame].getImageWidth() :  -1*jumpGraphics[currentFrame].getImageWidth();
		case player.WALK:
			return faceLeft ? walkGraphics[currentFrame].getImageWidth() :  -1*walkGraphics[currentFrame].getImageWidth();
		case player.RUN:
			return faceLeft ? runGraphics[currentFrame].getImageWidth() :  -1*runGraphics[currentFrame].getImageWidth();
		case player.DEAD:
			return  faceLeft ? deathGraphics[currentFrame].getImageWidth() :  -1*deathGraphics[currentFrame].getImageWidth();
		case player.SHOOT:
			return faceLeft ? shootGraphics[currentFrame].getImageWidth() :  -1*shootGraphics[currentFrame].getImageWidth();
		case player.FALL:
			return faceLeft ? fallGraphics[currentFrame].getImageWidth() :  -1*fallGraphics[currentFrame].getImageWidth();
		default:
			return 0;
		}		
	}
	public int getCurrentFrameHeight() 
	{
		switch(currentState) {
		case player.IDLE:
			return idleGraphics[currentFrame].getImageHeight();
		case player.JUMP:
			return jumpGraphics[currentFrame].getImageHeight();
		case player.WALK:
			return walkGraphics[currentFrame].getImageHeight();
		case player.RUN:
			return runGraphics[currentFrame].getImageHeight();
		case player.DEAD:
			return deathGraphics[currentFrame].getImageHeight();
		case player.SHOOT:
			return shootGraphics[currentFrame].getImageHeight();
		case player.FALL:
			return fallGraphics[currentFrame].getImageHeight();
		default:
			return 0;
		}		
	}	
}
