/**
 * 
 * @author Soumajyoti Sarkar
 * @version 1.0
 * @since 06-01-2018
 */

package main.java.com.belllabs.devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import javafx.util.Pair;
import main.java.com.belllabs.Helpers.PortPropObject;
import java.util.concurrent.ConcurrentHashMap;

import main.java.com.belllabs.devices.CreateRelationTemplates;
import main.java.com.belllabs.Utilities.*;

public class CreateDeviceTemplates {	
	/**
	 * @param graphDb - the graph Database instance
	 * @param deviceName - this is the device ID - like 'H1' - the device identifier
	 */
	public static void createHost(GraphDatabaseService graphDb,  String deviceName){
		
		Node newPort;
		String nameProp;
		int maxInDeg, maxOutDeg, minInDeg, minOutDeg;
		PortPropObject inpPorts, outPorts;
		Map<String, Object> portPropertiesObject;
		
		nameProp = deviceName;
		
		// TODO: Take these as input
		maxInDeg = 1;
		maxOutDeg = 1;
		minInDeg = 0;
		minOutDeg = 0;
		
		inpPorts = new PortPropObject("HOST", nameProp, "single");
		inpPorts.setComProperties(maxInDeg, maxOutDeg, minInDeg, minOutDeg);			
		
		newPort = graphDb.createNode(Label.label("HOST"));
		newPort.setProperty("portName", inpPorts.getKey());
		portPropertiesObject = inpPorts.propertiesObject;
		
		// Iterate over all the properties passed to the port
		for(Map.Entry<String, Object> entry : portPropertiesObject.entrySet()){
			newPort.setProperty(entry.getKey(), entry.getValue());
		}
	}

	// WILL BE DEPRECATED IN FUTURE - REPLACED BY SWITCHES WITH DYNAMIC PORT ATTRIBUTES
	/**
	 * This method creates a Switch OR Optical Fabric Device in ROADMs (with colored ports)
	 * 
	 * @param graphDb - the graph Database instance
	 * @param deviceLabel - label of the device like "SWITCH" / "OF" (Optical Fabric)
	 * @param deviceName - this is the device ID - like 'S1' or 'WSS1'
	 * @param numPorts - number of ports
	 * @param portBand - initial port bandwidth - UNUSED
	 * @param numColorPorts - number of colored ports - required for WSS
	 * @param colors - list of distinct colors (not including the colorless ports)
	 * @param portCapacity - capacity(maximum bandwidth) of the ports
	 * 
	 * TODO: take the type of ports - OR fixed
	 */
	
