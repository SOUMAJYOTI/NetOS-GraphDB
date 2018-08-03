package test.java.com.belllabs.devices;

import java.io.File;
import java.io.IOException;

import org.neo4j.logging.Log;

import javafx.util.Pair;

import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;

import java.util.*; 
import java.io.File;

import main.java.com.belllabs.Helpers.PortPropObject;
import main.java.com.belllabs.devices.*;
import main.java.com.belllabs.Utilities.IOOperations;

public class TestROADM {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		// This is the database where the graph database schema would be stored
		String dirSave = "target\\databases";
		File databaseDirectory = new File(dirSave);
		
		// The following operation clears any history database files in that folder everytime the program is executed
		// SUBJECT TO CHANGE
		 boolean deleteDatabaseDir = IOOperations.deleteDirectory(databaseDirectory);
		 System.out.printf("Directory to which database is saved: %s \n", dirSave );
		 System.out.println("Old files successfully deleted: " + deleteDatabaseDir);
		    
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory); // Creates Neo4j database
		
		Label deviceLabel; // Stores the  device label like Host / Switch / Adapter
		String deviceName="", deviceType="";
		
		System.out.println("Testing ROADM devices: independent...\n");
		// ADD TRANSACTION EXCEPTION ***************
		try ( Transaction tx = graphDb.beginTx() ){ // Creates Neo4j transaction
			
			System.out.println("Neo4j transactions successfully begin...\n");
			System.out.println("Starting to create device templates.. \n");
						
			// -------------------------------------------------------------------

			// ROADM
			//-------------------------------------------------------------------
			
			deviceLabel = Label.label("ROADM");
			deviceName = "RDM1"; // ROADM ID
			int k=3, n=2, c=3, d=2;
			float dPortBand = 1f, oPortBand = 1f, oPortRate=1f, dPortCap = 1f, oPortCap=1f, oPortReach=1;
			List<Pair<String, String>> portConnAB = new ArrayList<Pair<String, String>>();
			List<Pair<String, String>> portConnDB= new ArrayList<Pair<String, String>>();

			
			CreateDeviceTemplates.createROADMTemplate(graphDb, deviceLabel, deviceName, k, n, 
			                    c, d,
			                    dPortBand, oPortBand, oPortRate, dPortCap, oPortCap, oPortReach,
			                    portConnAB, portConnDB );
			
			
			System.out.printf("Created a ROADM device with ID: %s; Attributes: Number of digital ports: %d, Number of optical ports: %d "
							+ " Number of colors: %d, Number of directions: %d \n", deviceName, k*n, n, c, d  );
			
			
			// -------------------------------------------------------------------

			// ROADM
			//-------------------------------------------------------------------
			
			deviceLabel = Label.label("ROADM");
			deviceName = "RDM2"; // Switch ID
			k=3;
			n=2;
			c=3;
			d=2;
//			float dPortBand = 1f, oPortBand = 1f, oPortRate=1f, dPortCap = 1f, oPortCap=1f, oPortReach=1;
//			List<Pair<String, String>> portConnAB = new ArrayList<Pair<String, String>>();
//			List<Pair<String, String>> portConnDB= new ArrayList<Pair<String, String>>();

			
			CreateDeviceTemplates.createROADMTemplate(graphDb, deviceLabel, deviceName, k, n, 
                    c, d,
                    dPortBand, oPortBand, oPortRate, dPortCap, oPortCap, oPortReach,
                    portConnAB, portConnDB );
			
			
			System.out.printf("Created a ROADM device with ID: %s; Attributes: Number of digital ports: %d, Number of optical ports: %d "
							+ " Number of colors: %d, Number of directions: %d \n", deviceName, k*n, n, c, d  );

			
			// ---------------------------------------- INTER ROADM RELATIONSHIPS ---------------------------------'
			
			// For now manually create the input-output fiber direction port relations
			List<Pair<String, String>> ioPortPairs = new ArrayList<Pair<String, String>>();
//			ioPortPairs.add(new Pair<String, String>("RDM1.AB.out2", "RDM2.DB.in1"));
//			ioPortPairs.add(new Pair<String, String>("RDM2.AB.out1", "RDM1.DB.in2"));

			List<Pair<String, String>> roadmConn = new ArrayList<Pair<String, String>>();
			roadmConn.add(new Pair<String, String>("RDM1.AB.out2", "RDM2.DB.in1"));
			tx.success();	
			System.out.println("\nNeo4j transactions successful end...");
			
		 }
		
		// Shut the Graph database and delete the previous database files
		// TODO: DO not delete the previous files, keep versions / log the info
	    graphDb.shutdown();
	    System.out.println("Graph database shut down...");
		
	    System.out.println("\n Done...");
	}


}
