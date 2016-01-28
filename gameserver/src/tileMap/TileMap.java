package tileMap;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Arrays;

public class TileMap {
	
	// position
	private double x;
	private double y;
	
	// map
	public ArrayList<Tile> map;
	private int tileSize;
	private int numRows;
	private int numCols;
	private int width;
	private int height;
	
	// tileset
	private int[] tilesTypes;
	
	public TileMap(int tileSize) 
	{
		this.tileSize = tileSize;
	}	
	public void loadMap(String s) 
	{	
		try {	
			InputStream in = getClass().getResourceAsStream(s);
			BufferedReader br = new BufferedReader(
						new InputStreamReader(in)
					);
			numCols = Integer.parseInt(br.readLine());
			numRows = Integer.parseInt(br.readLine());
			//map = new Tile[numRows*numCols];
			map = new ArrayList<Tile>();
			width = numCols * tileSize;
			height = numRows * tileSize;
			
			String delims = "\\s+";
			for(int row = 0; row < numRows; row++) 
			{
				String line = br.readLine();
				String[] tokens = line.split(delims);
				for(int col = 0; col < numCols; col++) 
				{
					//empty space are ignored
					if(Integer.parseInt(tokens[col]) != 0)
						map.add(new Tile(Integer.parseInt(tokens[col]),col* tileSize, row * tileSize, tileSize, tileSize));
				}
			}
			tilesTypes = new int[Integer.parseInt(br.readLine())];
			String line = "";
			for(int x = 0; x < tilesTypes.length; x++)
			{
				line = br.readLine();
				String[] tokens = line.split(delims);
				tilesTypes[Integer.parseInt(tokens[0])] = Integer.parseInt(tokens[1]);
			}
			/*for(Tile temp : map )
			{
				System.out.println(temp.boundingBox.getX() + " " + temp.boundingBox.getY());
			}*/
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
	}
	public int getTileSize() { return tileSize; }
	public double getx() { return x; }
	public double gety() { return y; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
}