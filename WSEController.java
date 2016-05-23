import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.json.JSONException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class WSEController {
	
	private String usage = "java org.apache.lucene.demo.IndexFiles"
            + " [-index INDEX_PATH] [-docs DOCS_PATH] [-query QUERY_STRING] [-update UPDATE_FLAG]\n\n"
            + "This indexes the documents in DOCS_PATH, creating a Lucene index"
            + "in INDEX_PATH that can be searched with SearchFiles";
	private String indexPath = "index";
	private String docsPath = null;
	private String queryString = null;
	private boolean create = true;
	
	//required for lucene searchFiles
	private String field = "contents";
	private boolean raw = false;
	private int hitsPerPage = 100;
	private String queries = null;
	
	//required for document clustering
	private ClusteringDataFromLucene carrotCluster;
	private String clusterType = "Lingo";
	
	WSEController(String[] args) {
		try{
	    	for(int i=0;i<args.length;i++) {
		        if ("-index".equals(args[i])) {
		          indexPath = args[i+1];
		          i++;
		        } else if ("-docs".equals(args[i])) {
		          docsPath = args[i+1];
		          i++;
		        } else if ("-query".equals(args[i])) {
		          queryString = args[i+1];
		          queryString = queryString.replaceAll("[\\-\\+\\.\\^:,]","").replaceAll("[^\\w\\s]","");
		        } else if ("-update".equals(args[i])) {
			      create = false;
			    } else if ("-cluster".equals(args[i])) {
			    	clusterType = args[i+1];
			    }
		    }
	    } catch (Exception e) {
	    	System.err.println("Correct Usage: " + usage);
	    	
	    }
	    if (docsPath == null) {
	        System.err.println("Usage: " + usage);
	        System.exit(1);
	    }
	    
	    carrotCluster = new ClusteringDataFromLucene(this.clusterType,this.queryString);
	    
	}
	public void run() throws IOException, JSONException{
		
		System.out.println("Query received: " + queryString);
		/**uncomment later
		 *
		 */
		this.crawl();
		
		this.buildIndex();
		/**
		 * Disabled this as single list of ranked result is not necessary
		 * We will use clustering with relative ranks for each cluster
		 */
		//this.search();		
		this.runClustering();
	}
	/**
	 * Call this function once index is created
	 */
	public void runClustering(){
		//this.clusterize(clusterType);
		System.out.println("LINGO-CLUSTERING-START");
		this.clusterize("LINGO");
		System.out.println("LINGO-CLUSTERING-END");
		System.out.println("KMEANS-CLUSTERING-START");
		this.carrotCluster = new ClusteringDataFromLucene("KMeans",this.queryString);
		this.clusterize("KMeans");
		System.out.println("KMEANS-CLUSTERING-END");
		System.out.println("STC-CLUSTERING-START");
		this.carrotCluster = new ClusteringDataFromLucene("STC",this.queryString);
		this.clusterize("STC");
		System.out.println("STC-CLUSTERING-END");
	}
	
	public void runClustering(String clusterType){
		this.clusterize(clusterType);
	}
	
	//GET TOP STATIC DOCUMENTS FROM PUBLIC SEARCH ENGINE 
	private void crawl() throws IOException, JSONException {		
		
		BingCustomSearch bcs = new BingCustomSearch();
		bcs.generateBingLinks(this.queryString);
		
	}
	private void buildIndex() throws IOException, JSONException {	 
	    
	    
	    //CREATE LUCENE INDEX
		
		//check if document path(where indexed files are stored
	    Path docDir = Paths.get(docsPath);
	    ////System.out.println("docDir:'" + docDir.toString());
	    if (!Files.isReadable(docDir)) {
	      System.err.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }
	    
	    Date start = new Date();
	    try {
	      System.out.println("Indexing to directory '" + indexPath + "'...");

	      Directory dir = FSDirectory.open(Paths.get(indexPath));
	      Analyzer analyzer = new StandardAnalyzer();
	      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
	
	      /*
 		CREATE IS SET FALSE TO FORCE THE INDEX TO GROW WITH TIME
		*/
	      if (create) {
	        // Create a new index in the directory, removing any
	        // previously indexed documents:
	        iwc.setOpenMode(OpenMode.CREATE);
	      } else {
	        // Add new documents to an existing index:
	        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	      }
	      
	      IndexWriter writer = new IndexWriter(dir, iwc);
	      IndexFiles.indexDocs(writer, docDir);
	      
	      writer.close();

	      Date end = new Date();
	      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

	    } catch (IOException e) {
	      System.out.println(" caught a " + e.getClass() +
	       "\n with message: " + e.getMessage());
	    }	
		
	}
	private void clusterize(String clusterType) {
		try {
			this.carrotCluster.runClustering(clusterType);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("IOException detected while clustering: " + e.getMessage());
			//e.printStackTrace();
		}
	}
	private void search() {
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Searcher failed to open index path");
			e.printStackTrace();
			//System.exit(1);
		}
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer();
	    
	    BufferedReader in = null;
	    QueryParser parser = new QueryParser(field, analyzer);

	    Query query = null;
		try {
			query = parser.parse(queryString.toString());
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.err.println("Searcher failed to parse query");
			e.printStackTrace();
		}
		
		////System.out.println("Results for query: " + query.toString(field));
	    try {
	    	//search top hitsPerPage hits for the query
			searcher.search(query, hitsPerPage);
			SearchFiles.doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);
		    reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error fetching query from index");
			e.printStackTrace();
		}
	    
	    
	}
}
