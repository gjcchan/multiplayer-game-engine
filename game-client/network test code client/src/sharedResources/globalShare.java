package sharedResources;

import java.util.List;
import java.util.TreeMap;

import frameworkObjects.sendData;

public class globalShare 
{
	public int index;
	public sendData UDPstate;
	//used for interpolation
	public sendData previousState;
	public long betweenState;
	
	//public List<playerName> playerNames;
	public TreeMap<Integer, String> playerNames;
	public String username;
	
	public globalShare() {
		playerNames = new TreeMap<Integer, String>();
	}
}
