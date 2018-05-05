package jump.exp.search.lucene.searcher.lucene;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import jump.exp.search.lucene.searcher.configuration.Configuration;

@Component
public class LuceneSearcher {
	
	static final Logger log=LoggerFactory.getLogger(LuceneSearcher.class);
	
	private String index = "index";
	private String field = "contents";
	QueryParser parser;
	Analyzer analyzer;
	IndexSearcher searcher;
	IndexReader reader;
	
	@Autowired
	public LuceneSearcher(Configuration conf) throws IOException {
		log.debug("--> START INIT LuceneSearcher");
		log.debug("   -indexPath: "+conf.getIndexPath());
		this.index=conf.getIndexPath();
		log.debug("   -searchField: "+conf.getField());
		this.field=conf.getField();
		reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
		searcher = new IndexSearcher(reader);
		analyzer = new StandardAnalyzer();
		parser = new QueryParser(field, analyzer);
		log.debug("<-- END INIT LuceneSearcher");
	}
	
	@Bean
	public QueryParser getQueryParser() {
		return parser;
	}
	
	@Bean
	public IndexSearcher getIndexSearcher() {
		return searcher;
	}
	
	@Bean
	public Analyzer getAnalyzer() {
		return analyzer;
	}
	
	
	public void finalize() throws Throwable{
		log.debug("--> FINALIZE LuceneSearcher");
		reader.close();
	}
	
	
	
}
