package Graphics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class Background 
{	
	public Texture image;
	
	public Background()
	{
	}
	public void loadImage(String imageDirectory)
	{
		try {
			image = TextureLoader.getTexture("PNG", getClass().getResourceAsStream(imageDirectory) );	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
