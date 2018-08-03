package main.java.com.belllabs.Utilities;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class IOOperations {
		public static // Delete all directories and files in a folder
	boolean deleteDirectory(File directoryToBeDeleted) {
	    File[] allContents = directoryToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	            deleteDirectory(file);
	        }
	    }
	    return directoryToBeDeleted.delete();
	}
		
	public static void copyFilesToDirectory(String srcDir, String destDir){
		    System.out.println("\nCopying the database files to the Neo4j Directory");
		    File src = new File(srcDir);
		    File dest = new File(destDir);
		    boolean deleteDatabaseDir = deleteDirectory(dest);

		    try {
		    	FileUtils.copyDirectoryToDirectory(src, dest);
//		        FileUtils.copyDirectoryStructure(source, dest);
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    System.out.println("\n Done...");
	}
}
