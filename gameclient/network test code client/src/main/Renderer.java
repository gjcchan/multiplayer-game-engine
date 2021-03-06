package main;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import network.objects.RepackagedPlayer;
import network.objects.receiveData;
import network.objects.sendData;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.*;
import org.lwjgl.*;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;

import sharedResources.globalShare;
import Graphics.Background;
import TileMap.TileMap;
import gameEntities.player;



public class Renderer implements Runnable{

		private receiveData outPacket;
		private TileMap[] newMap;
		private Background[] background;
		
		private final int gameWidth = 1024;
		private final int gameHeight = 768;
		
		private final float scaling = 1.2f;
		
		private final int Level1 = 0;
		
		//keeping track of camera
		private int cameraX;
		private int cameraY;
		
		private int currentMap;
		private int currentStage;
		
		//set individual level's tile size, ie 32 = 32x32 pixel tiles
		private final int tileSizeLevel1 = 32;
		private int currentLevel = 0;
		
		public globalShare shared;
		//player graphics
		private player playerGraphics;
		
		//used for timing rendering time for most accurate interpolation
		public long renderTime;
		
		//keeps track of which is player
		public RepackagedPlayer clientPlayer;
		
		//used for setting fps
		private final double simulationRate = 66.667; //10ms = factor of 50ms, works better  but hackish, need to fix down the road
		private final int framerate = 60;
		private int simulationRateMsec = (int)Math.round(1000 / simulationRate);
		
		//text rendering
		private TrueTypeFont font;
		
		public Renderer(receiveData newPacket, globalShare newShared)
		{
			shared = newShared;
			outPacket = newPacket;
			
			shared = newShared;		
			clientPlayer = null;			
		}
		//game init code -called by run()
		private void initializeGameData()
		{
			//load text font
			font = new TrueTypeFont(new Font("Arial", Font.BOLD, 12), false);
			newMap = new TileMap[1];
			background = new Background[1];
			//load map tiles
			newMap[Level1] = new TileMap(tileSizeLevel1);
			newMap[Level1].loadMap("/Maps/testmap.map");
			//load level backgrounds
			background[Level1] = new Background();
			background[Level1].loadImage("/Backgrounds/testmap.png");
			//load player graphics
			playerGraphics = new player();
			glBindTexture(GL_TEXTURE_2D, 0);
		}		
		//GL drawing functions
		
		/*************** CHECK FUNCTIONS *******************
		 *  THESE FUNCTIONS ARE USED TO CHECK IF OBJECT IS ON-CAMERA OR NOT, IF NOT ON CAMERA, IT IS NOT DRAWN
		 * 
		 * @param x
		 * @param y
		 * @param width
		 * @param height
		 *********************************************/
		private void drawQuadChecker(int x, int y, int width, int height)
		{
			if( (x+width > cameraX || x < cameraX+gameWidth) && (y+height > cameraY || y < cameraY+gameHeight))
				drawQuad(x-cameraX, y-cameraY, width, height);
		}
		private void drawTriangleChecker(int x, int y, int z, int x2, int y2, int z2)
		{
			if( (Math.min(x, Math.min(y, z)) >= cameraX || Math.max(x, Math.max(y, z)) <= cameraX+gameWidth) &&
				(Math.min(x2, Math.min(y2,  z2)) >= cameraY || Math.max(x2, Math.max(y2, z2)) <= cameraY + gameHeight))
				drawTriangle(x-cameraX, y-cameraX, z-cameraX, x2-cameraY,y2-cameraY,z2-cameraY);			
		}
		
