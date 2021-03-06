package network.extensions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;


/*This is a shell for serializing objects for networks
 * 
 * 
 */
public class ObjectTransmitter {
	//input streams, used to rebuild objects from byte arrays
	private ByteArrayInputStream dataInputStream;
	private ObjectInputStream objectStream;
	
	//output streams used to turn objects into byte arrays
	private ByteArrayOutputStream dataOutputStream;
	private ObjectOutputStream objectOut;
	
	
	public static final boolean _COMPRESSION = false;
	//used for TCP only since persistent connection
	public boolean _TCP_HEADER = true;
	/***
	 * 
	 * HEADER DATA 3byte length, first 4 bits are objectID, other 20bits are length, making max object length 1048576 or ~1MB (index 0 is LSB)
	 * 
	 */
	public byte[] appendTCPHeader(byte[] data, String dataType) 
	{
		//create the new byte container and add data length to header
		byte[] bytes = new byte[3 + data.length];
		for (int i = 0; i < 3; i++) 
		{
		    bytes[i] = (byte) (data.length >>> (i * 8));
		}
		//turn 4 MSB into ObjectID
		///clear 4 MSB
		bytes[2] = (byte) (bytes[2] << 4);
		bytes[2] = (byte) (bytes[2] >>> 4);
		//insert objectID (binary addition)
		if(dataType.toLowerCase() == "string")
			bytes[2] = changeBit(bytes[2], 5, true);
		
		//populate the new byte array with the content
		for (int x = 3; x < data.length + 3; x++)
		{
			bytes[x] = data[x-3];
		}
		//System.out.println(Arrays.toString(bytes));
		//System.out.print("["+bytes[0]+", " + bytes[1] +", " + bytes[2] + "]" );
		return bytes;
	}
	//used to changing value of bits in a byte
	private byte changeBit (byte input, int pos, boolean value) 
	{
		if(value)
		{
			return (byte) (input | (1 << pos-1));
		}
		else
		{
			return (byte) (input & ~(1 << pos-1));
		}
	}
	public byte[] serializeObject(Object c) throws IOException
	{
		dataOutputStream = new ByteArrayOutputStream();
		objectOut = new ObjectOutputStream(dataOutputStream);
		objectOut.writeObject(c);
		byte[] serializedArray = dataOutputStream.toByteArray();
		objectOut.close();
		dataOutputStream.close();
		
		if(_COMPRESSION == true) 
			serializedArray = deltaCompress(serializedArray);
		
		return serializedArray;
	}
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException, StreamCorruptedException {
		//System.out.println(new String(data, "UTF-8"));
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);	    
	    Object a =  is.readObject();
	    is.close();
	    in.close();
	    return a;
	}
	//algorithims copied of wikipedia, need to verify
	public static byte[] deltaCompress(byte[] data)
	{
	    byte last = 0;
	    for (int i = 0; i < data.length; i++)
	    {
	    	byte current = data[i];
	    	data[i] = (byte) (data[i] - last);
	        last = current;
	    }
		return data;
	}
	public static byte[] deltaDecompress(byte[] data)
	{
		byte last = 0;
	    for (int i = 0; i < data.length; i++)
	    {
	    	data[i] = (byte) (data[i] + last);
	        last = data[i];
	    }
	    return data;
	}
	public void broadcast()
	{
		//implement later
	}
	protected Object buildObject(InputStream persistentInputStream) throws IOException, ClassNotFoundException
	{
		byte[] headerBuffer = new byte[3];
		byte[] inputBuffer;
		//read header
		headerBuffer[0] = (byte) persistentInputStream.read();
		headerBuffer[1] = (byte) persistentInputStream.read();
		headerBuffer[2] = (byte) persistentInputStream.read();
		//System.out.println(Arrays.toString(headerBuffer));
		//add compile length of packet value 
		int dataLength = (int)(headerBuffer[0]& 0xFF) + (int)(headerBuffer[1]& 0xFF * 256);
		
		//retrieve objectID from the 4 MSB from the header
		int objectID = (int)(headerBuffer[2] >>> 4) & 0xFF;
		
		//strip out objectID of third byte (& 0xFF operator converts signed to unsigned int) and retrieve data length
		headerBuffer[2] = (byte) (headerBuffer[2] << 4);
		headerBuffer[2] = (byte) (headerBuffer[2] >>> 4);
		//System.out.println(Arrays.toString(headerBuffer));
		dataLength += (int)(headerBuffer[2]& 0xFF * 65536);
		//read data
		inputBuffer = new byte[dataLength];
		persistentInputStream.read(inputBuffer);
	    ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(inputBuffer));	
		Object a = is.readObject();
		is.close();
		return a;
	}
}
