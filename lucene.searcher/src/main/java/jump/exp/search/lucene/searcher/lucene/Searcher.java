package jump.exp.search.lucene.searcher.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
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

import jump.exp.search.lucene.searcher.configuration.Configuration;

@Component
public class Searcher {
	
	static final Logger log=LoggerFactory.getLogger(Searcher.class);
	
	@SuppressWarnings("unused")
	private Configuration conf;
	private QueryParser parser;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private String field;
	
	@Autowired
	public Searcher(Configuration conf,QueryParser parser, IndexSearcher searcher, Analyzer analyzer) {
		this.conf=conf;
		this.parser=parser;
		this.searcher=searcher;
		this.analyzer=analyzer;
		this.field=conf.getField();
		log.debug("queryParser is null? "+(parser==null));
		log.debug("indexSearcher is null? "+(searcher==null));
		log.debug("Analyzer is null? "+(analyzer==null));
	}
	
	@Bean
	public Searcher getSearcher() {
		return this;
	}
	
	public String doPagingSearch(int hitsPerPage, int page, String usrQuery) throws IOException, InvalidTokenOffsetsException, ParseException {
		String result=null;
		
		Query query = parser.parse(usrQuery);
		log.info("Searching for: " + query.toString(field));
		StringBuffer sb=new StringBuffer();
		
		// Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
		Formatter formatter = new SimpleHTMLFormatter();
		// It scores text fragments by the number of unique query terms found
		// Basically the matching score in layman terms
		QueryScorer scorer = new QueryScorer(query);

		// used to markup highlighted terms found in the best sections of a text
		Highlighter highlighter = new Highlighter(formatter, scorer);

		// It breaks text up into same-size texts but does not split up spans
		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 10);

		// breaks text up into same-size fragments with no concerns over
		// spotting sentence boundaries.
		// Fragmenter fragmenter = new SimpleFragmenter(10);

		// set fragmenter to highlighter
		highlighter.setTextFragmenter(fragmenter);

		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, 5 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = Math.toIntExact(results.totalHits);
		sb.append("<html><body>");
		sb.append("<p>"+numTotalHits + " total matching documents"+"</p>");

		int start = 0;
		int end = Math.min(numTotalHits, hitsPerPage);

			if (end > hits.length) {
				sb.append("<p>"+"Only results 1 - " + hits.length + " of " + numTotalHits
						+ " total matching documents collected."+"</p>");
				hits = searcher.search(query, numTotalHits).scoreDocs;
			}

			end = Math.min(hits.length, start + hitsPerPage);

			for (int i = start; i < end; i++) {
				sb.append("<p>"+"doc=" + hits[i].doc + " score=" + hits[i].score+"</p>");
				Document doc = searcher.doc(hits[i].doc);
				String path = doc.get("url");
				if (path != null) {
					sb.append("<p>"+(i + 1) + ". " + path+"</p>");
					String title = doc.get("title");
					if (title != null) {
						sb.append("<p>"+"   Title: " + doc.get("title")+"</p>");
					}
					// Get stored text from found document
					String text = doc.get(field);

					// Create token stream
					@SuppressWarnings("deprecation")
					TokenStream stream = TokenSources.getTokenStream(field, text, analyzer);

					// Get highlighted text fragments
					String[] frags = highlighter.getBestFragments(stream, text, 10);
					for (String frag : frags) {
						sb.append("<p>"+"======================="+"</p>");
						sb.append("<p>"+frag+"</p>");
					}
				} else {
					sb.append("<p>"+(i + 1) + ". " + "No path for this document\n"+"</p>");
				}
			}
			sb.append("</body></html>");
			result=sb.toString();
			return result;
		}
}
