package main.java.com.belllabs.IOFormats.Outputs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.TransactionWork;

import apoc.generate.Neo4jGraphGenerator;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.StatementResultCursor;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import static org.neo4j.driver.v1.Values.parameters;

public class exportGraph implements AutoCloseable {
	
	/**
	 * This is a first test to export the Neo4j Graph to a GraphML format. This step is needed to load the graph for Gephi Toolkit usage and further visualizations
	 * @param args
	 * @throws IOException
	 */
	private final Driver driver;

    public exportGraph( String uri, String user, String password )
    {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @Override
    public void close() throws Exception
    {
        driver.close();
    }

    public void exportGraphToGML( final String message )
    {
        try ( Session session = driver.session() )
        {

            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    StatementResult result = tx.run( "CALL apoc.export.graphml.all(\'" + "D:/NetOS_Neo4j/NetOS_N4j/data/neo4j_export/" + "complete_graph.gml\', {})",
                            parameters( "message", message ) );
                    return result.single().get( 0 ).asString();
                }

//				@Override
//				public String execute(org.neo4j.driver.v1.Transaction arg0) {
//					// TODO Auto-generated method stub
//					return null;
//				}
            } );
            System.out.println( greeting );
        }
    }
    
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		 try (exportGraph exp = new exportGraph( "bolt://localhost:7687", "neo4j", "Justdoit5!" ) )
	        {
	            exp.exportGraphToGML( "hello, world" );
	        }
		
	}
}
