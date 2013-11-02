package tensyn.se;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

public class SearchService {
	
	private static SearchService instance = null;
	
	public IndexWriter indexWriter;
	
	public UpdateThread updateThread;
	
	private LinkedBlockingQueue<String> poolBlockingQueue;
	
	private TaxonomyWriter taxonomyWriter;
	private TaxonomyReader taxonomyReader;
	
	private SearchService(){}
	
	public static SearchService getInstance(){
		if(instance == null){
			instance = new SearchService();
		}
		return instance;
	}
	
	public void updateOrInsert(String event){
		poolBlockingQueue.add(event);
	}
	
	public IndexReader getReader(){
		try{
			return DirectoryReader.open(this.indexWriter, false);
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
		
	}
	
	class UpdateThread extends Thread{
		
		String event = null;
		
		public void run(){
			try {
				while((event=poolBlockingQueue.poll(100, TimeUnit.MILLISECONDS))!=null){
					//doTask
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	

}
