/**
 * 
 * @author Soumajyoti Sarkar
 * @version 1.0
 * @since 06-01-2018
 */

package main.java.com.belllabs.Helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//This class stores a relation property object - since each relation can have properties with different data types
//such as strings or integers - an object has been created
public class RelationPropObject{
	/**
	 * KEY - VALUE pairs
	 * KEY is always string
	 * VALUES - a Map containing integers or lists( example: flows) 
	 */ 
	// TODO: Index these RELATIONS instead of using string as the key - use Neo4j index properties
	private String key; // KEY is of type String - example d1.in1.out2 - this is also the unique relation name
	
	public Map<String, Object> propertiesObject = new HashMap<String, Object>();
	
	
	// This is to initialize variables if the proper constructor has not been called
	public RelationPropObject(String key){
		/**
		 *  @param key - link name - <INPPORT.OUTPORT>
		 */
		this.key = key;
		
		/*
		 * VALUES:
		 * 1. USED - 0/1 - indicates whether the edge has been used or not
		 * 2. FLOW - list of flows associated with the edge
		 */
		
		//  --------- Ideally use a loop with all possible properties that can fill them up automatically -------
		propertiesObject.put("used", 0 );
		propertiesObject.put("flow", new ArrayList<String>());
	}
	
	public void setProperties(int ifUsed, List<String> flowsEdge ){
	   propertiesObject.put("used", ifUsed );
	   propertiesObject.put("flow", flowsEdge);
	}
	
	public String getKey(){
		return key;
	}
}
