package jump.exp.search.crawler4j;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class LuceneCrawler extends WebCrawler {

	private static IndexWriter writer;
	public static Pattern FILTERS = Pattern.compile(".*");
	Logger log= LoggerFactory.getLogger(getClass());

	public LuceneCrawler() throws IOException {
		super();
		if (writer == null) {
			log.info("Constructor");
			String indexPath = "./index";
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			writer = new IndexWriter(dir, iwc);
		}
	}

	/**
	 * This method receives two parameters. The first parameter is the page in
	 * which we have discovered this new url and the second parameter is the new
	 * url. You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic). In this example,
	 * we are instructing the crawler to ignore urls that have css, js, git, ...
	 * extensions and to only accept urls that start with
	 * "http://www.ics.uci.edu/". In this case, we didn't need the referringPage
	 * parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		return FILTERS.matcher(href).matches();
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		log.info("URL: " + url);

		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();
			try {
				index(writer, text, url);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			log.info("Text length: " + text.length());
			log.info("Html length: " + html.length());
			log.info("Number of outgoing links: " + links.size());
		}
	}

	public static void finalized() throws IOException {
		writer.close();
	}

	private void index(IndexWriter writer, String text, String url) throws IOException {
		Document doc = new Document();
		Field pathField = new StringField("path", url, Field.Store.YES);
		doc.add(pathField);
		doc.add(new TextField("contents", text, Field.Store.YES));
		log.info("adding " + url);
		writer.addDocument(doc);
	}
}