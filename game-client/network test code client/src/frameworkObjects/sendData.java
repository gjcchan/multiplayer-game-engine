package frameworkObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class sendData implements Serializable 
{
	public List<RepackagedPlayer> playerList;
	
	public sendData()
	{
		playerList = new ArrayList<RepackagedPlayer>();
	}
}
