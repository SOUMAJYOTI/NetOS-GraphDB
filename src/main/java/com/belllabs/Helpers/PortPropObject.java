package main.java.com.belllabs.Helpers;

import java.util.HashMap;
import java.util.Map;

//This class stores a port property object - since each port can have properties with different data types
//such as strings or integers - an object has been created
public class PortPropObject{
	/**
	 * KEY - VALUE pairs
	 * KEY is always string
	 * VALUES - a Map containing the desired data corresponding to the port attribute
	 */ 
	
	 String key; // KEY stores the unique ID of the device - "S1" or "S2" or "OXC1" and so on
	 String device; // DEVICE stores the device type like "SWITCH" / "OXC" ...
	 public Map<String, Object> propertiesObject = new HashMap<String, Object>(); // this map stores the node properties for visualization 
	 
	 // Default Constructor
	 public PortPropObject(String device, String key, String portType){
		 /**
		  * 
		  * @param device - takes as input device type SWITCH/ROADM/ADAPTER/DWDM/WSS
		  * @param key - port ID : unique
		  * @param portType - for now the type is digital/optical/fiber (depending on the layer) - this can be changed
		  */
		this.device = device; // REQUIRED
	 	this.key = key; // REQUIRED
	 	propertiesObject.put("type", portType); // REQUIRED

	 	//--------------------------------------------------------------
	 	// THESE ARE OPTIONAL - varies for each device
	 	propertiesObject.put("maxInDeg", 0 );
	 	propertiesObject.put("maxOutDeg", 0 );
	 	propertiesObject.put("maxInDeg", 0 );
	 	propertiesObject.put("maxOutDeg", 0 );
	 	propertiesObject.put("available", 0);

	 	propertiesObject.put("capacity", 0f);
	 	propertiesObject.put("bandwidth", 0f);

	 	if(portType.equals("optical")){
	 		propertiesObject.put("rate", 0f);	
	 		propertiesObject.put("residualCapacity", 1f);
	 	}
		if(portType.equals("digital")){
	 		propertiesObject.put("residualCapacity", 40f);
	 	}
		if(portType.equals("fiber") || portType.equals("single")){
	 		propertiesObject.put("residualCapacity", 0f);
	 	}
		if(portType.equals("optical") && device.contains("ROADM")){
			propertiesObject.put("color", "None"); 
		}
		if(portType.equals("fiber") && device.contains("ROADM")){
		 	propertiesObject.put("direction", "None");
		}
	 }
	 
	 public PortPropObject(String key){
		 /**
		  * This constructor is for creating visualization objects
		  */
		 	this.key = key; // REQUIRED
		 }
	 
	 public void setAdapterProperties(float capacity, float rate, float bandwidth, float reach){
		 /**
		  * These are the adapter properties
		  * 
		  * @param capacity - capacity of the digital ports
		  * @param rate - rate of the optical ports (data/signal)
		  * @param bandwidth - bandwidth of the digital/optical ports
		  * @param reach - reach of the optical ports
		  *
		  */
		propertiesObject.put("capacity", capacity);
	 	propertiesObject.put("rate", rate);
	 	propertiesObject.put("bandwidth", bandwidth);
	 	propertiesObject.put("reach", reach);
	 }
	 
	 public void setDWDMProperties(String color, String direction){
		 /**
		  * These are the DWDM properties
		  * 
		  * @param color - color of the optical ports
		  * @param direction - direction of the fiber ports
		  */
		propertiesObject.put("color", color);
	 	propertiesObject.put("direction", direction);
	 }
	 
	 public void setSwitchProperties(float bandwidth){
		 /**
		  * These are the switch properties
		  * 
		  * @param bandwidth - initial bandwidth of the ports
		  *
		  */
		propertiesObject.put("bandwidth", bandwidth);
	 }
	
	public void setComProperties(int maxInDeg, int maxOutDeg, int minInDeg, int minOutDeg ){
		 /**
		  * The arguments structure should be a list holding key value pairs - only relevant values to be filled
		  * these are the common properties across devices
		  */
		 /**
		 * @param maxInDeg
		 * @param maxOutDeg
		 * @param minInDeg
		 * @param minOutDeg
		 */
	    propertiesObject.put("minInDeg", minInDeg );
	 	propertiesObject.put("minOutDeg", minOutDeg );
	 	propertiesObject.put("maxInDeg", maxInDeg );
	 	propertiesObject.put("maxOutDeg", maxOutDeg );
	 }
	 	
	public String getKey(){
	 	return key;
	 }
	 
	 // Return property elements for visualization
	 public Object getPropViz(String propName){
		 if(propertiesObject.containsKey(propName) == true)
			 return propertiesObject.get(propName);
		 else
			 return "None";
		 
	 }
}