package NetSimulator;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class DeviceNode {
	
	private int deviceId;
	private int edgeNodeId; // edge node associated with the device
	private double xPos;
	private double yPos;
	private Set<Integer> requestIds;
	private Vector<Integer> videoIds;

	
	public DeviceNode(int dev_id, double x_val, double y_val, int edge_id)
	{
		deviceId = dev_id;
		xPos = x_val;
		yPos = y_val;
		edgeNodeId = edge_id;
		requestIds = new HashSet();
		videoIds = new Vector<Integer>();
	}
	
	public int GetDeviceId ()
	{
		return deviceId;
	}
	
	public double GetDeviceLocationX()
	{
		return xPos;
	}
	
	public double GetDeviceLocationY()
	{
		return yPos;
	}

}