		/****************** DRAW FUNCTIONS ***********
		 * 	THESE FUNCTIONS ARE USED TO DRAW PRIMITIVES
		 * 
		 * @param x
		 * @param y
		 * @param width
		 * @param height
		 *********************************************/
		private void drawQuadScaled(int x, int y, int width, int height)
		{
			drawQuad((int)(x*scaling), (int)(y*scaling), (int)(width*scaling), (int)(height*scaling));
		}
		private void drawTriangleScaled(int x, int y, int z, int x2, int y2, int z2)
		{
			drawTriangle((int)(x*scaling), (int)(y*scaling), (int)(z*scaling), (int)(x2*scaling), (int)(y2*scaling), (int)(z2*scaling));
		}
		private void drawQuad(int x, int y, int width, int height)
		{
			glBegin(GL_QUADS);
				glTexCoord2f(0,0);	glVertex2i(x,y); //upperleft
				glTexCoord2f(1,0);	glVertex2i(x+width,y); //upperright	
				glTexCoord2f(1,1);	glVertex2i(x+width,y+height); //bottom-right
				glTexCoord2f(0,1);	glVertex2i(x,y+height); // bottom-left
			glEnd();	
		}
		private void drawTriangle(int x, int y, int z, int x2, int y2, int z2)
		{
			glBegin(GL_TRIANGLES);
				glVertex2i(x,x2); //upperleft
				glVertex2i(y,y2); //upperright			
				glVertex2i(z,z2); //bottom-right			
			glEnd();			
		}
		private void drawLine(int x, int y, int x2, int y2)
		{
			//glLineWidth(3.8f);
		    glBegin(GL11.GL_LINE_STRIP);
			    glVertex2d(x, y);
			    glVertex2d(x2, y2);
		    glEnd();			
		}
		
		/*********** ENTITY RENDER FUNCTIONS **************
		 *  THESE FUNCTIONS CALCULATE AND DRAW THE GAME ENTITIES
		 * 
		 * @param map
		 *********************************************/
		private void drawMap(TileMap map)
		{
			for(int y = 0; y < map.numRows; y++)
				for(int x = 0; x < map.numCols; x++)
					if(map.map[y][x] != 0)
					{
						if(map.getTile(map.map[y][x]).defaultTexture != null)
							glBindTexture(GL_TEXTURE_2D, map.getTile(map.map[y][x]).defaultTexture.getTextureID());
						glColor3f(1.0f, 1.0f, 1.0f);
						drawQuadChecker(x*map.tileSize, y*map.tileSize, map.tileSize, map.tileSize);	
						glBindTexture(GL_TEXTURE_2D, 0);
					}
		}
		private void drawPlayers(List<RepackagedPlayer> players)
		{
			//set color of other players  (useless when have textures)
			glColor3f(0.5f, 0.75f, 1.0f);	
			String username;
			for(RepackagedPlayer player : players)
			{
				//System.out.println("CLIENTPLAYER: " + player.posX + ", " + player.posY);
				synchronized(shared)
				{
					//check to see if it is client's player, if yes, then save for last
					if(player.playerIndex == shared.index)
					{
						clientPlayer = player;
						//System.out.println("CLIENTPLAYER: " + player.posX + ", " + player.posY);
					}
					else
					{
						// the -6 represents the display char width  - actual boundingBox >>2)
						drawQuadChecker((int)player.posX -6, 
								(int)player.posY, 
								tileSizeLevel1, 
								tileSizeLevel1);	
						
						drawTriangleChecker(
								(int)player.posX + 8, 
								(int)player.posX + 14, 
								(int)player.posX + 20,
								(int)player.posY - 16, 
								(int)player.posY - 4, 
								(int)player.posY - 16);
						TextureImpl.bindNone();
						//font.drawString((int)player.posX + 4 - cameraX, (int)player.posY - 32 - cameraY, "PLAYER " + player.playerIndex, Color.white);
						username = shared.playerNames.get(player.playerIndex);
						if(username == null || username.isEmpty())
							username = "<NO NAME>";
						font.drawString((int)player.posX + 4 - cameraX, (int)player.posY - 32 - cameraY, username, Color.white);
						TextureImpl.bindNone();
					}				
				}
			}
			//draw client's player
			if(clientPlayer != null)
			{						
				//why here???
				adjustCamera();	
				//draw player
				playerGraphics.incrementFrame();
				glColor3f(1.0f, 1.0f, 1.0f);
				glBindTexture(GL_TEXTURE_2D, playerGraphics.getFrame().getTextureID());
				int posX = (int)clientPlayer.posX -6;
				if(!playerGraphics.getLeft())
				{
					posX += Math.abs(playerGraphics.getCurrentFrameWidth());
				}
				drawQuadChecker(posX, //(int)clientPlayer.posX -6, //what is the -6 for?? because of actual collision block is 20px?? 32-20 / 2
						(int)clientPlayer.posY, 
						playerGraphics.getCurrentFrameWidth(), 
						playerGraphics.getCurrentFrameHeight());
				
				TextureImpl.bindNone();
				glColor3f(0.0f, 0.0f, 0.0f);
				//draw pointer to player name
				drawTriangleChecker(
						(int)clientPlayer.posX + 0, 
						(int)clientPlayer.posX + 10, 
						(int)clientPlayer.posX + 20,
						(int)clientPlayer.posY - 22, 
						(int)clientPlayer.posY - 4, 
						(int)clientPlayer.posY - 22);
				glColor3f(0.03f, 0.25f, 1.0f);
				drawTriangleChecker(
						(int)clientPlayer.posX + 4, 
						(int)clientPlayer.posX + 10, 
						(int)clientPlayer.posX + 16,
						(int)clientPlayer.posY - 20, 
						(int)clientPlayer.posY - 8, 
						(int)clientPlayer.posY - 20);
				TextureImpl.bindNone();
				font.drawString((int)clientPlayer.posX - 2 - cameraX, (int)clientPlayer.posY - 40 - cameraY, "YOU", Color.white);
			}	
		}
		
