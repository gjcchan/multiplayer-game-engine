package Network.Interface;

import frameworkObjects.sendData;

public class ObjectTransmitter {
	//buffer size (a place to store the bytes coming and going out), set if you receive too much data it will overflow, underflow is not an issue
	protected byte[] sendDataBuffer;
    protected byte[] receiveDataBuffer;
	
	//static parameters
	protected static final int sendBufferSize = 1024;
	protected static final int receiveBufferSize = 1024;
}
