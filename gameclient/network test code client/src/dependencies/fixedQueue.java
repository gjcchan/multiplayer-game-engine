package dependencies;

import java.util.ArrayList;


public class fixedQueue<K> extends ArrayList<K> {
	private int maxSize;
	
	public fixedQueue(int size) 
	{
		this.maxSize = size;
	}
	public boolean add(K obj)
	{
		boolean r = super.add(obj);
		if (size() > maxSize)
		{
            removeRange(0, size() - maxSize - 1);
        }
        return r;
	}
	public K popLast()
	{
		
		K item = get(size()-1);
		super.remove(size()-1);
		return item;
	}
	
	
}
