

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Element;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.lang.StringBuilder;
import java.text.*;

public class HtmlParser {
	String localurl;
	HtmlParser(String url) {
		this.localurl = url;
		////System.out.println("url received: " + this.localurl);
	}
	@SuppressWarnings("deprecation")
	public org.apache.lucene.document.Document getDocument(InputStream is) throws IOException{
        Tidy tidy = new Tidy();
        tidy.setShowErrors(0);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        org.w3c.dom.Document root = tidy.parseDOM(is, null);
        
        Element rawDoc = root.getDocumentElement();

        org.apache.lucene.document.Document doc =
                new org.apache.lucene.document.Document();

        try {
        	String body = getBody(rawDoc);
            String hid = getHeaderOrTitle(rawDoc);         
            
            if ((body != null) && (!body.equals(""))) {
                doc.add(new Field("contents", body, Field.Store.NO, Field.Index.ANALYZED));
                doc.add(new Field("hid", hid, Field.Store.NO, Field.Index.ANALYZED));   
                doc.add(new Field("localurl", this.localurl, Field.Store.NO, Field.Index.ANALYZED));
            }
        }catch(IOException e){
        	//rethrow if there is a problem in parsing -- malformed html
        	System.err.println(e.getMessage());
        	throw e;
        }
        

        return doc;
    }
	protected String getHeaderOrTitle(Element rawDoc) throws IOException{		 
		String title = getTitle(rawDoc);
		if(!title.isEmpty()){
			//System.out.println("Title:"+title);
			return title;
		}
			
		else{
			//System.out.println("title not found");
			String fh = getFirstHeader(rawDoc);
			//System.out.println("firstheader:"+fh);
			
			if(fh.isEmpty())
				throw new IOException("blank html found. do not index");
			return fh;
		}
			
		
	}
    protected String getTitle(Element rawDoc) {
        if (rawDoc == null) {
            return null;
        }

        String title = "";

        NodeList children = rawDoc.getElementsByTagName("title");
        if (children.getLength() > 0) {
            Element titleElement = ((Element) children.item(0));
            Text text = (Text) titleElement.getFirstChild();
            if (text != null) {
                title = text.getData();
            }
        }
        return title;
    }

    protected String getBody(Element rawDoc) throws IOException{
    	
        if (rawDoc == null) {
        	System.out.println("null body");
            return null;
        }

        String body = "";
        NodeList children = rawDoc.getElementsByTagName("body");
        if (children.getLength() > 0) {
            body = getText(children.item(0));
        }
        return body;
    }
    protected String getFirstHeader(Element rawDoc) throws IOException{
    	if (rawDoc == null) {
            return null;
        }

        String fhead="";
        NodeList children = rawDoc.getElementsByTagName("h1");
        if (children.getLength() > 0) {
        	fhead = getText(children.item(0));
        }
        if(!fhead.isEmpty())
        	return fhead;     
        children = rawDoc.getElementsByTagName("h2");
        if (children.getLength() > 0) {
        	//System.out.println("h2 tagcount:"+children.getLength());
        	fhead = getText(children.item(0));        	
        }
        if(!fhead.isEmpty())
        	return fhead;  
        children = rawDoc.getElementsByTagName("h3");
        if (children.getLength() > 0) {
            Element headElement = ((Element) children.item(0));
            Text text = (Text) headElement.getFirstChild();
            if (text != null) {
                fhead = text.getData();
            }
        }
        return fhead; 
    }
    protected String getText(Node node) {
        NodeList children = node.getChildNodes();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            switch (child.getNodeType()) {
                case Node.ELEMENT_NODE:
                    sb.append(getText(child));
                    sb.append(" ");
                    break;
                case Node.TEXT_NODE:
                    sb.append(((Text) child).getData());
                    break;
            }
        }
        return sb.toString();
    }
    
  
}