	public static void createSwitchTemplate(GraphDatabaseService graphDb, Label deviceLabel, String deviceName, int numPorts, float portBand,
			                                int numColorPorts, List<String> colors, float portCapacity){


		Node newPort; // new port to be created for device
		String nameProp; // name of ports to be sent to Neo4j as parameter
		int maxInDeg, maxOutDeg, minInDeg, minOutDeg; // These are properties that are now fixed for each device but for dynamism the properties might not be chamged
		PortPropObject inpPorts, outPorts; // property object of the ports holding the values
		Map<String, Object> portPropertiesObject; // This should ideally be the input argument
		
		// TODO: REPLACE THE PORT PROPERTIES HERE BY THE ARGUMENT LIST - Send a portProperty object directly
		
		for(int i=1; i<=numPorts; i++){
			// 1. Create the input ports
			nameProp = deviceName + ".in" + Integer.toString(i);
			
			inpPorts = new PortPropObject(deviceName, nameProp, "digital"); // By default port types are default, if optical fabric, the port types get overwritten in the next section
			inpPorts.setSwitchProperties(0); // Default bandwidth is 0 since there is no flow yet

			// NOTE: The input ports should have the input vertex degree attributes only. Similar for the output ports
			maxInDeg = numPorts;
			maxOutDeg = numPorts;
			minInDeg = 0;
			minOutDeg = 0;
			
			inpPorts.setComProperties(maxInDeg, maxOutDeg, minInDeg, minOutDeg);	// Common properties

			// Create a new port in the graphDb and set the properties
			newPort = graphDb.createNode(deviceLabel);
			newPort.setProperty("portName", inpPorts.getKey());
			newPort.setProperty("type", (String) inpPorts.propertiesObject.get("type"));
			
			portPropertiesObject = inpPorts.propertiesObject; 
			
			// Iterate over all the properties passed to the port
			for(Map.Entry<String, Object> entry : portPropertiesObject.entrySet()){
				newPort.setProperty(entry.getKey(), entry.getValue());
			}
			
			//------------------------------------------------------------------------------
			// 2. Create the output ports
			nameProp = deviceName + ".out" + Integer.toString(i);

			outPorts = new PortPropObject(deviceName, nameProp, "digital");
			outPorts.setSwitchProperties(0); // Default bandwidth is 0 since there is no flow yet

			
			maxInDeg = numPorts;
			maxOutDeg = numPorts;
			minInDeg = 0;
			minOutDeg = 0;
			
			outPorts.setComProperties(maxInDeg, maxOutDeg, minInDeg, minOutDeg);	// Common properties

			newPort = graphDb.createNode(deviceLabel);
			newPort.setProperty("portName", outPorts.getKey());
			newPort.setProperty("type", (String) inpPorts.propertiesObject.get("type"));
			
			portPropertiesObject = inpPorts.propertiesObject; 
			
			// Iterate over all the properties passed to the port
			for(Map.Entry<String, Object> entry : portPropertiesObject.entrySet()){
				newPort.setProperty(entry.getKey(), entry.getValue());
			}
											
		}		
		
		// Add the colors to the ports - For Optical Fabric Devices in ROADMs		
		/**
		 * CONDITION: If the number of colored ports > 0, then add the colors for Optical Fabric devices, else ignore this for switches
		 */
		
		/*
		 * The first k ( = numPorts - numColorPorts ) on the input/output are uncolored and then the rest numColorPorts are colored
		 */
		if(numColorPorts > 0){
			int c = colors.size();
			int d = numColorPorts/c;
			int startColorPort = numPorts - numColorPorts + 1; // Start adding colors from this port - just adhoc choice 
			Map<String, Object> params = new HashMap<>();
			StringBuilder query = new StringBuilder();
			String nameInpProp, nameOutProp;
		
			// Colored ports
			int i = startColorPort;
			int startColor = 0;
			while(i <= numPorts){
				for(int j=i; j<i+d; j++){
					nameInpProp = deviceName +  "." + "in" + Integer.toString(j);
					nameOutProp = deviceName +  "." + "out" + Integer.toString(j);
					
					params.clear();
					params.put("portInpName", nameInpProp);
					params.put("portOutName", nameOutProp);
					params.put("type", "optical");
					params.put("color", colors.get(startColor)); // c" + Integer.toString(j-i+1)); // example, c1, c2, c3 ---> THESE CAN BE CHANGED
					
					query.setLength(0);
					query.append("MATCH (o1) WHERE o1.portName = $portInpName ");
					query.append("MATCH (o2) WHERE o2.portName = $portOutName ");
					query.append("SET o1.type = $type ");  // Change the type of these ports to optical from the default digital
					query.append("SET o2.type = $type ");  // Change the type of these ports to optical from the default digital
					query.append("SET o1.color = $color ");
					query.append("SET o2.color = $color");
					
					graphDb.execute(query.toString(), params);
	
				}
				
				i = i+d;
				startColor += 1;
			}
			
			// Uncolored ports
			i = 1;
			while(i < startColorPort){
				
				nameInpProp = deviceName +  "." + "in" + Integer.toString(i);
				nameOutProp = deviceName +  "." + "out" + Integer.toString(i);
				
				params.clear();
				params.put("portInpName", nameInpProp);
				params.put("portOutName", nameOutProp);
				params.put("type", "optical");
				params.put("color", "None"); // None = No color
				
				query.setLength(0);
				query.append("MATCH (o1) WHERE o1.portName = $portInpName ");
				query.append("MATCH (o2) WHERE o2.portName = $portOutName ");
				query.append("SET o1.type = $type ");  // Change the type of these ports to optical from the default digital
				query.append("SET o2.type = $type ");  // Change the type of these ports to optical from the default digital
				query.append("SET o1.color = $color ");
				query.append("SET o2.color = $color");
				
				graphDb.execute(query.toString(), params);
								
				i = i+1;
			}
		}

	}
	