		private void drawBackground() 
		{
			//set to 1 for 1-1 pixel map, <1 for parallax
			final float tween = 0.5f;
			
			float leftX = (float)cameraX*tween / (float)background[currentLevel].image.getImageWidth();
			float topY = (float)cameraY*tween / (float)background[currentLevel].image.getImageHeight();
			float rightX = ((float)(cameraX*tween)+(float)gameWidth) / (float)background[currentLevel].image.getImageWidth();
			float bottomY = ((float)(cameraY*tween)+(float)(gameHeight)) / (float)background[currentLevel].image.getImageHeight();
			
			//dividing Y X by 1.8 because it doesnt scale properly for some reason (not 1-to-1 pixel mapped)
			//perhaps it doesnt like non base 2 square dimensions? use hack code to fix for now
			glColor3f(1.0f, 1.0f, 1.0f);
			glBindTexture(GL_TEXTURE_2D, background[currentLevel].image.getTextureID());
			//drawQuad((int)leftX, (int)topY, (int)rightX, (int)bottomY);
			glBegin(GL_QUADS);
				glTexCoord2f(leftX/1.5f, topY/1.07f);		glVertex2i(0,0); //upperleft
				glTexCoord2f(rightX/1.5f,topY/1.07f);		glVertex2i(gameWidth,0); //upperright	
				glTexCoord2f(rightX/1.5f,bottomY/1.07f);	glVertex2i(gameWidth,gameHeight); //bottom-right
				glTexCoord2f(leftX/1.5f, bottomY/1.07f);	glVertex2i(0,gameHeight); // bottom-left
			glEnd();
			
			//unbind texture
			glBindTexture(GL_TEXTURE_2D, 0);
		}
		private void drawLaserSight()
		{
			if(clientPlayer != null)
			{
				double originX = clientPlayer.posX - cameraX;
				double originY = clientPlayer.posY - cameraY;
				double rads;
				int destX;
				int destY;
				rads = Math.atan2(originX - Mouse.getX(), originY - Mouse.getY());
				/*if(rads < 0)
					rads = Math.abs(rads);
				else
					rads = 2*Math.PI - rads; */
				drawLine((int)originX, (int)originY, (int)(1000*Math.cos(rads)), (int)(1000*Math.sin(rads)) );
				//drawLine((int)originX, (int)originY, Mouse.getX(), gameHeight - Mouse.getY());
			}
			
		}
		private void drawForeground()
		{
			
		}
		private void drawRTT(long ping)
		{
			glColor3f(1.0f, 1.0f, 1.0f);	
			//need to properly implement ping
			font.drawString(10, 10, "Ping: " + ping +" ms", Color.white);
			glBindTexture(GL_TEXTURE_2D, 0);		
		}
		private void drawHUD()
		{
			glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
			drawQuad(gameWidth-200, 20, 180, 40);
		}


