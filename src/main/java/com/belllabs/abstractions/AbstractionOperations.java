package main.java.com.belllabs.abstractions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Value;
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

public class AbstractionOperations {
/**
 * 
 * 
 */
	public void abstractDevicePairs(GraphDatabaseService graphDb, GraphDatabaseService graphDbAbstract, String deviceLabel1, String deviceLabel2, String deviceName1, String deviceName2,
									String portTypeDevice1, String portTypeDevice2){
		/**
		 * Creates an abstract view from device1 to device2 
		 */
		
		try ( Transaction tx = graphDb.beginTx() ){ // Creates Neo4j transaction
			Label absDeviceLabel = Label.label(deviceLabel1 + "." + deviceLabel2);
			String absDeviceName = deviceName1 + "." + deviceName2; // Switch ID
			
			String query;
			Map<String, Object> params = new HashMap<>();
			
			// Get the portList Names from device 1 
			params.put("pName", deviceName1 + "." + portTypeDevice1);
			
			query = "MATCH (n) WHERE n.portName CONTAINS pName RETURN n";
			
			Result results = graphDb.execute(query, params);
			
			List<String> portListDevice1 = new ArrayList<String>();
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
	            Node x = (Node)row.get("n");
	            portListDevice1.add((String) x.getProperty("portName"));
			}
//			AbstractionProcedures.copyPortsFromDevices(graphDb, graphDbAbstract, deviceLabel, deviceName, absDeviceLabel, absDeviceName);
//			AbstractionProcedures.copyRelationsBetweenPorts(graphDb, graphDbAbstract, deviceLabel, deviceName, absDeviceLabel, absDeviceName);

			tx.success();	
			System.out.println("\nNeo4j transactions successful end...");
		 }
	}
	
	public static String abstractROADM(GraphDatabaseService graphDb, GraphDatabaseService graphDbAbstract, String deviceName){
		try ( Transaction tx = graphDb.beginTx() ){ // Creates Neo4j transaction
			System.out.println("Abstracting ROADM: " +  deviceName );
			Label absDeviceLabel = Label.label("Abstract" + "." + "ROADM");
			String absDeviceName = "Abstract" + "." + deviceName; // ROADM ID
			
			String query;
			Map<String, Object> params = new HashMap<>();
			
			// Get the portList Names of Client/Line side input ports 
			List<String> portNames = new ArrayList<String>();
			portNames.add(deviceName + ".AM" + ".in");
			portNames.add(deviceName + ".DB" + ".in");
			
			params.put("pNameList", portNames);
			
			query = "WITH $pNameList AS portNamesList "
					+ "UNWIND portNamesList as pName "
					+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
					+ "RETURN n ";
			
			Result results = graphDb.execute(query, params);
			
			List<String> inpPortList = new ArrayList<String>();
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
	            Node x = (Node)row.get("n");
	            inpPortList.add((String) x.getProperty("portName"));
			}
			
			// Add the client/line output ports
			AbstractionProcedures.copyPortsFromDevices(graphDb, graphDbAbstract,  inpPortList, absDeviceLabel );
			
			
			// Get the portList Names of Client/Line side output ports 
			portNames = new ArrayList<String>();
			portNames.add(deviceName + ".DDM" + ".out");
			portNames.add(deviceName + ".AB" + ".out");
			
			params.clear();
			params.put("pNameList", portNames);
			
			query = "WITH $pNameList AS portNamesList "
					+ "UNWIND portNamesList as pName "
					+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
					+ "RETURN n ";
			
			results = graphDb.execute(query, params);
			
			List<String> outPortList = new ArrayList<String>();
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
	            Node x = (Node)row.get("n");
	            outPortList.add((String) x.getProperty("portName"));
			}
			
			// Add the client/line input ports
			AbstractionProcedures.copyPortsFromDevices(graphDb, graphDbAbstract,  outPortList, absDeviceLabel );
			
			// Add relations between the input and output ports list 
			AbstractionProcedures.copyRelationBetweenPorts(graphDb, graphDbAbstract, inpPortList, outPortList);

			
			tx.success();	
			return absDeviceName;

		 }
	}

	public static void abstractSwitchAndROADM(GraphDatabaseService graphDb, GraphDatabaseService graphDbAbstract, Label switchLabel, Label ROADMLabel, String switchName, String ROADMName){
		try ( Transaction tx = graphDb.beginTx() ){ // Creates Neo4j transaction
			
			System.out.println("Abstracting ROADM: " +  ROADMName + " and Switch: " +  switchName );

			// Abstract ROADM and copy to new Graph Database
			String absROADMName = abstractROADM(graphDb, graphDbAbstract, ROADMName);
			
			// Copy the switch to the abstract view AS-IS
			AbstractionProcedures.copyDeviceIntoAbstractView(graphDb, graphDbAbstract, switchName, switchLabel);

			
			String query;
			Map<String, Object> params = new HashMap<>();
			List<String> portNames = new ArrayList<String>();
			
			// Get the portList Names of Switch input ports 
			portNames = new ArrayList<String>();
			portNames.add(switchName  + ".in");
			List<String> inpPortSwitchList = AbstractionProcedures.getPortList(graphDb, portNames);		
			
			// Get the portList Names of Switch output ports 
			portNames = new ArrayList<String>();
			portNames.add(switchName  + ".out");
			List<String> outPortSwitchList = AbstractionProcedures.getPortList(graphDb, portNames);
		
			// These are the only ports connected from ROADM to Switch
			// Get the portList Names of Client side input ports 
			portNames = new ArrayList<String>();
			portNames.add(ROADMName + ".AM" + ".in");
			List<String> inpPortROADMList = AbstractionProcedures.getPortList(graphDb, portNames);
			// Add the client/line output ports
						
			// Get the portList Names of Client side output ports 
			portNames = new ArrayList<String>();
			portNames.add(ROADMName + ".DDM" + ".out");
			List<String> outPortROADMList = AbstractionProcedures.getPortList(graphDb, portNames);
			// Add the client/line output ports
			
			System.out.println("Copying relations between Switch " + switchName + " and ROADM " + ROADMName);
			
			// Add relations between the input and output ports list 
			AbstractionProcedures.copyDirectRelationBetweenPorts(graphDb, graphDbAbstract, outPortROADMList, inpPortSwitchList);
			AbstractionProcedures.copyDirectRelationBetweenPorts(graphDb, graphDbAbstract, outPortSwitchList, inpPortROADMList);
			
			tx.success();	
		 }
	}
	
	
	public static void abstractPacketLayer(GraphDatabaseService graphDb, GraphDatabaseService graphDbAbstract, Label switchLabel, Label ROADMLabel, String switchName, String ROADMName){
		try ( Transaction tx = graphDb.beginTx() ){ // Creates Neo4j transaction
			
			System.out.println("Abstracting ROADM: " +  ROADMName + " and Switch: " +  switchName );

			// 1. Abstract ROADM and copy to new Graph Database
			String absROADMName = abstractROADM(graphDb, graphDbAbstract, ROADMName);
			
			List<String> portNames = new ArrayList<String>();

			// 2. Copy the switch device AS-IS into the abstract view
			AbstractionProcedures.copyDeviceIntoAbstractView(graphDb, graphDbAbstract, switchName, switchLabel);
			
			// 3. Copy hidden relation attributes between switch I/O port list and the ROADM I/O port list 
			// Get the portList Names of Switch input ports 
			portNames = new ArrayList<String>();
			portNames.add(switchName  + ".in");
			
			List<String> inpPortSwitchList = AbstractionProcedures.getPortList(graphDb, portNames);
//			System.out.println(inpPortSwitchList.size());
			
			// Get the portList Names of Switch output ports 
			portNames = new ArrayList<String>();
			portNames.add(switchName  + ".out");
			List<String> outPortSwitchList = AbstractionProcedures.getPortList(graphDb, portNames);

			portNames = new ArrayList<String>();
			portNames.add(absROADMName + "AB.out");
			List<String> outPortROADMList = AbstractionProcedures.getPortList(graphDb, portNames);

			Map<String, List<Map<String, Object>>> switchOutputToRDMOutputLinks  = AbstractionProcedures.getRelationsAttributesBetweenPorts(graphDbAbstract, outPortSwitchList, outPortROADMList);
			
			portNames = new ArrayList<String>();
			portNames.add(absROADMName + "DB.in");
			List<String> inpPortROADMList = AbstractionProcedures.getPortList(graphDb, portNames);

					
			Map<String, List<Map<String, Object>>> RDMInputToSwitchInputLinks  = AbstractionProcedures.getRelationsAttributesBetweenPorts(graphDbAbstract, inpPortROADMList, inpPortSwitchList);
			
			// Delete the ROADM from the abstract view
			String query;
			Map<String, Object> params = new HashMap<>();
			
			// Get the portList Names of ROADM 
			portNames = new ArrayList<String>();
			portNames.add(ROADMName);
			
			params.put("pNameList", portNames);
			
			query = "MATCH (n) WHERE n.portName STARTS WITH $pNameList DETACH DELETE (n)";
			
			graphDb.execute(query, params);
			
			tx.success();	
		 }
	}


}
