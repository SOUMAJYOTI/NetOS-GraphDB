package main.java.com.belllabs.Helpers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;


public class FlowOperations {
	public static List<String> unionFlowsOnLinks(List<String> flowsListFrom, List<String> flowsListTo){
		String flowPathString = "";
		Map<String, String> flowPaths = new HashMap<String, String>();
		for(String f: flowsListFrom){
			String flowLabel = "";
			flowLabel = (f.split("-"))[0];
			if(flowPaths.containsKey(flowLabel)){
				flowPathString = flowPaths.get(flowLabel);
				flowPathString += "|" + (f.split("-"))[1];
			}
			else{
				flowPathString += f;
			}
			flowPaths.put(flowLabel, flowPathString);
		}
		for(String f: flowsListTo){
			String flowLabel = "";
			flowLabel = (f.split("-"))[0];
			if(flowPaths.containsKey(flowLabel)){
				flowPathString = flowPaths.get(flowLabel);
				flowPathString += "|" + (f.split("-"))[1];
			}
			else{
				flowPathString += f;
			}
			flowPaths.put(flowLabel, flowPathString);
		}
		
		List<String> flowsListUnion = new ArrayList<String>();
		for(Map.Entry<String, String> fp: flowPaths.entrySet()){
			flowsListUnion.add(fp.getValue());
		}
		
		return flowsListUnion;
	}
	
	
	public static void flowDeterminismAdapter(GraphDatabaseService graphDb){
		/**
		 * this handles the flow determinism operations at the Adapter input and output ports
		 * 
		 */
		
		
		
		
		
	}
}
