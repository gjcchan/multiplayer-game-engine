package dependencies;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import AbstractObjects.Entities;

//quad trees: used to determine nearby objects of every object on map
//quad Tree collision detection, to use: for every frame: instantiate quadtree with screen size, populate, then check
public class QuadTree {

	private final static int  maxObjects = 10;
	private final static int maxDepth = 5;
	
	private final static int TOPLEFT = 0;
	private final static int TOPRIGHT = 1;
	private final static int BOTTOMRIGHT = 2;
	private final static int BOTTOMLEFT = 3;
	
	private int level;
	private List<Entities> objects;
	
	//bounds
	private int originX;
	private int originY;
	private int dimension;

	private List<QuadTree> nodes;
	
	//constructor
	public QuadTree(int pLevel,int newX,int newY,int newDimension) 
	{
		level = pLevel;
		objects = new ArrayList<Entities>();
		nodes = new ArrayList<QuadTree>();
		originX = newX;
		originY = newY;
		dimension = newDimension;
	}
	
	public List<Entities> retrieve(Entities object)
	{
		List<Entities> neighbours = new ArrayList<Entities>();
		int quadrant = getQuadrant(object);
		if(quadrant != -1 && nodes.get(TOPLEFT) != null)
		{
			switch(quadrant)
			{
				case TOPLEFT:
					neighbours.addAll(nodes.get(TOPLEFT).retrieve(object));
					break;
				case TOPRIGHT:
					neighbours.addAll(nodes.get(TOPRIGHT).retrieve(object));
					break;
				case BOTTOMRIGHT:
					neighbours.addAll(nodes.get(BOTTOMRIGHT).retrieve(object));
					break;
				case BOTTOMLEFT:
					neighbours.addAll(nodes.get(BOTTOMLEFT).retrieve(object));
					break;
			}			
		}
		else if(nodes.get(TOPLEFT) == null)
			neighbours.addAll(objects);
		return neighbours;
	}
	private void split()
	{
		int di = dimension>>1;
		nodes.add(TOPLEFT, new QuadTree(level+1, originX, originY, dimension));
		nodes.add(TOPRIGHT, new QuadTree(level+1, originX+di, originY,dimension));
		nodes.add(BOTTOMLEFT, new QuadTree(level+1, originX, originY+di, dimension));
		nodes.add(BOTTOMRIGHT, new QuadTree(level+1, originX+di, originY+di, dimension));	
	}
	private void refactor()
	{
		for(Entities object: objects)
		{
			int quadrant = getQuadrant(object);
			if(quadrant != -1)
			{
				switch(quadrant)
				{
					case TOPLEFT:
						nodes.get(TOPLEFT).insert(object);
						break;
					case TOPRIGHT:
						nodes.get(TOPRIGHT).insert(object);
						break;
					case BOTTOMRIGHT:
						nodes.get(BOTTOMRIGHT).insert(object);
						break;
					case BOTTOMLEFT:
						nodes.get(BOTTOMLEFT).insert(object);
						break;
				}
				objects.remove(object);
			}		
		}
	}
	private void insert(Entities object)
	{
		int quadrant = getQuadrant(object);
		if(nodes.get(TOPLEFT) != null)
		{
			if(quadrant != -1)
			{
				switch(quadrant)
				{
					case TOPLEFT:
						nodes.get(TOPLEFT).insert(object);
						break;
					case TOPRIGHT:
						nodes.get(TOPRIGHT).insert(object);
						break;
					case BOTTOMRIGHT:
						nodes.get(BOTTOMRIGHT).insert(object);
						break;
					case BOTTOMLEFT:
						nodes.get(BOTTOMLEFT).insert(object);
						break;
				}
				return;
			}
			objects.add(object);
		}
		if(objects.size() > maxObjects && level < maxDepth)
		{
			if(nodes.get(TOPLEFT) == null)
			{
				split();
				refactor();
			}	
		}	
	}
	
	private int getQuadrant(Entities object)
	{
		double midY = originY+dimension>>1;
		double midX = originX+dimension>>1;	
		//check left side (no need to check exceed right side max as previous node would've checked)
		if(object.x < midX && object.x + object.collisionPaddingRight < midX)
		{
			if(object.y < midY && object.y + object.collisionPaddingBottom + object.height < midY)
				return TOPLEFT;
			else if(object.y > midY)
				return BOTTOMLEFT;
		}
		if(object.x > midX)
		{
			if(object.y < midY && object.y + object.collisionPaddingBottom + object.height < midY)
				return TOPRIGHT;
			else if(object.y > midY)
				return BOTTOMRIGHT;
		}
		return -1;
	}
}
