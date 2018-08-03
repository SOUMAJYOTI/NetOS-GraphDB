package test.java.com.belllabs.viz;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Graph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import javafx.util.Pair;
import main.java.com.belllabs.Utilities.GetGraphDbInfo;
import main.java.com.belllabs.algorithms.RandomNetworkProvisioning;
import main.java.com.belllabs.netviz.GStream.*;
//import test.java.com.belllabs.devices.createNetworks_1;
import main.java.com.belllabs.Utilities.IOOperations;

public class TestDeviceGraphView {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		// 1. Start the network provisioning
		String dirSave = "target\\databases";
		File databaseDirectory = new File(dirSave);
		
//		 boolean deleteDatabaseDir = IOOperations.deleteDirectory(databaseDirectory);
		 System.out.printf("Directory to which database is saved: %s \n", dirSave );
//		 System.out.println("Old files successfully deleted: " + deleteDatabaseDir);
				    
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory); // Loads Neo4j database
		
		// Create a test network
		String deviceName = "RDM1"; // Switch ID
		List<String> flowPaths = new ArrayList<String>();
		try ( Transaction tx = graphDb.beginTx() ){ // Creates Neo4j transactions
			
			String flowAdded = "f1";
			String portFlowAdd = "S1.in1";
//			flowPaths = RandomNetworkProvisioning.addFlowToNetwork(graphDb, flowAdded, portFlowAdd);
//			
			tx.success();	
			System.out.println("\nNeo4j transactions successful end...");
			
		}
		graphDb.shutdown();
		    
		String dbPath = "target\\databases";
		VizNetwork newView = new VizNetwork(dbPath);
		
//		System.out.println("The devices present in the Graph database: \n");
//		List<Pair<String, String>> deviceList = GetGraphDbInfo.getDeviceList();
//		for (Pair<String, String> s: deviceList){
//			System.out.println(s.getKey() + " with ID: " +  s.getValue());
//		}
		
		String snapName = "Network_16";
		// Visualize the network
		System.out.println("\n Visualizing the network" );
		Graph graph = newView.createGraph();
		newView.findFlowPaths("S1.in1", "RDM1.AM.in2", "red", "1");
		newView.findFlowPaths("RDM1.AM.in2", "RDM2.DB.in1", "red", "1");
		newView.findFlowPaths("RDM2.DB.in1", "S2.out2", "red", "1");
		
		newView.findFlowPaths("S1.in2", "RDM1.AM.in1", "yellow", "2");
		newView.findFlowPaths("RDM1.AM.in1", "RDM1.WSS.out3", "yellow", "2");
		newView.findFlowPaths("RDM1.WSS.out3", "RDM1.AB.in1", "yellow", "2");
		newView.findFlowPaths("RDM1.AB.in1", "RDM2.DB.in2", "yellow", "2");
		newView.findFlowPaths("RDM2.DB.in2", "RDM2.DDM.in2", "yellow", "2");
		newView.findFlowPaths("RDM2.DDM.in2", "RDM2.DDM.out3", "yellow", "2");
		newView.findFlowPaths("RDM2.DDM.out3", "S2.out1", "yellow", "2");
		
		
		newView.findFlowPaths("S1.in2", "RDM1.AM.in4", "green", "3");
		newView.findFlowPaths("RDM1.AM.in4", "RDM1.WSS.out6", "green", "3");
		newView.findFlowPaths("RDM1.WSS.out6", "RDM1.AB.in4", "green", "3");
		newView.findFlowPaths("RDM1.AB.in4", "RDM2.DB.in1", "green", "3");
		newView.findFlowPaths("RDM2.DB.in1", "RDM2.DB.out2", "green", "3");
		newView.findFlowPaths("RDM2.DB.out2", "RDM2.WSS.out4", "green", "3");
		newView.findFlowPaths("RDM2.WSS.out4", "RDM2.AB.in2", "green", "3");
		newView.findFlowPaths("RDM2.AB.in2", "RDM3.DDM.in2", "green", "3");
		newView.findFlowPaths("RDM3.DDM.in2", "S3.out1", "green", "3");

		

//		newView.findFlowPaths("RDM1.WSS.out3", "RDM1.AB.out1", "RDM2.DB.in2", "yellow");
//		newView.findFlowPaths("RDM2.AB.out1", "RDM2.DDM.in1", "RDM2.WSS.in7", "yellow");
//		newView.findFlowPaths("RDM2.WSS.in7", "S2.out1", "yellow");
		
		
//		newView.displayGraph(snapName);
		
	}

}