	/**
	* This method creates an abstract SWITCH - so the input/output port properties are derived 
	* 
	* @param graphDb - the graph Database instance
	* @param deviceLabel - the label of the device - 'SWITCH' OR "Abstract.Switch"
	* @param deviceName - this is the device ID - like 'S1'
	* @param portsPropListInp - list of all input ports with their associated <Name, Properties> object
	* @param portsPropListOut - list of all output ports with their associated <Name, Properties> object
	*/
	public static void createSwitchWithDynamicPortsTemplate(GraphDatabaseService graphDb, Label deviceLabel, String deviceName, List<Pair<String, Map<String, Object>>> portsPropListInp, 
			List<Pair<String, Map<String, Object>>> portsPropListOut){

		
		if(portsPropListInp.size() != portsPropListOut.size()){
			throw new java.lang.Error("The number of input ports do match the number of output ports.");
		}
		
		Node newPort;
		String nameProp;
		
		for(Pair<String, Map<String, Object>> pports: portsPropListInp ){
			String portName  = pports.getKey();
			Map<String, Object> portProp = pports.getValue();
			try ( Transaction tx = graphDb.beginTx() ){
				newPort = graphDb.createNode(deviceLabel);
				newPort.setProperty("portName", portName);
				newPort.setProperty("type", (String) portProp.get("type"));		
				Iterator it = portProp.entrySet().iterator();
				while(it.hasNext()){
			        Map.Entry pairVal = (Map.Entry)it.next();
			        if(!(pairVal.getKey().equals("type"))){
			        	newPort.setProperty((String) pairVal.getKey(), pairVal.getValue());
			        }
				}
	            tx.success();
	            tx.close();
	        }catch(Exception e){
				System.out.println("The port type must be provided");
				e.printStackTrace();
			}
		}
		
		for(Pair<String, Map<String, Object>> pports: portsPropListOut ){
			String portName  = pports.getKey();
			Map<String, Object> portProp = pports.getValue();
			try ( Transaction tx = graphDb.beginTx() ){
				newPort = graphDb.createNode(deviceLabel);
				newPort.setProperty("portName", portName);
				newPort.setProperty("type", (String) portProp.get("type"));		
				Iterator it = portProp.entrySet().iterator();
				while(it.hasNext()){
			        Map.Entry pairVal = (Map.Entry)it.next();
			        if(!(pairVal.getKey().equals("type"))){
			        	newPort.setProperty((String) pairVal.getKey(), pairVal.getValue());
			        }
				}
	            tx.success();
	            tx.close();
	        }catch(Exception e){
				System.out.println("The port type must be provided");
				e.printStackTrace();
			}
		}

}
	/**
	 * Creates ADPATERS (mux / demux) with digital ports having numDPort signals aggregated (disaggregated in the reverse dir.) into 1 optical port (single wavelength) and with n such optical ports.
	 * 
	 * @param graphDb - the graph Database instance
	 * @param deviceLabel - the label of the device (like oPort for Optical port, dPort for switches, )
	 * @param deviceName - the device ID such as A1 		
	 * @param deviceType - the type of device - mux/demux/signal change line/ links
	 * @param k - number of digital ports multiplexed to / demultiplexed from one optical port
	 * @param n - number of optical ports
	 * @param dPortBand - bandwidth of the digital ports - UNUSED
	 * @param oPortBand - bandwidth of the optical ports - UNUSED
	 * @param oPortRate - rate of the optical ports - TODO: DECOMPOSE TO SIGNAL RATE AND DATA RATE
	 * @param dPortCap - digital port capacity - UNUSED
	 * @param oPortCap - optical port capacity - UNUSED
	 * @param oPortReach - optical port reach - UNUSED
	 */ 
		
