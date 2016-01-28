package main;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFrame;

import network.TCPManager;
import network.UDPManager;
import network.objects.receiveData;
import network.objects.sendData;
import sharedResources.globalShare;


public class runtime {

	public static String serverIP = "127.0.0.1";//"fraser.sfu.ca";//"192.168.1.149";
	public static int port = 7236;
	public static String username = "DEV_USER";
	
	public static void main(String[] args)
	{
		
		receiveData globalOut = new receiveData();
		sendData globalState = new sendData();
		
		globalShare globalInfo = new globalShare();
		globalInfo.UDPstate = globalState;
		globalInfo.previousState = new sendData();
		globalInfo.currentSyncedState = new sendData();
		globalInfo.previousSyncedState = new sendData(); 
		globalInfo.username = username;
		
		TCPManager tcpConnection = new TCPManager(serverIP, port+1, globalInfo);
		UDPManager udpConnection = new UDPManager(port+3, globalInfo);
		keyPoller keysSender = new keyPoller(globalOut,udpConnection,port);
		new Thread(tcpConnection).start();
		new Thread(udpConnection).start();

		Renderer OGLDraw = new Renderer(globalOut, globalInfo);
		new Thread(OGLDraw).start();
		new Thread(keysSender).start();
	}
}
