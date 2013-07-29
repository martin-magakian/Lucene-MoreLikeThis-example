package com.doduck.luceneMoreLikeThis;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class Main {


	public static void main(String[] args) throws IOException {
		Main m = new Main();
		m.start();
		m.writerEntries();
		m.findSilimar("doduck prototype");
	}
	



	private Directory indexDir;
	private StandardAnalyzer analyzer;
	private IndexWriterConfig config;
	
	public void start() throws IOException{
		analyzer = new StandardAnalyzer(Version.LUCENE_42);
		config = new IndexWriterConfig(Version.LUCENE_42, analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);
		
		indexDir = new RAMDirectory(); //don't write on disk
		//indexDir = FSDirectory.open(new File("/Path/to/luceneIndex/")); //write on disk
	}
	
	public void writerEntries() throws IOException{
		IndexWriter indexWriter = new IndexWriter(indexDir, config);
		indexWriter.commit();
		
		Document doc1 = createDocument("1","doduck","prototype your idea");
		Document doc2 = createDocument("2","doduck","love programming");
		Document doc3 = createDocument("3","We do", "prototype");
		Document doc4 = createDocument("4","We love", "challange");
		indexWriter.addDocument(doc1);
		indexWriter.addDocument(doc2);
		indexWriter.addDocument(doc3);
		indexWriter.addDocument(doc4);
		
		indexWriter.commit();
		indexWriter.forceMerge(100, true);
		indexWriter.close();
	}

	private Document createDocument(String id, String title, String content) {
		FieldType type = new FieldType();
		type.setIndexed(true);
		type.setStored(true);
		type.setStoreTermVectors(true); //TermVectors are needed for MoreLikeThis
		
		Document doc = new Document();
		doc.add(new StringField("id", id, Store.YES));
		doc.add(new Field("title", title, type));
		doc.add(new Field("content", content, type));
		return doc;
	}


	private void findSilimar(String searchForSimilar) throws IOException {
		IndexReader reader = DirectoryReader.open(indexDir);
		IndexSearcher indexSearcher = new IndexSearcher(reader);
		
		MoreLikeThis mlt = new MoreLikeThis(reader);
	    mlt.setMinTermFreq(0);
	    mlt.setMinDocFreq(0);
	    mlt.setFieldNames(new String[]{"title", "content"});
	    mlt.setAnalyzer(analyzer);
	    
	    
	    Reader sReader = new StringReader(searchForSimilar);
	    Query query = mlt.like(sReader, null);
		
	    TopDocs topDocs = indexSearcher.search(query,10);
	    
	    for ( ScoreDoc scoreDoc : topDocs.scoreDocs ) {
	        Document aSimilar = indexSearcher.doc( scoreDoc.doc );
	        String similarTitle = aSimilar.get("title");
	        String similarContent = aSimilar.get("content");
	        
	        System.out.println("====similar finded====");
	        System.out.println("title: "+ similarTitle);
	        System.out.println("content: "+ similarContent);
	    }
	    
	}
	
}
