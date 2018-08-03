/**
 * 
 * @author Soumajyoti Sarkar
 * @version 1.0
 * @since 06-01-2018
 */


package main.java.com.belllabs.devices;

import static java.lang.Math.toIntExact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Result;

import main.java.com.belllabs.Utilities.RelationProperties;


public class CreateRelationTemplates {
	// ------------------------------------- INTRA-DEVICE RELATIONSHIPS ---------------------------------------------------
	/**
	 * @param graphDb
	 * @param devcieLabel - label of the device - SWITCH / ROUTERS
	 * @param deviceName - name of the device like "S1" or "R2" whose input-output links need to be created. This identifies the 
	 *                        switch or router. 
	 */
	public static void createAutoRelationshipsIntraSwitches(GraphDatabaseService graphDb, Label deviceLabel, String deviceName){
	
		Map<String, Object> params = new HashMap<>(); // holds the map object for neo4j java queries
		String query;
		Result results;
		
		// 1. Find the input ports
		params.clear();
		params.put("type", "in");
		params.put("dName", deviceName);
		query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
		results = graphDb.execute(query, params);
				
		Long numInpPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numInpPorts = (Long) row.get("count");
		}
		 
		// **** Create the relationships - Run iterations connecting input and output ports
		
		int numIntInpPorts = toIntExact(numInpPorts); // this is done since the count return clause in neo4j returns long integers by default

		// Link the input port to output port for all the relationships
		String rQuery;
		int used=0; // By default all links are unused 
		
		List<String> flowList = new ArrayList<String>();
		final StringBuilder relationQuery = new StringBuilder();
		