		/************* NON-GRAPHICAL GAME FUNCTIONS ***********************************/
		//run ? times between packets (but have packet sync issue)
		private void interpolateMovement()
		{
			//long startTimer = System.nanoTime();
			if(shared.previousSyncedState != null)
			{
				//for(int x = 0; x < shared.previousSyncedState.playerList.size(); x++)
				//not the best method since it can cause glitches on slow machines
				for(int x = 0; x < Math.min(shared.previousSyncedState.playerList.size(), shared.currentSyncedState.playerList.size()); x++)
				{
					//System.out.println("POSX = " + shared.previousSyncedState.playerList.get(x).posX  + " + ( (" + shared.currentSyncedState.playerList.get(x).posX + " - " + shared.previousSyncedState.playerList.get(x).posX + ") / (" + shared.betweenState + " / " + simulationRateMsec +"))");
					try {
						synchronized(shared)
						{					
							//remember to subtract old from new to get calc
							shared.previousSyncedState.playerList.get(x).posX = shared.previousSyncedState.playerList.get(x).posX 
									+ ( (shared.currentSyncedState.playerList.get(x).posX - shared.previousSyncedState.playerList.get(x).posX) / (shared.betweenState / simulationRateMsec)); //the -2 makes it more even in movement
							shared.previousSyncedState.playerList.get(x).posY = shared.previousSyncedState.playerList.get(x).posY 
									+ ( (shared.currentSyncedState.playerList.get(x).posY - shared.previousSyncedState.playerList.get(x).posY) / (shared.betweenState / simulationRateMsec));
							//System.out.println(shared.previousSyncedState.playerList.get(x).posX + " + (" + shared.currentSyncedState.playerList.get(x).posX + " - " + shared.previousSyncedState.playerList.get(x).posX +" / ( " + shared.betweenState + " / " + simulationRateMsec + " ))");
						}
					}catch(NullPointerException nex) {
						System.out.println("null issue on interpolation on index: " + x);
						System.out.println("ARRAY SIZES : PREVIOUS=" + shared.previousSyncedState.playerList.size() + " CURRENT=" + shared.currentSyncedState.playerList.size() );
					} catch(IndexOutOfBoundsException iex) {
						System.out.println("out of bounds exception:");
						System.out.println("INDEX : " + x + " Max range size: " + Math.min(shared.previousSyncedState.playerList.size(), shared.currentSyncedState.playerList.size()) );
						System.out.println("ARRAY SIZES : PREVIOUS=" + shared.previousSyncedState.playerList.size() + " CURRENT=" + shared.currentSyncedState.playerList.size() );
					}
					//System.out.println("INTERPOLATE: " + shared.previousSyncedState.playerList.get(x).posX + ", " + shared.previousSyncedState.playerList.get(x).posY);
				}
			}
			//renderTime = System.nanoTime() - startTimer;
		}
		private void adjustCamera()
		{
			if(clientPlayer != null)
			{				
				//for x
				if(clientPlayer.posX + 30 > (cameraX + gameWidth-100))
					cameraX += (clientPlayer.posX + 30 - (cameraX + gameWidth - 100));
				else if(clientPlayer.posX < (cameraX + 100))
					cameraX -= (cameraX + (100 - clientPlayer.posX));
				//for y
				if(clientPlayer.posY + 30  > (cameraY + gameHeight-100))
					cameraY += (clientPlayer.posY - (cameraY + gameHeight - 100));
				else if(clientPlayer.posY + 30 < (cameraY + 100))
					cameraY -= (cameraY + (100 - clientPlayer.posY));
				
				//fix bounds
				if(cameraX + gameWidth > newMap[currentLevel].getWidth())
					cameraX = newMap[currentLevel].getWidth() - gameWidth;
				else if(cameraX < 0)
					cameraX = 0;
				
				if(cameraY + gameHeight > newMap[currentLevel].getHeight())
					cameraY = newMap[currentLevel].getHeight() - gameHeight;
				else if(cameraY < 0)
					cameraY = 0;
			}
		}
		
