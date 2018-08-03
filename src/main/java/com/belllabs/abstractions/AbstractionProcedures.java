package main.java.com.belllabs.abstractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import javafx.util.Pair;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;

public class AbstractionProcedures {
	/**
	
	 */
	public static void copyPortsFromDevices(GraphDatabaseService graphDb, GraphDatabaseService graphDbAbstract,  List<String> portList, Label absDeviceLabel ){
		/**
		 * 
		 * This method copies list of ports (either subset of input/output) from the detailed view to the new abstract view.
		 * 
		 * @param graphDb - graph database to read from 
		 * @param graphDbAbstract - graph database to write to
		 * @param portList - the port list to copy to the new database
		 * @param absDeviceLabel - abstract device label name
		 */
		
		// These two ADTs would be sent to the createAbstractDevice class methods to create a new device on
		// the abstract database
		List<Pair<String, Map<String, Object>>> portsPropList = new ArrayList<Pair<String, Map<String, Object>>>();

		
		String query;
		Map<String, Object> params = new HashMap<>();
//		List<String> inpPortNames = new ArrayList<>(), outPortNames = new ArrayList<>();
		

		params.put("portNames", portList);
		
		// Get the node properties from the detailed level graph database 
		// Store them in a data structure
		query = "WITH $portNames AS portNamesList "
				+ "UNWIND portNamesList as pName "
				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
				+ "RETURN n as port, n.portName as pNameID, labels(n) as l "; // Can only return node n
		
		/*
		 * The above query can also be replaced by 
		 * with $inpPortNames as list
			match (n)
			where any (item in list where n.portName contains item)
			return n
		 */
		Result results = graphDb.execute(query, params);
		
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
            Node x = (Node)row.get("port");
            String pid = (String)row.get("pNameID");
            
            Map<String, Object> portsProp = new HashMap<String, Object>();
                		
            try ( Transaction tx = graphDbAbstract.beginTx() ){
                Node newPort =  graphDbAbstract.createNode(absDeviceLabel);

	            for (String prop_x : x.getPropertyKeys()) {
	            	newPort.setProperty(prop_x,  x.getProperty(prop_x));
	            }
	            tx.success();
	            tx.close();
            }

//            portsPropList.add(new Pair<String, Map<String, Object>>(pid, portsProp));

	     }
	
//		return portsPropList;
		
