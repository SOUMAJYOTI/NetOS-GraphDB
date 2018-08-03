package test.java.com.belllabs.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import javafx.util.Pair;
import main.java.com.belllabs.abstractions.AbstractionOperations;
import main.java.com.belllabs.algorithms.RandomNetworkProvisioning;
import main.java.com.belllabs.devices.CreateDeviceTemplates;
import main.java.com.belllabs.devices.CreateRelationTemplates;
import main.java.com.belllabs.Utilities.IOOperations;

public class TestFlowProvision {
	public static void main(String[] args) throws IOException {
		
		// 1. Start the network provisioning
		String dirSave = "target\\graph.db";
		File databaseDirectory = new File(dirSave);
		
		 boolean deleteDatabaseDir = IOOperations.deleteDirectory(databaseDirectory);
		 System.out.printf("Directory to which database is saved: %s \n", dirSave );
		 System.out.println("Old files successfully deleted: " + deleteDatabaseDir);
				    
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory); // Loads Neo4j database
		
		// Create a test network
		String deviceName = "RDM1"; // Switch ID
		try ( Transaction tx = graphDb.beginTx() ){ // Creates Neo4j transaction
			int k=3, n=2, c=3, d=2;
			float dPortBand = 1f, oPortBand = 1f, oPortRate=1f, dPortCap = 1f, oPortCap=1f, oPortReach=1;
			List<Pair<String, String>> portConnAB = new ArrayList<Pair<String, String>>();
			List<Pair<String, String>> portConnDB= new ArrayList<Pair<String, String>>();

//			test_ROADM.createROADMS(graphDb, deviceName, k, n, c, d, portConnAB, portConnDB);
			
			String flowAdded = "f1";
			String portFlowAdd = "S1.in1";
			RandomNetworkProvisioning.addFlowToNetwork(graphDb, flowAdded, portFlowAdd);
			
			tx.success();	
			System.out.println("\nNeo4j transactions successful end...");
			
		}
		
	    String neo4jDir = "D:\\neo4j_package\\data\\databases";
	    IOOperations.copyFilesToDirectory(dirSave, neo4jDir);
	    
	    // 2. Abstract the new network with the flows provisioned
	    dirSave = "target\\abstract.db";
		File databaseDirectorySave = new File(dirSave);
	    GraphDatabaseService graphDbAbstract = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectorySave); // Creates Neo4j database for abstract views
	    
	    try ( Transaction tx = graphDb.beginTx() ){ // Creates Neo4j transaction
			AbstractionOperations.abstractROADM(graphDb, graphDbAbstract, deviceName);
			tx.success();	
			System.out.println("\nNeo4j transactions successful end...");
		 }
	    
	    graphDb.shutdown();
	    graphDbAbstract.shutdown();
	    System.out.println("Graph database shut down...");
	
	    IOOperations.copyFilesToDirectory(dirSave, neo4jDir);

	    
	}
}
