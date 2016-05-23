import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONException;


public class MainApp {
	public static void main(String[] args) {
		WSEController controller = new WSEController(args);
		try {
			controller.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("IO Exception in main: " + e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.err.println("JSON Exception in main: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
