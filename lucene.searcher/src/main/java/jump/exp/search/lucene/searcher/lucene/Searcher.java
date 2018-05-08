package jump.exp.search.lucene.searcher.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import jump.exp.search.lucene.domain.Document;
import jump.exp.search.lucene.domain.SearchResult;
import jump.exp.search.lucene.searcher.configuration.Configuration;

@Component
public class Searcher {

	static final Logger log = LoggerFactory.getLogger(Searcher.class);

	@SuppressWarnings("unused")
	private Configuration conf;
	private QueryParser parser;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private String field;
	
	private int phraseLenght;
	private int phraseNum;

	@Autowired 
	public Searcher(Configuration conf, QueryParser parser, IndexSearcher searcher, Analyzer analyzer) {
		this.conf = conf;
		this.parser = parser;
		this.searcher = searcher;
		this.analyzer = analyzer;
		this.phraseLenght=conf.getPhraseLenght();
		this.phraseNum=conf.getPhraseNum();
		this.field = conf.getField();
		log.debug("queryParser is null? " + (parser == null));
		log.debug("indexSearcher is null? " + (searcher == null));
		log.debug("Analyzer is null? " + (analyzer == null));
	}

	private Highlighter initHighlighter(Query query) {
		// Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
		Formatter formatter = new SimpleHTMLFormatter();
		// It scores text fragments by the number of unique query terms found
		// Basically the matching score in layman terms
		QueryScorer scorer = new QueryScorer(query);

		// used to markup highlighted terms found in the best sections of a text
		Highlighter highlighter = new Highlighter(formatter, scorer);

		// It breaks text up into same-size texts but does not split up spans
		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, phraseLenght);

		// breaks text up into same-size fragments with no concerns over
		// spotting sentence boundaries.
		// Fragmenter fragmenter = new SimpleFragmenter(10);

		// set fragmenter to highlighter
		highlighter.setTextFragmenter(fragmenter);
		return highlighter;
	}

	@Bean
	public Searcher getSearcher() {
		return this;
	}

	public SearchResult doPagingSearch(int hitsPerPage, int page, String usrQuery)
			throws IOException, InvalidTokenOffsetsException, ParseException {
		
		SearchResult result = new SearchResult();
		result.setPage(page);

		Query query = parser.parse(usrQuery);
		log.info("Searching for: " + query.toString(field));
		Highlighter highlighter=initHighlighter(query);
		
		TopDocs results = searcher.search(query, page * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = Math.toIntExact(results.totalHits);
		result.setTotalResult(numTotalHits);

		int start = (page - 1) * hitsPerPage;
		int desiredEnd = page * hitsPerPage;
		int end = Math.min(numTotalHits, desiredEnd);

		end = Math.min(hits.length, start + hitsPerPage);
		
		List<Document> listResult=new ArrayList<>();
		result.setResults(listResult);
		
		for (int i = start; i < end; i++) {
			Document resultDoc=new Document();
			resultDoc.setScore( hits[i].score);
			
			org.apache.lucene.document.Document doc = searcher.doc(hits[i].doc);
			String path = doc.get("url");
			resultDoc.setUrl( path);
			if (path != null) {
				String title = doc.get("title");
				if (title != null) {
					resultDoc.setTitle(title);
				}
				// Get stored text from found document
				String text = doc.get(field);

				// Create token stream
				@SuppressWarnings("deprecation")
				TokenStream stream = TokenSources.getTokenStream(field, text, analyzer);

				// Get highlighted text fragments
				StringBuilder sb=new StringBuilder();
				
				String[] frags = highlighter.getBestFragments(stream, text, 10);
				for (int n=0;n<Math.min(phraseNum, frags.length);n++) {
					sb.append(frags[n] + "... ");
				}
				resultDoc.setSummary(sb.toString());
			}
			listResult.add(resultDoc);
		}
		return result;
	}
}
