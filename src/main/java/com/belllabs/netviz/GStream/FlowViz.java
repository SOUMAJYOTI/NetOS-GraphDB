package main.java.com.belllabs.netviz.GStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.StoreLockException;

import javafx.util.Pair;

import org.graphstream.ui.swingViewer.*;

import main.java.com.belllabs.Helpers.VisualizationCSS;
import main.java.com.belllabs.algorithms.PathTraversals;

public class FlowViz {
	public static void flowPathHighlight(Graph graph, List<String> path, String color, String flowNum){
		StringBuilder cssOptions = new StringBuilder();
		cssOptions.append("edge.linkFlow_" + flowNum + "{");
		cssOptions.append(" size: 4px; ");
		cssOptions.append(" arrow-shape:arrow; ");
		cssOptions.append(" arrow-size: 10px, 7px; ");
		cssOptions.append(" fill-color: " + color + "; ");
		cssOptions.append("}");
		if(! (VisualizationCSS.ROADMStyleSheet.contains("linkFlow_" + flowNum)) )
			VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
		
		for(String link: path){
			org.graphstream.graph.Edge e = graph.getEdge(link);
			graph.removeEdge(e);
//			System.out.println(link);
			String[] portsList = link.split("\\.");
//			for(String s: portsList)
//				System.out.println(s);
			String src="", dest="";
			int i=0;
			while(true){
				if(portsList[i].contains("in") || portsList[i].contains("out")){
					src += portsList[i];
					i++;
					break;
				}
				src += portsList[i] + ".";
				i++;
			}
			while(i<portsList.length){
				dest += portsList[i] + ".";
				i++; 
			}
			dest = dest.substring(0, dest.length()-1);
//			System.out.println(src  + "   " + dest);
			org.graphstream.graph.Edge eNew = graph.addEdge(link, src, dest, true);
			eNew.setAttribute("ui.class", "linkFlow_" + flowNum);
		}
	}
	
	public static void flowPathHighlightManual(Graph graph, String src, String dest, String color, String flowNum){
		StringBuilder cssOptions = new StringBuilder();
		cssOptions.append("edge.linkFlow_" + flowNum + "{");
		cssOptions.append(" size: 4px; ");
		cssOptions.append(" arrow-shape:arrow; ");
		cssOptions.append(" arrow-size: 10px, 7px; ");
		cssOptions.append(" fill-color: " + color + "; ");
		cssOptions.append("}");
		if(! (VisualizationCSS.ROADMStyleSheet.contains("linkFlow_" + flowNum)) )
			VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
		
		
		String relName = src + "." + dest;
		System.out.println(src + "to " + dest);
		org.graphstream.graph.Edge eNew = graph.addEdge(relName, src, dest, true);
		eNew.setAttribute("ui.class", "linkFlow_" + flowNum);
		
	}
}
