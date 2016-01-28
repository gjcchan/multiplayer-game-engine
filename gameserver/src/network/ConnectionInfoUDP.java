package network;

public class ConnectionInfoUDP {
	public String IP;
	public int port;
	public String username;
	public int index;
	public ConnectionInfoUDP(String newIP) 
	{
		IP = newIP;
	}
	public ConnectionInfoUDP(String newIP, int newIndex) 
	{
		IP = newIP;
		index = newIndex;
	}
}
