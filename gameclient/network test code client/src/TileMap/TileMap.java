package TileMap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TileMap {
	
	// position
	private double x;
	private double y;
	
	private double tween;
	
	// map
	public int[][] map;
	public int tileSize;
	public int numRows;
	public int numCols;
	private int width;
	private int height;
	
	// tileset
	private Tile[] tiles;
	
	public TileMap(int tileSize) 
	{
		this.tileSize = tileSize;
		tween = 0.07;
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
			map = new int[numRows][numCols];
			width = numCols * tileSize;
			height = numRows * tileSize;
			
			String delims = "\\s+";
			for(int row = 0; row < numRows; row++) 
			{
				//System.out.println("reading row in TileMap.loadMap");
				String line = br.readLine();
				String[] tokens = line.split(delims);
				for(int col = 0; col < numCols; col++) 
				{
					map[row][col] = Integer.parseInt(tokens[col]);
				}
			}
			//System.out.println(Arrays.deepToString(map));
			tiles = new Tile[Integer.parseInt(br.readLine())];
			String line = "";
			for(int x = 0; x < tiles.length; x++)
			{
				line = br.readLine();
				String[] tokens = line.split(delims);
				tiles[Integer.parseInt(tokens[0])] = new Tile(Integer.parseInt(tokens[1]));
				if(x > 0)
					tiles[Integer.parseInt(tokens[0])].loadTexture("/Tiles/tile" + Integer.parseInt(tokens[0]) + ".png");
			}
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
	
	public void setTween(double d) { tween = d; }
	
	public void setPosition(double x, double y) 
	{	
		this.x += (x - this.x) * tween;
		this.y += (y - this.y) * tween;		
		//colOffset = (int)-this.x / tileSize;
		//rowOffset = (int)-this.y / tileSize;		
	}

	public int getType(int row, int col) {
		return tiles[map[row][col]].getType();
	}
	public Tile getTile(int index)
	{
		return tiles[index];
	}
}