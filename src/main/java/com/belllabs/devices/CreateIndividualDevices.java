package main.java.com.belllabs.devices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;

import javafx.util.Pair;

public class CreateIndividualDevices {
	public static void createSwitch(GraphDatabaseService graphDb, Label deviceLabel, String switchName, int numInpPorts ){

		CreateDeviceTemplates.createSwitchTemplate(graphDb, deviceLabel,  switchName,  numInpPorts,  0f, 0, new ArrayList<String>(), 40f);
		CreateRelationTemplates.createAutoRelationshipsIntraSwitches(graphDb, deviceLabel, switchName);
		System.out.printf("Created a SWITCH device with ID: %s; Attributes: Number of input ports: %d, Number of output ports: %d \n", switchName, numInpPorts, numInpPorts );
		
	}
	
	public static void createROADMS(GraphDatabaseService graphDb, String deviceName, int k, int n, int c, int d, List<Pair<String, String>> portConnAB, List<Pair<String, String>> portConnDB) throws IOException{
		Label deviceLabel = Label.label("ROADM");
		float dPortBand = 1f, oPortBand = 1f, oPortRate=1f, dPortCap = 1f, oPortCap=1f, oPortReach=1;

		CreateDeviceTemplates.createROADMTemplate(graphDb, deviceLabel, deviceName, k, n, 
		                    c, d,
		                    dPortBand, oPortBand, oPortRate, dPortCap, oPortCap, oPortReach,
		                    portConnAB, portConnDB );
		
		
		System.out.printf("\nCreated a ROADM device with ID: %s; Attributes: Number of digital ports: %d, Number of optical ports: %d "
						+ " Number of colors: %d, Number of directions: %d \n", deviceName, k*n, n, c, d  );

	}
	
	public static void createROADMSAndSwitch(GraphDatabaseService graphDb, String ROADMName, int k, int n, int c, int d, List<Pair<String, String>> portConnAB, List<Pair<String, String>> portConnDB,
            String switchName,  int numSwitchPorts, int rdmPortStart, int rdmPortEnd,int  switchPortStart, int switchPortEnd) throws IOException{
		
		// SWITCH
		//-------------------------------------------------------------------
		
		Label deviceLabel = Label.label("Switch");
		
		createSwitch(graphDb, deviceLabel, switchName, numSwitchPorts);		
		
		// -------------------------------------------------------------------
		// ROADM
		//-------------------------------------------------------------------
		
		deviceLabel = Label.label("ROADM");
		
		createROADMS(graphDb, ROADMName, k, n, c, d, portConnAB, portConnDB);
		
		
		// --------------------------------------------------------------------
		// CREATE THE INTER DEVICE RELATIONSHIPS
		//---------------------------------------------------------------------
		System.out.printf("\nConnecting Switch: " + switchName  + " to " + " Roadm device: " + ROADMName + "\n");
		
		CreateRelationTemplates.createAutoRelationshipsRAODMAndSwitch(graphDb, ROADMName, rdmPortStart, rdmPortEnd, switchName, switchPortStart, switchPortEnd);
		
		}
}
