package jump.exp.search.lucene.searcher.controller;

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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
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
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Simple command-line based search demo. */
public class SearchWeb {

	static final String FIELD_NAME = "contents";
	static final Logger log=LoggerFactory.getLogger(SearchWeb.class);

	private SearchWeb() {
	}

	/** Simple command-line based search demo. */
	public static void main(String[] args) throws Exception {
		String index = "index";
		String field = "contents";

		String queries = null;
		int repeat = 0;
		boolean raw = false;
		String queryString = null;
		int hitsPerPage = 10;

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

		QueryParser parser = new QueryParser(field, analyzer);
		String usrQuery = "";

		Query query = parser.parse(usrQuery);
		System.out.println("Searching for: " + query.toString(field));

		if (repeat > 0) { // repeat & time as benchmark
			Date start = new Date();
			for (int i = 0; i < repeat; i++) {
				searcher.search(query, 100);
			}
			Date end = new Date();
			log.info("Time: " + (end.getTime() - start.getTime()) + "ms");
		}

		doPagingSearch(in, searcher, analyzer, query, hitsPerPage, raw, queries == null && queryString == null);

		reader.close();
	}

	/**
	 * This demonstrates a typical paging search scenario, where the search engine
	 * presents pages of size n to the user. The user can then go to the next page
	 * if interested in the next hits.
	 * 
	 * When the query is executed for the first time, then only enough results are
	 * collected to fill 5 result pages. If the user wants to page beyond this
	 * limit, then the query is executed another time and all hits are collected.
	 * 
	 * @throws InvalidTokenOffsetsException
	 * 
	 */
	public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Analyzer analyzer, Query query,
			int hitsPerPage, boolean raw, boolean interactive) throws IOException, InvalidTokenOffsetsException {

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
		System.out.println(numTotalHits + " total matching documents");

		int start = 0;
		int end = Math.min(numTotalHits, hitsPerPage);

		while (true) {
			if (end > hits.length) {
				System.out.println("Only results 1 - " + hits.length + " of " + numTotalHits
						+ " total matching documents collected.");
				System.out.println("Collect more (y/n) ?");
				String line = in.readLine();
				if (line.length() == 0 || line.charAt(0) == 'n') {
					break;
				}

				hits = searcher.search(query, numTotalHits).scoreDocs;
			}

			end = Math.min(hits.length, start + hitsPerPage);

			for (int i = start; i < end; i++) {
				if (raw) { // output raw format
					System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);
					continue;
				}

				Document doc = searcher.doc(hits[i].doc);
				String path = doc.get("url");
				if (path != null) {
					System.out.println((i + 1) + ". " + path);
					String title = doc.get("title");
					if (title != null) {
						System.out.println("   Title: " + doc.get("title"));
					}
					// Get stored text from found document
					String text = doc.get(FIELD_NAME);

					// Create token stream
					@SuppressWarnings("deprecation")
					TokenStream stream = TokenSources.getTokenStream(FIELD_NAME, text, analyzer);

					// Get highlighted text fragments
					String[] frags = highlighter.getBestFragments(stream, text, 10);
					for (String frag : frags) {
						System.out.println("=======================");
						System.out.println(frag);
					}
				} else {
					System.out.println((i + 1) + ". " + "No path for this document");
				}

			}

			if (!interactive || end == 0) {
				break;
			}

			if (numTotalHits >= end) {
				boolean quit = false;
				while (true) {
					System.out.print("Press ");
					if (start - hitsPerPage >= 0) {
						System.out.print("(p)revious page, ");
					}
					if (start + hitsPerPage < numTotalHits) {
						System.out.print("(n)ext page, ");
					}
					System.out.println("(q)uit or enter number to jump to a page.");

					String line = in.readLine();
					if (line.length() == 0 || line.charAt(0) == 'q') {
						quit = true;
						break;
					}
					if (line.charAt(0) == 'p') {
						start = Math.max(0, start - hitsPerPage);
						break;
					} else if (line.charAt(0) == 'n') {
						if (start + hitsPerPage < numTotalHits) {
							start += hitsPerPage;
						}
						break;
					} else {
						int page = Integer.parseInt(line);
						if ((page - 1) * hitsPerPage < numTotalHits) {
							start = (page - 1) * hitsPerPage;
							break;
						} else {
							System.out.println("No such page");
						}
					}
				}
				if (quit)
					break;
				end = Math.min(numTotalHits, start + hitsPerPage);
			}
		}
	}
}