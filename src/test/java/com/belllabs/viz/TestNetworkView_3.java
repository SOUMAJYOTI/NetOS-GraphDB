package test.java.com.belllabs.viz;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.Graph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import javafx.util.Pair;
import main.java.com.belllabs.Utilities.GetGraphDbInfo;
import main.java.com.belllabs.algorithms.RandomNetworkProvisioning;
import main.java.com.belllabs.netviz.GStream.*;


public class TestNetworkView_3 {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		    
		String dbPath = "target\\graph.db";
		VizNetwork newView = new VizNetwork(dbPath);
		
		List<Pair<String, String>> deviceList = new ArrayList<Pair<String, String>>();
		deviceList.add(new Pair<String, String>("ROADM", "RDM1"));
		deviceList.add(new Pair<String, String>("Switch", "S1"));
//		deviceList.add(new Pair<String, String>("ROADM", "RDM2"));
//		deviceList.add(new Pair<String, String>("Switch", "S3"));
//		deviceList.add(new Pair<String, String>("ROADM", "RDM3"));
//		deviceList.add(new Pair<String, String>("Switch", "S3"));
		
		
		String snapName = "Network_23";
		// Visualize the network
//		System.out.println("\n Visualizing the network" );
//		Graph graph = VizNetwork.createGraph();
		Graph graph = VizNetwork.createSubGraph(deviceList);
		
//		newView.findPathBetweenPorts("RDM1.AM.in", "RDM1.AM.out", "blue", "1");
//		newView.findPathBetweenPorts("RDM1.AM.out", "RDM1.WSS.in", "green", "2");
//		newView.findPathBetweenPorts("RDM1.WSS.in", "RDM1.WSS.out", "blue", "1");
//		newView.findPathBetweenPorts("RDM1.WSS.out", "RDM1.AB.in", "green", "2");
//		newView.findPathBetweenPorts("RDM1.AB.in", "RDM1.AB.out", "blue", "1");


//		newView.findPathBetweenPorts("RDM1.DDM.in", "RDM1.DDM.out", "blue", "1");

//		
//		FlowViz.flowPathHighlightManual(graph, "S1.in1", "S1.out4", "yellow", "2");
//		FlowViz.flowPathHighlightManual(graph, "S1.out4", "RDM1.AM.in2", "yellow", "2");
//		newView.findFlowPaths("RDM1.AM.in2", "RDM1.AB.out2", "yellow", "2");
//		FlowViz.flowPathHighlightManual(graph, "RDM1.AB.out2", "RDM1.DB.in2", "red", "1");
//		FlowViz.flowPathHighlightManual(graph, "RDM2.DB.in2", "RDM2.DDM.out2", "red", "1");
//		FlowViz.flowPathHighlightManual(graph, "RDM2.DDM.out2", "S2.in6", "red", "1");
//		FlowViz.flowPathHighlightManual(graph, "S2.in6", "S2.out2", "red", "1");
		
//		newView.findFlowPaths("RDM1.DB.in1", "S1.out1", "blue", "3");
		
	
		
//		FlowViz.flowPathHighlightManual(graph, "S1.in2", "S1.out3", "blue", "2");
//		FlowViz.flowPathHighlightManual(graph, "S1.out3", "RDM1.AM.in1", "blue", "2");
//		newView.findFlowPaths("RDM1.AM.in1", "RDM1.AB.out1", "blue", "2");
		
//		FlowViz.flowPathHighlightManual(graph, "S1.in2", "S1.out3", "yellow", "2");
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