	public static void createAdapterTemplate(GraphDatabaseService graphDb, Label deviceLabel, String deviceName, String deviceType, int k, int n,
			                                 float dPortBand, float oPortBand, float oPortRate, float dPortCap, float oPortCap, float oPortReach){
		
		Node newPort;
		String nameProp="";
		int maxInDeg = 0, maxOutDeg=0, minInDeg=0, minOutDeg=0;
		Map<String, Object> portPropertiesObject; // This should be the input argument for the properties instead of single inputs for each
		PortPropObject oPorts, dPorts;
		
		int numDPort = n*k;
		int numOPort = n;
		
		for(int i=1; i<=numDPort; i++){
			/*
			 * These are the digital ports of the adaptors
			 */
						
			if(deviceType == "MUX"){
				/*
				 * this is a mux setting
				 */
				nameProp = deviceName +  "." + "in" + Integer.toString(i);
				
				// Take these as input
				maxInDeg = 0;
				maxOutDeg = n;
				minInDeg = 0;
				minOutDeg = 0;
				

				
			}
			else if (deviceType == "DEMUX"){
				/*
				 * this is a demux setting
				 */
				nameProp = deviceName +  "." + "out" + Integer.toString(i);
				
				// Take these as input
				maxInDeg = n;
				maxOutDeg = 0;
				minInDeg = 0;
				minOutDeg = 0;
			}
			
			dPorts = new PortPropObject("ADAPTER", nameProp, "digital"); // digital denotes type 
			dPorts.setAdapterProperties(dPortCap, 0f, 0f, 0f ); // UNUSED parameters in the input arguments can be used to fill  them
			
			dPorts.setComProperties(maxInDeg, maxOutDeg, minInDeg, minOutDeg);	// Common properties

			newPort = graphDb.createNode(deviceLabel);
			newPort.setProperty("portName", dPorts.getKey());
			newPort.setProperty("type", (String) dPorts.propertiesObject.get("type"));
			
			portPropertiesObject = dPorts.propertiesObject; 
			
			// Iterate over all the properties passed to the port
			for(Map.Entry<String, Object> entry : portPropertiesObject.entrySet()){
				newPort.setProperty(entry.getKey(), entry.getValue());
			}
		}	
		
		for(int i=1; i<=numOPort; i++){
			/*
			 * These are the optical ports of the adaptors
			 */
			if(deviceType == "MUX"){
				/*
				 * this is a mux setting
				 */
				nameProp = deviceName +  "." + "out" + Integer.toString(i);
				
				// Take these as input
				maxInDeg = k;
				maxOutDeg = 0;
				minInDeg = 0;
				minOutDeg = 0;
				

			}
			else if (deviceType == "DEMUX"){
				/*
				 * this is a demux setting
				 */
				nameProp = deviceName +  "." + "in" + Integer.toString(i);
				
				// Take these as input
				maxInDeg = 0;
				maxOutDeg = k;
				minInDeg = 0;
				minOutDeg = 0;
				

			}
			
			oPorts = new PortPropObject( "ADAPTER", nameProp, "optical"); // optical denotes type 
			oPorts.setAdapterProperties(oPortCap, oPortRate, oPortBand, oPortReach);
			oPorts.setComProperties(maxInDeg, maxOutDeg, minInDeg, minOutDeg);	// Common properties

			newPort = graphDb.createNode(deviceLabel);
			newPort.setProperty("portName", oPorts.getKey());
			newPort.setProperty("type", (String) oPorts.propertiesObject.get("type"));
			
			portPropertiesObject = oPorts.propertiesObject; 
			
			// Iterate over all the properties passed to the port
			for(Map.Entry<String, Object> entry : portPropertiesObject.entrySet()){
				newPort.setProperty(entry.getKey(), entry.getValue());
			}
		}
	}
	