		// NOTE: DYNAMIC SWITCH TEMPLATES ARE NOT NEEDED: JUST ADD PORTS AND RELATIONS
		// Create new nodes in the abstract graph database and copy the properties from the above list properties to these new nodes
//		createDeviceTemplates.createSwitchWithDynamicPortsTemplate(graphDbAbstract, absDeviceLabel,  absDeviceName,  portsPropListInp, portsPropListOut);
	}
	


	public static Map<String, List<Map<String, Object>>>  getRelationsAttributesBetweenPorts(GraphDatabaseService graphDb, List<String> srcPortList, List<String> destPortList){
		/**
		 * 
		 * This is similar to the method copyRelationsBetweenPorts except that it does not copy the ports to the abstract view in-situ
		 * Just fetch the relation attributes between ports
		 * 
		 * 
		 * @param graphDb
		 * @param srcPortList
		 * @param destPortList
		 * 
		 * @return map objects - key in each object is the source port from the srcPortList
		 */
		
		String query;
		Map<String, Object> params = new HashMap<>();
//		List<String> inpPortNames = new ArrayList<>(), outPortNames = new ArrayList<>();
		List<String> portsListInp = null, portsListOut=null;
		
		params.put("inpPortNames", srcPortList);
		params.put("outPortNames", destPortList);
		
		// Get the node properties from the detailed level graph database 
		// Store them in a data structure
		query = "WITH $inpPortNames AS portNamesList " + 
				"UNWIND portNamesList as pName " +
				"MATCH (n) WHERE n.portName STARTS WITH pName RETURN n.portName as pNameID"; 
		
		Result results = graphDb.execute(query, params);
		
		portsListInp = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
            String pid = (String)row.get("pNameID");
            
            portsListInp.add(pid);
		}
		           
		query = "WITH $outPortNames AS portNamesList " + 
				"UNWIND portNamesList as pName " +
				"MATCH (n) WHERE n.portName STARTS WITH pName RETURN n.portName as pNameID"; 
		
		results = graphDb.execute(query, params);
		
		portsListOut = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
            String pid = (String)row.get("pNameID");
            
            portsListOut.add(pid);
		}
		
		// The following data structure holds the relation properties of the source-destination port relations
		// Key - source port - Value - map object containing the destination port and its related properties
		Map<String, List<Map<String, Object>>> relationProperties = new HashMap<String, List<Map<String, Object>>>();
		
		for(String inputPort: portsListInp){
			List<Map<String, Object>> relPropMapList = new ArrayList<Map<String, Object>>(); 
			for(String outputPort: portsListOut){
				Map<String, Object> relPropMap = new HashMap<String, Object>();
				System.out.println(inputPort + " to  " +  outputPort);
				relPropMap.put("destination", outputPort);
				
				
				// 1. First get the paths where there is NO flow at any of the links in the path
				params = new HashMap<>();
				query = "MATCH path=shortestpath((n1)-[*]->(n2)) WHERE n1.portName = $pName1 AND n2.portName = $pName2 "+
				"WITH path, relationships(path) as steps " +
				"WHERE ALL ( i in Range(0, length(steps) - 1) " +
				"WHERE (size(steps[i]).flow)=0 ) " +
				"RETURN path as p";
				
				params.clear();
				params.put("pName1", inputPort);
				params.put("pName2", outputPort);
				graphDb.execute(query, params);
				
				results = graphDb.execute(query, params);
		
				String nodeNames = "Path: ";
				// NOTE: This is a negative MIN_INT value - if the value needs to change by unit - then need to adapt here
				float minResidualCapacity = 100000f; // this records the minimum residual capacity among all ports in a path 

				while (results.hasNext()) {
					Map<String, Object> row = results.next();
					Path path = (Path) row.get("p");

					Iterable<Node>  nodes = path.nodes();
					
					Iterable<Relationship>  relationships = path.relationships();
					java.util.Iterator<Node> nodeIterator = nodes.iterator();
					
				
					while (nodeIterator.hasNext()){
						Node n = nodeIterator.next();
						nodeNames += (String) n.getProperty("portName") + " -> ";
						
						float residualCapacity = (float) n.getProperty("residualCapacity");
						if (residualCapacity < minResidualCapacity)
							minResidualCapacity = residualCapacity;
					}
					
					
				}
				
				// The properties from source to destination links
				relPropMap.put("minResidualCap", minResidualCapacity);
				relPropMap.put("used", 0);
				relPropMap.put("flow", new ArrayList<String>());
				
				relPropMapList.add(relPropMap);
				
				// ------------------------------------------------------------------------------------------------
				
				// 2. Find all the paths which have flows on them 
				relPropMap = new HashMap<String, Object>();
				params = new HashMap<>();
				query = "MATCH path=shortestpath((n1)-[*]->(n2)) WHERE n1.portName = $pName1 AND n2.portName = $pName2 "+
				"WITH path, relationships(path) as steps " +
				"WHERE ALL ( i in Range(0, length(steps) - 1) " +
				"WHERE (size(steps[i]).flow)>0) " +
				"RETURN path as p";
				
				params.clear();
				params.put("pName1", inputPort);
				params.put("pName2", outputPort);
				graphDb.execute(query, params);
				
				results = graphDb.execute(query, params);
		
				int pathLen = 0;
				nodeNames = "Path: ";
				// NOTE: This is a negative MIN_INT value - if the value needs to change by unit - then need to adapt here
				minResidualCapacity = 100000f; // this records the minimum residual capacity among all ports in a path 
				List<String> flowsList = new ArrayList<String>();

				while (results.hasNext()) {
					
					pathLen=0;
					Map<String, Object> row = results.next();
					Path path = (Path) row.get("p");
					Long pathLength = (long) row.get("pathLength");

					if(pathLength == 0)
						break;

//					System.out.println("\n length of path: "  + Integer.toString(pathLength.intValue()));
					Iterable<Node>  nodes = path.nodes();
					
					Iterable<Relationship>  relationships = path.relationships();
					java.util.Iterator<Node> nodeIterator = nodes.iterator();
					
				
					while (nodeIterator.hasNext()){
						Node n = nodeIterator.next();
						nodeNames += (String) n.getProperty("portName") + " -> ";
						
						float residualCapacity = (float) n.getProperty("residualCapacity");
						if (residualCapacity < minResidualCapacity)
							minResidualCapacity = residualCapacity;
					}
					
//					System.out.println("\nPath: " + nodeNames);
					java.util.Iterator<Relationship> relIterator = relationships.iterator();
					String flowPathString = "";
					Map<String, String> flowPaths = new HashMap<String, String>();
					
					while(relIterator.hasNext()){
						Relationship rel = relIterator.next();
			
						String[] flow = (String []) rel.getProperty("flow");
						List<String> flowL = new ArrayList<String>(Arrays.asList(flow));
						String relName = (String) rel.getProperty("name");
						
						for(String f: flowL){
//							System.out.println("Flow: " + f);
							
							// This needs to be checked to ensure this works for both the detailed view ad any abtract view
							String flowLabel =  "";
							/*
							 * 1. If this is the first link in the path, then add flow label to front and then append link name
							 * 2. Else, append the link name to the flowPath string at the end
							 * 
							 */
							
							// First check if the flow "f" contains some already abstract links in its label.
							// This can be done by checking for the presence of '-' since that indicates the trace start
							if(f.contains("-")){
								flowLabel = (f.split("-"))[0];
								flowPaths.put(flowLabel, (f.split("-"))[1]);
							}
							else
								flowLabel = f;
							
//							System.out.println("\n name:  " +  relName );
							if(flowPaths.containsKey(flowLabel)){
								flowPathString = flowPaths.get(flowLabel);
								flowPathString += "|" + relName;
							}
							else{
								flowPathString += flowLabel + "-" + relName;
							}
//							System.out.println("flowPathString: " + flowPathString);
							flowPaths.put(flowLabel, flowPathString);
						}
					}
					// Add the  new appended flows to the flow variable
					
					for(Map.Entry<String, String> fp: flowPaths.entrySet()){
//						System.out.println(fp.getValue());
						flowsList.add(fp.getValue());
					}
				}
				
				// The properties from source to destination links
				relPropMap.put("minResidualCap", minResidualCapacity);
				
				if(minResidualCapacity == 0)
					relPropMap.put("used", 1);
				else
					relPropMap.put("used", 0);
				
				relPropMap.put("flow", flowsList);
				
				relPropMapList.add(relPropMap);
				
			}
			relationProperties.put("inputPort", relPropMapList);
		}
		
		return relationProperties;
	}
	
	
	public static void copyRelationBetweenPorts(GraphDatabaseService graphDb, GraphDatabaseService graphDbAbstract, List<String> srcPortList,
													List<String> destPortList){
		/**
		* @param graphDb - 
		* @param graphDbAbstract - 
		* @param deviceName - 
		* @param srcPort - the source port
		* @param destPort - the destination port
		*/
		
		/*
		* This method with no filters on path extraction - ONLY available paths: used <> 1
		*/
		String query;
		Map<String, Object> params = new HashMap<>();
//		List<String> inpPortNames = new ArrayList<>(), outPortNames = new ArrayList<>();
		List<String> portsListInp = null, portsListOut=null;
		
		params.put("inpPortNames", srcPortList);
		params.put("outPortNames", destPortList);
		
		// Get the node properties from the detailed level graph database 
		// Store them in a data structure
		query = "WITH $inpPortNames AS portNamesList " + 
				"UNWIND portNamesList as pName " +
				"MATCH (n) WHERE n.portName STARTS WITH pName RETURN n.portName as pNameID"; 
		
		Result results = graphDb.execute(query, params);
		
		portsListInp = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
            String pid = (String)row.get("pNameID");
            
            portsListInp.add(pid);
		}
		           
		query = "WITH $outPortNames AS portNamesList " + 
				"UNWIND portNamesList as pName " +
				"MATCH (n) WHERE n.portName STARTS WITH pName RETURN n.portName as pNameID"; 
		
		results = graphDb.execute(query, params);
		
		portsListOut = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
            String pid = (String)row.get("pNameID");
            
            portsListOut.add(pid);
		}
		
		for(String inputPort: portsListInp){
			for(String outputPort: portsListOut){
				System.out.println(inputPort + " to  " +  outputPort);
				
				// 1. First get the paths where there is NO flow at any of the links in the path
				params = new HashMap<>();
				query = "MATCH path=shortestpath((n1)-[*]->(n2)) WHERE n1.portName = $pName1 AND n2.portName = $pName2 "+
				"WITH path, relationships(path) as steps " +
				"WHERE ALL ( i in Range(0, length(steps) - 1) " +
				"WHERE size((steps[i]).flow)=0 ) " +
				"RETURN path as p, length(path) as pathLength";
				
				params.clear();
				params.put("pName1", inputPort);
				params.put("pName2", outputPort);
				graphDb.execute(query, params);
				
				results = graphDb.execute(query, params);
		
				String nodeNames = "Path: ";
				// NOTE: This is a negative MIN_INT value - if the value needs to change by unit - then need to adapt here
				float minResidualCapacity = 100000f; // this records the minimum residual capacity among all ports in a path 

				while (results.hasNext()) {
					Map<String, Object> row = results.next();
					Path path = (Path) row.get("p");
					Long pathLength = (long) row.get("pathLength");
					
					if(pathLength == 0)
						break;

					Iterable<Node>  nodes = path.nodes();
					
					Iterable<Relationship>  relationships = path.relationships();
					java.util.Iterator<Node> nodeIterator = nodes.iterator();
					
				
					while (nodeIterator.hasNext()){
						Node n = nodeIterator.next();
						nodeNames += (String) n.getProperty("portName") + " -> ";
						
						float residualCapacity = (float) n.getProperty("residualCapacity");
						if (residualCapacity < minResidualCapacity)
							minResidualCapacity = residualCapacity;
					}
					
//					System.out.printf("\nFlow length %d\n", flow.length);
					// Create a new relation in the abstract db and add the properties: flow, minResidualCapacity and used
					
					params = new HashMap<>();
					params.put("pName1", inputPort);
					params.put("pName2", outputPort);
					params.put("minResidualCapacity", minResidualCapacity);
					params.put("flow", new ArrayList<String>());
					
					// TODO: Uncomment the flow properties
					query = "MATCH (n1) WHERE n1.portName=$pName1 WITH n1 " + 
							"MATCH (n2) WHERE n2.portName=$pName2 WITH n1, n2 " + 
							"CREATE (n1)-[r:LINK]->(n2) " + 
							"SET r.used=0, r.minResidualCapacity=$minResidualCapacity " + 
							"SET r.flow=$flow";
					
					graphDbAbstract.execute(query, params);
				}
				

				
				// ------------------------------------------------------------------------------------------------
				
				// 2. Find all the paths which have flows on them 
				params = new HashMap<>();
				query = "MATCH path=shortestpath((n1)-[*]->(n2)) WHERE n1.portName = $pName1 AND n2.portName = $pName2 "+
				"WITH path, relationships(path) as steps " +
				"WHERE ALL ( i in Range(0, length(steps) - 1) " +
				"WHERE size((steps[i]).flow)>0 ) " +
				"RETURN path as p, length(path) as pathLength";
				
				params.clear();
				params.put("pName1", inputPort);
				params.put("pName2", outputPort);
				graphDb.execute(query, params);
				
				results = graphDb.execute(query, params);
		
				int pathLen = 0;
				nodeNames = "";
				// NOTE: This is a negative MIN_INT value - if the value needs to change by unit - then need to adapt here
				minResidualCapacity = 100000f; // this records the minimum residual capacity among all ports in a path 
				List<String> flowsList = new ArrayList<String>();

				while (results.hasNext()) {
					
					pathLen=0;
					Map<String, Object> row = results.next();
					Path path = (Path) row.get("p");
					Long pathLength = (long) row.get("pathLength");

					if(pathLength == 0)
						break;

//					System.out.println("\n length of path: "  + Integer.toString(pathLength.intValue()));
					Iterable<Node>  nodes = path.nodes();
					
					Iterable<Relationship>  relationships = path.relationships();
					java.util.Iterator<Node> nodeIterator = nodes.iterator();
					
				
					while (nodeIterator.hasNext()){
						Node n = nodeIterator.next();
						nodeNames += (String) n.getProperty("portName") + " -> ";
						
						float residualCapacity = (float) n.getProperty("residualCapacity");
						if (residualCapacity < minResidualCapacity)
							minResidualCapacity = residualCapacity;
					}
					
//					System.out.println("\nPath: " + nodeNames);
					java.util.Iterator<Relationship> relIterator = relationships.iterator();
					String flowPathString = "";
					Map<String, String> flowPaths = new HashMap<String, String>();
					
					while(relIterator.hasNext()){
						Relationship rel = relIterator.next();
			
						String[] flow = (String []) rel.getProperty("flow");
						List<String> flowL = new ArrayList<String>(Arrays.asList(flow));
						String relName = (String) rel.getProperty("name");
						
						for(String f: flowL){
//							System.out.println("Flow: " + f);
							
							// This needs to be checked to ensure this works for both the detailed view ad any abtract view
							String flowLabel =  "";
							/*
							 * 1. If this is the first link in the path, then add flow label to front and then append link name
							 * 2. Else, append the link name to the flowPath string
							 * 
							 */
							
							// First check if the flow "f" contains some already abstract links in its label.
							// This can be done by checking for the presence of '-' since that indicates the trace start
							if(f.contains("-")){
								flowLabel = (f.split("-"))[0];
								flowPaths.put(flowLabel, (f.split("-"))[1]);
							}
							else
								flowLabel = f;
							
//							System.out.println("\n name:  " +  relName );
							if(flowPaths.containsKey(flowLabel)){
								flowPathString = flowPaths.get(flowLabel);
								flowPathString += "|" + relName;
							}
							else{
								flowPathString += flowLabel + "-" + relName;
							}
//							System.out.println("flowPathString: " + flowPathString);
							flowPaths.put(flowLabel, flowPathString);
						}
					}
					// Add the  new appended flows to the flow variable
					
					for(Map.Entry<String, String> fp: flowPaths.entrySet()){
//						System.out.println(fp.getValue());
						flowsList.add(fp.getValue());
					}
					
					
					params = new HashMap<>();
					params.put("pName1", inputPort);
					params.put("pName2", outputPort);
					params.put("minResidualCapacity", minResidualCapacity);
//					params.put("flow", flowsList);
					if(minResidualCapacity == 0)
						params.put("used", 1);
					else
						params.put("used", 0);
					
//					for(String f: flowsList)
//						System.out.println(f);

					query = "MATCH (n1) WHERE n1.portName=$pName1 WITH n1 " + 
							"MATCH (n2) WHERE n2.portName=$pName2 WITH n1, n2 " + 
							"CREATE (n1)-[r:LINK]->(n2) " + 
							"SET r.used=$used, r.minResidualCapacity=$minResidualCapacity ";  
//							"SET r.flow=$flow";
					
					graphDbAbstract.execute(query, params);
					
				}
				
				// Create a new relation in the abstract db and add the properties: flow, minResidualCapacity and used
			
			}		
		
		}

	}	
	
	public static void copyDirectRelationBetweenPorts(GraphDatabaseService graphDb, GraphDatabaseService graphDbAbstract, List<String> srcPortList,
			List<String> destPortList){
		/**
		* @param graphDb - 
		* @param graphDbAbstract - 
		* @param srcPort - the source port
		* @param destPort - the destination port
		*/

		/*
		* This method with no filters on path extraction - ONLY available paths: used <> 1
		*/
		String query;
		Map<String, Object> params = new HashMap<>();
		Result results;
		
		for(String inputPort: srcPortList){
			for(String outputPort: destPortList){
				
				// 1. First get the paths where there is NO flow at any of the links in the path
				params = new HashMap<>();
				query = "MATCH (n1)-[r]->(n2) WHERE n1.portName=$pName1 AND n2.portName=$pName2 RETURN r, n1.residualCapacity as n1Res, n2.residualCapacity as n2Res";
				
				params.clear();
				params.put("pName1", inputPort);
				params.put("pName2", outputPort);
				graphDb.execute(query, params);
				
				results = graphDb.execute(query, params);
							

				while (results.hasNext()) {
					System.out.println("\nPath: " + inputPort + " -> " + outputPort);

					Map<String, Object> row = results.next();
					Relationship rel = (Relationship) row.get("r");
					
					
					// Create a new relation in the abstract db and add the properties: flow, minResidualCapacity and used					
					String[] flow = (String []) rel.getProperty("flow");
					List<String> flowL = new ArrayList<String>(Arrays.asList(flow));
					String relName = (String) rel.getProperty("name");
					String flowPathString = "";
					Map<String, String> flowPaths = new HashMap<String, String>();
					List<String> flowsList = new ArrayList<String>();

					for(String f: flowL){
//						System.out.println("Flow: " + f);
						
						// This needs to be checked to ensure this works for both the detailed view ad any abtract view
						String flowLabel =  "";
						/*
						 * 1. If this is the first link in the path, then add flow label to front and then append link name
						 * 2. Else, append the link name to the flowPath string
						 * 
						 */
						
						// First check if the flow "f" contains some already abstract links in its label.
						// This can be done by checking for the presence of '-' since that indicates the trace start
						if(f.contains("-")){
							flowLabel = (f.split("-"))[0];
							flowPaths.put(flowLabel, (f.split("-"))[1]);
						}
						else
							flowLabel = f;
						
//						System.out.println("\n name:  " +  relName );
						if(flowPaths.containsKey(flowLabel)){
							flowPathString = flowPaths.get(flowLabel);
							flowPathString += "|" + relName;
						}
						else{
							flowPathString += flowLabel + "-" + relName;
						}
//						System.out.println("flowPathString: " + flowPathString);
						flowPaths.put(flowLabel, flowPathString);
					}
					
					for(Map.Entry<String, String> fp: flowPaths.entrySet()){
//						System.out.println(fp.getValue());
						flowsList.add(fp.getValue());
					}
					float minResidualCap = Math.min((float) row.get("n1Res"), (float) row.get("n2Res"));
					
					params = new HashMap<>();
					params.put("pName1", inputPort);
					params.put("pName2", outputPort);
					params.put("flow", flowsList);
					params.put("name", (String) rel.getProperty("name") ); // relationship name may not be required 
					
					// TODO: this minimum residual capacity between INTER DEVICE relations need to be taken as input parameter
					if (minResidualCap > 0){
						params.put("used", 0);
					}
					else{
						params.put("used", 1);
					}
						
					
					// TODO: Uncomment the flow properties
					query = "MATCH (n1) WHERE n1.portName=$pName1 WITH n1 " + 
							"MATCH (n2) WHERE n2.portName=$pName2 WITH n1, n2 " + 
							"CREATE (n1)-[r:LINK]->(n2)  "
							+ "SET r.name=$name, r.flow=$flow, r.used=$used";
					
					graphDbAbstract.execute(query, params);
				}
			}
		}
	}
	
	public static void copyRelationBetweenPackets(GraphDatabaseService graphDb, GraphDatabaseService graphDbAbstract, List<String> srcPortList,
			List<String> destPortList){
		String query;
		Map<String, Object> params = new HashMap<>();
		
		copyRelationBetweenPorts(graphDb, graphDbAbstract, srcPortList, destPortList);
	}
	
	
	
	public static List<String> getPortList(GraphDatabaseService graphDb, List<String> portNames){
		/**
		 * 
		*/
		// Get the portList Names of Client/Line side input ports 		
		String query;
		Map<String, Object> params = new HashMap<>();
		
		params.put("pNameList", portNames);
		
		query = "WITH $pNameList AS portNamesList "
				+ "UNWIND portNamesList as pName "
				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
				+ "RETURN n ";
		
		Result results = graphDb.execute(query, params);
		
		List<String> portList = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
            Node x = (Node)row.get("n");
            portList.add((String) x.getProperty("portName"));
		}
		return portList;
		
	}
	

	
	public static void copyDeviceIntoAbstractView(GraphDatabaseService graphDb, GraphDatabaseService graphDbAbstract, String deviceName, Label deviceLabel){
		/**
		 * @param graphDb
		 * @param graphDbAbstract
		 * @param deviceName
		 * @param deviceLabel
		 */
		Map<String, Object> params = new HashMap<>();
		
		// Copy input port list
		params.put("portName", deviceName + ".in");
		
		// TODO: The relation name condition needs to be a regular expression conforming to the naming scheme
		String query = "MATCH (n1) WHERE n1.portName STARTS WITH $portName RETURN n1";
		Result results = graphDb.execute(query, params);
		
		List<String> inpPortList = new ArrayList<String>();
		while(results.hasNext()){
			Map<String, Object> row = results.next();
			Node n1 = (Node) row.get("n1");
			inpPortList.add((String) n1.getProperty("portName")); 
		}
		
		copyPortsFromDevices(graphDb, graphDbAbstract, inpPortList, Label.label("Abstract." + deviceLabel.toString()));
	
		// Copy output port list
		params = new HashMap<>();
		params.put("portName", deviceName + ".out");
		
		// TODO: The relation name condition needs to be a regular expression conforming to the naming scheme
		query = "MATCH (n1) WHERE n1.portName STARTS WITH $portName RETURN n1";
		results = graphDb.execute(query, params);
		
		List<String> outPortList = new ArrayList<String>();

		while(results.hasNext()){
			Map<String, Object> row = results.next();
			Node n1 = (Node) row.get("n1");
			outPortList.add((String) n1.getProperty("portName")); 
		}
		
		copyPortsFromDevices(graphDb, graphDbAbstract, outPortList, Label.label("Abstract." + deviceLabel.toString()));

		// Copy relations between input and output list
		copyRelationBetweenPorts(graphDb, graphDbAbstract, inpPortList, outPortList);
		
	}
	
	
}
