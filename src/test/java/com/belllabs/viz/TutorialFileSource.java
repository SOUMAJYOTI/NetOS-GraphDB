package test.java.com.belllabs.viz;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

import java.io.IOException;

public class TutorialFileSource {

	public static void main(String[] args) throws IOException {
		String filePath = "D:/NetOS_Neo4j/NetOS_N4j/data/neo4j_export/" + "complete_graph.gml";
		Graph g = new DefaultGraph("g");
		FileSource fs = FileSourceFactory.sourceFor(filePath);

		fs.addSink(g);

		try {
			fs.begin(filePath);
			
			g.display(false);
//			while (fs.nextEvents()) {
//				// Optionally some code here ...
//			}
		} catch( IOException e) {
			e.printStackTrace();
		}

		try {
			fs.end();
		} catch( IOException e) {
			e.printStackTrace();
		} finally {
			fs.removeSink(g);
		}
	}
}