		protected void drawQuadAngle(int x, int y, int width, int height, double angle)
		{
			double radians = Math.toRadians(angle);
			drawQuadChecker(x, y, (int)(width*Math.cos(radians)), (int)(height*Math.sin(radians)));
		}
		
		/*protected void rotateRect(int originX, int originY, int topRightX, int topRightY, int bottomLeftX, int bottomLeftY, int bottomRightX, int bottomRightY, double angle)
		{
			double sin = Math.sin( Math.toRadians(angle));
			double cos = Math.cos( Math.toRadians(angle));
			double newOriginX = cos*originX - sin*originY;
			double newOriginY = sin*originX  + cos*originY;
			double newTopRightX = cos*(topRightX-originX) - sin*(topRightY-originY);
			double newTopRightY = sin*(topRightX-originX)  + cos*(topRightY-originY);
			double newBottomLeftX = cos*(bottomLeftX-originX) - sin*(bottomLeftY-originY);
			double newBottomLeftY = sin*(bottomLeftX-originX) + cos*(bottomLeftY-originY);
			double newBottomRightX = cos*(bottomRightX-originX) - sin*(bottomRightY-originY);
			double newBottomRightY = sin*(bottomRightX-originX) + cos*(bottomRightY-originY);
			drawFreeQuad((int)originX, (int)originY, (int)newTopRightX+originX, (int)newTopRightY+originY, (int)newBottomLeftX+originX, (int)newBottomLeftY+originY, (int)newBottomRightX+originX, (int)newBottomRightY+originY);			
		}*/
		protected void drawRectangle(int originX, int originY, int width, int height, double angle)
		{
			double sin = Math.sin( Math.toRadians(angle));
			double cos = Math.cos( Math.toRadians(angle));
			double newOriginX = cos*originX - sin*originY;
			double newOriginY = sin*originX  + cos*originY;
			double newTopRightX = cos*(width);
			double newTopRightY = sin*(width);
			double newBottomLeftX = -sin*(height);
			double newBottomLeftY = cos*(height);
			double newBottomRightX = cos*(width) - sin*(height);
			double newBottomRightY = sin*(width) + cos*(height);
			drawFreeQuad((int)originX, (int)originY, (int)newTopRightX+originX, (int)newTopRightY+originY, (int)newBottomLeftX+originX, (int)newBottomLeftY+originY, (int)newBottomRightX+originX, (int)newBottomRightY+originY);
			
		}
		//draw free quad
		protected void drawFreeQuad(int originX, int originY, int topRightX, int topRightY, int bottomLeftX, int bottomLeftY, int bottomRightX, int bottomRightY)
		{
			glBegin(GL_QUADS);
				glTexCoord2f(0,0);	glVertex2i(originX,originY); //upperleft
				glTexCoord2f(1,0);	glVertex2i(topRightX,topRightY); //upperright	
				glTexCoord2f(1,1);	glVertex2i(bottomRightX,bottomRightY); //bottom-right
				glTexCoord2f(0,1);	glVertex2i(bottomLeftX,bottomLeftY); // bottom-left
			glEnd();			
		}
		
		
		public void run() 
		{
			Cursor customCursor;
			boolean keyPressed;
			double angle = 0;
			//set counter for fps sleep
			long start;
			
			try{
				//set sync rate
				//Display.setVSyncEnabled(true);
				//set resolution
				Display.setDisplayMode(new DisplayMode(gameWidth, gameHeight));
				Display.create();
				initializeGameData();
				//creates a blank cursor so you can use custom cursor in game
				customCursor = new Cursor(1, 1, 0, 0, 1, BufferUtils.createIntBuffer(1), null);
				Mouse.setNativeCursor(customCursor);
			} catch (LWJGLException e) {
				e.printStackTrace();
			} 
						
			//init code for OpenGL
			//set to matrix mode for gl projection
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			//set perspective

			// set upper left to (x=0,y=0), bottom right to (640, 480)
			//glOrtho(Xbegin-leftside, Xend-rightside, Ybegin-bottom, Yend-top, 3d param[set 1 for 2d], 3d param [set -1 for 2d])
			//ie if 640 width cube, (0, 640, 640,0, 640, 0) etc
			glOrtho(0,gameWidth, gameHeight,0, 1, -1);
			glMatrixMode(GL_MODELVIEW);
			//enable textures
			glEnable(GL_TEXTURE_2D);
			//enable transparency --enable, draw, then disable
			glEnable(GL_BLEND);			
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			//glColor4f allows colored transparency
			//glColor4f(1.0f, 1.0f, 1.0f, 0.5f); // 4th value determines opacity
			//set which color to be considered transparent
			//glClearColor(0.7f, 0.7f, 0.7f, 1.0f); 
			
			//render loop
			while(!Display.isCloseRequested())
			{
				//log beginning timer
				start = System.nanoTime();
				
				outPacket.keyLeft = false;
				outPacket.keyRight = false;
				outPacket.keyUp = false;
				keyPressed = false;
				//clear screen (buffer_bit is 2d only)
				glClear(GL_COLOR_BUFFER_BIT);
				
				//set color of drawing -uses floats
				//glColor3f(colorRed, colorGreen, colorBlue);
				drawBackground();				
				//adjustCamera();		//look for it in draw players instead, incoming packet must have messed with it	
				
				glColor3f(0.03f, 0.25f, 0.01f);
				drawMap(newMap[0]);
				//glColor3f(0.03f, 0.25f, 1.0f);	
				interpolateMovement();
				drawPlayers(shared.previousSyncedState.playerList);
				
				//drawForeground();
				drawHUD();
				drawRTT(shared.pingTime);
				drawLaserSight();
				drawRectangle(100, 100, 100, 100, ++angle);
				if(angle > 360)
					angle = 0;
				//key event listener
				if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) || Display.isCloseRequested())
				{
					Display.destroy();
					System.exit(0);
				}
				synchronized(outPacket)
				{
					if(Keyboard.isKeyDown(Keyboard.KEY_A) && !Keyboard.isKeyDown(Keyboard.KEY_D))
					{
						outPacket.keyLeft = true;
						playerGraphics.setState(player.WALK);
						playerGraphics.setLeft(false);
						keyPressed = true;
					}				
					if(Keyboard.isKeyDown(Keyboard.KEY_D) && !Keyboard.isKeyDown(Keyboard.KEY_A))
					{
						outPacket.keyRight = true;
						playerGraphics.setState(player.WALK);
						playerGraphics.setLeft(true);
						keyPressed = true;
					}
					if(!Keyboard.isKeyDown(Keyboard.KEY_D) && !Keyboard.isKeyDown(Keyboard.KEY_A))
					{
						playerGraphics.setState(player.IDLE);
					}	
					if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
					{
						outPacket.keyUp = true;	
						keyPressed = true;
					}
					if(Mouse.isButtonDown(0))
					{
						playerGraphics.setState(player.SHOOT);
					}
					outPacket.notify();
				}
				//test example of using mouse in game
				glColor3f(0.05f, 0.4f, 0.5f);
				glBindTexture(GL_TEXTURE_2D, 0);
				drawQuad(Mouse.getX(),gameHeight - Mouse.getY(), 5, 5);
				
				Display.update();	
				//Display.sync(framerate);
				//sleep for fps rate
				
				
				try {
					Thread.sleep(simulationRateMsec - (System.nanoTime() - start) / 1000000);
					//speeding up cpu when have the chance
					if(simulationRateMsec > 10)
						simulationRateMsec--;	
				} catch (InterruptedException e) {
					System.out.println("RENDER THREAD: no time to sleep: wait < 0");
				} catch(IllegalArgumentException ex) {
					System.out.println("RENDER THREAD: cpu too slow, slowing down simulation");	
					simulationRateMsec++;
				}	
				
			}
			//close display window
			Display.destroy();	
			System.exit(0);
		}
}
