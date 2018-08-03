package test.java.com.belllabs.devices;

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
import main.java.com.belllabs.Utilities.IOOperations;
import main.java.com.belllabs.devices.CreateDeviceTemplates;
import main.java.com.belllabs.devices.CreateIndividualDevices;
import main.java.com.belllabs.devices.CreateRelationTemplates;
import main.java.com.belllabs.netviz.GStream.VizDeviceTemplates;

public class TestIndividualDevices {
	
	public static void main(String[] args) throws IOException {
		String dirSave = "target\\graph.db";
	    String neo4jDir = "D:\\neo4j_package\\data\\databases\\graph.db";

		File databaseDirectory = new File(dirSave);
		
		// The following operation clears any history database files in that folder everytime the program is executed
		// SUBJECT TO CHANGE
		 boolean deleteDatabaseDir = IOOperations.deleteDirectory(databaseDirectory);
		 System.out.printf("Directory to which database is saved: %s \n", dirSave );
		 System.out.println("Old files successfully deleted: " + deleteDatabaseDir);
		    
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory); // Creates Neo4j database
		
		Label deviceLabel; // Stores the  device label like Host / Switch / Adapter
		String switchName="", ROADMName="", deviceType="";
		int numSwitchPorts=0;
		
		// ADD TRANSACTION EXCEPTION ***************
		try ( Transaction tx = graphDb.beginTx() ){ // Creates Neo4j transaction
			
			System.out.println("Neo4j transactions successfully begin...\n");
			System.out.println("Starting to create device templates.. \n");
			for(int i=1; i<=4; i++){
				// -------------------------------------------------------------------
				// SWITCH
				//-------------------------------------------------------------------
				
				deviceLabel = Label.label("Switch");
				switchName = "S" + Integer.toString(i); // Switch ID
				numSwitchPorts = 6;
				
				CreateIndividualDevices.createSwitch(graphDb, deviceLabel, switchName, numSwitchPorts);				
				
				// -------------------------------------------------------------------
				// ROADM
				//-------------------------------------------------------------------
				
				
				deviceLabel = Label.label("ROADM");
				ROADMName = "RDM" + Integer.toString(i); // Switch ID
							
				int k=2, n=2, c=3, d=2;
				float dPortBand = 1f, oPortBand = 1f, oPortRate=1f, dPortCap = 1f, oPortCap=1f, oPortReach=1;
				List<Pair<String, String>> portConnAB = new ArrayList<Pair<String, String>>();
				List<Pair<String, String>> portConnDB= new ArrayList<Pair<String, String>>();

//				CreateIndividualDevices.createROADMS(graphDb, ROADMName, k, n, c, d, portConnAB, portConnDB);
				
				// -------------------------------------------------------------------
				// Switch and ROADM
				// -------------------------------------------------------------------
				int rdmPortStart = 1, rdmPortEnd = 4;
				int switchPortStart = 3, switchPortEnd = 6;
				CreateIndividualDevices.createROADMSAndSwitch(graphDb, ROADMName, k, n, c, d, portConnAB, portConnDB, 
									switchName, numSwitchPorts, rdmPortStart, rdmPortEnd, switchPortStart, switchPortEnd);
			}
			
			System.out.println("\nConnecting ROADM to ROADM" );
			
			List<Pair<String, String>> roadmConn = new ArrayList<Pair<String, String>>();
			
			roadmConn.add(new Pair<String, String>("RDM1.AB.out2", "RDM2.DB.in1"));
			roadmConn.add(new Pair<String, String>("RDM1.AB.out1", "RDM2.DB.in2"));
			roadmConn.add(new Pair<String, String>("RDM3.AB.out1", "RDM1.DB.in2"));
			roadmConn.add(new Pair<String, String>("RDM2.AB.out1", "RDM3.DB.in1"));


			CreateRelationTemplates.createAutoRelationshipsRAODMAndROADM(graphDb, roadmConn);
			
			tx.success();	// THIS IS IMPORTANT
			System.out.println("\nNeo4j transactions successful end...");
		 }
		
		// Shut the Graph database and delete the previous database files
		// TODO: DO not delete the previous files, keep versions / log the info
	    graphDb.shutdown();
	    System.out.println("Graph database shut down...");
	    
	    // Copy the files from the current local directory to the Neo4j server directory
	    File source = databaseDirectory;
	    
	    File dest = new File(neo4jDir);
	    deleteDatabaseDir = IOOperations.deleteDirectory(dest);

	    try {
	        FileUtils.copyDirectory(source, dest);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

}
