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
import main.java.com.belllabs.Utilities.IOOperations;

public class TestNetworkView_1 {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		    
		String dbPath = "target\\abstract.db";
		VizNetwork newView = new VizNetwork(dbPath);
		
		List<Pair<String, String>> deviceList = new ArrayList<Pair<String, String>>();
		deviceList.add(new Pair<String, String>("Abstract.ROADM", "RDM1"));
		deviceList.add(new Pair<String, String>("Abstract.Switch", "S1"));
		
		String snapName = "Network_12";
		// Visualize the network
//		System.out.println("\n Visualizing the network" );
		Graph graph = VizNetwork.createSubGraph(deviceList);

		FlowViz.flowPathHighlightManual(graph, "S1.in1", "S1.out4", "yellow", "2");
		FlowViz.flowPathHighlightManual(graph, "S1.out4", "RDM1.AM.in2", "yellow", "2");
		newView.findAbstractPathBetweenPorts("RDM1.AM.in2", "RDM1.AB.out2", "yellow", "2");		
//		newView.findAbstractPathBetweenPorts("RDM1.DB", "RDM1.DDM", "black", "1");


//		FlowViz.flowPathHighlightManual(graph, "S1.in1", "S1.out4", "yellow", "2");
//		FlowViz.flowPathHighlightManual(graph, "S1.out4", "RDM1.AM.in1", "yellow", "2");
//		FlowViz.flowPathHighlightManual(graph, "RDM1.AM.in1", "RDM1.AB.out1", "yellow", "2");
//		FlowViz.flowPathHighlightManual(graph, "RDM1.AB.out2", "RDM2.DB.in2", "red", "1");
//		FlowViz.flowPathHighlightManual(graph, "RDM2.DB.in2", "RDM2.DDM.out2", "red", "1");
//		FlowViz.flowPathHighlightManual(graph, "RDM2.DDM.out2", "S2.in6", "red", "1");
//		FlowViz.flowPathHighlightManual(graph, "S2.in6", "S2.out2", "red", "1");
//
//		FlowViz.flowPathHighlightManual(graph, "RDM1.DB.in1", "RDM1.DDM.out4", "blue", "3");
//		FlowViz.flowPathHighlightManual(graph, "RDM1.DDM.out4", "S1.in6", "blue", "3");
//		FlowViz.flowPathHighlightManual(graph, "S1.in6", "S1.out1", "blue", "3");

//		FlowViz.flowPathHighlightManual(graph, "S1.out3", "RDM1.AM.in1", "yellow", "2");
//		FlowViz.flowPathHighlightManual(graph, "RDM1.AM.in1", "RDM1.AB.out1", "yellow", "2");
//		FlowViz.flowPathHighlightManual(graph, "RDM1.AB.out1", "RDM2.DB.in1", "yellow", "2");
//		FlowViz.flowPathHighlightManual(graph, "RDM2.DB.in1", "RDM2.DDM.out3", "yellow", "2");
//		FlowViz.flowPathHighlightManual(graph, "RDM2.DDM.out3", "S2.in5", "yellow", "2");
//		FlowViz.flowPathHighlightManual(graph, "S2.in5", "S2.out1", "yellow", "2");
//
//		FlowViz.flowPathHighlightManual(graph, "S1.in1", "S1.out6", "green", "3");
//		FlowViz.flowPathHighlightManual(graph, "S1.out6", "RDM1.AM.in4", "green", "3");
//		FlowViz.flowPathHighlightManual(graph, "RDM1.AM.in4", "RDM1.AB.out2", "green", "3");
//		FlowViz.flowPathHighlightManual(graph, "RDM1.AB.out2", "RDM2.DB.in2", "green", "3");
//		FlowViz.flowPathHighlightManual(graph, "RDM2.DB.in2", "RDM2.AB.out1", "green", "3");
//		FlowViz.flowPathHighlightManual(graph, "RDM2.AB.out1", "RDM3.DB.in1", "green", "3");
//		FlowViz.flowPathHighlightManual(graph, "RDM3.DB.in1", "RDM3.DDM.out2", "green", "3");
//		FlowViz.flowPathHighlightManual(graph, "RDM3.DDM.out2", "S3.in5", "green", "3");
//		FlowViz.flowPathHighlightManual(graph, "S3.in5", "S3.out1", "green", "3");

		
//		FlowViz.flowPathHighlightManual(graph, "S1.in1", "S1.out4", "red", "1");
//		FlowViz.flowPathHighlightManual(graph, "S1.out4", "S2.in6", "red", "1");
//		FlowViz.flowPathHighlightManual(graph, "S2.in6", "S2.out2", "red", "1");
//
//		FlowViz.flowPathHighlightManual(graph, "S1.in1", "S1.out3", "yellow", "2");
//		FlowViz.flowPathHighlightManual(graph, "S1.out3", "S2.in5", "yellow", "2");
//		FlowViz.flowPathHighlightManual(graph, "S2.in5", "S2.out1", "yellow", "2");
//
//		FlowViz.flowPathHighlightManual(graph, "S1.in2", "S1.out6", "green", "3");
//		FlowViz.flowPathHighlightManual(graph, "S1.out6", "S3.in5", "green", "3");
//		FlowViz.flowPathHighlightManual(graph, "S3.in5", "S3.out1", "green", "3");

		

		int dimX = 500, dimY = 800;
		newView.displayGraph(snapName, dimX, dimY);		
	}

}

