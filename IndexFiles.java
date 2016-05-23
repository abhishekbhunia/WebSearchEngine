/**
 * Lucene Version used: 5.4.1
 * Development Platform: Windows, Eclipse
 * Html Parser: Used JTidy source: https://sourceforge.net/projects/jtidy/files/JTidy/r938/jtidy-r938.zip/download
 * 
 * Library: Included only lucene and jtidy archive files(jar) in respective directories
 * 
 * Note: 
 * 1. Modified the default IndexFiles and SearchFiles functions to meet the demands of the assignment
 * 2. For html parsing used this example as a starting point for accessing DOM: (used Lai Xin Chu's answer)
 * 		http://stackoverflow.com/questions/12576119/lucene-indexing-of-html-files
 * 3. Did not attempt extra credit, maintained command line execution scheme as mentioned in the assignment page
 */



/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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


/** Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing.
 * Run it with no command-line arguments for usage information.
 */
public class IndexFiles {
  
  private IndexFiles() {}

  /** Index all text files under a directory. */
  /*
  public static void main(String[] args) {
    String usage = "java org.apache.lucene.demo.IndexFiles"
                 + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                 + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                 + "in INDEX_PATH that can be searched with SearchFiles";
    String indexPath = "index";
    String docsPath = null;
    boolean create = true;
    for(int i=0;i<args.length;i++) {
      if ("-index".equals(args[i])) {
        indexPath = args[i+1];
        i++;
      } else if ("-docs".equals(args[i])) {
        docsPath = args[i+1];
        i++;
      } else if ("-update".equals(args[i])) {
        create = false;
      }
    }

    if (docsPath == null) {
      System.err.println("Usage: " + usage);
      System.exit(1);
    }

    final Path docDir = Paths.get(docsPath);
    if (!Files.isReadable(docDir)) {
      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    
    Date start = new Date();
    try {
      System.out.println("Indexing to directory '" + indexPath + "'...");

      Directory dir = FSDirectory.open(Paths.get(indexPath));
      Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

      if (create) {
        // Create a new index in the directory, removing any
        // previously indexed documents:
        iwc.setOpenMode(OpenMode.CREATE);
      } else {
        // Add new documents to an existing index:
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
      }

      // Optional: for better indexing performance, if you
      // are indexing many documents, increase the RAM
      // buffer.  But if you do this, increase the max heap
      // size to the JVM (eg add -Xmx512m or -Xmx1g):
      //
      // iwc.setRAMBufferSizeMB(256.0);

      IndexWriter writer = new IndexWriter(dir, iwc);
      indexDocs(writer, docDir);

      // NOTE: if you want to maximize search performance,
      // you can optionally call forceMerge here.  This can be
      // a terribly costly operation, so generally it's only
      // worth it when your index is relatively static (ie
      // you're done adding documents to it):
      //
      // writer.forceMerge(1);

      writer.close();

      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e) {
      System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
  }

  
  */
  /**
   * Indexes the given file using the given writer, or if a directory is given,
   * recurses over files and directories found under the given directory.
   * 
   * NOTE: This method indexes one document per input file.  This is slow.  For good
   * throughput, put multiple documents into your input file(s).  An example of this is
   * in the benchmark module, which can create "line doc" files, one document per line,
   * using the
   * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
   * >WriteLineDocTask</a>.
   *  
   * @param writer Writer to the index where the given file/dir info will be stored
   * @param path The file to index, or the directory to recurse into to find files to index
   * @throws IOException If there is a low-level I/O error
   */
  static void indexDocs(final IndexWriter writer, Path path) throws IOException {
    if (Files.isDirectory(path)) {
    	try{
    		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
    	        @Override
    	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    	          try {
    	            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
    	          } catch (IOException ignore) {
    	            // don't index files that can't be read.
    	        	  System.err.println("read error on: " + file.toString());
    	          }
    	          return FileVisitResult.CONTINUE;
    	        }
    	      });
    	} catch(Exception e){
    		
    	}
      
    } else {
    	try{
    		indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
    	} catch(IOException e){
    		System.err.println("read error on: " + path.toString());
    	}
      
    }
  }

  /** Indexes a single document */
  static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
    try (InputStream stream = Files.newInputStream(file)) {
      // make a new, empty document
      //Document doc = new Document();
      
      
    	File f = new File(file.toString());
    	////System.out.println("file length: " + f.length());
    	if(f.length() == 0){
    		System.err.println("0 byte file. exiting\n\n");
    		//return;
    	}
  else{ 
      try{
    	  HtmlParser obj = new HtmlParser(file.toString());
    	  
          Document doc = obj.getDocument(stream);
          
          Field hidField = new StringField("hid", doc.get("hid"), Field.Store.YES);
         
          doc.add(hidField);
          
          Field pathField = new StringField("path", file.toString(), Field.Store.YES);
          doc.add(pathField);
          doc.add(new LongField("modified", lastModified, Field.Store.NO));
          
          
          if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
              // New index, so we just add the document (no old document can be there):
              ////System.out.println("adding " + file);
              writer.addDocument(doc);
          } else {
              // Existing index (an old copy of this document may have been indexed) so 
              // we use updateDocument instead to replace the old one matching the exact 
              // path, if present:
              ////System.out.println("updating " + file);
              writer.updateDocument(new Term("path", file.toString()), doc);
            }
          
          
          
      } catch(IOException e) {
    	  System.err.println("parsing problem -- malformed html: " + e.getMessage());
      }
        
     }
    } catch(IOException e) {
    	System.err.println("error in indexdoc for: " + file.toString());
    }
  }
}