		String relName; 
		for(int i=1; i<=numIntInpPorts; i++) {
			for(int j=1; j<=numIntInpPorts; j++){
				
				relName = deviceName + "." + "in" + Integer.toString(i) + "." +  "out" + Integer.toString(j); // <deviceName>.<inPortId>.<outPortId>
				
				System.out.println(deviceName + "." + "in" + Integer.toString(i) + "  to   "  + deviceName + "." + "out" + Integer.toString(j));
				
				params.clear();
				params.put("portIn", deviceName + ".in" + Integer.toString(i));
				params.put("portOut", deviceName + ".out" + Integer.toString(j));
				params.put("rName", relName); 
				
				relationQuery.setLength(0);
				relationQuery.append("MATCH (si) WHERE si.portName = $portIn ");
				relationQuery.append("MATCH (so) WHERE so.portName = $portOut ");
				relationQuery.append("CREATE (si)-[r:LINK ]->(so) ");
				relationQuery.append("SET r.name = $rName");
				
				rQuery = relationQuery.toString();
				graphDb.execute(rQuery, params);
				
				// These are the relation object properties.
				used = 0; // initially every link is unused
				flowList = new ArrayList<String>();
				flowList.add("N"); // Initially every link has no flows attached - this should be empty and commentd out
				
				// Set the relation properties on the link
				RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
			}
		}
	}
	/**
	 * @param graphDb
	 * @param devcieLabel - label of the device - WSS
	 * @param deviceName - name of the device like "W1" or "W2" whose input-output links need to be created. This identifies the 
	 *                        WSS device uniquely. - NEED BETTER IDENTITY PROPERTIES
	 * @param colorList - the ITU grid channel maps / Flex grid
	 */
	public static void createAutoRelationshipsIntraWSS(GraphDatabaseService graphDb, Label deviceLabel, String deviceName, List<String> colorList){
	
		Map<String, Object> params = new HashMap<>();
		String query;
		Result results = null;
		
		
		// 1. Find the number of colors - including colorless 
		params.clear();
		params.put("dName", deviceName);
		

		query = "MATCH (n) WHERE n.portName CONTAINS $dName RETURN DISTINCT COUNT(DISTINCT(n.color)) as count";
		results = graphDb.execute(query, params);
		
		Long numColors = 0l; // This includes "None" which is Colorless
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numColors = (Long) row.get("count");
		}
		

		// 2. Connect the colorless inputs to all outputs
		params.clear();
		params.put("InpType", "in");
		params.put("OutType", "out");
		params.put("InpColor", "None");
		params.put("dName", deviceName);
		
		query = "MATCH (n1) WHERE n1.portName CONTAINS $dName AND  n1.portName CONTAINS $InpType AND n1.color CONTAINS $InpColor "
				+ "WITH COLLECT(n1) AS nodesInp "
				+ "MATCH (n2) WHERE n2.portName CONTAINS $dName AND n2.portName CONTAINS $OutType "
				+ "WITH nodesInp, COLLECT(n2) AS nodesOut "
				+ "FOREACH (ni in nodesInp | "
				+ "FOREACH (no in nodesOut | "
				+ " CREATE (ni)-[r:COLORLESS ]->(no) "
				+ "))"; // The relation name can be changed
		
		results = graphDb.execute(query, params);
		
	
		// 3. Connect the colored inputs to colorless and their respective colored ones
		int colorInd = 1;
		for(String color: colorList){ 
			params.clear();
			params.put("InpType", "in");
			params.put("OutType", "out");
			
			List<String> codes = new ArrayList<String>();
			// Both colorless and colored 
			codes.add("None"); 
			codes.add(color);

			params.put("InpColor", color);
			params.put("OutColor", codes);
			params.put("dName", deviceName);
			
			query = "MATCH (n1) WHERE n1.portName CONTAINS $dName AND n1.portName CONTAINS $InpType AND  n1.color CONTAINS $InpColor "
					+ "WITH COLLECT(n1) AS nodesInp "
					+ "MATCH (n2) WHERE n2.portName CONTAINS $dName AND  n2.portName CONTAINS $OutType AND n2.color IN $OutColor "
					+ "WITH nodesInp, COLLECT(n2) AS nodesOut "
					+ "FOREACH (ni in nodesInp | "
					+ "FOREACH (no in nodesOut | "
					+ "CREATE (ni)-[r:c" + Integer.toString(colorInd)  +  "]->(no) "
					+ "))";
			results = graphDb.execute(query, params);
			colorInd += 1;

		} 
		
		// These are the relation object properties.
	
		// Get all the relationships created between input and output ports
		params.clear();
		query = "MATCH (n1)-[r]->(n2) RETURN n1.portName as inpNode, n2.portName as outNode";
		results = graphDb.execute(query, params);
		
		List<Pair<String, String>> edgePairsList = new ArrayList<Pair<String, String>>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			edgePairsList.add(new Pair<String, String>( (String)row.get("inpNode"),  (String)row.get("outNode")));
		}
		
		// Set the relation properties between the edge pairs
		int used;
		List<String> flowList = new ArrayList<String>();
		
		for(Pair<String, String> e: edgePairsList){
			String nodeFrom = e.getKey();
			String nodeTo = e.getValue();
			
			used = 0; // initially every link is unused
			flowList = new ArrayList<String>();
			flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
		
			RelationProperties.setRelationProperties(graphDb, nodeFrom, nodeTo, used, flowList);
		}
			
	}

	@Deprecated
	/**
	 * @param graphDb
	 * @param deviceLabel - label of the device
	 * @param deviceName - name of the device like "X1" or "X2" whose input-output links need to be created. This identifies the OXC.
	 */
	public static void createAutoRelationshipsIntraOXC(GraphDatabaseService graphDb, Label devicetLabel, String deviceName){
	
		// 1. Find the input ports
		Map<String, Object> params = new HashMap<>();
		String query;
		Result results;
		
		// 1. Find the input ports
		params.clear();
		params.put("type", "in");
		params.put("dName", deviceName);
		query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
		results = graphDb.execute(query, params);
				
		Long numInpPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numInpPorts = (Long) row.get("count");
		}
		 
		// **** Create the relationships - Run iterations connecting input and output ports
		
		int numIntInpPorts = toIntExact(numInpPorts); // this is done since the count return clause in neo4j returns long integers by default
		

		// Link the input port to output port for all the relationships
		String rQuery;
		int used;
		
		List<String> flowList = new ArrayList<String>();
		final StringBuilder relationQuery = new StringBuilder();
		
		String relName;
		
		for(int i=1; i<=numIntInpPorts; i++) {
			for(int j=1; j<=numIntInpPorts; j++){
				
				relName = deviceName + "." + "in" + Integer.toString(i) + "." +  "out" + Integer.toString(j);
				
				params.clear();
				params.put("portIn", deviceName + ".in" + Integer.toString(i));
				params.put("portOut", deviceName + ".out" + Integer.toString(j));
				params.put("rName", relName); // <deviceName>.<inPortId>.<outPortId>
				
				if (i == j){ // TO_LINE RELATION
					relationQuery.setLength(0);
					relationQuery.append("MATCH (xi: OXC) WHERE xi.portName = $portIn ");
					relationQuery.append("MATCH (xo: OXC) WHERE xo.portName = $portOut ");
					relationQuery.append("CREATE (xi)-[r:LINK ]->(xo) ");
					relationQuery.append("SET r.name = $rName");
				}
				else{
					relationQuery.setLength(0);
					relationQuery.append("MATCH (xi: OXC) WHERE xi.portName = $portIn ");
					relationQuery.append("MATCH (xo: OXC) WHERE xo.portName = $portOut ");
					relationQuery.append("CREATE (xi)-[r:LINK ]->(xo) ");
					relationQuery.append("SET r.name = $rName");
				}
				rQuery = relationQuery.toString();
				graphDb.execute(rQuery, params);
				
				
				// These are the relation object properties.
				used = 0; // initially every link is unused
				flowList = new ArrayList<String>();
				flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
				
				RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
			}
		}
	}
	/**
	 * @param graphDb - the graph database
	 * @param deviceLabel - adaptor name (can be change to optional)
	 * @param deviceName - adaptor ID like A1/A2.
	 * @param deviceType - MUX/DEMUX/ADD/DROP
	 * 
	 * ************RELATIONS USED - MUX, DEMUX, TO_LINE (1-1) - CHECK**********
	 */
	public static void createAutoRelationshipsIntraAdapters(GraphDatabaseService graphDb, Label deviceLabel, String deviceName, String deviceType){
	
		// 1. Find the input ports - can be digital or optical
		Map<String, Object> params = new HashMap<>();
		String query;
		Result results;
		int numDPortBundle=1;
		// The following 2 queries can also be used to determine the device type of the adapters (input > output ports)
		// 1. Find the number of input ports
		
	 	params.clear();
		params.put("type", "in");
		params.put("dName", deviceName);
		query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName CONTAINS $dName RETURN count(n) as count"  ; // Returns count
		results = graphDb.execute(query, params);
		
		Long numInpPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numInpPorts = (Long) row.get("count");
		}
		
		// 1. Find the number of output ports
	 	params.clear();
		params.put("type", "out");
		params.put("dName", deviceName);
		query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName CONTAINS $dName RETURN count(n) as count"  ; // Returns count 
		results = graphDb.execute(query, params);
		
		Long numOutPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numOutPorts = (Long) row.get("count");
		}
		
		if ( deviceType == "MUX" || deviceType == "ADD" ){ // ( deviceType == "MUX" || deviceType == "ADD")  && 
			/*
			 * Multiplexer - numDPorts to 1 - E->O
			 * 
			 */
			/*
			 * QUERY: MATCH (dp:dPort) WHERE dp.name IN [x IN range(i, j) | "in" + x]  WITH COLLECT(dp) as dpList
			 *        MATCH (op:oPort) WHERE op.name = "out" + k WITH op
			 *        FOREACH( n1 in dpList | CREATE n1-[:MUX]->op) 
			 * The JAVA method executes this query numOutPorts times - since it is a MUX
			 */
			
			try{
				numDPortBundle = toIntExact(numInpPorts/numOutPorts);
			}catch(Exception e){
				System.out.println("Number of output ports cannot be 0");
			}
			

			// Link the input port to output port for all the relationships
			String rQuery;
			int used;
			List<String> flowList = new ArrayList<String>();
			final StringBuilder relationQuery = new StringBuilder();
			int start=1;
			
			String relName;
			
			for(int i=1; i<=numOutPorts; i++){
				for(int j=start; j<start+numDPortBundle;j++ ){
					/*
					 * TODO: The following can be replaced by running FOREACH clause and avoiding creating one link at a time in the loop
					 */
					relName = deviceName + "." + "in" + Integer.toString(i) + "." +  "out" + Integer.toString(j);
							
					params.clear();
					params.put("portOut", deviceName + ".out" + Integer.toString(i));
					params.put("portIn", deviceName + ".in" + Integer.toString(j));
					params.put("rName", relName); // <deviceName>.<inPortId>.<outPortId>
					
//					System.out.println(deviceName + ".in" + Integer.toString(j) + " to " + deviceName + ".out" + Integer.toString(i));
					
					relationQuery.setLength(0);
					relationQuery.append("MATCH (dp) WHERE dp.portName = $portIn ");
					relationQuery.append("MATCH (op) WHERE op.portName = $portOut ");
					relationQuery.append("CREATE (dp)-[r:" + deviceType +  "]->(op) ");
					relationQuery.append("SET r.name = $rName");
					
					rQuery = relationQuery.toString();
					graphDb.execute(rQuery, params);
					
					// These are the relation object properties.
					used = 0; // initially every link is unused
					flowList.clear();
					flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
					
					RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
					
				}
				start += numDPortBundle;
			}	
		}
		else if ( deviceType == "DEMUX" || deviceType == "DROP" ){
			/*
			 * DeMultiplexer - 1 to numDPorts - O->E
			 * 
			 */
			/*
			 * QUERY: MATCH (dp:dPort) WHERE dp.name IN [x IN range(i, j) | "out" + x]  WITH dp
			 *        MATCH (op:oPort) WHERE op.name = "in" + k WITH op
			 *        FOREACH( n1 in dp | CREATE op-[:DEMUX]->n1) 
			 * The JAVA method executes this query numOutPorts times - since it is a MUX
			 */

			try{
				numDPortBundle = toIntExact(numOutPorts/numInpPorts);
			}catch(Exception e){
				System.out.println("Number of output ports cannot be 0");
			}
			
			String rQuery;
			int used;			
			List<String> flowList = new ArrayList<String>();
			final StringBuilder relationQuery = new StringBuilder();
			int start=1;
			
			String relName;
			for(int i=1; i<=numInpPorts; i++){
				for(int j=start; j<start+numDPortBundle;j++ ){
					/*
					 * TODO: The following can be replaced by running FOREACH clause and avoiding creating one link at a time in the loop
					 */
					
					relName = deviceName + "." + "in" + Integer.toString(i) + "." +  "out" + Integer.toString(j); // <deviceName>.<inPortId>.<outPortId>
							
					params.clear();
					params.put("portOut", deviceName + ".out" + Integer.toString(j));
					params.put("portIn", deviceName + ".in" + Integer.toString(i));
					params.put("rName", relName); 
					
					relationQuery.setLength(0);
					relationQuery.append("MATCH (dp) WHERE dp.portName = $portOut ");
					relationQuery.append("MATCH (op) WHERE op.portName = $portIn ");
					relationQuery.append("CREATE (op)-[r:" + deviceType +  "]->(dp) ");
					relationQuery.append("SET r.name = $rName");

					
					rQuery = relationQuery.toString();
					graphDb.execute(rQuery, params);
					
					used = 0; // initially every link is unused
					flowList.clear();
					flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
					
					RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
				}
				start += numDPortBundle;
			}
		}
		else{
			/*
			 * Line - 1 to 1 adapters - these can also be used as parallel links n to n adapters
			 * 
			 */
			/*
			 * QUERY: MATCH (dp:dPort) WHERE dp.name = "out" + k  WITH dp
			 *        MATCH (op:oPort) WHERE op.name = "in" + k WITH op
			 *        CREATE op-[:TO_LINE]->dp | CREATE dp-[:TO_LINE]->op 
			 * The JAVA method executes this query numInpPorts times 
			 */
			// Link the input port to output port for all the relationships
			String rQuery;
			int used;
			List<String> flowList = new ArrayList<String>();
			final StringBuilder relationQuery = new StringBuilder();
			
			String relName;
			for(int i=1; i<=numInpPorts; i++){
				
				relName = deviceName + "." + "in" + Integer.toString(i) + "." +  "out" + Integer.toString(i);
						
				params.clear();
				params.put("portOut", deviceName + ".out" + Integer.toString(i));
				params.put("portIn", deviceName + ".in" + Integer.toString(i));
				params.put("rName", relName ); // <deviceName>.<inPortId>.<outPortId>
			
				relationQuery.setLength(0);
				// RELATION DIG->OPT
				if (deviceType == "MUX"){
					System.out.println("Created Adapter relations: Digital to Optical ");
					
					relationQuery.append("MATCH (dp) WHERE dp.portName = $portIn ");
					relationQuery.append("MATCH (op) WHERE op.portName = $portOut ");
					relationQuery.append("CREATE (dp)-[r:LINK ]->(op) ");
					relationQuery.append("SET r.name = $rName");
				}
				// RELATION OPT->DIG
				else{
					System.out.println("Created Adapter relations: Optical to Digital ");
					relationQuery.append("MATCH (dp) WHERE dp.portName = $portOut ");
					relationQuery.append("MATCH (op) WHERE op.portName = $portIn ");
					relationQuery.append("CREATE (op)-[r:LINK ]->(dp) ");
					relationQuery.append("SET r.name = $rName");
				}
				
				rQuery = relationQuery.toString();
				graphDb.execute(rQuery, params);
				
				// These are the relation object properties.
				used = 0; // initially every link is unused
				flowList.clear();
				flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
				
				RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
			}
		}
	}
	
	// -------------------------------------- INTER-DEVICE RELATIONSHIPS ---------------------------------------------------
	// TODO: Take an argument for denoting fromHost / toHost Link
	/**
	 * @param graphDb
	 * @param NodeLabel 
	 * @param hostName - the name of host device - H1 / H2/...
	 * @param switchName - the name of the switch node like "S1" or "S2"
	 * @param switchPortNumber - the connections (line/client) vertex number in a device, for example if S1.in1 connected to Host, then 1 is the PortNumber

	 */
	public static void createAutoRelationshipsHost2Switch(GraphDatabaseService graphDb, String hostName,  String switchName, int switchPortNumber){
		
		Map<String, Object> params = new HashMap<>();
		
		int used;
		
		List<String> flowList = new ArrayList<String>();
		String rQuery;
		final StringBuilder relationQuery = new StringBuilder();
		
		// String deviceName = hostName + "&" + switchName; // H1&S2
		String relNameH2S = hostName + "." + switchName + ".in" + Integer.toString(switchPortNumber);
		String relNameS2H =  switchName + ".in" + Integer.toString(switchPortNumber) + "." + hostName;

		// Currently only one port of host is connected to one input and one output port of a switch - This can be extended based on parameters - Looping
		params.put("hName", hostName);
		params.put("SInp", switchName + ".in" + Integer.toString(switchPortNumber));
		params.put("SOut", switchName + ".out" + Integer.toString(switchPortNumber));
		params.put("rNameH2S", relNameH2S);
		params.put("rNameS2H", relNameS2H);
		
		relationQuery.append("MATCH (h) WHERE h.name = $hName ");
		relationQuery.append("MATCH (si) WHERE si.portName = $SInp ");
		relationQuery.append("MATCH (so) WHERE so.portName = $SOut ");
		relationQuery.append("CREATE (h)-[ri]->(si) ");
		relationQuery.append("CREATE (so)-[re]->(h) ");
		relationQuery.append("SET ri.name = $rNameH2S ");
		relationQuery.append("SET re.name = $rNameS2H");

		rQuery = relationQuery.toString();
		graphDb.execute(rQuery, params);
		
		/*
		 * 1. H2S Link
		 * 2. S2H Link
		 */
		
		// These are the relation object properties.
		used = 0; // initially every link is unused
		flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
		
		RelationProperties.setRelationProperties(graphDb, relNameH2S, used, flowList);
		RelationProperties.setRelationProperties(graphDb, relNameS2H, used, flowList);
		
	}
	/**
	* This is the connection between switches
	* 
	* @param graphDb
	* @param switchFromName - the name of the switch like "S1" or "S2"
	* @param switchFromPortStart - the start port of the switch from which connections  would be made 
	* @param switchFromPortEnd - the end port of the switch from which connections  would be made 
	* @param switchToName - the name of the switch like "S1" or "S2"
	* @param switchToPortStart - the start port of the switch to which connections would be made 
	* @param switchToPortEnd - the end port of the switch to which connections would be made
	* 
	* TODO: The start and end ports of the switch should be selected based on the first AVAILABLE PORT
	*/
	
	public static void createAutoRelationshipsSwitchAndSwitch(GraphDatabaseService graphDb, String switchFromName, int switchFromPortStart, int switchFromPortEnd,
			   String switchToName, int switchToPortStart, int switchToPortEnd ){
	
		if ((switchToPortEnd - switchToPortStart) != (switchFromPortEnd - switchFromPortStart)){
			throw new java.lang.Error("The number of switch ports to be connected to the other switch should be equal.");
		}
		
		Map<String, Object> params = new HashMap<>();
		
		// Link the input port to output port for all the relationships
		String rQuery;		
		int used;
		
		List<String> flowList = new ArrayList<String>();
		final StringBuilder relationQuery = new StringBuilder();
		
		String relName; // relationship Name
		for(int i=switchFromPortStart, j=switchToPortStart; i<=switchFromPortEnd; i++, j++) {
		
			params.clear();
			params.put("portSwitchFrom", switchFromName + ".out" + Integer.toString(i));
			params.put("portSwitchTo", switchToName + ".in" +  Integer.toString(j));
			
			relationQuery.setLength(0);
			relationQuery.append("MATCH (s1) WHERE s1.portName = $portSwitchFrom ");
			relationQuery.append("MATCH (s2) WHERE s2.portName = $portSwitchTo ");
			
			relationQuery.append("CREATE (s1)-[r:LINK ]->(s2) "); 
			// Relation Name
			relName = switchFromName + "." + "in" + Integer.toString(i) + "." +  switchToName + "." + "out" + Integer.toString(j); // <deviceNameFrom>.<inPortId>.<deviceNameTo>.<outPortId>
			params.put("rName", relName); 
			relationQuery.append("SET r.name = $rName"); 
			
			
			rQuery = relationQuery.toString();
			graphDb.execute(rQuery, params);
			
			
			// These are the relation object properties.
			used = 0; // initially every link is unused
			flowList = new ArrayList<String>();
			flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
			
			RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
						
		}
	}
	
	/**
	 * This is the connection from/to switch to/from MUX adapters OR n-n LINKS
	 * 
	 * @param graphDb
	 * @param switchName - the name of the switch like "S1" or "S2"
	 * @param switchPortStart - the start port of the switch from/to which connections to/from adapters would be made 
	 * @param switchPortEnd - the end port of the switch from/to which connections to/from adapters would be made 
	 * @param adapterName - the name of the adapter like "A1" or "A2"
	 * @param adapterPortStart - the start port of the adapter from/to which connections to/from switch would be made 
	 * @param adapterPortEnd - the end port of the adapter from/to which connections to/from switch would be made
	 * @param fromSwitch - this boolean flag indicates whether the connections are FROM switch TO ADAPTER (1) or the Reverse way (0).
	 * 
	 * TODO: The start and end ports of the switch should be selected based on the first AVAILABLE PORT and NUMBER OF PORTS required
	 */
	public static void createAutoRelationshipsSwitchAndAdapter(GraphDatabaseService graphDb, String switchName, int switchPortStart, int switchPortEnd,
															   String adapterName, int adapterPortStart, int adapterPortEnd, boolean fromSwitch){
		
		
		if ((adapterPortEnd - adapterPortStart) != (switchPortEnd - switchPortStart)){
			throw new java.lang.Error("The number of switch ports to be connected to the adapter should be equal to the number of adaptor ports.");
		}
		
		Map<String, Object> params = new HashMap<>();
	
		// Link the input port to output port for all the relationships
		String rQuery, switchPortType, adapterPortType;		
		int used;
		List<String> flowList = new ArrayList<String>();
		final StringBuilder relationQuery = new StringBuilder();
		String deviceNameFrom, deviceNameTo;
		
		// If the direction of connection is from switch to adapter, then the connections leave the output of switches
		if (fromSwitch == true){
			switchPortType = "out";
			adapterPortType = "in";
			deviceNameFrom = switchName;
			deviceNameTo = adapterName;
		}
		else{
			switchPortType = "in";
			adapterPortType = "out";
			deviceNameFrom = adapterName;
			deviceNameTo = switchName;
		}
		System.out.println("Creating Switch/WSS and Adapter relations...");
		String relName; // relationship Name
		for(int i=switchPortStart, j=adapterPortStart; i<=switchPortEnd; i++, j++) {
			
			params.clear();
			params.put("portSwitch", switchName + "." + switchPortType + Integer.toString(i));
			params.put("portAdapter", adapterName + "." + adapterPortType + Integer.toString(j));

			relationQuery.setLength(0);
			relationQuery.append("MATCH (s) WHERE s.portName = $portSwitch ");
			relationQuery.append("MATCH (a) WHERE a.portName = $portAdapter ");
			
			
			if (fromSwitch == true){
				relationQuery.append("CREATE (s)-[r:LINK ]->(a) "); 
				// Relation Name
				relName = deviceNameFrom + "." + "out" + Integer.toString(i) + "." +  deviceNameTo + "." + "in" + Integer.toString(j); // <deviceNameFrom>.<inPortId>.<deviceNameTo>.<outPortId>
				params.put("rName", relName); 
				relationQuery.append("SET r.name = $rName"); 
				
				System.out.println(deviceNameFrom + "." + "out" + Integer.toString(i) + "   to   " +  deviceNameTo + "." + "in" + Integer.toString(j));

			}
			else{
				relationQuery.append("CREATE (a)-[r:LINK ]->(s)");  
				//Relation Name
				relName =  deviceNameFrom + "." + "out" + Integer.toString(j) + "." +  deviceNameTo + "." + "in" + Integer.toString(i); // <deviceNameFrom>.<inPortId>.<deviceNameTo>.<outPortId>
				params.put("rName", relName); 
				relationQuery.append("SET r.name = $rName"); 
				
				System.out.println(deviceNameFrom + "." + "out" + Integer.toString(j) + "   to   " +  deviceNameTo + "." + "in" + Integer.toString(i));

			}
			
			rQuery = relationQuery.toString();
			graphDb.execute(rQuery, params);
			
			
			// These are the relation object properties.
			used = 0; // initially every link is unused
			flowList = new ArrayList<String>();
			flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
			
			RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
		}
	}
	
	/**
	* This is the connection from/to WSS to/from DWDM
	* 
	* @param graphDb
	* @param dwdmName - the name of the adapter like "W1" or "W2"
	* @param fromWSS - this boolean flag indicates whether the connections are FROM WSS TO DWDM (1) or the Reverse way (0).
	* @param connPairs - the list of connection pairs - this parameter will be deprecated in future and substituted by an algorithm.
	* 
	*/
	public static void createAutoRelationshipsWSSAndDWDM(GraphDatabaseService graphDb, String wssName, String dwdmName,  boolean fromWSS, List<Pair<String, String>> connPairs){
	
		Map<String, Object> params = new HashMap<>();
		Result results;
		
		
		// Link the input port to output port for all the relationships
		String rQuery;		
		int used;
		
		List<String> flowList = new ArrayList<String>();
		final StringBuilder relationQuery = new StringBuilder();
		
		/*
		 * This is a simple algorithm to connect compatible ports between the WSS and the DWDM - CD ROADM
		 * Can be changed by an actual algorithm that is used  for other RAODMS
		 */
		// Find number of uncolored ports of WSS
		params.clear();
		if(fromWSS == true)
			params.put("type", "out"); 
		else
			params.put("type", "in");
		
		params.put("dName", wssName);
		params.put("color", "None");
		
		rQuery = "MATCH (n) WHERE n.color CONTAINS $color MATCH (n) WHERE n.portName CONTAINS $dName MATCH (n) WHERE n.portName CONTAINS $type RETURN count(n) as count"  ; 
		results = graphDb.execute(rQuery, params);
				
		Long numWSSUnColorPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numWSSUnColorPorts = (Long) row.get("count");
		}
		
		// Find the number of colored ports of WSS
		params.clear();
		if(fromWSS == true)
			params.put("type", "out"); 
		else
			params.put("type", "in");
		
		params.put("dName", wssName);
		params.put("color", "None");
		
		rQuery = "MATCH (n) WHERE n.color <> $color MATCH (n) WHERE n.portName CONTAINS $dName MATCH (n) WHERE n.portName CONTAINS $type RETURN count(n) as count"  ; 
		results = graphDb.execute(rQuery, params);
				
		Long numWSSColorPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numWSSColorPorts = (Long) row.get("count");
		}
		
		
		//Find the number of colored ports of DWDM
		if(fromWSS == true)
			params.put("type", "in"); 
		else
			params.put("type", "out");
		params.put("dName", dwdmName);
		params.put("color", "None");
		rQuery = "MATCH (n) WHERE n.color <> $color MATCH (n) WHERE n.portName CONTAINS $dName MATCH (n) WHERE n.portName CONTAINS $type RETURN count(n) as count"  ; 
		results = graphDb.execute(rQuery, params);
				
		Long numdwdmPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numdwdmPorts = (Long) row.get("count");
		}
		
		// Assertion check to match the ports by count 
		if (numdwdmPorts != numWSSColorPorts){
			System.out.println("\nNo. of colors in DWDM: " + numdwdmPorts + ", No. of colors in WSS: " + numWSSColorPorts);
			throw new java.lang.Error("Device mismatch: Number of colored ports do not match. ");
		}
		
		// Find the number of directions of DWDM
		if(fromWSS == true)
			params.put("type", "out"); 
		else
			params.put("type", "in");	
		
		params.put("dName", dwdmName);
		rQuery = "MATCH (n) WHERE n.portName CONTAINS $dName MATCH (n) WHERE n.portName CONTAINS $type RETURN count(n) as count"  ; 
		results = graphDb.execute(rQuery, params);
				
		Long numDir = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numDir = (Long) row.get("count");
		}
		
		int numColors = toIntExact(numdwdmPorts/numDir);
		
		System.out.println("\nCreating WSS and DWDM connections");
		String relName;
		int ci = 1; // current color index for the loop 
		
		/*
		 * 1. Loop the colors 
		 * 2. Connect the corresponding colored ports between the WSS and the Banks.
		 */
		int i=toIntExact(numWSSUnColorPorts)+1, j=1;
		while(ci <= numColors){
			// i - WSS port index, j - Line Bank port index
			j=ci;
			int k = 1;
			System.out.println("Num colors: " + numColors);
			while(k <= numDir ){
//				System.out.println(i + " --- " + j);
				if (fromWSS == true){
					params.clear();
					params.put("portWSS", wssName + "." + "out"+ Integer.toString(i));
					params.put("portDWDM", dwdmName + "." + "in" + Integer.toString(j));
					
					System.out.println(wssName + "." + "out" + Integer.toString(i) + "  to   "  + dwdmName + "." + "in"  + Integer.toString(j));
	
					relationQuery.setLength(0);
					relationQuery.append("MATCH (w) WHERE w.portName = $portWSS ");
					relationQuery.append("MATCH (d) WHERE d.portName = $portDWDM ");
					
					relationQuery.append("CREATE (w)-[r:LINK ]->(d) "); 
					// Relation Name
					relName = wssName + "." + "out" + Integer.toString(i) + "." +  dwdmName + "." + "in" +  Integer.toString(j); // <deviceNameFrom>.<inPortId>.<deviceNameTo>.<outPortId>
					params.put("rName", relName); 
					relationQuery.append("SET r.name = $rName"); 
				}
				else{
					params.clear();
					params.put("portWSS", wssName + "." + "in" + Integer.toString(i));
					params.put("portDWDM", dwdmName + "." + "out" + Integer.toString(j));
					
					relationQuery.setLength(0);
					relationQuery.append("MATCH (w) WHERE w.portName = $portWSS ");
					relationQuery.append("MATCH (d) WHERE d.portName = $portDWDM ");
					
					System.out.println(dwdmName + "." + "in"   + Integer.toString(j) + "  to  " + wssName + "." + "out" + Integer.toString(i)  );
	
					
					relationQuery.append("CREATE (d)-[r:LINK ]->(w) "); 
					// Relation Name
					relName = dwdmName + "." +  "out" + Integer.toString(j) + "." +  wssName + "."+ "in"  + Integer.toString(i); // <deviceNameFrom>.<inPortId>.<deviceNameTo>.<outPortId>
					
					params.put("rName", relName); 
					relationQuery.append("SET r.name = $rName"); 
				}
				
//				System.out.println("\n relationshp name  : " + relName);

				rQuery = relationQuery.toString();
				graphDb.execute(rQuery, params);
				
				// These are the relation object properties.
				used = 0; // initially every link is unused
				flowList = new ArrayList<String>();
				flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
				
				RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
				
				i++;
				j = j + numColors;
				k++;
			}		
			ci += 1;
		}
		
	}
	
	/**
	* This is the connection between Optical Devices / Optical Cross Connects
	* 
	* @param graphDb
	* @param oxcFromName - the name of the oxc like "O1"
	* @param oxcFromPortStart - the start port of the oxc from which connections  would be made 
	* @param oxcFromPortEnd - the end port of the oxc from which connections  would be made 
	* @param oxcToName - the name of the oxc like "O1" or "O2"
	* @param oxcToPortStart - the start port of the oxc to which connections would be made 
	* @param oxcToPortEnd - the end port of the oxc to which connections would be made
	* 
	* TODO: The start and end ports of the oxc should be selected based on the first AVAILABLE PORT
	*/
	public static void createAutoRelationshipsOXCAndOXC(GraphDatabaseService graphDb, String oxcFromName, int oxcFromPortStart, int oxcFromPortEnd,
			                                            String oxcToName, int oxcToPortStart, int oxcToPortEnd ){

		
		if ((oxcToPortEnd - oxcToPortStart) != (oxcFromPortEnd - oxcFromPortStart)){
			throw new java.lang.Error("The number of oxc ports to be connected to the other oxc should be equal.");
		}
		
		Map<String, Object> params = new HashMap<>();
		
		// Link the input port to output port for all the relationships
		String rQuery;		
		int used;		
		List<String> flowList = new ArrayList<String>();
		final StringBuilder relationQuery = new StringBuilder();
		
		String relName; // relationship Name
		for(int i=oxcFromPortStart, j=oxcToPortStart; i<=oxcFromPortEnd; i++, j++) {
		
			params.clear();
			params.put("portOXCFrom", oxcFromName + ".out" + Integer.toString(i));
			params.put("portOXCTo", oxcToName + ".in" +  Integer.toString(j));
			
			relationQuery.setLength(0);
			relationQuery.append("MATCH (o1) WHERE o1.portName = $portOXCFrom ");
			relationQuery.append("MATCH (o2) WHERE o2.portName = $portOXCTo ");
			
			relationQuery.append("CREATE (o1)-[r:LINK ]->(o2) "); 
			// Relation Name
			relName = oxcFromName + "." + "in" + Integer.toString(i) + "." +  oxcToName + "." + "out" + Integer.toString(j); // <deviceNameFrom>.<inPortId>.<deviceNameTo>.<outPortId>
			params.put("rName", relName); 
			relationQuery.append("SET r.name = $rName"); 
			
			
			rQuery = relationQuery.toString();
			graphDb.execute(rQuery, params);
			
			
			// These are the relation object properties.
			used = 0; // initially every link is unused
			flowList = new ArrayList<String>();
			flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
			
			RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
		}
	}

	@Deprecated
	/**
	 * This is the connection from/to switch to/from MUX adapters OR n-n LINKS
	 * 
	 * @param graphDb
	 * @param oxcName - the name of the OXC like "X1" or "X2"
	 * @param oxcPortStart - the start port of the OXC from/to which connections to/from adapters would be made 
	 * @param oxcPortEnd - the end port of the OXC from/to which connections to/from adapters would be made 
	 * @param adapterName - the name of the adapter like "A1" or "A2"
	 * @param adapterPortStart - the start port of the adapter from/to which connections to/from OXC would be made 
	 * @param adapterPortEnd - the end port of the adapter from/to which connections to/from OXC would be made
	 * @param fromSwitch - this boolean flag indicates whether the connections are FROM OXC TO ADAPTER (1) or the Reverse way (0).
	 * 
	 * TODO: The start and end ports of the OXC should be selected based on the first AVAILABLE PORT
	 */
	public static void createAutoRelationshipsOXCAndAdapter(GraphDatabaseService graphDb, String oxcName, int oxcPortStart, int oxcPortEnd,
			   String adapterName, int adapterPortStart, int adapterPortEnd, boolean fromOXC){
	
		
		if ((adapterPortEnd - adapterPortStart) != (oxcPortEnd - oxcPortStart)){
			throw new java.lang.Error("The number of OXC ports to be connected to the adapter should be equal to the number of adaptor ports.");
		}
		
		Map<String, Object> params = new HashMap<>();
		
		// Link the input port to output port for all the relationships
		String rQuery, oxcPortType, adapterPortType;		
		int used;
		
		List<String> flowList = new ArrayList<String>();
		final StringBuilder relationQuery = new StringBuilder();
		String deviceNameFrom, deviceNameTo;
		
		// If the direction of connection is from switch to adapter, then the connections leave the output of switches
		if (fromOXC == true){
			oxcPortType = "out"; 
			adapterPortType = "in";
			deviceNameFrom = oxcName;
			deviceNameTo = adapterName;
		}
		else{
			oxcPortType = "in";
			adapterPortType = "out"; // This is also the optical side
			deviceNameTo = oxcName;
			deviceNameFrom = adapterName;
		}
		
		String relName; // relationship Name
		for(int i=oxcPortStart, j=adapterPortStart; i<=oxcPortEnd; i++, j++) {
			
			params.clear();
			params.put("portOXC", oxcName + "." + oxcPortType + Integer.toString(i));
			params.put("portAdapter", adapterName + "." + adapterPortType + Integer.toString(j));
			
			relationQuery.setLength(0);
			relationQuery.append("MATCH (x: OXC) WHERE x.portName = $portOXC ");
			relationQuery.append("MATCH (a: Adapter) WHERE a.portName = $portAdapter ");
			
			if (fromOXC == true){
				relationQuery.append("CREATE (x)-[:LINK ]->(a)");  
				relName = deviceNameFrom + "." + "in" + Integer.toString(i) + "." +  deviceNameTo + "." + "out" + Integer.toString(j); // <deviceNameFrom>.<inPortId>.<deviceNameTo>.<outPortId>
				params.put("rName", relName); 
				relationQuery.append("SET r.name = $rName"); 
			}
			else{
				relationQuery.append("CREATE (a)-[:LINK ]->(x)");  
				//Relation Name
				relName =  deviceNameFrom + "." + "in" + Integer.toString(j) + "." +  deviceNameTo + "." + "out" + Integer.toString(i); // <deviceNameFrom>.<inPortId>.<deviceNameTo>.<outPortId>
				params.put("rName", relName); 
				relationQuery.append("SET r.name = $rName"); 
			}
			
			rQuery = relationQuery.toString();
			graphDb.execute(rQuery, params);
			
			// These are the relation object properties.
			used = 0; // initially every link is unused
			flowList = new ArrayList<String>();
			flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
			
			RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
		}
	}
	
	/**
	 * This is the connection between a Switch and a ROADM
	 * 
	 * @param graphDb
	 * @param rdmName - the name of the ROADM like "RDM1" or "RDM2"
	 * @param rdmPortStart - the start port of the ROADM from/to which connections to/from switches would be made 
	 * @param rdmPortEnd - the end port of the ROADM from/to which connections to/from witches would be made 
	 * @param switchName - the name of the adapter like "A1" or "A2"
	 * @param switchPortStart - the start port of the switch
	 * @param switchPortEnd - the end port of the switch
	 * 
	 * TODO: The start and end ports of the Switch should be selected based on the first AVAILABLE PORT
	 */
	public static void createAutoRelationshipsRAODMAndSwitch(GraphDatabaseService graphDb, String rdmName, int rdmPortStart, int rdmPortEnd,
			   String switchName, int switchPortStart, int switchPortEnd){
	
		
		if ((switchPortEnd - switchPortStart) != (rdmPortEnd - rdmPortStart)){
			throw new java.lang.Error("The number of ROADM MUX/DEMUX ports to be connected to the switch does not match.");
		}
		
		Map<String, Object> params = new HashMap<>();
		
		// Link the input port to output port for all the relationships
		String rQuery, rdmPortType, switchPortType;		
		int used;
		
		List<String> flowList = new ArrayList<String>();
		final StringBuilder relationQuery = new StringBuilder();
		String deviceNameFrom, deviceNameTo;
		
		
		// First create connections FROM switch TO ROADM (MUX)
		rdmPortType = "in"; 
		switchPortType = "out";
		deviceNameTo = rdmName;
		deviceNameFrom = switchName;
		
		String relName; // relationship Name
		for(int i=rdmPortStart, j=switchPortStart; i<=rdmPortEnd; i++, j++) {
			
			params.clear();
			params.put("portRDM", rdmName + ".AM." + rdmPortType + Integer.toString(i));
			params.put("portSwitch", switchName + "." + switchPortType + Integer.toString(j));
			
			relationQuery.setLength(0);
			relationQuery.append("MATCH (rd) WHERE rd.portName = $portRDM ");
			relationQuery.append("MATCH (so) WHERE so.portName = $portSwitch ");
			
			relationQuery.append("CREATE (so)-[r:LINK ]->(rd)");  
			relName = deviceNameFrom + "." + "out" + Integer.toString(j) + "." +  deviceNameTo + ".AM." + "in" + Integer.toString(i); // <deviceNameFrom>.<inPortId>.<deviceNameTo>.<outPortId>
			
			System.out.println(deviceNameFrom + "." + "out" + Integer.toString(j) + "  to  " + deviceNameTo + ".AM." + "in" + Integer.toString(i) );

			params.put("rName", relName); 
			relationQuery.append("SET r.name = $rName"); 
			
			rQuery = relationQuery.toString();
			graphDb.execute(rQuery, params);
			
			// These are the relation object properties.
			used = 0; // initially every link is unused
			flowList = new ArrayList<String>();
			flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
			
			RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
		}
		
		// Create connections FROM ROADM (DEMUX) TO switch
		rdmPortType = "out"; 
		switchPortType = "in";
		deviceNameFrom = rdmName;
		deviceNameTo = switchName;
		
		for(int i=rdmPortStart, j=switchPortStart; i<=rdmPortEnd; i++, j++) {
			
			params.clear();
			params.put("portRDM", rdmName + ".DDM." + rdmPortType + Integer.toString(i));
			params.put("portSwitch", switchName + "." + switchPortType + Integer.toString(j));
			
			relationQuery.setLength(0);
			relationQuery.append("MATCH (rd) WHERE rd.portName = $portRDM ");
			relationQuery.append("MATCH (so) WHERE so.portName = $portSwitch ");
			
			relationQuery.append("CREATE (rd)-[r:LINK ]->(so)");  
			relName = deviceNameFrom + ".DDM." + "out" + Integer.toString(i) + "." +  deviceNameTo + "." + "in" + Integer.toString(j); // <deviceNameFrom>.<outPortId>.<deviceNameTo>.<inPortId>
			System.out.println(deviceNameFrom + ".DDM." + "out" + Integer.toString(i) + "  to  " + deviceNameTo + "." + "in" + Integer.toString(j) );

			params.put("rName", relName); 
			relationQuery.append("SET r.name = $rName"); 
			
			rQuery = relationQuery.toString();
			graphDb.execute(rQuery, params);
			
			// These are the relation object properties.
			used = 0; // initially every link is unused
			flowList = new ArrayList<String>();
			flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
			
			RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
		}
				
	}
	
	/**
	 * This is the connection between ROADM direction fiber ports
	 * 
	 * @param graphDb
	 * @param rdmPortPairs - input-output pairs between the ROADM devices
	 * 
	 */
	public static void createAutoRelationshipsRAODMAndROADM(GraphDatabaseService graphDb, List<Pair<String, String>> rdmPortPairs){
		
	
		Map<String, Object> params = new HashMap<>();		
		
		// Link the input port to output port for all the relationships
		String rQuery;		
		int used;
		
		List<String> flowList = new ArrayList<String>();
		final StringBuilder relationQuery = new StringBuilder();
		
		for(Pair<String, String> ioPair: rdmPortPairs){
			String relName; // relationship Name
	
			params.clear();
			params.put("portRDM1", ioPair.getKey());
			params.put("portRDM2", ioPair.getValue());
			
			relationQuery.setLength(0);
			relationQuery.append("MATCH (n1) WHERE n1.portName = $portRDM1 ");
			relationQuery.append("MATCH (n2) WHERE n2.portName = $portRDM2 ");
			
			relationQuery.append("CREATE (n1)-[r:LINK ]->(n2)");  
			relName = ioPair.getKey() + "." +  ioPair.getValue(); // <deviceNameFrom>.<inPortId>.<deviceNameTo>.<outPortId>
			
			System.out.println(ioPair.getKey() + "  to  " + ioPair.getValue() );

			params.put("rName", relName); 
			relationQuery.append("SET r.name = $rName"); 
			
			rQuery = relationQuery.toString();
			graphDb.execute(rQuery, params);
			
			// These are the relation object properties.
			used = 0; // initially every link is unused
			flowList = new ArrayList<String>();
			flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
			
			RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
		}
				
	}

	// --------------------------------------MANUAL INTER-DEVICE RELATIONSHIPS------------------------------------------------
	/**
	* This is the connection from/to switch to/from MUX adapters OR n-n LINKS 
	* Here the connection links are manually specified
	* 
	* @param graphDb
	* @param switchName - the name of the switch like "S1" or "S2"
	* @param switchPortStart - the start port of the switch from/to which connections to/from adapters would be made 
	* @param switchPortEnd - the end port of the switch from/to which connections to/from adapters would be made 
	* @param adapterName - the name of the adapter like "A1" or "A2"
	* @param adapterPortStart - the start port of the adapter from/to which connections to/from switch would be made 
	* @param adapterPortEnd - the end port of the adapter from/to which connections to/from switch would be made
	* @param fromSwitch - this boolean flag indicates whether the connections are FROM switch TO ADAPTER (1) or the Reverse way (0).
	* @param portConn - list of connection pairs between switch and the adapter ports - Pairs should be of the form <fromPort, toPort>
	* 
	* 		
	* */
	public static void createManualRelationshipsSwitchAndAdapter(GraphDatabaseService graphDb, String switchName, int switchPortStart, int switchPortEnd,
			   String adapterName, int adapterPortStart, int adapterPortEnd, boolean fromSwitch, List<Pair<String, String>> portConn){
	
	
		if ((adapterPortEnd - adapterPortStart) != (switchPortEnd - switchPortStart)){
			throw new java.lang.Error("The number of switch ports to be connected to the adapter should be equal to the number of adaptor ports.");
		}
	
		Map<String, Object> params = new HashMap<>();
		
		// Link the input port to output port for all the relationships
		String rQuery;		
		int used;
		
		List<String> flowList = new ArrayList<String>();
		final StringBuilder relationQuery = new StringBuilder();
		String deviceNameFrom, deviceNameTo;
		
		// If the direction of connection is from switch to adapter, then the connections leave the output of switches
		if (fromSwitch == true){
			deviceNameFrom = switchName;
			deviceNameTo = adapterName;
		}
		else{
			deviceNameFrom = adapterName;
			deviceNameTo = switchName;
		}
	
		String relName; // relationship Name
		for(Pair<String, String> pairConn: portConn){
			/*
			 * The connections pairs are of the form: <in1, out2> ORF <out1, in2> where 1 and 2 are exampe port numbers
			 */
			
			
			if (fromSwitch == true){
				params.clear();
				params.put("portSwitch", switchName + "." + pairConn.getKey());
				params.put("portAdapter", adapterName + "." + pairConn.getValue());
				
				relationQuery.setLength(0);
				relationQuery.append("MATCH (s: Switch) WHERE s.portName = $portSwitch ");
				relationQuery.append("MATCH (a: Adapter) WHERE a.portName = $portAdapter ");
				
				relationQuery.append("CREATE (s)-[r:LINK ]->(a) "); 
				// Relation Name
				relName = deviceNameFrom + "." + pairConn.getKey() + "." +  deviceNameTo + "." + pairConn.getValue(); // <deviceNameFrom>.<inPortId>.<deviceNameTo>.<outPortId>
				params.put("rName", relName); 
				relationQuery.append("SET r.name = $rName"); 
			}
			else{
				params.clear();
				params.put("portSwitch", switchName + "." + pairConn.getValue());
				params.put("portAdapter", adapterName + "." + pairConn.getKey());
				
				relationQuery.setLength(0);
				relationQuery.append("MATCH (s: Switch) WHERE s.portName = $portSwitch ");
				relationQuery.append("MATCH (a: Adapter) WHERE a.portName = $portAdapter ");
				
				relationQuery.append("CREATE (a)-[:LINK ]->(s)");  
				//Relation Name
				relName =  deviceNameFrom + "." + pairConn.getKey() + "." +  deviceNameTo + "." + pairConn.getValue(); // <deviceNameFrom>.<inPortId>.<deviceNameTo>.<outPortId>
				params.put("rName", relName); 
				relationQuery.append("SET r.name = $rName"); 
			}
			
			rQuery = relationQuery.toString();
			graphDb.execute(rQuery, params);
			
			
			// These are the relation object properties.
			used = 0; // initially every link is unused
			flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
			
			RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
		}
	}
	
	/**
	 * 
	 * @param graphDb - graph database instance
	 * @param src - source port
	 * @param dest - destination
	 */
	public static void createManualRelationships(GraphDatabaseService graphDb, String src, String dest){
		
		Map<String, Object> params = new HashMap<>();
		
		// Link the input port to output port for all the relationships
		String rQuery;		
		int used;
		
		List<String> flowList = new ArrayList<String>();
		final StringBuilder relationQuery = new StringBuilder();
		
		params.clear();
		params.put("src", src);
		params.put("dest", dest);
		
		relationQuery.setLength(0);
		relationQuery.append("MATCH (s) WHERE s.portName = $src ");
		relationQuery.append("MATCH (d) WHERE d.portName = $dest ");
		
		relationQuery.append("CREATE (s)-[r:LINK ]->(d) "); 
		// Relation Name
		String relName = src+ "." + dest;
		params.put("rName", relName); 
		relationQuery.append("SET r.name = $rName"); 
		
		rQuery = relationQuery.toString();
		graphDb.execute(rQuery, params);
		
		// These are the relation object properties.
		used = 0; // initially every link is unused
		flowList = new ArrayList<String>();
		flowList.add("N"); // Initially every link has no flows attached - this can be empty as well
		
		RelationProperties.setRelationProperties(graphDb, relName, used, flowList);
	}
}
