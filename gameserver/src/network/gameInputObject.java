package network;

import network.objects.receiveData;

// essentially recieveData with IP and port tacked on
public class gameInputObject {
	public  boolean keyUp;
	public  boolean keyDown;
	public  boolean keyLeft;
	public  boolean keyRight;
	public  boolean jumpKey;
	public  boolean fire;

	public  String IPAddress;
	public  String command;
	public  int port;
	
	public gameInputObject(receiveData input, String IP, int newPort)
	{
		keyUp = input.keyUp;
		keyDown = input.keyDown;
		keyLeft = input.keyLeft;
		keyRight = input.keyRight;
		jumpKey = input.fire;
		IPAddress = IP;
		port = newPort;
		command = null;
	}
}
