import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.store.FSDirectory;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.clustering.kmeans.*;
import org.carrot2.clustering.stc.*;
import org.carrot2.clustering.synthetic.*;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.Document;
import org.carrot2.core.ProcessingComponentConfiguration;
import org.carrot2.core.ProcessingResult;
import org.carrot2.core.attribute.CommonAttributesDescriptor;
//import org.carrot2.examples.ConsoleFormatter;
//import org.carrot2.examples.CreateLuceneIndex;
import org.carrot2.source.lucene.LuceneDocumentSource;
import org.carrot2.source.lucene.LuceneDocumentSourceDescriptor;
import org.carrot2.source.lucene.SimpleFieldMapperDescriptor;

import org.carrot2.shaded.guava.common.collect.Maps;

public class ClusteringDataFromLucene
{
	private String clusterType = "Lingo";
	private String queryString = null;
	private String indexPath = null;//"/home/ab5966/public_html/cgi-bin/index";
	final Controller controller = ControllerFactory.createPooling();
	final Map<String, Object> luceneGlobalAttributes = new HashMap<String, Object>();
	
	public void runClustering(String clusterType) throws IOException{
		this.clusterType = clusterType;
		
		org.apache.log4j.BasicConfigurator.configure();
    	org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    	
    	LuceneDocumentSourceDescriptor
        .attributeBuilder(luceneGlobalAttributes)
        .directory(FSDirectory.open(Paths.get(indexPath)));
    	
    	
    	SimpleFieldMapperDescriptor
        .attributeBuilder(luceneGlobalAttributes)
        .titleField("hid")
        .contentField("contents")
        .urlField("path")
        
        .searchFields(Arrays.asList(new String [] {"hid", "contents", "path"}));
    	
    	controller.init(new HashMap<String, Object>(),
                new ProcessingComponentConfiguration(LuceneDocumentSource.class, "lucene",
                    luceneGlobalAttributes));
    	
    	//String query = "learn chinese";
        final Map<String, Object> processingAttributes = Maps.newHashMap();
        CommonAttributesDescriptor.attributeBuilder(processingAttributes)
            .query(this.queryString);
        
        ProcessingResult process=null;
		if(clusterType == "KMeans") { //k-means clustering
			System.out.println("RESULT USING KMEANS");
                        try{
				process = controller.process(processingAttributes, "lucene",
	        		BisectingKMeansClusteringAlgorithm.class.getName());
			}
			catch(Exception e){
				System.out.println("Kmeans clustering could not be done");
			}
	        
		} else if(clusterType == "STC") {//suffix tree clustering
			System.out.println("RESULT USING STC");
			try{
					process = controller.process(processingAttributes, "lucene",
					STCClusteringAlgorithm.class.getName());
			}
			catch(Exception e) {
				System.out.println("STC(Suffix Tree) Clustering could not be done");
			}
	        
		} else {//lingo clustering
			System.out.println("RESULT USING LINGO");
			try{
				process = controller.process(processingAttributes, "lucene",
	            		LingoClusteringAlgorithm.class.getName());
			}
			catch(Exception e){
				System.out.println("LINGO clustering could not be done");
			}
		}
		
		try{	
			ConsoleFormatter.displayResults(process);
		}
		catch(Exception e){
			System.out.println(clusterType + " clustering did not generate any results");
		}
	}
	
	ClusteringDataFromLucene(String query) {
		this.queryString = query;
		Path curRelative = Paths.get("");
		String s = curRelative.toAbsolutePath().toString();
		s = s + "/index";
		this.indexPath = s;
	}
	ClusteringDataFromLucene(String clusterType, String query) {
		this.clusterType = clusterType;
		this.queryString = query;
		Path curRelative = Paths.get("");
                String s = curRelative.toAbsolutePath().toString();
                s = s + "/index";
                this.indexPath = s;

	}  
    
}
