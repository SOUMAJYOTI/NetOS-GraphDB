package test.java.com.belllabs.abstractions;

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

import main.java.com.belllabs.Utilities.IOOperations;
import main.java.com.belllabs.abstractions.AbstractionOperations;
import main.java.com.belllabs.abstractions.AbstractionProcedures;
import main.java.com.belllabs.devices.*;

public class TestAbsROADM {

	public static void main(String[] args) throws IOException {

		// This is the database where the graph database schema would be stored
		String dirSave = "target\\abstract.db";
		String dirExtract = "target\\graph.db";

		File databaseDirectorySave = new File(dirSave);
		
		// The following operation clears any history database files in that folder every time the program is executed
		// SUBJECT TO CHANGE
		 boolean deleteDatabaseDirSave = IOOperations.deleteDirectory(databaseDirectorySave);				 
				 
		 System.out.printf("Directory to which abtract database is saved: %s \n", dirSave );
//		 System.out.println("Old files successfully deleted: " + deleteDatabaseDirSave);
		
		 File databaseDirectoryExtract = new File(dirExtract);
		 
		 System.out.printf("Directory from which curent database is extracted: %s \n", dirExtract );

		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectoryExtract); // Extracts existing Neo4j database
	
		System.out.println("\nSaving abstract view of the network ...." );
		    
		GraphDatabaseService graphDbAbstract = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectorySave); // Creates Neo4j database for abstract views
		
		Label deviceLabel = Label.label("ROADM");
		String deviceName;
		try ( Transaction tx = graphDb.beginTx() ){ // Creates Neo4j transaction
			deviceName = "RDM1";
//			AbstractionOperations.abstractROADM(graphDb, graphDbAbstract, deviceName);
			AbstractionOperations.abstractSwitchAndROADM(graphDb, graphDbAbstract, Label.label("Switch"), Label.label("ROADM"), "S1", "RDM1");

			deviceName = "RDM2";
//			AbstractionOperations.abstractROADM(graphDb, graphDbAbstract, deviceName);
			AbstractionOperations.abstractSwitchAndROADM(graphDb, graphDbAbstract, Label.label("Switch"), Label.label("ROADM"), "S2", "RDM2");
			
			deviceName = "RDM3";
//			AbstractionOperations.abstractROADM(graphDb, graphDbAbstract, deviceName);
			AbstractionOperations.abstractSwitchAndROADM(graphDb, graphDbAbstract, Label.label("Switch"), Label.label("ROADM"), "S3", "RDM3");
			
			CreateRelationTemplates.createManualRelationships(graphDbAbstract, "RDM1.AB.out2", "RDM2.DB.in1");
			CreateRelationTemplates.createManualRelationships(graphDbAbstract, "RDM1.AB.out1", "RDM2.DB.in2");
			CreateRelationTemplates.createManualRelationships(graphDbAbstract, "RDM3.AB.out1", "RDM1.DB.in2");
			CreateRelationTemplates.createManualRelationships(graphDbAbstract, "RDM2.AB.out1", "RDM3.DB.in1");


			tx.success();	
			System.out.println("\nNeo4j transactions successful end...");
		 }

		
		
//		// Shut the Graph database and delete the previous database files
//		// TODO: DO not delete the previous files, keep versions / log the info
	    graphDb.shutdown();
	    graphDbAbstract.shutdown();
	    System.out.println("Graph database shut down...");
	
	    // ---------------------- Copy the files from the current local directory to the Neo4j server directory ----------------------------------
//	    System.out.println("\n Copying the database files to the Neo4j Directory");
//	    File source = databaseDirectorySave;
//	    String neo4jDir = "D:\\neo4j_package\\data\\databases\\graph.db";
//	    
//	    File dest = new File(neo4jDir);
//	    boolean deleteDatabaseDir = deleteDirectory(dest);
//
//	    try {
//	        FileUtils.copyDirectory(source, dest);
//	    } catch (IOException e) {
//	        e.printStackTrace();
//	    }
//	    System.out.println("\n Done...");
	}

}

