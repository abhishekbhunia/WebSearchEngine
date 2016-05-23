import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
//import java.io.UnsupportedEncodingException;
//import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.apache.commons.io.*;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
public class BingCustomSearch {
	String accountKey;
	String bingUrlPattern;
	ArrayList<URL> crawledURLs;
	ArrayList<StringPair> storedFilesWithDomain;
	class StringPair{
		String a, b;
		StringPair(String a, String b){
			this.a = a; this.b = b;
		}
	}
	BingCustomSearch() {
		accountKey = "<Use your's>";
        bingUrlPattern = "https://api.datamarket.azure.com/Bing/Search/Web?Query=%%27%s%%27&$format=JSON&$skip=";        				
        crawledURLs = new ArrayList<URL>();
        storedFilesWithDomain = new ArrayList<StringPair>();
	}
	private void storePages(URL u) throws Exception {
		String extension = "";

		int i = u.toString().lastIndexOf('.');
		if (i > 0) {
		    extension = u.toString().substring(i+1);
		}
		extension = extension.trim();
		//System.out.println("extension: " + extension);
		try{
			if(u.toString().length()<80 && (extension.equals("htm") || extension.equals("html"))){
				crawledURLs.add(u);
				Path currentRelativePath = Paths.get("");
				String s = currentRelativePath.toAbsolutePath().toString();

				String str = s + "/crawldocs/" + u.getHost() + "." + u.toString().split("/")[u.toString().split("/").length - 1];//"/crawldocs/" + u.toString();
				Path target = Paths.get(str);
				try (InputStream in = u.openStream()) {
				    System.out.println("storing in: " + target.toString());
				    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
				}
			}
			else{
				return;
			}
		} catch (Exception e){
			throw e;
		}
		
			
		
		
	}
	
	private void iterate(String queryString, int k) throws IOException, JSONException{
		String query=URLEncoder.encode(queryString, Charset.defaultCharset().name());
        String temp = bingUrlPattern + k*50;
        
    	String bingUrl = String.format(temp, query);
        String accountKey_t = accountKey + ":" + accountKey;
        byte[] message = accountKey_t.getBytes("UTF-8");
        String encoded = DatatypeConverter.printBase64Binary(message);
                
        URL url = new URL(bingUrl);
		URLConnection connection = url.openConnection();;
		
		connection.setRequestProperty("Authorization", "Basic " + encoded);
		
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            final StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            final JSONObject json = new JSONObject(response.toString());
            final JSONObject d = json.getJSONObject("d");
            final JSONArray results = d.getJSONArray("results");
            final int resultsLength = results.length();
            for (int i = 0; i < resultsLength; i++) {
                final JSONObject aResult = results.getJSONObject(i);
                try{
                	storePages(new URL(aResult.get("Url").toString()));
                }catch(Exception e){
                	
                }
                
            }
        }   
       
	}
        private void deleteExisting() {
	  Path currentRelativePath = Paths.get("");
          String s = currentRelativePath.toAbsolutePath().toString();
	  s = s + "/crawldocs";
          System.out.println("will be deleting the contents of: " + s);
          try {

             FileUtils.cleanDirectory(new File(s));
	  } catch (IOException e) {
		System.err.println("Error deleting existing files" + e.getMessage());
	  }
	}
	
	public void generateBingLinks(String queryString) throws IOException, JSONException {
	//delete all existing files
        deleteExisting();		
		
        int k=0;
        System.out.println("Search Query: " + queryString);
        for(; crawledURLs.size()<50 && k<10; ){
        	iterate(queryString,k);
        	k++;
        }
       
	}
	
}
