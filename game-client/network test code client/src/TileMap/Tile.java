package TileMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class Tile {
	private int type;
	public Texture defaultTexture;
	
	public Tile(int type) 
	{
		this.type = type;
	}
	public int getType() { return type; }
	
	
	public void loadTexture(String imageDirectory) throws FileNotFoundException, IOException 
	{
		try {
			//do not use toURI if packaging in JAR, use get resource as stream
			defaultTexture = TextureLoader.getTexture("PNG",  getClass().getResourceAsStream(imageDirectory) );	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