	/**
	* Creates DWDMs for ROADMs (ADD/DROP BANKS)
	* 
	* @param graphDb - the graph Database instance
	* @param deviceLabel - the label of the device (like oPort for Optical port, dPort for switches, )
	* @param deviceName - the device ID such as A1 		
	* @param deviceType - the type of device - mux/demux/signal change line/ links
	* @param c - number of colors
	* @param d - number of direction
	* 
	* 
	* @return NULL
	*/
	public static void createDWDMTemplate(GraphDatabaseService graphDb, Label deviceLabel, String deviceName, String deviceType, int c, int d, List<String> colorList) throws IOException{
	 
		
		// Data Structures
		Node newPort;
		String nameProp="";
		int inDeg = 0, outDeg=0, maxDeg=0, minDeg=0;
		Map<String, Object> portPropertiesObject;

		PortPropObject oPorts, dPorts;
		
		int numColorPort = c*d;
		int numDirPort = d;
		

		for(int i=1; i<=numColorPort; i++){
			/*
			* These are the optical ports of the DWDM
			*/
			
			if(deviceType == "ADD"){
				/*
				* this is a mux setting
				*/
				nameProp = deviceName +  "." + "in" + Integer.toString(i);
				
			}
			else if (deviceType == "DROP"){
				/*
				* this is a demux setting
				*/
				nameProp = deviceName +  "." + "out" + Integer.toString(i);
			}
			
			dPorts = new PortPropObject("DWDM", nameProp, "optical"); // optical denotes type
						
			newPort = graphDb.createNode(deviceLabel);
			newPort.setProperty("portName", dPorts.getKey());
			newPort.setProperty("type", (String) dPorts.propertiesObject.get("type"));
			
			portPropertiesObject = dPorts.propertiesObject; 
			
			// Iterate over all the properties passed to the port
			for(Map.Entry<String, Object> entry : portPropertiesObject.entrySet()){
				newPort.setProperty(entry.getKey(), entry.getValue());
			}

		}	
		
		// Set the colors of the optical ports
		Map<String, Object> params = new HashMap<>();
		StringBuilder query = new StringBuilder();
		
		for(int i=1; i<=numDirPort; i++){  // directions = banks
			for(int j=1; j<=c; j++){ //colors
				// (1, 2, 3)  ---> (4, 5, 6) --> ..... (So 1, 2, 3 gets different colors), (4, 5, 6 in the same order as 1, 2, 3)
				int k = j + ((i-1)*c); // port number
				

				if(deviceType == "ADD"){
					/*
					* this is a mux setting
					*/
					nameProp = deviceName +  "." + "in" + Integer.toString(k);
					
				}
				else if (deviceType == "DROP"){
					/*
					* this is a demux setting
					*/
					nameProp = deviceName +  "." + "out" + Integer.toString(k);
				}
				
				params.clear();
				params.put("name", nameProp);
				params.put("color", colorList.get(j-1)); // example, c1, c2, c3 ---> THESE CAN BE CHANGED
				
				System.out.println(nameProp + " gets color " + colorList.get(j-1));
				query.setLength(0);
				query.append("MATCH (o) WHERE o.portName = $name ");
				query.append("SET o.color = $color");
				
				graphDb.execute(query.toString(), params);

				
			}
		}
		
		
		for(int i=1; i<=numDirPort; i++){
			/*
			* These are the fiber ports of the DWDM
			*/
			if(deviceType == "ADD"){
				/*
				* this is a mux setting
				*/
				nameProp = deviceName +  "." + "out" + Integer.toString(i);
			}
			else if (deviceType == "DROP"){
				/*
				* this is a demux setting
				*/
				nameProp = deviceName +  "." + "in" + Integer.toString(i);
			}
			
			oPorts = new PortPropObject("DWDM", nameProp, "fiber"); // fiber denotes direction ports
			
			newPort = graphDb.createNode(deviceLabel);
			newPort.setProperty("portName", oPorts.getKey());
			newPort.setProperty("type", (String) oPorts.propertiesObject.get("type"));
			
			portPropertiesObject = oPorts.propertiesObject; 
			
			// Iterate over all the properties passed to the port
			for(Map.Entry<String, Object> entry : portPropertiesObject.entrySet()){
				newPort.setProperty(entry.getKey(), entry.getValue());
			}

		}
		
		// Set the directions of the banks one by one on the fiber ports
		for(int i=1; i<=numDirPort; i++){
			if(deviceType == "ADD"){
				/*
				* this is a mux setting
				*/
				nameProp = deviceName +  "." + "out" + Integer.toString(i);
			}
			else if (deviceType == "DROP"){
				/*
				* this is a demux setting
				*/
				nameProp = deviceName +  "." + "in" + Integer.toString(i);
			}
			
			params.clear();
			params.put("name", nameProp);
			params.put("direction", "d" + Integer.toString(d)); // example, d1, d2, d3 ---> THESE SHOULD BE PARTIUCLAR ROADM directions
			
			query.setLength(0);
			query.append("MATCH (o) WHERE o.portName = $name ");
			query.append("SET o.direction = $direction");
			
			graphDb.execute(query.toString(), params);
		}
		
	}
	/**
	 * 
	 * ROADM is a composite device consisting of Optical Fabric device, Adaptors, DWDM(another type of adaptors)
	 * 
	 * @param graphDb - the graph Database instance
	 * @param deviceLabel - the label of the device (like oPort for Optical port, dPort for switches, )
	 * @param deviceName - the device ID such as R1
	 * @param k - number of digital ports multiplexed to / demultiplexed from one optical port
	 * @param n - number of optical ports
	 * @param c - number of colors - THE COLORS ARE CHOSEN MANUALLY - TODO: INPUT 
	 * @param d - number of directions - THE DIRECTIONS ARE MANUALLY CHOSEN NOW - TODO: INPUT
	 * @param dPortBand - bandwidth of the digital ports 
	 * @param oPortBand - bandwidth of the optical ports 
	 * @param oPortRate - rate of the optical ports - TODO: DECOMPOSE TO SIGNAL RATE AND DATA RATE
	 * @param dPortCap - digital port capacity 
	 * @param oPortCap - optical port capacity 
	 * @param oPortReach - optical port reach 
	 */ 
	
