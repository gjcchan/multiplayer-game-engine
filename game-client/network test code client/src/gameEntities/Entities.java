package gameEntities;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class Entities {

	
	public Texture loadTexture(String imageDirectory) //throws FileNotFoundException, IOException 
	{
		try {
			//do not use toURI if packaging in JAR, use get resource as stream
			return TextureLoader.getTexture("PNG",  getClass().getResourceAsStream(imageDirectory) );	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
