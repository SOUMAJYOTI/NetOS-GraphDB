package main.java.com.belllabs.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import javafx.util.Pair;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;

public class PathTraversals {
	public static List<String> findSingleShortestPathBetweenPorts(GraphDatabaseService graphDb, String src, String dest){
		String query;
		Map<String, Object> params = new HashMap<>();
	
		params = new HashMap<>();
		query = "MATCH path=shortestpath((n1)-[*]->(n2)) WHERE n1.portName = $pName1 AND n2.portName = $pName2 "+
		"WITH path, relationships(path) as steps " +
//		"WHERE ALL ( i in Range(0, length(steps) - 1) " +
//		"WHERE size((steps[i]).flow)>=0 ) " +
		"RETURN path as p, length(path) as pathLength";
		
//		query = "MATCH path=shortestpath((n1)-[*]->(n2)) WHERE n1.portName = $pName1 AND n2.portName = $pName2 "+
//				"WITH path, relationships(path) as steps " +
//				"WHERE ANY ( i in Range(0, length(steps) - 1) " +
//				"WHERE (steps[i]).name CONTAINS $deviceName ) " +
//				"RETURN path as p, length(path) as pathLength";
		
		params.clear();
		params.put("pName1", src);
		params.put("pName2", dest);
		params.put("deviceName", "RDM1");
		graphDb.execute(query, params);
		
		Result results = graphDb.execute(query, params);

		List<String> pathNameList = new ArrayList<String>();
		
		while (results.hasNext()) {
			System.out.println("Path found.......");
			Map<String, Object> row = results.next();
			Path path = (Path) row.get("p");
			
			Iterable<Relationship>  relationships = path.relationships();
			java.util.Iterator<Relationship> relIterator = relationships.iterator();

			while(relIterator.hasNext()){
				Relationship rel = relIterator.next();
	
				String relName = (String) rel.getProperty("name");
				pathNameList.add(relName);
			}
		}
		return pathNameList;
	}
	
	public static List<String> findSingleShortestPathBetweenPortsThroughPorts(GraphDatabaseService graphDb, String src, String dest, String portThrough){
		String query;
		Map<String, Object> params = new HashMap<>();
	
		params = new HashMap<>();
		query = "MATCH path=shortestpath((n1)-[*]->(n2)) WHERE n1.portName = $pName1 AND n2.portName = $pName2 "+
		"WITH path, relationships(path) as steps " +
//		"WHERE ALL ( i in Range(0, length(steps) - 1) " +
//		"WHERE size((steps[i]).flow)>=0 ) WITH path, relationships(path) as steps " +
		"WHERE ANY( i in Range(0, length(steps) - 1) " +
		"WHERE (steps[i]).name STARTS WITH $portName ) " + 
		"RETURN path as p, length(path) as pathLength";
		
//		query = "MATCH path=shortestpath((n1)-[*]->(n2)) WHERE n1.portName = $pName1 AND n2.portName = $pName2 "+
//				"WITH path, relationships(path) as steps " +
//				"WHERE ANY ( i in Range(0, length(steps) - 1) " +
//				"WHERE (steps[i]).name CONTAINS $deviceName ) " +
//				"RETURN path as p, length(path) as pathLength";
		
		params.clear();
		params.put("pName1", src);
		params.put("pName2", dest);
		params.put("portName", portThrough);
		graphDb.execute(query, params);
		
		Result results = graphDb.execute(query, params);

		List<String> pathNameList = new ArrayList<String>();
		
		while (results.hasNext()) {
			System.out.println("Path found.......");
			Map<String, Object> row = results.next();
			Path path = (Path) row.get("p");
			
			Iterable<Relationship>  relationships = path.relationships();
			java.util.Iterator<Relationship> relIterator = relationships.iterator();

			while(relIterator.hasNext()){
				Relationship rel = relIterator.next();
	
				String relName = (String) rel.getProperty("name");
				pathNameList.add(relName);
			}
		}
		return pathNameList;
	}
	
	public static List<String> findKShortestPathBetweenPorts(GraphDatabaseService graphDb, String src, String dest){
		String query;
		Map<String, Object> params = new HashMap<>();
		return null;
	}
	
	public static List<List<String>> findAllShortestPathsBetweenPorts(GraphDatabaseService graphDb, List<String> srcList, List<String> destList){
		String query;
		Map<String, Object> params = new HashMap<>();
	
		params = new HashMap<>();
		query = "WITH $srcList as srcPorts, $destList as destPorts"
				+ " UNWIND srcPorts as src"
				+ " UNWIND destPorts as dest"
				+ " MATCH path=allShortestPaths((n1)-[*]->(n2)) WHERE n1.portName = src AND n2.portName = dest " 
				+ " RETURN path as p, length(path) as pathLength";
		
		params.clear();
		params.put("srcList", srcList);
		params.put("destList", destList);
		graphDb.execute(query, params);
		
		Result results = graphDb.execute(query, params);
		
		List<List<String>> pathLists = new ArrayList<List<String>>();
		while (results.hasNext()) {
//			System.out.println("Path found.......");
			Map<String, Object> row = results.next();
			Path path = (Path) row.get("p");
			
			Iterable<Relationship>  relationships = path.relationships();
			java.util.Iterator<Relationship> relIterator = relationships.iterator();

			List<String> pathNameList = new ArrayList<String>();
			while(relIterator.hasNext()){
				Relationship rel = relIterator.next();
	
				String relName = (String) rel.getProperty("name");
				pathNameList.add(relName);
			}
			pathLists.add(pathNameList);
		}
		return pathLists;
	}
}