	public static void createROADMTemplate(GraphDatabaseService graphDb, Label deviceLabel, String deviceName, int k, int n, 
			                               int c, int d,
			                               float dPortBand, float oPortBand, float oPortRate, float dPortCap, float oPortCap, float oPortReach,
			                               List<Pair<String, String>> portConnAB, List<Pair<String, String>> portConnDB ) throws IOException{

		
		// GET THE LIST OF COLORS FOR THE ROADM device
		// Get the ITU grid channels and their mapped colors
		int spacing = 100; // unit in Ghz
		ArrayList<Pair<Integer, Integer>> channels =  ItuGrid.channelFreq(spacing);
		Map<Object, List<Integer>> channelColors = new ConcurrentHashMap<>();
		List<String> colorCSSList = new ArrayList<String>();
//		System.out.println("\n The channels and their frequencies are: " );
		
		/*
		 * For now, need c colors - c taken as input
		 */
//		int countColors = 0;
//		for(Pair<Integer, Integer> ch: channels){
//			List<Integer> rgb = Colors.color(Integer.toString(ch.getValue()), channelColors); // converted to string to add more contrastive colors
//			String cssColor ="rgb("; // format "rgb(255, 0, 0)" example for CSS rendering
//			for(Integer v: rgb){
//				cssColor += Integer.toString(v) ;
//				cssColor += ", ";
//			}
//			cssColor = cssColor.substring(0, cssColor.length()-2);
//			cssColor += ")";
//
//			colorCSSList.add(cssColor);
//			countColors += 1;
//			if(countColors >= c)
//				break;
//		}
		
		// Use fixed colors from the fixed colors List
		List<List<Integer>> colorList = Colors.fixedColors();
		for(int countColors=0; countColors<c; countColors++){
			String cssColor ="rgb("; // format "rgb(255, 0, 0)" example for CSS rendering
			List<Integer> rgb = colorList.get(countColors);
			for(Integer v: rgb){
				cssColor += Integer.toString(v) ;
				cssColor += ", ";
			}
			cssColor = cssColor.substring(0, cssColor.length()-2);
			cssColor += ")";

			colorCSSList.add(cssColor);
		}
		
		
		// Create individual devices
		
		String deviceType, deviceNameLocal;
		// Create client ADD MUX (AM) / DROP DEMUX(DDM)
		deviceType = "MUX";
		deviceNameLocal = deviceName + ".AM";
		createAdapterTemplate(graphDb, deviceLabel, deviceNameLocal, deviceType, k, n, dPortBand, oPortBand, oPortRate, dPortCap, oPortCap, oPortReach);
		CreateRelationTemplates.createAutoRelationshipsIntraAdapters(graphDb, deviceLabel, deviceNameLocal, deviceType);
		
		deviceType = "DEMUX";
		deviceNameLocal = deviceName + ".DDM";
		createAdapterTemplate(graphDb, deviceLabel, deviceNameLocal, deviceType,  k, n, dPortBand, oPortBand, oPortRate, dPortCap, oPortCap, oPortReach);
		CreateRelationTemplates.createAutoRelationshipsIntraAdapters(graphDb, deviceLabel, deviceNameLocal, deviceType);
		
		// Create ADD / DROP BANKS
		deviceType = "ADD";
		deviceNameLocal = deviceName + ".AB";
		createDWDMTemplate(graphDb, deviceLabel, deviceNameLocal, deviceType, c, d, colorCSSList); // **capacity for both ports = 1 --> full wavelength
		CreateRelationTemplates.createAutoRelationshipsIntraAdapters(graphDb, deviceLabel, deviceNameLocal, deviceType);
		
		deviceType = "DROP";
		deviceNameLocal = deviceName + ".DB";
		createDWDMTemplate(graphDb, deviceLabel, deviceNameLocal, deviceType, c, d, colorCSSList ); // **capacity for both ports = 1 --> full wavelength
		CreateRelationTemplates.createAutoRelationshipsIntraAdapters(graphDb, deviceLabel, deviceNameLocal, deviceType);
		
		// Create the WSS device
		deviceNameLocal = deviceName + ".WSS";
		
	
		int numPorts = (c*d) + (n); // THIS NEEDS TO BE PRECISELY DEFINED
		float portBand = 0f;
		createSwitchTemplate(graphDb, deviceLabel, deviceNameLocal, numPorts, 0f, c*d, colorCSSList, portBand); // Fill the colors
		CreateRelationTemplates.createAutoRelationshipsIntraWSS(graphDb, deviceLabel, deviceNameLocal, colorCSSList);
		
		// Create the inter device relationships
		
		String WSSName = deviceName + ".WSS";

		/*
		 * TODO: This needs some inspection
		 */
		
		// Client MUX/DEMUX and WSS
		String adapterName = deviceName + ".AM";
		CreateRelationTemplates.createAutoRelationshipsSwitchAndAdapter(graphDb, WSSName, 1, n, adapterName, 1, n, false);
		
		adapterName = deviceName + ".DDM";
		CreateRelationTemplates.createAutoRelationshipsSwitchAndAdapter(graphDb, WSSName, 1, n, adapterName, 1, n, true);
		
		// LINE ADD/DROP BANK and WSS
		adapterName = deviceName + ".AB";
		CreateRelationTemplates.createAutoRelationshipsWSSAndDWDM(graphDb, WSSName,  adapterName,  true, portConnAB);
		
		adapterName = deviceName + ".DB";
		CreateRelationTemplates.createAutoRelationshipsWSSAndDWDM(graphDb, WSSName,  adapterName, false, portConnDB);

		
	}
	
}
