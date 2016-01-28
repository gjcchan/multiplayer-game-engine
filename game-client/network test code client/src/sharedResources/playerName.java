package sharedResources;

public class playerName {
	public int index;
	public String name;
	
	public playerName(String userName, int newIndex)
	{
		index = newIndex;
		name = userName;
	}
	public playerName(String userName, String newIndex) throws Exception
	{
		index = Integer.parseInt(newIndex);
		name = userName;
	}
}
