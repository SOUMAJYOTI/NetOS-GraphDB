package test.java.com.belllabs.abstractions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import javafx.util.Pair;
import main.java.com.belllabs.abstractions.AbstractionOperations;
import main.java.com.belllabs.abstractions.AbstractionProcedures;
import main.java.com.belllabs.devices.CreateIndividualDevices;
import main.java.com.belllabs.devices.CreateRelationTemplates;
import main.java.com.belllabs.Utilities.IOOperations;

public class TestAbstraction1 {
	public static void main(String[] args) throws IOException {

		// This is the database where the graph database schema would be stored
		String dirSave = "target\\abstract.db";
		File databaseDirectorySave = new File(dirSave);
		String dirExtract = "target\\databases";
		File databaseDirectoryExtract = new File(dirExtract);
		 
		
		// The following operation clears any history database files in that folder every time the program is executed
		// SUBJECT TO CHANGE
		 boolean deleteDatabaseDirExtract = IOOperations.deleteDirectory(databaseDirectoryExtract);				 
		 boolean deleteDatabaseDirSave = IOOperations.deleteDirectory(databaseDirectorySave);				 
				 
		 System.out.printf("Directory to which abtract database is saved: %s \n", dirSave );
		 System.out.println("Old files successfully deleted: " + deleteDatabaseDirSave);
		 System.out.println("Old files successfully deleted: " + deleteDatabaseDirExtract);

		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectoryExtract); // Extracts existing Neo4j database
	
		System.out.println("\nSaving abstract view of the network ...." );
		    
		GraphDatabaseService graphDbAbstract = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectorySave); // Creates Neo4j database for abstract views
		
		
		try ( Transaction tx = graphDb.beginTx() ){ // Creates Neo4j transaction
			Label ROADMLabel = Label.label("ROADM");
			String ROADMName = "RDM1";
			Label switchLabel = Label.label("Switch");
			String switchName = "S1";
			
			int k=2, n=2, c=3, d=2;
			int numSwitchPorts = 6;
			int rdmPortStart=1, rdmPortEnd=4, switchPortStart=3, switchPortEnd=6;
			List<Pair<String, String>> portConnAB = new ArrayList<Pair<String, String>>();
			List<Pair<String, String>> portConnDB= new ArrayList<Pair<String, String>>();

			CreateIndividualDevices.createROADMSAndSwitch(graphDb, ROADMName, k, n, c, d, portConnAB, portConnDB, switchName, numSwitchPorts, rdmPortStart, rdmPortEnd, switchPortStart, switchPortEnd);
			// Horizontal abstractions
			AbstractionOperations.abstractPacketLayer(graphDb, graphDbAbstract, switchLabel, ROADMLabel, switchName, ROADMName);
			
			ROADMLabel = Label.label("ROADM");
			ROADMName = "RDM2";
			switchLabel = Label.label("Switch");
			switchName = "S2";
			
			CreateIndividualDevices.createROADMSAndSwitch(graphDb, ROADMName, k, n, c, d, portConnAB, portConnDB, switchName, numSwitchPorts, rdmPortStart, rdmPortEnd, switchPortStart, switchPortEnd);
			// Horizontal abstractions
			AbstractionOperations.abstractPacketLayer(graphDb, graphDbAbstract, switchLabel, ROADMLabel, switchName, ROADMName);
					
			// For now manually create the input-output fiber direction port relations
			List<Pair<String, String>> ioPortPairs = new ArrayList<Pair<String, String>>();
			ioPortPairs.add(new Pair<String, String>("RDM1.AB.out2", "RDM2.DB.in1"));
			ioPortPairs.add(new Pair<String, String>("RDM2.AB.out1", "RDM1.DB.in2"));

			CreateRelationTemplates.createAutoRelationshipsRAODMAndROADM(graphDb, ioPortPairs);
			
			System.out.println("\nCopying links bewteen Packets containing Switch S1 and Switch S2");
			List<String> srcPortList = new ArrayList<String>();
			srcPortList.add("S1.out");	
			
			List<String> destPortList = new ArrayList<String>();
			destPortList.add("S2.in");
			
			AbstractionProcedures.copyRelationBetweenPackets(graphDb, graphDbAbstract, srcPortList, destPortList);
			
			tx.success();	
			System.out.println("\nNeo4j transactions successful end...");
		 }

		
//		// Shut the Graph database and delete the previous database files
//		// TODO: DO not delete the previous files, keep versions / log the info
	    graphDb.shutdown();
	    graphDbAbstract.shutdown();
	    System.out.println("Graph database shut down...");
	
	}
 
}
